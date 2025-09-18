<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入服务器接口
import { getUserReportListService,getOmdUserRoleListService } from '@/api/user';

// 路由实例
const router = useRouter();

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const reportList = ref([]);
const loading = ref(false);
const activeStatus = ref('1'); // 默认显示已审核（1）
// 图片查看器状态
const viewerVisible = ref(false);
const viewerUrlList = ref([]);

// 格式化举报类型
const formatType = (type) => {
  const typeMap = {
    1: '垃圾信息',
    2: '侮辱谩骂',
    3: '欺诈',
    4: '其他违规'
  };
  return typeMap[type] || '未知类型';
};

// 获取举报类型标签样式
const getTypeTag = (type) => {
  const typeMap = {
    1: 'warning',
    2: 'danger',
    3: 'primary',
    4: 'info'
  };
  return typeMap[type] || 'info';
};

// 格式化处理状态
const formatStatus = (status) => {
  const statusMap = {
    0: '未处理',
    1: '已处理',
    2: '已驳回'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态标签样式
const getStatusTag = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  };
  return statusMap[status] || 'warning';
};


// 获取举报列表
const fetchReportList = async () => {
  loading.value = true;
  try {
    const response = await getUserReportListService(
        pageNum.value,
        pageSize.value,
        parseInt(activeStatus.value) // 转换为数字类型（0-未处理，1-已处理，2-已驳回）
    );

    if (response.code === 0) {
      reportList.value = response.data.items || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error('获取举报列表失败');
    }
  } catch (error) {
    console.error('获取用户举报列表错误:', error);
    ElMessage.error('获取举报列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 标签页切换（切换状态）
const handleStatusChange = () => {
  pageNum.value = 1; // 重置页码
  // 关闭图片查看器
  viewerVisible.value = false;
  viewerUrlList.value = [];
  fetchReportList();
};

// 打开图片查看器
const openImageViewer = (urls) => {
  const validUrls = urls.filter(url => url.trim() !== '');
  if (validUrls.length === 0) return;

  viewerUrlList.value = validUrls;
  viewerVisible.value = true;
};

// 关闭图片查看器
const closeImageViewer = () => {
  viewerVisible.value = false;
  // 清空URL列表确保组件完全卸载
  setTimeout(() => {
    viewerUrlList.value = [];
  }, 300);
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

// 跳转到用户详情页面
const goToUserInfo = async (omdUserId) => {
  if (!omdUserId) {
    console.error('用户ID不存在，无法跳转');
    ElMessage.warning('用户ID不存在');
    return;
  }

  try {
    // 查询用户角色判断是否为歌手
    const result = await getOmdUserRoleListService(omdUserId);
    const isSinger = result.data.some(role => role.omdRoleCode === 'ROLE_SINGER');

    // 根据角色跳转不同页面
    if (isSinger) {
      router.push(`/introduction/singerDetail/${omdUserId}`);
    } else {
      // 普通用户详情页路由（根据实际项目调整）
      router.push(`/introduction/userDetail/${omdUserId}`);
    }
  } catch (error) {
    console.error('查询用户角色失败:', error);
    ElMessage.error('跳转失败，请重试');
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchReportList();
});
</script>


<template>
  <el-card class="user-report-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的举报用户记录</h2>
    </div>

    <!-- 状态筛选标签页 -->
    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="handleStatusChange">
      <el-tab-pane label="已审核" name="1"></el-tab-pane>
      <el-tab-pane label="未审核" name="0"></el-tab-pane>
      <el-tab-pane label="已驳回" name="2"></el-tab-pane>
    </el-tabs>

    <!-- 举报列表 -->
    <div class="report-list">
      <!-- 加载状态 -->
      <el-skeleton v-if="loading" :rows="6" class="report-skeleton" />

      <!-- 空状态 -->
      <div v-else-if="reportList.length === 0" class="empty-state">
        <el-empty description="暂无该状态的举报记录" />
      </div>

      <!-- 举报列表 -->
      <el-table
          v-else
          :data="reportList"
          border
          stripe
          :row-key="row => row.omdUserReportId"
          style="width: 100%"
      >
        <!-- 被举报用户信息 -->
        <el-table-column
            label="被举报用户"
            min-width="200"
        >
          <template #default="scope">
            <div class="reported-user" @click="goToUserInfo(scope.row.omdUserReportedUserId)">
              <el-avatar :src="scope.row.omdReportedUser?.omdUserAvatar || defaultAvatar" class="user-avatar">
                <img :src="defaultAvatar" alt="默认头像" v-if="!scope.row.omdReportedUser?.omdUserAvatar" />
              </el-avatar>
              <div class="user-info">
                <div class="user-name">
                  {{ scope.row.omdReportedUser?.omdUserNickname || scope.row.omdReportedUser?.omdUserName || '未知用户' }}
                </div>
              </div>
              <el-icon class="go-icon"><ArrowRight /></el-icon>
            </div>
          </template>
        </el-table-column>

        <!-- 举报类型 -->
        <el-table-column
            prop="omdUserReportType"
            label="举报类型"
            width="140"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.omdUserReportType)">
              {{ formatType(scope.row.omdUserReportType) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 举报原因 -->
        <el-table-column
            prop="omdUserReportReason"
            label="举报原因"
            min-width="200"
        >
          <template #default="scope">
            <div class="report-reason">{{ scope.row.omdUserReportReason }}</div>
          </template>
        </el-table-column>

        <!-- 举报截图 -->
        <el-table-column
            prop="omdUserReportEvidence"
            label="举报截图"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-image
                v-if="scope.row.omdUserReportEvidence"
                v-for="(url, index) in scope.row.omdUserReportEvidence.split(',')"
                :key="index"
                :src="url"
                fit="cover"
                :alt="'举报证据图片' + (index + 1)"
                @click="openImageViewer(scope.row.omdUserReportEvidence.split(','))"
                style="cursor: pointer; margin-right: 5px"
            />
            <span v-else>举报时未上传</span>
          </template>
        </el-table-column>

        <!-- 举报状态 -->
        <el-table-column
            prop="omdUserReportStatus"
            label="状态"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.omdUserReportStatus)">
              {{ formatStatus(scope.row.omdUserReportStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 处理信息 -->
        <el-table-column
            label="处理信息"
            min-width="220"
        >
          <template #default="scope">
            <div v-if="scope.row.omdUserReportStatus !== 0" class="handle-info">
              <p>处理时间：{{ scope.row.omdUserReportHandleTime || '暂无' }}</p>
              <p v-if="scope.row.omdUserReportHandleRemark" class="handle-remark">
                处理备注：{{ scope.row.omdUserReportHandleRemark }}
              </p>
            </div>
            <div v-else class="pending-info">待处理</div>
          </template>
        </el-table-column>

        <!-- 举报时间 -->
        <el-table-column
            prop="omdUserReportCreateTime"
            label="举报时间"
            width="180"
            align="center"
        >
          <template #default="scope">
            {{ scope.row.omdUserReportCreateTime }}
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

  <!-- 图片查看器 -->
  <div v-if="viewerUrlList.length > 0">
    <el-image-viewer
        :visible="viewerVisible"
        :url-list="viewerUrlList"
        @close="closeImageViewer"
    />
  </div>

</template>



<style scoped>
.user-report-container {
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

/* 被举报用户信息样式 */
.reported-user {
  display: flex;
  align-items: center;
  padding: 5px 0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.reported-user:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  width: 50px;
  height: 50px;
  margin-right: 15px;
  border-radius: 50%;
  overflow: hidden;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.user-id {
  font-size: 12px;
  color: #909399;
}

.go-icon {
  color: #c0c4cc;
  font-size: 16px;
  margin-left: 5px;
}

/* 举报原因样式 */
.report-reason {
  line-height: 1.6;
  color: #666;
  word-break: break-all;
}

/* 处理信息样式 */
.handle-info {
  font-size: 13px;
  line-height: 1.6;
  color: #333;
}

.handle-remark {
  margin-top: 4px;
  color: #67c23a;
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