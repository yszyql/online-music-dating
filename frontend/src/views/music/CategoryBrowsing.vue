<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import {useRouter} from "vue-router";

// 默认封面图
const defaultCover = '@/assets/images/defaultCover';

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'

// 导入自定义组件
import PlaylistAddToDialog from "@/components/playlist/PlaylistAddToDialog.vue";

// 导入服务器请求
import {
  getMusicInfoListByQueryParamsService,
  getTopMusicInfoListService
} from '@/api/public.js';

// 导入组合式函数
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";
import {useAudioPlayer} from "@/composable/useAudioPlayer.js";

// 状态管理
const authStore = useAuthStore()

// 初始化路由实例
const router = useRouter();

// 使用组合式函数
const{
  setupAudio
} = useAudioPlayer()

const {
  playlistList,
  loadingPlaylistList,
  showPlaylistAddToDialog,
  openAddToPlaylistDialog,
  addSongToPlaylistHandler,
  loadMorePlaylists,
  hasMore
} = usePlaylistManager();

// 状态控制
const isSearchMode = ref(false);
const isLoading = ref(false);
let loadingInstance = null;
const loadingTarget = ref(null);
// 音乐类型列表
const musicGenres = ref([
  '流行', '摇滚', '民谣', '电子', '嘻哈', '爵士', '古典', '金属', '朋克', 'R&B', '乡村', '雷鬼', '蓝调', '福音'
]);

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);         // 总页数
const musicList = ref([]);

// 计算属性
const currentRankList = computed(() => {
  return rankData.value[`${activeRankTab.value}RankData`] || [];
});

// 排行榜相关
const activeRankTab = ref('total');
const rankData = ref({
  totalRankData: [],
  userRankData: [],
  guestRankData: []
});

// 搜索表单数据
const searchForm = ref({
  omdMusicInfoName: '',
  omdSingerName: '',
  omdMusicInfoAlbum: '',
  omdMusicInfoGenre: ''
});

// 分页相关方法
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  fetchMusicList();
};

const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  fetchMusicList();
};

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

// 获取排行榜数据
const fetchRankData = async () => {
  isLoading.value = true;
  try {
    const response = await getTopMusicInfoListService(10); // 获取前10名
    if (response.data) {
      rankData.value = response.data;
      // 如果没有搜索关键词，默认显示总排行榜
      if (!isSearchMode.value) {
        activeRankTab.value = 'total';
      }
    } else {
      ElMessage.error('获取排行榜数据失败');
    }
  } catch (error) {
    console.error('获取排行榜数据错误:', error);
    ElMessage.error('获取排行榜数据失败');
  } finally {
    isLoading.value = false;
  }
};

// 搜索音乐
const onSearch = async () => {
  isSearchMode.value = true;
  pageNum.value = 1; // 重置页码
  await fetchMusicList();
};

// 获取音乐列表
const fetchMusicList = async () => {
  showLoading();
  isLoading.value = true;
  try {
    const response = await getMusicInfoListByQueryParamsService(
        pageNum.value,
        pageSize.value,
        searchForm.value
    );

    if (response.data) {
      musicList.value = response.data.items || [];
      totalPages.value = response.data.total || 0;
    } else {
      ElMessage.error('搜索音乐失败');
    }
  } catch (error) {
    console.error('搜索音乐错误:', error);
    ElMessage.error('搜索音乐失败');
  } finally {
    isLoading.value = false;
    hideLoading();
  }
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    omdMusicInfoName: '',
    omdSingerName: '',
    omdMusicInfoAlbum: '',
    omdMusicInfoGenre: ''
  };
  isSearchMode.value = false;
};

// 显示加载状态
const showLoading = () => {
  if (loadingInstance) return;
  isLoading.value = true;
  loadingInstance = ElLoading.service({
    target: loadingTarget.value,
    text: '加载中...',
    background: 'rgba(255, 255, 255, 0.7)'
  });
};

// 隐藏加载状态
const hideLoading = () => {
  if (loadingInstance) {
    loadingInstance.close();
    loadingInstance = null;
    isLoading.value = false;
  }
};

// 切换排行榜选项卡
const switchRankTab = (tab) => {
  activeRankTab.value = tab.name;
  isSearchMode.value = false;
};

// 处理添加按钮点击
const handleAddClick = async (song) => {
  await openAddToPlaylistDialog(song, [song.omdMusicInfoId]);
};

// 监听搜索模式变化，在退出搜索模式时加载排行榜数据
watch(isSearchMode, (newVal) => {
  if (!newVal) {
    fetchRankData();
  }
});

// 生命周期钩子
onMounted(() => {
  loadingTarget.value = document.querySelector('.music-list-container');
  fetchRankData(); // 初始化加载排行榜数据
});


</script>

<template>
  <div class="music-browse-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">音乐分类浏览</h1>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-container">
      <el-form :model="searchForm" class="search-form" inline>
        <el-form-item label="音乐名称">
          <el-input v-model="searchForm.omdMusicInfoName" placeholder="请输入音乐名称" clearable @clear="onSearch"></el-input>
        </el-form-item>
        <el-form-item label="歌手名称">
          <el-input v-model="searchForm.omdSingerName" placeholder="请输入歌手名称" clearable @clear="onSearch"></el-input>
        </el-form-item>
        <el-form-item label="音乐专辑">
          <el-input v-model="searchForm.omdMusicInfoAlbum" placeholder="请输入专辑名称" clearable @clear="onSearch"></el-input>
        </el-form-item>
        <el-form-item label="音乐类型">
          <el-select v-model="searchForm.omdMusicInfoGenre" placeholder="请选择类型" clearable @clear="onSearch" style="width: 130px">
            <el-option v-for="genre in musicGenres" :key="genre" :label="genre" :value="genre"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch" @keyup.enter="onSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 排行榜切换选项卡 -->
    <div v-if="isSearchMode === false" class="rank-tabs">
      <el-tabs v-model="activeRankTab" @tab-click="switchRankTab">
        <el-tab-pane label="总排行榜" name="total"></el-tab-pane>
        <el-tab-pane label="用户排行榜" name="user"></el-tab-pane>
        <el-tab-pane label="游客排行榜" name="guest"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 暂无数据提示 -->
    <div
        v-if="(isLoading && (musicList.length === 0 || currentRankList.length === 0)) ||
        (musicList.length === 0 && currentRankList.length === 0 && !isSearchMode)"
        class="empty-tip"
    >
      <el-empty description="暂无音乐数据"></el-empty>
    </div>

    <!-- 音乐列表区域 -->
    <div class="music-list-container">
      <!-- 搜索结果列表 -->
      <div v-if="isSearchMode" class="search-result-section">

        <el-card class="music-card" v-for="(music, index) in musicList" :key="music.omdMusicInfoId">
          <div class="music-card-content">
            <div class="music-cover">
              <img :src="music.omdMusicInfoCoverUrl || defaultCover" alt="音乐封面" @click="setupAudio(music)">
            </div>
            <div class="music-info">
              <h3 class="music-name" @click="setupAudio(music)">{{ music.omdMusicInfoName }}</h3>
              <p class="music-artist" @click="goToUserInfo(music.omdSinger?.omdSingerId || '')">
                {{ music.omdSinger?.omdSingerName || '未知歌手' }}
              </p>
              <p class="music-album">{{ music.omdMusicInfoAlbum || '未知专辑' }}</p>
              <p class="music-genre">{{ music.omdMusicInfoGenre || '未知类型' }}</p>
            </div>
            <div class="music-actions">
              <el-button type="primary" size="small" @click="setupAudio(music)">
                <el-icon><VideoPlay /></el-icon> 播放
              </el-button>
              <el-button type="text" size="small" @click="handleAddClick(music)" v-if="!authStore.isGuest">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 通用分页组件 -->
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total"
        />
      </div>

      <!-- 排行榜列表 -->
      <div v-else class="rank-section">
        <el-card class="rank-card" v-for="(music, index) in currentRankList" :key="music.omdMusicInfoId">
          <div class="rank-card-content">
            <div class="rank-number" :class="{'top-three': index < 3}">{{ index + 1 }}</div>
            <div class="music-cover">
              <img :src="music.omdMusicInfoCoverUrl || defaultCover" alt="音乐封面" @click="setupAudio(music)">
            </div>
            <div class="music-info">
              <h3 class="music-name" @click="setupAudio(music)">{{ music.omdMusicInfoName }}</h3>
              <p class="music-artist" @click="goToUserInfo(music.omdSingerId || '')">
                {{ music.omdSingerName || '未知歌手' }}
              </p>
              <p class="music-play-count">播放量: {{ music.playCount || 0 }}</p>
            </div>
            <div class="music-actions">
              <el-button type="primary" size="small" @click="setupAudio(music)">
                <el-icon><VideoPlay /></el-icon> 播放
              </el-button>
              <el-button type="text" size="small" @click="handleAddClick(music)" v-if="!authStore.isGuest">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 加载中提示 -->
    <div v-if="isLoading" class="loading-container">
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

</template>

<style scoped>
.music-browse-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.search-filter-container {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.rank-tabs {
  margin-bottom: 20px;
}

.music-list-container {
  min-height: 400px;
  position: relative;
}

.music-card {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.music-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.music-card-content {
  display: flex;
  align-items: center;
}

.music-cover {
  width: 60px;
  height: 60px;
  margin-right: 15px;
  overflow: hidden;
  border-radius: 4px;
}

.music-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s;
}

.music-cover img:hover {
  transform: scale(1.05);
}

.music-info {
  flex: 1;
  margin-right: 15px;
}

.music-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
  cursor: pointer;
  color: #333;
}

.music-artist, .music-album, .music-genre {
  font-size: 13px;
  color: #666;
  margin-bottom: 3px;
}

.music-artist {
  cursor: pointer;
}

.music-artist:hover {
  color: #409eff;
}

.music-actions {
  display: flex;
  gap: 10px;
}

.rank-card {
  margin-bottom: 15px;
  position: relative;
}

.rank-card-content {
  display: flex;
  align-items: center;
}

.rank-number {
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  margin-right: 15px;
  border-radius: 50%;
}

.top-three {
  background-color: #ffd700;
  color: #fff;
}

.music-play-count {
  font-size: 13px;
  color: #999;
}

.empty-tip {
  padding: 50px 0;
  text-align: center;
}

.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 10;
}
</style>