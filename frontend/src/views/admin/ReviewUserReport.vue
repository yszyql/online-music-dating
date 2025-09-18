<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from "vue-router";

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入服务
import { getUserReportListService, updateUserReportStatusService } from '@/api/admin';
import { getOmdUserRoleListService } from "@/api/user.js";
import { updateUserStatusService } from '@/api/admin'; // 导入冻结用户接口

// 初始化路由实例
const router = useRouter();

// 状态管理
const loading = ref(false);
const reportList = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 详情对话框
const detailDialogVisible = ref(false);
const currentReport = ref(null);

// 处理对话框 - 改为必须点击确定才能关闭
const handleDialogVisible = ref(false);
const handleForm = reactive({
  id: null,
  status: 1,
  remark: ''
});

const handleRules = reactive({
  remark: [
    { required: true, message: '请输入处理备注', trigger: 'blur' },
    { min: 2, max: 200, message: '备注长度在2-200个字符之间', trigger: 'blur' }
  ]
});
const handleFormRef = ref(null);

// 冻结用户相关状态
const freezeDialogVisible = ref(false);
const currentUser = ref(null);
const freezeReason = ref('');
const freezeType = ref('1'); // 1-临时冻结，2-永久冻结
const freezeEndTime = ref('');

// 跳转到用户基本信息页面
const goToUserInfo = async (omdUserId) => {
  if (!omdUserId) {
    console.error('用户ID不存在，无法跳转');
    return;
  }

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
};

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true;
  try {
    const res = await getUserReportListService(pageNum.value, pageSize.value);
    reportList.value = res.data.items || [];
    total.value = res.data.total || 0;
  } catch (error) {
    ElMessage.error('获取举报列表失败');
    console.error('获取举报列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 分页相关方法
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  fetchReportList();
};

const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchReportList();
};

// 获取举报类型文本
const getReportTypeText = (type) => {
  const map = {
    1: '垃圾信息',
    2: '侮辱谩骂',
    3: '欺诈',
    4: '其他违规'
  };
  return map[type] || '未知类型';
};

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    0: '未处理',
    1: '已处理',
    2: '已驳回'
  };
  return map[status] || '未知状态';
};

// 获取类型标签样式
const getTypeTagType = (type) => {
  const map = {
    1: 'info',
    2: 'warning',
    3: 'danger',
    4: 'primary'
  };
  return map[type] || 'default';
};

// 获取状态标签样式
const getStatusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger'
  };
  return map[status] || 'default';
};

// 显示举报详情
const showReportDetail = (report) => {
  currentReport.value = report;
  detailDialogVisible.value = true;
};

// 关闭详情对话框
const handleCloseDetail = () => {
  detailDialogVisible.value = false;
};

// 处理通过 - 修改为首先显示冻结弹窗
const handleApprove = (report) => {
  // 保存当前举报信息
  currentReport.value = report;
  // 设置冻结用户信息
  currentUser.value = {
    ...report.omdReportedUser,
    omdUserStatus: 1 // 假设当前是正常状态
  };
  // 重置冻结表单
  freezeReason.value = `因举报违规: ${report.omdUserReportReason}`;
  freezeType.value = '1';
  freezeEndTime.value = '';
  // 显示冻结弹窗
  freezeDialogVisible.value = true;
};

// 处理驳回
const handleReject = (report) => {
  handleForm.id = report.omdUserReportId;
  handleForm.status = 2;
  handleForm.remark = '';
  handleDialogVisible.value = true;
};

// 关闭处理对话框 - 仅允许通过确定按钮关闭
const handleCloseHandleDialog = () => {
  // 不做任何操作，强制用户点击确定按钮
};

// 禁用过去的日期
const disablePastDates = (time) => {
  // 只能选择当前时间之后的日期时间
  return time.getTime() < Date.now() - 8.64e7; // 允许选择今天及以后
};

const handleFreezeTypeChange = () => {
  // 切换冻结类型时，重置结束时间
  if (freezeType.value === '2') {
    freezeEndTime.value = null;
  }
}

// 确认冻结用户
const confirmFreezeUser = async () => {
  if (!currentUser.value?.omdUserId) return;

  // 冻结时的参数校验
  if (freezeType.value === '1' && !freezeEndTime.value) {
    return ElMessage.error('请选择冻结结束时间');
  }
  // 永久冻结不需要结束时间
  if (freezeType.value === '2') {
    freezeEndTime.value = null;
  }

  // 将日期字符串转换为时间戳
  let freezeEndTimeValue = freezeEndTime.value;
  if (freezeEndTimeValue) {
    // 确保日期格式正确（处理时区问题）
    const date = new Date(freezeEndTimeValue);
    freezeEndTimeValue = date.getTime();
  }

  try {
    // 调用冻结用户接口
    const response = await updateUserStatusService(
        currentUser.value.omdUserId,
        freezeReason.value,
        0, // 0表示冻结
        parseInt(freezeType.value),
        freezeEndTimeValue
    );

    if (response.code === 0) {
      ElMessage.success('用户已冻结');
      // 关闭冻结弹窗
      freezeDialogVisible.value = false;
      // 显示处理对话框，设置为通过状态
      handleForm.id = currentReport.value.omdUserReportId;
      handleForm.status = 1;
      handleForm.remark = `已冻结用户，原因：${freezeReason.value}`;
      // 显示审核弹窗
      handleDialogVisible.value = true;
    } else {
      ElMessage.error('冻结用户失败');
    }
  } catch (error) {
    console.error('冻结用户出错:', error);
    ElMessage.error('冻结用户失败，请重试');
  }
};

// 提交处理结果
const submitHandle = async () => {
  if (!handleForm.id) {
    ElMessage.warning('举报ID不存在');
    return;
  }

  await handleFormRef.value.validate();

  try {
    await updateUserReportStatusService(
        handleForm.id,
        handleForm.status,
        handleForm.remark
    );

    ElMessage.success('处理成功');
    handleDialogVisible.value = false;
    await fetchReportList();
  } catch (error) {
    ElMessage.error('处理失败');
    console.error('处理举报失败:', error);
  }
};

// 生命周期钩子
onMounted(() => {
  fetchReportList();
});

</script>

<template>
  <div class="admin-user-report-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户举报审核</h1>
    </div>

    <!-- 数据表格 -->
    <el-card class="report-table-card">
      <el-table
          :data="reportList"
          stripe
          border
          highlight-current-row
          :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="举报信息" width="200">
          <template #header>
            <span>举报信息</span>
          </template>
          <template #default="scope">
            <div class="reporter-info">
              <el-avatar :src="scope.row.omdUser?.omdUserAvatar || defaultAvatar" size="40" />
              <div class="user-details">
                <div class="user-name" @click="goToUserInfo(scope.row.omdUserId)">
                  <el-link type="primary">{{ scope.row.omdUser?.omdUserNickname || scope.row.omdUser?.omdUserName }}</el-link>
                </div>
                <div class="report-time">
                  {{ scope.row.omdUserReportCreateTime }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="被举报用户" width="180">
          <template #default="scope">
            <div class="reported-user-info">
              <el-avatar :src="scope.row.omdReportedUser?.omdUserAvatar || defaultAvatar" size="36" />
              <div class="user-details" @click="goToUserInfo(scope.row.omdUserReportedUserId)">
                <div class="user-name">
                  <el-link type="primary">{{ scope.row.omdReportedUser?.omdUserNickname || scope.row.omdReportedUser?.omdUserName }}</el-link>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="举报类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.omdUserReportType)">{{ getReportTypeText(scope.row.omdUserReportType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="举报原因" width="200">
          <template #default="scope">
            <el-tooltip content="查看详情" placement="top">
              <div class="reason-text" @click="showReportDetail(scope.row)">{{ scope.row.omdUserReportReason || '-' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.omdUserReportStatus)">{{ getStatusText(scope.row.omdUserReportStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" width="120">
          <template #default="scope">
            {{ scope.row.omdAdminId ? '管理员' + scope.row.omdAdminId : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  type="primary"
                  size="small"
                  @click="handleApprove(scope.row)"
                  :disabled="scope.row.omdUserReportStatus !== 0"
              >
                通过
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click="handleReject(scope.row)"
                  :disabled="scope.row.omdUserReportStatus !== 0"
              >
                驳回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 -->
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

    <!-- 举报详情对话框 -->
    <el-dialog
        v-model="detailDialogVisible"
        title="举报详情"
        width="600px"
        :before-close="handleCloseDetail"
    >
      <div>
        <div class="detail-content">
          <div class="detail-item">
            <span class="item-label">举报人:</span>
            <el-link type="primary" @click="goToUserInfo(currentReport?.omdUserId)">{{ currentReport?.omdUser?.omdUserNickname || currentReport?.omdUser?.omdUserName}}</el-link>
          </div>
          <div class="detail-item">
            <span class="item-label">被举报用户:</span>
            <el-link type="warning" @click="goToUserInfo(currentReport?.omdUserReportedUserId)">{{ currentReport?.omdReportedUser?.omdUserNickname || currentReport?.omdReportedUser?.omdUserName }}</el-link>
          </div>
          <div class="detail-item">
            <span class="item-label">举报类型:</span>
            <el-tag :type="getTypeTagType(currentReport?.omdUserReportType)">{{ getReportTypeText(currentReport?.omdUserReportType) }}</el-tag>
          </div>
          <div class="detail-item">
            <span class="item-label">举报原因:</span>
            <div class="reason-content">{{ currentReport?.omdUserReportReason }}</div>
          </div>
          <div class="detail-item">
            <span class="item-label">举报证据:</span>
            <div class="evidence-content" v-if="currentReport?.omdUserReportEvidence">
              <template v-for="(imgUrl, index) in currentReport.omdUserReportEvidence.split(',').map(item => item.trim())" :key="index">
                <el-image
                    :src="imgUrl"
                    :preview-src-list="currentReport.omdUserReportEvidence.split(',').map(item => item.trim())"
                    style="max-width: 100%; cursor: zoom-in; margin-bottom: 10px; display: block;"
                    :alt="'证据' + (index + 1)"
                />
              </template>
            </div>
            <div v-else>-</div>
          </div>
          <div class="detail-item" v-if="currentReport?.omdUserReportStatus !== 0">
            <span class="item-label">处理备注:</span>
            <div>{{ currentReport?.omdUserReportHandleRemark || '-' }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理结果对话框 - 改为必须点击确定才能关闭 -->
    <el-dialog
        v-model="handleDialogVisible"
        title="处理举报"
        width="500px"
        :before-close="handleCloseHandleDialog"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
    >
      <el-form :model="handleForm" :rules="handleRules" ref="handleFormRef" label-width="100px">
        <el-form-item label="处理结果" prop="status">
          <el-radio-group v-model="handleForm.status" disabled>
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="remark">
          <el-input
              v-model="handleForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 冻结用户弹窗 -->
    <el-dialog
        v-model="freezeDialogVisible"
        title="冻结用户"
        width="500px"
        :close-on-click-modal="false"
    >
      <div class="freeze-dialog-content">
        <div class="user-info-in-dialog">
          <img
              :src="currentUser.value?.omdUserAvatar || defaultAvatar"
              :alt="currentUser.value?.omdUserName"
              class="dialog-avatar"
          >
          <div class="dialog-user-name">
            {{ currentUser.value?.omdUserName }}
          </div>
        </div>

        <!-- 冻结类型选择 -->
        <el-form-item
            label="冻结类型"
            required
            class="form-item"
        >
          <el-select
              v-model="freezeType"
              placeholder="请选择冻结类型"
              style="width: 100%"
              @change="handleFreezeTypeChange"
          >
            <el-option label="临时冻结" value="1" />
            <el-option label="永久冻结" value="2" />
          </el-select>
        </el-form-item>

        <!-- 冻结结束时间（仅临时冻结显示） -->
        <el-form-item
            label="冻结结束时间"
            required
            class="form-item"
            v-if="freezeType === '1'"
        >
          <el-date-picker
              v-model="freezeEndTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
              :disabled-date="disablePastDates"
              :minute-step="15"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
          <div class="time-hint">* 只能选择当前时间之后的日期</div>
        </el-form-item>

        <!-- 操作原因 -->
        <el-form-item
            label="冻结原因"
            required
            class="reason-form-item"
        >
          <el-input
              v-model="freezeReason"
              type="textarea"
              :rows="4"
              placeholder="请输入冻结原因"
              maxlength="200"
          ></el-input>
          <div class="word-count">{{ freezeReason.length }}/200</div>
        </el-form-item>
      </div>

      <template #footer>
        <el-button @click="freezeDialogVisible = false">取消</el-button>
        <el-button
            type="danger"
            @click="confirmFreezeUser"
            :disabled="!freezeReason.trim() || (freezeType === '1' && !freezeEndTime)"
        >
          确认冻结
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
}

.page-title {
  font-size: 24px;
  font-weight: bold;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.report-table-card {
  min-height: 500px;
}

.reporter-info,
.reported-user-info {
  display: flex;
  align-items: center;
}

.user-details {
  margin-left: 10px;
}

.user-name {
  font-weight: 500;
  cursor: pointer;
}

.user-name:hover {
  text-decoration: underline;
}

.report-time,
.user-id {
  font-size: 12px;
  color: #909399;
}

.reason-text {
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.reason-text:hover {
  text-decoration: underline;
}

.action-buttons {
  display: flex;
  gap: 5px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
}

.item-label {
  font-weight: 500;
  min-width: 80px;
  color: #606266;
}

.reason-content,
.evidence-content {
  flex: 1;
}

/* 冻结弹窗样式 */
.form-item {
  margin-bottom: 15px;
}

.time-hint {
  color: #666;
  font-size: 12px;
  margin-top: 5px;
  line-height: 1.5;
}

.reason-form-item {
  margin-top: 10px;
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.freeze-dialog-content {
  margin-top: 10px;
}

.user-info-in-dialog {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;
}

.dialog-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 15px;
}

.dialog-user-name {
  font-size: 16px;
  font-weight: 500;
}
</style>