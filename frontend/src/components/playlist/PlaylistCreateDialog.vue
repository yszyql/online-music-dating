<script setup>
import {ref, computed} from 'vue';
import { Plus } from '@element-plus/icons-vue';
import {ElMessage} from "element-plus";

// 导入状态管理
import { useAuthStore } from '@/stores/auth';

// props 和 emits
const props = defineProps({
  showDialog: {
    type: Boolean,
    default:false,
  },
  formData: {
    type:Object,
    default: () => ({})
  },
  formRules: {
    type:Object,
    default: () => ({})
  },
  formLoading: {
    type:Boolean,
    default:false,
  },
  playlist:{
    type:Object,
    default:() => ([])
  },
  isEdit:{
    type:Boolean,
    default:false,
  }
});
// 定义组件 emits
const emit = defineEmits(['createPlaylist','updatePlaylist', 'update:showDialog', 'update:formData']);

// 状态管理
const authStore = useAuthStore();

const formRef = ref(null);

// 计算属性，用于控制对话框的显示和隐藏
const showDialog = computed({
  get: () => props.showDialog,
  set: (value) => emit('update:showDialog', value)
});

// 封面图片上传成功处理
const handleCoverSuccess = (response) => {
  if (response.data) {
    emit('update:formData', { ...props.formData, omdPlaylistCover: response.data });
  }
};

// 上传前校验
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }

  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB');
    return false;
  }

  return true;
};

// 提交表单
const handleSubmit = () => {
  if (!formRef.value) return;

  formRef.value.validate((valid) => {
    if (valid) {
      if(props.isEdit) {
        // 编辑歌单
        emit('updatePlaylist');
      }else {
        // 创建歌单
        emit('createPlaylist');
      }
    }
  });

};

</script>

<template>
  <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑歌单' : '创建新歌单'"
      width="500px"
      :close-on-click-modal="false"
      class="create-playlist-dialog"
  >
    <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
    >
      <el-form-item label="歌单名称" prop="omdPlaylistName">
        <el-input
            v-model="formData.omdPlaylistName"
            :placeholder=" playlist?.omdPlaylistName || '请输入歌单名称' "
            clearable
        />
      </el-form-item>

      <el-form-item label="歌单描述" prop="omdPlaylistDescription">
        <el-input
            v-model="formData.omdPlaylistDescription"
            type="textarea"
            :rows="3"
            :placeholder="playlist?.omdPlaylistDescription || '请输入歌单描述（可选）' "
            clearable
        />
      </el-form-item>

      <el-form-item label="歌单封面" prop="omdPlaylistCover" v-if="isEdit">
        <el-upload
            class="cover-uploader"
            action="/api/music/coverFileUpload"
            name="coverFile"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            :headers="{ Authorization: `Bearer ${authStore.token}` }"
            >
        <img v-if="formData.omdPlaylistCover || (playlist && playlist.omdPlaylistCover)"
             :src="formData.omdPlaylistCover || (playlist?.omdPlaylistCover || defaultCover)"
             class="cover-image">
        <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="歌单可见性" prop="omdPlaylistPublic">
        <el-radio-group v-model="formData.omdPlaylistPublic">
          <el-radio :label="0">私密</el-radio>
          <el-radio :label="1">公开</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="showDialog = false">取消</el-button>
      <el-button
          type="primary"
          @click="handleSubmit"
          :loading="formLoading"
      >
        {{ isEdit ? '保存修改' : '创建歌单' }}
      </el-button>
    </template>
  </el-dialog>
</template>



<style scoped>
.create-playlist-dialog {
  .cover-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 150px;
    height: 150px;

    &:hover {
      border-color: #409eff;
    }
  }

  .cover-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 150px;
    height: 150px;
    line-height: 150px;
    text-align: center;
  }

  .cover-image {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
  }
}
</style>