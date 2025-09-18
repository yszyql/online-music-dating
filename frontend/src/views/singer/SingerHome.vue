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
        <el-aside>
          <el-col>
            <h3 class="mb-2">
              <el-icon>
                <Guide/>
              </el-icon>
              导航栏
            </h3>
            <el-menu class="el-menu-vertical-demo" router>
              <el-menu-item index="/singer/info">
                <el-icon>
                  <User/>
                </el-icon>
                <span>基本资料</span>
              </el-menu-item>
              <el-menu-item index="/singer/updateSingerPassword">
                <el-icon>
                  <EditPen/>
                </el-icon>
                <span>重置密码</span>
              </el-menu-item>
              <el-menu-item index="/singer/uploadMusic">
                <el-icon>
                  <Promotion/>
                </el-icon>
                <span>上传音乐</span>
              </el-menu-item>
              <el-menu-item index="/singer/ownMusic">
                <el-icon>
                  <Histogram />
                </el-icon>
                <span>音乐信息</span>
              </el-menu-item>
              <el-menu-item index="/singer/musicReportToMyself">
                <el-icon>
                  <FolderRemove />
                </el-icon>
                <span>举报下架</span>
              </el-menu-item>
              <el-menu-item index="/singer/musicAppeal">
                <el-icon>
                  <List />
                </el-icon>
                <span>申诉信息</span>
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

  <!-- 音乐播放器组件 -->
  <MusicPlay/>

</template>

<style scoped>
.common-layout {
  height: 100vh;

  /* 头部导航栏 */

  .el-header {
    /* 背景色 */
    background-color: #fff;
    margin-bottom: 30px;
    padding-right: 50px;
  }

  /* 主体区域 */

  .principle {
    /* 减去头部和底部高度 */
    height: calc(100vh - 30px - 200px);


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
    background-color: #fff;
    display: flex;
    align-items: center;
    /* 让子元素在垂直方向居中（可选，根据需求调整） */
    flex-direction: column;
    justify-content: center;
    font-size: 14px;
    color: #666;
    margin-top: 20px;
  }
}
</style>
