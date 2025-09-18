<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 导入服务器请求函数
import { getMusicInfoListByStatusService, updateMusicInfoStatusService } from '@/api/admin.js';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);
const tableData = ref([]);
const loading = ref(false);

// 审核弹窗相关
const dialogVisible = ref(false);
const currentMusic = ref({});
const reviewStatus = ref(1); // 1-通过 2-拒绝
const reviewRemark = ref('');
const dialogTitle = ref('审核音乐');

// 监听页码变化
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  fetchMusicList(pageNum.value, pageSize.value);
};

// 监听每页数量变化
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  fetchMusicList(pageNum.value, pageSize.value);
};

// 获取音乐列表
const fetchMusicList = async (pageNum, pageSize) => {
  loading.value = true;
  try {
    const response = await getMusicInfoListByStatusService(pageNum, pageSize);

    if (response.code === 0) {
      tableData.value = response.data.items || [];
      totalPages.value = response.data.total || 0;
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

// 显示审核弹窗
const showReviewDialog = (music, status) => {
  currentMusic.value = { ...music };
  reviewStatus.value = status;
  reviewRemark.value = '';
  dialogTitle.value = status === 1 ? '通过审核' : '拒绝审核';
  dialogVisible.value = true;
};

// 确认审核
const confirmReview = async () => {
  try {
    // 调用审核API
    const response = await updateMusicInfoStatusService(
        currentMusic.value.omdMusicInfoId,
        reviewStatus.value,
        reviewRemark.value
    );

    if (response.code === 0) {
      ElMessage.success(reviewStatus.value === 1 ? '审核通过成功' : '审核拒绝成功');
      dialogVisible.value = false;
      await fetchMusicList(pageNum.value, pageSize.value); // 刷新列表
    } else {
      ElMessage.error(response.msg || '审核操作失败');
    }
  } catch (error) {
    console.error('审核操作出错:', error);
    ElMessage.error('审核操作失败，请重试');
  }
};

// 格式化时长（秒转 MM:SS）
const formatDuration = (seconds) => {
  if (!seconds) return '00:00';
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 页面加载时获取数据
onMounted(() => {
  fetchMusicList(pageNum.value, pageSize.value);
});
</script>

<template>
  <el-card class="music-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">待审核歌曲</h1>
        <el-tag type="warning" class="total-count">共 {{ totalPages }} 首</el-tag>
      </div>
    </template>

    <!-- 音乐列表区域 -->
    <div class="music-list">
      <!-- 骨架屏（加载中） -->
      <div v-if="loading" class="skeleton-container">
        <div class="skeleton-card" v-for="i in 6" :key="i"></div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="暂无待审核的音乐" />
      </div>

      <!-- 音乐卡片列表 -->
      <div v-else class="music-cards">
        <div class="music-card" v-for="(music, index) in tableData" :key="music.omdMusicInfoId">
          <div class="card-content">
            <!-- 封面图 -->
            <div class="music-cover">
              <div class="cover-wrapper">
                <img
                    :src="music.omdMusicInfoCoverUrl"
                    :alt="music.omdMusicInfoName"
                    class="cover-image"
                >
              </div>
              <el-tag class="status-tag" type="warning">待审核</el-tag>
            </div>
            <!-- 音乐信息 -->
            <div class="music-info">
              <h3 class="music-title">{{ music.omdMusicInfoName }}</h3>
              <p class="music-artist">
                <span class="music-album">{{ music.omdMusicInfoAlbum }}</span>
              </p>
              <p class="music-details">
                <span class="music-genre">{{ music.omdMusicInfoGenre }}</span>
                <span class="music-duration">{{ formatDuration(music.omdMusicInfoDuration) }}</span>
                <span class="music-time">{{ music.omdMusicInfoCreateTime }}</span>
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
                  type="primary"
                  icon="Check"
                  size="small"
                  @click="showReviewDialog(music, 1)"
              >
                通过
              </el-button>
              <el-button
                  type="danger"
                  icon="Close"
                  size="small"
                  @click="showReviewDialog(music, 2)"
              >
                拒绝
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

    <!-- 审核弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="450px"
        :close-on-click-modal="false"
    >
      <div class="review-dialog">
        <div class="dialog-cover">
          <img
              :src="currentMusic.omdMusicInfoCoverUrl"
              :alt="currentMusic.omdMusicInfoName"
              class="dialog-cover-image"
          >
        </div>
        <div class="dialog-info">
          <h4 class="dialog-music-title">{{ currentMusic.omdMusicInfoName }}</h4>
          <p class="dialog-music-album">{{ currentMusic.omdMusicInfoAlbum }}</p>
          <p class="dialog-music-genre">{{ currentMusic.omdMusicInfoGenre }}</p>
        </div>

        <el-form-item label="审核意见" class="review-opinion">
          <el-input
              v-model="reviewRemark"
              type="textarea"
              :rows="4"
              placeholder="请输入审核意见（选填）"
              maxlength="200"
          ></el-input>
          <div class="word-count">{{ reviewRemark.length }}/200</div>
        </el-form-item>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            :type="reviewStatus === 1 ? 'primary' : 'danger'"
            @click="confirmReview"
        >
          {{ reviewStatus === 1 ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>



<style scoped>
.music-container {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f5;
  background-color: #fafafa;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  position: relative;
  padding-left: 12px;
}

.title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background-color: #409eff;
  border-radius: 2px;
}

.total-count {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}

.music-list {
  padding: 24px;
  background-color: #fafafa;
}

.music-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.music-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border: none;
  position: relative;
}

.music-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.08);
}

/* 封面图样式优化 */
.music-cover {
  position: relative;
  margin: 0 auto;
  overflow: hidden;
  padding: 20px;
}

.cover-wrapper {
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.music-card:hover .cover-wrapper {
  transform: scale(1.05);
}

.cover-image {
  width: 200px;
  height: 200px;
  padding: 0 80px;
  object-fit: cover;
  transition: transform 0.7s cubic-bezier(0.4, 0, 0.2, 1);
}

.music-card:hover .cover-image {
  transform: scale(1.1);
}

.status-tag {
  position: absolute;
  top: 30px;
  right: 20px;
  z-index: 10;
  background-color: #ff7d00;
  color: #fff;
  border: none;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(255, 125, 0, 0.3);
}

.music-info {
  padding: 20px 20px 10px;
  flex: 1;
}

.music-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s;
}

.music-card:hover .music-title {
  color: #409eff;
}

.music-artist {
  margin-bottom: 12px;
}

.music-album {
  color: #666;
  font-size: 14px;
  background-color: #f5f7fa;
  padding: 4px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.music-card:hover .music-album {
  background-color: #ecf5ff;
}

.music-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #888;
  font-size: 13px;
  margin-bottom: 0;
  padding-top: 4px;
}

.music-time {
  font-size: 12px;
  opacity: 0.8;
}

.music-player {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  transition: background-color 0.3s;
}

.music-card:hover .music-player {
  background-color: #f5f9ff;
}

.audio-player {
  width: 100%;
  height: 42px;
  outline: none;
  filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.05));
}

.music-actions {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  gap: 10px;
}

.music-actions .el-button {
  flex: 1;
  transition: all 0.3s;
  border-radius: 6px;
  height: 36px;
  font-size: 13px;
}

.music-actions .el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
}

.music-actions .el-button--primary:hover {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.music-actions .el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.music-actions .el-button--danger:hover {
  background-color: #f78989;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.pagination {
  padding: 20px 0;
  display: flex;
  justify-content: center;
  background-color: #fafafa;
  margin-top: 10px;
  border-top: 1px solid #f0f0f0;
}

/* 骨架屏样式优化 */
.skeleton-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.skeleton-card {
  height: 380px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  animation: skeleton-loading 1.5s infinite ease-in-out;
  overflow: hidden;
}

.skeleton-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  animation: skeleton-shine 1.5s infinite;
}

@keyframes skeleton-loading {
  0%, 100% {
    background-color: #f9f9f9;
  }
  50% {
    background-color: #f0f0f0;
  }
}

@keyframes skeleton-shine {
  100% {
    left: 100%;
  }
}

.empty-state {
  padding: 100px 0;
  display: flex;
  justify-content: center;
  background-color: #fff;
  border-radius: 8px;
  margin-top: 20px;
}

/* 审核弹窗样式优化 */
.review-dialog {
  padding: 15px 0;
}

.dialog-cover {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  margin: 0 auto 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.dialog-cover:hover {
  transform: scale(1.03);
}

.dialog-cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dialog-info {
  text-align: center;
  margin-bottom: 25px;
  padding: 0 10px;
}

.dialog-music-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.dialog-music-album {
  color: #666;
  margin: 0 0 5px 0;
  font-size: 14px;
}

.dialog-music-genre {
  color: #888;
  margin: 0;
  font-size: 13px;
}

.review-opinion {
  margin-top: 20px;
}

.el-textarea__inner {
  border-radius: 6px;
  padding: 12px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .music-cards {
    grid-template-columns: 1fr;
  }

  .music-cover {
    width: 180px;
    height: 180px;
  }

  .card-header {
    padding: 15px 18px;
  }

  .title {
    font-size: 18px;
  }

  .music-list {
    padding: 15px;
  }
}
</style>