<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox, ElEmpty, ElTag, ElUpload, ElIcon } from 'element-plus';

// 导入状态管理
import { useAuthStore } from '@/stores/auth';

// 导入服务
import {
  getMusicReportListBySingerIdService,
  getMusicAppealStatusByMusicService,
  insertMusicAppealService
} from '@/api/singer';

// 状态管理
const authStore = useAuthStore();

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const tableData = ref([]);

// 申诉相关状态
const appealDialogVisible = ref(false);
const currentAppealMusic = ref(null);
const appealLoading = ref({}); // 记录每个音乐的申诉状态查询加载中
const submitLoading = ref(false);
const imageFileList = ref([]); // 上传的图片列表

// 申诉表单数据
const appealForm = reactive({
  omdMusicInfoId: null,
  omdMusicReportId: null,
  omdMusicAppealReason: '',
  omdMusicAppealEvidence: '' // 存储图片URL，用逗号分隔
});

// 表单验证规则
const appealRules = {
  omdMusicAppealReason: [
    { required: true, message: '请填写申诉理由', trigger: 'blur' },
    { min: 20, message: '申诉理由至少20个字符', trigger: 'blur' }
  ]
};

// 表单引用
const appealFormRef = ref(null);

// 页面加载时获取数据
onMounted(() => {
  fetchReportedMusicList();
});

// 获取被举报下架音乐列表
const fetchReportedMusicList = async () => {
  loading.value = true;
  try {
    const response = await getMusicReportListBySingerIdService(
        pageNum.value,
        pageSize.value
    );

    if (response.code === 0) {
      const data = response.data || {};
      tableData.value = data.items || [];
      total.value = data.total || 0;

      // 查询每个音乐的申诉状态
      await Promise.all(
          tableData.value.map(async (item) => {
            try {
              const appealResponse = await getMusicAppealStatusByMusicService(item.omdMusicInfoId);
              // 接口返回true表示未申诉，false表示已申诉
              item.appealed = !appealResponse.data;
            } catch (error) {
              console.error(`查询音乐 ${item.omdMusicInfoId} 申诉状态失败`, error);
              item.appealed = false; // 默认视为未申诉
            }
          })
      );
    } else {
      ElMessage.error('获取被举报音乐列表失败');
    }
  } catch (error) {
    console.error('获取被举报音乐列表错误:', error);
    ElMessage.error('获取数据失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 处理分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchReportedMusicList();
};

// 处理每页条数变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchReportedMusicList();
};

// 格式化举报类型
const formatReportType = (type) => {
  const typeMap = {
    1: '盗版侵权',
    2: '色情低俗',
    3: '暴力血腥',
    4: '政治敏感',
    5: '其他违规'
  };
  return typeMap[type] || `未知类型(${type})`;
};

// 格式化举报状态
const formatReportStatus = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已受理（违规成立）',
    2: '已驳回（违规不成立）'
  };
  return statusMap[status] || `未知状态(${status})`;
};

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '';
  const options = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  };
  return new Date(date).toLocaleString('zh-CN', options);
};

// 打开申诉弹窗
const handleAppealClick = async (row) => {
  // 记录当前申诉的音乐信息
  currentAppealMusic.value = { ...row };

  // 初始化表单数据
  appealForm.omdMusicInfoId = row.omdMusicInfoId;
  appealForm.omdMusicReportId = row.omdMusicReportId;
  appealForm.omdMusicAppealReason = '';
  appealForm.omdMusicAppealEvidence = '';

  // 清空图片列表
  imageFileList.value = [];

  // 显示弹窗
  appealDialogVisible.value = true;
};

// 关闭申诉弹窗
const handleDialogClose = () => {
  appealDialogVisible.value = false;
  // 重置表单验证
  nextTick(() => {
    appealFormRef.value?.resetFields();
  });
};

// 图片上传成功处理
const handleImageUploadSuccess = (response, file, fileList) => {
  if (response.code === 0 && response.data) {
    // 收集所有图片URL，用逗号分隔
    appealForm.omdMusicAppealEvidence = fileList
        .map(item => item.response?.data || item.url)
        .filter(Boolean)
        .join(',');
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('图片上传失败');
  }
};

// 图片上传失败处理
const handleImageUploadError = (error) => {
  console.error('图片上传错误:', error);
  ElMessage.error('图片上传失败，请重试');
};

// 超出上传数量限制处理
const handleExceed = (files, fileList) => {
  ElMessage.warning(`最多只能上传3张图片，已自动忽略多余的${files.length}张`);
};

// 提交申诉
const submitAppeal = async () => {
  try {
    // 表单验证
    await appealFormRef.value.validate();

    // 提交申诉
    submitLoading.value = true;
    const response = await insertMusicAppealService(appealForm);

    if (response.code === 0) {
      ElMessage.success('申诉提交成功，等待管理员审核');
      // 关闭弹窗
      handleDialogClose();
      // 更新列表中该音乐的申诉状态
      const index = tableData.value.findIndex(
          item => item.omdMusicInfoId === appealForm.omdMusicInfoId
      );
      if (index !== -1) {
        tableData.value[index].appealed = true;
      }
    } else {
      ElMessage.error('申诉提交失败');
    }
  } catch (error) {
    console.error('提交申诉错误:', error);
    if (error.name !== 'ValidationError') {
      ElMessage.error('申诉提交失败，请重试');
    }
  } finally {
    submitLoading.value = false;
  }
};

// 监听分页变化
watch([pageNum, pageSize], () => {
  fetchReportedMusicList();
});
</script>


<template>
  <div class="reported-music-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>被举报下架音乐管理</h2>
      <p class="header-desc">查看您的音乐因举报被下架的详情，可对未申诉的音乐进行申诉</p>
    </div>

    <!-- 被举报音乐列表 -->
    <el-card class="music-list-card">
      <el-table
          :data="tableData"
          border
          stripe
          :loading="loading"
          :row-key="row => row.omdMusicReportId"
      >
        <!-- 序号 -->
        <el-table-column type="index" label="序号" width="60" />

        <!-- 歌曲封面 -->
        <el-table-column label="歌曲封面" width="100">
          <template #default="scope">
            <el-image
                :src="scope.row.omdReportedMusic.omdMusicInfoCoverUrl"
                fit="cover"
                class="music-cover"
                :alt="scope.row.omdReportedMusic.omdMusicInfoName"
            />
          </template>
        </el-table-column>

        <!-- 歌曲信息与播放 -->
        <el-table-column label="歌曲信息" min-width="300">
          <template #default="scope">
            <div class="music-info">
              <div class="music-name">{{ scope.row.omdReportedMusic.omdMusicInfoName }}</div>
              <div class="music-report-type">
                举报类型：{{ formatReportType(scope.row.omdMusicReportType) }}
              </div>
              <div class="music-report-reason">
                举报原因：{{ scope.row.omdMusicReportReason }}
              </div>
              <div class="music-report-handle">
                处理结果：{{ scope.row.omdMusicReportHandleRemark || '无' }}
              </div>
              <div class="music-report-time">
                举报时间：{{ formatDateTime(scope.row.omdMusicReportCreateTime) }}
              </div>

              <!-- 音频播放器 -->
              <div class="audio-player">
                <audio
                    controls
                    :src="scope.row.omdReportedMusic.omdMusicInfoSongUrl"
                    class="audio-element"
                >
                  您的浏览器不支持音频播放
                </audio>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 处理状态 -->
        <el-table-column label="处理状态" width="140">
          <template #default="scope">
            <el-tag type="danger">
              {{ formatReportStatus(scope.row.omdMusicReportStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 申诉状态 -->
        <el-table-column label="申诉状态" width="140">
          <template #default="scope">
            <el-tag
                :type="scope.row.appealed ? 'warning' : 'info'"
            >
              {{ scope.row.appealed ? '已申诉' : '未申诉' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleAppealClick(scope.row)"
                :disabled="scope.row.appealed || appealLoading[scope.row.omdMusicInfoId]"
                :loading="appealLoading[scope.row.omdMusicInfoId]"
            >
              申诉
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="!loading && tableData.length === 0" class="empty-state">
        <el-empty description="暂无被举报下架的音乐" />
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total"
        />
      </div>
    </el-card>

    <!-- 申诉弹窗 -->
    <el-dialog
        title="音乐申诉"
        v-model="appealDialogVisible"
        width="600px"
        :before-close="handleDialogClose"
    >
      <!-- 被申诉音乐信息 -->
      <div class="appeal-music-info" v-if="currentAppealMusic">
        <el-image
            :src="currentAppealMusic.omdReportedMusic.omdMusicInfoCoverUrl"
            fit="cover"
            class="appeal-music-cover"
        />
        <div class="appeal-music-details">
          <h3 class="appeal-music-name">{{ currentAppealMusic.omdReportedMusic.omdMusicInfoName }}</h3>
          <p class="appeal-report-type">
            举报类型：{{ formatReportType(currentAppealMusic.omdMusicReportType) }}
          </p>
          <p class="appeal-report-reason">
            举报原因：{{ currentAppealMusic.omdMusicReportReason }}
          </p>
        </div>
      </div>

      <el-form
          ref="appealFormRef"
          :model="appealForm"
          :rules="appealRules"
          label-width="100px"
          class="appeal-form"
      >
        <!-- 申诉理由 -->
        <el-form-item
            label="申诉理由"
            prop="omdMusicAppealReason"
        >
          <el-input
              type="textarea"
              v-model="appealForm.omdMusicAppealReason"
              :rows="4"
              placeholder="请详细描述您的申诉理由..."
              maxlength="1000"
          />
          <div class="word-count">{{ appealForm.omdMusicAppealReason.length }}/1000</div>
        </el-form-item>

        <!-- 证据上传 -->
        <el-form-item
            label="申诉证据"
            prop="omdMusicAppealEvidence"
        >
          <el-upload
              class="image-uploader"
              action="/api/user/evidenceFileUpload"
              name="evidenceFile"
              list-type="picture-card"
              :headers="{ Authorization: `Bearer ${authStore.token}` }"
              :on-success="handleImageUploadSuccess"
              :on-error="handleImageUploadError"
              :limit="3"
              :on-exceed="handleExceed"
              :file-list="imageFileList"
          >
            <el-icon><Plus /></el-icon>
            <div class="upload-text">上传图片</div>
          </el-upload>
          <div class="upload-hint">
            提示：最多上传3张图片作为证据，支持JPG、PNG格式
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button
            type="primary"
            @click="submitAppeal"
            :loading="submitLoading"
        >
          提交申诉
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>



<style scoped>
.reported-music-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 5px;
}

.header-desc {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.music-list-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.music-cover {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.music-info {
  line-height: 1.8;
}

.music-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.music-report-type,
.music-report-reason,
.music-report-handle,
.music-report-time {
  font-size: 13px;
  color: #666;
}

.audio-player {
  margin-top: 10px;
  width: 100%;
}

.audio-element {
  width: 100%;
  max-width: 400px;
  margin-top: 8px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 申诉弹窗样式 */
.appeal-music-info {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #f0f0f0;
}

.appeal-music-cover {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  margin-right: 15px;
}

.appeal-music-details {
  flex: 1;
}

.appeal-music-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.appeal-report-type,
.appeal-report-reason {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.appeal-form {
  margin-top: 10px;
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.image-uploader {
  margin-bottom: 10px;
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>