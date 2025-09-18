<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 导入服务器请求函数
import {
  getCommentReportListByStatusService,
  updateCommentReportStatusService
} from '@/api/admin.js';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);
const tableData = ref([]);
const loading = ref(false);

// 审核弹窗相关
const dialogVisible = ref(false);
const currentReportId = ref(null);
const reviewStatus = ref(1); // 1-确认举报 2-驳回举报
const reviewReason = ref('');
const dialogTitle = ref('');

// 监听页码变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchReportList();
};

// 监听每页数量变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchReportList();
};

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true;
  try {
    const response = await getCommentReportListByStatusService(
        pageNum.value,
        pageSize.value
    );

    if (response.code === 0) {
      tableData.value = response.data.items || [];
      totalPages.value = response.data.total || 0;
    } else {
      ElMessage.error('获取举报列表失败');
    }
  } catch (error) {
    console.error('获取举报列表出错:', error);
    ElMessage.error('获取举报列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 显示审核弹窗
const showReviewDialog = (reportId, status) => {
  currentReportId.value = reportId;
  reviewStatus.value = status;
  reviewReason.value = '';
  dialogTitle.value = status === 1 ? '确认举报' : '驳回举报';
  dialogVisible.value = true;
};

// 确认审核结果
const confirmReview = async () => {
  if (!currentReportId.value || !reviewReason.value.trim()) return;

  try {
    const response = await updateCommentReportStatusService(
        currentReportId.value,
        reviewStatus.value,
        reviewReason.value
    );

    if (response.code === 0) {
      ElMessage.success(
          reviewStatus.value === 1 ? '举报处理成功' : '举报已驳回'
      );
      dialogVisible.value = false;
      await fetchReportList();
    } else {
      ElMessage.error('操作失败');
    }
  } catch (error) {
    console.error('处理举报出错:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 格式化举报原因
const formatReportReason = (reasonCode) => {
  const reasonMap = {
    1: '垃圾广告',
    2: '恶意攻击',
    3: '色情内容',
    4: '违法信息',
    5: '其他'
  };
  return reasonMap[reasonCode] || `未知原因(${reasonCode})`;
};

// 根据举报原因获取标签类型
const getReasonType = (reasonCode) => {
  const typeMap = {
    1: 'info',
    2: 'danger',
    3: 'warning',
    4: 'error',
    5: 'primary'
  };
  return typeMap[reasonCode] || 'info';
};

// 页面加载时获取数据
onMounted(() => {
  fetchReportList();
});
</script>

<template>
  <el-card class="report-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">评论举报审核</h1>
        <el-tag type="warning" class="total-count">共 {{ totalPages }} 条</el-tag>
      </div>
    </template>

    <!-- 举报列表区域 -->
    <div class="report-list">
      <!-- 骨架屏（加载中） -->
      <el-skeleton
          v-if="loading"
          :rows="5"
          :cols="1"
          class="skeleton"
      ></el-skeleton>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty
            description="暂无待审核的举报"
            image-size="120"
        ></el-empty>
      </div>

      <!-- 举报卡片列表 -->
      <div v-else class="report-cards">
        <div
            class="report-card"
            v-for="(report, index) in tableData"
            :key="report.omdCommentReportId"
        >
          <div class="card-content">
            <!-- 举报信息 -->
            <div class="report-info">
              <div class="report-header">
                <h3 class="report-title">
                  <el-tag
                      :type="getReasonType(report.omdCommentReportReason)"
                      size="small"
                  >
                    {{ formatReportReason(report.omdCommentReportReason) }}
                  </el-tag>
                </h3>
                <div class="report-time">
                  {{ report.omdCommentReportCreateTime }}
                </div>
              </div>

              <div class="report-details">
                <p class="report-description">
                  <span class="label">举报描述：</span>
                  {{ report.omdCommentReportDescription }}
                </p>

                <div class="reported-comment">
                  <span class="label">被举报评论：</span>
                  <div class="comment-content">
                    {{ report.omdMusicComment?.omdMusicCommentContent || '评论已删除' }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="report-actions">
              <el-button
                  type="primary"
                  size="small"
                  @click="showReviewDialog(report.omdMusicCommentId, 1)"
              >
                确认举报
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="showReviewDialog(report.omdMusicCommentId, 2)"
              >
                驳回举报
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

    <!-- 审核弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="450px"
        :close-on-click-modal="false"
    >
      <div class="review-content">
        <el-form-item
            label="审核原因"
            required
            :label-width="90"
        >
          <el-input
              v-model="reviewReason"
              type="textarea"
              :rows="4"
              placeholder="请输入审核原因（必填）"
              maxlength="200"
          ></el-input>
          <div class="word-count">{{ reviewReason.length }}/200</div>
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            :type="reviewStatus === 1 ? 'primary' : 'danger'"
            @click="confirmReview"
            :disabled="!reviewReason.trim()"
        >
          {{ reviewStatus === 1 ? '确认举报' : '驳回举报' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>



<style scoped>
.report-container {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.total-count {
  font-size: 14px;
}

.report-list {
  padding: 15px;
}

.skeleton {
  width: 100%;
}

.report-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 15px;
}

.report-card {
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.report-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  flex-direction: column;
}

.report-info {
  padding: 15px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.report-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.report-time {
  font-size: 13px;
  color: #909399;
}

.report-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.report-description, .reported-comment {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.label {
  font-weight: 500;
  color: #303133;
  margin-right: 5px;
}

.comment-content {
  background-color: #f5f7fa;
  padding: 8px 10px;
  border-radius: 4px;
  margin-top: 5px;
  white-space: pre-wrap;
  word-break: break-word;
}

.report-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 10px 15px;
  border-top: 1px solid #ebeef5;
  background-color: #fafafa;
}

.empty-state {
  padding: 40px 0;
}

.pagination {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

/* 弹窗样式 */
.review-content {
  padding: 10px 0;
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>