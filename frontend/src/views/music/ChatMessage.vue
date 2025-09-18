<script setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from "element-plus";
import {Promotion} from "@element-plus/icons-vue";
import {format} from 'date-fns';
import {useRouter} from "vue-router";

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入状态管理
import {useAuthStore} from "@/stores/auth.js";

// 导入自定义组件
import ReportUserDialog from "@/components/message/ReportUserDialog.vue";

// 导入服务器请求
import {
  blackFriendService, deleteFriendService,
  getChatMessagesService,
  getFriendIsOnlineService,
  getFriendListByOmdUserIdService
} from "@/api/friend.js";

// 导入WebSocket消息服务
import {webSocketChat} from "@/services/WebSocketService.js";
import {getOmdUserRoleListService} from "@/api/user.js";

// 初始化路由实例
const router = useRouter();

// 初始化状态管理实例
const authStore = useAuthStore();

// 响应式数据
const pageNum = ref(1); // 当前页码
const pageSize = ref(10); // 每页显示数量
const hasMore = ref(true); // 是否有更多数据
const isLoading = ref(false); // 是否正在加载
const friends = ref([]); // 好友数据
const searchKeyword = ref(''); // 搜索关键词
const currentChatFriend = ref(null); // 当前选中的好友
const currentFriendId = ref(null); // 当前选中好友的ID
const friendOnlineStatus = ref({}); // 存储好友在线状态（key: 好友ID，value: 在线状态）
const messageInput = ref(''); // 消息输入框内容
const messageScrollbar = ref(null); // 消息滚动条引用
const isFriendListCollapsed = ref(false); // 控制好友列表折叠状态
const chatPageNum = ref(1); // 聊天消息页码
const chatHasMore = ref(true); // 聊天消息是否有更多
const chatMessages = ref([]);
const loading = ref(false);
const error = ref('');
const sendMessage = ref(() => {}); // 初始化为空函数
const reportPopupVisible = ref(false); // 弹窗显示状态

// 初始化WebSocket（根据当前好友ID）
const initWebSocket = async (friendId) => {
  if (!friendId) return;
  currentFriendId.value = friendId;

  try {
    const wsInstance = await webSocketChat(friendId);
    // 确保这些变量已初始化
    chatMessages.value = wsInstance.messages.value || [];
    loading.value = wsInstance.loading.value || false;
    error.value = wsInstance.error.value || '';
    sendMessage.value = wsInstance.sendMessage;
  } catch (err) {
    console.error('WebSocket 初始化失败:', err);
    error.value = err.message;
  }
};

// 选择好友时重新初始化WebSocket
const selectFriend = (friend) => {
  currentChatFriend.value = friend;

  // 重置消息状态
  chatMessages.value = [];
  chatPageNum.value = 1;
  chatHasMore.value = true;

  // 初始化WebSocket（已包含获取历史消息的逻辑）
  initWebSocket(friend.omdFriendUserId);
};

// 获取好友数据
const fetchFriends = async (loadMore = false) => {
  if (!loadMore) {
    pageNum.value = 1;
    hasMore.value = true;
    friends.value = [];
  }

  if (isLoading.value || !hasMore.value) return;

  isLoading.value = true;

  try {
    const response = await getFriendListByOmdUserIdService(pageNum.value, pageSize.value, 1);
    const newFriends = response.data.items || [];

    if (loadMore) {
      friends.value = [...friends.value, ...newFriends];
    } else {
      friends.value = newFriends;
    }

    // 更新总页数和是否有更多数据
    hasMore.value = (pageNum.value * pageSize.value) < response.data.total;

    // 自动选择第一个好友
    if (friends.value.length > 0 && !currentChatFriend.value) {
      selectFriend(friends.value[0]);
    }

    pageNum.value++;
  } catch (error) {
    console.error('获取好友列表失败:', error);
  } finally {
    isLoading.value = false;
  }
};

// 批量获取好友在线状态
const fetchFriendOnlineStatus = async () => {
  if (friends.value.length === 0) return;
  for (const friend of friends.value) {
    try {
      friendOnlineStatus.value[friend.omdFriendUserId] = await getFriendIsOnlineService(friend.omdFriendUserId);
    } catch (err) {
      console.error(`获取好友${friend.omdFriendUserId}在线状态失败`, err);
      friendOnlineStatus.value[friend.omdFriendUserId] = false;
    }
  }
};

// // 获取聊天消息
const fetchChatMessages = async (friendId, loadMore = false) => {
  if (!friendId) return;

  if (!loadMore) {
    chatPageNum.value = 1;
    chatHasMore.value = true;
    chatMessages.value = [];
  }

  try {
    const response = await getChatMessagesService(
        friendId,
        chatPageNum.value,
        pageSize.value,
    );

    const newMessages = response.data || [];

    if (loadMore) {
      // 将新消息添加到前面
      chatMessages.value = [...newMessages, ...chatMessages.value];
    } else {
      chatMessages.value = newMessages;
    }
    chatHasMore.value = newMessages.length === pageSize.value;
    chatPageNum.value++;

    // 如果不是加载更多，滚动到底部
    if (!loadMore) {
      nextTick(scrollToBottom);
    }
  } catch (error) {
    console.error('获取聊天消息失败:', error);
  }
};

// 处理发送消息
const handleSend = async () => {
  if (!messageInput.value.trim() || !currentChatFriend.value) return;

  try {
    // 调用WebSocket的sendMessage，传入内容即可（已关联当前好友）
    await sendMessage.value(messageInput.value.trim());
    messageInput.value = ''; // 清空输入框
    scrollToBottom(); // 滚动到底部
    await fetchChatMessages(currentChatFriend.value.omdFriendUserId); // 刷新消息列表
  } catch (err) {
    ElMessage.error('发送失败：' + err.message);
  }
};

// 监听滚动事件实现无限滚动
const handleFriendListScroll = (event) => {
  const el = event.target;
  const scrollTop = el.scrollTop;
  const scrollHeight = el.scrollHeight;
  const clientHeight = el.clientHeight;

  // 距离底部50px时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50 && hasMore.value && !isLoading.value) {
    fetchFriends(true);
  }
};

// 监听聊天消息滚动事件
const handleMessageScroll = (event) => {
  const el = event.target;
  const scrollTop = el.scrollTop;

  // 滚动到顶部时加载更多历史消息
  if (scrollTop < 100 && chatHasMore.value && !isLoading.value) {
    fetchChatMessages(currentChatFriend.value?.id, true);
  }
};

// 搜索好友
const searchFriends = () => {
  fetchFriends(false);
};

// 过滤好友列表
const filteredFriends = computed(() => {
  if (!searchKeyword.value.trim()) {
    return friends.value;
  }
  const keyword = searchKeyword.value.toLowerCase().trim();
  return friends.value.filter(friend => {
    // 修正：使用好友用户的昵称或用户名过滤
    const friendName = friend.omdFriendUser?.omdUserNickname || friend.omdFriendUser?.omdUserName || '';
    return friendName.toLowerCase().includes(keyword);
  });
});

// 滚动到消息底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageScrollbar.value) {
      const scrollContent = messageScrollbar.value.$el.querySelector('.el-scrollbar__view');
      if (scrollContent) {
        scrollContent.scrollTop = scrollContent.scrollHeight;
      }
    }
  });
};

// 切换好友列表显示/隐藏
const toggleFriendList = () => {
  isFriendListCollapsed.value = !isFriendListCollapsed.value;
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';

  // 提取消息时间的年月日部分（格式：YYYY-MM-DD）
  const messageDateStr = time.split(' ')[0];
  // 获取当前日期的年月日部分
  const now = new Date();
  const nowYear = now.getFullYear();
  const nowMonth = String(now.getMonth() + 1).padStart(2, '0');
  const nowDay = String(now.getDate()).padStart(2, '0');
  const todayDateStr = `${nowYear}-${nowMonth}-${nowDay}`;

  // 比较日期字符串
  if (messageDateStr === todayDateStr) {
    return format(new Date(time), 'HH:mm'); // 今天的消息显示时分
  }

  // 计算昨天的日期字符串
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  const yesterdayYear = yesterday.getFullYear();
  const yesterdayMonth = String(yesterday.getMonth() + 1).padStart(2, '0');
  const yesterdayDay = String(yesterday.getDate()).padStart(2, '0');
  const yesterdayDateStr = `${yesterdayYear}-${yesterdayMonth}-${yesterdayDay}`;

  if (messageDateStr === yesterdayDateStr) {
    return '昨天'; // 昨天的消息显示"昨天"
  }

  // 其他日期显示月/日（如：06-25）
  return messageDateStr;
};

// 拉黑
const blackFriend = async () => {
  ElMessageBox.confirm(
      '确定要拉黑该用户吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    // 调用拉黑接口
    try {
      await blackFriendService(currentChatFriend?.omdFriendUserId);
      ElMessage.success('拉黑成功');
      // 刷新好友
      await fetchFriends();
    }catch (error) {
      ElMessage.error(error);
    }
  })
};

// 删除好友
const deleteFriend = async () => {
  ElMessageBox.confirm(
      '确定要删除该用户吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    // 调用删除接口
    try {
      await deleteFriendService(currentChatFriend?.omdFriendUserId);
      ElMessage.success('删除成功');
      // 刷新好友
      await fetchFriends();
    }catch (error) {
      ElMessage.error(error);
    }
  })
};

// 打开举报弹窗
const openReportPopup = () => {
  // 打开举报弹窗
  ElMessageBox.confirm(
      '确定要举报该用户吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    // 打开举报弹窗
    reportPopupVisible.value = true;
  })
};

// 举报成功回调
const handleReportSuccess = async () => {
  await fetchFriends();
};

// 跳转到用户基本信息页面
const goToUserInfo = async (omdUserId) => {
  if (!omdUserId) {
    console.error('用户ID不存在，无法跳转');
    return;
  }

  const result = await getOmdUserRoleListService(omdUserId);
  const isSinger = result.data.some(role => role.omdRoleCode === 'ROLE_SINGER');

  if (isSinger) {
    router.push({
      path: `/introduction/singerDetail/${omdUserId}`
    });
  }else {

    router.push({
      path: `/introduction/userDetail/${omdUserId}`
    });
  }

};

// 组件中 watch 函数修改
watch(currentChatFriend, async (newVal) => {
  if (newVal) {
    await fetchChatMessages(currentChatFriend.value?.omdFriendUserId);
  }
});

// 页面挂载时初始化（如果有默认好友）
onMounted(async () => {
  await fetchFriends();
  if (friends.value.length > 0) {
    selectFriend(friends.value[0]); // 选中第一个好友
  }
  // 获取好友在线状态
  await fetchFriendOnlineStatus();
  // 如果有默认好友，获取其聊天消息
  await fetchChatMessages(currentChatFriend.value?.omdFriendUserId);
  // 初始化WebSocket
  await initWebSocket(currentChatFriend.value?.omdFriendUserId);
  // 定时更新在线状态
  setInterval(fetchFriendOnlineStatus, 300000000);
});

</script>

<template>
  <div class="chat-container">
    <!-- 左侧：好友列表 -->
    <div class="friend-list-container" :class="{ 'friend-list-collapsed': isFriendListCollapsed }">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索好友"
            clearable
            prefix-icon="Search"
            @keyup.enter="searchFriends"
            v-if="!isFriendListCollapsed"
        ></el-input>
      </div>

      <!-- 好友列表 -->
      <div class="friends" @scroll="handleFriendListScroll">
        <el-scrollbar>
          <el-card
              v-for="friend in filteredFriends"
              :key="friend.omdFriendId"
              :class="{ 'active-friend': currentChatFriend?.omdFriendUserId  === friend.omdFriendUserId }"
              shadow="never"
              @click="selectFriend(friend)"
          >
            <template #header>
              <div class="friend-header">
                <el-avatar :size="40" :src="friend.omdFriendUser.omdUserAvatar || defaultAvatar"></el-avatar>
                <div class="friend-info">
                  <div :class="{ 'active-name': currentChatFriend?.omdFriendUserId  === friend.omdFriendUserId }">
                    <h3 class="friend-name">{{ friend.omdFriendUser.omdUserName }}</h3>
                  </div>
                  <p class="friend-status" :class="{ 'online': friendOnlineStatus[friend.omdFriendUserId]  }">
                    {{ friendOnlineStatus[friend.omdFriendUserId]  ? '在线' : '离线' }}
                  </p>
                </div>
                <div class="unread-badge" v-if="friend.unreadMessageCount > 0">
                  {{ friend.unreadMessageCount }}
                </div>
              </div>
            </template>
            <div class="friend-last-message">
              <span class="message-content">{{ friend.lastMessage?.omdMessageContent || '暂无消息' }}</span>
              <span class="message-time">{{ formatTime(friend.lastMessage?.omdMessageSendTime) }}</span>
            </div>
          </el-card>

          <!-- 加载更多提示 -->
          <div v-if="isLoading" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="!hasMore" class="no-more-data">
            没有更多好友了
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- 右侧：聊天框 -->
    <div class="chat-box-container" :class="{ 'full-width': isFriendListCollapsed }">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <el-button
            class="toggle-friend-list"
            type="text"
            @click="toggleFriendList"
        >
          <el-icon v-if="isFriendListCollapsed"><ArrowRight /></el-icon>
          <el-icon v-else><ArrowLeft /></el-icon>
        </el-button>
        <div v-if="currentChatFriend" class="current-chat-info">
          <el-avatar :size="36" :src="currentChatFriend.omdFriendUser.omdUserAvatar || defaultAvatar"></el-avatar>
          <div class="chat-info-text" @click="goToUserInfo(currentChatFriend.omdFriendUserId)">
            <div class="chat-info-text-h2">
              <h2>{{ currentChatFriend.omdFriendUser.omdUserName }}</h2>
            </div>
            <p class="chat-status" :class="{ 'online': friendOnlineStatus[currentChatFriend?.omdFriendUserId] }">
              {{ friendOnlineStatus[currentChatFriend?.omdFriendUserId] ? '在线' : '离线'}}
            </p>
          </div>
        </div>
        <div v-else class="no-chat-selected">
          <h2>选择好友开始聊天</h2>
        </div>
        <el-button v-if="currentChatFriend" style="margin-left: 600px" size="small" type="warning"  @click="blackFriend">拉黑</el-button>
        <el-button v-if="currentChatFriend" style="margin-left: 20px" type="danger" size="small" @click="openReportPopup">举报</el-button>
        <el-button v-if="currentChatFriend" style="margin-left: 20px" type="info" size="small" @click="deleteFriend">删除</el-button>
      </div>

      <!-- 聊天内容区域 -->
      <div class="chat-messages" @scroll="handleMessageScroll">
        <el-scrollbar ref="messageScrollbar">
          <!-- 加载历史消息提示 -->
          <div v-if="chatHasMore && currentChatFriend" class="load-history-hint" @click="fetchChatMessages(currentChatFriend?.omdFriendUserId, true)">
            <el-icon><ArrowDown /></el-icon>
            <span>加载更多历史消息</span>
          </div>

          <!-- 消息列表 -->
          <div
              v-for="message in chatMessages"
              :key="message.omdMessageId"
              class="message-item"
              :class="{ 'right': message.omdUserId === authStore.userId  }"
          >
            <el-avatar :size="32" v-if="message.omdUserId === authStore.userId" :src="authStore.userInfo.omdUserAvatar || defaultAvatar"></el-avatar>
            <el-avatar :size="32" v-else :src="currentChatFriend?.omdFriendUser.omdUserAvatar || defaultAvatar"></el-avatar>
            <div class="message-content-wrapper">
              <!-- 消息状态 -->
              <div class="message-status" v-if="message.omdUserId === authStore.userId" >
                <p v-if="message.omdMessageReadStatus"><el-icon ><Check /></el-icon><span>已读</span></p>
                <p v-if="!message.omdMessageReadStatus" style="color: #ff4d4f"><el-icon><Promotion /></el-icon><span>未读</span></p>
              </div>
              <div class="message-bubble" :class="{ 'mine': message.omdUserId === authStore.userId }">
                <p>{{ message.omdMessageContent }}</p>
                <span class="message-time">{{ formatTime(message.omdMessageSendTime) }}</span>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 输入区域 -->
      <div v-if="currentChatFriend" class="chat-input-area">
        <el-input
            v-model="messageInput"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 3 }"
            placeholder="输入消息..."
            @keyup.enter="handleSend"
        ></el-input>

        <el-button
            class="send-button"
            type="primary"
            round
            :disabled="!messageInput.trim() || !currentChatFriend"
            @click="handleSend"
        >
          发送
        </el-button>
      </div>
      <div v-else class="chat-input-area empty">
        <p>请先选择一个好友开始聊天</p>
      </div>
    </div>
  </div>

  <!-- 引入举报弹窗组件 -->
  <ReportUserDialog
      :reported-user-id="currentChatFriend?.omdFriendUserId"
      :visible="reportPopupVisible"
      @update:visible="reportPopupVisible = $event"
      @report-success="handleReportSuccess"
  />

</template>

<style scoped>
.chat-container {
  display: flex;
  height: 60vh;
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin: 10px 0;
}

.friend-list-container {
  width: 320px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  background-color: #fff;
}

.friend-list-collapsed {
  transform: translateX(-100%);
  width: 0;
  border-right: none;
}

.search-box {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.friends {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.friends .el-card {
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.friends .el-card:hover {
  background-color: #f5f7fa;
}

.active-friend {
  background-color: #ecf5ff !important;
  border: 1px solid #d9ecff !important;
}

.friend-header {
  display: flex;
  align-items: center;
}

.friend-info {
  margin-left: 12px;
  flex: 1;
}

.active-name {
  font-weight: 600;
  color: #409eff;
}

.friend-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.friend-status {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.friend-status.online {
  color: #67c23a;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  font-size: 12px;
  padding: 0 6px;
}

.friend-last-message {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.message-content {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.message-time {
  color: #c0c4cc;
}

.chat-box-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  transition: all 0.3s ease;
}

.full-width {
  width: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.toggle-friend-list {
  margin-right: 15px;
  font-size: 18px;
}

.current-chat-info {
  display: flex;
  align-items: center;
}

.chat-info-text {
  margin-left: 12px;
}

.chat-info-text h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.chat-info-text-h2{
  display: flex;
}

.chat-info-text-h2 :hover{
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s ease;
}

.chat-status {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.chat-status.online {
  color: #67c23a;
}

.no-chat-selected {
  flex: 1;
  text-align: center;
  color: #909399;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

.date-separator {
  text-align: center;
  margin: 20px 0;
  position: relative;
  color: #909399;
  font-size: 12px;
}

.date-separator:before,
.date-separator:after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background-color: #dcdfe6;
}

.date-separator:before {
  left: 0;
}

.date-separator:after {
  right: 0;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-item.right {
  flex-direction: row-reverse;
}

.message-item.left {
  flex-direction: row-reverse;
  text-align: right;
}

.message-content-wrapper {
  display: flex;
  align-items: flex-end;
  max-width: 70%;
}

.message-bubble {
  position: relative;
  padding: 10px 15px;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  margin: 0 10px;
}

.message-bubble.mine {
  background-color: #d0e8ff;
}

.message-bubble p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 10px;
  color: #909399;
  margin-top: 4px;
  display: block;
  text-align: right;
}

.message-status {
  font-size: 12px;
  color: #67c23a;
  display: flex; /* 横向排列子元素 */
  align-items: center; /* 垂直居中对齐 */
  gap: 8px; /* 图标和文字之间的间距 */
}

/* 修复嵌套el-icon导致的样式问题 */
.message-status .el-icon {
  display: inline-flex; /* 确保图标 inline 显示 */
  vertical-align: middle;
}

/* 消息气泡：避免被状态挤压 */
.message-bubble {
  flex: 1; /* 占满剩余空间 */
}

.chat-input-area {
  padding: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: flex-end;
}

.chat-input-area.empty {
  justify-content: center;
  color: #909399;
}

.send-button {
  margin-left: 10px;
  height: 36px;
}

.loading-more,
.no-more-data {
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 12px;
}

.loading-more .el-icon {
  animation: rotating 2s linear infinite;
  margin-right: 5px;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.load-history-hint {
  text-align: center;
  padding: 8px;
  background-color: #f0f2f5;
  color: #409eff;
  font-size: 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.load-history-hint .el-icon {
  margin-right: 5px;
}
</style>