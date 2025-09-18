<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 导入状态管理
import { useAuthStore } from '@/stores/auth';

// 导入服务器接口
import {
  getMusicAppealListBySingerIdService,
  updateMusicAppealService
} from '@/api/singer';

// 状态管理
const authStore = useAuthStore();

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

// 状态筛选（默认显示已审核内容，即状态1）
const selectedStatus = ref('1');
const appealList = ref([]);

// 重新申诉相关状态
// 表单引用
const reAppealFormRef = ref(null);
const reAppealDialogVisible = ref(false);
const currentMusicName = ref('');
const reAppealForm = reactive({
  omdMusicAppealId: null,
  omdMusicInfoId: null,
  omdMusicAppealReason: '',
  omdMusicAppealEvidence: '',
  omdMusicAppealStatus: 0  // 重置为待审核状态
});
const evidenceFileList = ref([]);
const reAppealRules = {
  omdMusicAppealReason: [
    { required: true, message: '请填写申诉理由', trigger: 'blur' },
    { min: 20, message: '申诉理由至少20个字符', trigger: 'blur' }
  ]
};
const submitReAppealLoading = ref(false);
const dialogWidth = ref('600px');

// 获取申诉列表
const fetchAppealList = async () => {
  loading.value = true;
  try {
    const response = await getMusicAppealListBySingerIdService(
        pageNum.value,
        pageSize.value,
        selectedStatus.value || undefined
    );

    if (response.code === 0) {
      appealList.value = response.data.items || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error('获取申诉列表失败');
    }
  } catch (error) {
    console.error('获取申诉列表错误:', error);
    ElMessage.error('获取申诉列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 状态筛选变化
const handleStatusChange = () => {
  pageNum.value = 1;
  fetchAppealList();
};

// 分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchAppealList();
};

// 每页条数变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchAppealList();
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '申诉通过',
    2: '申诉驳回'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',  // 待审核-警告色
    1: 'success',  // 申诉通过-成功色
    2: 'danger'    // 申诉驳回-危险色
  };
  return typeMap[status] || 'info';
};

// 点击重新申诉
const handleReAppeal = (appeal) => {
  // 初始化表单数据
  reAppealForm.omdMusicAppealId = appeal.omdMusicAppealId;
  reAppealForm.omdMusicInfoId = appeal.omdMusicInfoId;
  reAppealForm.omdMusicAppealReason = appeal.omdMusicAppealReason || '';
  reAppealForm.omdMusicAppealEvidence = appeal.omdMusicAppealEvidence || '';

  // 初始化证据文件列表
  evidenceFileList.value = [];
  if (appeal.omdMusicAppealEvidence) {
    // 如果有历史证据，拆分并添加到文件列表（仅显示名称，实际URL可能已失效）
    const evidenceUrls = appeal.omdMusicAppealEvidence.split(',');
    evidenceUrls.forEach(url => {
      if (url) {
        const fileName = url.substring(url.lastIndexOf('/') + 1);
        evidenceFileList.value.push({
          name: fileName,
          url: url
        });
      }
    });
  }

  // 记录当前音乐名称
  currentMusicName.value = appeal.omdAppealMusicInfo?.omdMusicInfoName || '未知歌曲';

  // 打开弹窗
  reAppealDialogVisible.value = true;
};

// 证据上传成功
const handleEvidenceUploadSuccess = (response, file, fileList) => {
  if (response.code === 0) {
    file.url = response.data;
    // 更新证据URL字段
    reAppealForm.omdMusicAppealEvidence = fileList
        .map(f => f.url)
        .filter(Boolean)
        .join(',');
    ElMessage.success('证据上传成功');
  } else {
    ElMessage.error('证据上传失败');
  }
};

// 移除证据文件
const handleEvidenceRemove = (file) => {
  const index = evidenceFileList.value.findIndex(f => f.uid === file.uid || f.name === file.name);
  if (index !== -1) {
    evidenceFileList.value.splice(index, 1);
    // 更新证据URL字段
    reAppealForm.omdMusicAppealEvidence = evidenceFileList.value
        .map(f => f.url)
        .filter(Boolean)
        .join(',');
  }
};

// 上传失败
const handleUploadError = (err) => {
  console.error('文件上传错误:', err);
  ElMessage.error('文件上传失败，请重试');
};

// 提交重新申诉
const handleSubmitReAppeal = async () => {
  // 表单验证
  try {
    await reAppealFormRef.value.validate();
  } catch (error) {
    return;
  }

  submitReAppealLoading.value = true;
  try {
    const response = await updateMusicAppealService(reAppealForm);
    if (response.code === 0) {
      ElMessage.success('重新申诉提交成功');
      reAppealDialogVisible.value = false;
      // 刷新列表
      await fetchAppealList();
    } else {
      ElMessage.error('重新申诉提交失败');
    }
  } catch (error) {
    console.error('重新申诉提交失败:', error);
    ElMessage.error('重新申诉提交失败，请重试');
  } finally {
    submitReAppealLoading.value = false;
  }
};

// 关闭弹窗
const handleDialogClose = () => {
  reAppealDialogVisible.value = false;
  if (reAppealFormRef.value) {
    reAppealFormRef.value.clearValidate();
  }
};

// 页面加载时获取列表
onMounted(() => {
  fetchAppealList();
});


</script>

<template>
  <div class="appeal-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的音乐申诉记录</h2>
      <p class="header-desc">查看您提交的音乐申诉及处理状态</p>
    </div>

    <!-- 状态筛选 -->
    <div class="status-filter">
      <el-select
          v-model="selectedStatus"
          placeholder="全部状态"
          @change="handleStatusChange"
          clearable
      >
        <el-option label="全部" value="" />
        <el-option label="待审核" value="0" />
        <el-option label="申诉通过" value="1" />
        <el-option label="申诉驳回" value="2" />
      </el-select>
    </div>

    <!-- 申诉列表 -->
    <el-table
        :data="appealList"
        border
        stripe
        :loading="loading"
        :row-key="row => row.omdMusicAppealId"
        class="appeal-table"
    >
      <!-- 序号 -->
      <el-table-column type="index" label="序号" width="60" />

      <!-- 被申诉音乐信息 -->
      <el-table-column label="被申诉音乐" min-width="300">
        <template #default="scope">
          <div class="music-item">
            <el-image
                :src="scope.row.omdAppealMusicInfo?.omdMusicInfoCoverUrl"
                fit="cover"
                class="music-cover"
                :alt="scope.row.omdAppealMusicInfo?.omdMusicInfoName"
            />
            <div class="music-details">
              <div class="music-name">{{ scope.row.omdAppealMusicInfo?.omdMusicInfoName }}</div>
              <div class="music-id">音乐ID: {{ scope.row.omdMusicInfoId }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 申诉理由 -->
      <el-table-column label="申诉理由" min-width="250">
        <template #default="scope">
          <div class="appeal-reason" :title="scope.row.omdMusicAppealReason">
            {{ scope.row.omdMusicAppealReason }}
          </div>
        </template>
      </el-table-column>

      <!-- 申诉状态 -->
      <el-table-column label="申诉状态" width="120">
        <template #default="scope">
          <el-tag
              :type="getStatusTagType(scope.row.omdMusicAppealStatus)"
          >
            {{ getStatusText(scope.row.omdMusicAppealStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 处理信息 -->
      <el-table-column label="处理信息" min-width="200">
        <template #default="scope">
          <div class="handle-info">
            <p v-if="scope.row.omdMusicAppealStatus !== 0">
              处理时间: {{ scope.row.omdMusicAppealHandleTime || '暂无' }}
            </p>
            <p v-else>待审核处理</p>
            <p v-if="scope.row.omdMusicAppealHandleRemark" class="handle-remark">
              处理备注: {{ scope.row.omdMusicAppealHandleRemark }}
            </p>
          </div>
        </template>
      </el-table-column>

      <!-- 提交时间 -->
      <el-table-column label="提交时间" width="180">
        <template #default="scope">
          {{ scope.row.omdMusicAppealCreateTime }}
        </template>
      </el-table-column>

      <!-- 操作 -->
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <!-- 只有申诉驳回状态(2)才显示重新申诉按钮 -->
          <el-button
              v-if="scope.row.omdMusicAppealStatus === 2"
              type="text"
              @click="handleReAppeal(scope.row)"
              class="reAppeal-btn"
          >
            重新申诉
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="!loading && appealList.length === 0" class="empty-state">
      <el-empty description="暂无申诉记录" />
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

    <!-- 重新申诉弹窗 -->
    <el-dialog
        v-model="reAppealDialogVisible"
        title="重新申诉"
        :width="dialogWidth"
        :before-close="handleDialogClose"
    >
      <el-form
          ref="reAppealFormRef"
          :model="reAppealForm"
          :rules="reAppealRules"
          label-width="100px"
          class="reAppeal-form"
      >
        <el-form-item label="歌曲名称" disabled>
          <el-input v-model="currentMusicName" />
        </el-form-item>

        <el-form-item label="申诉理由" prop="omdMusicAppealReason">
          <el-input
              v-model="reAppealForm.omdMusicAppealReason"
              type="textarea"
              :rows="4"
              placeholder="请详细描述您的申诉理由..."
          />
        </el-form-item>

        <el-form-item label="申诉证据">
          <el-upload
              class="evidence-upload"
              action="/api/user/evidenceFileUpload"
              name="evidenceFile"
              :headers="{ Authorization: `Bearer ${authStore.token}` }"
              :on-success="handleEvidenceUploadSuccess"
              :on-error="handleUploadError"
              :file-list="evidenceFileList"
              :on-remove="handleEvidenceRemove"
              accept=".jpg,.jpeg,.png,.pdf"
              :limit="3"
          >
            <el-button type="primary" icon="Upload">选择证据文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持jpg、png、pdf格式，最多3个文件
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item v-if="evidenceFileList.length > 0" label="已上传证据">
          <div class="evidence-list">
            <div
                v-for="(file, index) in evidenceFileList"
                :key="index"
                class="evidence-item"
            >
              <el-icon class="file-icon"><Document /></el-icon>
              <el-image
                  :src="file.url"
                  fit="cover"
                  class="file-preview"
                  :alt="file.name"
              />
              <span class="file-name">{{ file.name }}</span>
              <el-button
                  size="small"
                  type="text"
                  @click.stop="handleEvidenceRemove(file)"
                  class="remove-btn"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button
            type="primary"
            @click="handleSubmitReAppeal"
            :loading="submitReAppealLoading"
        >
          提交申诉
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.appeal-list-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 5px;
}

.header-desc {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.status-filter {
  margin-bottom: 15px;
}

.appeal-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.music-item {
  display: flex;
  align-items: center;
}

.music-cover {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  margin-right: 12px;
}

.music-details {
  line-height: 1.6;
}

.music-name {
  font-weight: 500;
  color: #333;
}

.music-id {
  font-size: 13px;
  color: #666;
}

.appeal-reason {
  line-height: 1.5;
  word-break: break-all;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.handle-info {
  line-height: 1.5;
}

.handle-remark {
  margin-top: 5px;
  color: #f56c6c;
  font-size: 13px;
}

.reAppeal-btn {
  color: #409eff;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 重新申诉弹窗样式 */
.reAppeal-form {
  margin-top: 10px;
}

.evidence-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 5px;
}

.evidence-item {
  display: flex;
  align-items: center;
  padding: 5px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  max-width: 400px;
}

.file-icon {
  margin-right: 8px;
  color: #409eff;
}

.file-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
}

.remove-btn {
  color: #f56c6c;
  padding: 0 5px;
}

::v-deep .el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
}
</style>