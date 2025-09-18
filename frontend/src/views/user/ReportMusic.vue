<script setup>
import {ref, onMounted, watch, nextTick} from 'vue';
import { ElMessage } from 'element-plus';

// 导入服务器请求
import { getOmdMusicReportListService } from '@/api/user';

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
    1: '盗版侵权',
    2: '色情低俗',
    3: '暴力血腥',
    4: '政治敏感',
    5: '其他违规'
  };
  return typeMap[type] || '未知类型';
};

// 获取举报类型标签样式
const getTypeTag = (type) => {
  const typeMap = {
    1: 'success',
    2: 'danger',
    3: 'warning',
    4: 'primary',
    5: 'gray'
  };
  return typeMap[type] || 'info';
};

// 格式化处理状态
const formatStatus = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已受理',
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
  return statusMap[status] || 'info';
};

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true;
  try {
    const response = await getOmdMusicReportListService(
        pageNum.value,
        pageSize.value,
        parseInt(activeStatus.value) // 转换为数字类型（0-待审核，1-已受理，2-已驳回）
    );

    if (response.code === 0) {
      reportList.value = response.data.items || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error('获取举报列表失败');
    }
  } catch (error) {
    console.error('获取音乐举报列表错误:', error);
    ElMessage.error('获取举报列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 标签页切换（切换状态）
const handleStatusChange = () => {
  pageNum.value = 1;
  // 关闭图片查看器
  viewerVisible.value = false;
  viewerUrlList.value = [];
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

// 获取所有图片URL
const getImageList = (evidence) => {
  if (!evidence) return [];
  return evidence.split(',').filter(url => url.trim() !== '');
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

// 页面加载时获取数据
onMounted(() => {
  fetchReportList();
});
</script>


<template>
  <el-card class="music-report-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的举报音乐记录</h2>
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
          :row-key="row => row.omdMusicReportId"
          style="width: 100%"
      >

        <!-- 举报类型 -->
        <el-table-column
            prop="omdMusicReportType"
            label="举报类型"
            width="140"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.omdMusicReportType)">
              {{ formatType(scope.row.omdMusicReportType) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 被举报音乐信息 -->
        <el-table-column
            label="被举报音乐"
            min-width="250"
        >
          <template #default="scope">
            <div class="reported-music">
              <el-image
                  :src="scope.row.omdReportedMusic?.omdMusicInfoCoverUrl"
                  :preview-src-list="getImageList(scope.row.omdMusicReportEvidence)"
                  class="music-cover"
                  fit="cover"
                  :alt="scope.row.omdReportedMusic?.omdMusicInfoName || '音乐封面'"
              >
                <template #error>
                  <img :src="defaultCover" class="music-cover" alt="默认封面" />
                </template>
              </el-image>
              <div class="music-info">
                <div class="music-name">
                  {{ scope.row.omdReportedMusic?.omdMusicInfoName || '未知音乐' }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 举报原因 -->
        <el-table-column
            prop="omdMusicReportReason"
            label="举报原因"
            min-width="200"
        >
          <template #default="scope">
            <div class="report-reason">{{ scope.row.omdMusicReportReason }}</div>
          </template>
        </el-table-column>

        <!-- 举报证据 -->
        <el-table-column
            prop="omdMusicReportEvidence"
            label="举报证据"
            min-width="200"
        >
          <template #default="scope">
            <div class="report-reason">
              <el-image
                  v-if="scope.row.omdMusicReportEvidence"
                  v-for="(url, index) in scope.row.omdMusicReportEvidence.split(',')"
                  :key="index"
                  :src="url"
                  class="music-cover"
                  fit="cover"
                  :alt="'举报证据图片' + (index + 1)"
                  @click="openImageViewer(scope.row.omdMusicReportEvidence.split(','))"
              />
              <span v-else>举报时未上传</span>
            </div>
          </template>
        </el-table-column>

        <!-- 举报状态 -->
        <el-table-column
            prop="omdMusicReportStatus"
            label="状态"
            width="120"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.omdMusicReportStatus)">
              {{ formatStatus(scope.row.omdMusicReportStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 处理信息 -->
        <el-table-column
            label="处理信息"
            min-width="220"
        >
          <template #default="scope">
            <div v-if="scope.row.omdMusicReportStatus !== 0" class="handle-info">
              <p>处理时间：{{ scope.row.omdMusicReportHandleTime || '暂无' }}</p>
              <p v-if="scope.row.omdMusicReportHandleRemark" class="handle-remark">
                处理备注：{{ scope.row.omdMusicReportHandleRemark }}
              </p>
            </div>
            <div v-else class="pending-info">待审核处理</div>
          </template>
        </el-table-column>

        <!-- 举报时间 -->
        <el-table-column
            prop="omdMusicReportCreateTime"
            label="举报时间"
            width="180"
            align="center"
        >
          <template #default="scope">
            {{ scope.row.omdMusicReportCreateTime }}
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
.music-report-container {
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

/* 被举报音乐信息样式 */
.reported-music {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.music-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  margin-right: 15px;
  object-fit: cover;
  cursor: pointer;
}

.music-info {
  flex: 1;
}

.music-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.music-id {
  font-size: 12px;
  color: #909399;
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