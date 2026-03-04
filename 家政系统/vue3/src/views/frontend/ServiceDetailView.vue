<template>
  <div class="service-detail-page">
    <el-skeleton :loading="loading" animated>
      <template #default>
        <div class="container animate__animated animate__fadeIn">
          <!-- 服务基本信息 -->
          <div class="service-header">
            <div class="service-info">
              <div class="category-path">
                <el-tag size="small" type="info" effect="plain">
                  {{ service?.category?.categoryName }}
                </el-tag>
              </div>
              <h1 class="service-title">{{ service.title }}</h1>
              <div class="service-meta">
                <div class="price-box">
                  <span class="price-label">服务价格</span>
                  <span class="price-value">¥{{ service.price }}</span>
                  <span class="price-unit">/小时</span>
                </div>
                <el-tag 
                  :type="service.status === 1 ? 'success' : 'info'"
                  effect="light"
                >
                  {{ service.status === 1 ? '可预约' : '暂停预约' }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 修改服务描述卡片部分 -->
          <el-card class="detail-card description-card">
            <template #header>
              <div class="card-header">
                <h2 class="align-left">服务详情</h2>
              </div>
            </template>
            <div class="service-detail-content">
              <!-- 服务基本信息 -->
              <div class="detail-section">
                <h3 class="section-title">基本信息</h3>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="服务类型">
                    <el-tag size="small">{{ service.category?.categoryName }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="服务状态">
                    <el-tag 
                      :type="service.status === 1 ? 'success' : 'warning'"
                      size="small"
                    >
                      {{ service.status === 1 ? '正常提供' : '暂停服务' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="创建时间">
                    {{ formatDate(service.createTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="最近更新">
                    {{ formatDate(service.updateTime) }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <!-- 服务说明 -->
              <div class="detail-section">
                <h3 class="section-title">服务说明</h3>
                <div class="service-description" v-if="service.description">
                  {{ service.description }}
                </div>
                <el-empty v-else description="暂无服务说明" />
              </div>

              <!-- 服务特点 -->
              <div class="detail-section">
                <h3 class="section-title">服务特点</h3>
                <div class="service-features">
                  <div class="feature-item">
                    <el-icon><Timer /></el-icon>
                    <div class="feature-content">
                      <h4>灵活预约</h4>
                      <p>可根据您的时间安排灵活预约服务时间</p>
                    </div>
                  </div>
                  <div class="feature-item">
                    <el-icon><UserFilled /></el-icon>
                    <div class="feature-content">
                      <h4>专业服务</h4>
                      <p>经过专业培训的服务人员，确保服务质量</p>
                    </div>
                  </div>
                  <div class="feature-item">
                    <el-icon><Service /></el-icon>
                    <div class="feature-content">
                      <h4>品质保障</h4>
                      <p>服务全程保险保障，让您无后顾之忧</p>
                    </div>
                  </div>
                  <div class="feature-item">
                    <el-icon><CircleCheck /></el-icon>
                    <div class="feature-content">
                      <h4>满意优先</h4>
                      <p>服务不满意可随时沟通调整</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 服务流程 -->
              <div class="detail-section">
                <h3 class="section-title">服务流程</h3>
                <el-steps :active="4" finish-status="success" align-center>
                  <el-step title="在线预约" description="选择服务人员和时间">
                    <template #icon>
                      <el-icon><Calendar /></el-icon>
                    </template>
                  </el-step>
                  <el-step title="确认订单" description="确认服务内容和费用">
                    <template #icon>
                      <el-icon><Document /></el-icon>
                    </template>
                  </el-step>
                  <el-step title="上门服务" description="专业人员准时上门">
                    <template #icon>
                      <el-icon><Van /></el-icon>
                    </template>
                  </el-step>
                  <el-step title="完成评价" description="服务完成后评价反馈">
                    <template #icon>
                      <el-icon><Star /></el-icon>
                    </template>
                  </el-step>
                </el-steps>
              </div>
            </div>
          </el-card>

          <!-- 可用服务人员 -->
          <el-card class="detail-card staff-card">
            <template #header>
              <div class="card-header">
                <h2 class="align-left">可预约的服务人员</h2>
              </div>
            </template>
            <div class="staff-list" v-loading="staffLoading">
              <template v-if="availableStaff?.length">
                <div v-for="staff in availableStaff" :key="staff.id" class="staff-item">
                  <!-- 左侧：头像和姓名 -->
                  <div class="staff-left">
                    <div class="staff-avatar">
                      <template v-if="staff.user?.avatar">
                        <img :src="fileUtils.getImageUrl(staff.user.avatar)" :alt="staff.user?.name">
                      </template>
                      <div v-else class="avatar-fallback">
                        {{ staff.user?.name?.charAt(0) || '未' }}
                      </div>
                    </div>
                    <h3>{{ staff.user?.name }}</h3>
                  </div>
                  
                  <!-- 右侧：信息和按钮 -->
                  <div class="staff-right">
                    <div class="staff-info">
                      <div class="staff-rating">
                        <el-rate v-model="staff.rating" disabled show-score />
                      </div>
                      <p class="staff-exp">{{ staff.experience }}年经验 | 完成率{{ staff.completionRate }}%</p>
                 
                    </div>
                    <div class="staff-action">
                      <el-button type="primary" @click="handleOrder(staff.id)">
                        立即预约
                      </el-button>
                    </div>
                  </div>
                </div>
              </template>
              <el-empty v-else class="centered-empty" description="暂无可用的服务人员" />
            </div>
          </el-card>

          <!-- 修改推荐服务卡片部分 -->
          <el-card class="detail-card recommend-card">
            <template #header>
              <div class="card-header">
                <h2 class="align-left">相关推荐</h2>
              </div>
            </template>
            <div class="recommend-list" v-loading="recommendLoading">
              <template v-if="recommendServices?.length">
                <div v-for="item in recommendServices" 
                  :key="item.id" 
                  class="recommend-item"
                >
                  <!-- 使用 @click.prevent 防止可能的默认行为 -->
                  <div class="recommend-info" @click.prevent="handleServiceClick(item.id)">
                    <div class="recommend-header">
                      <h3>{{ item.title }}</h3>
                      <el-tag 
                        size="small" 
                        :type="item.status === 1 ? 'success' : 'info'"
                        effect="plain"
                      >
                        {{ item.status === 1 ? '可预约' : '暂停预约' }}
                      </el-tag>
                    </div>
                    <div class="recommend-category">
                      <el-tag size="small" type="warning" effect="plain">
                        {{ item.category?.categoryName }}
                      </el-tag>
                    </div>
                    <p class="recommend-desc">{{ item.description || '暂无描述' }}</p>
                  </div>
                  <div class="recommend-footer">
                    <div class="price-box">
                      <span class="price-value">¥{{ item.price }}</span>
                      <span class="price-unit">/小时</span>
                    </div>
                    <!-- 使用 @click.prevent.stop 阻止事件冒泡和默认行为 -->
                    <el-button type="primary" link @click.prevent.stop="handleServiceClick(item.id)">
                      查看详情 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                    </el-button>
                  </div>
                </div>
              </template>
              <el-empty v-else class="centered-empty" description="暂无推荐服务" />
            </div>
          </el-card>
        </div>
      </template>
    </el-skeleton>

    <!-- 服务人员详情对话框 -->
    <staff-detail-dialog
      v-model="staffDetailVisible"
      :staff-id="selectedStaffId"
      @close="handleStaffDialogClose"
    />
  </div>
</template> 

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'
import StaffDetailDialog from '@/components/frontend/StaffDetailDialog.vue'
import 'animate.css'
import { Timer, UserFilled, Service, CircleCheck, Calendar, Document, Van, Star, ArrowRight } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/dateUtils'
import { recordBrowseHistory } from '@/api/browseHistory'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const staffLoading = ref(false)
const recommendLoading = ref(false)
const service = ref({})
const availableStaff = ref([])
const recommendServices = ref([])

// 服务人员详情对话框
const staffDetailVisible = ref(false)
const selectedStaffId = ref(null)

// 获取服务详情
const fetchServiceDetail = async () => {
  try {
    const res = await request.get(`/service/${route.params.id}`)
    service.value = res || {}
  } catch (error) {
    console.error('获取服务详情失败:', error)
    ElMessage.error('获取服务详情失败')
    service.value = {}
  }
}

// 获取可用服务人员
const fetchAvailableStaff = async () => {
  staffLoading.value = true
  try {
    const res = await request.get(`/staff/available?serviceId=${route.params.id}`)
    availableStaff.value = res || []
  } catch (error) {
    console.error('获取服务人员失败:', error)
    ElMessage.error('获取服务人员失败')
    availableStaff.value = []
  } finally {
    staffLoading.value = false
  }
}

// 获取推荐服务
const fetchRecommendServices = async () => {
  recommendLoading.value = true
  try {
    const res = await request.get(`/recommend/item-based/${route.params.id}?limit=4`)
    recommendServices.value = res || []
  } catch (error) {
    console.error('获取推荐服务失败:', error)
    ElMessage.error('获取推荐服务失败')
    recommendServices.value = []
  } finally {
    recommendLoading.value = false
  }
}

// 处理预约
const handleOrder = (staffId) => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('请先登录后再进行预约', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户点击取消按钮时，不做任何操作
      // 添加空的 catch 处理器来避免未处理的 Promise 拒绝错误
    })
    return
  }

  // 跳转到预约页面
  router.push({
    path: '/order/create',
    query: {
      staffId: staffId,
      serviceId: route.params.id
    }
  })
}

// 封装获取所有数据的方法
const fetchAllData = async () => {
  loading.value = true
  try {
    // 并行加载页面数据
    await Promise.all([
      fetchServiceDetail(),
      fetchAvailableStaff(),
      fetchRecommendServices()
    ])
    
    // 单独处理浏览历史记录，避免未登录用户报错
    try {
      if (route.params.id) {
        await recordBrowseHistory(route.params.id)
      }
    } catch (err) {
      console.error('记录浏览历史失败:', err)
      // 浏览历史记录失败不影响页面整体功能
    }
  } catch (error) {
    console.error('页面加载失败:', error)
    ElMessage.error('页面加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchAllData()
      // 滚动到页面顶部
      window.scrollTo(0, 0)
    }
  }
)

// 修改服务点击处理函数
const handleServiceClick = (serviceId) => {
  console.log('Clicking service:', serviceId)
  if (serviceId === Number(route.params.id)) return
  router.push({
    path: `/service/${serviceId}`,
    replace: true
  })
}

// 处理对话框关闭
const handleStaffDialogClose = () => {
  selectedStaffId.value = null
}

onMounted(() => {
  if (!route.params.id) {
    ElMessage.error('参数错误')
    router.push('/services')
    return
  }
  fetchAllData()
})
</script>

<style lang="scss" scoped>
.service-detail-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 0;
  padding-top: calc(40px + var(--el-header-height, 60px));
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.service-header {
  background: white;
  padding: 32px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .category-path {
    margin-bottom: 16px;
  }

  .service-title {
    font-size: 32px;
    color: #303133;
    margin: 0 0 24px;
  }

  .service-meta {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .price-box {
    display: flex;
    align-items: baseline;
    gap: 8px;

    .price-label {
      color: #909399;
      font-size: 14px;
    }

    .price-value {
      font-size: 32px;
      color: #f56c6c;
      font-weight: 600;
    }

    .price-unit {
      color: #909399;
      font-size: 14px;
    }
  }
}

.detail-card {
  margin-bottom: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .card-header {
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 16px;

    h2 {
      font-size: 18px;
      color: #303133;
      margin: 0;
      text-align: left;
    }
  }
}

.service-detail-content {
  .detail-section {
    margin-bottom: 32px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 16px;
      padding-left: 10px;
      border-left: 3px solid var(--el-color-primary);
      text-align: left;
    }
  }

  .service-description {
    font-size: 14px;
    line-height: 1.8;
    color: #606266;
    white-space: pre-wrap;
    background: #f8f9fa;
    padding: 16px;
    border-radius: 4px;
  }

  .service-features {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-top: 16px;

    .feature-item {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        background: white;
      }

      .el-icon {
        font-size: 24px;
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
        padding: 12px;
        border-radius: 8px;
      }

      .feature-content {
        flex: 1;

        h4 {
          font-size: 16px;
          color: #303133;
          margin: 0 0 8px;
        }

        p {
          font-size: 14px;
          color: #606266;
          margin: 0;
          line-height: 1.5;
        }
      }
    }
  }

  :deep(.el-descriptions) {
    margin-top: 16px;
  }

  :deep(.el-steps) {
    margin-top: 24px;
  }
}

.staff-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 200px;

  .centered-empty {
    grid-column: 1 / -1;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    min-height: inherit;
  }

  .staff-item {
    display: flex;
    gap: 24px;
    padding: 24px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    // 左侧区域：头像和姓名
    .staff-left {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      width: 120px;
      flex-shrink: 0;

      .staff-avatar {
        flex-shrink: 0;
        width: 100px;
        height: 100px;
        border-radius: 50%;
        overflow: hidden;
        border: 2px solid #4a90e2;
        position: relative;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          position: absolute;
          top: 0;
          left: 0;
        }

        .avatar-fallback {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #4a90e2;
          color: white;
          font-size: 24px;
        }
      }

      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
        text-align: center;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    // 右侧区域：信息和按钮
    .staff-right {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      min-height: 140px;

      .staff-info {
        .staff-rating {
          margin-bottom: 8px;
        }

        .staff-exp {
          color: #909399;
          font-size: 13px;
          margin-bottom: 8px;
        }

        .staff-price {
          color: #f56c6c;
          font-size: 16px;
          font-weight: 600;
        }
      }

      .staff-action {
        display: flex;
        justify-content: flex-end;
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid var(--el-border-color-lighter);
        
        .el-button {
          padding: 12px 24px;
          font-size: 14px;
        }
      }
    }
  }
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;

  .centered-empty {
    grid-column: 1 / -1;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    min-height: inherit;
  }

  .recommend-item {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 20px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: white;
    height: 100%;
    min-height: 200px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      border-color: var(--el-color-primary-light-5);

      .el-button {
        color: var(--el-color-primary);
        .el-icon {
          transform: translateX(4px);
        }
      }
    }

    .recommend-info {
      flex: 1;
      cursor: pointer;
      padding: 10px;

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      .recommend-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 12px;

        h3 {
          margin: 0;
          font-size: 16px;
          color: #303133;
          text-align: left;
          flex: 1;
        }
      }

      .recommend-category {
        margin-bottom: 12px;
      }

      .recommend-desc {
        font-size: 13px;
        color: #606266;
        line-height: 1.6;
        margin: 0;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .recommend-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid var(--el-border-color-lighter);

      .price-box {
        .price-value {
          font-size: 20px;
          color: #f56c6c;
          font-weight: 600;
        }

        .price-unit {
          font-size: 12px;
          color: #909399;
          margin-left: 4px;
        }
      }

      .el-button {
        .el-icon {
          transition: transform 0.3s ease;
        }
      }
    }
  }
}

.animate__animated {
  animation-duration: 0.8s;
}
</style> 