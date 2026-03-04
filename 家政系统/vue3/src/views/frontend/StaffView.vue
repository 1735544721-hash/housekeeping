<template>
  <div class="staff-page">
    <el-skeleton :loading="loading && !isSearch" animated :count="3">
      <template #default>
        <div class="container" :class="{ 'animate__animated animate__fadeIn': !isSearch }">
          <div class="page-header">
            <h1 class="page-title">优质服务人员</h1>
            <p class="page-desc">专业、可靠的家政服务团队</p>
          </div>

          <!-- 筛选条件 -->
          <div class="filter-section" :class="{ 'animate__animated animate__fadeInUp': !isSearch }">
            <el-form :inline="true" :model="filterForm" class="filter-form">
              <el-form-item label="服务类型">
                <el-cascader v-model="filterForm.serviceType" :options="serviceTypes" :props="{
                    checkStrictly: false,
                    value: 'categoryName',
                    label: 'categoryName',
                    children: 'children',
                    emitPath: false,
                    leaf: (data) => !data.children?.length
                  }" placeholder="选择服务类型" clearable filterable :filter-method="filterNode" />
              </el-form-item>
              <el-form-item label="工作区域">
                <el-input v-model="filterForm.workArea" placeholder="输入工作区域" clearable />
              </el-form-item>
              <el-form-item label="最低评分">
                <el-rate v-model="filterForm.minRating" allow-half />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">
                  <el-icon>
                    <Search />
                  </el-icon> 搜索
                </el-button>
                <el-button @click="resetForm">
                  <el-icon>
                    <RefreshRight />
                  </el-icon> 重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 服务人员列表 -->
          <div class="staff-list" :class="{ 'animate__animated animate__fadeInUp': !isSearch }">
            <template v-if="!loading">
              <el-empty v-if="staffList && !staffList.length" description="暂无符合条件的服务人员" />
              <div v-else-if="staffList" v-for="staff in staffList" :key="staff.id" class="staff-card">
                <div class="staff-avatar">
                  <template v-if="staff.user?.avatar">
                    <img :src="fileUtils.getImageUrl(staff.user.avatar)" :alt="staff.user?.name">
                  </template>
                  <div v-else class="avatar-fallback">
                    {{ staff.user?.name?.charAt(0) || '未' }}
                  </div>
                </div>
                <div class="staff-info">
                  <h3>{{ staff.user?.name }}</h3>
                  <div class="staff-rating">
                    <el-rate v-model="staff.rating" disabled show-score text-color="#ff9900" />
                  </div>
                  <div class="staff-meta">
                    <span class="meta-item">
                      <el-icon>
                        <Timer />
                      </el-icon>
                      {{ staff.experience }}年经验
                    </span>
                    <span class="meta-item">
                      <el-icon>
                        <Document />
                      </el-icon>
                      {{ staff.totalOrders }}单完成
                    </span>
                    <span class="meta-item">
                      <el-icon>
                        <CircleCheck />
                      </el-icon>
                      {{ staff.completionRate }}%完成率
                    </span>
                  </div>
                  <div class="staff-tags">
                    <el-tag v-for="type in staff.serviceType" :key="type" size="small" effect="plain">
                      {{ type }}
                    </el-tag>
                  </div>
                  <div class="staff-footer">
                 
                    <el-button type="primary" @click="showStaffDetail(staff)">
                      查看详情
                    </el-button>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 分页 -->
          <div v-if="!loading && staffList && staffList.length" class="pagination">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[12, 24, 36, 48]"
              layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
          </div>

          <!-- 添加推荐服务卡片 -->
          <el-card class="detail-card recommend-card" v-if="userStore.isLoggedIn">
            <template #header>
              <div class="card-header">
                <h2 class="align-left">猜你喜欢</h2>
              </div>
            </template>
            <div class="recommend-list" v-loading="recommendLoading">
              <template v-if="recommendServices?.length">
                <div v-for="item in recommendServices" 
                  :key="item.id" 
                  class="recommend-item" 
                  @click="handleServiceClick(item.id)"
                >
                  <div class="recommend-info">
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
                    <el-button type="primary" link>
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

    <!-- 添加服务人员详情对话框 -->
    <staff-detail-dialog
      v-model="dialogVisible"
      :staff-id="currentStaff?.id"
      @close="handleDialogClose"
    />

    <!-- 添加订单确认对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="预约确认"
      width="500px"
      :close-on-click-modal="false"
      class="order-dialog"
    >
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="100px">
        <el-form-item label="服务项目" class="service-info">
          <div class="selected-service">
            <span class="service-title">{{ selectedService?.title }}</span>
            <el-tag size="small" type="success">¥{{ selectedService?.price }}/小时</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="服务时间" prop="serviceDate">
          <el-date-picker
            v-model="orderForm.serviceDate"
            type="datetime"
            placeholder="选择服务时间"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="服务时长" prop="duration">
          <el-input-number
            v-model="orderForm.duration"
            :min="1"
            :max="8"
            :step="0.5"
            style="width: 100%"
          >
            <template #append>小时</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="订单金额" class="order-amount">
          <span class="amount">¥{{ calculateTotalAmount }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="orderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmOrder">确认预约</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'
import { Search, RefreshRight, Timer, Document, CircleCheck, ArrowRight } from '@element-plus/icons-vue'
import 'animate.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import StaffDetailDialog from '@/components/frontend/StaffDetailDialog.vue'

const router = useRouter()
const staffList = ref([])
const serviceTypes = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(true)
const isSearch = ref(false)

const filterForm = reactive({
  serviceType: '',
  workArea: '',
  minRating: 0
})

const dialogVisible = ref(false)
const currentStaff = ref(null)
const detailLoading = ref(false)

// 添加服务选择相关的状态
const showServiceSelection = ref(false)
const staffServices = ref([])
const selectedServiceId = ref(null)

// 订单相关
const orderDialogVisible = ref(false)
const orderFormRef = ref(null)
const selectedService = ref(null)
const userStore = useUserStore()

const orderForm = reactive({
  serviceDate: '',
  duration: 2
})

const orderRules = {
  serviceDate: [
    { required: true, message: '请选择服务时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请选择服务时长', trigger: 'change' }
  ]
}

// 计算订单总金额
const calculateTotalAmount = computed(() => {
  if (!selectedService.value?.price || !orderForm.duration) return 0
  return (selectedService.value.price * orderForm.duration).toFixed(2)
})

// 日期限制：只能选择今天之后的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选择今天之前的日期
}

// 限制服务时间：早8点到晚8点
const disabledHours = () => {
  const hours = []
  for (let i = 0; i < 24; i++) {
    if (i < 8 || i > 20) {
      hours.push(i)
    }
  }
  return hours
}

// 获取服务人员列表
const getStaffList = async () => {
  try {
    loading.value = true
    const res = await request.get('/staff/list', {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...filterForm
    }, {
      showDefaultMsg: false
    })
    staffList.value = res.records || []
    total.value = res.total
  } catch (error) {
    console.error('获取服务人员列表失败:', error)
    staffList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  isSearch.value = true
  currentPage.value = 1
  getStaffList()
}

const resetForm = () => {
  isSearch.value = true
  filterForm.serviceType = ''
  filterForm.workArea = ''
  filterForm.minRating = 0
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  getStaffList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getStaffList()
}

const showStaffDetail = async (staff) => {
  try {
    detailLoading.value = true
    await request.get(`/staff/${staff.id}`, null, {
      onSuccess: (res) => {
        currentStaff.value = res
        dialogVisible.value = true
        showServiceSelection.value = false
        selectedServiceId.value = null
      }
    })
  } catch (error) {
    console.error('获取服务人员详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const handleOrder = () => {
  if (!selectedServiceId.value) {
    ElMessage.warning('请选择服务项目')
    return
  }

  // 检查是否登录
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('请先登录后再进行预约', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    })
    return
  }

  // 跳转到预约页面
  router.push({
    path: '/order/create',
    query: {
      staffId: currentStaff.value.id,
      serviceId: selectedServiceId.value
    }
  })
}

const getServiceTypes = async () => {
  try {
    const res = await request.get('/category/tree', null, {
      showDefaultMsg: false
    })
    serviceTypes.value = res
  } catch (error) {
    console.error('获取服务类型失败:', error)
  }
}

// 添加自定义过滤方法，支持搜索父级和子级类型
const filterNode = (node, keyword) => {
  const { categoryName, children } = node
  // 如果当前节点匹配关键字
  if (categoryName.toLowerCase().includes(keyword.toLowerCase())) {
    return true
  }
  // 如果有子节点，检查子节点是否匹配
  if (children) {
    return children.some(child =>
      child.categoryName.toLowerCase().includes(keyword.toLowerCase())
    )
  }
  return false
}

// 获取服务人员的服务项目
const fetchStaffServices = async (staffId) => {
  try {
    await request.get(`/staff/service-items/list?staffId=${staffId}`, null, {
      onSuccess: (res) => {
        // 确保获取到的每个服务项目都包含完整信息
        staffServices.value = res.map(item => ({
          id: item.serviceId,  // 使用serviceId作为id
          title: item.serviceItem?.title || '未知服务',  // 从serviceItem中获取标题
          price: item.serviceItem?.price || 0,  // 从serviceItem中获取价格
          status: item.serviceItem?.status || 0,  // 从serviceItem中获取状态
          category: item.serviceItem?.category || null,  // 从serviceItem中获取分类
          description: item.serviceItem?.description || ''  // 从serviceItem中获取描述
        }))
        console.log('服务项目列表:', staffServices.value)  // 添加日志查看数据
      }
    })
  } catch (error) {
    console.error('获取服务项目失败:', error)
    staffServices.value = []  // 确保失败时设置为空数组
  }
}

// 显示服务选择
const showServiceSelect = async () => {
  if (currentStaff.value) {
    showServiceSelection.value = true
    await fetchStaffServices(currentStaff.value.id)
  }
}

// 选择服务
const selectService = (service) => {
  if (!service || !service.id) return  // 添加空值检查
  selectedServiceId.value = service.id === selectedServiceId.value ? null : service.id
}

// 处理对话框关闭
const handleDialogClose = () => {
  currentStaff.value = null
}

// 确认创建订单
const confirmOrder = async () => {
  if (!orderFormRef.value) return

  try {
    await orderFormRef.value.validate()

    // 构建订单备注信息
    const remark = `服务时间：${orderForm.serviceDate}，服务时长：${orderForm.duration}小时`

    // 构建订单数据
    const orderData = {
      userId: userStore.userInfo.id,
      staffId: currentStaff.value.id,
      serviceId: selectedServiceId.value,
      totalAmount: calculateTotalAmount.value,
      remark: remark
    }

    // 二次确认
    await ElMessageBox.confirm(
      `确认创建订单？\n服务时间：${orderForm.serviceDate}\n服务时长：${orderForm.duration}小时\n订单金额：¥${calculateTotalAmount.value}`,
      '订单确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 创建订单
    await request.post('/order', orderData, {
      successMsg: '预约成功'
    })

    // 关闭所有对话框
    orderDialogVisible.value = false
    handleDialogClose()

    // 可以选择跳转到订单列表页面
    router.push('/orders')
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('创建订单失败:', error)
    }
  }
}

// 添加推荐服务相关的状态
const recommendLoading = ref(false)
const recommendServices = ref([])

// 获取推荐服务
const fetchRecommendServices = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return

  recommendLoading.value = true
  try {
    const res = await request.get(`/recommend/user-based/${userStore.userInfo.id}?limit=3`)
    recommendServices.value = res || []
  } catch (error) {
    console.error('获取推荐服务失败:', error)
    recommendServices.value = []
  } finally {
    recommendLoading.value = false
  }
}

// 修改服务点击处理函数
const handleServiceClick = (serviceId) => {
  router.push(`/service/${serviceId}`)
}

onMounted(async () => {
  try {
    loading.value = true
    isSearch.value = false
    await Promise.all([
      getStaffList(),
      getServiceTypes(),
      // 添加获取推荐服务
      userStore.isLoggedIn ? fetchRecommendServices() : Promise.resolve()
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.staff-page {
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

.page-header {
  text-align: center;
  margin-bottom: 48px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, #fff 100%);
  padding: 32px 0;
  border-radius: 8px;
}

.page-title {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-desc {
  color: #909399;
  font-size: 16px;
}

.filter-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #ebeef5;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.staff-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.staff-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #ebeef5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.staff-card:hover {
  transform: translateY(-2px);
  border-color: #4a90e2;
  box-shadow: 0 6px 16px rgba(74, 144, 226, 0.1);
}

.staff-avatar {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #4a90e2;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  background: #4a90e2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
}

.staff-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.staff-info {
  text-align: center;
}

.staff-info h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px;
}

.staff-rating {
  margin: 12px 0;
}

.staff-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin: 12px 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.meta-item .el-icon {
  font-size: 16px;
  color: #4a90e2;
}

.staff-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin: 16px 0;
}

.staff-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.price {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 动画持续时间 */
.animate__animated {
  animation-duration: 0.8s;
}

/* 优化级联选择器样式 */
:deep(.el-cascader) {
  width: 240px;
}

/* 调整表单项间距 */
.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

/* 优化级联选择器下拉菜单样式 */
:deep(.el-cascader-panel) {
  max-height: 300px;
}

:deep(.el-cascader-node.is-disabled) {
  color: #909399;
  cursor: not-allowed;
}

/* 添加详情对话框样式 */
.staff-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.staff-avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #4a90e2;
}

.staff-avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  flex: 1;
}

.detail-info h2 {
  margin: 0 0 12px;
  font-size: 24px;
  color: #303133;
}

.rating-large {
  transform: scale(1.2);
  transform-origin: left;
}

.detail-tag {
  margin: 0 8px 8px 0;
}

.cert-item {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.cert-code {
  color: #909399;
}

:deep(.el-descriptions__label) {
  width: 120px;
  justify-content: flex-end;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

.staff-detail-dialog {
  .service-selection {
    margin-top: 24px;
    border-top: 1px solid var(--el-border-color-light);
    padding-top: 24px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      .title-text {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .service-count {
        font-size: 13px;
      }
    }

    .service-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-height: 400px;
      overflow-y: auto;
      padding: 4px;

      .service-item {
        display: flex;
        justify-content: space-between;
        align-items: stretch;
        padding: 16px 20px;
        border: 2px solid var(--el-border-color-light);
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        background-color: var(--el-bg-color);
        
        &:hover {
          border-color: var(--el-color-primary-light-5);
          background-color: var(--el-color-primary-light-9);
          transform: translateX(4px);
        }
        
        &.is-selected {
          background-color: var(--el-color-primary);
          border-color: var(--el-color-primary);
          transform: translateX(4px);
          box-shadow: 0 4px 16px rgba(var(--el-color-primary-rgb), 0.2);
          
          .service-info {
            .service-title,
            .service-desc {
              color: #ffffff;
            }
          }

          .service-price-box {
            background-color: rgba(255, 255, 255, 0.1);
            
            .price-label,
            .price-value,
            .price-unit {
              color: #ffffff;
            }
          }
        }

        .service-info {
          flex: 1;
          margin-right: 20px;
          display: flex;
          flex-direction: column;
          gap: 8px;

          .service-header {
            display: flex;
            align-items: center;
            gap: 12px;

            .service-title {
              font-size: 16px;
              font-weight: 500;
              color: var(--el-text-color-primary);
            }
          }

          .service-category {
            display: flex;
            gap: 8px;
          }

          .service-desc {
            font-size: 13px;
            color: var(--el-text-color-secondary);
            line-height: 1.5;
            margin-top: 4px;
          }
        }

        .service-price-box {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 12px 20px;
          background-color: var(--el-color-primary-light-9);
          border-radius: 6px;
          min-width: 120px;

          .price-label {
            font-size: 12px;
            color: var(--el-text-color-secondary);
            margin-bottom: 4px;
          }

          .price-value {
            font-size: 20px;
            font-weight: 600;
            color: var(--el-color-danger);
            line-height: 1.2;
          }

          .price-unit {
            font-size: 12px;
            color: var(--el-text-color-secondary);
            margin-top: 2px;
          }
        }
      }
    }
  }
}

.order-dialog {
  .service-info {
    .selected-service {
      display: flex;
      align-items: center;
      gap: 12px;

      .service-title {
        font-size: 15px;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }
  }

  .order-amount {
    .amount {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-color-danger);
    }
  }

  :deep(.el-input-number) {
    width: 100%;
  }

  :deep(.el-date-picker) {
    width: 100%;
  }
}

// 添加推荐服务相关样式
.recommend-card {
  margin-top: 24px;
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
</style> 