<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 导入服务器请求函数
import { getApplicationsListByStatusService, updateApplicationsStatusService } from '@/api/admin.js';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);
const tableData = ref([]);
const loading = ref(false);

// 拒绝弹窗相关
const rejectDialogVisible = ref(false);
const rejectReason = ref('');
const currentApplicationId = ref(null);

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

// 获取申请列表
const fetchMusicList = async (pageNum, pageSize) => {
  loading.value = true;
  try {
    const response = await getApplicationsListByStatusService(pageNum, pageSize);

    if (response.code === 0) {
      tableData.value = response.data.items || [];
      totalPages.value = response.data.total || 0;
    } else {
      ElMessage.error(response.msg || '获取申请列表失败');
    }
  } catch (error) {
    console.error('获取申请列表出错:', error);
    ElMessage.error('获取申请列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 审核通过
const approveMusic = (omdApplicationsId) => {
  ElMessageBox.confirm(
      '确定要通过该歌手的入驻申请吗？',
      '通过确认',
      {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        type: 'success'
      }
  ).then(async () => {
    // 调用审核通过API
    try {
      const response = await updateApplicationsStatusService(omdApplicationsId, 1, '通过审核');
      if (response.code === 0) {
        ElMessage.success('审核通过成功');
        await fetchMusicList(pageNum.value, pageSize.value); // 刷新列表
      } else {
        ElMessage.error(response.msg || '审核通过失败');
      }
    } catch (error) {
      console.error('审核通过出错:', error);
      ElMessage.error('审核通过失败，请重试');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 打开拒绝弹窗
const rejectMusic = (omdApplicationsId) => {
  currentApplicationId.value = omdApplicationsId;
  rejectReason.value = '';
  rejectDialogVisible.value = true;
};

// 确认拒绝
const confirmReject = async () => {
  if (!currentApplicationId.value) return;

  try {
    const response = await updateApplicationsStatusService(
        currentApplicationId.value,
        2,
        rejectReason.value
    );

    if (response.code === 0) {
      ElMessage.success('已拒绝该申请');
      rejectDialogVisible.value = false;
      await fetchMusicList(pageNum.value, pageSize.value); // 刷新列表
    } else {
      ElMessage.error( '操作失败');
    }
  } catch (error) {
    console.error('审核拒绝出错:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchMusicList(pageNum.value, pageSize.value);
});
</script>

<template>
  <el-card class="application-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">歌手入驻申请审核</h1>
        <el-tag type="info" class="total-count">
          待审核申请：{{ totalPages }} 份
        </el-tag>
      </div>
    </template>

    <!-- 申请列表区域 -->
    <div class="application-list">
      <!-- 加载状态 -->
      <el-skeleton
          v-if="loading"
          :rows="6"
          :cols="1"
          class="application-skeleton"
      ></el-skeleton>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty
            description="暂无待审核的歌手申请"
            image-size="120"
        ></el-empty>
      </div>

      <!-- 申请卡片列表 -->
      <div v-else class="application-cards">
        <div
            class="application-card"
            v-for="(application, index) in tableData"
            :key="application.omdApplicationsId"
        >
          <div class="card-content">
            <!-- 歌手基本信息 -->
            <div class="application-header">
              <div class="singer-info">
                <h3 class="singer-name">{{ application.omdSingerName }}</h3>
                <div class="singer-meta">
                  <el-tag type="primary" size="small">{{ application.omdApplicationsGenre }}</el-tag>
                  <span class="apply-time">
                    <el-icon><Clock /></el-icon>
                    {{ application.omdApplicationsApplyTime }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 歌手简介 -->
            <div class="singer-intro">
              <h4 class="section-title">歌手简介</h4>
              <p class="intro-content">
                {{ application.omdApplicationsIntroduction || '无简介信息' }}
              </p>
            </div>

            <!-- 作品试听区域 -->
            <div class="audio-audition">
              <h4 class="section-title">作品试听</h4>
              <div class="audio-container">
                <audio
                    controls
                    :src="application.omdApplicationsSingSample"
                    class="audio-player"
                    preload="metadata"
                >
                  您的浏览器不支持音频播放
                </audio>
                <el-tooltip
                    v-if="!application.omdApplicationsSingSample"
                    content="暂无作品链接"
                    placement="top"
                >
                  <div class="audio-placeholder">
                    <el-icon><Headset /></el-icon>
                    <span>暂无作品可播放</span>
                  </div>
                </el-tooltip>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="application-actions">
              <el-button
                  type="primary"
                  icon="Check"
                  size="small"
                  @click="approveMusic(application.omdApplicationsId)"
              >
                通过申请
              </el-button>
              <el-button
                  type="danger"
                  icon="Close"
                  size="small"
                  @click="rejectMusic(application.omdApplicationsId)"
              >
                拒绝申请
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

    <!-- 拒绝理由弹窗 -->
    <el-dialog
        v-model="rejectDialogVisible"
        title="拒绝申请"
        width="400px"
        :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item
            label="拒绝理由"
            required
            :label-width="80"
        >
          <el-input
              v-model="rejectReason"
              type="textarea"
              :rows="4"
              placeholder="请输入拒绝理由（将告知申请人）"
              maxlength="200"
          ></el-input>
          <div class="word-count">{{ rejectReason.length }}/200</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button
            type="danger"
            @click="confirmReject"
            :disabled="!rejectReason.trim()"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>



<style scoped>
.application-container {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.total-count {
  background-color: #f5f7fa;
  color: #409eff;
}

.application-list {
  padding: 20px;
}

.application-skeleton {
  width: 100%;
}

.application-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.application-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.application-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-content {
  display: flex;
  flex-direction: column;
}

.application-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.singer-info {
  display: flex;
  flex-direction: column;
}

.singer-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}

.singer-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.apply-time {
  font-size: 13px;
  color: #888;
  display: flex;
  align-items: center;
}

.apply-time el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  background-color: #409eff;
  border-radius: 2px;
  margin-right: 8px;
}

.singer-intro {
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.intro-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.audio-audition {
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
  background-color: #fafafa;
}

.audio-container {
  width: 100%;
  position: relative;
}

.audio-player {
  width: 100%;
  height: 42px;
  outline: none;
  border-radius: 4px;
  background-color: #fff;
  padding: 5px;
  box-sizing: border-box;
}

.audio-placeholder {
  width: 100%;
  height: 42px;
  background-color: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.audio-placeholder el-icon {
  margin-right: 6px;
  font-size: 16px;
}

.application-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  background-color: #fafafa;
  border-top: 1px solid #f5f5f5;
}

.empty-state {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.pagination {
  padding: 20px 0 10px;
  display: flex;
  justify-content: center;
}

/* 拒绝弹窗样式 */
.word-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .application-cards {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>