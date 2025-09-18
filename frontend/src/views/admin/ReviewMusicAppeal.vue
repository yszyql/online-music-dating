<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 静态资源
const defaultCover = ref('@/assets/images/defaultCover.png');

// 导入服务
import { getMusicAppealListService, updateMusicAppealStatusService } from '@/api/admin';

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
// 申诉列表数据
const appealList = ref([]);

// 弹窗状态
const playDialogVisible = ref(false);
const evidenceDialogVisible = ref(false);
const reviewDialogVisible = ref(false);
const currentAppeal = ref(null);
const evidenceUrls = ref([]);
const audioRef = ref(null);

// 审核表单数据
const reviewForm = reactive({
  omdMusicAppealId: null,
  status: 1,
  remark: ''
});

// 表单验证规则
const reviewRules = reactive({
  status: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入处理备注', trigger: 'blur' },
    { min: 5, max: 500, message: '处理备注长度需在5-500字符之间', trigger: 'blur' }
  ]
});

// 状态控制
const submitLoading = ref(false);
const reviewFormRef = ref(null);

// 获取申诉列表
const fetchAppealList = async () => {
  loading.value = true;
  try {
    const response = await getMusicAppealListService(pageNum.value, pageSize.value);
    appealList.value = response.data.items || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取申诉列表失败');
    console.error('获取申诉列表错误:', error);
  } finally {
    loading.value = false;
  }
};

// 分页变化处理
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchAppealList();
};

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchAppealList();
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间';
  return new Date(dateStr).toLocaleString();
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过（上架）',
    2: '已驳回（维持下架）'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态标签样式
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  };
  return typeMap[status] || 'info';
};

// 播放音乐
const playMusic = (appeal) => {
  currentAppeal.value = appeal;
  playDialogVisible.value = true;

  // 自动播放（需用户交互触发）
  watch(
      () => playDialogVisible.value,
      (newVal) => {
        if (newVal && audioRef.value) {
          audioRef.value.play().catch(err => {
            console.warn('自动播放失败，需用户交互:', err);
          });
        }
      },
      { immediate: true }
  );
};

// 停止播放
const stopMusic = () => {
  if (audioRef.value) {
    audioRef.value.pause();
  }
  playDialogVisible.value = false;
};

// 查看申诉证据
const viewEvidence = (appeal) => {
  currentAppeal.value = appeal;
  // 分割证据URL
  evidenceUrls.value = appeal.omdMusicAppealEvidence
      ? appeal.omdMusicAppealEvidence.split(',').filter(url => url)
      : [];
  evidenceDialogVisible.value = true;
};

// 处理申诉
const handleAppeal = (appeal, status) => {
  currentAppeal.value = appeal;
  reviewForm.omdMusicAppealId = appeal.omdMusicAppealId;
  reviewForm.status = status;
  reviewForm.remark = '';
  reviewDialogVisible.value = true;
};

// 提交审核结果
const submitReview = async () => {
  try {
    // 表单验证
    await reviewFormRef.value.validate();

    // 二次确认
    const confirmText = reviewForm.status === 1
        ? '确定要通过申诉并上架音乐吗？'
        : '确定要驳回申诉并维持音乐下架状态吗？';

    await ElMessageBox.confirm(confirmText, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    submitLoading.value = true;
    // 调用接口提交处理结果
    await updateMusicAppealStatusService(
        reviewForm.omdMusicAppealId,
        reviewForm.status,
        reviewForm.remark
    );

    ElMessage.success('申诉处理成功');
    reviewDialogVisible.value = false;
    // 刷新列表
    await fetchAppealList();
  } catch (error) {
    if (error !== 'cancel' && error.name !== 'ValidationError') {
      ElMessage.error('申诉处理失败，请重试');
      console.error('处理申诉错误:', error);
    }
  } finally {
    submitLoading.value = false;
  }
};

// 初始化加载数据
onMounted(() => {
  fetchAppealList();
});

</script>

<template>
  <div class="music-appeal-review-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">音乐申诉审核</h1>
    </div>

    <!-- 申诉列表 -->
    <el-card class="appeal-list-card">
      <el-table
          :data="appealList"
          border
          stripe
          :loading="loading"
          :row-key="row => row.omdMusicAppealId"
          empty-text="暂无申诉数据"
      >
        <!-- 序号 -->
        <el-table-column type="index" label="序号" width="60" />

        <!-- 音乐信息 -->
        <el-table-column label="申诉音乐" min-width="250">
          <template #default="scope">
            <div class="music-info">
              <el-image
                  :src="scope.row.omdAppealMusicInfo?.omdMusicInfoCoverUrl"
                  class="music-cover"
                  fit="cover"
              />
              <div class="music-detail">
                <div
                    class="music-name"
                    @click="playMusic(scope.row)"
                    style="cursor: pointer"
                >
                  {{ scope.row.omdAppealMusicInfo?.omdMusicInfoName || '未知音乐' }}
                </div>
                <div class="appeal-time">
                  申诉时间: {{ scope.row.omdMusicAppealCreateTime }}
                </div>
              </div>
            </div>
            <div>
              <audio
                  ref="audioRef"
                  :src="scope.row.omdAppealMusicInfo?.omdMusicInfoSongUrl"
                  controls
              />
            </div>
          </template>
        </el-table-column>

        <!-- 申诉信息 -->
        <el-table-column label="申诉内容" min-width="300">
          <template #default="scope">
            <div class="appeal-content">
              <div class="appeal-reason">
                <span class="label">申诉理由：</span>
                <span class="content">{{ scope.row.omdMusicAppealReason }}</span>
              </div>
              <div class="appeal-evidence" v-if="scope.row.omdMusicAppealEvidence">
                <span class="label">申诉证据：</span>
                <el-button
                    type="text"
                    @click="viewEvidence(scope.row)"
                    class="evidence-btn"
                >
                  查看证据（{{ scope.row.omdMusicAppealEvidence.split(',').length }}张）
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 申诉状态 -->
        <el-table-column label="申诉状态" width="140">
          <template #default="scope">
            <el-tag
                :type="getStatusTagType(scope.row.omdMusicAppealStatus)"
            >
              {{ getStatusText(scope.row.omdMusicAppealStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 处理信息 -->
        <el-table-column label="处理信息" min-width="200">
          <template #default="scope">
            <div class="handle-info">
              <p v-if="scope.row.omdMusicAppealStatus !== 0">
                处理时间：{{ scope.row.omdMusicAppealHandleTime ? scope.row.omdMusicAppealHandleTime : '未知' }}
              </p>
              <p v-else>待审核处理</p>
              <p v-if="scope.row.omdMusicAppealHandleRemark" class="handle-remark">
                处理备注：{{ scope.row.omdMusicAppealHandleRemark }}
              </p>
            </div>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button
                  type="primary"
                  size="small"
                  @click="handleAppeal(scope.row, 1)"
                  :disabled="scope.row.omdMusicAppealStatus !== 0"
              >
                通过（上架）
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="handleAppeal(scope.row, 2)"
                  :disabled="scope.row.omdMusicAppealStatus !== 0"
              >
                驳回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 15, 20, 25]"
            :total="total"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total, sizes"
        />
      </div>
    </el-card>

    <!-- 音乐播放弹窗 -->
    <el-dialog
        v-model="playDialogVisible"
        title="播放音乐"
        width="400px"
        :before-close="stopMusic"
    >
      <div class="music-player">
        <el-image
            :src="currentAppeal?.omdAppealMusicInfo?.omdMusicInfoCoverUrl || defaultCover"
            class="player-cover"
            fit="cover"
        />
        <div class="player-info">
          <h3>{{ currentAppeal?.omdAppealMusicInfo?.omdMusicInfoName || '未知音乐' }}</h3>
          <audio
              ref="audioRef"
              :src="currentAppeal?.omdAppealMusicInfo?.omdMusicInfoSongUrl"
              controls
              class="audio-player"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 证据查看弹窗 -->
    <el-dialog
        v-model="evidenceDialogVisible"
        title="申诉证据"
        width="700px"
    >
      <div class="evidence-container">
        <el-image
            v-for="(url, index) in evidenceUrls"
            :key="index"
            :src="url"
            :preview-src-list="evidenceUrls"
            fit="contain"
            class="evidence-image"
        />
      </div>
    </el-dialog>

    <!-- 审核处理弹窗 -->
    <el-dialog
        v-model="reviewDialogVisible"
        title="处理申诉"
        width="500px"
    >
      <el-form
          ref="reviewFormRef"
          :model="reviewForm"
          :rules="reviewRules"
          label-width="100px"
      >
        <el-form-item label="音乐名称">
          <el-input
              :value="currentAppeal?.omdAppealMusicInfo?.omdMusicInfoName || '未知音乐'"
              disabled
          />
        </el-form-item>
        <el-form-item label="处理结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过（上架音乐）</el-radio>
            <el-radio :label="2">驳回（维持下架）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="remark">
          <el-input
              v-model="reviewForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入处理备注或理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="submitReview"
            :loading="submitLoading"
        >
          提交处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>



<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 22px;
  margin: 0;
  color: #333;
}

.filter-card {
  margin-bottom: 20px;
}

.appeal-list-card {
  margin-bottom: 30px;
}

.music-info {
  display: flex;
  align-items: center;
}

.music-cover {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  margin-right: 12px;
}

.music-detail {
  line-height: 1.6;
}

.music-name {
  font-weight: 500;
  color: #333;
}

.appeal-time {
  font-size: 12px;
  color: #666;
}

.appeal-content {
  line-height: 1.6;
}

.appeal-reason {
  margin-bottom: 8px;
}

.label {
  font-weight: 500;
  color: #333;
}

.evidence-btn {
  padding: 0;
  color: #409eff;
}

.evidence-btn:hover {
  color: #66b1ff;
}

.handle-info {
  line-height: 1.6;
}

.handle-remark {
  margin-top: 4px;
  color: #666;
  font-size: 13px;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.music-player {
  padding: 20px 0;
  text-align: center;
}

.player-cover {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  margin: 0 auto 15px;
}

.audio-player {
  width: 100%;
  margin-top: 15px;
}

.player-info h3 {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 15px;
}

.evidence-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
}

.evidence-image {
  width: 100%;
  max-height: 400px;
  border-radius: 4px;
}
</style>