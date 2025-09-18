<script setup>
import { ref, onMounted } from 'vue'

// 导入轮播图
import home1 from '@/assets/images/homeSwiper/home1.jpg'
import home2 from '@/assets/images/homeSwiper/home2.jpg'
import home3 from '@/assets/images/homeSwiper/home3.jpg'
import home4 from '@/assets/images/homeSwiper/home4.jpg'
import home5 from '@/assets/images/homeSwiper/home5.jpeg'
import home6 from '@/assets/images/homeSwiper/home6.jpg'

// 导入状态管理
import { useAuthStore } from '@/stores/auth';

// 导入服务器接口
import {getMusicInfoByIdListService, getTopMusicService} from "@/api/public";

// 导入组件
import MusicEcharts from "@/components/home/MusicEcharts.vue";
import MusicFeatured from "@/components/home/MusicFeatured.vue";
import MusicSinger from "@/components/home/MusicSinger.vue";

// 状态管理
const authStore = useAuthStore();

// 轮播图
const banners = [
  { url: home1 },
  { url: home2 },
  { url: home3 },
  { url: home4 },
  { url: home5 },
  { url: home6 },
]

// 响应式数据
const totalData = ref({names: [], counts: []});
const userData = ref({names: [], counts: []});
const guestData = ref({names: [], counts: []});
const isDataLoading = ref(false);

// 获取图表数据
const getEchartsData = async () => {
  try {
    const {data} = await getTopMusicService(10);

    // 处理总播放榜数据
    const totalIds = data.total?.map((i) => i.omdMusicInfoId) || [];
    const totalNames = totalIds.length > 0 ? await getMusicInfoByIdListService(totalIds) : {data: []};
    totalData.value = {
      names: totalNames.data || [],
      counts: data.total?.map((i) => i.playCount) || []
    };

    // 处理用户播放榜数据
    const userIds = data.user?.map((i) => i.omdMusicInfoId) || [];
    const userNames = userIds.length > 0 ? await getMusicInfoByIdListService(userIds) : {data: []};
    userData.value = {
      names: userNames.data || [],
      counts: data.user?.map((i) => i.playCount) || []
    };

    // 处理游客播放榜数据
    const guestIds = data.guest?.map((i) => i.omdMusicInfoId) || [];
    const guestNames = guestIds.length > 0 ? await getMusicInfoByIdListService(guestIds) : {data: []};
    guestData.value = {
      names: guestNames.data || [],
      counts: data.guest?.map((i) => i.playCount) || []
    };

    isDataLoading.value = true;
  } catch (error) {
    console.error('获取数据失败：', error);
  }
}

onMounted(() => {
  // 获取数据
  getEchartsData();
})
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <HeaderMenu/>
      </el-header>

      <el-main>
        <!-- 轮播图 -->
        <div class="carousel-container">
          <el-carousel :interval="4000" type="card" height="400px" arrow="always">
            <el-carousel-item v-for="(item, index) in banners" :key="index">
              <div class="carousel-item-content">
                <img :src="item.url" alt="轮播图" class="carousel-image" />
                <div class="carousel-caption">
                  <p>{{ item.description || '' }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="container">
          <div class="content-section">
            <h2>总播放榜TOP10
              <el-icon>
                <Histogram/>
              </el-icon>
            </h2>
            <div class="content">
              <MusicEcharts
                  v-if="isDataLoading"
                  chartTitle="总播放榜"
                  :chartData="totalData"
                  chartType="bar"
                  seriesName="播放量"
                  yAxisName="播放次数"
              />
              <div v-else class="loading">
                <el-skeleton animated/>
              </div>
            </div>
          </div>

          <div class="content-section">
            <h2>用户播放TOP10趋势
              <el-icon>
                <TrendCharts/>
              </el-icon>
            </h2>
            <div class="content">
              <MusicEcharts
                  v-if="isDataLoading"
                  chartTitle="用户播放趋势"
                  :chartData="userData"
                  chartType="line"
                  seriesName="播放量"
                  yAxisName="播放次数"
              />
            </div>
          </div>

          <div class="content-section">
            <h2>游客播放TOP10比例
              <el-icon>
                <Ticket/>
              </el-icon>
            </h2>
            <div class="content">
              <MusicEcharts
                  v-if="isDataLoading"
                  chartTitle="游客播放榜"
                  :chartData="guestData"
                  chartType="pie"
                  seriesName="播放量"
                  yAxisName="播放次数"
              />
            </div>
          </div>

          <div class="content-section">
            <h2>歌曲推荐
              <el-icon>
                <CollectionTag/>
              </el-icon>
            </h2>
            <div class="content">
              <MusicFeatured/>
            </div>
          </div>

          <div class="content-section">
            <h2>宝藏歌手
              <el-icon>
                <GoldMedal/>
              </el-icon>
            </h2>
            <div class="content">
              <MusicSinger/>
            </div>
          </div>
        </div>
      </el-main>

      <!-- AI -->
      <AI v-if="!authStore.isGuest"/>

      <!-- 音乐播放器组件 -->
      <MusicPlay/>

      <el-footer>
        <Footer/>
      </el-footer>
    </el-container>
  </div>
</template>


<style scoped>
  /* 公共区域 */
.common-layout {
  /* 导入图片 */
  background-image: url('@/assets/images/bg.jpeg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  min-height: 100vh;
  overflow: auto;

  /* 头部导航栏 */
  .el-header {
    background-color: #fff;
    margin-bottom: 50px;
    padding-right: 50px;
  }

  /* 轮播图 */
  .carousel-container {
    margin: 0 30px;
    padding: 20px;
  }

  .carousel-item-content {
    position: relative;
    height: 100%;
    width: 100%;
    overflow: hidden;
    border-radius: 8px;
  }

  .carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }

  .carousel-image:hover {
    transform: scale(1.05);
  }

  .carousel-caption {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    color: white;
    padding: 20px;
    transition: all 0.3s ease;
  }

  .carousel-caption h3 {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }

  .carousel-caption p {
    margin: 5px 0 0;
    font-size: 14px;
    opacity: 0.8;
  }

  /* 自定义指示器样式 */
  .el-carousel__indicator button {
    background-color: rgba(255, 255, 255, 0.5);
    width: 12px;
    height: 12px;
    border-radius: 6px;
  }

  .el-carousel__indicator.is-active button {
    background-color: white;
    width: 24px;
  }

  /* 自定义箭头样式 */
  .el-carousel__arrow {
    background-color: rgba(0, 0, 0, 0.3);
    width: 40px;
    height: 40px;
    border-radius: 50%;
    font-size: 18px;
  }

  .el-carousel__arrow:hover {
    background-color: rgba(0, 0, 0, 0.5);
  }

  /* 中间区域 */
  .container {
    margin: 0 60px;
    margin-top: 100px;
    margin-bottom: 50px;

    /* 卡片区域 */
    .content-section {
      padding: 40px;
      margin: 50px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border: 1px solid #ebeef5;
      border-radius: 8px;
      background-color: #f8f9fa;

      /* 标题 */
      h2 {
        padding-bottom: 50px;
        font-size: 24px;
        color: #303133;
      }

      /* 卡片主体 */
      .content {
        width: 100%;
        margin: 30px 0;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
      }
    }
  }

  /* 底部 */
  .el-footer {
    padding-top: 70px;
    padding-bottom: 180px;
    background-color: #fff;
    display: flex;
    align-items: center;
    flex-direction: column;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}
</style>