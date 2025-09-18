<script setup>
import {ref, computed, onMounted, onUnmounted, watch} from 'vue';
import {ElButton, ElIcon, ElMessage} from 'element-plus';
import axios from 'axios';
import router from "@/router/index.js";

// 导入状态管理
import {useAuthStore} from '@/stores/auth.js'
import {useMusicStore} from '@/stores/music.js';

// 导入服务端接口
import {getMusicInfoLyricByMusicIdService} from '@/api/public.js';

// 导入自定义组合函数
import {useAudioPlayer} from '@/composable/useAudioPlayer.js';
import {useMusicHandle} from "@/composable/useMusicHandle.js";
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";

// 导入自定义组件
import CommentItem from "@/components/message/CommentItem.vue";
import PlaylistAddToDialog from "@/components/playlist/PlaylistAddToDialog.vue";

// 状态管理
const musicStore = useMusicStore();
const authStore = useAuthStore()

// 响应式数据
const lyricWrapper = ref(null);
const lyricContent = ref(null);
const lyricLines = ref([]); // 解析后的歌词行
const currentLineIndex = ref(-1);
const isLoadingLyric = ref(false);
const musicIdList = ref([musicStore.currentMusic.omdMusicInfoId]);


// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 播放器相关组合函数
const {
  audio,
  progressPercent,
  bufferPercent,
  isMuted,
  togglePlay,
  playMode,
} = useAudioPlayer();

// 音乐处理组合函数
const {
  initMusicLikeInfo,
  getMusicLikeInfo,
} = useMusicHandle();

const {
  playlistList,
  loadingPlaylistList,
  showPlaylistAddToDialog,
  hasMore,
  addSongToPlaylistHandler,
  loadMorePlaylists,
} = usePlaylistManager();

// 核心功能方法
// 获取歌词URL
const getLyricUrl = async (omdMusicInfoId) => {
  if (!omdMusicInfoId) return null;

  const cacheKey = `lyric_url_${omdMusicInfoId}`;
  const cacheTimestampKey = `${cacheKey}_timestamp`;
  const cacheDuration = 24 * 60 * 60 * 1000; // 缓存有效期：24小时

  // 检查缓存
  const cachedUrl = localStorage.getItem(cacheKey);
  const cachedTime = localStorage.getItem(cacheTimestampKey);
  const isCacheValid = cachedUrl && cachedTime &&
      (Date.now() - parseInt(cachedTime) < cacheDuration);

  if (isCacheValid) {
    console.log('使用缓存歌词URL:', cachedUrl);
    return cachedUrl;
  }

  try {
    // 调用服务端接口获取歌词URL
    const result = await getMusicInfoLyricByMusicIdService(omdMusicInfoId);

    // 处理"暂无歌词"的情况
    if (result.data.includes("暂无歌词")) {
      ElMessage.warning('该音乐暂无歌词');
      console.log('该音乐暂无歌词');
      return null;
    }
    const lyricUrl = result.data || null;

    if (lyricUrl) {
      localStorage.setItem(cacheKey, lyricUrl);
    }

    return lyricUrl;
  } catch (error) {
    console.error('获取歌词URL失败:', error);
    return null;
  }
};

/** 加载当前音乐的歌词（改进版） */
const loadCurrentMusicLyric = async () => {
  try {
    const omdMusicInfoId = musicStore.currentMusic?.omdMusicInfoId;
    if (!omdMusicInfoId) {
      console.log('无音乐ID，跳过加载歌词');
      return;
    }

    isLoadingLyric.value = true;

    // 清除可能的旧缓存
    const oldLyricUrl = localStorage.getItem(`lyric_url_${omdMusicInfoId}`);
    if (oldLyricUrl) {
      const oldCacheKey = `lyric_${oldLyricUrl}`;
      localStorage.removeItem(oldCacheKey);
      localStorage.removeItem(`${oldCacheKey}_timestamp`);
    }

    const lyricUrl = await getLyricUrl(omdMusicInfoId);

    console.log('获取到的歌词URL:', lyricUrl);

    if (!lyricUrl) {
      lyricLines.value = [];
      ElMessage.info('暂无歌词URL');
      return;
    }

    await loadLyric(lyricUrl);

    // 检查解析结果
    if (lyricLines.value.length === 0) {
      console.warn('歌词解析结果为空，可能格式不匹配');
      ElMessage.warning('歌词格式不支持');
    }
  } catch (error) {
    console.error('加载歌词失败:', error);
    lyricLines.value = [];
    ElMessage.error('加载歌词失败: ' + error.message);
  } finally {
    isLoadingLyric.value = false;
  }
};

/** 解析歌词（改进版） */
const parseLyric = (lyricText) => {
  console.log('开始解析歌词:', lyricText.substring(0, 100) + '...');

  if (!lyricText) {
    console.log('歌词文本为空');
    lyricLines.value = [];
    return;
  }

  const lines = lyricText.split('\n');
  const result = [];

  // 增强的时间标签正则表达式，支持多种格式
  const timeReg = /\[(\d{1,2}):(\d{1,2})(?:[.:](\d{1,3}))?\](.*)/;

  lines.forEach(line => {
    const match = line.match(timeReg);
    if (!match) {
      // 记录无法解析的行（可能是歌词元数据或特殊格式）
      console.debug('无法解析的歌词行:', line);
      return;
    }

    const minute = parseInt(match[1], 10);
    const second = parseInt(match[2], 10);
    const millisecondStr = match[3] || '0';

    // 处理不同精度的毫秒值（2位或3位）
    let millisecond;
    if (millisecondStr.length === 3) {
      millisecond = parseInt(millisecondStr, 10);
    } else if (millisecondStr.length === 2) {
      millisecond = parseInt(millisecondStr, 10) * 10; // 2位转为3位精度
    } else {
      millisecond = 0;
    }

    const lyric = match[4].trim();

    if (lyric) {
      // 计算时间戳（秒）
      const time = minute * 60 + second + (millisecond / 1000);

      result.push({
        time,
        lyric,
        duration: 0 // 后续计算持续时间
      });
    }
  });

  // 排序歌词
  result.sort((a, b) => a.time - b.time);

  // 计算每句歌词的持续时间
  for (let i = 0; i < result.length; i++) {
    if (i < result.length - 1) {
      result[i].duration = result[i + 1].time - result[i].time;
    } else {
      // 最后一句歌词的持续时间
      result[i].duration = musicStore.currentMusic.omdMusicInfoDuration
          ? musicStore.currentMusic.omdMusicInfoDuration - result[i].time
          : 5; // 默认5秒
    }
  }

  console.log('解析后的歌词行数:', result.length);
  lyricLines.value = result;
};

/** 加载歌词（改进版） */
const loadLyric = async (lyricUrl) => {
  if (!lyricUrl) return;

  const cacheKey = `lyric_${lyricUrl}`;
  const timestampKey = `${cacheKey}_timestamp`;
  const CACHE_DURATION = 3 * 24 * 60 * 60 * 1000; // 3天缓存期

  try {
    // 检查缓存
    const cachedLyric = localStorage.getItem(cacheKey);
    const cachedTimestamp = localStorage.getItem(timestampKey);
    const isCacheValid = cachedLyric && cachedTimestamp &&
        (Date.now() - parseInt(cachedTimestamp) < CACHE_DURATION);

    if (isCacheValid) {
      console.log('使用缓存歌词内容');
      parseLyric(cachedLyric);
      return;
    }

    // 构建代理请求URL
    const cosPath = lyricUrl.replace('https://online-music-dating-1362513882.cos.ap-guangzhou.myqcloud.com', '');
    const proxyUrl = `/proxy/lyric${cosPath}`;

    console.log('请求歌词URL:', proxyUrl);

    const response = await axios.get(proxyUrl);

    // 检查响应状态和内容
    if (response.status !== 200 || !response.data) {
      throw new Error(`获取歌词失败，状态码: ${response.status}`);
    }

    const lyricText = response.data;

    console.log('获取到的歌词内容长度:', lyricText.length);

    // 更新缓存
    localStorage.setItem(cacheKey, lyricText);
    localStorage.setItem(timestampKey, Date.now().toString());

    parseLyric(lyricText);
  } catch (error) {
    console.error('加载歌词失败:', error);
    throw error; // 向上抛出错误以便统一处理
  }
};

// 监听歌词
// 更新歌词位置与高亮
const updateLyricPosition = (currentTime) => {
  if (!lyricWrapper.value || lyricLines.value.length === 0) return;

  // 找当前行索引（同之前逻辑）
  let targetIndex = -1;
  for (let i = 0; i < lyricLines.value.length; i++) {
    const currentLine = lyricLines.value[i];
    const nextLine = lyricLines.value[i + 1];
    if (currentLine.time <= currentTime && (!nextLine || nextLine.time > currentTime)) {
      targetIndex = i;
      break;
    }
  }
  currentLineIndex.value = targetIndex;

  if (targetIndex === -1) return;

  const LINE_HEIGHT = 30; // 固定行高，和样式保持一致
  const containerHeight = lyricWrapper.value.clientHeight;

  // 目标滚动距离：让当前行居中
  const targetScrollTop = targetIndex * LINE_HEIGHT - containerHeight / 2 + LINE_HEIGHT / 2;
  // 边界处理：不能滚出歌词范围
  const maxScrollTop = (lyricLines.value.length - 1) * LINE_HEIGHT - containerHeight;
  const safeScrollTop = Math.max(0, Math.min(targetScrollTop, maxScrollTop));

  // 平滑滚动
  lyricWrapper.value.scrollTo({
    top: safeScrollTop,
    behavior: 'smooth'
  });
};

// 监听播放进度
watch(() => musicStore.currentTime, (currentTime) => {
  updateLyricPosition(currentTime);
});

/** 清理过期的歌词缓存 */
const cleanupLyricCache = () => {
  const now = Date.now();
  const oneDay = 24 * 60 * 60 * 1000; // 1天的毫秒数

  // 获取所有歌词缓存键
  const lyricKeys = Object.keys(localStorage)
      .filter(key => key.startsWith('lyric_') && !key.endsWith('_timestamp'));

  // 检查并删除过期缓存
  lyricKeys.forEach(key => {
    const timestampKey = `${key}_timestamp`;
    const timestamp = localStorage.getItem(timestampKey);

    if (timestamp && now - parseInt(timestamp) > oneDay) {
      localStorage.removeItem(key);
      localStorage.removeItem(timestampKey);
    }
  });
};

// 生命周期钩子
onMounted(() => {

  initMusicLikeInfo(musicIdList.value)

  // 清理缓存
  cleanupLyricCache();

  // 初始化时加载当前音乐的歌词
  loadCurrentMusicLyric();

  // 监听当前音乐变化，自动加载新歌词
  watch(musicStore.currentMusic, (newMusic) => {
    if (newMusic && newMusic.omdMusicInfoId) {
      loadCurrentMusicLyric();
    } else {
      // 没有当前音乐时清空歌词
      lyricLines.value = [];
    }
  }, {immediate: true});


  // 监听播放进度，更新歌词位置
  audio.value?.addEventListener('timeupdate', () => {
    updateLyricPosition(audio.value.currentTime);
  });
});

onUnmounted(() => {
  // 清理事件监听
  audio.value?.removeEventListener('timeupdate', updateLyricPosition); // 清理时间更新事件
  if (audio.value) {
    musicStore.updateCurrentTime(audio.value.currentTime);
  }

  // 游客模式下清理UUID
  if (authStore.isGuest) {
    localStorage.removeItem('guest_uuid');
  }
});

</script>

<template>
  <div class="music-player-page">

    <div class="back-button" @click="goBack">
      <div class="button-container">
        <el-icon class="button-icon">
          <ArrowLeft/>
        </el-icon>
        <span class="button-text">返回</span>
      </div>
    </div>

    <div class="player-container">
      <!-- 左侧专辑封面 -->
      <div class="album-cover">
        <img
            v-if="musicStore.currentMusic.omdMusicInfoCoverUrl"
            :src="musicStore.currentMusic.omdMusicInfoCoverUrl"
            alt="专辑封面"
            class="cover-image"
            @click="togglePlay"
        />
        <div v-else class="default-cover">
          <el-icon>
            <Music/>
          </el-icon>
        </div>
      </div>

      <!-- 右侧歌词区域 -->
      <div class="lyric-container">
        <div class="lyric-header">
          <h1 class="song-title">{{ musicStore.currentMusic.omdMusicInfoName }}</h1>
          <p class="song-artist">{{ musicStore.currentMusic.omdSingerName }}</p>
          <p class="song-album">{{ musicStore.currentMusic.omdMusicInfoAlbum || '未知专辑' }}</p>
        </div>

        <div class="lyric-wrapper" ref="lyricWrapper">
          <div class="lyric-content" ref="lyricContent">
            <div
                v-for="(line, index) in lyricLines"
                :key="index"
                class="lyric-line"
                :class="{ 'active': index === currentLineIndex }"
            >
              {{ line.lyric }}
            </div>
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

    <!-- 评论区域 -->
    <CommentItem/>

  </div>

</template>

<style scoped>
/* 页面整体布局 */
.music-player-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f7fa;
}

/* 顶部返回按钮 */
.back-button {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.button-container {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.button-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.button-container:active {
  transform: translateY(1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.button-icon {
  margin-right: 8px;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.button-container:hover .button-icon {
  transform: translateX(-2px);
}

.button-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.player-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  height: 500px;
}

.album-cover {
  width: 30%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30px;
  background-color: #f8f9fa;
}

.cover-image {
  width: 300px;
  height: 300px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s;
}

.cover-image:hover {
  transform: scale(1.05);
}

.default-cover {
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #e9ecef;
  border-radius: 8px;
  font-size: 48px;
  color: #909399;
}

.lyric-container {
  width: 70%;
  padding: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.lyric-header {
  text-align: center;
  margin-bottom: 20px;
}

.song-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.song-artist, .song-album {
  font-size: 16px;
  color: #666;
  margin: 5px 0;
}

.lyric-wrapper {
  flex: 1;
  overflow: hidden;
  overflow-y: auto;
  position: relative;
}

.lyric-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  transition: transform 0.3s ease;
}

.lyric-line {
  line-height: 30px;
  text-align: center;
  transition: all 0.3s;
}

.lyric-content div {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-size: 16px;
}

.active {
  color: #4ecdc4; /* 高亮颜色 */
  font-size: 1.1em;
  font-weight: bold;
}


</style>