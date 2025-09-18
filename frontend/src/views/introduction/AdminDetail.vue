<script setup>
import { onMounted, ref} from 'vue'
import { useRoute } from 'vue-router';

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png'

// 导入服务
import {getAdminInfoService} from "@/api/admin.js";

// 获取路由参数
const route = useRoute();

// 响应式变量
const routeUserId = route.params.omdAdminId; // 从路由参数中获取用户ID
const omdAdminData = ref(null); // 用户信息数据

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

// 钩子函数
onMounted(async () => {

  // 获取用户信息
  await getAdminInfoService(routeUserId).then(response => {
    omdAdminData.value = response.data;
  }).catch(error => {
    console.error('获取用户信息失败:', error);
  });

});

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

      <div class="user-info-container">
        <!-- 表格 -->
        <el-descriptions border :label-width="150">
          <el-descriptions-item :rowspan="2" :width="140" label="头像" align="center">
            <el-image style="width: 100px; height: 100px" :src="omdAdminData?.omdAdminAvatar || defaultAvatar"  prop="omdUserAvator"/>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="用户名" prop="omdUserName">{{ omdAdminData?.omdAdminName }}</el-descriptions-item>
          <el-descriptions-item align="center" label="身份" prop="omdRoleCode">
            <el-tag type="danger" size="small">{{ omdAdminData?.omdAdminRole === 0 ? '普通管理员' : '超级管理员' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="邮箱" prop="omdUserNickname">{{ omdAdminData?.omdAdminEmail || '暂无' }}</el-descriptions-item>
          <el-descriptions-item align="center" label="性别" prop="omdUserGender">{{ gender(omdAdminData?.omdUserGender) }}</el-descriptions-item>
          <el-descriptions-item align="center" label="创建时间" prop="omdUserCreateTime">{{ omdAdminData?.omdAdminCreateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

    </el-container>


    <template #footer>

    </template>

  </el-card>

</template>

<style scoped lang="scss">
.el-card {

  .card-header {
    text-align: center;
  }

  /* 卡片主体 */
  .el-container {

    .el-descriptions{
      padding: 50px 400px;
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