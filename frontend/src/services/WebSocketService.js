import SockJS from 'sockjs-client';
import {Stomp} from '@stomp/stompjs';
import {onUnmounted, ref} from 'vue';

// 导入状态管理
import {useAuthStore} from "@/stores/auth.js";

// 创建 SockJS 实例并设置请求头
const socket = new SockJS('http://localhost:8080/chat-websocket');

// 创建 Stomp 客户端实例并连接到 WebSocket
const stompClient = Stomp.over(socket);
stompClient.configure({
    debug: () => {} // 正确的禁用调试日志方式
});

// WebSocket连接状态
const isConnected = ref(false);
// 当前用户ID
let currentUserId = null;
// 订阅的消息
const subscribedMessages = ref([]);
// 错误信息
const errorMessage = ref('');
// 订阅对象
let subscription = null;

// 获取当前用户的AuthStore（动态获取，而非模块顶层）
const getAuthStore = () => {
    return useAuthStore();
};


// 连接WebSocket
export const connectWebSocket = (userId) => {
    return new Promise((resolve, reject) => {
        if (!userId) {
            reject(new Error('用户ID不能为空'));
            return;
        }

        currentUserId = userId; // 保存当前用户ID

        if (stompClient.connected) {
            resolve();
            return;
        }

        // 动态获取AuthStore实例并获取token
        const authStore = getAuthStore();
        const token = authStore.token;

        stompClient.connect(
            { Authorization: `Bearer ${token}` }, // 在connect时设置token
            () => {
                isConnected.value = true;
                console.log('WebSocket连接成功');
                resolve();
            },
            (error) => {
                isConnected.value = false;
                errorMessage.value = error?.headers?.message || 'WebSocket连接失败';
                console.error('WebSocket连接错误:', error);
                reject(error);
            }
        );
    });
};

// 断开WebSocket连接
export const disconnectWebSocket = () => {
    if (subscription) {
        subscription.unsubscribe();
        subscription = null;
    }

    if (stompClient.connected) {
        stompClient.disconnect(() => {
            isConnected.value = false;
            console.log('WebSocket已断开连接');
        });
    }
};

// 订阅聊天消息
export const subscribeToChat = (friendUserId, callback) => {
    if (!stompClient.connected || !currentUserId) { // 使用currentUserId变量
        console.error('未连接WebSocket或用户ID不存在');
        return;
    }

    // 取消之前的订阅
    if (subscription) {
        subscription.unsubscribe();
    }

    // 订阅新的消息
    subscription = stompClient.subscribe(`/user/${currentUserId}/queue/friend`, (message) => {
        try {
            const data = JSON.parse(message.body);
            console.log('收到新消息:', data);

            // 如果提供了回调函数，则调用它
            if (typeof callback === 'function') {
                callback(data);
            }

            // 将消息添加到订阅的消息列表中
            subscribedMessages.value.push(data);
        } catch (error) {
            console.error('解析消息失败:', error);
        }
    });
};

// 发送聊天消息
export const sendChatMessage = (friendUserId, content) => {
    return new Promise((resolve, reject) => {
        // 确认连接状态
        console.log('WebSocket连接状态:', stompClient.connected);
        if (!stompClient.connected) {
            reject(new Error('WebSocket未连接'));
            return;
        }

        // 打印发送的消息内容
        const message = {
            omdUserId: currentUserId,
            omdFriendUserId: friendUserId,
            omdMessageContent: content,
        };
        console.log('准备发送的消息:', message);

        try {
            stompClient.send(
                '/app/sendMessage',
                { Authorization: `Bearer ${getAuthStore().token}` },
                JSON.stringify(message)
            );
            console.log('消息已发送至 STOMP 代理');
            resolve(message);
        } catch (error) {
            console.error('发送消息失败:', error);
            reject(error);
        }
    });
};

// 获取历史聊天记录
export const fetchChatHistory = async (friendUserId, pageNum = 1, pageSize = 20) => {
    try {
        // 动态获取 authStore
        const authStore = getAuthStore();
        const response = await fetch(`/api/friend/messages/${friendUserId}?pageNum=${pageNum}&pageSize=${pageSize}`, {
            headers: {
                'Authorization': `Bearer ${authStore.token}` // 修正：使用动态获取的 token
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP错误，状态码: ${response.status}`);
        }

        const data = await response.json();
        if (data.code === 0) {
            return data.data;
        } else {
            throw new Error(data.msg || '获取聊天记录失败');
        }
    } catch (error) {
        console.error('获取聊天记录出错:', error);
        throw error;
    }
};

// 在组件中使用的组合式函数
export const webSocketChat = async (friendUserId) => {
    const messages = ref([]);
    const loading = ref(false);
    const error = ref('');
    let currentSubscription = null; // 当前订阅对象

    // 连接并订阅消息
    const connectAndSubscribe = async () => {
        if (!friendUserId) return;

        try {
            loading.value = true;
            // 动态获取 authStore
            const authStore = getAuthStore();
            const userId = authStore.userId;

            if (!userId) {
                throw new Error('用户未登录');
            }

            // 连接WebSocket
            await connectWebSocket(userId);

            // 取消之前的订阅
            if (currentSubscription) {
                currentSubscription.unsubscribe();
            }

            // 订阅当前好友的消息
            currentSubscription = subscribeToChat(friendUserId, (newMessage) => {
                if (newMessage.omdFriendUserId === friendUserId || newMessage.omdUserId === friendUserId) {
                    messages.value.push(newMessage);
                }
            });

            // 获取历史消息
            messages.value = await fetchChatHistory(friendUserId);
        } catch (err) {
            error.value = err.message;
        } finally {
            loading.value = false;
        }
    };

    // 发送消息（自动关联当前friendUserId）
    const sendMessage = async (content) => {
        if (!content.trim() || !friendUserId) return;
        try {
            const sentMsg = await sendChatMessage(friendUserId, content);
            // 本地先显示发送的消息（等待后端回显确认）
            messages.value.push({
                ...sentMsg,
                formMe: true, // 标记为发送者
                sent: false // 标记为待发送
            });
            return sentMsg;
        } catch (err) {
            error.value = err.message;
            throw err;
        }
    };

    // 初始化连接
    await connectAndSubscribe();

    // 组件卸载时清理
    onUnmounted(() => {
        if (currentSubscription) {
            currentSubscription.unsubscribe();
        }
        // 不需要断开全局连接，避免频繁重连（仅取消订阅）
    });

    return {
        messages,
        loading,
        error,
        sendMessage
    };
};