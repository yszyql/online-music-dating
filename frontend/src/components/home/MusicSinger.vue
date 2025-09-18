<script setup>
// 导入vue的函数
import {ref, onMounted} from 'vue';
import {useRouter} from "vue-router";

// 导入状态管理
import { useMusicStore } from '@/stores/music.js';

// 导入服务器请求
import {getMusicInfoBySingerIdService, getRandomSingersInfoService} from "@/api/public.js";
import {useAudioPlayer} from "@/composable/useAudioPlayer.js";
import {ElMessage} from "element-plus";

// 状态管理
const musicStore = useMusicStore();

// 初始化路由实例
const router = useRouter();

// 定义响应式数据存储歌手
const singersData = ref([]);

const{
  setupAudio
} = useAudioPlayer()

// 跳转到用户基本信息页面
const goToUserInfo = async (omdUserId) => {
  if (authStore.isGuest) {
    ElMessage.warning('游客用户无法查看用户信息');
    return;
  }

  if (!omdUserId) {
    console.error('用户ID不存在，无法跳转');
    return;
  }

  router.push({
    path: `/introduction/singerDetail/${omdUserId}`
  });

};

onMounted(async () => {
  try {
    // 请求数据并赋值给响应式变量
    const { data } = await getRandomSingersInfoService();
    // 赋值给响应式变量
    singersData.value = data.singers || [];
  }catch (error) {
    console.error('获取数据失败:', error);
  }
});

// 播放歌曲的方法
const playSong = async ( omdSingerId , omdMusicInfoName,omdSingerName ) => {
  // 设置当前播放歌曲
  try {
    const response = await getMusicInfoBySingerIdService(omdSingerId, omdMusicInfoName);
    if (response.data) {
      const songData = {
        id: response.data.omdMusicInfoId,
        omdMusicInfoName: omdMusicInfoName,
        omdSingerName: omdSingerName,
        omdMusicInfoId: response.data.omdMusicInfoId,
        omdMusicInfoSongUrl: response.data.omdMusicInfoSongUrl,
        omdMusicInfoCoverUrl: response.data.omdMusicInfoCoverUrl || '',
        omdMusicInfoStatus: response.data.omdMusicInfoStatus || 0,
        omdMusicInfoDuration: response.data.omdMusicInfoDuration || 0,
        omdMusicInfoLikes: response.data.omdMusicInfoLikes || 0,
        omdMusicInfoAlbum: response.data.omdMusicInfoAlbum || '',
      };

      musicStore.setCurrentMusic(songData);
      await setupAudio(songData);

    } else {
      console.error('获取歌曲URL失败:', response.data);
    }
  } catch (error) {
    console.error('获取歌曲URL出错:', error);
  }
};

</script>

<template>
  <el-row :gutter="20">
    <!-- 循环生成多个 el-col，每个对应一个歌手卡片 -->
    <el-col :span="8" v-for="(singer, index) in singersData" :key="index">
      <div class="chart-container">
        <!-- 头像 + 基本信息布局 -->
        <div class="singer-header">
          <!-- 歌手头像（如果有字段，可替换成真实封面） -->
          <div class="singer-avatar" :style="{ backgroundImage: `url(${singer.omdUser.omdUserAvatar})` }"></div>
          <div class="singer-basic" @click="goToUserInfo(singer.omdUser.omdUserId)">
            <el-link
                type="primary"
                target="_blank"
                style="display: block; margin-bottom: 8px;"
            >
              <h3>{{ singer.omdSingerName }}</h3>
            </el-link>
            <div class="singer-tag" v-if="singer.omdSingerStatus === 1">
              <el-tag type="success" size="small">已认证</el-tag>
            </div>
          </div>
        </div>

        <!-- 信息列表 -->
        <ul class="singer-info-list">
          <li>
            <span class="label">公司：</span>
            {{ singer.omdSingerLabel }}
          </li>
          <li>
            <span class="label">风格：</span>
            {{ singer.omdSingerGenre }}
          </li>
          <li>
            <span class="label">代表作：</span>
            <div class="song-list">
              <el-button
                  v-for="(song, songIndex) in (() => {
                    const representativeSing = singer.omdSingerRepresentativeSing;
                    if (!representativeSing) return [];
                    return representativeSing.split(/[,、]/).filter(s => s.trim());
                  })()"
                  :key="songIndex"
                  type="text"
                  @click="playSong(singer.omdSingerId, song , singer.omdSingerName)"
              >
                {{ song }}
              </el-button>
            </div>
          </li>
          <li>
            <span class="label">个人简介：</span>
            <p class="introduction">{{ singer.omdSingerIntroduction }}</p>
          </li>
        </ul>
      </div>
    </el-col>
    <el-col :span="8" v-if="singersData.length === 0" class="empty-tip-col">
      <div class="chart-container">
        <div class="empty-tip">
          <p>暂无歌手数据</p>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<style scoped>
/* 全局柔和变量 */
:root {
  --primary-color: #409eff;   /* 主题色 */
  --light-gray: #f5f7fa;      /* 浅灰背景 */
  --border-color: #e5e7eb;    /* 边框色 */
  --text-main: #333;          /* 主要文字色 */
  --text-secondary: #666;     /* 次要文字色 */
  --text-light: #999;         /* 辅助文字色 */
}

/* 容器样式：卡片质感升级 */
.chart-container {
  padding: 24px;
  margin: 0 25px;
  background: #fff;
  border-radius: 16px;          /* 更大圆角更柔和 */
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.02); /* 更细腻阴影 */
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  display: flex;
  flex-direction: column;
  min-width: 350px;
  height: 500px;
  position: relative;
}

/* 悬浮反馈：更灵动的动效 */
.chart-container:hover {
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.04);
  transform: translateY(-6px);
}

/* 歌手头部：排版优化 */
.singer-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px; /* 增加间距 */
}

/* 头像：精致化处理 */
.singer-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background-color: var(--light-gray);
  background-size: cover;
  background-position: center;
  margin-right: 16px;
  border: 2px solid #fff; /* 白色边框防边缘锯齿 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 微阴影增加层次 */
}

/* 基础信息区：文字层级优化 */
.singer-basic {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 歌手名称：突出显示 */
.singer-basic h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
  margin: 0 0 4px; /* 紧凑排版 */
  line-height: 1.2;
}

/* 认证标签：位置调整 */
.singer-tag {
  margin-top: 2px;
}

/* 信息列表：分隔线升级 */
.singer-info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.singer-info-list li {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  margin: 8px 0;
}

/* 标签固定宽度 + 右对齐 */
.singer-info-list .label {
  width: 6em;          /* 固定宽度，适配中文标签 */
  text-align: right;   /* 标签右对齐 */
  margin-right: 12px;  /* 与内容保持间距 */
  color: #666;
  font-weight: 500;
}

/* 内容区域自适应 */
.singer-info-list li > div,
.singer-info-list li > p {
  flex: 1;             /* 让内容占满剩余空间 */
  margin: 0;
  line-height: 1.6;
  color: #333;
}

/* 代表作按钮：统一风格 */
.song-list .el-button {
  color: var(--primary-color) !important;
  padding: 0 6px !important;
  margin: 2px 0 !important;
  font-size: 14px;
  transition: all 0.2s ease;
}

.song-list .el-button:hover {
  color: #66b1ff !important;
  transform: translateX(4px);
}

/* 简介文本：可读性优化 */
.introduction {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-line; /* 保留换行 */
}

/* 空状态：更柔和的提示 */
.empty-tip {
  text-align: center;
  color: var(--text-light);
  margin: 0;
  padding-top: 10px;
}

.empty-tip-col .chart-container {
  justify-content: center;
  align-items: center;
}

/* 响应式适配：小屏幕优化 */
@media (max-width: 1200px) {
  .chart-container {
    margin: 0 20px;
  }
}

@media (max-width: 768px) {
  .singer-avatar {
    width: 64px;
    height: 64px;
  }
  .singer-basic h3 {
    font-size: 16px;
  }
}
</style>