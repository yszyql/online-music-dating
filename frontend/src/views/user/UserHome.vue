<script setup>

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
              <el-menu-item index="/user/info">
                <el-icon>
                  <Postcard/>
                </el-icon>
                <span>基本资料</span>
              </el-menu-item>
              <el-menu-item index="/user/updatePassword">
                <el-icon>
                  <EditPen/>
                </el-icon>
                <span>重置密码</span>
              </el-menu-item>
              <el-menu-item index="/user/applicationSinger">
                <el-icon>
                  <Stamp/>
                </el-icon>
                <span>成为歌手</span>
              </el-menu-item>
              <el-menu-item index="/user/reportComment">
                <el-icon>
                  <Remove />
                </el-icon>
                <span>举报评论</span>
              </el-menu-item>
              <el-menu-item index="/user/reportUser">
                <el-icon>
                  <ChatSquare />
                </el-icon>
                <span>举报用户</span>
              </el-menu-item>
              <el-menu-item index="/user/reportMusic">
                <el-icon>
                  <Reading />
                </el-icon>
                <span>举报音乐</span>
              </el-menu-item>
              <el-menu-item index="/user/feedback">
                <el-icon>
                  <Notification/>
                </el-icon>
                <span>反馈信息</span>
              </el-menu-item>
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

          <!-- 底部区域 -->
          <el-footer>
            <Footer/>
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>

  <!-- AI -->
  <AI/>

  <!-- 音频播放 -->
  <MusicPlay/>

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
    height: calc(100vh - 60px - 200px);


    /* 左侧导航栏 */

    .el-aside {
      width: 15%;
      padding-left: 30px;
      margin-top: 130px;

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
    background-color: #fff;
    display: flex;
    align-items: center;
    /* 让子元素在垂直方向居中（可选，根据需求调整） */
    flex-direction: column;
    justify-content: center;
    font-size: 14px;
    color: #666;
    margin-top: 60px;
  }
}
</style>
