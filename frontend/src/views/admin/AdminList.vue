<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import {useRouter} from "vue-router";

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png'

// 初始化路由实例
const router = useRouter();

// 导入服务
import {
  getAdminInfoService,
  getAllAdminListService,
  insertAdminService,
  updateAdminStatusService
} from '@/api/admin';

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const adminInfo = ref({});
const adminList = ref([]);
const searchKeyword = ref('');
const addDialogVisible = ref(false);

// 新增表单数据
const addForm = ref({
  omdAdminName: '',
  omdAdminPassword: '',
  omdAdminPhone: '',
  omdAdminEmail: '',
});
const addFormRef = ref(null);

// 表单验证规则
const addRules = ref({
  omdAdminName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  omdAdminPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  omdAdminPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  omdAdminEmail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
});

// 分页相关方法
const handlePageChange = (newPage) => {
  pageNum.value = newPage;
  fetchAdminList();
};

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  pageNum.value = 1;
  fetchAdminList();
};


// 是否为超级管理员
const isSuperAdmin = async () => {
  const result = await getAdminInfoService();
  adminInfo.value = result.data;
  if (result.data.omdAdminRole === 0) {
    ElNotification({
      title: '权限提示',
      message: '您没有权限访问此页面',
      type: 'warning',
      duration: 3000
    });
    return false;
  }
};

// 获取管理员列表
const fetchAdminList = async () => {
  try {
    const response = await getAllAdminListService(pageNum.value, pageSize.value);
    if (response.code === 0) {
      adminList.value = response.data.items || [];
      total.value = response.data.total || 0;
    } else {
      ElMessage.error(response.msg || '获取管理员列表失败');
    }
  } catch (error) {
    console.error('获取管理员列表出错:', error);
    ElMessage.error('获取管理员列表失败，请重试');
  }
};

// 重置新增表单
const resetAddForm = () => {
  addForm.value = {
    omdAdminName: '',
    omdAdminPassword: '',
    omdAdminPhone: '',
    omdAdminEmail: '',
  };
  if (addFormRef.value) {
    addFormRef.value.resetFields();
  }
};

// 提交新增表单
const submitAddForm = async () => {
  if (!addFormRef.value) return;

  addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await insertAdminService(addForm.value);
        if (response.code === 0) {
          ElMessage.success('新增管理员成功');
          addDialogVisible.value = false;
          await fetchAdminList();
        } else {
          ElMessage.error('新增管理员失败');
        }
      } catch (error) {
        console.error('新增管理员出错:', error);
        ElMessage.error('新增管理员失败，请重试');
      }
    } else {
      ElMessage.warning('请完善表单信息');
      return false;
    }
  });
};

// 切换管理员状态（冻结/解冻）
const toggleAdminStatus = (admin) => {
  const action = admin.omdAdminStatus === 1 ? '冻结' : '解冻';

  ElMessageBox.confirm(
      `确定要${action}管理员 "${admin.omdAdminName}" 吗？`,
      `${action}确认`,
      {
        confirmButtonText: `确认${action}`,
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const response = await updateAdminStatusService(admin.omdAdminId, {
        omdAdminStatus: admin.omdAdminStatus === 1? 0 : 1
      } );
      if (response.code === 0) {
        ElMessage.success(`${action}成功`);
        fetchAdminList();
      } else {
        ElMessage.error(`${action}失败`);
      }
    } catch (error) {
      console.error(`${action}管理员出错:`, error);
      ElMessage.error(`${action}失败，请重试`);
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 行点击事件（可选）
const handleRowClick = (row) => {
  const omdAdminId = row.omdAdminId;
  if (!omdAdminId) {
    console.error('用户ID不存在，无法跳转');
    return;
  }
  router.push({
    path: `/introduction/adminDetail/${omdAdminId}`
  });
};

// 页面加载时获取数据
onMounted(() => {
  if (!isSuperAdmin()) return;
  fetchAdminList();
});
</script>

<template>
  <el-card class="admin-container">
    <!-- 页面标题 -->
    <template #header>
      <div class="card-header">
        <h1 class="title">管理员管理</h1>
        <el-button
            v-if="isSuperAdmin"
            type="primary"
            icon="Plus"
            @click="addDialogVisible = true"
        >
          新增管理员
        </el-button>
      </div>
    </template>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索管理员用户名/手机号/邮箱"
          clearable
          @clear="fetchAdminList"
          @keyup.enter="fetchAdminList"
      >
        <template #suffix>
          <el-button icon="Search" @click="fetchAdminList"></el-button>
        </template>
      </el-input>
    </div>

    <!-- 管理员列表 -->
    <el-table
        :data="adminList"
        stripe
        border
        style="width: 100%"
        @row-click="handleRowClick"
    >
      <el-table-column prop="omdAdminId" label="ID" width="100" align="center"></el-table-column>
      <el-table-column label="头像" width="80" align="center">
        <template #default="scope">
          <el-avatar
              :size="40"
              :src="scope.row.omdAdminAvatar || defaultAvatar "
              :alt="scope.row.omdAdminName"
          ></el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="omdAdminName" label="用户名" align="center"></el-table-column>
      <el-table-column prop="omdAdminPhone" label="手机号" align="center"></el-table-column>
      <el-table-column prop="omdAdminEmail" label="邮箱" align="center"></el-table-column>
      <el-table-column label="角色" align="center">
        <template #default="scope">
          <el-tag
              :type="scope.row.omdAdminRole === 1 ? 'success' : 'info'"
          >
            {{ scope.row.omdAdminRole === 1 ? '超级管理员' : '普通管理员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template #default="scope">
          <el-tag
              :type="scope.row.omdAdminStatus === 1 ? 'success' : 'danger'"
          >
            {{ scope.row.omdAdminStatus === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="180">
        <template #default="scope">
          {{ scope.row.omdAdminCreateTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <div class="action-buttons">
            <el-button
                v-if="isSuperAdmin && scope.row.omdAdminId !== adminInfo.omdAdminId"
                size="small"
                :type="scope.row.omdAdminStatus === 1 ? 'warning' : 'success'"
                @click="toggleAdminStatus(scope.row)"
            >
              {{ scope.row.omdAdminStatus === 1 ? '冻结' : '解冻' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
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

    <!-- 新增管理员对话框 -->
    <el-dialog
        v-model="addDialogVisible"
        title="新增管理员"
        width="40%"
        @close="resetAddForm"
    >
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="120px">
        <el-form-item label="用户名" prop="omdAdminName">
          <el-input v-model="addForm.omdAdminName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="omdAdminPassword">
          <el-input v-model="addForm.omdAdminPassword" type="password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="omdAdminPhone">
          <el-input v-model="addForm.omdAdminPhone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="omdAdminEmail">
          <el-input v-model="addForm.omdAdminEmail" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddForm">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.admin-container {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表格行悬停样式 */
.el-table__row:hover {
  cursor: pointer;
  background-color: #f5f7fa !important;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.search-filter {
  padding: 15px 0;
  margin-bottom: 15px;
}

.pagination {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>