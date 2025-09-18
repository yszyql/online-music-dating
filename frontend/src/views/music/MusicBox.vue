<script lang="ts" setup>
import {ref} from 'vue'

// 导入自定义组件
import PlaylistList from "@/components/playlist/PlaylistList.vue";
import PlaylistPublic from "@/components/playlist/PlaylistPublic.vue";

// 响应数据
const isCollapse = ref(false) // 折叠状态

// 记录当前激活的按钮
const activeButton = ref('music')

// 点击按钮时更新激活状态
const activateButton = (buttonName) => {
  activeButton.value = buttonName
  // 同时更新折叠状态
  if (buttonName === 'music') {
    isCollapse.value = false
  } else {
    isCollapse.value = true
  }
}

</script>


<template>

  <div class="container">
    <div class="sidebar">
      <el-button link @click="activateButton('music')" :class="{ 'active': activeButton === 'music' }">&nbsp;&nbsp;&nbsp;<h1>歌单分享</h1></el-button>
      <el-button link @click="activateButton('playlist')" :class="{ 'active': activeButton === 'playlist' }"><h1>播放列表</h1></el-button>
    </div>
    <div class="content" v-show="!isCollapse">
      <PlaylistPublic/>
    </div>
    <div class="content" v-show="isCollapse">
      <PlaylistList/>
    </div>
  </div>


</template>

<style scoped>
.container {
  display: flex;
  height: 100%; /* 视口高度，使容器占满整个视口 */

  .sidebar {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100%;
    gap: 16px;

    /* 定义激活状态的样式 */
    .active {
      color: var(--el-color-primary); /* Element Plus主色调 */
    }

  }

  .content {
    flex-grow: 1; /* 内容区域占据剩余空间 */
    overflow-y: auto; /* 如果内容超出，添加滚动条 */
    padding: 20px; /* 内边距 */
  }
}


</style>
