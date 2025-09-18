<script setup>
import { onMounted, ref} from 'vue'
import { useRoute } from 'vue-router';
import {ElMessage, ElMessageBox} from "element-plus";

// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png';

// 导入自定义组件
import PlaylistList from "@/components/playlist/PlaylistList.vue";
import ReportUserDialog from "@/components/message/ReportUserDialog.vue";
import MusicRankItem from "@/components/home/MusicRankItem.vue";
import PlaylistAddToDialog from "@/components/playlist/PlaylistAddToDialog.vue";

// 导入服务
import {
  getUserInfoByOmdUserIdService,
  getUserReportStatusService,
  getMusicInfoBySingerIdService,
  getOmdSingerInfoByOmdUserIdService
} from "@/api/user.js";
import {blackFriendService, deleteBlackUserService, getFriendStatusService, insertFriendService} from "@/api/friend.js";

// 导入组合式函数
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";

// 获取路由参数
const route = useRoute();

// 组合式函数
const {
  playlistList,
  loadingPlaylistList,
  showPlaylistAddToDialog,
  openAddToPlaylistDialog,
  addSongToPlaylistHandler,
  loadMorePlaylists,
  hasMore
} = usePlaylistManager();

// 响应式变量
const pageNum = ref(1); // 初始页码
const pageSize = ref(10); // 每页显示数量
const totalPages = ref(0); // 总页数
const routeUserId = route.params.omdSingerId; // 从路由参数中获取用户ID
const omdUserData = ref(null); // 用户信息数据
const singerInfo = ref(null); // 歌手信息数据
const singerMusicList = ref([]); // 歌手歌曲列表数据
const omdFriendStatus = ref(null); // 好友状态
const omdUserReportStatus = ref(false); // 举报状态
const reportPopupVisible = ref(false); // 弹窗显示状态

// 性别计算
const gender = (genderData) =>{
  if(genderData === 0){
    return '男'
  }else if(genderData === 1){
    return '女'
  }else if(genderData === 2){
    return '未知'
  }else{
    return '未知'
  }
}

// 分页逻辑保持不变
const handlePageChange = (newPageNum) => {
  pageNum.value = newPageNum;
  getSingerMusicList(pageNum.value,pageSize.value);
};

// 调整每页显示数量
const handleSizeChange = (newPageSize) => {
  pageSize.value = newPageSize;
  pageNum.value = 1;
  getSingerMusicList(pageNum.value,pageSize.value);
};

// 获取歌手信息
const getSingerInfo = async () => {
  try {
    const response = await getOmdSingerInfoByOmdUserIdService(routeUserId);
    singerInfo.value = response.data;
  }catch(error){
    console.log(error);
  }
}

// 获取歌手歌曲列表
const getSingerMusicList = async (pageNum,pageSize) => {
  try {
    const response = await getMusicInfoBySingerIdService(pageNum,pageSize,singerInfo?.value.omdSingerId);
    singerMusicList.value = response.data.items;
    totalPages.value = response.data.total;
  }catch(error){
    console.log(error);
  }
}

// 监听事件
const handleAddClick = async (song) => {
  await openAddToPlaylistDialog(song, [song.omdMusicInfoId]);
}

// 获取好友状态
const getFriendStatus = async () => {
  try {
    const response = await getFriendStatusService(routeUserId);
    if(response.data !== null){
      omdFriendStatus.value = response.data.omdFriendStatus;
    }
  }catch (error) {
    ElMessage.error('获取好友状态失败');
  }
}

// 添加好友
const handleInsertFriend = async () => {
  try {
    // 调用添加好友的服务
    await insertFriendService(routeUserId);
    ElMessage.success('添加好友成功');
  } catch (error) {
    ElMessage.error('添加好友失败');
    console.error(error);
  }
}

// 拉黑
const blackFriend = async () => {
  ElMessageBox.confirm(
      '确定要拉黑该用户吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    // 调用拉黑接口
    try {
      await blackFriendService(routeUserId);
      ElMessage.success('拉黑成功');
      // 刷新好友状态
      await getFriendStatus();
    }catch (error) {
      ElMessage.error(error);
    }
  })
};

// 取消拉黑
const cancelBlackFriend = async () => {
  ElMessageBox.confirm(
      '确定要取消拉黑该用户吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ) .then(async () => {
    // 调用拉黑接口
    try {
      await deleteBlackUserService(routeUserId);
      ElMessage.success('取消拉黑成功');
    }catch (error) {
      ElMessage.error(error);
    }
  })
}

// 获取举报状态
const getReportStatus = async () => {
  try {
    const response = await getUserReportStatusService(routeUserId);
    omdUserReportStatus.value = response.data;
  }catch (error) {
    ElMessage.error('获取举报状态失败');
  }
}

// 打开举报弹窗
const openReportPopup = () => {
  // 打开举报弹窗
  ElMessageBox.confirm(
      '确定要举报该用户吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    // 打开举报弹窗
    reportPopupVisible.value = true;
  })
};

// 举报成功回调
const handleReportSuccess = async () => {
  await getFriendStatus();
  // 获取举报状态
  await getReportStatus();
};

// 钩子函数
onMounted(async () => {

  // 获取用户信息
  await getUserInfoByOmdUserIdService(routeUserId).then(response => {
    omdUserData.value = response.data;
  }).catch(error => {
    console.error('获取用户信息失败:', error);
  });

  // 获取歌手信息
  await getSingerInfo();

  // 获取歌手歌曲列表
  await getSingerMusicList(pageNum.value,pageSize.value,singerInfo?.value.omdSingerId);

  // 获取好友状态
  await getFriendStatus();

  // 获取举报状态
  await getReportStatus();

});

</script>

<template>
  <el-card>

    <!-- 卡片头部 -->
    <template #header>

      <!-- 卡片标题 -->
      <div class="card-header">
        <h1>基本资料</h1>
      </div>

    </template>

    <!-- 卡片内容 -->
    <el-container>

      <div class="user-info-container">
        <!-- 按钮 -->
        <div class="user-info-button">
          <el-button v-if="omdFriendStatus === null && !omdUserReportStatus" type="primary" @click="handleInsertFriend">添加为好友</el-button>
          <el-button v-else-if="omdFriendStatus === 0  && !omdUserReportStatus" type="warning">已经申请添加为好友</el-button>
          <el-button v-else-if="omdFriendStatus === 1  && !omdUserReportStatus" type="success">已添加</el-button>
          <el-button v-else-if="omdFriendStatus === 3 || omdUserReportStatus" type="danger">已拉黑</el-button>
          <el-button v-else-if="omdFriendStatus === 3 || omdUserReportStatus" @click="cancelBlackFriend" type="primary">取消拉黑</el-button>
          <el-button type="warning" v-if="omdFriendStatus !== 3  && !omdUserReportStatus" @click="blackFriend">拉黑用户</el-button>
          <el-button type="danger" v-if="!omdUserReportStatus" @click="openReportPopup">举报用户</el-button>
          <el-button type="danger" v-else>已举报过该用户</el-button>
        </div>
        <!-- 表格 -->
        <el-descriptions border :label-width="150">
          <el-descriptions-item :rowspan="2" :width="140" label="头像" align="center">
            <el-image style="width: 100px; height: 100px" :src="omdUserData?.omdUserAvatar || defaultAvatar"  prop="omdUserAvator"/>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="用户名" prop="omdUserName">{{ omdUserData?.omdUserName }}</el-descriptions-item>
          <el-descriptions-item align="center" label="身份" prop="omdRoleCode">
            <el-tag type="success" size="small">歌手</el-tag>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="昵称" prop="omdUserNickname">{{ omdUserData?.omdUserNickname }}</el-descriptions-item>
          <el-descriptions-item align="center" label="性别" prop="omdUserGender">{{ gender(omdUserData?.omdUserGender) }}</el-descriptions-item>
          <el-descriptions-item align="center" label="地区" prop="omdUserRegion">{{ omdUserData?.omdUserRegion }}</el-descriptions-item>
          <el-descriptions-item align="center" label="生日" prop="omdUserBirthday">{{ omdUserData?.omdUserBirthday }}</el-descriptions-item>

          <el-descriptions-item align="center" label="艺名" prop="omdSingerName">{{ singerInfo?.omdSingerName }}</el-descriptions-item>
          <el-descriptions-item align="center" label="流派" prop="omdSingerGenre">{{ singerInfo?.omdSingerGenre }}</el-descriptions-item>
          <el-descriptions-item align="center" label="所属公司" prop="omdSingerLabel">{{ singerInfo?.omdSingerLabel }}</el-descriptions-item>
          <el-descriptions-item align="center" label="代表作" prop="omdSingerRepresentativeSing">{{ singerInfo?.omdSingerRepresentativeSing }}</el-descriptions-item>
          <el-descriptions-item align="center" label="认证时间" prop="omdSingerVerifyTime">{{ singerInfo?.omdSingerVerifyTime }}</el-descriptions-item>
          <el-descriptions-item align="center" label="个人简介" prop="omdSingerIntroduction">{{ singerInfo?.omdSingerIntroduction }}</el-descriptions-item>
        </el-descriptions>
      </div>

    </el-container>


    <!-- 该用户的公开歌单 -->
    <div class="playlist-container">
      <PlaylistList
          :omdUserId="routeUserId"/>
    </div>

    <div class="music-list-container">
      <h1>该歌手的歌曲</h1>
      <MusicRankItem
          v-for="item in singerMusicList"
          :key="item.omdMusicInfoId"
          :item="item"
          @add-song="handleAddClick"
      />
      <!-- 空状态 -->
      <p v-if="singerMusicList.length === 0" class="empty-tip">
        暂无歌曲
      </p>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total"
        />
      </div>
    </div>

    <!-- 添加歌曲到歌单弹窗 -->
    <PlaylistAddToDialog
        :show-dialog="showPlaylistAddToDialog"
        :playlistList="playlistList"
        :loading="loadingPlaylistList"
        :hasMore="hasMore"
        @loadMore="loadMorePlaylists"
        @update:show-dialog="showPlaylistAddToDialog = $event"
        @add-to-playlist="addSongToPlaylistHandler"
    />

    <!-- 引入举报弹窗组件 -->
    <ReportUserDialog
        :reported-user-id="routeUserId"
        :visible="reportPopupVisible"
        @update:visible="reportPopupVisible = $event"
        @report-success="handleReportSuccess"
    />

    <template #footer>

    </template>

  </el-card>

</template>

<style scoped lang="scss">
.el-card {

  .card-header {
    text-align: center;
  }

  /* 卡片主体 */
  .el-container {

    .el-descriptions{
      padding: 20px 250px;
      margin-left: 150px;
    }

    .el-descriptions-item {
      padding: 20px 0;
    }


    /* 按钮 */
    .user-info-button {
      display: flex;
      /* 子元素靠右对齐 */
      justify-content: flex-end;
    }


  }

  .playlist-container{
    margin-top: 50px;
  }

  .music-list-container {
    padding: 20px;
    border-radius: 8px;
    margin: 0 auto ;
    width: 700px;
  }


  /* 编辑按钮 */
  .button {
    margin-left: 10px;
  }

  /* 头像上传 */
  .cover-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 150px;
    height: 150px;

    &:hover {
      border-color: #409eff;
    }
  }

  .cover-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 150px;
    height: 150px;
    line-height: 150px;
    text-align: center;
  }

  .cover-image {
    display: block;
    object-fit: cover;
  }

}
</style>