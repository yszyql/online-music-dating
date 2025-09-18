<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {useRouter} from "vue-router";

import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入服务
import { getUserAppealListService, updateUserAppealStatusService } from '@/api/admin';
import { getOmdUserRoleListService } from '@/api/user';

// 初始化路由实例
const router = useRouter();

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

// 申诉列表数据
const appealList = ref([]);

// 弹窗状态
const evidenceDialogVisible = ref(false);
const reviewDialogVisible = ref(false);
const currentAppeal = ref(null);
const evidenceUrls = ref([]);

// 审核表单数据
const reviewForm = reactive({
  omdUserAppealId: null,
  status: 1,
  result: ''
});

// 表单验证规则
const reviewRules = reactive({
  status: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  result: [
    { required: true, message: '请输入处理结果', trigger: 'blur' },
    { min: 5, max: 500, message: '处理结果长度需在5-500字符之间', trigger: 'blur' }
  ]
});

// 状态控制
const submitLoading = ref(false);
const reviewFormRef = ref(null);


// 获取申诉列表
const fetchAppealList = async () => {
  loading.value = true;
  try {
    const response = await getUserAppealListService(pageNum.value, pageSize.value);
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已驳回'
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

// 处理申诉
const handleAppeal = (appeal, status) => {
  currentAppeal.value = appeal;
  reviewForm.omdUserAppealId = appeal.omdUserAppealId;
  reviewForm.status = status;
  reviewForm.result = '';
  reviewDialogVisible.value = true;
};

// 提交审核结果
const submitReview = async () => {
  try {
    // 表单验证
    await reviewFormRef.value.validate();

    // 二次确认
    const confirmText = reviewForm.status === 1
        ? '确定要通过申诉并解冻用户账号吗？'
        : '确定要驳回申诉并维持用户冻结状态吗？';

    await ElMessageBox.confirm(confirmText, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    submitLoading.value = true;
    // 调用接口提交处理结果
    await updateUserAppealStatusService(
        reviewForm.omdUserAppealId,
        reviewForm.status,
        reviewForm.result
    );

    ElMessage.success('申诉处理成功');
    reviewDialogVisible.value = false;
    // 刷新列表
    fetchAppealList();
  } catch (error) {
    if (error !== 'cancel' && error.name !== 'ValidationError') {
      ElMessage.error('申诉处理失败，请重试');
      console.error('处理申诉错误:', error);
    }
  } finally {
    submitLoading.value = false;
  }
};

// 跳转到用户基本信息页面（按要求实现）
const goToUserInfo = async (omdUserId) => {
  if (!omdUserId) {
    console.error('用户ID不存在，无法跳转');
    return;
  }

  try {
    const result = await getOmdUserRoleListService(omdUserId);
    const isSinger = result.data.some(role => role.omdRoleCode === 'ROLE_SINGER');

    if (isSinger) {
      router.push({
        path: `/introduction/singerDetail/${omdUserId}`
      });
    } else {
      router.push({
        path: `/introduction/userDetail/${omdUserId}`
      });
    }
  } catch (error) {
    console.error('查询用户角色失败:', error);
    ElMessage.error('跳转失败，请重试');
  }
};

// 初始化加载数据
onMounted(() => {
  fetchAppealList();
});

</script>


<template>
  <div class="user-appeal-review-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户申诉审核</h1>
    </div>

    <!-- 申诉列表 -->
    <el-card class="appeal-list-card">
      <el-table
          :data="appealList"
          border
          stripe
          :loading="loading"
          :row-key="row => row.omdUserAppealId"
          empty-text="暂无申诉数据"
      >
        <!-- 序号 -->
        <el-table-column type="index" label="序号" width="60" />

        <!-- 申诉用户信息 -->
        <el-table-column label="申诉用户" min-width="200">
          <template #default="scope">
            <div class="user-info" @click="goToUserInfo(scope.row.omdUserId)">
              <el-avatar :src="scope.row.omdAppealUser?.omdUserAvatar || defaultAvatar" class="user-avatar" />
              <div class="user-detail">
                <div class="user-name">
                  <el-link type="primary">{{ scope.row.omdAppealUser?.omdUserNickname || scope.row.omdAppealUser?.omdUserName }}</el-link>
                </div>
                <div class="user-id">ID: {{ scope.row.omdUserId || '未知' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 申诉信息 -->
        <el-table-column label="申诉内容" min-width="300">
          <template #default="scope">
            <div class="appeal-content">
              <div class="appeal-reason">
                <span class="label">申诉理由：</span>
                <span class="content">{{ scope.row.omdUserAppealReason || '无' }}</span>
              </div>
              <div class="appeal-time">
                提交时间：{{ scope.row.omdUserAppealCreateTime }}
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 申诉状态 -->
        <el-table-column label="申诉状态" width="120">
          <template #default="scope">
            <el-tag
                :type="getStatusTagType(scope.row.omdUserAppealStatus)"
            >
              {{ getStatusText(scope.row.omdUserAppealStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 处理信息 -->
        <el-table-column label="处理信息" min-width="200">
          <template #default="scope">
            <div class="handle-info">
              <p v-if="scope.row.omdUserAppealStatus !== 0">
                处理时间：{{ scope.row.omdUserAppealHandleTime ? scope.row.omdUserAppealHandleTime : '未知' }}
              </p>
              <p v-else>待审核处理</p>
              <p v-if="scope.row.omdUserAppealResult" class="handle-result">
                处理结果：{{ scope.row.omdUserAppealResult }}
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
                  :disabled="scope.row.omdUserAppealStatus !== 0"
              >
                通过
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="handleAppeal(scope.row, 2)"
                  :disabled="scope.row.omdUserAppealStatus !== 0"
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
            :total="total"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total"
        />
      </div>
    </el-card>

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
        <el-form-item label="用户信息">
          <div class="user-info-preview">
            <el-avatar :src="currentAppeal?.omdAppealUser?.omdUserAvatar || defaultAvatar" size="40" />
            <div class="user-name-preview">
              {{ currentAppeal?.omdAppealUser?.omdUserNickname || '未知用户' }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="处理结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过（解冻用户）</el-radio>
            <el-radio :label="2">驳回（维持冻结）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结果" prop="result">
          <el-input
              v-model="reviewForm.result"
              type="textarea"
              :rows="4"
              placeholder="请输入处理结果或理由"
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

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-avatar {
  width: 50px;
  height: 50px;
  margin-right: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.user-id {
  font-size: 12px;
  color: #666;
}

.appeal-content {
  line-height: 1.6;
}

.appeal-reason {
  margin-bottom: 8px;
}

.appeal-time {
  font-size: 12px;
  color: #666;
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

.handle-result {
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

.user-info-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name-preview {
  font-weight: 500;
  margin-top: 8px;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .filter-form {
    flex-wrap: wrap;
  }

  .el-table-column {
    min-width: auto !important;
  }
}
</style>