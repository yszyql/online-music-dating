<script setup>
import { ref, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { insertSongAndLyricService } from '@/api/singer';

// 状态管理
const authStore = useAuthStore();

// 响应式数据
const songFileList = ref([]); // 歌曲文件列表
const lrcFileList = ref([]); // 歌词文件列表
const coverFileList = ref([]); // 封面文件列表
const submitLoading = ref(false); // 提交按钮加载状态
const uploadFormRef = ref(null); // 表单引用
const isDurationAutoCalculated = ref(false); // 标记是否已自动计算时长

// 表单数据
const formData = reactive({
  musicInfo: {
    omdMusicInfoName: '',
    omdMusicInfoAlbum: '',
    omdMusicInfoGenre: '',
    omdMusicInfoDuration: null,
    omdMusicInfoSongUrl: '',    // 歌曲文件URL（上传后填充）
    omdMusicInfoCoverUrl: ''    // 封面URL（上传后填充）
  },
  musicLyric: {
    omdMusicLyricLanguage: 'zh', // 默认中文
    omdMusicLyricContentUrl: ''  // 歌词文件URL（上传后填充）
  }
});

// 表单验证规则
const formRules = {
  'musicInfo.omdMusicInfoName': [
    { required: true, message: '请输入歌曲名称', trigger: 'blur' }
  ],
  'musicInfo.omdMusicInfoAlbum': [
    { required: true, message: '请输入专辑名称', trigger: 'blur' }
  ],
  'musicInfo.omdMusicInfoGenre': [
    { required: true, message: '请选择音乐流派', trigger: 'change' }
  ],
  'musicInfo.omdMusicInfoDuration': [
    { required: true, message: '请输入歌曲时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '歌曲时长必须大于0秒', trigger: 'blur' }
  ],
  'musicInfo.omdMusicInfoSongUrl': [
    { required: true, message: '请上传歌曲文件', trigger: 'change' }
  ],
  'musicLyric.omdMusicLyricContentUrl': [
    { required: true, message: '请上传歌词文件', trigger: 'change' }
  ],
  'musicInfo.omdMusicInfoCoverUrl': [
    { required: true, message: '请上传封面图片', trigger: 'change' }
  ]
};

// 歌曲上传成功回调
const handleSongUploadSuccess = (response, file) => {
  if (response.code === 0) {
    formData.musicInfo.omdMusicInfoSongUrl = response.data;
    file.url = response.data;
    songFileList.value = [file];
    ElMessage.success('歌曲文件上传成功');
  } else {
    ElMessage.error('歌曲文件上传失败');
  }
};

// 新增：文件选择后解析时长（关键逻辑）
const handleSongFileChange = (file) => {
  // 只处理新增文件
  if (file.status !== 'ready') return;

  // 使用Audio API解析时长
  const audio = new Audio();
  audio.src = URL.createObjectURL(file.raw); // 创建临时URL

  audio.onloadedmetadata = () => {
    // 解析成功：获取时长（秒，取整数）
    const duration = Math.floor(audio.duration);
    formData.musicInfo.omdMusicInfoDuration = duration;
    isDurationAutoCalculated.value = true; // 标记为自动计算
    ElMessage.success(`已自动计算时长：${formatDuration(duration)}`);

    // 释放临时URL
    URL.revokeObjectURL(audio.src);
  };

  audio.onerror = () => {
    // 解析失败：提示并允许手动输入
    ElMessage.warning('音频文件解析失败，无法自动获取时长，请手动输入');
    isDurationAutoCalculated.value = false; // 允许手动编辑
    URL.revokeObjectURL(audio.src);
  };
};

// 格式化时长（秒 → 分:秒，如 185 → 3:05）
const formatDuration = (seconds) => {
  if (!seconds || seconds < 0) return '0:00';
  const minute = Math.floor(seconds / 60);
  const second = seconds % 60;
  return `${minute}:${second.toString().padStart(2, '0')}`;
};

// 歌词上传成功回调
const handleLrcUploadSuccess = (response, file) => {
  if (response.code === 0) {
    formData.musicLyric.omdMusicLyricContentUrl = response.data;
    file.url = response.data;
    lrcFileList.value = [file];
    ElMessage.success('歌词文件上传成功');
  } else {
    ElMessage.error('歌词文件上传失败');
  }
};

// 封面上传成功回调
const handleCoverUploadSuccess = (response, file) => {
  if (response.code === 0) {
    formData.musicInfo.omdMusicInfoCoverUrl = response.data;
    file.url = response.data;
    coverFileList.value = [file];
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error('封面上传失败');
  }
};

// 上传失败回调
const handleUploadError = (err) => {
  console.error('文件上传错误:', err);
  ElMessage.error('文件上传失败，请重试');
};

// 移除歌曲文件
const handleSongRemove = () => {
  formData.musicInfo.omdMusicInfoSongUrl = '';
  formData.musicInfo.omdMusicInfoDuration = null;
  isDurationAutoCalculated.value = false;
  songFileList.value = [];
};

// 移除歌词文件
const handleLrcRemove = () => {
  formData.musicLyric.omdMusicLyricContentUrl = '';
  lrcFileList.value = [];
};

// 移除封面
const handleCoverRemove = () => {
  formData.musicInfo.omdMusicInfoCoverUrl = '';
  coverFileList.value = [];
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await uploadFormRef.value.validate();

    // 检查是否所有文件都已上传
    if (!formData.musicInfo.omdMusicInfoSongUrl) {
      return ElMessage.error('请等待歌曲文件上传完成');
    }
    if (!formData.musicLyric.omdMusicLyricContentUrl) {
      return ElMessage.error('请等待歌词文件上传完成');
    }
    if (!formData.musicInfo.omdMusicInfoCoverUrl) {
      return ElMessage.error('请等待封面上传完成');
    }

    // 构造请求参数
    const requestMap = {
      musicInfo: formData.musicInfo,
      musicLyric: formData.musicLyric
    };

    submitLoading.value = true;
    // 调用接口提交数据
    const response = await insertSongAndLyricService(requestMap);

    if (response.code === 0) {
      ElMessage.success('歌曲上传成功！');
      handleReset(); // 重置表单
    } else {
      ElMessage.error('歌曲上传失败');
    }
  } catch (error) {
    console.error('提交失败:', error);
    ElMessage.error('表单验证失败，请检查填写内容');
  } finally {
    submitLoading.value = false;
  }
};

// 重置表单
const handleReset = () => {
  uploadFormRef.value.resetFields();
  // 清空文件列表和URL
  songFileList.value = [];
  lrcFileList.value = [];
  coverFileList.value = [];
  formData.musicInfo.omdMusicInfoSongUrl = '';
  formData.musicInfo.omdMusicInfoCoverUrl = '';
  formData.musicLyric.omdMusicLyricContentUrl = '';
};
</script>


<template>
  <div class="music-upload-page">
    <!-- 页面标题 -->
    <div class="page-title">
      <h2>歌曲上传</h2>
      <p class="title-desc">请填写歌曲信息并上传音频及歌词文件</p>
    </div>

    <!-- 上传表单 -->
    <el-card class="upload-card">
      <el-form
          ref="uploadFormRef"
          :model="formData"
          :rules="formRules"
          label-width="140px"
          class="upload-form"
      >
        <!-- 歌曲名称 -->
        <el-form-item label="歌曲名称" prop="musicInfo.omdMusicInfoName">
          <el-input
              v-model="formData.musicInfo.omdMusicInfoName"
              placeholder="请输入歌曲名称"
              clearable
          />
        </el-form-item>

        <!-- 专辑名称 -->
        <el-form-item label="专辑名称" prop="musicInfo.omdMusicInfoAlbum">
          <el-input
              v-model="formData.musicInfo.omdMusicInfoAlbum"
              placeholder="请输入专辑名称"
              clearable
          />
        </el-form-item>

        <!-- 音乐流派 -->
        <el-form-item label="音乐流派" prop="musicInfo.omdMusicInfoGenre">
          <el-select
              v-model="formData.musicInfo.omdMusicInfoGenre"
              placeholder="请选择音乐流派"
              clearable
          >
            <el-option label="流行" value="流行" />
            <el-option label="摇滚" value="摇滚" />
            <el-option label="R&B" value="R&B" />
            <el-option label="古典" value="古典" />
            <el-option label="爵士" value="爵士" />
            <el-option label="嘻哈" value="嘻哈" />
            <el-option label="电子" value="电子" />
            <el-option label="民谣" value="民谣" />
            <el-option label="古风" value="古风" />
            <el-option label="Pop" value="Pop" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 歌词语言 -->
        <el-form-item label="歌词语言" prop="omdMusicLyricLanguage">
          <el-select
              v-model="formData.musicLyric.omdMusicLyricLanguage"
              placeholder="请选择歌词语言"
              clearable
              style="width: 100%"
          >
            <el-option label="中文" value="zh" />
            <el-option label="英语" value="en" />
            <el-option label="日语" value="jp" />
            <el-option label="韩语" value="kr" />
            <el-option label="法语" value="fr" />
            <el-option label="西班牙语" value="es" />
          </el-select>
        </el-form-item>

        <!-- 歌曲时长 (秒) -->
        <el-form-item label="歌曲时长(秒)" prop="musicInfo.omdMusicInfoDuration">
          <el-input
              v-model="formData.musicInfo.omdMusicInfoDuration"
              placeholder="上传歌曲后自动计算，也可手动修改"
              type="number"
              min="1"
              :disabled="isDurationAutoCalculated"
              style="margin-bottom: 10px"
          />
          <el-tooltip effect="light" content="上传音频后自动计算时长" placement="right" style="margin-top: 10px">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
          <p v-if="formData.musicInfo.omdMusicInfoDuration" class="duration-display">
            时长预览：{{ formatDuration(formData.musicInfo.omdMusicInfoDuration) }}
          </p>
        </el-form-item>

        <!-- 歌曲文件上传 -->
        <el-form-item label="歌曲文件" prop="songFile" class="upload-item">
          <el-upload
              class="audio-upload"
              action="/api/singer/songFileUpload"
              name="songFile"
              :headers="{ Authorization: `Bearer ${authStore.token}` }"
              :on-success="handleSongUploadSuccess"
              :on-change="handleSongFileChange"
              :on-error="handleUploadError"
              :file-list="songFileList"
              :on-remove="handleSongRemove"
              accept=".mp3,.wav"
              :limit="1"
              :disabled="songFileList.length >= 1"
          >
            <el-button type="primary" icon="Upload">选择音频文件</el-button>
          </el-upload>

          <!-- 上传成功后显示文件信息 -->
          <div v-if="songFileList.length" class="file-info">
            <el-icon class="file-icon"><Music /></el-icon>
            <div class="file-details">
              <p class="file-name">{{ songFileList[0].name }}</p>
              <p class="file-size">{{ formatFileSize(songFileList[0].size) }}</p>
            </div>
            <audio
                :src="songFileList[0].url"
                controls
                class="audio-player"
            />
          </div>
          <div class="upload-tip">支持 mp3、wav 格式，单个文件不超过 50MB</div>
        </el-form-item>

        <!-- 歌词文件上传 -->
        <el-form-item label="歌词文件" prop="lrcFile" class="upload-item">
          <el-upload
              class="lrc-upload"
              action="/api/singer/lrcFileUpload"
              name="lrcFile"
              :headers="{ Authorization: `Bearer ${authStore.token}` }"
              :on-success="handleLrcUploadSuccess"
              :on-error="handleUploadError"
              :file-list="lrcFileList"
              :on-remove="handleLrcRemove"
              accept=".lrc"
              :limit="1"
              :disabled="lrcFileList.length >= 1"
          >
            <el-button type="primary" icon="Upload">选择歌词文件</el-button>
          </el-upload>

          <!-- 上传成功后显示文件信息 -->
          <div v-if="lrcFileList.length" class="file-info">
            <el-icon class="file-icon"><Document /></el-icon>
            <div class="file-details">
              <p class="file-name">{{ lrcFileList[0].name }}</p>
              <p class="file-size">{{ formatFileSize(lrcFileList[0].size) }}</p>
            </div>
          </div>

          <div class="upload-tip">支持 lrc 格式歌词文件，不超过 1MB</div>
        </el-form-item>

        <!-- 封面上传区域 -->
        <el-form-item label="专辑封面" prop="coverFile" class="upload-item">
          <el-upload
              class="image-upload"
              action="/api/music/coverFileUpload"
              name="coverFile"
              :headers="{ Authorization: `Bearer ${authStore.token}` }"
          :on-success="handleCoverUploadSuccess"
          :on-error="handleUploadError"
          :file-list="coverFileList"
          :on-remove="handleCoverRemove"
          accept=".jpg,.jpeg,.png"
          :limit="1"
          :disabled="coverFileList.length >= 1"
          >
          <el-button type="primary" icon="Upload">选择封面图片</el-button>
          </el-upload>

          <!-- 封面预览 -->
          <div v-if="coverFileList.length" class="cover-preview">
            <img
                v-if="coverFileList[0].url"
                :src="coverFileList[0].url"
                fit="cover"
                class="preview-img"
            />
          </div>

          <div class="upload-tip">支持 jpg、png 格式，建议尺寸 500x500px</div>
        </el-form-item>

        <!-- 提交按钮区域 -->
        <el-form-item class="form-actions">
          <el-button
              type="primary"
              @click="handleSubmit"
              :loading="submitLoading"
              class="submit-btn"
          >
            <el-icon><Check /></el-icon>
            提交歌曲
          </el-button>
          <el-button
              type="default"
              @click="handleReset"
              class="reset-btn"
          >
            <el-icon><Refresh /></el-icon>
            重置表单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>



<style scoped>
.music-upload-page {
  max-width: 900px;
  margin: 30px auto;
  padding: 0 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.page-title .title-desc {
  color: #666;
  font-size: 14px;
}

.upload-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: none;
}

.upload-form {
  padding: 30px 20px;
}

.upload-item {
  margin-bottom: 15px;
}

.audio-upload, .lrc-upload, .image-upload {
  margin-bottom: 15px;
}

.file-info {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background: #f9f9f9;
  border-radius: 6px;
  width: 100%;
  max-width: 600px;
}

.file-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 15px;
}

.file-details {
  flex: 1;
  overflow: hidden;
}

.file-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  font-size: 14px;
}

.file-size {
  margin: 5px 0 0 0;
  font-size: 12px;
  color: #888;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-left: 15px;
  padding-bottom: 30px;
}

.cover-preview {
  margin-top: 15px;
}

.preview-img {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #eee;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-btn {
  min-width: 120px;
  margin-right: 15px;
}

.reset-btn {
  min-width: 120px;
}

/* 新增样式：时长显示 */
.duration-display {
  margin-left: 10px;
  color: #67c23a; /* 绿色提示 */
  font-size: 13px;
}

.info-icon {
  margin-left: 8px;
  color: #409eff;
  cursor: help;
}

/* 美化上传按钮 */
::v-deep .el-upload--text .el-button {
  background-color: #409eff;
  color: white;
  border: none;
  transition: all 0.3s;
}

::v-deep .el-upload--text .el-button:hover {
  background-color: #66b1ff;
}

/* 表单元素间距优化 */
::v-deep .el-form-item {
  margin-bottom: 20px;
}

/* 输入框和选择器美化 */
::v-deep .el-input__inner,
::v-deep .el-select__inner {
  border-radius: 6px;
  padding: 10px 15px;
}
</style>