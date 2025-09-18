<script setup>

// 导入钩子函数
import {onMounted} from 'vue'

// 禁止滚动
const disableScroll = () => {
  document.body.style.overflow = 'hidden'
  document.documentElement.style.overflow = 'hidden' // 兼容某些浏览器
}
// 在页面加载时就禁止滚动
onMounted(() => {
  disableScroll() // 应用加载时禁止滚动
})


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
              <el-menu-item index="/admin/info">
                <el-icon>
                  <User/>
                </el-icon>
                <span>基本资料</span>
              </el-menu-item>
              <el-menu-item index="/admin/userList">
                <el-icon>
                  <Cpu/>
                </el-icon>
                <span>用户列表</span>
              </el-menu-item>
              <el-menu-item index="/admin/musicList">
                <el-icon>
                  <Mic/>
                </el-icon>
                <span>歌曲列表</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewMusic">
                <el-icon>
                  <Headset/>
                </el-icon>
                <span>音乐审核</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewSingerApplication">
                <el-icon>
                  <Postcard/>
                </el-icon>
                <span>歌手申请</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewFeedback">
                <el-icon>
                  <Service />
                </el-icon>
                <span>反馈信息</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewCommentReport">
                <el-icon>
                  <ChatSquare/>
                </el-icon>
                <span>评论举报</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewUserReport">
                <el-icon>
                  <UserFilled />
                </el-icon>
                <span>用户举报</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewMusicReport">
                <el-icon>
                  <Ticket />
                </el-icon>
                <span>音乐举报</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewUserAppeal">
                <el-icon>
                  <Microphone />
                </el-icon>
                <span>用户申诉</span>
              </el-menu-item>
              <el-menu-item index="/admin/reviewMusicAppeal">
                <el-icon>
                  <Flag />
                </el-icon>
                <span>音乐申诉</span>
              </el-menu-item>
              <el-menu-item index="/admin/adminList">
                <el-icon>
                  <Edit/>
                </el-icon>
                <span>编辑管理员</span>
              </el-menu-item>
              <el-menu-item index="/admin/operationLog">
                <el-icon><SetUp /></el-icon>
                <span>操作日志</span>
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

          <AI/>

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
    height: calc(100vh - 30px - 80px);


    /* 左侧导航栏 */

    .el-aside {
      width: 15%;
      padding-left: 30px;

      /* 左侧导航栏的头部 */

      .mb-2 {
        text-align: center;
      }
    }

    /* 主体内容 */

    .el-main {
      height: 100%;
      padding: 0 50px;
      margin-top: 30px;
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
