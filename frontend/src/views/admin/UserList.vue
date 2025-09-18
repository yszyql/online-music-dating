<script setup>
// 保持原有脚本逻辑不变
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {useRouter} from "vue-router";

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入服务
import { getAllUserService, getUserByUsernameService, updateUserStatusService } from '@/api/admin.js';
import {getOmdUserRoleListService} from "@/api/user.js";

// 响应式变量
const pageNum = ref(1); // 初始页码
const pageSize = ref(10); // 每页显示数量
const totalPages = ref(0); // 总页数
const tableData = ref([]); // 表格数据
const loading = ref(false); // 加载状态
const isSearchMode = ref(false); // 是否处于搜索模式
const searchForm = ref({ omdUserName: '' }); // 搜索表单
const freezeDialogVisible = ref(false); // 冻结用户弹窗可见性
const freezeReason = ref(''); // 冻结原因
const currentUser = ref({}); // 当前用户信息
const freezeType = ref('1'); // 1-临时冻结，2-永久冻结（默认临时）
const freezeEndTime = ref(''); // 冻结结束时间

// 初始化路由实例
const router = useRouter();

// 搜索用户
const onSearch = async () => {
  isSearchMode.value = true;
  pageNum.value = 1;
  try {
    const result = await getUserByUsernameService(searchForm.value.omdUserName, pageNum.value, pageSize.value);
    tableData.value = result.data?.items || [];
    totalPages.value = result.data?.total || 0;
  } catch (error) {
    console.error('搜索用户出错:', error);
    ElMessage.error('搜索失败，请重试');
  }
};

// 重置搜索条件
const resetSearch = () => {
  searchForm.value = { omdUserName: '' };
  isSearchMode.value = false;
  fetchUserList(pageNum.value, pageSize.value);
};

// 分页逻辑保持不变
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  isSearchMode.value ? onSearch() : fetchUserList(pageNum.value, pageSize.value);
};

// 调整每页显示数量
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  pageNum.value = 1;
  isSearchMode.value ? onSearch() : fetchUserList(pageNum.value, pageSize.value);
};

// 获取用户列表
const fetchUserList = async (pageNum, pageSize) => {
  loading.value = true;
  try {
    const response = await getAllUserService(pageNum, pageSize);
    if (response.code === 0) {
      tableData.value = response.data?.items || [];
      totalPages.value = response.data?.total || 0;
    } else {
      ElMessage.error(response.msg || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表出错:', error);
    ElMessage.error('获取用户列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 打开冻结弹窗
const handleStatusChange = (user) => {
  currentUser.value = { ...user };
  freezeReason.value = '';
  freezeType.value = '1'; // 默认临时冻结
  freezeEndTime.value = ''; // 重置结束时间
  freezeDialogVisible.value = true;
}

// 确认冻结/解冻操作
const confirmStatusChange = async () => {
  if (!currentUser.value.omdUserId) return;

  const isFreezing = currentUser.value.omdUserStatus === 1; // true表示冻结操作
  const actionText = isFreezing ? '冻结' : '解冻';

  // 冻结时的参数校验
  if (isFreezing) {
    // 临时冻结必须选择结束时间
    if (freezeType.value === '1' && !freezeEndTime.value) {
      return ElMessage.error('请选择冻结结束时间');
    }
    // 永久冻结不需要结束时间
    if (freezeType.value === '2') {
      freezeEndTime.value = null;
    }
  } else {
    // 解冻时强制设置参数
    freezeType.value = '0'; // 解冻时冻结类型设为0
    freezeEndTime.value = null; // 解冻时结束时间设为null
  }

  // 将日期字符串转换为时间戳（仅在冻结且有值时转换）
  let freezeEndTimeValue = freezeEndTime.value;
  if (isFreezing && freezeEndTimeValue) {
    // 确保日期格式正确（处理时区问题）
    const date = new Date(freezeEndTimeValue);
    freezeEndTimeValue = date.getTime();
  }

  try {
    // 调用更新接口
    const response = await updateUserStatusService(
        currentUser.value.omdUserId,
        freezeReason.value,
        isFreezing ? 0 : 1, // 0表示冻结，1表示解冻
        parseInt(freezeType.value), // 冻结类型：0(解冻)、1(临时)、2(永久)
        freezeEndTimeValue
    );

    if (response.code === 0) {
      ElMessage.success(`${actionText}成功`);
      freezeDialogVisible.value = false;
      isSearchMode.value ? onSearch() : fetchUserList(pageNum.value, pageSize.value);
    } else {
      ElMessage.error(response.msg || `${actionText}失败`);
    }
  } catch (error) {
    console.error(`${actionText}用户出错:`, error);
    ElMessage.error(`${actionText}失败，请重试`);
  }
};

// 禁用过去的日期
const disablePastDates = (time) => {
  // 只能选择当前时间之后的日期时间
  return time.getTime() < Date.now() - 8.64e7; // 减去一天的毫秒数，允许选择今天
};

// 性别映射
const gender = (genderData) => {
  const genderMap = { 0: '未知', 1: '男', 2: '女' };
  return genderMap[genderData] || '未知';
};

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
  }else {

    router.push({
      path: `/introduction/userDetail/${omdUserId}`
    });
  }

};

// 组件挂载时加载用户列表
onMounted(() => {
  fetchUserList(pageNum.value, pageSize.value);
});
</script>

<template>
  <el-card class="user-container">
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">网站用户管理</h1>
        <el-tag type="info" class="total-count">
          共 {{ totalPages }} 位用户
        </el-tag>
      </div>
    </template>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-container">
      <el-form :model="searchForm" class="search-form" inline>
        <el-form-item label="用户名">
          <el-input
              v-model="searchForm.omdUserName"
              placeholder="请输入用户名"
              clearable
              @clear="onSearch"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表区域 -->
    <div class="user-list">
      <!-- 骨架屏（加载中） -->
      <div v-if="loading" class="skeleton-container">
        <div class="skeleton-card" v-for="i in 6" :key="i"></div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="暂无用户数据" />
      </div>

      <!-- 用户卡片列表 -->
      <div v-else class="user-cards">
        <div class="user-card" v-for="(user, index) in tableData" :key="user.omdUserId">
          <div class="card-content">
            <!-- 用户信息行（含头像） -->
            <div class="user-header">
              <!-- 用户头像 -->
              <div class="user-avatar">
                <img
                    :src="user.omdUserAvatar || defaultAvatar"
                    :alt="user.omdUserName"
                    class="avatar-image"
                >
              </div>

              <!-- 基础信息 -->
              <div class="user-basic-info" @click="goToUserInfo(user.omdUserId)">
                <div class="name-status">
                  <h3 class="user-name">{{ user.omdUserName }}</h3>
                  <el-tag
                      :type="user.omdUserStatus === 1 ? 'success' : 'danger'"
                      class="status-tag"
                  >
                    {{ user.omdUserStatus === 1 ? '正常' : '已冻结' }}
                  </el-tag>
                </div>
                <p class="user-nickname">昵称：{{ user.omdUserNickname || '未设置昵称' }}</p>
              </div>
            </div>

            <!-- 详细信息 -->
            <div class="user-details">
              <div class="detail-item">
                <span class="detail-label">性别：</span>
                <span class="detail-value">{{ gender(user.omdUserGender) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">地区：</span>
                <span class="detail-value">{{ user.omdUserRegion || '未设置' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">注册时间：</span>
                <span class="detail-value">{{ user.omdUserCreateTime }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="user-actions">
              <el-button
                  :type="user.omdUserStatus === 1 ? 'danger' : 'success'"
                  size="small"
                  @click="handleStatusChange(user)"
              >
                {{ user.omdUserStatus === 1 ? '冻结' : '解冻' }}
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
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total"
        />
      </div>
    </template>

    <!-- 冻结用户弹窗 -->
    <el-dialog
        v-model="freezeDialogVisible"
        :title="currentUser.omdUserStatus === 1 ? '冻结用户' : '解冻用户'"
        width="500px"
        :close-on-click-modal="false"
    >
      <div class="freeze-dialog-content">
        <div class="user-info-in-dialog">
          <img
              :src="currentUser.omdUserAvatar || defaultAvatar"
              :alt="currentUser.omdUserName"
              class="dialog-avatar"
          >
          <div class="dialog-user-name">
            {{ currentUser.omdUserName }}
          </div>
        </div>

        <!-- 冻结类型选择 -->
        <el-form-item
            label="冻结类型"
            required
            class="form-item"
            v-if="currentUser.omdUserStatus === 1"
        >
          <el-select
              v-model="freezeType"
              placeholder="请选择冻结类型"
              style="width: 100%"
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
            v-if="currentUser.omdUserStatus === 1 && freezeType === '1'"
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
            label="操作原因"
            required
            class="reason-form-item"
        >
          <el-input
              v-model="freezeReason"
              type="textarea"
              :rows="4"
              placeholder="请输入操作原因"
              maxlength="200"
          ></el-input>
          <div class="word-count">{{ freezeReason.length }}/200</div>
        </el-form-item>
      </div>

      <template #footer>
        <el-button @click="freezeDialogVisible = false">取消</el-button>
        <el-button
            :type="currentUser.omdUserStatus === 1 ? 'danger' : 'success'"
            @click="confirmStatusChange"
            :disabled="!freezeReason.trim()"
        >
          {{ currentUser.omdUserStatus === 1 ? '确认冻结' : '确认解冻' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>



<style scoped>
.user-container {
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
  background-color: #f5f7fa;
}

.search-filter-container {
  padding: 15px 20px;
  background-color: #fafafa;
  border-bottom: 1px solid #f5f5f5;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-list {
  padding: 20px;
}

.user-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.user-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.user-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.card-content {
  padding: 16px;
}

/* 头像与基础信息区域 */
.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #f0f0f0;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid #f0f0f0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-basic-info {
  flex: 1;
}

.name-status {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
}

.user-nickname {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 详细信息区域 */
.user-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 36px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  color: #606266;
}

/* 操作按钮区域 */
.user-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px dashed #f0f0f0;
}

.pagination {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

/* 骨架屏样式 */
.skeleton-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 180px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
  0% { background-color: rgba(255, 255, 255, 0.8); }
  50% { background-color: rgba(240, 240, 240, 0.8); }
  100% { background-color: rgba(255, 255, 255, 0.8); }
}

.empty-state {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

/* 弹窗样式 */
.freeze-dialog-content {
  padding: 10px 0;
}

.user-info-in-dialog {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-avatar {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
  margin-right: 15px;
  border: 1px solid #f0f0f0;
}

.dialog-user-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.reason-form-item {
  margin-bottom: 0;
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

@media (max-width: 768px) {
  .user-cards {
    grid-template-columns: 1fr;
  }
}
</style>