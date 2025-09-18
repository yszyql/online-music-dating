<script setup>
import {ref, onMounted, onUnmounted} from 'vue';
import {ElButton} from 'element-plus';

// 导入默认播放列表封面
import defaultCover from '@/assets/images/defaultCover.png';

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js';
import { useMusicStore } from '@/stores/music.js';

// 导入服务器接口
import {getOmdUserPlaylistPublicService, getPlaylistListByUserIdService} from '@/api/user.js';

// 导入自定义组件
import PlaylistModal from "@/components/playlist/PlaylistModal.vue";

// 导入组合式函数
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";
import PlaylistCreateDialog from "@/components/playlist/PlaylistCreateDialog.vue";
import {useMusicHandle} from "@/composable/useMusicHandle.js";

// 状态管理
const authStore = useAuthStore();
const musicStore = useMusicStore();

// 定义 props
const props = defineProps({
  omdUserId:{
    type:String,
    default:null
  }
})

// 响应式数据
const pageNum = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);         // 总页数
const playlistList = ref([]); // 播放列表数据
const playlistSelect = ref([]); // 播放列表选中项
const playlistDetailVisible = ref(false);

// 使用组合式函数
const {
  showCreatePlaylistDialog,
  showEditPlaylistDialog,
  formData,
  formRules,
  formLoading,
  openCreateDialog,
  openEditDialog,
  submitForm,
  deletePlaylist
} = usePlaylistManager();

const {
  onLikeStatusChange
} = useMusicHandle();


// 监听页码变化
const handlePageChange = async (pageNum) => {
  if (props.omdUserId === null){
    await  fetchUserPlaylists(pageNum, pageSize.value);
  }else {
    await getOmdUserPlaylistPublicService(pageNum,pageSize.value,props.omdUserId);
  }

};

// 监听每页数量变化
const handleSizeChange = async (pageSize) => {
  if (props.omdUserId === null){
    await fetchUserPlaylists(pageNum.value, pageSize);
  }else {
    await getOmdUserPlaylistPublicService(pageNum.value,pageSize,props.omdUserId);
  }

};

// 获取播放列表
const fetchUserPlaylists = async (pageNum,pageSize) => {
  if(authStore.isGuest){
    return;
  }

  try {
    const result = await getPlaylistListByUserIdService(pageNum,pageSize);
    playlistList.value = result.data.items || [];
    totalPages.value = result.data.total || 0;
  } catch (error) {
    console.error('获取播放列表失败:', error);
  }
};

// 加载播放列表里的音乐信息
const loadPlaylist = async (playlist) => {
  try {
    playlistSelect.value = playlist;
    musicStore.playlist.value = playlist;
    playlistDetailVisible.value = true;
  }catch (error) {
    console.error('获取歌单里的音乐列表失败：', error);
  }
};

// 新建播放列表
const handleCreatePlaylist = async () => {
  try {
    // 等待创建操作完成
    await submitForm(false);
    await fetchUserPlaylists(pageNum.value, pageSize.value);
  } catch (error) {
    console.error('创建歌单失败:', error);
  }
};

// 编辑歌单
const handleEditPlaylist = async (omdPlaylistId) => {
  try {
    // 等待编辑操作完成
    await submitForm(true, omdPlaylistId);
    // 编辑成功后重新获取列表
    await fetchUserPlaylists(pageNum.value, pageSize.value);
  } catch (error) {
    console.error('编辑歌单失败:', error);
  }
}

// 新增：处理点赞状态变化
const handleLikeStatusChange = async (musicId, isLiked) => {
  // 如果是点赞操作（创建新歌单的情况）
  if (isLiked) {
    // 重新获取歌单列表
    await fetchUserPlaylists(pageNum.value, pageSize.value);
  }
};

// 删除播放列表回调函数
const refreshPlaylists = async () => {
  await fetchUserPlaylists(pageNum.value, pageSize.value);
};

// 刷新歌单
const refreshPlaylist = async () => {
  if (props.omdUserId === null) {
    // 如果是当前用户，直接获取用户播放列表
    await fetchUserPlaylists(pageNum.value,pageSize.value);
  }else {
    // 如果不是当前用户，获取公共播放列表
    try {
      const result = await getOmdUserPlaylistPublicService(pageNum.value,pageSize.value,props.omdUserId);
      playlistList.value = result.data.items || [];
      totalPages.value = result.data.total || 0;
    } catch (error) {
      console.error('获取公共播放列表失败:', error);
    }
  }
}

// 组件挂载时获取用户播放列表
onMounted( async () => {
  if(authStore.isGuest){
    return;
  }
  await refreshPlaylist();
  // 新增：设置点赞状态变化回调
  onLikeStatusChange.value = handleLikeStatusChange;
});

// 当组件卸载时，清除回调（避免内存泄漏）
onUnmounted(() => {
  onLikeStatusChange.value = null;
});
</script>

<template>

  <!-- 播放列表主容器 -->
  <div class="playlists-section">
    <!-- 背景装饰 -->
    <div class="bg-pattern"></div>

    <!-- 内容卡片 -->
    <div class="playlists-card">
      <!-- 顶部标题栏 -->
      <div class="playlist-header">
        <h2 v-if="props.omdUserId === authStore.userId">
          我的公开歌单
        </h2>
        <h2 v-else-if="props.omdUserId === null">
          我的播放列表
        </h2>
        <h2 v-else>
          公开歌单
        </h2>
        <el-button
            type="primary"
            icon="Plus"
            @click="openCreateDialog"
            class="playlist-btn"
            v-if="props.omdUserId === authStore.userId || props.omdUserId === null"
        >
          新建
        </el-button>
      </div>

      <!-- 播放列表内容区 -->
      <div class="playlist-container">
        <!-- 单个播放列表 -->
        <div
            v-for="(playlist, index) in playlistList"
            :key="playlist.omdPlaylistId"
            class="playlist-item"
        >

          <div class="playlist-click" @click="loadPlaylist(playlist)">

            <!-- 封面 -->
            <div class="playlist-cover">
              <img
                  :src="playlist.omdPlaylistCover || defaultCover"
                  :alt="playlist.omdPlaylistName"
              >
            </div>
            <!-- 信息 -->
            <div class="playlist-info">
              <h3>{{ playlist.omdPlaylistName }}</h3>
              <p>创建时间 · {{ playlist.omdPlaylistCreateTime }}</p>
              <el-tag
                  type="primary"
                  size="small"
                  class="playlist-tag"
                  :effect="playlist.omdPlaylistPublic ? 'dark' : 'plain'"
              >
                <span v-if="playlist.omdPlaylistPublic">公开</span>
                <span v-else>私有</span>
              </el-tag>
            </div>

          </div>

          <div class="playlist-edit">
            <!-- 编辑按钮 -->
            <el-button
                type="text"
                icon="Edit"
                @click="openEditDialog(playlist); playlistSelect.value = playlist;"
                class="edit-button"
                v-if="playlist.omdPlaylistName !== '我喜欢的音乐' && (props.omdUserId === null || props.omdUserId === authStore.userId)"
            />
            <!-- 删除按钮 -->
            <el-button
                type="text"
                icon="Delete"
                @click="deletePlaylist(playlist.omdPlaylistName,refreshPlaylists)"
                class="delete-button"
                v-if="playlist.omdPlaylistName !== '我喜欢的音乐' && (props.omdUserId === null || props.omdUserId === authStore.userId)"
            />

          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="playlistList.length === 0" class="playlist-empty-state">
          <p>还没有创建任何播放列表</p>
          <p v-if="props.omdUserId === null || props.omdUserId === authStore.userId">创建播放列表来整理你喜欢的音乐，让聆听更有仪式感</p>
          <el-button
              type="primary"
              icon="Plus"
              @click="openCreateDialog"
              v-if="props.omdUserId === null || props.omdUserId === authStore.userId"
          >
            创建播放列表
          </el-button>
        </div>


        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 15, 20 ,25]"
            :total="totalPages"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="prev, pager, next, jumper, ->, total, sizes"
        />

      </div>
    </div>
  </div>

  <!-- 播放列表详情弹窗 -->
  <PlaylistModal
      :playlist="playlistSelect"
      :visible="playlistDetailVisible"
      @close="playlistDetailVisible = false;refreshPlaylist()"
  />

  <!-- 创建歌单弹窗 -->
  <PlaylistCreateDialog
      :show-dialog="showCreatePlaylistDialog"
      :form-data="formData"
      :form-rules="formRules"
      :form-loading="formLoading"
      @update:show-dialog="showCreatePlaylistDialog = $event"
      @update:formData="formData = $event"
      @createPlaylist="handleCreatePlaylist"
  />

  <!-- 编辑歌单弹窗 -->
  <PlaylistCreateDialog
      :show-dialog="showEditPlaylistDialog"
      :form-data="formData"
      :form-rules="formRules"
      :form-loading="formLoading"
      :playlist="playlistSelect"
      :isEdit="true"
      @update:show-dialog="showEditPlaylistDialog = $event"
      @update:formData="formData = $event"
      @updatePlaylist="handleEditPlaylist(playlistSelect.value.omdPlaylistId)"
  />

</template>


<style scoped>
/* 主容器 */
.playlists-section {
  position: relative;
  padding: 2rem 1rem;
  margin-bottom: 10rem;
}

/* 内容卡片 */
.playlists-card {
  position: relative;
  z-index: 1;
  max-width: 70rem;
  margin: 0 auto;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-radius: 1rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 滚动条美化 */
.playlists-card::-webkit-scrollbar {
  width: 6px;
}

.playlists-card::-webkit-scrollbar-track {
  background: #f8fafc;
}

.playlists-card::-webkit-scrollbar-thumb {
  background-color: #0062ef;
  border-radius: 3px;
}

/* 标题栏 */
.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
}

.playlist-btn {
  display: flex;
  justify-content: flex-end;
}

.playlist-header h2 {
  display: flex;
  align-items: center;
  font-size: 1.5rem;
  font-weight: bold;
}

.playlist-header h2 .el-icon {
  color: #3b82f6;
  margin-right: 0.5rem;
}

.playlist-header .el-button {
  padding: 0.5rem 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.playlist-header .el-button:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

/* 播放列表容器 */
.playlist-container {
  max-height: 600px;
  overflow-y: auto;
  padding: 1.5rem;
}

/* 单个播放列表项 */
.playlist-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  margin-bottom: 0.75rem;
  border-radius: 0.75rem;
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.playlist-item:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
}

.playlist-item:hover .playlist-cover img {
  transform: scale(1.1);
}

.playlist-item:hover .playlist-info h3 {
  color: #3b82f6;
}

.playlist-click{
  display: flex;
}

/* 封面 */
.playlist-cover {
  width: 5rem;
  height: 5rem;
  border-radius: 0.75rem;
  overflow: hidden;
  margin-right: 1rem;
  flex-shrink: 0;
}

.playlist-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

/* 信息 */
.playlist-info {
  flex: 1;
  min-width: 0;
}

.playlist-info h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0.25rem;
  transition: color 0.3s ease;
}

.playlist-info p {
  font-size: 0.875rem;
  color: #6b7280;
}

.playlist-edit{
  display: flex;
  justify-content: flex-end; /* 右对齐 */
  align-items: center;
  position: absolute; /* 绝对定位 */
  right: 1rem; /* 右侧距离 */
  top: 50%; /* 垂直居中 */
  transform: translateY(-50%); /* 垂直居中调整 */
  transition: opacity 0.3s ease; /* 过渡效果 */
}

/* 编辑按钮样式 - 蓝色 */
.edit-button {
  color: #3b82f6; /* 蓝色文本 */
  transition: color 0.3s ease;
  font-size: 1.25rem;
}

/* 删除按钮样式 - 红色 */
.delete-button {
  color: #ef4444; /* 红色文本 */
  transition: color 0.3s ease;
  font-size: 1.25rem;
}

/* 空状态 */
.playlist-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
}

.playlist-empty-state .el-icon {
  font-size: 3rem;
  color: #93c5fd;
  margin-bottom: 1rem;
}

.playlist-empty-state p:first-of-type {
  font-size: 1.25rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.playlist-empty-state p:nth-of-type(2) {
  font-size: 0.875rem;
  color: #9ca3af;
  max-width: 300px;
  margin: 0 auto 1.5rem;
}

.playlist-empty-state .el-button {
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.playlist-empty-state .el-button:hover {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 640px) {
  .playlists-card {
    margin: 0 1rem;
    border-radius: 1rem;
  }

  .playlist-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .playlist-cover {
    margin-bottom: 0.75rem;
  }
}
</style>