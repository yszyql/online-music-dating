<script setup>
// 保持原有脚本逻辑不变
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAllMusicInfoListService, updateMusicInfoStatusService } from '@/api/admin.js';
import { getMusicInfoListByQueryParamsService } from "@/api/public.js";

const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);
const tableData = ref([]);
const loading = ref(false);
const isSearchMode = ref(false);
const musicGenres = ref([
  '流行', '摇滚', '民谣', '电子', '嘻哈', '爵士', '古典', '金属', '朋克', 'R&B', '乡村', '雷鬼', '蓝调', '福音','其它'
]);
const searchForm = ref({
  omdMusicInfoName: '',
  omdSingerName: '',
  omdMusicInfoAlbum: '',
  omdMusicInfoGenre: ''
});

const onSearch = async () => {
  isSearchMode.value = true;
  if (pageNum.value === 0) {
    pageNum.value = 1;
  }
  try {
    const result = await getMusicInfoListByQueryParamsService(pageNum.value, pageSize.value, searchForm.value);
    tableData.value = result.data?.items || [];
    totalPages.value = result.data?.total || 0;
  } catch (error) {
    console.error('搜索音乐出错:', error);
    ElMessage.error('搜索失败，请重试');
  }
};

const resetSearch = () => {
  searchForm.value = {
    omdMusicInfoName: '',
    omdSingerName: '',
    omdMusicInfoAlbum: '',
    omdMusicInfoGenre: ''
  };
  isSearchMode.value = false;
  fetchMusicList(pageNum.value, pageSize.value);
};

// 分页页码改变时的处理函数
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  isSearchMode.value ? onSearch() : fetchMusicList(pageNum.value, pageSize.value);
};

// 分页大小改变时的处理函数
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  pageNum.value = 1;
  isSearchMode.value ? onSearch() : fetchMusicList(pageNum.value, pageSize.value);
};

// 初始加载音乐列表
const fetchMusicList = async (pageNum, pageSize) => {
  loading.value = true;
  try {
    const response = await getAllMusicInfoListService(pageNum, pageSize);
    if (response.code === 0) {
      tableData.value = response.data?.items || [];
      totalPages.value = response.data?.total || 0;
    } else {
      ElMessage.error('获取音乐列表失败');
    }
  } catch (error) {
    console.error('获取音乐列表出错:', error);
    ElMessage.error('获取音乐列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 拒绝音乐
const rejectMusic = (omdMusicInfoId, currentStatus) => {
  const actionText = currentStatus === 1 ? '下架' : '恢复上架';
  const targetStatus = currentStatus === 1 ? 2 : 1;
  ElMessageBox.confirm(
      `确定要${actionText}这首音乐吗？`,
      `${actionText}确认`,
      {
        confirmButtonText: actionText,
        cancelButtonText: '取消',
        type: currentStatus === 1 ? 'warning' : 'info'
      }
  ).then(async () => {
    try {
      const response = await updateMusicInfoStatusService(omdMusicInfoId, targetStatus);
      if (response.code === 0) {
        ElMessage.success(`${actionText}成功`);
        isSearchMode.value ? onSearch() : fetchMusicList(pageNum.value, pageSize.value);
      } else {
        ElMessage.error(`${actionText}失败`);
      }
    } catch (error) {
      console.error(`${actionText}出错:", error`);
      ElMessage.error(`${actionText}失败，请重试`);
    }
  }).catch(() => {});
};

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds || seconds < 0) return '00:00';
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 页面加载时获取音乐列表
onMounted(() => {
  fetchMusicList(pageNum.value, pageSize.value);
});
</script>

<template>
  <el-card class="music-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">网站音乐管理</h1>
        <el-tag type="success" class="total-count">
          共 {{ totalPages }} 首音乐
        </el-tag>
      </div>
    </template>

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
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 音乐列表区域 -->
    <div class="music-list">
      <!-- 骨架屏（加载中） -->
      <div v-if="loading" class="skeleton-container">
        <div class="skeleton-card" v-for="i in 6" :key="i"></div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="暂无音乐数据" />
      </div>

      <!-- 音乐卡片列表 -->
      <div v-else class="music-cards">
        <div class="music-card" v-for="(music, index) in tableData" :key="music.omdMusicInfoId">
          <div class="card-content">
            <!-- 封面图（优化后） -->
            <div class="music-cover">
              <div class="cover-wrapper">
                <img
                    :src="music.omdMusicInfoCoverUrl"
                    :alt="music.omdMusicInfoName"
                    class="cover-image"
                >
              </div>
              <el-tag type="warning" v-if="music.omdMusicInfoStatus === 0" class="status-tag">未审核</el-tag>
              <el-tag type="success" v-if="music.omdMusicInfoStatus === 1"  class="status-tag">已通过</el-tag>
              <el-tag type="danger" v-if="music.omdMusicInfoStatus === 2"  class="status-tag">已下架</el-tag>
            </div>

            <!-- 音乐信息 -->
            <div class="music-info">
              <h3 class="music-title">{{ music.omdMusicInfoName }}</h3>
              <p class="music-artist">
                <span class="music-singer">{{ music.omdSinger?.omdSingerName }}</span>
                <span class="music-album">{{ music.omdMusicInfoAlbum }}</span>
              </p>
              <p class="music-details">
                <span class="music-genre">{{ music.omdMusicInfoGenre }}</span>
                <span class="music-duration">{{ formatDuration(music.omdMusicInfoDuration) }}</span>
              </p>
            </div>

            <!-- 音频播放器 -->
            <div class="music-player">
              <audio
                  controls
                  :src="music.omdMusicInfoSongUrl"
                  class="audio-player"
                  preload="metadata"
              ></audio>
            </div>

            <!-- 操作按钮 -->
            <div class="music-actions">
              <el-button
                  :type="music.omdMusicInfoStatus === 1 ? 'danger' : 'primary'"
                  :icon="music.omdMusicInfoStatus === 1 ? 'Delete' : 'Check' "
                  size="small"
                  @click="rejectMusic(music.omdMusicInfoId, music.omdMusicInfoStatus)"
                  v-if="music.omdMusicInfoStatus !== 0"
              >
                {{ music.omdMusicInfoStatus === 1 ? '下架' : '恢复上架' }}
              </el-button>
              <el-button
                  type="info"
                  size="small"
                  @click="rejectMusic(music.omdMusicInfoId, music.omdMusicInfoStatus)"
                  v-if="music.omdMusicInfoStatus === 0"
                  :disabled="music.omdMusicInfoStatus === 0"
              >
                待审核
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <template #footer>
      <div class="pagination">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 15, 20, 25]"
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total, sizes"
        />
      </div>
    </template>
  </el-card>
</template>



<style scoped>
/* 保持原有样式不变，只修改封面相关样式 */
.music-container {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.total-count {
  background-color: #f0f7ff;
}

.search-filter-container {
  padding: 15px 20px;
  background-color: #fafafa;
  border-bottom: 1px solid #f5f5f5;
}

.search-form {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.music-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.music-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.music-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.music-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 封面图优化样式 */
.music-cover {
  position: relative;
  padding: 15px;
  display: flex;
  justify-content: center;
}

.cover-wrapper {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.music-card:hover .cover-wrapper {
  transform: scale(1.03);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.2);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.music-card:hover .cover-overlay {
  opacity: 1;
}

.play-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  transform: scale(0.9);
  transition: transform 0.3s ease;
}

.music-card:hover .play-icon {
  transform: scale(1);
}

.status-tag {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.music-info {
  padding: 0 15px 15px;
  flex: 1;
}

.music-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.music-artist {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}

.music-singer, .music-album {
  color: #666;
  font-size: 14px;
}

.music-details {
  display: flex;
  justify-content: space-between;
  color: #888;
  font-size: 13px;
  margin-bottom: 10px;
}

.music-player {
  padding: 12px 15px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.audio-player {
  width: 100%;
  height: 40px;
  outline: none;
}

.music-actions {
  display: flex;
  justify-content: center;
  padding: 12px 15px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.pagination {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

/* 骨架屏样式 */
.skeleton-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.skeleton-card {
  height: 360px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
  0% {
    background-color: rgba(255, 255, 255, 0.8);
  }
  50% {
    background-color: rgba(240, 240, 240, 0.8);
  }
  100% {
    background-color: rgba(255, 255, 255, 0.8);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.el-form-item {
  margin-bottom: 10px;
}
</style>