<script setup>
import { ref, reactive,  watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 导入状态管理
import {useAuthStore} from "@/stores/auth.js";

// 导入API
import { insertMusicReportService } from '@/api/user';

// 接收父组件参数
const props = defineProps({
  // 被举报音乐ID（必传）
  musicId: {
    type: Object,
    required: true,
    default: null
  },
  // 弹窗显示状态（双向绑定）
  visible: {
    type: Boolean,
    default: false
  },
  // 弹窗宽度
  dialogWidth: {
    type: String,
    default: '650px'
  }
});

// 向父组件传递事件
const emit = defineEmits(['update:visible', 'reportSuccess']);

// 创建状态管理实例
const authStore = useAuthStore();

// 表单数据（对应OmdMusicReport实体类）
const musicReportForm = reactive({
  omdMusicInfoId: props.musicId, // 被举报音乐ID
  omdMusicReportType: '', // 举报类型（1-5）
  omdMusicReportReason: '', // 举报原因
  omdMusicReportEvidence: '' // 证据URL，多个用逗号分隔
});

// 表单验证规则
const reportRules = reactive({
  omdMusicReportType: [
    { required: true, message: '请选择举报类型', trigger: 'change' }
  ],
  omdMusicReportReason: [
    { required: true, message: '请填写举报原因', trigger: 'blur' },
    { min: 10, message: '举报原因至少10个字符', trigger: 'blur' }
  ]
});

// 上传相关状态
const fileList = ref([]); // 上传的证据文件列表
const previewUrls = ref([]); // 预览图片URL列表
const submitLoading = ref(false); // 提交按钮加载状态
const isUploadDisabled = ref(false); // 上传按钮是否禁用

// 监听父组件传入的可见性变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 显示弹窗时初始化
    initReportForm();
  }
});

// 初始化表单
const initReportForm = () => {
  musicReportForm.omdMusicReportType = '';
  musicReportForm.omdMusicReportReason = '';
  musicReportForm.omdMusicReportEvidence = '';
  fileList.value = [];
  previewUrls.value = [];
  isUploadDisabled.value = false;
  if (musicReportFormRef.value) {
    musicReportFormRef.value.clearValidate();
  }
};

// 表单引用
const musicReportFormRef = ref(null);

// 举报类型变化处理
const handleTypeChange = (type) => {
  // 可根据类型给出提示（例如：选择"盗版侵权"时提示上传版权证明）
  if (type === '1') {
    ElMessage.info('请上传版权证明或授权文件截图作为证据');
  }
};

// 上传成功回调
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

// 上传失败回调
const handleUploadError = (error) => {
  console.error('证据上传错误:', error);
  ElMessage.error('证据上传失败，请重试');
};

// 超过上传数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传3张证据图片');
};

// 删除证据
const handleRemoveEvidence = (index) => {
  fileList.value.splice(index, 1);
  updateEvidenceUrls();
};

// 更新证据URL字符串（多个用逗号分隔）
const updateEvidenceUrls = () => {
  previewUrls.value = fileList.value.map(file => file.url);
  musicReportForm.omdMusicReportEvidence = previewUrls.value.join(',');
};

// 提交举报
const handleSubmit = async () => {
  // 表单验证
  try {
    await musicReportFormRef.value.validate();
  } catch (error) {
    return; // 验证失败不提交
  }
  // 检查是否有证据（非必填，但建议上传）
  if (fileList.value.length === 0) {
    try {
      await ElMessageBox.confirm(
          '未上传任何证据，可能影响举报处理结果，仍要提交吗？',
          '提示',
          {
            confirmButtonText: '继续提交',
            cancelButtonText: '取消',
            type: 'warning'
          }
      );
    } catch (error) {
      return; // 取消提交
    }
  }

  // 提交举报
  submitLoading.value = true;
  try {
    const response = await insertMusicReportService(musicReportForm);
    if (response.code === 0) {
      ElMessage.success('举报提交成功，我们将尽快审核');
      emit('reportSuccess'); // 通知父组件举报成功
      handleClose(); // 关闭弹窗
    } else {
      ElMessage.error('举报提交失败');
    }
  } catch (error) {
    console.error('举报提交失败:', error);
    ElMessage.error('网络异常，举报提交失败');
  } finally {
    submitLoading.value = false;
  }
};

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false); // 通知父组件更新visible状态
};
</script>


<template>
  <el-dialog
      v-model="props.visible"
      title="举报音乐"
      :width="dialogWidth"
      :before-close="handleClose"
      destroy-on-close
  >
    <!-- 举报表单 -->
    <el-form
        ref="musicReportFormRef"
        :model="musicReportForm"
        :rules="reportRules"
        label-width="100px"
        style="margin-top: 15px"
    >
      <!-- 隐藏的被举报音乐ID（从父组件传入） -->
      <el-form-item
          prop="omdMusicInfoId"
          hidden
      >
      </el-form-item>

      <!-- 举报类型 -->
      <el-form-item
          label="举报类型"
          prop="omdMusicReportType"
      >
        <el-select
            v-model="musicReportForm.omdMusicReportType"
            placeholder="请选择举报类型"
            clearable
            @change="handleTypeChange"
        >
          <el-option label="盗版侵权" value="1" />
          <el-option label="色情低俗" value="2" />
          <el-option label="暴力血腥" value="3" />
          <el-option label="政治敏感" value="4" />
          <el-option label="其他违规" value="5" />
        </el-select>
      </el-form-item>

      <!-- 举报原因 -->
      <el-form-item
          label="举报原因"
          prop="omdMusicReportReason"
      >
        <el-input
            v-model="musicReportForm.omdMusicReportReason"
            type="textarea"
            :rows="4"
            placeholder="请详细描述举报原因（例如：该音乐为盗版内容，未获得版权授权）..."
        />
      </el-form-item>

      <!-- 证据上传 -->
      <el-form-item label="证据上传">
        <el-upload
            class="music-evidence-upload"
            action="/api/user/evidenceFileUpload"
            name="evidenceFile"
            :headers="{ Authorization: `Bearer ${authStore.token}` }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="3"
            :file-list="fileList"
            :on-exceed="handleExceed"
            accept="image/*"
            :disabled="isUploadDisabled"
        >
          <el-button type="primary" icon="Upload">选择证据图片</el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持上传jpg/png格式图片，最多3张（例如：版权证明截图、违规内容截图）
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <!-- 已上传证据预览 -->
      <el-form-item
          v-if="fileList.length > 0"
          label="已上传证据"
      >
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
                  class="delete-btn"
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
      <el-button @click="handleClose">取消</el-button>
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
.music-evidence-upload {
  margin-top: 10px;
}

.evidence-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
}

.evidence-img {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  cursor: zoom-in;
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

.delete-btn {
  color: #f56c6c;
  margin-top: 5px;
}

::v-deep .el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
  padding-bottom: 10px;
}

::v-deep .el-select__popper,
::v-deep .el-dialog__header {
  z-index: 1000 !important;
}
</style>