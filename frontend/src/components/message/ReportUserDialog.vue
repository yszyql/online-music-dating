<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js';

// 导入服务器请求
import { insertUserReportService } from '@/api/user';

const props = defineProps({
  reportedUserId: {
    type: Object,
    required: true
  },
  visible: {
    type: Boolean,
    default: false
  },
  dialogWidth: {
    type: String,
    default: '600px'
  }
});

const emit = defineEmits(['update:visible', 'reportSuccess']);

// 创建状态管理实例
const authStore = useAuthStore();

// 响应式
const reportFormRef = ref(null);
const fileList = ref([]);
const previewUrls = ref([]);
const submitLoading = ref(false);

// 表单数据
const reportForm = reactive({
  omdUserReportedUserId: props.reportedUserId,
  omdUserReportType: '',
  omdUserReportReason: '',
  omdUserReportEvidence: ''
});

// 表单验证规则
const reportRules = reactive({
  omdUserReportType: [
    { required: true, message: '请选择举报类型', trigger: 'change' }
  ],
  omdUserReportReason: [
    { required: true, message: '请填写举报原因', trigger: 'blur' },
    { min: 5, message: '举报原因至少5个字符', trigger: 'blur' }
  ]
});

// 上传成功处理
const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    ElMessage.success('证据上传成功');
    // 保存文件URL到列表
    fileList.value.push({
      name: `evidence-${Date.now()}`,
      url: response.data
    });
    // 更新预览列表和证据字段
    updateEvidenceUrls();
  } else {
    ElMessage.error('证据上传失败');
  }
};

// 更新证据URL
const updateEvidenceUrls = () => {
  previewUrls.value = fileList.value.map(f => f.url); // 更新预览列表
  // 将所有图片URL用逗号连接，存入表单字段
  reportForm.omdUserReportEvidence = previewUrls.value.join(',');
};

// 上传错误处理
const handleUploadError = (err) => {
  ElMessage.error('上传失败，请重试');
  console.error('上传错误:', err);
};

// 超出上传限制处理
const handleExceed = (files, fileList) => {
  ElMessage.warning('最多只能上传3张证据图片');
};

// 删除证据时同步更新
const handleRemoveEvidence = (index) => {
  fileList.value.splice(index, 1);
  previewUrls.value = fileList.value.map(f => f.url); // 保持数组格式
  reportForm.omdUserReportEvidence = previewUrls.value.join(',');
};

// 重置表单
const resetForm = () => {
  reportForm.omdUserReportType = '';
  reportForm.omdUserReportReason = '';
  reportForm.omdUserReportEvidence = '';
  fileList.value = [];
  previewUrls.value = [];
  if (reportFormRef.value) {
    reportFormRef.value.clearValidate();
  }
};

// 提交举报
const handleSubmit = async () => {
  try {
    await reportFormRef.value.validate();
  } catch (err) {
    console.error('表单验证失败:', err);
    return;
  }

  if (fileList.value.length === 0) {
    const confirm = await ElMessageBox.confirm(
        '未上传任何证据，仍要提交举报吗？',
        '提示',
        {
          confirmButtonText: '继续提交',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).catch(() => false);

    if (!confirm) return;
  }

  submitLoading.value = true;
  try {
    const response = await insertUserReportService(reportForm);
    if (response.code === 0) {
      ElMessage.success('举报提交成功，我们将尽快处理');
      emit('reportSuccess');
      handleClose();
    } else {
      ElMessage.error('举报提交失败');
    }
  } catch (error) {
    console.error('举报提交失败:', error);
    ElMessage.error('提交失败，请重试');
  } finally {
    submitLoading.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  emit('update:visible', false);
  resetForm();
};

// 监听 reportedUserId 的变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    reportForm.omdUserReportedUserId = props.reportedUserId;
    resetForm();
  }
});
</script>


<template>
  <el-dialog
      v-model="props.visible"
      title="举报用户"
      :width="dialogWidth"
      :before-close="handleClose"
      destroy-on-close
  >
    <!-- 举报表单 -->
    <el-form
        ref="reportFormRef"
        :model="reportForm"
        :rules="reportRules"
        label-width="100px"
        style="margin-top: 15px"
    >
      <!-- 隐藏的被举报用户ID（从父组件传入） -->
      <el-form-item
          prop="omdUserReportedUserId"
          hidden
      >
      </el-form-item>

      <!-- 举报类型 -->
      <el-form-item
          label="举报类型"
          prop="omdUserReportType"
      >
        <el-select
            v-model="reportForm.omdUserReportType"
            placeholder="请选择举报类型"
            clearable
        >
          <el-option
              label="垃圾信息"
              value="1"
          />
          <el-option
              label="侮辱谩骂"
              value="2"
          />
          <el-option
              label="欺诈"
              value="3"
          />
          <el-option
              label="其他违规"
              value="4"
          />
        </el-select>
      </el-form-item>

      <!-- 举报原因 -->
      <el-form-item
          label="举报原因"
          prop="omdUserReportReason"
      >
        <el-input
            v-model="reportForm.omdUserReportReason"
            type="textarea"
            :rows="4"
            placeholder="请详细描述举报原因..."
        />
      </el-form-item>

      <!-- 证据上传 -->
      <el-form-item label="证据上传">
        <el-upload
            class="upload-demo"
            action="/api/user/evidenceFileUpload"
            name="evidenceFile"
            :headers="{ Authorization: `Bearer ${authStore.token}` }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="3"
            :file-list="fileList"
            :on-exceed="handleExceed"
            accept="image/*"
        >
          <el-button type="primary" icon="Upload">选择图片证据</el-button>
          <template #tip>
            <div class="el-upload__tip text-gray-500">
              支持上传jpg/png格式图片，最多3张
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item v-if="fileList.length > 0" label="已上传证据">
        <div class="evidence-preview">
          <el-image
              v-for="(file, index) in fileList"
              :key="index"
              :src="file.url"
              :preview-src-list="previewUrls"
          class="evidence-img"
          >
          <template #error>
            <div class="image-error">加载失败</div>
          </template>
          <template #footer>
            <el-button
                size="small"
                type="text"
                @click.stop="handleRemoveEvidence(index)"
            >
              删除
            </el-button>
          </template>
          </el-image>
        </div>
      </el-form-item>
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button
          @click="handleClose"
      >
        取消
      </el-button>
      <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitLoading"
      >
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.evidence-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.evidence-img {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  cursor: pointer;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #999;
  font-size: 12px;
}

::v-deep .el-upload--picture-card {
  width: 120px;
  height: 120px;
}

::v-deep .el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
  padding-bottom: 10px;
}
</style>