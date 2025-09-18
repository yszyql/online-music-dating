<script setup>
import {ref, onMounted, watch, nextTick, onUnmounted} from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  // 图表标题
  chartTitle: {
    type: String,
    required: true
  },
  // 图表数据，格式为 { names: ['A', 'B', 'C'], counts: [10, 20, 30] }
  chartData: {
    type: Object,
    default: () => ({ names: [], counts: [] }) // 明确默认值为对象
  },
  // 控制图表类型，默认柱状图
  chartType: {
    type: String,
    default: 'bar',
    validator: (value) => {
      return ['bar', 'line', 'pie'].includes(value);
    }
  },
  // 系列名称（用于 tooltip、图例等）
  seriesName: {
    type: String,
    default: '数据'
  },
  // Y 轴名称（柱状图、折线图用）
  yAxisName: {
    type: String,
    default: ''
  }
});

const chartRef = ref(null);
let chartInstance = null; // 保存ECharts实例

const renderChart = () => {
  if (!chartRef.value) return;
  // 先销毁旧实例
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  const { clientWidth, clientHeight } = chartRef.value;
  if (clientWidth === 0 || clientHeight === 0) {
    console.warn('ECharts 容器宽高为 0，跳过本次渲染');
    return;
  }
  const chart = echarts.init(chartRef.value);
  let option = {};

  // 根据图表类型配置不同的 option
  if (props.chartType === 'bar') {
    option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: props.chartData.names,
        // 核心配置：强制显示所有标签，并设置倾斜/换行
        axisLabel: {
          show: true,
          interval: 0, // 0 = 强制显示所有标签，默认是 auto（会隐藏重叠的）
          margin: 8, // 标签与轴线的间距
          overflow: 'break',// 若名称过长，可开启自动换行
          ellipsis: '\n'
        }
      },
      yAxis: { type: 'value', name: props.yAxisName },
      series: [
        {
          name: props.seriesName,
          type: 'bar',
          data: props.chartData.counts,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#009688' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }
      ]
    };
  } else if (props.chartType === 'line') {
    option = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: props.chartData.names,
        // 核心配置：强制显示所有标签，并设置倾斜/换行
        axisLabel: {
          show: true,
          interval: 0, // 0 = 强制显示所有标签，默认是 auto（会隐藏重叠的）
          margin: 8, // 标签与轴线的间距
          overflow: 'break',// 若名称过长，可开启自动换行
          ellipsis: '\n'
        }
      },
      yAxis: { type: 'value', name: props.yAxisName },
      series: [
        {
          name: props.seriesName,
          type: 'line',
          data: props.chartData.counts,
          smooth: true,
          lineStyle: {
            color: '#67c23a'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgb(245,231,138)' },
              { offset: 1, color: 'rgb(204,143,143)' }
            ])
          }
        }
      ]
    };
  }else if (props.chartType === 'pie') {
    option = {
      tooltip: { trigger: 'item', formatter: "{b}: {c} ({d}%)" }, // 悬浮显示：名称+数值+占比
      legend: {
        orient: 'vertical',
        left: 'left', // 图例居左，方便看数据
        data: props.chartData.names
      },
      series: [
        {
          name: props.seriesName,
          type: 'pie',
          radius: ['40%', '70%'], // 环形图内外半径
          data: props.chartData.names.map((name, index) => ({
            name,
            value: props.chartData.counts[index]
          })),
          label: {
            show: true,
            position: 'outside', // 标签显示在饼图外
            formatter: "{b}: {c} ({d}%)" // 显示：名称+数值+占比
          },
          itemStyle: {
            color: (params) => {
              const colorList = [
                  '#009688', '#188df0', '#2f4050', '#871515',
                  '#aa6c39','#ff6b6b','#67c23a','#e6a23c',
                  '#d05ce3','#ff7d00'
              ];
              return colorList[params.dataIndex % colorList.length];
            },
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)' //  hover 时阴影，突出交互
            }
          }
        }
      ]
    };
  }

  // 空数据处理
  if (!props.chartData || props.chartData.names.length === 0 || props.chartData.counts.length === 0) {
    option = {
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value', name: props.yAxisName },
      series: [],
      graphic: {
        type: 'text',
        left: 'center',
        top: 'center',
        style: {
          text: '暂无数据',
          fill: '#999',
          fontSize: 14
        }
      }
    };
  }

  chart.setOption(option);
  // 响应式适配，窗口变化时重新渲染
  window.addEventListener('resize', () => {
    chart.resize();
  });
};


// 优化窗口Resize逻辑，只保留一处
watch(() => window.innerWidth, () => {
  if (chartInstance) {
    chartInstance.resize();
    const isSmallScreen = window.innerWidth < 768;
    chartInstance.setOption({
      legend: { orient: isSmallScreen ? 'horizontal' : 'vertical' }
    });
  }
});

// 监听 chartData 变化，重新渲染图表
watch(
    () => props.chartData,
    (newVal, oldVal) => {
      // 明确新数据是否有效
      const newDataIsValid =
          newVal &&
          Array.isArray(newVal.names) &&
          Array.isArray(newVal.counts) &&
          newVal.names.length > 0 &&
          newVal.names.length === newVal.counts.length;

      // 旧数据是否有效（避免首次监听时 oldVal 为 undefined）
      const oldDataWasValid =
          oldVal &&
          Array.isArray(oldVal.names) &&
          Array.isArray(oldVal.counts) &&
          oldVal.names.length > 0 &&
          oldVal.names.length === oldVal.counts.length;

      // 仅当「新数据有效」或「新数据无效（需显示空态）」时触发渲染
      if (newDataIsValid || !oldDataWasValid) {
        renderChart();
      }
    },
    { deep: true, immediate: true }
);

// 初始渲染
onMounted(async () => {
  await nextTick();
  // 主动触发一次尺寸检查，防止因异步渲染导致容器宽高为0
  const checkSize = setInterval(() => {
    const { clientWidth, clientHeight } = chartRef.value;
    if (clientWidth > 0 && clientHeight > 0) {
      clearInterval(checkSize);
      renderChart();
    }
  }, 100);
});

// 组件卸载时销毁实例
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
  }
});

</script>

<template>
  <div class="chart-container">
    <h3>{{ chartTitle }}</h3>
    <div ref="chartRef" class="chart-wrapper"></div>
  </div>
</template>

<style scoped>

/* 图表容器样式优化 */
.chart-container {
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  /* 给容器设置最小宽高，确保 ECharts 能获取到尺寸 */
  width: 1300px;
  min-height: 400px;
}

/* 标题样式优化 */
.chart-container h3 {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

/* 图表包裹层，用于设置图表实际渲染区域的宽高 */
.chart-wrapper {
  flex: 1; /* 占满父容器剩余空间 */
  width: 100%;
}

/* 悬停动画优化 */
.chart-container:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

</style>