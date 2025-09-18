<script setup>
import {ref, onMounted, computed} from 'vue';
import {ElMessage} from 'element-plus';

// 导入服务器请求
import {getOperationLogListService} from '@/api/admin.js';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);
const tableData = ref([]);
const loading = ref(false);
const activeTab = ref('all');

// 分页逻辑保持不变
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  fetchOperationLogs();
};

// 调整每页显示数量
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  pageNum.value = 1;
  fetchOperationLogs();
};

// 格式化时间显示
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 格式化操作类型显示
const formatLogType = (type) => {
  if (!type) return '未知操作';
  const typeMap = {
    'user:freeze': '用户冻结',
    'user:unfreeze': '用户解冻',
    'user:auto_unfreeze': '系统自动解冻',
    'user:unfreeze_auto': '系统自动解冻',
    'review:music:pass': '音乐通过审核',
    'review:music:fail': '音乐审核失败',
    'review:comment:pass': '评论被举报成功',
    'review:comment:fail': '评论被举报失败',
    'review:applications:pass': '申请通过',
    'review:applications:fail': '申请驳回',
    'review:feedback': '反馈回复',
    'admin:insert': '管理员新增',
    'admin:unfreeze': '管理员解冻',
    'admin:freeze': '管理员冻结',
    'user:appeal:pass' : '用户申诉通过',
    'user:appeal:fail' : '用户申诉驳回',
    'music:appeal:pass' : '音乐申诉通过',
    'music:appeal:fail' : '音乐申诉驳回',
    'review:music:report:fail' : '音乐举报失败',
   'review:music:report:pass' : '音乐举报成功',
   'review:user:report:fail' : '用户举报失败',
  'review:user:report:pass' : '用户举报成功',
  };
  return typeMap[type] || type;
};

// 根据操作类型获取标签样式
const getLogTypeTag = (type) => {
  if (!type) return 'info';
  const typeMap = {
    'user:freeze': 'danger',
    'user:unfreeze': 'success',
    'user:auto_unfreeze': 'info',
    'user:unfreeze_auto': 'info',
    'review:music:pass': 'warning',
    'review:music:fail': 'primary',
    'review:comment:pass': 'warning',
    'review:comment:fail': 'warning',
    'review:applications:pass': 'success',
    'review:applications:fail': 'danger',
    'review:feedback': 'primary',
    'admin:insert': 'success',
    'admin:unfreeze': 'success',
    'admin:freeze': 'danger',
    'review:user:report:pass': 'success',
    'review:user:report:fail': 'danger',
    'review:music:report:pass': 'success',
    'review:music:report:fail': 'danger',
   'user:appeal:pass' : 'success',
    'user:appeal:fail' : 'danger',
   'music:appeal:pass' : 'success',
   'music:appeal:fail' : 'danger',
  };
  return typeMap[type] || 'info';
};

// 获取对象类型文本
const getTargetTypeText = (type) => {
  const typeMap = {
    'user': '用户',
    'music': '音乐',
    'comment': '评论',
    'applications': '申请',
    'feedback': '反馈',
    'admin': '管理员',
    'userAppeal': '用户申诉',
    'musicAppeal': '音乐申诉',
    'musicReport': '音乐举报',
    'userReport': '用户举报'
  };
  return typeMap[type] || '未知类型';
};


// 标签页切换事件
const handleTabChange = () => {
  // 滚动到顶部
  pageNum.value = 1; // 重置页码
  fetchOperationLogs(); // 重新加载数据
  document.querySelector('.log-list').scrollTop = 0;
};

// 获取操作日志列表
const fetchOperationLogs = async () => {
  loading.value = true;
  try {
    // 传递当前标签页的类型作为筛选条件
    const targetTypes = activeTab.value === 'all' ? null : activeTab.value;

    // 修改API调用，添加第三个参数
    const response = await getOperationLogListService(
        pageNum.value,
        pageSize.value,
        targetTypes
    );

    if (response.code === 0) {
      tableData.value = response.data.items || [];
      totalPages.value = response.data.total || 0;
    } else {
      ElMessage.error('获取操作日志失败');
    }
  } catch (error) {
    console.error('获取操作日志出错:', error);
    ElMessage.error('获取操作日志失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchOperationLogs();
});
</script>


<template>
  <el-card class="operation-log-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">操作日志记录</h1>
        <el-tag type="success" class="total-count">
          共 {{ totalPages }} 条记录
        </el-tag>
      </div>
    </template>

    <!-- 分类标签页 -->
    <el-tabs v-model="activeTab" class="log-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="用户操作" name="user"></el-tab-pane>
      <el-tab-pane label="音乐操作" name="music"></el-tab-pane>
      <el-tab-pane label="评论操作" name="comment"></el-tab-pane>
      <el-tab-pane label="申请操作" name="applications"></el-tab-pane>
      <el-tab-pane label="反馈操作" name="feedback"></el-tab-pane>
      <el-tab-pane label="用户申诉" name="userAppeal"></el-tab-pane>
      <el-tab-pane label="音乐申诉" name="musicAppeal"></el-tab-pane>
      <el-tab-pane label="管理员操作" name="admin"></el-tab-pane>
    </el-tabs>

    <!-- 操作日志列表 -->
    <div class="log-list">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton
            :rows="6"
            :cols="1"
            class="log-skeleton"
        ></el-skeleton>
      </div>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="暂无该类型的操作日志记录"/>
      </div>

      <!-- 日志列表 -->
      <div v-else class="log-items">
        <div
            class="log-item"
            v-for="(log, index) in tableData"
            :key="log.omdOperationLogId"
            :class="`log-type-${log.omdOperationLogTargetType}`"
        >
          <!-- 日志头部 -->
          <div class="log-header">
            <div class="log-type">
              <el-tag
                  :type="getLogTypeTag(log.omdOperationLogType)"
                  class="type-tag"
              >
                {{ formatLogType(log.omdOperationLogType) }}
              </el-tag>
              <el-tag
                  size="small"
                  class="target-type-tag"
              >
                {{ getTargetTypeText(log.omdOperationLogTargetType) }}
              </el-tag>
            </div>
            <div class="log-time">
              {{ formatTime(log.omdOperationLogTime) }}
            </div>
          </div>

          <!-- 日志内容 -->
          <div class="log-content">
            <p class="log-description">
              {{ log.omdOperationLogDesc }}
            </p>

            <!-- 被操作对象信息 -->
            <div class="target-info">
              <span class="target-label">操作对象：</span>
              <span class="target-name">{{ log.targetName }}</span>
              <span v-if="log.targetDetail" class="target-detail">（{{ log.targetDetail || '暂无' }}）</span>
            </div>
          </div>

          <!-- 日志底部 -->
          <div class="log-footer">
            <div class="log-operator">
              <span class="operator-label">操作人：</span>
              <span class="operator-name">{{ log.adminName || '系统自动操作' }}</span>
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
            :page-sizes="[10, 20, 30, 40, 50]"
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total, sizes"
        />
      </div>
    </template>
  </el-card>
</template>


<style scoped>
.operation-log-container {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.total-count {
  background-color: #f0f2f5;
}

.log-tabs {
  padding: 0 15px;
  border-bottom: 1px solid #f5f5f5;
}

.log-list {
  padding: 15px;
  min-height: 300px;
}

.loading-state {
  padding: 20px 0;
}

.log-skeleton {
  width: 100%;
}

.log-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.log-item {
  background-color: #fff;
  border-radius: 6px;
  padding: 16px;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border-left: 3px solid transparent;
}

.log-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 不同类型日志的左侧边框颜色 */
.log-type-user {
  border-left-color: #409eff;
}

.log-type-music {
  border-left-color: #67c23a;
}

.log-type-comment {
  border-left-color: #e6a23c;
}

.log-type-applications {
  border-left-color: #f56c6c;
}

.log-type-feedback {
  border-left-color: #909399;
}

.log-type-admin {
  border-left-color: #ef4444;
}

.log-type-userAppeal {
  border-left-color: #99a9bf;
}

.log-type-musicAppeal {
  border-left-color: #d05ce3;
}

.log-type-userReport {
  border-left-color: rgb(45, 255, 210);
}

.log-type-musicReport {
  border-left-color: #8f8442;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.log-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-tag {
  font-size: 12px;
  padding: 3px 8px;
}

.target-type-tag {
  background-color: #f0f2f5;
}

.log-time {
  font-size: 13px;
  color: #888;
  white-space: nowrap;
}

.log-content {
  margin-bottom: 12px;
  line-height: 1.6;
}

.log-description {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #444;
  word-break: break-word;
}

.target-info {
  font-size: 14px;
  padding: 10px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 2px solid #e5e6eb;
}

.target-label {
  color: #666;
  font-weight: 500;
}

.target-name {
  color: #333;
  font-weight: 500;
}

.target-detail {
  color: #666;
  margin-left: 5px;
}

.log-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  margin-top: 10px;
  border-top: 1px dashed #eee;
}

.log-operator {
  font-size: 13px;
  color: #666;
}

.operator-label {
  color: #999;
}

.operator-name {
  font-weight: 500;
  color: #333;
}

.empty-state {
  padding: 80px 0;
  display: flex;
  justify-content: center;
}

.pagination {
  padding: 15px 0;
  display: flex;
  justify-content: center;
  border-top: 1px solid #f5f5f5;
  margin-top: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .log-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .log-time {
    align-self: flex-end;
  }
}
</style>