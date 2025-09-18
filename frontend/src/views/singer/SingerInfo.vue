<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { ElMessage, ElDivider, ElTooltip } from "element-plus";
// 导入默认头像
import defaultAvatar from '@/assets/images/defaultAvatar.png'

// 导入自定义组件
import PlaylistList from '@/components/playlist/PlaylistList.vue'
import PlaylistAddToDialog from "@/components/playlist/PlaylistAddToDialog.vue";

// 导入状态管理
import { useAuthStore } from '@/stores/auth';

// 导入服务
import {
  getUserInfoService,
  userInfoUpdateService,
  getMusicInfoBySingerIdService,
} from "@/api/user.js";
import {
  getOmdSingerInfoService,
  updateOmdSingerService,
  getMusicInfoListBySingerIdService,
} from "@/api/singer.js";

// 导入自定义组件
import MusicRankItem from "@/components/home/MusicRankItem.vue";

// 组合式函数
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";

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

// 状态管理
const authStore = useAuthStore();

// 响应式变量
const singerInfo = ref({}); // 歌手信息
const dialogVisible = ref(false); // 编辑对话框是否可见
const singerSongs = ref([]); // 歌手已通过的歌曲列表
const totalPages = ref(0); // 总页数
const singerMusicList = ref([]);

// 分页加载相关变量（新增）
const pageNum = ref(1); // 当前页码
const pageSize = ref(10); // 每页数量
const hasMoreData = ref(true); // 是否有更多数据
const isLoadingMore = ref(false); // 加载状态

// 用户表单数据
const editUserForm = reactive({
  omdUserAvatar: '',
  omdUserNickname: '',
  omdUserBirthday: '',
  omdUserEmail: '',
  omdUserGender: 0,
  omdUserRegion: ''
});

// 歌手表单数据
const editSingerForm = reactive({
  omdSingerId: singerInfo.value.omdSingerId,
  omdSingerName: '',
  omdSingerGenre: '',
  omdSingerLabel: '',
  omdSingerRepresentativeSing: [],
  omdSingerIntroduction: ''
});

// 地区选项
const regions = [
  { value: '中国', label: '中国' },
  { value: '国外', label: '国外' }
];

// 表单ref
const editFormRef = ref(null);

// 表单验证规则
const editFormRules = {
  omdUserNickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 16, message: '昵称长度必须在1到16个字符之间', trigger: 'blur' }
  ],
  omdUserEmail: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  omdSingerGenre: [
    { max: 50, message: '流派长度不能超过50个字符', trigger: 'blur' }
  ],
  omdSingerLabel: [
    { max: 100, message: '公司名称长度不能超过100个字符', trigger: 'blur' }
  ],
  omdSingerIntroduction: [
    { max: 500, message: '个人简介长度不能超过500个字符', trigger: 'blur' }
  ]
};

// 格式化性别显示
const gender = (genderData) => {
  const genderMap = {
    0: '男',
    1: '女',
    2: '保密'
  };
  return genderMap[genderData] || '未知';
};

// 格式化代表作显示（添加书名号）
const formatRepresentative = (representative) => {
  if (!representative) return '暂无';

  // 如果是数组格式
  if (Array.isArray(representative)) {
    return representative.map(item => `《${item}》`).join('、');
  }

  // 如果是字符串格式
  if (typeof representative === 'string') {
    // 检查是否已经有书名号
    if (representative.includes('《') && representative.includes('》')) {
      return representative;
    }
    // 按逗号分割多个作品
    return representative.split(',').map(item => `《${item}》`).join('、');
  }

  return '暂无';
};

// 处理编辑按钮点击（打开弹窗时加载歌曲）
const handleEditClick = async () => {
  // 初始化表单数据前先加载歌曲
  await getSingerPassedSongs();
  initFormData();
  dialogVisible.value = true;
};

// 初始化表单数据
const initFormData = () => {
  // 初始化用户表单
  const userInfo = authStore.userInfo;
  editUserForm.omdUserAvatar = userInfo.omdUserAvatar || '';
  editUserForm.omdUserNickname = userInfo.omdUserNickname || '';
  editUserForm.omdUserBirthday = userInfo.omdUserBirthday || '';
  editUserForm.omdUserEmail = userInfo.omdUserEmail || '';
  editUserForm.omdUserGender = userInfo.omdUserGender !== undefined ? userInfo.omdUserGender : 0;
  editUserForm.omdUserRegion = userInfo.omdUserRegion || '';

  // 初始化歌手表单
  editSingerForm.omdSingerId = singerInfo.value.omdSingerId || '';
  editSingerForm.omdSingerName = singerInfo.value.omdSingerName || '';
  editSingerForm.omdSingerGenre = singerInfo.value.omdSingerGenre || '';
  editSingerForm.omdSingerLabel = singerInfo.value.omdSingerLabel || '';

  // 处理代表作（去除书名号）
  if (singerInfo.value.omdSingerRepresentativeSing) {
    const reps = typeof singerInfo.value.omdSingerRepresentativeSing === 'string'
        ? singerInfo.value.omdSingerRepresentativeSing
        : '';
    // 去除书名号并分割
    editSingerForm.omdSingerRepresentativeSing = reps
        .replace(/《|》/g, '')
        .split('、')
        .filter(Boolean);
  } else {
    editSingerForm.omdSingerRepresentativeSing = '';
  }

  editSingerForm.omdSingerIntroduction = singerInfo.value.omdSingerIntroduction || '';
};

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

// 封面图片上传成功处理
const handleCoverSuccess = (response) => {
  if (response.data) {
    editUserForm.omdUserAvatar = response.data;
    ElMessage.success('上传成功');
  }
};

// 上传前校验
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }

  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB');
    return false;
  }

  return true;
};

// 处理提交
const handleSubmit = async () => {
  try {
    // 表单验证
    await editFormRef.value.validate();

    // 处理代表作格式（添加书名号）
    if (editSingerForm.omdSingerRepresentativeSing && editSingerForm.omdSingerRepresentativeSing.length > 0) {
      editSingerForm.omdSingerRepresentativeSing = editSingerForm.omdSingerRepresentativeSing
          .map(song => `《${song}》`)
          .join('、');
    }

    // 先更新用户信息
    const userResult = await userInfoUpdateService(editUserForm);
    if (userResult.code !== 0) {
      ElMessage.error('用户信息更新失败');
      return;
    }

    // 再更新歌手信息
    const singerResult = await updateOmdSingerService(editSingerForm);
    if (singerResult.code !== 0) {
      ElMessage.error('歌手信息更新失败');
      return;
    }

    // 更新成功
    ElMessage.success('信息更新成功');

    // 刷新数据
    await refreshData();

    // 关闭弹窗
    dialogVisible.value = false;
  } catch (error) {
    console.error('更新失败:', error);
    ElMessage.error('更新失败，请稍后重试');
  }
};

// 刷新数据
const refreshData = async () => {
  // 更新用户信息
  const userInfo = await getUserInfoService();
  authStore.setUserInfo(userInfo.data);
  localStorage.setItem('userInfo', JSON.stringify(authStore.userInfo));

  // 更新歌手信息
  await getSingerInfo();

  // 重新加载歌曲列表
  await getSingerPassedSongs();
};

// 获取歌手信息
const getSingerInfo = async () => {
  try {
    const response = await getOmdSingerInfoService();
    singerInfo.value = response.data || {};
  } catch (error) {
    console.error('获取歌手信息失败:', error);
  }
};

// 获取歌手已通过的歌曲（支持分页和加载更多）
const getSingerPassedSongs = async (loadMore = false) => {
  // 如果没有更多数据或正在加载，直接返回
  if (!hasMoreData.value || isLoadingMore.value) return;

  // 加载更多时页码+1，否则重置页码
  if (loadMore) {
    pageNum.value++;
  } else {
    pageNum.value = 1;
    singerSongs.value = []; // 清空现有数据
  }

  isLoadingMore.value = true;
  try {
    if (!singerInfo.value?.omdSingerId) {
      hasMoreData.value = false;
      return;
    }

    // 调用分页接口（传递页码、每页数量、状态）
    const response = await getMusicInfoListBySingerIdService(
        pageNum.value,
        pageSize.value,
        1 // 只查询已通过的歌曲
    );

    const newSongs = response.data?.items || [];

    // 加载更多时追加数据，否则直接赋值
    if (loadMore) {
      singerSongs.value = [...singerSongs.value, ...newSongs];
    } else {
      singerSongs.value = newSongs;
    }

    // 判断是否还有更多数据
    const total = response.data?.total || 0;
    hasMoreData.value = singerSongs.value.length < total;
  } catch (error) {
    console.error('获取歌手歌曲列表失败:', error);
    // 加载失败时回退页码
    if (loadMore) {
      pageNum.value--;
    }
  } finally {
    isLoadingMore.value = false;
  }
};

// 监听编辑弹窗的滚动事件（实现下拉加载更多）
const handleDialogScroll = async (e) => {
  // 只在弹窗打开且有更多数据时监听
  if (!dialogVisible.value || !hasMoreData.value || isLoadingMore.value) return;

  const dialogBody = e.target;
  // 计算滚动到底部的距离（小于200px时加载更多）
  if (dialogBody.scrollHeight - dialogBody.scrollTop - dialogBody.clientHeight < 200) {
    await getSingerPassedSongs(true);
  }
};

// 初始化
onMounted(async () => {
  await getSingerInfo();
  // 初始加载第一页歌曲
  await getSingerPassedSongs();
  // 加载歌手歌曲
  await getSingerMusicList(pageNum.value,pageSize.value);
});
</script>

<template>
  <el-card>
    <!-- 卡片头部 -->
    <template #header>
      <div class="card-header">
        <h1>基本资料</h1>
      </div>
    </template>

    <!-- 卡片内容 -->
    <el-container>
      <!-- 登录用户的基本信息 -->
      <div class="user-info-container">
        <!-- 编辑按钮 -->
        <div class="user-info-button">
          <el-button type="primary" @click="handleEditClick">编辑</el-button>
        </div>
        <!-- 表格 -->
        <el-descriptions border :label-width="150">
          <el-descriptions-item :rowspan="2" :width="140" label="头像" align="center">
            <el-image
                style="width: 100px; height: 100px"
                :src="authStore.userInfo.omdUserAvatar || defaultAvatar"
            />
          </el-descriptions-item>
          <el-descriptions-item align="center" label="用户名" prop="omdUserName">{{ authStore.userInfo.omdUserName }}</el-descriptions-item>
          <el-descriptions-item align="center" label="身份" prop="omdRoleCode">
            <el-tag type="success" size="small">歌手</el-tag>
          </el-descriptions-item>
          <el-descriptions-item align="center" label="手机号" prop="omdUserPhone">{{ authStore.userInfo.omdUserPhone }}</el-descriptions-item>
          <el-descriptions-item align="center" label="邮箱" prop="omdUserEmail">{{ authStore.userInfo.omdUserEmail }}</el-descriptions-item>

          <el-descriptions-item align="center" label="昵称" prop="omdUserNickname">{{ authStore.userInfo.omdUserNickname }}</el-descriptions-item>
          <el-descriptions-item align="center" label="性别" prop="omdUserGender">{{ gender(authStore.userInfo.omdUserGender) }}</el-descriptions-item>
          <el-descriptions-item align="center" label="地区" prop="omdUserRegion">{{ authStore.userInfo.omdUserRegion }}</el-descriptions-item>
          <el-descriptions-item align="center" label="生日" prop="omdUserBirthday">{{ authStore.userInfo.omdUserBirthday }}</el-descriptions-item>
          <el-descriptions-item align="center" label="创建时间" prop="omdUserCreateTime">{{ authStore.userInfo.omdUserCreateTime }}</el-descriptions-item>

          <el-descriptions-item align="center" label="艺名" prop="omdSingerName">{{ singerInfo.omdSingerName }}</el-descriptions-item>
          <el-descriptions-item align="center" label="流派" prop="omdSingerGenre">{{ singerInfo.omdSingerGenre }}</el-descriptions-item>
          <el-descriptions-item align="center" label="所属公司" prop="omdSingerLabel">{{ singerInfo.omdSingerLabel }}</el-descriptions-item>
          <el-descriptions-item align="center" label="代表作" prop="omdSingerRepresentativeSing">
            {{ formatRepresentative(singerInfo.omdSingerRepresentativeSing) }}
          </el-descriptions-item>
          <el-descriptions-item align="center" label="认证时间" prop="omdSingerVerifyTime">{{ singerInfo.omdSingerVerifyTime }}</el-descriptions-item>
          <el-descriptions-item align="center" label="个人简介" prop="omdSingerIntroduction">{{ singerInfo.omdSingerIntroduction }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-container>

    <!-- 歌曲列表 -->
    <div>
      <PlaylistList
        :omdUserId="authStore.userInfo.omdUserId"
        />
    </div>

    <div class="music-list-container">
      <h1>该歌手的歌曲</h1>
      <MusicRankItem
          v-for="item in singerSongs"
          :key="item.omdMusicInfoId"
          :item="item"
          @add-song="handleAddClick"
      />
      <!-- 空状态 -->
      <p v-if="singerSongs.length === 0" class="empty-tip">
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

    <!-- 编辑弹窗 -->
    <el-dialog
        title="编辑资料"
        v-model="dialogVisible"
        width="800px"
        :before-close="() => dialogVisible = false"
        @scroll="handleDialogScroll"
    >
    <el-form ref="editFormRef" size="large" label-width="120px" autocomplete="off" :rules="editFormRules">
      <!-- 分割线：用户信息 -->
      <el-divider content="用户信息" />

      <el-form-item label="用户名：">
        <el-input prefix-icon="User" :value="authStore.userInfo.omdUserName" disabled></el-input>
      </el-form-item>

      <el-form-item label="手机号：">
        <el-input prefix-icon="Cellphone" :value="authStore.userInfo.omdUserPhone" disabled></el-input>
      </el-form-item>

      <el-form-item label="头像：">
        <el-upload
            class="cover-uploader"
            action="/api/user/avatarFileUpload"
            name="avatarFile"
            :headers="{ Authorization: `Bearer ${authStore.token}` }"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
        >
          <img
              v-if="editUserForm.omdUserAvatar"
              :src="editUserForm.omdUserAvatar"
              class="cover-image"
              style="width: 100px; height: 100px"
          />
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          <el-button type="primary" style="margin-left: 50px;">点击上传</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item label="昵称：" prop="omdUserNickname">
        <el-input
            prefix-icon="User"
            placeholder="请输入昵称"
            clearable
            v-model="editUserForm.omdUserNickname"
        ></el-input>
      </el-form-item>

      <el-form-item label="邮箱：" prop="omdUserEmail">
        <el-input
            prefix-icon="Message"
            placeholder="请输入邮箱"
            clearable
            v-model="editUserForm.omdUserEmail"
        ></el-input>
      </el-form-item>

      <el-form-item label="出生日期：">
        <el-date-picker
            type="date"
            placeholder="请选择您的生日"
            v-model="editUserForm.omdUserBirthday"
        />
      </el-form-item>

      <el-form-item label="性别：">
        <el-radio-group v-model="editUserForm.omdUserGender">
          <el-radio :value="0">
            <el-icon><Male/></el-icon>男
          </el-radio>
          <el-radio :value="1">
            <el-icon><Female/></el-icon>女
          </el-radio>
          <el-radio :value="2">
            <el-icon><UserFilled/></el-icon>保密
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="地区：">
        <el-select
            v-model="editUserForm.omdUserRegion"
            clearable
            placeholder="请选择您的地区"
            style="width: 240px"
        >
          <el-option
              v-for="item in regions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>

      <!-- 分割线：歌手信息 -->
      <el-divider content="歌手信息" />

      <el-form-item label="艺名：">
        <el-input
            prefix-icon="User"
            :value="editSingerForm.omdSingerName"
            disabled
        ></el-input>
      </el-form-item>

      <el-form-item label="流派：" prop="omdSingerGenre">
        <el-input
            placeholder="请输入音乐流派"
            clearable
            v-model="editSingerForm.omdSingerGenre"
        ></el-input>
      </el-form-item>

      <el-form-item label="所属公司：" prop="omdSingerLabel">
        <el-input
            placeholder="请输入所属公司"
            clearable
            v-model="editSingerForm.omdSingerLabel"
        ></el-input>
      </el-form-item>

      <el-form-item label="代表作：" prop="omdSingerRepresentativeSing">
        <el-select
            v-model="editSingerForm.omdSingerRepresentativeSing"
            placeholder="请选择代表作"
            multiple
            :disabled="singerSongs.length === 0"
        >
          <el-option
              v-for="song in singerSongs"
              :key="song.omdMusicInfoId"
              :label="song.omdMusicInfoName"
              :value="song.omdMusicInfoName"
          />
        </el-select>

        <!-- 加载状态提示 -->
        <div v-if="isLoadingMore.value" class="loading-tip">加载更多歌曲中...</div>

        <el-tooltip
            v-if="singerSongs.length === 0"
            effect="warning"
            content="暂无已通过的歌曲，无法选择代表作"
        >
          <div class="no-song-tip">⚠️ 需先上传并通过审核的歌曲才能设置代表作</div>
        </el-tooltip>
        <p class="tip-text">提示：选择后将自动用书名号包裹</p>
      </el-form-item>

      <el-form-item label="个人简介：" prop="omdSingerIntroduction">
        <el-input
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介"
            v-model="editSingerForm.omdSingerIntroduction"
        ></el-input>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交修改</el-button>
      </el-form-item>
    </el-form>
    </el-dialog>
  </el-card>
</template>

<style scoped lang="scss">
.el-card {
  margin: 20px;

  .card-header {
    text-align: center;
    margin-bottom: 20px;
  }

  .el-container {

    padding:0 130px;

    .user-info-container {
      .user-info-button {
        margin-bottom: 20px;
        text-align: right;
      }

      .el-descriptions {
        padding: 20px;
        width: 100%;

        .el-descriptions-item {
          padding: 15px 10px;

          &__label {
            font-weight: 500;
          }
        }
      }
    }
  }

  .music-list-container{
    margin: 20px 230px;

    .pagination-container{
      margin-top: 20px;
      text-align: center;
      margin-left: 600px;
    }
  }

}

// 编辑弹窗样式
.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: inline-block;
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
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-song-tip {
  color: #e6a23c;
  font-size: 12px;
  margin-top: 5px;
  display: inline-block;
}

.tip-text {
  color: #666;
  font-size: 12px;
  margin-top: 5px;
  margin-bottom: 0;
}

.loading-tip {
  color: #409eff;
  font-size: 12px;
  margin-top: 5px;
}

::v-deep .el-select__tags {
  flex-wrap: wrap;
}

::v-deep .el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
  padding-bottom: 20px;
}

::v-deep .el-dialog__footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}
</style>