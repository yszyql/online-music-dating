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

      <el-container class="principle">
        <!-- 左侧导航栏 default-active="2"-->
        <el-aside>
          <el-col>
            <h3 class="mb-2">
              <el-icon>
                <Guide/>
              </el-icon>
              导航栏
            </h3>
            <el-menu class="el-menu-vertical-demo" router>
              <el-menu-item index="/music/musicBox">
                <el-icon>
                  <VideoPlay/>
                </el-icon>
                <span>音乐盒</span>
              </el-menu-item>
              <el-menu-item index="/music/categoryBrowsing">
                <el-icon>
                  <Filter/>
                </el-icon>
                <span>分类浏览</span>
              </el-menu-item>
              <el-sub-menu index="/music/chatMessage">
                <template #title><el-icon><MessageBox /></el-icon>消息</template>
                <el-menu-item index="/music/chatMessage">
                  <el-icon>
                    <ChatDotSquare/>
                  </el-icon>
                  <span>短信息</span>
                </el-menu-item>
                <el-menu-item index="/music/chatNotice">
                  <el-icon><Promotion /></el-icon>
                  <span>好友通知</span>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-col>
        </el-aside>

        <!-- 主体区域 -->
        <el-container>
          <!-- 主体内容 -->
          <el-main>
            <!-- 利用路由导入页面 -->
            <router-view/>
          </el-main>

          <!-- AI -->
          <AI v-if="!authStore.isGuest"/>

          <!-- 音乐播放 -->
          <MusicPlay/>

          <!-- 底部区域 -->
          <el-footer>
            <Footer/>
          </el-footer>

        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.common-layout {
  height: 100vh;

  /* 头部导航栏 */

  .el-header {
    /* 背景色 */
    background-color: #fff;
    margin-bottom: 50px;
    padding-right: 50px;
  }

  /* 主体区域 */

  .principle {
    /* 减去头部和底部高度 */
    height: calc(100vh - 60px - 70px);


    /* 左侧导航栏 */

    .el-aside {
      width: 15%;
      padding-left: 30px;
      margin-top: 180px;

      /* 左侧导航栏的头部 */

      .mb-2 {
        text-align: center;
      }
    }

    /* 主体内容 */

    .el-main {
      height: 100%;
      padding: 0 50px;
    }

  }


  /* 底部 */
  .el-footer {
    /* 背景色 */
    background-color: #fff;
    text-align: center;
    color: #909399;
    font-size: 14px;
    margin-bottom: 130px;
    padding-bottom: 100px;
  }

}
</style>
