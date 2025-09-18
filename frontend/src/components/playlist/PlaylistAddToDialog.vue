<script setup>
import { computed } from 'vue';

// 导入默认播放列表封面
import defaultCover from '@/assets/images/defaultCover.png';

// 导入自定义组件
import PlaylistCreateDialog from '@/components/playlist/PlaylistCreateDialog.vue';

// 导入组合式函数
import {usePlaylistManager} from "@/composable/usePlaylistManager.js";

// 定义组件 props
const props = defineProps({
  showDialog: {
    type: Boolean,
    default: false
  },
  playlistList:  {
    type: Object,
    default: () => ([])
  },
  loading: {
    type: Boolean,
    default: false
  },
  hasMore: {
    type: Boolean,
    default: false
  }
});
// 定义组件 emits
const emit = defineEmits(
['add-to-playlist',
            'update:showDialog',
            'load-more']
        );

// 添加到歌单处理
const addToPlaylist = (omdPlaylistName) => {
  emit('add-to-playlist', omdPlaylistName);
  emit('update:showDialog', false);
};

// 使用组合式函数
const {
  showCreatePlaylistDialog,
  formData,
  formRules,
  formLoading,
  openCreateDialog,
  submitForm
} = usePlaylistManager();

// 加载更多处理
const loadMore = () => {
  emit('load-more');
};

const showDialog = computed({
  get: () => props.showDialog,
  set: (value) => emit('update:showDialog', value)
});

// 处理创建歌单提交
const handleCreatePlaylist = () => {
  // 可以传递回调函数，在创建成功后执行特定操作
  submitForm();
};

</script>

<template>
  <el-dialog
      v-model="showDialog"
      title="添加到歌单"
      width="500px"
      class="add-to-playlist-dialog"
  >
    <div v-loading="loading">
      <!-- 空状态 -->
      <el-empty v-if="!loading && playlistList.length === 0" description="暂无歌单"/>

      <!-- 歌单列表 -->
      <div v-else class="playlist-list">

        <div class="insert-playlist">
          <el-button type="primary" @click="openCreateDialog">
            创建歌单
          </el-button>
        </div>

        <div
            v-for="playlist in playlistList"
            :key="playlist.omdPlaylistId"
            class="playlist-item"
            @click="addToPlaylist(playlist.omdPlaylistName)"
        >
          <el-avatar :src="playlist.omdPlaylistCover || defaultCover" shape="square" />
          <div class="playlist-info">
            <div class="playlist-name">{{ playlist.omdPlaylistName }}</div>
            <div class="playlist-stats">
              创建时间 · {{ playlist.omdPlaylistCreateTime }}
            </div>
          </div>
        </div>


        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore">
          <el-button
              :loading="loading"
              type="text"
              @click="loadMore"
          >
            {{ loading ? '加载中...' : '加载更多' }}
          </el-button>
        </div>

        <!-- 没有更多提示 -->
        <div v-else class="no-more">
          <span>没有更多歌单了</span>
        </div>

      </div>
    </div>
  </el-dialog>

  <!-- 创建歌单弹窗 -->
  <PlaylistCreateDialog
      :show-dialog="showCreatePlaylistDialog"
      :form-data="formData"
      :form-rules="formRules"
      :form-loading="formLoading"
      @update:show-dialog="showCreatePlaylistDialog = $event"
      @update:formData="formData = $event"
      @submit="handleCreatePlaylist"
  />

</template>

<style scoped>
.add-to-playlist-dialog {
  .playlist-list {
    font-weight: bold;
    max-height: 400px;
    overflow-y: auto;
    padding: 10px 0;

    .insert-playlist {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      margin-bottom: 10px;
    }
  }

  .playlist-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: background-color 0.3s;
    border-radius: 8px;

    &:hover {
      transition: border 0.1s;
      border: #3b82f6 dashed 1px;

      .playlist-name {
        color: #3b82f6;
      }
    }

    .el-avatar {
      width: 60px;
      height: 60px;
      margin-right: 15px;
    }

    .playlist-info {
      flex: 1;

      .playlist-name {
        font-size: 16px;
        font-weight: 900;
        margin-bottom: 5px;
      }
      .playlist-stats {
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .load-more,
  .no-more {
    text-align: center;
    padding: 10px 0;
  }

  .no-more {
    color: #909399;
    font-size: 13px;
  }
}
</style>