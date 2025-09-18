<script setup>
import {ref, onMounted} from 'vue';
import {useRouter} from "vue-router";

// 导入默认图片
import defaultAvatar from '@/assets/images/defaultAvatar.png';
import defaultCover from '@/assets/images/defaultCover.png';

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js';
import { useMusicStore } from '@/stores/music.js';

// 导入服务器接口
import {getOmdUserRoleListService, getPlaylistPublicListService} from '@/api/user.js';

// 导入自定义组件
import PlaylistModal from "@/components/playlist/PlaylistModal.vue";

// 状态管理
const authStore = useAuthStore();
const musicStore = useMusicStore();

// 初始化路由实例
const router = useRouter();

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);         // 总页数
const playlistList = ref([]); // 播放列表数据
const playlistSelect = ref([]); // 播放列表选中项
const playlistDetailVisible = ref(false);
const totalCount = ref(0); // 符合条件的歌单总条数

// 监听页码变化
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  fetchPublicPlaylists(pageNum, pageSize.value);
};

// 监听每页数量变化
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  fetchPublicPlaylists(pageNum.value, pageSize);
};

// 获取播放列表
const fetchPublicPlaylists = async (pageNum,pageSize) => {

  try {
    const result = await getPlaylistPublicListService(pageNum,pageSize);
    playlistList.value = result.data.items || [];
    totalPages.value = result.data.total || 0;
    totalCount.value = result.data.total || 0;
  } catch (error) {
    console.error('获取播放列表失败:', error);
  }
};

// 加载播放列表里的音乐信息
const loadPlaylist = async (playlist) => {
  try {
    playlistSelect.value = playlist;
    musicStore.playlist.value = playlist;
    playlistDetailVisible.value = true;
  }catch (error) {
    console.error('获取歌单里的音乐列表失败：', error);
  }
};

// 跳转到用户基本信息页面
const goToUserInfo = async (omdUserId) => {
  if (!omdUserId) {
    console.error('用户ID不存在，无法跳转');
    return;
  }

  const result = await getOmdUserRoleListService(omdUserId);
  const isSinger = result.data.some(role => role.omdRoleCode === 'ROLE_SINGER');

  if (isSinger) {
    router.push({
      path: `/introduction/singerDetail/${omdUserId}`
    });
  }else {

    router.push({
      path: `/introduction/userDetail/${omdUserId}`
    });
  }

};

// 组件挂载时获取用户播放列表
onMounted( async () => {

  await fetchPublicPlaylists(pageNum.value,pageSize.value);

});

</script>

<template>
  <!-- 播放列表主容器 -->
  <div class="playlists-section">
    <!-- 背景装饰 -->
    <div class="bg-pattern"></div>

    <!-- 内容卡片 -->
    <div class="playlists-card">
      <!-- 顶部标题栏 -->
      <div class="playlist-header">
        <h2>随机推荐歌单</h2>
      </div>

      <!-- 播放列表内容区（栅栏布局） -->
      <div class="playlist-container">
        <!-- 单个播放列表卡片 -->
        <div
            v-for="(playlist, index) in playlistList"
            :key="playlist.omdPlaylistId"
            class="playlist-item"
        >
          <div @click="loadPlaylist(playlist)">

            <!-- 圆形封面 -->
            <div class="playlist-cover">
              <div class="cover-wrapper">
                <img
                    :src="playlist.omdPlaylistCover || defaultCover"
                    :alt="playlist.omdPlaylistName"
                    class="cover-img"
                >
              </div>
            </div>

            <!-- 信息区域 -->
            <div class="playlist-info">
              <h3 class="playlist-title">{{ playlist.omdPlaylistName }}</h3>
              <p class="create-time">
                创建于 {{ playlist.omdPlaylistCreateTime }}
              </p>
            </div>

          </div>

            <!-- 用户信息（头像+用户名同一排） -->
            <div class="user-info" @click="goToUserInfo(playlist.omdUser.omdUserId)">
              <img
                  :src="playlist.omdUser.omdUserAvatar || defaultAvatar"
                  :alt="playlist.omdUser.omdUserName"
                  class="user-avatar"
              >
              <span class="user-name">
                {{ playlist.omdUser.omdUserNickname || playlist.omdUser.omdUserName }}
              </span>
            </div>
          </div>


        <!-- 空状态 -->
        <div v-if="playlistList.length === 0" class="playlist-empty-state">
          <p>暂无分享歌单</p>
        </div>

      </div>
    </div>

    <!-- 分页组件 -->
    <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 15 , 20, 25]"
        :total="totalPages"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        layout="prev, pager, next, jumper, ->, total, sizes"
    />
  </div>

  <!-- 播放列表详情弹窗 -->
  <PlaylistModal
      :playlist="playlistSelect"
      :visible="playlistDetailVisible"
      @close="playlistDetailVisible = false"
  />
</template>

<style scoped lang="scss">
// 栅栏布局容器
.playlist-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); // 自动适应宽度，最小240px
  gap: 24px; // 卡片间距
  padding: 20px;
  margin-top: 10px;
}

// 单个卡片样式
.playlist-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease; // 过渡动画
  cursor: pointer;
  overflow: hidden;

  // 悬浮效果：轻微放大+阴影加深
  &:hover {
    transform: scale(1.03); // 轻微放大
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }
}

// 圆形封面
.playlist-cover {
  width: 100%;
  margin-bottom: 16px;

  .cover-wrapper {
    width: 100%;
    padding-top: 100%; // 正方形比例（宽高相等）
    position: relative;
    border-radius: 50%; // 圆形
    overflow: hidden; // 裁剪成圆形
    background: #f0f0f0;
  }

  .cover-img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover; // 封面自适应填充
    transition: transform 0.5s ease;

    // 悬浮时封面微放大
    .playlist-item:hover & {
      transform: scale(1.08);
    }
  }
}

// 歌单信息区域
.playlist-info {
  padding: 0 4px;

  .playlist-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.3s ease; // 颜色过渡

    // 悬浮时变蓝色
    .playlist-item:hover & {
      color: #1677ff; // 蓝色（可根据主题调整）
    }
  }

  .create-time {
    font-size: 12px;
    color: #868686;
    margin-bottom: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// 用户信息（头像+用户名同一排）
.user-info {
  display: flex;
  align-items: center;
  gap: 8px; // 头像和用户名间距

  .user-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%; // 圆形头像
    object-fit: cover;
    background: #f0f0f0;
  }

  .user-name {
    font-size: 13px;
    color: #666;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1; // 用户名占剩余空间，避免溢出
    border-bottom: 1px solid transparent; // 初始下划线透明
    transition: color 0.3s ease, border-bottom 0.3s ease; // 颜色和下划线过渡

    // 悬浮时变蓝色
   .playlist-item:hover & {
     color: #1677ff; // 蓝色（可根据主题调整）
     border-bottom: 1px #1677ff solid;
     width: 100%;
   }

  }
}

// 空状态样式
.playlist-empty-state {
  grid-column: 1 / -1; // 占满整行
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}

// 其他基础样式
.playlists-card {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 12px;
  margin: 20px auto;
  max-width: 1200px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.playlist-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;

  h2 {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}
</style>