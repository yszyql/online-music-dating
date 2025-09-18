<script setup>
import {onMounted, ref} from 'vue';

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入服务器请求方法
import {
  agreeAddFriendService,
  anewAddFriendService,
  getFriendApplicationListByOmdUserIdService,
  getFriendListByOmdUserIdService,
  refuseAddFriendService
} from "@/api/friend";
import {ElMessage} from "element-plus";

// 响应数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalApplicationPages = ref(0);         // 总页数
const totalWaitPages = ref(0);         // 总页数
const totalRefusePages = ref(0);         // 总页数
const isCollapse = ref(false) // 折叠状态
const friendApplication = ref([]) // 好友申请列表
const applicationWaitResult = ref([]) // 等待申请结果列表
const applicationRefuseResult = ref([]) // 申请被拒绝结果列表

// 记录当前激活的按钮
const activeButton = ref('friendApplication')
const activateChildButton = ref('wait')

// 点击按钮时更新激活状态
const activateButton = (buttonName) => {
  activeButton.value = buttonName
  // 同时更新折叠状态
  if (buttonName === 'friendApplication') {
    isCollapse.value = false
    getFriendApplication(pageNum.value,pageSize.value)
  } else {
    isCollapse.value = true
    if(activateChildButton.value === 'wait') {
      getApplicationWaitResult(pageNum.value,pageSize.value,0)
    }else {
      getApplicationRefuseResult(pageNum.value,pageSize.value,2)
    }
  }
}

// 新增：处理子按钮激活的函数
const handleChildButtonActivate = (buttonName) => {
  activateChildButton.value = buttonName; // 更新激活状态
  // 同时加载对应的数据
  if (buttonName === 'wait') {
    getApplicationWaitResult(pageNum.value, pageSize.value, 0);
  } else if (buttonName === 'refuse') {
    getApplicationRefuseResult(pageNum.value, pageSize.value, 2);
  }
};

// 分页相关方法
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  if(activeButton.value === 'friendApplication') {
    getFriendApplication(pageNum.value,pageSize.value)
  } else {
    if(activateChildButton.value === 'wait') {
      getApplicationWaitResult(pageNum.value,pageSize.value,0)
    }else {
      getApplicationRefuseResult(pageNum.value,pageSize.value,2)
    }
  }
};

// 分页大小改变事件处理
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  if(activeButton.value === 'friendApplication') {
    getFriendApplication(pageNum.value,pageSize.value)
  } else {
    if(activateChildButton.value === 'wait') {
      getApplicationWaitResult(pageNum.value,pageSize.value,0)
    }else {
      getApplicationRefuseResult(pageNum.value,pageSize.value,2)
    }
  }
};

// 发送请求获取好友申请列表
const getFriendApplication = async (pageNum,pageSize) => {
  try {
    const result = await getFriendApplicationListByOmdUserIdService(pageNum,pageSize);
    friendApplication.value = result.data.items || [];
    totalApplicationPages.value = result.data.total || 0;
  }catch(error) {
    console.log(error);
  }
}

// 等待申请结果列表
const getApplicationWaitResult = async (pageNum,pageSize,omdFriendStatus) => {
  try {
    const result = await getFriendListByOmdUserIdService(pageNum,pageSize,omdFriendStatus);
    applicationWaitResult.value = result.data.items || [];
    totalWaitPages.value = result.data.total || 0;
  }catch(error) {
    console.log(error);
  }
}

// 申请好友被拒绝列表
const getApplicationRefuseResult = async (pageNum,pageSize,omdFriendStatus) => {
  try {
    const result = await getFriendListByOmdUserIdService(pageNum,pageSize,omdFriendStatus);
    applicationRefuseResult.value = result.data.items || [];
    totalRefusePages.value = result.data.total || 0;
  }catch(error) {
    console.log(error);
  }
}

// 同意添加好友
const agreeAddFriend = async (omdFriendUserId) => {
  try {
    await agreeAddFriendService(omdFriendUserId);
    await getFriendApplication(pageNum.value,pageSize.value)
    ElMessage.success('添加好友成功')
  }catch(error) {
    console.log(error);
  }
}

// 拒绝添加好友
const refuseAddFriend = async (omdFriendUserId) => {
  try {
    await refuseAddFriendService(omdFriendUserId);
    await getFriendApplication(pageNum.value,pageSize.value)
    ElMessage.success('拒绝添加好友成功')
  }catch(error) {
    console.log(error);
  }
}

// 重新申请
const reInsertApplication = async (omdFriendUserId) => {
  try {
    await anewAddFriendService(omdFriendUserId);
    await getApplicationRefuseResult(pageNum.value,pageSize.value,2);
    await getApplicationWaitResult(pageNum.value,pageSize.value,0)
    ElMessage.success('重新申请成功')
  }catch(error) {
    console.log(error);
  }
}

// 页面加载时获取好友申请列表
onMounted( async () => {
  await getFriendApplication(pageNum.value,pageSize.value)
})

</script>


<template>
  <div class="container">
    <!-- 左侧原始按钮（未修改） -->
    <div class="sidebar">
      <el-button
          link
          @click="activateButton('friendApplication')"
          :class="{ 'active': activeButton === 'friendApplication' }"
      >
        &nbsp;&nbsp;&nbsp;<h1>好友申请</h1>
      </el-button>
      <el-button
          link
          @click="activateButton('applicationResult')"
          :class="{ 'active': activeButton === 'applicationResult' }"
      >
        <h1>申请结果</h1>
      </el-button>
    </div>

    <!-- 好友申请内容区域（未修改，保持原有逻辑） -->
    <div class="content" v-show="!isCollapse">
      <!-- 此处可添加好友申请列表内容 -->
      <div class="list-container">
        <h2>好友申请列表</h2>
        <el-empty v-if="friendApplication.length === 0" description="暂无好友申请"></el-empty>
        <el-card
            v-for="item in friendApplication"
            :key="item.omdFriendId"
            class="list-item"
        >
          <div class="item-info">
            <el-avatar :src="item.omdFriendUser?.omdUserAvatar || defaultAvatar" size="40"></el-avatar>
            <div class="user-info">
              <p class="name">{{ item.omdFriendUser?.omdUserNickname || item.omdFriendUser?.omdUserName }}</p>
              <p class="time" style="width: 200px;">申请时间：{{ item.omdFriendCreateTime }}</p>
            </div>
            <div class="item-actions" style="margin-left: 600px">
              <el-button type="success" @click="agreeAddFriend(item.omdUserId)">同意</el-button>
              <el-button type="danger" @click="refuseAddFriend(item.omdUserId)">拒绝</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="friendApplication.length > 0">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="totalApplicationPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total"
        />
      </div>

    </div>

    <!-- 申请结果内容区域（修改后） -->
    <div class="content" v-show="isCollapse">
      <!-- 子按钮改为并排显示 -->
      <div class="child-buttons">
        <el-button
            link
            @click="handleChildButtonActivate('wait')"
            :class="{ 'active': activateChildButton === 'wait' }"
        >
          <h2>等待通过</h2>
        </el-button>
        <el-button
            link
            @click="handleChildButtonActivate('refuse')"
            :class="{ 'active': activateChildButton === 'refuse' }"
        >
          <h2>用户拒绝</h2>
        </el-button>
      </div>

      <!-- 等待通过列表 -->
      <div class="list-container" v-show="activateChildButton === 'wait'">
        <h2 style="margin-left: 500px">等待通过的申请</h2>
        <el-empty v-if="applicationWaitResult.length === 0" description="暂无等待通过的申请"></el-empty>
        <el-card
            v-for="item in applicationWaitResult"
            :key="item.omdFriendId"
            class="list-item"
        >
          <div class="item-info">
            <el-avatar :src="item.omdFriendUser?.omdUserAvatar || defaultAvatar" size="40"></el-avatar>
            <div class="user-info">
              <p class="name">{{ item.omdFriendUser?.omdUserNickname || item.omdFriendUser?.omdUserName }}</p>
              <p class="time" style="width: 200px;">申请时间：{{ item.omdFriendCreateTime }}</p>
            </div>
            <div class="item-status" style="margin-left: 500px;">
              <el-tag type="warning" size="large">等待对方通过...</el-tag>
            </div>
          </div>
        </el-card>

        <!-- 分页组件 -->
        <div class="pagination" v-if="applicationWaitResult.length > 0">
          <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="totalWaitPages"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
              layout="prev, pager, next, jumper, ->, total"
          />
        </div>

      </div>

      <!-- 被拒绝列表 -->
      <div class="list-container" v-show="activateChildButton === 'refuse'">
        <h2 style="margin-left: 500px">被拒绝的申请</h2>
        <el-empty v-if="applicationRefuseResult.length === 0" description="暂无被拒绝的申请"></el-empty>
        <el-card
            v-for="item in applicationRefuseResult"
            :key="item.omdFriendId"
            class="list-item"
        >
          <div class="item-info">
            <el-avatar :src="item.omdFriendUser?.omdUserAvatar || defaultAvatar" size="40"></el-avatar>
            <div class="user-info">
              <p class="name">{{ item.omdFriendUser?.omdUserNickname || item.omdFriendUser?.omdUserName }}</p>
              <p class="time" style="width: 200px;">申请时间：{{ item.omdFriendCreateTime }}</p>
            </div>
            <div class="item-status" style="margin-left: 550px;">
              <el-tag type="danger">已拒绝</el-tag>
              <el-button type="primary" style="margin-left: 30px" @click="reInsertApplication(item.omdFriendUserId)">重新申请</el-button>
            </div>
          </div>
        </el-card>

        <!-- 分页组件 -->
        <div class="pagination" v-if="applicationRefuseResult.length > 0">
          <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="totalRefusePages"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
              layout="prev, pager, next, jumper, ->, total"
          />
        </div>

      </div>
    </div>

  </div>
</template>

<style scoped>
.container {
  display: flex;
  padding-bottom: 80px; /* 底部留白，避免列表内容被分页遮挡 */
}

/* 左侧按钮样式（保留） */
.sidebar {
  width: 200px;
  border-right: 1px solid #eee;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 200px;
}

.sidebar .el-button {
  width: 100%;
  justify-content: flex-start;
  padding: 15px 20px;
}

.sidebar .active {
  color: var(--el-color-primary);
}

/* 内容区域样式 */
.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 子按钮并排样式 */
.child-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.child-buttons .el-button {
  padding: 10px 20px;
}

.child-buttons .active {
  color: var(--el-color-primary);
}

/* 列表样式 */
.list-container {
  margin-top: 20px;
}

.list-item {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info .name {
  font-weight: 500;
  margin-bottom: 5px;
}

.user-info .time {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.item-status {
  display: flex;
  align-items: center;
}

/* 分页组件固定在右下角 */
.pagination {
  z-index: 10; /* 层级高于列表内容 */
  padding: 10px 20px;
  border-radius: 4px;
}

</style>