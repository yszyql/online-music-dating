<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import {ElDialog, ElButton, ElAvatar, ElIcon, ElMessage} from 'element-plus'

// 导入默认播放列表封面
import defaultCover from '@/assets/images/defaultCover.png';

// 导入状态管理
import {useAuthStore} from "@/stores/auth.js";
import {useMusicStore} from '@/stores/music.js';

// 导入服务器接口
import {getMusicListFromPlaylistService} from "@/api/music.js";

// 导入自定义组件
import PlaylistAddToDialog from "@/components/playlist/PlaylistAddToDialog.vue";

// 导入组合式函数
import {useAudioPlayer} from "@/composable/useAudioPlayer.js";
import {useMusicHandle} from "@/composable/useMusicHandle.js";
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";

// Props & Emits
const props = defineProps({
  visible: {type: Boolean, default: false}, // 接收父组件的显示状态
  playlist: {
    type: Object,
    default: () => ([])
  }
})
const emit = defineEmits(['close']) // 定义更新visible的事件

// 状态管理
const musicStore = useMusicStore();
const authStore = useAuthStore();

// 响应式数据
const hoverIndex = ref(-1) // 鼠标悬浮的索引
const visible = ref(props.visible) // 本地维护visible状态
const playlistMusic = ref([]) // 存储歌单音乐列表
const maxMusicCount = ref(0) // 最大歌曲数量
const isCurrentUser = ref(false); // 是否为当前用户的歌单

// 全选状态计算
const isAllSelected = computed(() => {
  return playlistMusic.value.length > 0 &&
      selectedSongs.value.length === playlistMusic.value.length;
});

// 使用组合式函数
const {
  playMusicFromPlaylist,
  playAll,
  shufflePlay,
} = useAudioPlayer()

const {
  initMusicLikeInfo,
  getMusicLikeInfo,
  toggleLike,
  onLikeStatusChange
} = useMusicHandle();

const {
  playlistList,
  loadingPlaylistList,
  showPlaylistAddToDialog,
  hasMore,
  openAddToPlaylistDialog,
  addSongToPlaylistHandler,
  loadMorePlaylists,
  deletePlaylist,
  batchMode,
  selectedSongs,
  toggleBatchMode,
  toggleSongSelection,
  toggleSelectAll,
  deleteMusicFromPlaylistHandler,
  handleBatchDownload,
} = usePlaylistManager();

// 监听父组件visible变化
watch(() => props.visible, (newVal) => {
  visible.value = newVal
})

// 加载播放列表里的音乐信息
const getMusicListFromPlaylist = async (playlist) => {

  if (playlist.omdPlaylistId === undefined) {
    return
  }

  try {
    const result = await getMusicListFromPlaylistService(playlist.omdPlaylistId);
    playlistMusic.value = result.data;

    // 获取歌单数量的最大值
    if (playlistMusic.value?.length > 0) {
      maxMusicCount.value = playlistMusic.value.length;
    }

    // 歌单里有音乐则开始查询点赞状态
    if (playlistMusic.value?.omdMusicInfo) {
      const musicIds = playlistMusic.value.omdMusicInfo.map(
          m => m.omdMusicInfoId
      );
      await initMusicLikeInfo(musicIds);
    }
  } catch (error) {
    console.error('获取歌单里的音乐列表失败：', error);
  }
};

// 修改：播放全部和随机播放的实现
const playAllSongs = (playlist) => {
  // 提取歌曲信息
  const currentPlaylist = playlistMusic.value.map(item => item.omdMusicInfo);
  playAll(currentPlaylist, playlist);
};

// 修改：随机播放的实现
const shufflePlayAll = (playlist) => {
  // 提取歌曲信息
  const currentPlaylist = playlistMusic.value.map(item => item.omdMusicInfo);
  shufflePlay(currentPlaylist, playlist);
};

// 处理点击音乐播放
const handleMusicClick = (omdMusicInfo, index) => {
  const currentMusic = omdMusicInfo;
  // 提取歌曲信息
  const currentPlaylist = playlistMusic.value.map(item => item.omdMusicInfo);
  playMusicFromPlaylist(currentMusic, index, props.playlist, currentPlaylist);
};

// 处理添加按钮点击
const handleAddClick = async (song) => {
  if (batchMode.value) {
    // 批量模式：使用已选中的歌曲
    if (selectedSongs.value.length === 0) {
      ElMessage.warning('请选择要添加的歌曲');
      return;
    }
    await openAddToPlaylistDialog(null, selectedSongs.value);
    await getMusicListFromPlaylist(props.playlist)
  } else {
    // 单曲模式：添加当前歌曲
    await openAddToPlaylistDialog(song, [song.omdMusicInfoId]);
    await getMusicListFromPlaylist(props.playlist)
  }
};

// 处理下载按钮点击
const handleSingleDownload = async (song) => {
  if (batchMode.value) {
    // 批量模式：下载已选中的歌曲
    if (selectedSongs.value.length === 0) {
      ElMessage.warning('请选择要下载的歌曲');
      return;
    }
    await handleBatchDownload(selectedSongs.value,playlistMusic.value);
  } else {
    // 单曲模式：下载当前歌曲
    await handleBatchDownload(song);
  }
};

// 处理删除按钮点击
const handleDeleteClick = async (omdPlaylistName, song) => {
  if (batchMode.value) {
    // 批量模式：删除已选中的歌曲
    if (selectedSongs.value.length === 0) {
      ElMessage.warning('请选择要添加的歌曲');
      return;
    }
    await deleteMusicFromPlaylistHandler(omdPlaylistName, selectedSongs.value);
    await getMusicListFromPlaylist(props.playlist)
  } else {
    // 单曲模式：删除当前歌曲
    await deleteMusicFromPlaylistHandler(omdPlaylistName, [song.omdMusicInfoId]);
    await getMusicListFromPlaylist(props.playlist)
  }
};

// 关闭弹窗
const handleClose = () => {
  // 重置选择状态
  selectedSongs.value = [];
  batchMode.value = false;
  // 重置歌曲列表

  emit('close') // 触发关闭事件
}

// 监听歌单变化
watch(() => props.playlist, async (newVal, oldVal) => {
  if (newVal.omdPlaylistId !== oldVal.omdPlaylistId) {
    await getMusicListFromPlaylist(newVal)
  }
});

// 监听playlistMusic变化
watch(() => playlistMusic.value, (newValue) => {
  if (newValue) {
    playlistMusic.value = newValue;
  }
}, {immediate: true});

// 初始化
onMounted(async () => {
  if(authStore.isGuest){
    return;
  }

  // 检查是否为当前用户的歌单
  isCurrentUser.value = authStore.userInfo.omdUserId === props.playlist.omdUserId;

  // 加载歌单音乐列表
  await getMusicListFromPlaylist(props.playlist)

  // 监听点赞状态变化（尤其是取消点赞）
  onLikeStatusChange.value = async (musicId, isLiked) => {
    // 仅在“我喜欢的音乐”歌单中，且是取消点赞操作时刷新
    if (!isLiked && props.playlist.omdPlaylistName === '我喜欢的音乐') {
      await getMusicListFromPlaylist(props.playlist); // 重新加载歌单歌曲
    }
  };
})

// 当组件卸载时，清除回调（避免内存泄漏）
onUnmounted(() => {
  onLikeStatusChange.value = null;
});

</script>

<template>
  <el-dialog
      v-model="visible"
      title=""
      width="600px"
      :before-close="handleClose"
      class="playlist-modal"
  >
    <div class="modal-header">
      <!-- 左侧：封面 + 歌单信息 -->
      <div class="flex items-center">
        <el-avatar :src="playlist.omdPlaylistCover || defaultCover" size="80" class="mr-4"/>
        <div>
          <h2 class="text-xl">{{ playlist.omdPlaylistName || '播放列表' }}</h2>
          <p class="text-sm">
            {{ maxMusicCount }} 首歌曲 • {{ playlist.omdPlaylistCreateTime }} 创建
          </p>
          <p class="text-sm" v-if="playlist.omdPlaylistDescription !== null && playlist.omdPlaylistDescription !== ''">
            歌曲简介 • {{ playlist.omdPlaylistDescription }}
          </p>
        </div>
      </div>

      <!-- 右侧：按钮区（动态布局） -->
      <div
          class="song-btn-wrap"
          :class="{ 'batch-mode': batchMode }"
      >
        <!-- 批量选择按钮 -->
        <el-button
            :type="batchMode ? 'success' : 'default'"
            size="small"
            class="batch-toggle-btn"
            @click="toggleBatchMode"
        >
          <el-icon><Select/></el-icon>
          {{ batchMode ? '取消批量&nbsp;' : '批量选择' }}
        </el-button>

        <!-- 添加/删除按钮（仅批量模式显示） -->
        <el-button
            type="primary"
            size="small"
            class="batch-toggle-btn"
            @click="handleAddClick(selectedSongs)"
            :disabled="batchMode && selectedSongs.length === 0"
            v-if="batchMode"
        >
          <el-icon>
            <Plus/>
          </el-icon>
          {{ `添加(${selectedSongs.length})首` }}
        </el-button>

        <!-- 批量下载按钮（仅批量模式显示） -->
        <el-button
            type="primary"
            size="small"
            class="batch-toggle-btn"
            color="warning"
            @click="handleSingleDownload"
            :disabled="batchMode && selectedSongs.length === 0"
            v-if="batchMode"
        >
          <el-icon><Download /></el-icon>
          {{ `下载(${selectedSongs.length})首` }}
        </el-button>

        <!-- 批量删除按钮（仅批量模式显示） -->
        <el-button
            type="danger"
            size="small"
            class="batch-toggle-btn"
            @click="handleDeleteClick(playlist.omdPlaylistName,selectedSongs)"
            :disabled="batchMode && selectedSongs.length === 0"
            v-if="batchMode && isCurrentUser"
        >
          <el-icon>
            <Delete/>
          </el-icon>
          {{ `删除(${selectedSongs.length})首` }}
        </el-button>
      </div>
    </div>

    <!-- 歌曲列表 -->
    <div class="modal-body">

      <!-- 批量选择状态提示 -->
      <div v-if="batchMode" class="batch-mode-indicator">
        <el-checkbox :model-value="isAllSelected" @change="toggleSelectAll(playlistMusic)">
          全选
        </el-checkbox>
        <span>已选择 {{ selectedSongs.length }} 首歌曲</span>
      </div>

      <div v-if="playlistMusic.value !== null" class="song-list">
        <div
            v-for="(item, index) in playlistMusic"
            :key="item.omdMusicInfo.omdMusicInfoId"
            class="song-item"
            :class="{ 'name-active': hoverIndex === index }"
            @mouseenter="hoverIndex = index"
            @mouseleave="hoverIndex = -1"
        >

          <!-- 新增：批量选择复选框 -->
          <div class="batch-checkbox" v-if="batchMode">
            <el-checkbox
                :model-value="selectedSongs.includes(item.omdMusicInfo.omdMusicInfoId)"
                @change="() => toggleSongSelection(item.omdMusicInfo.omdMusicInfoId)"
            />
          </div>

          <!-- 序号 -->
          <div class="order-index">{{ index + 1 }}</div>

          <!-- 歌曲基本信息 -->
          <div class="song" @click="batchMode ?
                                      toggleSongSelection(item.omdMusicInfo.omdMusicInfoId) :
                                      handleMusicClick(item.omdMusicInfo,index + 1)">
            <!-- 歌曲封面 -->
            <el-avatar :src="item.omdMusicInfo.omdMusicInfoCoverUrl" class="song-cover"/>

            <!-- 歌曲信息 -->
            <div class="flex-1 music-info-wrapper">
              <!-- 歌名 + 专辑名（上下排列） -->
              <div class="music-info">
                <div class="song-name-wrapper">
                  <span
                      class="song-name"
                      :class="{ 'name-active': hoverIndex === index }"
                      @mouseenter="hoverIndex = index"
                      @mouseleave="hoverIndex = -1"
                  >
                    {{ item.omdMusicInfo.omdMusicInfoName }}
                  </span>
                </div>
                <div class="album-wrapper">
                  <span class="album-name">{{ item.omdMusicInfo.omdMusicInfoAlbum }}</span>
                </div>
              </div>
              <!-- 歌手名（右侧并排） -->
              <div class="artist-wrapper">
                <span class="artist-name">
                  {{ item.omdMusicInfo.omdSinger.omdSingerName }}
                </span>
              </div>
            </div>
          </div>

          <!-- 右侧按钮组 -->
          <div class="song-actions">

            <!-- 耳机图标 -->
            <el-icon
                v-if="musicStore.currentMusic?.omdMusicInfoId === item.omdMusicInfo.omdMusicInfoId"
                class="headset-icon"
            >
              <Headset/>
            </el-icon>

            <el-button type="text" @click="handleAddClick(item.omdMusicInfo)" v-if="!batchMode" title="添加到播放列表">
              <el-icon class="add-to-playlist-btn">
                <CirclePlusFilled/>
              </el-icon>
            </el-button>
            <!-- 收藏按钮 -->
            <div class="like-container">
              <el-button
                  type="primary"
                  plain
                  @click="toggleLike(item.omdMusicInfo.omdMusicInfoId)"
                  class="like-btn"
                  v-if="!batchMode"
                  :title="getMusicLikeInfo(musicStore.currentMusic.omdMusicInfoId).isUserLiked ? '取消收藏' : '收藏'"
              >
                <el-icon :class="{'Star': !getMusicLikeInfo(item.omdMusicInfo.omdMusicInfoId).isUserLiked,
                    'StarFilled': getMusicLikeInfo(item.omdMusicInfo.omdMusicInfoId).isUserLiked}"
                         style="font-size: 50px">
                  <StarFilled/>
                </el-icon>
              </el-button>
              <span class="like-count" v-if="!batchMode">
                {{ getMusicLikeInfo(item.omdMusicInfo.omdMusicInfoId).likeCount }}
              </span>
            </div>
            <!-- 下载按钮 -->
            <el-button type="text" @click="handleSingleDownload(item.omdMusicInfo)" v-if="!batchMode" title="下载音乐">
              <el-icon class="download-btn">
                <Download/>
              </el-icon>
            </el-button>
            <!-- 删除按钮 -->
            <el-button type="text" @click="handleDeleteClick(playlist.omdPlaylistName,item.omdMusicInfo)"
                       v-if="!batchMode && isCurrentUser">
              <el-icon class="delete-btn">
                <Delete/>
              </el-icon>
            </el-button>
          </div>

        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <p>播放列表为空</p>
        <p v-if="isCurrentUser">添加一些歌曲，享受音乐吧～</p>
        <el-button icon="plus" type="primary" size="small" class="mt-4 add-song-btn" v-if="isCurrentUser">
          添加歌曲
        </el-button>
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="footer-actions">
        <!-- 新增删除歌单危险按钮 -->
        <el-button
            type="danger"
            size="small"
            class="delete-playlist-btn"
            @click="deletePlaylist(playlist.omdPlaylistName)"
            v-if="playlist.omdPlaylistName !== '我喜欢的音乐' || isCurrentUser">
          <el-icon>
            <Delete/>
          </el-icon>
          删除歌单
        </el-button>
        <div class="play-button">
          <el-button type="primary" @click="shufflePlayAll(playlist)" class="shuffle-btn">
            <el-icon>
              <RefreshRight/>
            </el-icon>
            随机播放
          </el-button>
          <el-button type="success" @click="playAllSongs(playlist)" class="play-all-btn">
            <el-icon>
              <Star/>
            </el-icon>
            播放全部
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>

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

</template>

<style scoped>

/* 弹窗基础样式 */
.playlist-modal {
  border-radius: 12px;
  max-width: 400px !important;
  max-height: 500px !important;
  overflow: hidden;
}

.el-dialog__body {
  max-height: calc(500px - 120px); /* 减去头部和底部高度 */
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0;
}

/* 头部区域样式调整 */
.modal-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between; /* 让左右区域分离 */
  align-items: flex-start;
  position: relative;
}

.items-center {
  align-items: center;
  display: flex;
}

.text-xl {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.text-sm {
  font-size: 0.875rem;
  color: #6c757d;
  margin-bottom: 12px;
}

.mr-4 {
  margin-right: 1rem;
  height: 80px;
  width: 80px;
}

.add-song-btn {
  margin: 0;
  padding: 6px 12px;
  font-size: 14px;
}

/* 歌曲列表样式 */
.song-list {
  min-height: calc(500px - 200px); /* 调整高度留出底部空间 */
  overflow-y: auto;
  overflow-x: hidden;
}

.song-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  transition: all 0.3s ease;
  position: relative;
}

/* 鼠标悬停放大效果 */
.song-item:hover {
  transform: scale(1.02);
  background-color: #f8f9fa;
}

/* 新增样式 */
.batch-mode-indicator {
  padding: 8px 12px;
  background-color: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.batch-checkbox {
  margin-right: 10px;
  display: flex;
  align-items: center;
  margin-top: 6px;
}

/* 按钮区默认样式（非批量模式）：水平排列 */
.song-btn-wrap {
  display: flex;
  align-items: center; /* 与左侧信息垂直居中 */
  justify-content: flex-end;
  margin-top: 50px;
  gap: 10px; /* 按钮间距 */
}

/* 批量模式下：按钮区垂直排列 */
.song-btn-wrap.batch-mode {
  flex-direction: column;
  align-items: flex-end; /* 按钮靠右垂直堆叠 */
  margin-top: 10px; /* 与左侧信息拉开距离 */
}

/* 统一按钮样式，清除默认 margin */
.batch-toggle-btn {
  margin: 0;
}

.batch-toggle-btn,
.add-song-btn,
.del-song-btn {
  margin: 0; /* 清除默认 margin */
}

.song-btn-wrap {
  gap: 10px; /* 用 gap 控制按钮间距 */
  align-items: center; /* 确保按钮垂直居中 */
}

/* 右侧侧按钮组 调整为左右排列且变大 */
.song-actions {
  position: absolute;
  right: 12px;
  display: flex;
  gap: 8px;
  top: 50%;
  transform: translateY(-50%);
}

/* 序号样式 调整为居中 */
.order-index {
  width: 30px;
  text-align: center;
  color: #6c757d;
  font-size: 14px;
  margin-top: 12px;;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.song {
  display: flex;
}

/* 歌曲封面样式 */
.song-cover {
  width: 50px;
  height: 50px;
  margin-right: 12px;
}

/* 歌曲信息样式 调整歌手名字位置到右边 */
.music-info-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
}

/* 歌名 + 专辑名（上下排列） */
.music-info {
  display: flex;
  flex-direction: column;
  gap: 2px; /* 上下间距 */
  margin-right: 20px;
}

.song-name-wrapper {
  margin-bottom: 0;
}

.song-name {
  color: #333;
  font-size: 14px;
  font-weight: 800;
  transition: color 0.3s ease;
}

.name-active {
  color: #0062ef;
  border-bottom: 2px solid #409eff;
}

.album-wrapper {
  margin-bottom: 0;
}

.album-name {
  color: #9ca3af;
  font-size: 12px;
}

/* 歌手名（右侧并排） */
.artist-wrapper {
  display: flex;
  align-items: center;
}

.artist-name {
  font-size: 12px;
  font-weight: 600;
}


/* 耳机图标样式 */
.headset-icon {
  margin-top: 7px;
  margin-right: 5px;
  font-size: 20px;
  color: #409eff;
}

.add-to-playlist-btn {
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
  margin-left: 6px;
}

/* 点赞按钮基础样式 */
.like-btn {
  border: none !important; /* 强制去除边框 */
  background-color: transparent !important; /* 透明背景 */
  padding: 0 !important; /* 移除内边距 */
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

/* 下载按钮样式 */
.download-btn {
  font-size: 16px;
  margin-left: 10px;
  color: blue;
  transition: all 0.3s ease; /* 过渡动画 */
}

.download-btn:hover {
  transform: translateY(-3px)!important;
  text-shadow: 0 2px 4px rgba(0, 0, 255, 0.5)!important;
}

.delete-btn {
  font-size: 16px;
  margin-left: 10px;
  color: #ff4d4f;
  transition: all 0.3s ease; /* 过渡动画 */
}

.delete-btn:hover {
  transform: translateY(-3px) !important;
  text-shadow: 0 2px 4px rgba(255, 77, 79, 0.5) !important;
}


/* 空状态样式 */
.empty-state {
  padding: 40px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-state .el-icon {
  font-size: 48px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-state p {
  margin-bottom: 8px;
  color: #6c757d;
}

/* 底部操作样式 调整为按钮并排右侧，新增删除按钮 */
.footer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.delete-playlist-btn {
  padding: 6px 12px;
  font-size: 12px;
}

.play-button {
  display: flex;
  gap: 8px;
}

.shuffle-btn,
.play-all-btn {
  padding: 6px 12px;
  font-size: 12px;
}
</style>