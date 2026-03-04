<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <el-avatar 
          :size="80" 
          :src="avatarUrl"
          class="modern-avatar"
        >
          {{ userInfo?.name?.charAt(0)?.toUpperCase() }}
        </el-avatar>
        <div class="welcome-text">
          <h2>{{ greeting }}, {{ userInfo?.name || userInfo?.username }}</h2>
          <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快！</p>
        </div>
      </div>
    </el-card>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :span="6" v-for="(item, index) in overviewData" :key="index">
        <el-card class="data-card" shadow="hover">
          <div class="card-header">
            <span>{{ item.label }}</span>
            <el-icon :class="item.icon"></el-icon>
          </div>
          <div class="card-content">
            <div class="number">{{ item.value }}</div>
            <div class="label">{{ item.description }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-section" v-loading="loading.trend">
          <template #header>
            <div class="chart-header">
              <div class="chart-title">
                <span>订单金额趋势</span>
                <el-tag size="small" effect="plain" class="time-tag">
                  {{ timeRangeLabel }}
                </el-tag>
              </div>
              <div class="chart-controls">
                <el-select v-model="chartType" size="small" style="margin-right: 10px" @change="handleChartTypeChange">
                  <el-option label="折线图" value="line" />
                  <el-option label="柱状图" value="bar" />
                </el-select>
                <el-radio-group v-model="timeRange" @change="fetchTrendData" size="small">
                  <el-radio-button label="month">近一月</el-radio-button>
                  <el-radio-button label="year">近一年</el-radio-button>
                  <el-radio-button label="three_years">近三年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="chartRef" style="height: 400px"></div>
          <div class="chart-summary" v-if="trendSummary">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="summary-item">
                  <div class="label">时间范围内总金额</div>
                  <div class="value">¥{{ trendSummary.amount?.toLocaleString() }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="label">订单总数</div>
                  <div class="value">{{ trendSummary.count }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="label">平均订单金额</div>
                  <div class="value">¥{{ trendSummary.avgAmount?.toLocaleString() }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'
import * as echarts from 'echarts'
import { User, Money, ShoppingCart, Service } from '@element-plus/icons-vue'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const roleLabel = computed(() => userStore.userInfo?.role || '普通用户')

// 图表相关
const chartRef = ref(null)
const chartInstance = ref(null)
const timeRange = ref('month')
const chartType = ref('line')
const trendSummary = ref(null)
const loading = ref({
  trend: false
})

// 数据概览
const overviewData = ref([
  { label: '总用户数', value: 0, icon: 'User', description: '注册用户总数' },
  { label: '总订单数', value: 0, icon: 'ShoppingCart', description: '累计订单数' },
  { label: '总金额', value: '0.00', icon: 'Money', description: '交易总额(元)' },
  { label: '服务项目', value: 0, icon: 'Service', description: '可用服务数' }
])

// 处理头像URL
const avatarUrl = computed(() => {
  return fileUtils.getImageUrl(userInfo.value?.avatar)
})

// 根据时间生成问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早安'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 格式化当前日期
const currentDate = computed(() => {
  const date = new Date()
  return date.toLocaleDateString('zh-CN', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
})

// 时间范围标签
const timeRangeLabel = computed(() => {
  const labels = {
    month: '近30天',
    year: '近12个月',
    three_years: '近3年'
  }
  return labels[timeRange.value]
})

// 获取概览数据
const fetchOverviewData = async () => {
  try {
    const staffId=userStore.isStaff?userStore.staffInfo?.id:null
    const params={
      staffId:staffId
    }
    const data = await request.get('/statistics/overview',params)
    overviewData.value = [
      { label: '总用户数', value: data.totalUsers, icon: 'User', description: '注册用户总数' },
      { label: '总订单数', value: data.totalOrders, icon: 'ShoppingCart', description: '累计订单数' },
      { label: '总金额', value: data.totalAmount.toFixed(2), icon: 'Money', description: '交易总额(元)' },
      { label: '服务项目', value: data.totalServices, icon: 'Service', description: '可用服务数' }
    ]
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  loading.value.trend = true
  try {
    const staffId=userStore.isStaff?userStore.staffInfo?.id:null
    const params={
      staffId:staffId,
      timeRange:timeRange.value,
      groupBy:null
    }
    const { data } = await request.get('/statistics/order-trend',params,{
      showDefaultMsg:false,
      onSuccess:(data)=>{
        updateTrendChart(data||{})
        trendSummary.value = data.total
      }
    })
    
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  } finally {
    loading.value.trend = false
  }
}

// 更新趋势图表
const updateTrendChart = (data) => {
  if (!chartInstance.value) {
    initCharts()
  }

  try {
    if (!data.xAxis || !data.series || !data.series[0]?.data) {
      // 如果数据不完整，显示空状态
      chartInstance.value?.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#909399',
            fontSize: 16,
            fontWeight: 'normal'
          }
        },
        xAxis: {
          show: false,
          type: 'category',
          data: []
        },
        yAxis: {
          show: false,
          type: 'value'
        },
        series: [{
          type: chartType.value,
          data: []
        }]
      }, true)
      return
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          const [param] = params
          return `<div style="padding: 8px;">
            <div style="margin-bottom: 4px;">${param.name}</div>
            <div style="font-weight: bold;">¥${param.value.toLocaleString()}</div>
          </div>`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: chartType.value === 'bar',
        data: data.xAxis || [],
        axisLabel: {
          rotate: 45,
          formatter: (value, index) => {
            // 如果是按月查看，每2天显示一个标签
            if (timeRange.value === 'month') {
              return index % 2 === 0 ? value : ''
            }
            // 其他时间范围保持原样
            return value?.length > 8 ? value.substring(0, 8) + '...' : value
          },
          color: '#666',
          interval: 0  // 设置为0以启用自定义formatter
        },
        axisTick: {
          alignWithLabel: true,
          interval: (index) => {
            // 如果是按月查看，每2天显示一个刻度
            if (timeRange.value === 'month') {
              return index % 2 === 0
            }
            return true
          }
        },
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value) => `¥${value.toLocaleString()}`,
          color: '#666'
        },
        splitLine: {
          lineStyle: {
            color: '#eee'
          }
        }
      },
      series: [{
        name: '订单金额',
        type: chartType.value,
        smooth: chartType.value === 'line',
        data: data.series[0]?.data || [],
        areaStyle: chartType.value === 'line' ? {
          opacity: 0.3,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.7)' },
            { offset: 1, color: 'rgba(64,158,255,0.1)' }
          ])
        } : undefined,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#60ACFC' },
            { offset: 1, color: '#409EFF' }
          ]),
          borderRadius: chartType.value === 'bar' ? [4, 4, 0, 0] : 0
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      }]
    }

    chartInstance.value.setOption(option, true)
  } catch (error) {
    console.error('更新图表失败:', error)
    // 发生错误时重新初始化
    initCharts()
    fetchTrendData()
  }
}

// 处理图表类型切换
const handleChartTypeChange = () => {
  try {
    if (!chartInstance.value) {
      initCharts()
    }
    fetchTrendData()
  } catch (error) {
    console.error('切换图表类型失败:', error)
    initCharts()
    fetchTrendData()
  }
}

// 初始化图表
const initCharts = () => {
  if (chartRef.value) {
    // 如果已存在实例，先销毁
    if (chartInstance.value) {
      chartInstance.value.dispose()
    }
    chartInstance.value = echarts.init(chartRef.value)
  }
}

// 处理窗口大小变化
const handleResize = () => {
  if (chartInstance.value) {
    try {
      chartInstance.value.resize()
    } catch (error) {
      console.error('图表调整大小失败:', error)
      // 如果调整大小失败，重新初始化图表
      initCharts()
      // 重新加载数据
      fetchTrendData()
    }
  }
}

// 使用防抖函数处理resize
const debounce = (fn, delay) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

const debouncedResize = debounce(handleResize, 300)

// 更新生命周期钩子
onMounted(() => {
  initCharts()
  fetchOverviewData()
  fetchTrendData()
  window.addEventListener('resize', debouncedResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', debouncedResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})

// 监听路由变化，确保组件切换时正确处理图表实例
import { onBeforeRouteLeave } from 'vue-router'

onBeforeRouteLeave(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-card {
    margin-bottom: 20px;
    border: none;
    background: linear-gradient(135deg, #4A90E2 0%, #6C5CE7 100%);
    
    :deep(.el-card__body) {
      padding: 30px;
    }
    
    .welcome-content {
      display: flex;
      align-items: center;
      gap: 30px;
      
      .modern-avatar {
        border: 4px solid rgba(255, 255, 255, 0.2);
        background: #fff;
        font-size: 24px;
        font-weight: 600;
        transition: all 0.3s ease;
        
        &:hover {
          transform: scale(1.05);
          border-color: rgba(255, 255, 255, 0.4);
        }
      }
      
      .welcome-text {
        color: #fff;
        
        h2 {
          margin: 0;
          font-size: 28px;
          font-weight: 600;
          letter-spacing: 0.5px;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        
        .welcome-subtitle {
          margin: 8px 0 0;
          font-size: 16px;
          opacity: 0.9;
          font-weight: 400;
        }
      }
    }
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(74, 144, 226, 0.2);
    }
  }

  .data-overview {
    margin-bottom: 20px;

    .data-card {
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

        .number {
          transform: scale(1.1);
          color: #409eff;
        }

        .el-icon {
          transform: scale(1.2);
          color: #409eff;
        }
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 16px;

        .el-icon {
          font-size: 24px;
          transition: all 0.3s ease;
        }
      }

      .card-content {
        text-align: center;
        
        .number {
          font-size: 28px;
          font-weight: bold;
          margin: 10px 0;
          transition: all 0.3s ease;
          animation: countUp 1s ease-out;
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;
    
    .chart-section {
      height: 100%;
      
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .chart-title {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 16px;
          font-weight: 500;
          
          .time-tag {
            font-weight: normal;
            font-size: 12px;
            margin-left: 8px;
          }
        }
        
        .chart-controls {
          display: flex;
          align-items: center;
          gap: 10px;
          
          :deep(.el-radio-group) {
            display: flex;
            flex-direction: row;
            
            .el-radio-button {
              &:first-child {
                .el-radio-button__inner {
                  border-radius: 4px 0 0 4px;
                }
              }
              
              &:last-child {
                .el-radio-button__inner {
                  border-radius: 0 4px 4px 0;
                }
              }
              
              .el-radio-button__inner {
                padding: 8px 15px;
                height: 32px;
                line-height: 1;
                font-size: 12px;
              }
            }
          }
          
          .el-select {
            width: 100px;
          }
        }
      }
    }
  }
}

// 添加动画关键帧
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes gradientText {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

// 添加全局进入动画
.dashboard {
  animation: fadeInUp 0.8s ease;
}

.data-overview {
  .data-card {
    .label {
      color: #666;
      font-size: 14px;
      margin-top: 8px;
    }
  }
}

.chart-section {
  .chart-summary {
    margin-top: 20px;
    padding: 0 20px;
    
    .summary-item {
      text-align: center;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      
      .label {
        color: #666;
        font-size: 14px;
        margin-bottom: 8px;
      }
      
      .value {
        color: #409EFF;
        font-size: 24px;
        font-weight: bold;
      }
    }
  }
}
</style> 