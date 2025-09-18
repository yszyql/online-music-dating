<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 静态资源
const defaultCover = ref('@/assets/images/defaultCover.png'); // 默认封面图

// 导入服务
import { getMusicReportListService, updateMusicReportStatusService } from '@/api/admin';

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

// 举报列表数据
const reportList = ref([]);

// 弹窗状态
const playDialogVisible = ref(false);
const evidenceDialogVisible = ref(false);
const reviewDialogVisible = ref(false);

// 当前操作数据
const currentReport = ref(null);
const currentMusic = ref(null);
const audioRef = ref(null);

// 审核表单
const reviewForm = reactive({
  omdMusicReportId: null,
  status: 1,
  remark: ''
});

// 审核表单规则
const reviewRules = reactive({
  status: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入处理备注', trigger: 'blur' },
    { min: 5, max: 200, message: '备注长度在5-200个字符之间', trigger: 'blur' }
  ]
});

// 状态控制
const submitLoading = ref(false);
const reviewFormRef = ref(null);

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true;
  try {
    const response = await getMusicReportListService(pageNum.value, pageSize.value);
    reportList.value = response.data.items || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取举报列表失败');
    console.error('获取举报列表错误:', error);
  } finally {
    loading.value = false;
  }
};

// 分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchReportList();
};

// 每页条数变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchReportList();
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '未处理',
    1: '已通过（下架）',
    2: '已驳回'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态标签样式
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'danger',
    2: 'success'
  };
  return typeMap[status] || 'info';
};

// 播放音乐
const playMusic = (report) => {
  currentReport.value = report;
  currentMusic.value = report.omdReportedMusic;
  playDialogVisible.value = true;

  // 自动播放
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

// 查看证据
const viewEvidence = (report) => {
  currentReport.value = report;
  evidenceDialogVisible.value = true;
};

// 处理审核
const handleReview = (report, status) => {
  currentReport.value = report;
  reviewForm.omdMusicReportId = report.omdMusicReportId;
  reviewForm.status = status;
  reviewForm.remark = '';
  reviewDialogVisible.value = true;
};

// 提交审核结果
const submitReview = async () => {
  try {
    // 表单验证
    await reviewFormRef.value.validate();

    // 确认提交
    ElMessageBox.confirm(
        `确定要${reviewForm.status === 1 ? '通过举报并下架音乐' : '驳回举报并维持音乐上架'}吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(async () => {
      submitLoading.value = true;

      // 调用接口
      await updateMusicReportStatusService(
          reviewForm.omdMusicReportId,
          reviewForm.status,
          reviewForm.remark
      );
      reviewDialogVisible = false;
      await fetchReportList(); // 刷新列表
      ElMessage.success('处理成功');

    });
  } catch (error) {
    if (error.name !== 'ValidationError' && error !== 'cancel') {
      ElMessage.error('处理失败，请重试');
      console.error('审核提交失败:', error);
    }
  } finally {
    submitLoading.value = false;
  }
};

// 初始化加载数据
onMounted(() => {
  fetchReportList();
});


</script>

<template>
  <div class="music-report-review-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">音乐举报审核</h1>
    </div>

    <!-- 举报列表 -->
    <el-card class="report-list-card">
      <el-table
          :data="reportList"
          border
          stripe
          :loading="loading"
          :row-key="row => row.omdMusicReportId"
          empty-text="暂无举报数据"
      >
        <!-- 序号 -->
        <el-table-column type="index" label="序号" width="60" />

        <!-- 音乐信息 -->
        <el-table-column label="被举报音乐" min-width="300">
          <template #default="scope">
            <div class="music-info">
              <el-image
                  :src="scope.row.omdReportedMusic?.omdMusicInfoCoverUrl"
                  class="music-cover"
                  fit="cover"
              />
              <div class="music-detail">
                <div
                    class="music-name"
                    @click="playMusic(scope.row)"
                    style="cursor: pointer"
                >
                  {{ scope.row.omdReportedMusic?.omdMusicInfoName }}
                </div>
                <div class="report-time">
                  举报时间: {{ scope.row.omdMusicReportCreateTime }}
                </div>
                <audio
                    controls
                    :src="scope.row.omdReportedMusic?.omdMusicInfoSongUrl"
                />
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 举报原因 -->
        <el-table-column label="举报原因" min-width="200">
          <template #default="scope">
            <div class="report-reason">
              {{ scope.row.omdMusicReportReason || '无' }}
            </div>
          </template>
        </el-table-column>

        <!-- 举报证据 -->
        <el-table-column label="举报证据" width="120">
          <template #default="scope">
            <el-button
                type="text"
                @click="viewEvidence(scope.row)"
                v-if="scope.row.omdMusicReportEvidence"
            >
              查看证据
            </el-button>
            <span v-else>无</span>
          </template>
        </el-table-column>

        <!-- 处理状态 -->
        <el-table-column label="处理状态" width="120">
          <template #default="scope">
            <el-tag
                :type="getStatusTagType(scope.row.omdMusicReportStatus)"
            >
              {{ getStatusText(scope.row.omdMusicReportStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 处理人 -->
        <el-table-column label="处理人" width="100">
          <template #default="scope">
            {{ scope.row.omdAdminId ? '管理员#' + scope.row.omdAdminId : '未处理' }}
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleReview(scope.row, 1)"
                :disabled="scope.row.omdMusicReportStatus !== 0"
            >
              通过（下架）
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="handleReview(scope.row, 2)"
                :disabled="scope.row.omdMusicReportStatus !== 0"
            >
              驳回
            </el-button>
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

    <!-- 播放音乐弹窗 -->
    <el-dialog
        v-model="playDialogVisible"
        title="播放音乐"
        width="350px"
        :before-close="stopMusic"
    >
      <div class="music-player">
        <el-image
            :src="currentMusic?.omdMusicInfoCoverUrl || defaultCover"
            class="player-cover"
            fit="cover"
        />
        <div class="player-info">
          <h3>{{ currentMusic?.omdMusicInfoName || '未知音乐' }}</h3>
          <audio
              ref="audioRef"
              :src="currentMusic?.omdMusicInfoSongUrl"
              controls
              class="audio-player"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 证据查看弹窗 -->
    <el-dialog
        v-model="evidenceDialogVisible"
        title="举报证据"
        width="600px"
    >
      <div class="evidence-container">
        <el-image
            :src="currentReport?.omdMusicReportEvidence"
            :preview-src-list="[currentReport?.omdMusicReportEvidence]"
            fit="contain"
            class="evidence-image"
        />
      </div>
    </el-dialog>

    <!-- 审核处理弹窗 -->
    <el-dialog
        v-model="reviewDialogVisible"
        title="处理举报"
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
              :value="currentReport?.omdReportedMusic?.omdMusicInfoName || '未知音乐'"
              disabled
          />
        </el-form-item>
        <el-form-item label="处理结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过（下架音乐）</el-radio>
            <el-radio :label="2">驳回（维持上架）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="remark">
          <el-input
              v-model="reviewForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入处理原因或备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="submitReview"
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
  font-size: 20px;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.report-list-card {
  margin-bottom: 30px;
}

.music-info {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.music-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  margin-right: 10px;
}

.music-detail {
  flex: 1;
  overflow: hidden;
}

.music-name {
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.report-time {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
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

.evidence-container {
  text-align: center;
  padding: 20px 0;
}

.evidence-image {
  max-width: 100%;
  max-height: 500px;
  margin: 0 auto;
}

.player-info h3 {
  margin-bottom: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>