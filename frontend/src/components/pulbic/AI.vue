<script setup>
import { ref, watch, nextTick, onMounted, getCurrentInstance } from 'vue';
import { useAuthStore } from '@/stores/auth';
import {talkWithAIService} from '@/api/ai.js';

// 状态管理
const authStore = useAuthStore();

// 组件状态
const isExpanded = ref(false);
const inputMessage = ref('');
const messages = ref([]);
const isLoading = ref(false);
const loadingText = ref('思考中...');
const messageContainer = ref(null);
const instance = getCurrentInstance();

// 切换聊天窗口显示/隐藏
const toggleChatWindow = () => {
  isExpanded.value = !isExpanded.value;
  // 展开时滚动到底部
  if (isExpanded.value) {
    scrollToBottom();
  }
};

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim();
  if (!content) return;

  // 添加用户消息到列表
  messages.value.push({
    content,
    isUser: true,
    timestamp: new Date()
  });

  inputMessage.value = '';
  isLoading.value = true;
  scrollToBottom();

  try {
    // 调用混元大模型接口
    const response = await talkWithAIService(content);
    console.log('AI回复:', response);

    // 添加AI回复到列表
    messages.value.push({
      content: response.data || '抱歉，未能获取到有效回复',
      isUser: false,
      timestamp: new Date()
    });
  } catch (error) {
    messages.value.push({
      content: `请求失败：${error.message || '未知错误'}`,
      isUser: false,
      isError: true,
      timestamp: new Date()
    });
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      const container = messageContainer.value;
      container.scrollTop = container.scrollHeight;
    }
  });
};

// 监听登录状态，管理消息历史
watch(
    () => authStore.isLoggedIn,
    (newVal, oldVal) => {
      // 登录时初始化消息历史
      if (newVal && !oldVal) {
        // 从本地存储加载当前登录用户的消息历史
        const savedMessages = localStorage.getItem(`ai_chat_${authStore.userId}`);
        if (savedMessages) {
          messages.value = JSON.parse(savedMessages);
        }
      }
      // 登出时清除消息历史（可选）
      else if (!newVal && oldVal) {
        messages.value = [];
      }
    },
    { immediate: true }
);

// 监听消息变化，在登录状态下保存到本地
watch(
    () => messages.value,
    (newMessages) => {
      // 仅在用户登录时保存
      if (authStore.isLoggedIn) {
        localStorage.setItem(
            `ai_chat_${authStore.userId}`,
            JSON.stringify(newMessages)
        );
      }
    },
    { deep: true }
);

// 组件挂载时初始化
onMounted(() => {
  // 初始化消息容器高度
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.style.maxHeight = `calc(100% - ${messageContainer.value.offsetTop}px)`;
    }
  });
});

</script>

<template>
  <!-- 悬浮按钮 -->
  <div
      class="ai-float-button"
      @click="toggleChatWindow"
      :class="{ 'active': isExpanded }"
  >
    <el-icon :size="isExpanded ? 15 : 20">
      <ChatDotRound />
    </el-icon>
  </div>

  <!-- 聊天窗口 -->
  <div
      class="ai-chat-window"
      :class="{ 'visible': isExpanded }"
  >
    <!-- 窗口头部 -->
    <div class="chat-header">
      <h3>智能助手</h3>
      <el-icon class="close-icon" @click="toggleChatWindow">
        <Close />
      </el-icon>
    </div>

    <!-- 聊天内容区域 -->
    <div class="chat-messages" ref="messageContainer">
      <!-- 系统提示 -->
      <div class="system-message">
        您好！我是智能助手，有什么可以帮助您的吗？
      </div>

      <!-- 消息列表 -->
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-item', msg.isUser ? 'user-message' : 'ai-message']"
      >
        <el-avatar :size="32" class="message-avatar">
          <el-icon v-if="msg.isUser"><User /></el-icon>
          <el-icon v-else><Service /></el-icon>
        </el-avatar>
        <div class="message-content">
          {{ msg.content }}
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading-indicator">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <el-input
          v-model="inputMessage"
          placeholder="请输入您的问题..."
          @keyup.enter="sendMessage"
          :disabled="isLoading"
          clearable
      />
      <el-button
          type="primary"
          @click="sendMessage"
          :disabled="!inputMessage.trim() || isLoading"
          size="small"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<style scoped>
/* 悬浮按钮样式 */
.ai-float-button {
  position: fixed;
  right: 30px;
  bottom: 130px;
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  z-index: 9999;
}

.ai-float-button.active {
  background-color: #337ab7;
  transform: scale(0.95);
}

.ai-float-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 聊天窗口样式 */
.ai-chat-window {
  position: fixed;
  right: 30px;
  bottom: 220px;
  width: 380px;
  height: 500px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 9998;
}

.ai-chat-window.visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

/* 窗口头部 */
.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 10px 10px 0 0;
  background-color: #f5f7fa;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.close-icon {
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.close-icon:hover {
  color: #ff4d4f;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: #fafafa;
}

/* 系统消息 */
.system-message {
  align-self: center;
  font-size: 12px;
  color: #666;
  padding: 4px 12px;
  background-color: #f0f0f0;
  border-radius: 12px;
}

/* 消息项 */
.message-item {
  display: flex;
  max-width: 85%;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 8px;
  flex-shrink: 0;
}

.message-content {
  padding: 8px 14px;
  border-radius: 18px;
  line-height: 1.5;
  word-break: break-word;
  font-size: 14px;
}

.user-message .message-content {
  background-color: #409eff;
  color: white;
}

.ai-message .message-content {
  background-color: white;
  border: 1px solid #e5e6eb;
  color: #333;
}

/* 加载指示器 */
.loading-indicator {
  padding: 8px 0;
  align-self: flex-start;
}

/* 输入区域 */
.chat-input-area {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
}

.chat-input-area .el-input {
  flex: 1;
}

/* 滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .ai-chat-window {
    width: calc(100% - 60px);
    height: 450px;
  }
}
</style>