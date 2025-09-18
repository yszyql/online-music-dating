<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 导入服务接口
import { getOmdCommentReportListService } from '@/api/user';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const reportList = ref([]);
const loading = ref(false);
const activeStatus = ref('1'); // 默认显示已审核（1）

// 格式化举报类型
const formatReason = (reason) => {
  const reasonMap = {
    1: '垃圾广告',
    2: '恶意攻击',
    3: '色情低俗',
    4: '违法信息',
    5: '其他违规'
  };
  return reasonMap[reason] || '未知类型';
};

// 获取举报类型标签样式
const getReasonTagType = (reason) => {
  const typeMap = {
    1: 'info',
    2: 'danger',
    3: 'warning',
    4: 'primary',
    5: 'gray'
  };
  return typeMap[reason] || 'info';
};

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true;
  try {
    const response = await getOmdCommentReportListService(
        pageNum.value,
        pageSize.value,
        parseInt(activeStatus.value) // 转换为数字类型（0-未审核，1-已审核）
    );

    if (response.code === 0) {
      reportList.value = response.data.items || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error(response.msg || '获取举报列表失败');
    }
  } catch (error) {
    console.error('获取评论举报列表错误:', error);
    ElMessage.error('获取举报列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 标签页切换（切换已审核/未审核）
const handleStatusChange = () => {
  pageNum.value = 1; // 重置页码
  fetchReportList();
};

// 分页-页码变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchReportList();
};

// 分页-每页条数变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1; // 重置页码
  fetchReportList();
};

// 页面加载时获取数据
onMounted(() => {
  fetchReportList();
});
</script>

<template>
  <el-card class="comment-report-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的评论举报记录</h2>
    </div>

    <!-- 状态筛选标签页 -->
    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="handleStatusChange">
      <el-tab-pane label="已审核" name="1"></el-tab-pane>
      <el-tab-pane label="未审核" name="0"></el-tab-pane>
    </el-tabs>

    <!-- 举报列表 -->
    <div class="report-list">
      <!-- 加载状态 -->
      <el-skeleton v-if="loading" :rows="6" class="report-skeleton" />

      <!-- 空状态 -->
      <div v-else-if="reportList.length === 0" class="empty-state">
        <el-empty description="暂无该状态的评论举报记录" />
      </div>

      <!-- 举报列表 -->
      <el-table
          v-else
          :data="reportList"
          border
          stripe
          :row-key="row => row.omdCommentReportId"
          style="width: 100%"
      >

        <!-- 举报类型 -->
        <el-table-column
            prop="omdCommentReportReason"
            label="举报类型"
            width="140"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="getReasonTagType(scope.row.omdCommentReportReason)">
              {{ formatReason(scope.row.omdCommentReportReason) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 被举报评论内容 -->
        <el-table-column
            label="被举报评论"
            min-width="250"
        >
          <template #default="scope">
            <div class="comment-content">
              {{ scope.row.omdMusicComment?.omdMusicCommentContent || '评论已删除' }}
            </div>
          </template>
        </el-table-column>

        <!-- 举报描述 -->
        <el-table-column
            prop="omdCommentReportDescription"
            label="举报描述"
            min-width="200"
        >
          <template #default="scope">
            <div class="report-description">{{ scope.row.omdCommentReportDescription }}</div>
          </template>
        </el-table-column>

        <!-- 举报状态 -->
        <el-table-column
            prop="omdCommentReportStatus"
            label="状态"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-tag
                :type="scope.row.omdCommentReportStatus === 1 ? 'success' : 'warning'"
            >
              {{ scope.row.omdCommentReportStatus === 1 ? '已审核' : '未审核' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 处理信息 -->
        <el-table-column
            label="处理信息"
            min-width="200"
        >
          <template #default="scope">
            <div v-if="scope.row.omdCommentReportStatus === 1" class="handle-info">
              <p>处理人：{{ scope.row.omdAdminName || '系统处理' }}</p>
              <p>处理时间：{{ scope.row.omdCommentReportHandleTime }}</p>
              <p v-if="scope.row.omdCommentReportRemark">处理备注：{{ scope.row.omdCommentReportRemark }}</p>
            </div>
            <div v-else class="pending-info">待审核处理</div>
          </template>
        </el-table-column>

        <!-- 举报时间 -->
        <el-table-column
            prop="omdCommentReportCreateTime"
            label="举报时间"
            width="180"
            align="center"
        >
          <template #default="scope">
            {{ scope.row.omdCommentReportCreateTime }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
          layout="prev, pager, next, jumper, ->, total"
      />
    </div>
  </el-card>
</template>



<style scoped>
.comment-report-container {
  margin: 20px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.status-tabs {
  margin-bottom: 20px;
}

.report-list {
  margin-bottom: 20px;
}

.report-skeleton {
  width: 100%;
  padding: 10px 0;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
}

/* 表格内容样式 */
.comment-content {
  line-height: 1.6;
  color: #444;
  word-break: break-all;
}

.report-description {
  line-height: 1.6;
  color: #666;
  word-break: break-all;
}

.handle-info {
  line-height: 1.6;
  color: #333;
}

.handle-info p {
  margin: 4px 0;
  font-size: 13px;
}

.pending-info {
  color: #909399;
  font-style: italic;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 表格样式优化 */
::v-deep .el-table {
  border-radius: 4px;
  overflow: hidden;
}

::v-deep .el-table th {
  background-color: #f5f7fa;
  font-weight: 500;
  color: #606266;
}

::v-deep .el-table td {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

::v-deep .el-table__empty-text {
  color: #909399;
}
</style>