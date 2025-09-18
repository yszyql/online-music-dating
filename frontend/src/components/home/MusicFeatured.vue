<script setup>
import {computed, onMounted, ref, watch} from 'vue';

// 导入状态管理
import { useAuthStore } from '@/stores/auth.js'

// 导入自定义组件
import MusicRankItem from '@/components/home/MusicRankItem.vue';

// 导入复合式函数
import {useMusicHandle} from "@/composable/useMusicHandle.js";

// 导入服务器请求
import {getTopMusicInfoListService} from '@/api/public.js';

// 导入组合式函数
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";

// 导入自定义组件
import PlaylistAddToDialog from '@/components/playlist/PlaylistAddToDialog.vue';

// 状态管理
const authStore = useAuthStore();

// 音乐处理组合函数
const {
  fetchMusicLikeInfo,
} = useMusicHandle();

const {
  playlistList,
  loadingPlaylistList,
  showPlaylistAddToDialog,
  openAddToPlaylistDialog,
  addSongToPlaylistHandler,
  loadMorePlaylists,
  hasMore
} = usePlaylistManager();

// 定义响应式数据存储排行榜
const totalRankData = ref([]);
const userRankData = ref([]);
const guestRankData = ref([]);

// 计算属性
const userId = computed(() => authStore.userId);

// 监听事件
const handleAddClick = async (song) => {
  await openAddToPlaylistDialog(song, [song.omdMusicInfoId]);
}

// 生命周期钩子
onMounted(async () => {
  try {
    // 1. 请求排行榜数据并赋值给响应式变量
    const { data } = await getTopMusicInfoListService(10);
    totalRankData.value = data.totalRankData || [];
    userRankData.value = data.userRankData || [];
    guestRankData.value = data.guestRankData || [];

    // 2. 提取所有音乐ID
    const allMusicIds = [
      ...totalRankData.value.map(item => item.omdMusicInfoId),
      ...userRankData.value.map(item => item.omdMusicInfoId),
      ...guestRankData.value.map(item => item.omdMusicInfoId)
    ].filter((id, index, arr) => arr.indexOf(id) === index); // 去重

    // 3. 如果有音乐ID
    if (allMusicIds.length > 0) {
      await fetchMusicLikeInfo(allMusicIds);
    }

  } catch (error) {
    console.error('获取数据失败:', error);
  }
});

// 监听用户登录状态变化，更新点赞信息
watch(userId, (newUserId, oldUserId) => {
  if (newUserId && newUserId !== oldUserId) {
    // 用户登录了，重新获取点赞信息
    const allMusicIds = [
      ...totalRankData.value.map(item => item.omdMusicInfoId),
      ...userRankData.value.map(item => item.omdMusicInfoId),
      ...guestRankData.value.map(item => item.omdMusicInfoId)
    ].filter((id, index, arr) => arr.indexOf(id) === index);

    if (allMusicIds.length > 0) {
      fetchMusicLikeInfo(allMusicIds);
    }
  }
});

</script>

<template>
  <el-row :gutter="20">
    <!-- 总播放榜 -->
    <el-col :span="8">
      <div class="chart-container">
        <h3>总播放排行榜</h3>
        <div class="scroll-container" style="height:400px">
          <ul style="list-style: none; padding:0;">
            <MusicRankItem
                v-for="item in totalRankData"
                :key="item.omdMusicInfoId"
                :item="item"
                @add-song="handleAddClick"
            />
            <!-- 空状态 -->
            <p v-if="totalRankData.length === 0" class="empty-tip">
              暂无总播放排行数据
            </p>
          </ul>
        </div>
      </div>
    </el-col>

    <!-- 用户播放榜 -->
    <el-col :span="8">
      <div class="chart-container">
        <h3>用户播放排行榜</h3>
        <div class="scroll-container" style="height:400px">
          <ul style="list-style: none; padding:0;">
            <MusicRankItem
                v-for="item in userRankData"
                :key="item.omdMusicInfoId"
                :item="item"
                @add-song="handleAddClick"
            />
            <p v-if="userRankData.length === 0" class="empty-tip">
              暂无用户播放排行数据
            </p>
          </ul>
        </div>
      </div>
    </el-col>

    <!-- 游客播放榜 -->
    <el-col :span="8">
      <div class="chart-container">
        <h3>游客播放排行榜</h3>
        <div class="scroll-container" style="height:400px">
          <ul style="list-style: none; padding:0;">
            <MusicRankItem
                v-for="item in guestRankData"
                :key="item.omdMusicInfoId"
                :item="item"
                @add-song="handleAddClick"
            />
            <p v-if="guestRankData.length === 0" class="empty-tip">
              暂无游客播放排行数据
            </p>
          </ul>
        </div>
      </div>
    </el-col>
  </el-row>


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

</template>

<style scoped>

/* 公共样式 */
.chart-container {
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  width: 350px;
  height: 500px;
}

/* 滚动容器样式 - 核心修改 */
.scroll-container {
  flex: 1; /* 占满剩余高度 */
  overflow-y: auto; /* 开启垂直滚动 */
  padding-right: 8px; /* 预留滚动条空间 */
  height: 0; /* 触发flex自适应高度 */
}

/* 美化滚动条 */
.scroll-container::-webkit-scrollbar {
  width: 6px; /* 滚动条宽度 */
}

.scroll-container::-webkit-scrollbar-track {
  background: #f5f5f5; /* 轨道背景 */
  border-radius: 3px;
}

.scroll-container::-webkit-scrollbar-thumb {
  background: #ddd; /* 滑块颜色 */
  border-radius: 3px;
}

.scroll-container::-webkit-scrollbar-thumb:hover {
  background: #bbb; /*  hover时滑块颜色 */
}

/* 标题样式 */
.chart-container h3 {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

/* 悬停动画 */
.chart-container:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.empty-tip {
  text-align: center;
  color: #999;
  margin: 0;
  padding-top: 10px;
}

</style>