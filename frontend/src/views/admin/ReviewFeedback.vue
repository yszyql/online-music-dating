<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Phone, Clock, Edit, Calendar } from '@element-plus/icons-vue';

// 导入服务器请求函数
import { getFeedbackListService, updateFeedbackStatusService } from '@/api/admin.js';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);
const tableData = ref([]);
const loading = ref(false);

// 弹窗相关数据
const dialogVisible = ref(false);
const currentFeedback = ref(null);
const omdUserFeedbackId = ref(0);
const replyContent = ref('');

// 监听页码变化
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  fetchMusicList(pageNum.value, pageSize.value);
};

// 监听每页数量变化
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  fetchMusicList(pageNum.value, pageSize.value);
};

// 获取反馈列表
const fetchMusicList = async (pageNum, pageSize) => {
  loading.value = true;
  try {
    const response = await getFeedbackListService(pageNum, pageSize);

    if (response.code === 0) {
      tableData.value = response.data.items || [];
      totalPages.value = response.data.total || 0;
    } else {
      ElMessage.error(response.msg || '获取反馈列表失败');
    }
  } catch (error) {
    console.error('获取反馈列表出错:', error);
    ElMessage.error('获取反馈列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 打开回复弹窗
const openReplyDialog = (feedback) => {
  currentFeedback.value = feedback;
  omdUserFeedbackId.value = feedback.omdUserFeedbackId;
  replyContent.value = ''; // 清空之前的回复内容
  dialogVisible.value = true;
};

// 提交回复
const submitReply = async () => {
  if (!omdUserFeedbackId) return;

  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }

  try {
    // 调用更新反馈状态的API，传入反馈ID和回复内容
    const response = await updateFeedbackStatusService(
        omdUserFeedbackId.value,
        replyContent.value
    );

    if (response.code === 0) {
      ElMessage.success('回复成功');
      dialogVisible.value = false;
      await fetchMusicList(pageNum.value, pageSize.value); // 刷新列表
    } else {
      ElMessage.error('回复失败');
    }
  } catch (error) {
    console.error('回复反馈出错:', error);
    ElMessage.error('回复失败，请重试');
  }
};

// 格式化反馈类型
const formatFeedbackType = (typeCode) => {
  const typeMap = {
    1: '功能建议',
    2: '问题反馈',
    3: '投诉举报',
    4: '其他'
  };
  return typeMap[typeCode] || `未知类型(${typeCode})`;
};

// 根据反馈类型获取标签样式
const getFeedbackTypeStyle = (typeCode) => {
  const styleMap = {
    1: 'success',   // 功能建议-绿色
    2: 'warning',   // 问题反馈-黄色
    3: 'danger',    // 投诉举报-红色
    4: 'primary'    // 其他-蓝色
  };
  return styleMap[typeCode] || 'info';
};

// 页面加载时获取数据
onMounted(() => {
  fetchMusicList(pageNum.value, pageSize.value);
});
</script>

<template>
  <el-card class="feedback-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">用户反馈审核</h1>
        <el-tag type="warning">共 {{ totalPages }} 条反馈</el-tag>
      </div>
    </template>

    <!-- 反馈列表区域 -->
    <div class="feedback-list">
      <!-- 骨架屏（加载中） -->
      <el-skeleton
          v-if="loading"
          :rows="6"
          :cols="1"
          class="skeleton"
      ></el-skeleton>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty
            description="暂无待审核的反馈"
            image-size="120"
        ></el-empty>
      </div>

      <!-- 反馈卡片列表 -->
      <div v-else class="feedback-cards">
        <div
            class="feedback-card"
            v-for="(feedback, index) in tableData"
            :key="feedback.omdUserFeedbackId"
        >
          <div class="card-content">
            <!-- 反馈信息 -->
            <div class="feedback-info">
              <div class="feedback-header">
                <h3 class="feedback-title">
                  <el-tag
                      :type="getFeedbackTypeStyle(feedback.omdUserFeedbackType)"
                  >
                    {{ formatFeedbackType(feedback.omdUserFeedbackType) }}
                  </el-tag>
                </h3>
                <div class="feedback-time">
                  {{ feedback.omdUserFeedbackCreateTime }}
                </div>
              </div>

              <div class="feedback-content">
                <p class="content-text">{{ feedback.omdUserFeedbackContent }}</p>

                <div class="feedback-meta">
                  <span v-if="feedback.omdUserFeedbackContact" class="contact-info">
                    <el-icon><Phone /></el-icon>
                    {{ feedback.omdUserFeedbackContact }}
                  </span>
                  <span class="feedback-status">
                    <el-icon><Clock /></el-icon>
                    {{ feedback.omdUserFeedbackStatus === 0 ? '待处理' : '已回复' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="feedback-actions">
              <el-button
                  type="primary"
                  size="small"
                  @click="openReplyDialog(feedback)"
                  :disabled="feedback.omdUserFeedbackStatus !== 0"
              >
                <el-icon><Edit /></el-icon>
                审核回复
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <template #footer>
      <div class="pagination">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 15, 20, 25]"
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total, sizes"
        />
      </div>
    </template>

    <!-- 回复反馈弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        title="回复用户反馈"
        width="600px"
        :close-on-click-modal="false"
    >
      <div class="reply-dialog">
        <div class="feedback-detail">
          <h3 class="detail-title">
            <el-tag
                :type="currentFeedback ? getFeedbackTypeStyle(currentFeedback.omdUserFeedbackType) : 'info'"
            >
              {{ currentFeedback ? formatFeedbackType(currentFeedback.omdUserFeedbackType) : '' }}
            </el-tag>
          </h3>

          <div class="detail-meta">
            <span class="detail-time">
              <el-icon><Calendar /></el-icon>
              反馈时间 · {{ currentFeedback?.omdUserFeedbackCreateTime }}
            </span>
            <span v-if="currentFeedback?.omdUserFeedbackContact" class="detail-contact">
              <el-icon><Phone /></el-icon>
              {{ currentFeedback?.omdUserFeedbackContact }}
            </span>
          </div>

          <div class="detail-content">
            <p>{{ currentFeedback?.omdUserFeedbackContent }}</p>
          </div>
        </div>

        <div class="reply-section">
          <el-form-item label="回复内容" required>
            <el-input
                v-model="replyContent"
                type="textarea"
                :rows="6"
                placeholder="请输入回复内容..."
                maxlength="500"
            ></el-input>
            <div class="word-count">{{ replyContent.length }}/500</div>
          </el-form-item>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.feedback-container {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.total-count {
  background-color: #f5f7fa;
  color: #606266;
}

.feedback-list {
  padding: 20px;
}

.skeleton {
  width: 100%;
}

.feedback-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}

.feedback-card {
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.feedback-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  flex-direction: column;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px dashed #f0f0f0;
}

.feedback-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.feedback-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.feedback-content {
  padding: 15px;
  flex: 1;
}

.content-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  word-break: break-word;
}

.feedback-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #f0f0f0;
}

.contact-info, .feedback-status {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #606266;
}

.contact-info el-icon, .feedback-status el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.feedback-actions {
  display: flex;
  justify-content: flex-end;
  padding: 12px 15px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.empty-state {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.pagination {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

/* 弹窗样式 */
.reply-dialog {
  padding: 10px 0;
}

.feedback-detail {
  padding: 10px 0 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.detail-title {
  margin: 0 0 15px;
  font-size: 16px;
  font-weight: 500;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
  color: #606266;
  font-size: 13px;
}

.detail-time, .detail-contact {
  display: flex;
  align-items: center;
}

.detail-time el-icon, .detail-contact el-icon {
  margin-right: 5px;
}

.detail-content {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
}

.reply-section {
  padding: 10px 0;
}

.el-textarea {
  width: 100%;
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .feedback-cards {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>