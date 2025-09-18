<script setup>
import {useAuthStore} from "@/stores/auth.js";
const authStore = useAuthStore();
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- 头部 -->
      <el-header>
        <HeaderMenu/>
      </el-header>


        <!-- 主体区域 -->
        <el-container>
          <!-- 主体内容 -->
          <el-main>
            <!-- 利用路由导入页面 -->
            <router-view/>
          </el-main>

          <!-- AI -->
          <AI v-if="!authStore.isGuest"/>

          <!-- 底部区域 -->
          <el-footer>
            <Footer/>
          </el-footer>
        </el-container>
      </el-container>
  </div>

  <MusicPlay/>
</template>

<style scoped>
.common-layout {

  /* 头部导航栏 */

  .el-header {
    /* 背景色 */
    background-color: #fff;
    margin-bottom: 50px;
    padding-right: 50px;
  }

  /* 底部 */

  .el-footer {
    background-color: #fff;
    display: flex;
    align-items: center;
    /* 让子元素在垂直方向居中（可选，根据需求调整） */
    flex-direction: column;
    justify-content: center;
    font-size: 14px;
    color: #666;
    margin-top: 60px;
    padding-bottom: 170px;
  }
}
</style>
