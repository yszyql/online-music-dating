<script setup>
import { onMounted, watch} from "vue";
import {ElMessage, ElMessageBox} from 'element-plus';
import {useRouter} from 'vue-router';

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'
import { useMusicStore } from "@/stores/music.js";

// 导入服务器接口
import {checkLoginStatusService} from "@/api/public.js";
import {getUserInfoService , logoutService} from '@/api/user.js';

// 路由器实例
const router = useRouter();

// 状态管理
const authStore = useAuthStore();
const musicStore = useMusicStore();

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 检查登录状态
const checkLogin = async () => {
  try {
    const response = await checkLoginStatusService();
    return response.data;
  } catch (error) {
    console.error('检查登录状态失败', error);
    ElMessage.error('检查登录状态失败');
    return false;
  }
};

// 获取个人信息
const getUserInfo = async () => {

  try {
    const result = await getUserInfoService();
    authStore.setUserInfo(result.data);
    return true;
  } catch (error) {
    console.error('获取用户信息失败', error);
    ElMessage.error('获取用户信息失败，请稍后再试');
    return false;
  }
};


// 组件挂载时执行
onMounted(async () => {
  try {
    const hasToken = authStore.token !== '';

    if (hasToken) {
      const isLogin = await checkLogin();
      if (isLogin) {
        await getUserInfo();
      }
    }
  } catch (error) {
    console.error('组件初始化失败', error);
    ElMessage.error('组件初始化失败');
  }
});

// 跳转到个人信息页面
const goToDetail = () => {
  if (authStore.isAdmin) {
    router.push({
      path: `/admin/`
    });
  }else if (authStore.isSinger) {
    router.push({
      path: `/singer`
    });
  }else{
    router.push({
      path: `/user`
    })
  }
};

// 右上角用户信息跳转事件
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm(
        '你确认退出登录吗？',
        '温馨提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )
        .then(async () => {
          const response = await logoutService();
          if (response.code === 0) {
            // 清空状态
            authStore.logout();
            musicStore.clearPersistentData();
            router.push('/login');
          }
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '取消退出',
          });
        });
  } else if (command === 'info') {
    // 根据角色动态跳转
    if (authStore.isAdmin) {
      router.push({
        path: `/admin/info`
      });
    } else if (authStore.isSinger) {
      router.push({
        path: `/singer/info`
      });
    } else{
      router.push({
        path: `/user/info`
      });
    }
  } else {
    // 其他命令默认跳转
    router.push('/user/' + command);
  }
};

// 监听token变化，重新获取用户信息
watch(
    () => authStore.token,
    (newToken, oldToken) => {
      if (newToken && !oldToken) {
        // token从无到有，重新获取用户信息
        getUserInfo();
      }
    }
);

</script>


<template>

  <el-menu class="el-menu-demo" mode="horizontal" :ellipsis="false" router>

    <!-- 首页 - 始终显示 -->
    <el-menu-item index="/">
      <el-link href="/" underline="hover">
        <el-icon>
          <Headset/>
        </el-icon>
        <strong :style="{ fontSize: '20px' }">首页</strong>
      </el-link>
    </el-menu-item>

    <!-- 分类浏览 - 始终显示 -->
    <el-menu-item index="/music/categoryBrowsing">
      <el-icon>
        <Filter/>
      </el-icon>
      分类浏览
    </el-menu-item>

    <!-- 音乐盒 - 登录后显示 -->
    <el-menu-item v-if="authStore.isLoggedIn" index="/music/musicBox">
      <el-icon>
        <VideoPlay/>
      </el-icon>
      音乐盒
    </el-menu-item>

    <!-- 短信息 - 登录后显示 -->
    <el-menu-item v-if="authStore.isLoggedIn" index="/music/chatMessage">
      <el-icon>
        <ChatDotSquare/>
      </el-icon>
      短信息
    </el-menu-item>

    <!-- 上传歌曲 - 歌手角色显示 -->
    <el-menu-item v-if="authStore.isLoggedIn && authStore.isSinger" index="/singer/uploadMusic">
      <el-icon>
        <Promotion/>
      </el-icon>
      上传歌曲
    </el-menu-item>

    <!-- 系统管理 - 管理员角色显示 -->
    <el-menu-item v-if="authStore.isLoggedIn && authStore.isAdmin" index="/admin">
      <el-icon>
        <Tools/>
      </el-icon>
      系统管理
    </el-menu-item>

    <!-- 用户菜单 - 登录后显示 -->
    <el-menu-item v-if="authStore.isLoggedIn" @click="goToDetail">
      <el-dropdown placement="bottom-end" @command="handleCommand">
        <div class="el-dropdown__box">
          <strong :style="{ fontSize: '16px' }">
            <el-tag v-if="authStore.isAdmin" type="danger">
              管理员
            </el-tag>
            <el-tag v-else-if="authStore.isSinger" type="success">
              歌手
            </el-tag>
            <el-tag v-else>
              普通用户
            </el-tag>
            {{ authStore.userInfo?.omdUserNickname || authStore.userInfo?.omdUserName }}
          </strong>
          <el-avatar :src="authStore.userInfo?.omdUserAvatar || defaultAvatar"/>
        </div>
        <!-- 下拉菜单 -->
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="info">
              <el-icon>
                <Postcard/>
              </el-icon>
              基本资料
            </el-dropdown-item>
            <el-dropdown-item command="updatePassword" v-if="!authStore.isSinger && !authStore.isAdmin">
              <el-icon>
                <EditPen/>
              </el-icon>
              重置密码
            </el-dropdown-item>
            <el-dropdown-item command="applicationSinger" v-if="!authStore.isSinger && !authStore.isAdmin">
              <el-icon>
                <Stamp/>
              </el-icon>
              成为歌手
            </el-dropdown-item>
            <el-dropdown-item command="logout">
              <el-icon>
                <SwitchButton/>
              </el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-menu-item>

    <!-- 登录/注册 - 未登录时显示 -->
    <el-menu-item v-else index="/login">
      <el-link href="/login" underline="hover">
        <el-icon><User /></el-icon>
        登录/注册
      </el-link>
    </el-menu-item>
  </el-menu>

</template>


<style scoped>
/* 形成左 - 右分布的效果 */
.el-menu--horizontal {
  display: flex;
  align-items: center;
  width: 100%;
}

/* 形成左 - 右分布的效果 */
.el-menu--horizontal > .el-menu-item:last-child {
  margin-left: auto;
}

/* 移除下拉菜单触发元素的焦点轮廓（黑框） */
.el-dropdown__box:focus {
  /* 强制移除焦点轮廓 */
  outline: none !important;
}

</style>