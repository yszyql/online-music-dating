<script setup>
import { onMounted, ref} from "vue";
import {ElButton, ElIcon} from "element-plus";

// 导入状态管理
import { useMusicStore } from '@/stores/music.js';
import { useAuthStore } from '@/stores/auth.js'

// 引入组合式函数
import { useMusicHandle } from "@/composable/useMusicHandle.js";
import {useAudioPlayer} from "@/composable/useAudioPlayer.js";

// 定义属性和事件
const props = defineProps({
  item: {
    type: Object,
    required: true
  }
});

const emit = defineEmits([
  'add-song',
])

// 状态管理
const authStore = useAuthStore();
const musicStore = useMusicStore();

// 使用组合式函数
const{
  setupAudio
} = useAudioPlayer()

const {
  initMusicLikeInfo,
  getMusicLikeInfo,
  toggleLike,
} = useMusicHandle();


// 响应式数据
const musicIdList = ref([props.item.omdMusicInfoId]);

// 处理添加按钮点击
const handleAddClick = (song) => {
  emit("add-song", song);
};

// 钩子函数
onMounted(() => {
  initMusicLikeInfo(musicIdList.value);
})

</script>

<template>
  <li v-if="item.omdMusicInfoStatus === 1">
    <!-- 封面 + 音乐信息容器 -->
    <div class="cover-and-info" @click="setupAudio(item)">
      <img
          :src="item.omdMusicInfoCoverUrl"
          :alt="item.omdMusicInfoName"
          class="music-cover"
      >
      <div class="music-info">
        <p class="music-title">{{ item.omdMusicInfoName }}</p>
        <p class="singer-name">{{ item.omdSingerName }}</p>
        <p class="play-count" v-if="item.playCount > 0">播放量：{{ item.playCount }}</p>
      </div>
    </div>

    <!-- 耳机图标 -->
    <el-icon
        v-if="musicStore.currentMusic?.omdMusicInfoId === item.omdMusicInfoId"
        class="headset-icon"
    >
      <Headset />
    </el-icon>

    <!-- 添加到播放列表按钮 -->
    <el-button type="text" @click="handleAddClick(item)" v-if="!authStore.isGuest" title="添加到播放列表">
      <el-icon class="add-to-playlist-btn"><CirclePlusFilled /></el-icon>
    </el-button>

    <!-- 点赞区域 -->
    <div class="like-container" v-if="!authStore.isGuest">
      <el-button
          :type="getMusicLikeInfo(item.omdMusicInfoId).isUserLiked ? 'danger' : ''"
          size="large"
          @click="toggleLike(item.omdMusicInfoId)"
          :title="getMusicLikeInfo(musicStore.currentMusic.omdMusicInfoId).isUserLiked ? '取消收藏' : '收藏'"
          class="like-btn">
        <el-icon :class="{'Star': !getMusicLikeInfo(item.omdMusicInfoId).isUserLiked,
            'StarFilled': getMusicLikeInfo(item.omdMusicInfoId).isUserLiked}" style="font-size: 20px">
          <StarFilled />
        </el-icon>
      </el-button>
      <span class="like-count">{{ getMusicLikeInfo(item.omdMusicInfoId).likeCount }}</span>
    </div>
  </li>

</template>

<style scoped>
/* 列表项基础样式 */
li {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  cursor: pointer;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
}

/* 封面 + 音乐信息 容器 */
.cover-and-info {
  display: flex;
  align-items: center;
  flex: 1; /* 让音乐信息占满剩余空间 */
}

/* 封面样式 */
.music-cover {
  width: 48px;
  height: 48px;
  margin-right: 12px;
  border-radius: 6px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

/* 音乐信息容器：紧邻封面右侧 */
.music-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 歌名样式 */
.music-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
  transition: color 0.3s ease;
}

/* 歌手名样式 */
.singer-name {
  margin: 2px 0;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 播放量样式 */
.play-count {
  margin: 2px 0;
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 耳机样式 */
.headset-icon {
  font-size: 20px;
  color: #409eff;
  margin-right: 20px;
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

/* 按钮悬停时的整体效果 */
.like-btn:hover {
  transform: translateY(-1px); /* 按钮整体轻微上浮 */
}

/* 点赞图标基础样式 */
.like-btn .el-icon {
  font-size: 20px;
  color: #ccc; /* 未点赞时灰色 */
  transition: all 0.3s ease; /* 过渡动画 */

}

/* 点赞后图标变黄 */
.like-btn .el-icon.StarFilled {
  color: #ffd700; /* 点赞后黄色 */
  font-size: 23px !important;
}

/* 点赞后悬停效果（可选） */
.like-btn:hover .el-icon.StarFilled {
  transform: translateY(-2px); /* 点赞后悬停也上浮 */
  text-shadow: 0 2px 4px rgba(255, 215, 0, 0.7); /* 增强阴影 */
}


/* 鼠标悬停效果 - 未点赞时 */
.like-btn:hover .el-icon.Star {
  color: #ffd700; /* 悬停变黄 */
  transform: translateY(-2px); /* 上浮2px */
  text-shadow: 0 2px 4px rgba(255, 215, 0, 0.5); /* 阴影效果 */
  font-size: 23px !important;
}

/* 点赞数样式 */
.like-count {
  font-size: 12px;
  color: #666;
}

/* 耳机图标样式 */
.headset-icon {
  margin-left: 12px;
  font-size: 20px;
  color: #409eff;
}

/* 鼠标悬停效果 */
li:hover {
  background-color: #f0f5ff;
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px) scale(1.02);
}

li:hover .music-cover {
  transform: scale(1.05);
}

li:hover .music-title {
  color: #3b82f6;
}
</style>