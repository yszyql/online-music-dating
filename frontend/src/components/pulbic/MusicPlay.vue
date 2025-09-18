<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {ElButton, ElEmpty, ElIcon, ElMessage, ElMessageBox} from 'element-plus';
import {CaretLeft, CaretRight, VideoPlay} from '@element-plus/icons-vue';
import router from "@/router/index.js";

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'
import {useMusicStore} from '@/stores/music.js';

// 导入组件
import PlaylistModal from "@/components/playlist/PlaylistModal.vue";
import PlaylistAddToDialog from "@/components/playlist/PlaylistAddToDialog.vue";
import ReportMusicDialog from "@/components/message/ReportMusicDialog.vue";

// 导入复合式函数
import {useAudioPlayer} from "@/composable/useAudioPlayer.js";
import {useMusicHandle} from "@/composable/useMusicHandle.js";
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";
import {getMusicReportStatusService} from "@/api/user.js";

// 状态管理
const musicStore = useMusicStore();
const authStore = useAuthStore();

// 响应式数据
const musicIdList = ref([musicStore.currentMusic.omdMusicInfoId]);
const musicReportVisible = ref(false); // 音乐举报弹窗是否显示
const musicReportStatus = ref(false); // 音乐举报状态

// 播放器相关组合函数
const {
  progressPercent,
  bufferPercent,
  playlistVisible,
  togglePlay,
  playNext,
  playPrev,
  seekByClick,
  startDrag,
  setVolume,
  toggleMute,
  formatTime,
  togglePlaylist,
  togglePlayMode,
  PlayMode,
  playMode,
  playModeName,
} = useAudioPlayer();

// 音乐处理组合函数
const {
  initMusicLikeInfo,
  getMusicLikeInfo,
  toggleLike,
} = useMusicHandle();

const {
  playlistList,
  loadingPlaylistList,
  showPlaylistAddToDialog,
  hasMore,
  openAddToPlaylistDialog,
  addSongToPlaylistHandler,
  loadMorePlaylists,
  handleBatchDownload
} = usePlaylistManager();

// 格式化音乐名称
const formatMusicName = (name) => {
  if (!name) return '';

  // 使用正则表达式判断是否包含书名号
  if (/[《》]/.test(name)) {
    // 包含书名号时进行替换
    return name.replace(/[《》]/g, '');
  }

  // 不包含书名号时直接返回
  return name;
};

// 处理添加按钮点击
const handleAddClick = async (song) => {
  await openAddToPlaylistDialog(song, [song.omdMusicInfoId]);
};

// 处理下载按钮点击
const handleSingleDownload = async (song) => {
  await handleBatchDownload(song);
};

// 打开举报弹窗
const openMusicReportPopup = () => {
  ElMessageBox.confirm(
      '确定要举报该音乐吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    musicReportVisible.value = true;
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: '已取消举报'
    });
  })
};

// 获取音乐举报状态
const getReportStatus = async () => {
  // 如果是游客，直接返回
  if (authStore.isGuest) {
    return
  }
  try {
    const response = await getMusicReportStatusService(musicStore.currentMusic.omdMusicInfoId);
    musicReportStatus.value = response.data;
  } catch (error) {
    ElMessage.error('获取音乐举报状态失败');
  }
};

// 举报成功回调
const handleMusicReportSuccess = async () => {
  await getReportStatus();
};

// 跳转到详情页
const goToDetail = () => {
  if (musicStore.currentMusic.omdMusicInfoId) {
    router.push({ name: 'music-detail', params: { omdMusicInfoId: musicStore.currentMusic.omdMusicInfoId } });
  }
};

// 监听当前音乐变化，更新音乐举报状态
watch(() => musicStore.currentMusic, (newMusic) => {
  if (newMusic) {
    musicIdList.value = [newMusic.omdMusicInfoId];
    getReportStatus();
  }
})

// 初始化音乐信息
onMounted(async () => {
  if(!authStore.isGuest){
    return;
  }
  await initMusicLikeInfo(musicIdList.value);
  await getReportStatus();
});

</script>

<template>
  <!-- 当有当前音乐时显示播放条 -->
  <div v-if="musicStore.currentMusic && musicStore.currentMusic.omdMusicInfoStatus === 1" class="player-content">
    <div class="music-player-bar">

      <div class="player-left"  @click="goToDetail" >
        <img
            v-if="musicStore.currentMusic.omdMusicInfoCoverUrl"
            :src="musicStore.currentMusic.omdMusicInfoCoverUrl"
            alt="专辑封面"
            class="cover-img"
        />
        <div class="music-info">
          <div class="music-name">{{ formatMusicName(musicStore.currentMusic.omdMusicInfoName) }}</div>
          <div class="album">{{ musicStore.currentMusic.omdMusicInfoAlbum }}</div>
        </div>
        <div class="singer">{{ musicStore.currentMusic.omdSinger?.omdSingerName || musicStore.currentMusic.omdSingerName }}</div>

      </div>

      <!-- 添加到播放列表按钮 -->
      <el-button type="text" @click="handleAddClick(musicStore.currentMusic)" v-if="!authStore.isGuest"  title="添加到播放列表">
        <el-icon class="add-to-playlist-btn"><CirclePlusFilled /></el-icon>
      </el-button>

      <!-- 收藏按钮 -->
      <div class="like-container" v-if="!authStore.isGuest">
        <el-button
            type="primary"
            plain
            @click="toggleLike(musicStore.currentMusic.omdMusicInfoId)"
            class="like-btn"
            :title="getMusicLikeInfo(musicStore.currentMusic.omdMusicInfoId).isUserLiked ? '取消收藏' : '收藏'"
        >
          <el-icon :class="{'Star': !getMusicLikeInfo(musicStore.currentMusic.omdMusicInfoId).isUserLiked,
           'StarFilled': getMusicLikeInfo(musicStore.currentMusic.omdMusicInfoId).isUserLiked}" style="font-size: 50px">
            <StarFilled />
          </el-icon>
        </el-button>
          <span class="like-count">
            {{ getMusicLikeInfo(musicStore.currentMusic.omdMusicInfoId).likeCount }}
          </span>
      </div>

      <!-- 下载按钮 -->
      <el-button
          type="text"
          @click="handleSingleDownload(musicStore.currentMusic)"
          class="download-btn"
          title="下载音乐"
          v-if="!authStore.isGuest"
          :disabled="!musicStore.currentMusic?.omdMusicInfoId"
      >
        <el-icon><Download /></el-icon>
      </el-button>

      <!-- 中间控制器（单独一层，实现居中） -->
      <div class="player-controls-center">
        <div class="player-controls">
          <el-button @click="playPrev" v-if="!authStore.isGuest">
            <el-icon><CaretLeft /></el-icon>
          </el-button>
          <el-button @click="togglePlay" :class="musicStore.isPlaying ? 'playing' : ''">
            <el-icon v-if="!musicStore.isPlaying"><VideoPlay /></el-icon>
            <el-icon v-else ><VideoPause /></el-icon>
          </el-button>
          <el-button @click="playNext" v-if="!authStore.isGuest">
            <el-icon><CaretRight /></el-icon>
          </el-button>

        </div>

        <!-- 新增的进度条区域 -->
        <div class="progress-container">
          <div class="progress-bar" @click="seekByClick" @mousedown="startDrag">
            <div class="progress-buffer" :style="{ width: musicStore.bufferPercent + '%' }"></div>
            <div class="progress-played" :style="{ width: musicStore.progressPercent + '%' }"></div>
            <div class="progress-handle" :style="{ left: musicStore.progressPercent + '%' }"></div>
          </div>
          <div class="time">
            {{ formatTime(musicStore.currentTime) }} / {{ formatTime(musicStore.currentMusic.omdMusicInfoDuration) }}
          </div>
        </div>

      </div>

      <div class="playlist-controller">

        <!-- 举报按钮 -->
        <el-button
            type="text"
            @click="openMusicReportPopup"
            icon="Warning"
            size="small"
            :title="!musicReportStatus? '已举报' : '举报'"
            class="report-btn"
            :disabled="musicReportStatus || !musicStore.currentMusic?.omdMusicInfoId"
            v-if="!authStore.isGuest && musicStore.currentMusic.omdMusicInfoId && musicStore.currentMusic.omdSingerId !== authStore.userId"
        >
          {{ musicReportStatus? '已举报' : '举报' }}
        </el-button>

        <!-- 播放模式切换按钮 -->
        <el-button @click="togglePlayMode" :title="'当前模式: ' + playModeName" v-if="!authStore.isGuest">
          <el-icon v-if="playMode === PlayMode.SEQUENTIAL">
            <Sort />
          </el-icon>
          <el-icon v-if="playMode === PlayMode.LOOP">
            <Switch />
          </el-icon>
          <el-icon v-if="playMode === PlayMode.RANDOM">
            <Rank />
          </el-icon>
          <el-icon v-if="playMode === PlayMode.SINGLE">
            <Refresh />
          </el-icon>
        </el-button>

      </div>

      <!-- 右侧区域 -->
      <div class="player-right">

        <!-- 音量控制 -->
        <el-button @click="toggleMute" size="large" class="volume-btn" :title="musicStore.isMuted ? '静音' : '取消静音'">
          <el-icon v-if="!musicStore.isMuted && musicStore.volume > 0" style="font-size: 18px"><Bell /></el-icon>
          <el-icon v-else style="font-size: 18px"><MuteNotification /></el-icon>
        </el-button>

        <el-slider
            v-model="musicStore.volume"
            :disabled="musicStore.isMuted"
            :step="1"
            :min="0"
            :max="100"
            @input="setVolume"
            class="volume-slider"
        />

        <!-- 播放列表 -->
        <div v-if="!authStore.isGuest" title="打开歌单">
          <el-button @click="togglePlaylist" size="small" class="playlist-btn">
            <el-icon><Fold /></el-icon>
          </el-button>

          <!-- 播放列表弹窗 -->
          <PlaylistModal
              :visible="playlistVisible"
              @close="togglePlaylist"
              :playlist="musicStore.playlist"
          />
        </div>
        <div v-else class="guest-tip">
          <el-tag type="warning">游客模式</el-tag>
        </div>
      </div>
    </div>
  </div>

  <!-- 添加歌曲到歌单弹窗 -->
  <PlaylistAddToDialog
      :show-dialog="showPlaylistAddToDialog"
      :playlistList="playlistList"
      :loading="loadingPlaylistList"
      :hasMore="hasMore"
      @add-to-playlist="addSongToPlaylistHandler"
      @update:show-dialog="showPlaylistAddToDialog = $event"
      @load-more="loadMorePlaylists"
  />

  <!-- 音乐举报弹窗组件 -->
  <ReportMusicDialog
      :music-id="musicStore.currentMusic.omdMusicInfoId"
      :visible="musicReportVisible"
      @update:visible="musicReportVisible = $event"
      @report-success="handleMusicReportSuccess"
  />

</template>



<style scoped>

.music-player-bar {
  display: flex;
  align-items: center;
  /* 移除原来的 justify-content，改为下面的方式让中间区域居中 */
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  padding: 0 15px;
  z-index: 999;
}

/* 左侧区域保持左对齐 */
.player-left {
  display: flex;
  align-items: center;
  overflow: hidden; /* 确保子元素放大时不超出容器 */
  transition: all 0.3s ease; /* 容器过渡效果 */
  cursor: pointer; /* 鼠标悬停时显示指针 */
  margin-right: 15px;
}

.player-left:hover {
  transform: scale(1.02); /* 容器轻微放大 */
  z-index: 10; /* 悬浮时提升层级 */
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1); /* 悬浮阴影效果 */
}
.player-content {
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.3s ease; /* 内容过渡效果 */
}

.cover-img {
  width: 62px;
  height: 62px;
  border-radius: 4px;
  margin-right: 12px;
  object-fit: cover;
  transition: transform 0.3s ease; /* 封面图过渡效果 */
}

.player-left:hover .cover-img {
  transform: scale(1.1); /* 封面图悬浮放大 */
}

.music-info {
  display: flex;
  flex-direction: column;
  min-width: 80px;
}

.music-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s ease; /* 歌曲名过渡效果 */
}

.player-left:hover .music-name {
  color: #409eff; /* 悬浮时文字变色 */
}

.singer {
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s ease;
  margin-right: 12px;
  margin-left: 15px;
}

.player-left:hover .singer {
  transform: translateX(-10px);
}

.album {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s ease; /* 专辑名过渡效果 */
}

.player-left:hover .album {
  transform: translateX(5px); /* 专辑名悬浮位移效果 */
}

/* 添加歌曲 */
.add-to-playlist-btn{
  font-size: 16px;
  color: #67c23a;
  transition: all 0.3s ease; /* 过渡动画 */
}

.add-to-playlist-btn:hover {
  transform: translateY(-3px) !important;
  text-shadow: 0 2px 4px rgba(128, 236, 87, 0.5) !important;
}

/* 点赞区域样式 */
.like-container {
  display: flex;
  align-items: center;
  margin-left: 16px;
}

/* 点赞按钮基础样式 */
.like-btn {
  border: none !important; /* 强制去除边框 */
  background-color: transparent !important; /* 透明背景 */
  padding: 0 !important; /* 移除内边距 */
  margin-right: 8px;
  transition: all 0.3s ease; /* 过渡动画 */
}

/* 点赞图标基础样式 */
.like-btn .el-icon {
  color: #ccc;
  transition: all 0.3s ease;
  font-size: 23px !important;
}

/* 点赞后图标变黄 */
.like-btn .el-icon.StarFilled {
  color: #ffd700 !important;
  font-size: 23px !important;
}

/* 鼠标悬停效果 - 未点赞时 */
.like-btn:hover .el-icon.Star {
  color: #ffd700 !important;
  transform: translateY(-3px) !important;
  text-shadow: 0 2px 4px rgba(255, 215, 0, 0.5) !important;
}

/* 点赞后悬停效果 */
.like-btn:hover .el-icon.StarFilled {
  transform: translateY(-3px) !important;
  text-shadow: 0 2px 4px rgba(255, 215, 0, 0.7) !important;
}

/* 点赞数样式 */
.like-count {
  font-size: 12px;
  color: #666;
}

/* 给下载按钮添加样式 */
.download-btn {
  margin-left: 20px;
  color: blue;
  transition: all 0.3s ease;
  font-size: 18px !important;
}

.download-btn:hover {
  color: blue;
}

/* 中间控制器区域，用 flex: 1 让它自动填充剩余空间，再让内部控制器居中 */
.player-controls-center {
  flex: 1;
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center; /* 水平居中 */
  justify-content: center; /* 垂直居中 */
  margin-bottom: 10px;
}

.player-controls {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 8px; /* 控制器与进度条之间的间距 */
}
.player-controls .el-button {
  width: 48px;
  height: 48px;
  border-radius: 50%; /* 圆形按钮 */
  background-color: #99a9bf;
  border: none;
  transition: background-color 0.3s;
}

.player-controls .el-button:hover {
  background-color: #66b1ff;
}

.player-controls .el-icon {
  font-size: 26px;
  color: white;
}

.player-controls .playing .el-icon {
  animation: spin 1.5s linear infinite;
}


.progress-container {
  transition: all 0.2s ease;
}

.progress-container:hover .progress-bar-fill {
  background-color: #d05ce3;
}

.playlist-btn {
  transition: all 0.2s ease;
}

/* 进度条容器样式 */
.progress-container {
  width: 100%; /* 进度条宽度占满父容器 */
  max-width: 500px; /* 限制最大宽度 */
  height: 4px;
  cursor: pointer;
  border-radius: 2px;
  margin-bottom: 10px;
}

.progress-container::before {
  content: '';
  width: 100%;
  height: 4px;
  border-radius: 2px;
  display: block;
}

.progress-bar {
  position: relative;
  height: 6px;
  background-color: #e5e9f2;
  border-radius: 3px;
  cursor: pointer;
}

.progress-buffer {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background-color: #d3dce6;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-played {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background-color: #409eff;
  border-radius: 3px;
  transition: width 0.1s ease;
}

.progress-handle {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 14px;
  height: 14px;
  background-color: white;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  transition: all 0.1s ease;
  opacity: 0;
  pointer-events: none;
}

.progress-bar:hover .progress-handle,
.progress-bar:active .progress-handle {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.2);
}

.time {
  margin-top: 5px;
  font-size: 12px;
  color: #ccc;
  text-align: center; /* 时间显示居中 */
}

.report-btn{
  transition: all 0.2s ease;
  color: #ef4444;
}

.report-btn:hover{
  color: #ef4444;
  transform: translateY(-3px)!important;
  text-shadow: 0 2px 4px rgba(239, 68, 68, 0.5)!important;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.playlist-controller {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 20px;
}

/* 右侧区域保持右对齐 */
.player-right {
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 180px;
}


.volume-btn {
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
  transition: all 0.2s ease;

}

.volume-btn:hover {
  color: #409eff;
}

.volume-slider {
  width: 100px;
}

.playlist-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
  transition: all 0.2s ease;
}

.playlist-btn:hover {
  color: #409eff;
}


.empty-player {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: #999;
}

.guest-tip {
  margin-left: 10px;
  color: #909399;
}
</style>