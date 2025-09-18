<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getUserFeedbackListService } from '@/api/user';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const feedbackList = ref([]);
const loading = ref(false);
const activeStatus = ref('1'); // 默认显示已审核（1）

// 格式化反馈类型
const formatType = (type) => {
  const typeMap = {
    1: '功能建议',
    2: '问题反馈',
    3: '投诉举报',
    4: '其他',
  };
  return typeMap[type] || '未知类型';
};

// 获取反馈类型标签样式
const getTypeTag = (type) => {
  const typeMap = {
    1: 'primary',
    2: 'warning',
    3: 'danger',
    4: 'info',
  };
  return typeMap[type] || 'info';
};

// 获取反馈列表
const fetchFeedbackList = async () => {
  loading.value = true;
  try {
    const response = await getUserFeedbackListService(
        pageNum.value,
        pageSize.value,
        parseInt(activeStatus.value) // 转换为数字类型
    );

    if (response.code === 0) {
      feedbackList.value = response.data.items || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error('获取反馈列表失败');
    }
  } catch (error) {
    console.error('获取反馈列表错误:', error);
    ElMessage.error('获取反馈列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 标签页切换（切换已审核/未审核）
const handleStatusChange = () => {
  pageNum.value = 1; // 重置页码
  fetchFeedbackList();
};

// 分页-页码变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchFeedbackList();
};

// 分页-每页条数变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1; // 重置页码
  fetchFeedbackList();
};


// 页面加载时获取数据
onMounted(() => {
  fetchFeedbackList();
});
</script>

<template>
  <el-card class="feedback-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的反馈记录</h2>
    </div>

    <!-- 状态筛选标签页 -->
    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="handleStatusChange">
      <el-tab-pane label="已审核" name="1"></el-tab-pane>
      <el-tab-pane label="未审核" name="0"></el-tab-pane>
    </el-tabs>

    <!-- 反馈列表 -->
    <div class="feedback-list">
      <!-- 加载状态 -->
      <el-skeleton v-if="loading" :rows="5" class="feedback-skeleton" />

      <!-- 空状态 -->
      <div v-else-if="feedbackList.length === 0" class="empty-state">
        <el-empty description="暂无反馈记录" />
      </div>

      <!-- 反馈列表 -->
      <el-table
          v-else
          :data="feedbackList"
          border
          stripe
          :row-key="row => row.omdUserFeedbackId"
      >
        <el-table-column
            prop="omdUserFeedbackType"
            label="反馈类型"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.omdUserFeedbackType)">
              {{ formatType(scope.row.omdUserFeedbackType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="omdUserFeedbackContent"
            label="反馈内容"
            min-width="300"
        >
          <template #default="scope">
            <div class="content-wrapper">{{ scope.row.omdUserFeedbackContent }}</div>
          </template>
        </el-table-column>

        <el-table-column
            prop="omdUserFeedbackContact"
            label="联系方式"
            width="150"
            align="center"
        >
          <template #default="scope">
            <el-tag type="info">{{ scope.row.omdUserFeedbackContact  || '未填写' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="omdUserFeedbackStatus"
            label="状态"
            width="100"
            align="center"
        >
          <template #default="scope">
            <el-tag
                :type="scope.row.omdUserFeedbackStatus === 1 ? 'success' : 'warning'"
            >
              {{ scope.row.omdUserFeedbackStatus === 1 ? '已审核' : '未审核' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="omdUserFeedbackCreateTime"
            label="提交时间"
            width="180"
            align="center"
        />

        <el-table-column
            label="回复内容"
            min-width="200"
        >
          <template #default="scope">
            <div v-if="scope.row.omdUserFeedbackResponse" class="response-content">
              {{ scope.row.omdUserFeedbackResponse }}
            </div>
            <span v-else class="no-response">暂无回复</span>
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
.feedback-container {
  margin: 20px;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.status-tabs {
  margin-bottom: 20px;
}

.feedback-list {
  margin-bottom: 20px;
}

.feedback-skeleton {
  width: 100%;
  padding: 10px 0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.content-wrapper {
  white-space: normal;
  word-break: break-all;
  line-height: 1.6;
}

.response-content {
  color: #ff4d4f;
  line-height: 1.6;
}

.no-response {
  color: #909399;
  font-style: italic;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 表格样式优化 */
::v-deep .el-table th {
  background-color: #f5f7fa;
  font-weight: 500;
}

::v-deep .el-table td {
  padding: 12px 0;
}
</style>