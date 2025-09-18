<script setup>
import {computed, ref} from 'vue'
import {ElMessage} from "element-plus";

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png'

// 导入状态管理
import { useAuthStore } from '@/stores/auth';

// 导入服务
import {getUserInfoService, userInfoUpdateService} from "@/api/user.js";
import {updateAdminInfoService} from "@/api/admin.js";

// 状态管理
const authStore = useAuthStore();

// 响应式变量
const dialogVisible = ref(false); // 编辑对话框是否可见

// 表单绑定数据
const editFormData = ref({
  omdUserAvatar: authStore.userInfo.omdUserAvatar || defaultAvatar,
  omdUserNickname: authStore.userInfo.omdUserNickname || '',
  omdUserBirthday:  authStore.userInfo.omdUserBirthday || '',
  omdUserEmail:  authStore.userInfo.omdUserEmail || '',
  omdUserGender:  authStore.userInfo.omdUserGender || 2,
  omdUserRegion:  authStore.userInfo.omdUserRegion || '',
})

const adminFormData = ref({
  omdAdminEmail: editFormData.omdAdminEmail || '',
  omdAdminAvatar: editFormData.omdAdminAvatar || defaultAvatar,
})

// 地区选项
const regions = [
  {
    value: '中国',
    label: '中国',
  },
  {
    value: '国外',
    label: '国外',
  }
]

// 表单ref
const editFormRef = ref(null)

// 表单验证规则
const editFormRules = ref({
  omdUserNickname: [
    { message: '请输入昵称', trigger: 'blur'},
    {min: 1, max: 16, message: '昵称长度必须在1到16个字符之间', trigger: 'blur'}
  ],
  omdUserEmail: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,message: '请输入有效的邮箱地址',trigger: 'blur'}
  ]
})

// 性别计算
const gender = (genderData) =>{
  if(genderData === 0){
    return '男'
  }else if(genderData === 1){
    return '女'
  }else if(genderData === 2){
    return '未知'
  }else{
    return '未知'
  }
}

// 认证请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${authStore.token}`
  };
});

// 封面图片上传成功处理
const handleCoverSuccess = (response) => {
  if (response.data) {
    editFormData.value.omdUserAvatar = response.data;
    ElMessage.success('上传成功');
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

// 编辑表单提交
const edit = async (editFormData) => {
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userInfoUpdateService(editFormData)
        await updateAdminInfoService(adminFormData.value)
        ElMessage.success('修改成功');
        const updatedInfo = await getUserInfoService();
        // 更新本地存储
        authStore.setUserInfo(updatedInfo.data);
        localStorage.setItem('userInfo', JSON.stringify(authStore.userInfo));
        dialogVisible.value = false;
      }catch (error) {
        ElMessage.error('修改失败');
        console.error(error);
      }
    }
  })
};

</script>

<template>
  <el-card>

    <!-- 卡片头部 -->
    <template #header>

      <!-- 卡片标题 -->
      <div class="card-header">
        <h1>基本资料</h1>
      </div>


    </template>

    <!-- 卡片内容 -->
    <el-container>

      <!-- 登录用户的基本信息 -->
      <div class="user-info-container">
        <!-- 编辑按钮 -->
        <div class="user-info-button">
          <el-button type="primary" @click="dialogVisible = true">编辑</el-button>
        </div>
        <!-- 表格 -->
        <el-descriptions border :label-width="150">
          <el-descriptions-item :rowspan="2" :width="140" label="头像" align="center">
            <el-image style="width: 100px; height: 100px" :src="authStore.userInfo.omdUserAvatar || defaultAvatar"  prop="omdUserAvator"/>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="用户名" prop="omdUserName">{{ authStore.userInfo.omdUserName }}</el-descriptions-item>
          <el-descriptions-item align="center" label="身份" prop="omdRoleCode">
            <el-tag type="danger">管理员</el-tag>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="手机号" prop="omdUserPhone">{{ authStore.userInfo.omdUserPhone }}</el-descriptions-item>
          <el-descriptions-item align="center" label="邮箱" prop="omdUserEmail">{{ authStore.userInfo.omdUserEmail || '暂无' }}</el-descriptions-item>
          <el-descriptions-item align="center" label="昵称" prop="omdUserNickname">{{ authStore.userInfo.omdUserNickname || '暂无' }}</el-descriptions-item>
          <el-descriptions-item align="center" label="性别" prop="omdUserGender">{{ gender(authStore.userInfo.omdUserGender) }}</el-descriptions-item>
          <el-descriptions-item align="center" label="地区" prop="omdUserRegion">{{ authStore.userInfo.omdUserRegion || '暂无' }}</el-descriptions-item>
          <el-descriptions-item align="center" label="生日" prop="omdUserBirthday">{{ authStore.userInfo.omdUserBirthday || '暂无' }}</el-descriptions-item>
          <el-descriptions-item align="center" label="创建时间" prop="omdUserCreateTime">{{ authStore.userInfo.omdUserCreateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

    </el-container>

    <template #footer>

    </template>

  </el-card>

  <el-dialog
      title="编辑资料"
      v-model="dialogVisible"
      width="600px"
      :before-close="dialogVisible == false"
  >
    <el-form ref="editFormRef" size="large" label-width="100px" autocomplete="off" :rules="editFormRules" :model="editFormData">
      <el-form-item label="用户名：">
        <el-input prefix-icon="User" :placeholder="authStore.userInfo.omdUserName" disabled></el-input>
      </el-form-item>
      <el-form-item label="手机号：">
        <el-input prefix-icon="Cellphone" :placeholder="authStore.userInfo.omdUserPhone" disabled></el-input>
      </el-form-item>
      <el-form-item label="头像：">
        <el-upload
            class="cover-uploader"
            action="/api/user/avatarFileUpload"
            name="avatarFile"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            :headers="uploadHeaders"
        >
          <img v-if="editFormData.omdUserAvatar || (authStore.userInfo.omdUserAvatar)"
               :src="editFormData.omdUserAvatar || (authStore.userInfo.omdUserAvatar || editFormData.omdUserAvatar)"
               class="cover-image" style="width: 100px; height: 100px" />
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          <el-button type="primary" style="margin-left: 50px;">点击上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="昵称：" prop="omdUserNickname">
        <el-input prefix-icon="User" placeholder="请输入昵称" clearable v-model="editFormData.omdUserNickname"></el-input>
      </el-form-item>
      <el-form-item label="邮箱：" prop="omdUserEmail">
        <el-input prefix-icon="Message" placeholder="请输入邮箱" clearable v-model="editFormData.omdUserEmail"></el-input>
      </el-form-item>
      <el-form-item label="出生日期：">
        <el-date-picker
            type="date"
            placeholder="请选择您的生日"
            v-model="editFormData.omdUserBirthday"
        />
      </el-form-item>
      <el-form-item label="性别：">
        <el-radio-group v-model="editFormData.omdUserGender">
          <el-radio :value="0">
            <el-icon>
              <Male/>
            </el-icon>
            男
          </el-radio>
          <el-radio :value="1">
            <el-icon>
              <Female/>
            </el-icon>
            女
          </el-radio>
          <el-radio :value="2">
            <el-icon>
              <UserFilled/>
            </el-icon>
            保密
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="地区：">
        <el-select
            v-model="editFormData.omdUserRegion"
            clearable
            placeholder="请选择您的地区"
            style="width: 240px"
        >
          <el-option
              v-for="item in regions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <!-- 编辑按钮 -->
      <el-form-item>
        <el-button class="button" type="primary" @click="dialogVisible = false">
          取消
        </el-button>
        <el-button class="button" type="primary" auto-insert-space @click="edit(editFormData)">
          提交
        </el-button>
      </el-form-item>
    </el-form>

  </el-dialog>

</template>

<style scoped lang="scss">
.el-card {

  .card-header {
    text-align: center;
  }

  /* 卡片主体 */
  .el-container {

    .el-descriptions{
      padding: 50px 200px;
    }

    .el-descriptions-item {
      padding: 20px 0;
    }


    /* 按钮 */
    .user-info-button {
      display: flex;
      /* 子元素靠右对齐 */
      justify-content: flex-end;
    }


  }

  .playlist-container{
    margin-top: 50px;
  }


  /* 编辑按钮 */
  .button {
    margin-left: 10px;
  }

  /* 头像上传 */
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
    display: block;
    object-fit: cover;
  }

}
</style>