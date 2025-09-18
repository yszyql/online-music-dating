<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElTable, ElTableColumn, ElPagination, ElSelect, ElOption, ElImage, ElButton } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
// 导入接口，根据实际路径调整
import {
  getMusicInfoListBySingerIdService,
  deleteMusicInfoService,
  findMusicReportByIdService, updateMusicInfoStatusService
} from '@/api/singer';

const authStore = useAuthStore();

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

// 状态筛选
const selectedStatus = ref('1');
const musicList = ref([]);

// 分页参数等保持原有逻辑
onMounted(() => {
  fetchMusicList();
});

// 获取音乐列表，新增单个查询是否因举报下架逻辑
const fetchMusicList = async () => {
  loading.value = true;
  try {
    const response = await getMusicInfoListBySingerIdService(
        pageNum.value,
        pageSize.value,
        parseInt(selectedStatus.value)
    );

    if (response.code === 0) {
      const fetchedMusicList = response.data.items || [];
      // 遍历音乐列表，逐个查询是否因举报下架
      for (const item of fetchedMusicList) {
        const reportRes = await findMusicReportByIdService(item.omdMusicInfoId);
        // 根据接口返回判断，这里假设返回 { code: 0, data: true/false }
        item.isReported = reportRes.code === 0 ? reportRes.data : false;
      }
      musicList.value = fetchedMusicList;
      total.value = response.data.total || 0;
    } else {
      ElMessage.error('获取音乐列表失败');
    }
  } catch (error) {
    console.error('获取音乐列表错误:', error);
    ElMessage.error('获取音乐列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 状态筛选变化，重置页码并重新获取列表
const handleStatusChange = () => {
  pageNum.value = 1;
  fetchMusicList();
};

// 分页变化，重新获取列表
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchMusicList();
};

// 每页条数变化，重置页码并重新获取列表
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchMusicList();
};

// 格式化时长（秒 -> 分:秒）
const formatDuration = (seconds) => {
  if (!seconds) return '00:00';
  const minute = Math.floor(seconds / 60);
  const second = seconds % 60;
  return `${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}`;
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已下架'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',  // 待审核-警告色
    1: 'success',  // 已通过-成功色
    2: 'danger'    // 已下架-危险色
  };
  return typeMap[status] || 'info';
};

// 处理删除按钮点击事件
const handleDeleteClick = async (row) => {
  try {
    await deleteMusicInfoService(row.omdMusicInfoId);
    ElMessage.success('删除成功');
    // 删除后重新获取列表，更新页面数据
    await fetchMusicList();
  } catch (error) {
    console.error('删除歌曲错误:', error);
    ElMessage.error('删除失败，请重试');
  }
};

// 处理重新上传按钮点击事件（可根据实际需求，跳转到重新上传页面等）
const handleReUploadClick = async (row) => {
  try {
    await updateMusicInfoStatusService(row.omdMusicInfoId);
    ElMessage.success('重新上传成功');
    // 更新页面数据
    await fetchMusicList();
  } catch (error) {
    console.error('删除歌曲错误:', error);
    ElMessage.error('删除失败，请重试');
  }
};

</script>

<template>
  <div class="singer-music-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的音乐管理</h2>
      <p class="header-desc">查看和管理您上传的所有音乐作品</p>
    </div>

    <!-- 状态筛选 -->
    <div class="status-filter">
      <el-select
          v-model="selectedStatus"
          placeholder="全部状态"
          @change="handleStatusChange"
          clearable
      >
        <el-option label="待审核" value="0" />
        <el-option label="已通过" value="1" />
        <el-option label="已下架" value="2" />
      </el-select>
    </div>

    <!-- 音乐列表 -->
    <el-table
        :data="musicList"
        border
        stripe
        :loading="loading"
        :row-key="row => row.omdMusicInfoId"
        class="music-table"
    >
      <!-- 序号 -->
      <el-table-column type="index" label="序号" width="60" />

      <!-- 歌曲封面 -->
      <el-table-column label="封面" width="100">
        <template #default="scope">
          <el-image
              :src="scope.row.omdMusicInfoCoverUrl"
              fit="cover"
              class="music-cover"
              :alt="scope.row.omdMusicInfoName"
          />
        </template>
      </el-table-column>

      <!-- 歌曲信息 -->
      <el-table-column label="歌曲信息" min-width="300">
        <template #default="scope">
          <div class="music-info">
            <div class="music-name">{{ scope.row.omdMusicInfoName }}</div>
            <div class="music-album">专辑: {{ scope.row.omdMusicInfoAlbum }}</div>
            <div class="music-genre">风格: {{ scope.row.omdMusicInfoGenre }}</div>
            <div class="music-duration">时长: {{ formatDuration(scope.row.omdMusicInfoDuration) }}</div>
          </div>
          <audio
              controls
              :src="scope.row.omdMusicInfoSongUrl"
          >
            您的浏览器不支持音频播放
            <think>
              <source :src="scope.row.omdMusicInfoSongUrl" type="audio/mpeg" />
            </think>
          </audio>
        </template>
      </el-table-column>

      <!-- 状态 -->
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag
              :type="getStatusTagType(scope.row.omdMusicInfoStatus)"
          >
            {{ getStatusText(scope.row.omdMusicInfoStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 审核备注 -->
      <el-table-column label="审核备注" min-width="200">
        <template #default="scope">
          <div class="audit-remark">
            {{ scope.row.omdMusicInfoRemark || '无' }}
          </div>
        </template>
      </el-table-column>

      <!-- 上传时间 -->
      <el-table-column label="上传时间" width="180">
        <template #default="scope">
          {{ scope.row.omdMusicInfoCreateTime }}
        </template>
      </el-table-column>

      <!-- 操作 -->
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <!-- 重新上传按钮：仅当下架（status=2）且 不是因举报下架 时显示 -->
          <el-button
              v-if="scope.row.omdMusicInfoStatus === 2 && !scope.row.isReported"
              type="text"
              @click="handleReUploadClick(scope.row)"
          >
            重新上传
          </el-button>
          <!-- 因举报下架提示：当下架且是因举报下架时显示 -->
          <span
              v-else-if="scope.row.omdMusicInfoStatus === 2 && scope.row.isReported"
              class="reported-tip"
          >
            因举报下架
          </span>
          <!-- 其他状态显示占位符 -->
          <span v-else>-</span>
          <!-- 删除按钮：始终显示 -->
          <el-button
              type="text"
              @click="handleDeleteClick(scope.row)"
              style="margin-left: 10px;"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="!loading && musicList.length === 0" class="empty-state">
      <el-empty description="暂无音乐作品" />
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
  </div>
</template>

<style scoped>
.singer-music-list {
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

.status-filter {
  margin-bottom: 15px;
}

.music-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.music-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.music-info {
  line-height: 1.6;
}

.music-name {
  font-weight: 500;
  color: #333;
}

.music-album, .music-genre, .music-duration {
  font-size: 13px;
  color: #666;
}

.audit-remark {
  line-height: 1.5;
  word-break: break-all;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.reported-tip {
  color: #f56c6c; /* 红色提示 */
  font-size: 12px;
  line-height: 32px; /* 与按钮高度对齐，保证排版美观 */
}
</style>