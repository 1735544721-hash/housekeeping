<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { ElMessageBox } from 'element-plus'
import { Document, Timer, Star, Location, Phone, User, Service } from '@element-plus/icons-vue'
import fileUtils from '@/utils/fileUtils'
import { useRouter } from 'vue-router'
import { formatDate } from '@/utils/dateUtils'

const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const router = useRouter()
const reviewDialogVisible = ref(false)
const currentOrder = ref(null)
const reviewForm = ref({
  skillRating: 5,
  attitudeRating: 5,
  experienceRating: 5,
  content: ''
})
const currentReview = ref(null)

// 订单状态映射
const orderStatusMap = {
  1: { label: '待支付', type: 'warning' },
  2: { label: '待接单', type: 'info' },
  3: { label: '已接单', type: 'primary' },
  4: { label: '服务中', type: 'success' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'danger' },
  7: { label: '已关闭', type: 'info' }
}

// 获取订单列表
const fetchOrders = async () => {
  if (!userStore.isLoggedIn) return
  
  loading.value = true
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    userId: userStore.userInfo?.id
  }
  try {
    const res = await request.get('/order/list', params, {
      onSuccess: (res) => {
        // 初始化订单数据，为每个订单添加评价状态属性
        orders.value = res.records.map(order => ({
          ...order,
          reviewStatus: null // 评价状态：null = 未检查, false = 未评价, object = 已评价
        }))
        total.value = res.total
        // 检查每个订单的评价状态
        orders.value.forEach(order => {
          checkReviewStatus(order)
        })
      }
    })
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 检查评价状态
const checkReviewStatus = async (order) => {
  if (order.orderStatus !== 5) return // 只检查已完成的订单
  
  try {
    await request.get(`/review/order/${order.id}/user/${userStore.userInfo.id}`, null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        // 只有当评价内容确实存在并且有ID时，才认为评价存在
        if (res && res.id) {
          order.reviewStatus = res
        } else {
          order.reviewStatus = false // 无评价
        }
      }
    })
  } catch (error) {
    console.error(`获取订单 ${order.id} 评价信息失败:`, error)
    order.reviewStatus = false // 设置为未评价
  }
}

// 取消订单
const cancelOrder = async (order) => {
  const { value: reason } = await ElMessageBox.prompt('请输入取消理由', '取消订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入理由',
    type: 'warning'
  });

  if (reason) {
    try {
      await request.put(`/order/${order.id}/status?status=6&reason=${encodeURIComponent(reason)}`, {
        successMsg: '订单已取消'
      });
      
      fetchOrders();
    } catch (error) {
      console.error('取消订单失败:', error);
    }
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 格式化金额
const formatAmount = (amount) => {
  return `¥${Number(amount).toFixed(2)}`
}

// 解析备注JSON
const parseRemark = (remarkStr) => {
  try {
    return JSON.parse(remarkStr)
  } catch (e) {
    return {}
  }
}

// 获取头像显示文本
const getAvatarText = (staff) => {
  if (!staff?.user?.name) return '?'
  return staff.user.name.charAt(0)
}

// 支付处理函数 - 跳转到支付页面
const handlePay = (order) => {
  router.push({
    path: '/payment',
    query: { orderId: order.id }
  })
}

// 申请退款
const handleRefund = (order) => {
  router.push({
    path: '/refund/apply',
    query: { orderId: order.id }
  })
}

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const map = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'BALANCE': '余额支付'
  }
  return map[method] || '未知'
}

// 获取退款状态文本
const getRefundStatusText = (status) => {
  const map = {
    1: '退款中',
    2: '已退款',
    3: '退款失败'
  }
  return map[status] || '未知'
}

// 获取退款状态类型
const getRefundStatusType = (status) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

// 添加检查评价和打开评价对话框的方法
const handleReview = async (order) => {
  currentOrder.value = order
  currentReview.value = order.reviewStatus
  reviewDialogVisible.value = true
}

// 提交评价
const submitReview = async () => {
  try {
    const reviewData = {
      ...reviewForm.value,
      orderId: currentOrder.value.id,
      userId: userStore.userInfo.id,
      staffId: currentOrder.value.staff.id
    }
    
    await request.post('/review', reviewData, {
      successMsg: '评价提交成功'
    })
    
    reviewDialogVisible.value = false
    // 重置表单
    reviewForm.value = {
      skillRating: 5,
      attitudeRating: 5,
      experienceRating: 5,
      content: ''
    }
    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    console.error('提交评价失败:', error)
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="my-orders-page">
    <div class="container">
      <div class="page-header">
        <h1>我的订单</h1>
      </div>

      <div class="orders-card" v-loading="loading">
        <el-table 
          :data="orders" 
          style="width: 100%"
          :header-cell-style="{
            background: '#F8FAFC',
            color: '#2C3E50',
            fontWeight: '500',
            fontSize: '14px',
            height: '50px'
          }"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="order-detail">
                <!-- 服务人员信息 -->
                <div class="detail-section">
                  <div class="section-header">
                    <el-icon><User /></el-icon>
                    <span>服务人员信息</span>
                  </div>
                  <div class="staff-card">
                    <el-avatar 
                      :size="60" 
                      :src="fileUtils.getImageUrl(row.staff?.user?.avatar)"
                      class="staff-avatar"
                    >
                      {{ getAvatarText(row.staff) }}
                    </el-avatar>
                    <div class="staff-info">
                      <h3>{{ row.staff?.user?.name }}</h3>
                      <div class="staff-meta">
                        <el-rate 
                          v-model="row.staff.rating" 
                          disabled 
                          show-score 
                          :max="5"
                          :colors="['#FFD93D', '#FFD93D', '#FFD93D']"
                        />
                        <span class="experience">{{ row.staff.experience }}年经验</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 服务信息 -->
                <div class="detail-section">
                  <div class="section-header">
                    <el-icon><Service /></el-icon>
                    <span>服务信息</span>
                  </div>
                  <div class="service-info">
                    <div class="info-row">
                      <div class="info-item">
                        <span class="label">服务项目</span>
                        <span class="value">{{ row.serviceItem?.title }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">服务类别</span>
                        <span class="value">{{ row.serviceItem?.category?.categoryName }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">服务价格</span>
                        <span class="value price">¥{{ row.serviceItem?.price }}/小时</span>
                      </div>
                    </div>
                    <div class="service-desc">
                      <span class="label">服务说明</span>
                      <p class="value">{{ row.serviceItem?.description || '暂无说明' }}</p>
                    </div>
                  </div>
                </div>

                <!-- 订单信息 -->
                <div class="detail-section">
                  <div class="section-header">
                    <el-icon><Document /></el-icon>
                    <span>订单信息</span>
                  </div>
                  <div class="order-info">
                    <div class="info-row">
                      <div class="info-item">
                        <span class="label">订单编号</span>
                        <span class="value">{{ row.id }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">下单时间</span>
                        <span class="value">{{ formatDate(row.createTime) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">订单金额</span>
                        <span class="value price">{{ formatAmount(row.totalAmount) }}</span>
                      </div>
                    </div>
                    <div class="info-row">
                      <div class="info-item">
                        <span class="label">服务时间</span>
                        <span class="value">{{ formatDate(row.serviceTime) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">服务时长</span>
                        <span class="value">{{ row.duration }} 小时</span>
                      </div>
                    </div>
                    <!-- 支付信息 -->
                    <div class="info-row" v-if="row.paymentTime">
                      <div class="info-item">
                        <span class="label">支付方式</span>
                        <span class="value">{{ getPaymentMethodName(row.paymentMethod) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">支付时间</span>
                        <span class="value">{{ formatDate(row.paymentTime) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">实付金额</span>
                        <span class="value price">{{ formatAmount(row.paidAmount) }}</span>
                      </div>
                    </div>
                    
                    <!-- 退款信息 -->
                    <div class="info-row" v-if="row.refundStatus && row.refundStatus !== 0">
                      <div class="info-item">
                        <span class="label">退款状态</span>
                        <span class="value">
                          <el-tag :type="getRefundStatusType(row.refundStatus)" size="small">
                            {{ getRefundStatusText(row.refundStatus) }}
                          </el-tag>
                        </span>
                      </div>
                      <div class="info-item" v-if="row.refundAmount">
                        <span class="label">退款金额</span>
                        <span class="value price">{{ formatAmount(row.refundAmount) }}</span>
                      </div>
                    </div>
                    
                    <div class="info-row" v-if="row.cancelTime || row.completeTime">
                      <div class="info-item" v-if="row.cancelTime">
                        <span class="label">取消时间</span>
                        <span class="value">{{ formatDate(row.cancelTime) }}</span>
                      </div>
                      <div class="info-item" v-if="row.cancelReason">
                        <span class="label">取消原因</span>
                        <span class="value">{{ row.cancelReason }}</span>
                      </div>
                      <div class="info-item" v-if="row.completeTime">
                        <span class="label">完成时间</span>
                        <span class="value">{{ formatDate(row.completeTime) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 表格列配置 -->
          <el-table-column prop="id" label="订单号" min-width="120">
            <template #default="{ row }">
              <div class="cell-with-icon">
                <el-icon><Document /></el-icon>
                <span>{{ row.id }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="serviceItem.title" label="服务项目" min-width="150" />

          <el-table-column prop="staff.user.name" label="服务人员" min-width="120">
            <template #default="{ row }">
              <div class="staff-cell">
                <el-avatar 
                  :size="24" 
                  :src="fileUtils.getImageUrl(row.staff?.user?.avatar)"
                >
                  {{ getAvatarText(row.staff) }}
                </el-avatar>
                <span>{{ row.staff?.user?.name }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="serviceTime" label="服务时间" min-width="150">
            <template #default="{ row }">
              {{ formatDate(row.serviceTime) }}
            </template>
          </el-table-column>

          <el-table-column prop="totalAmount" label="金额" min-width="120">
            <template #default="{ row }">
              <span class="price">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="orderStatus" label="订单状态" min-width="100">
            <template #default="{ row }">
              <el-tag 
                :type="orderStatusMap[row.orderStatus]?.type"
                effect="light"
                class="status-tag"
              >
                {{ orderStatusMap[row.orderStatus]?.label }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="退款状态" min-width="100">
            <template #default="{ row }">
              <el-tag 
                v-if="row.refundStatus && row.refundStatus !== 0"
                :type="getRefundStatusType(row.refundStatus)"
                size="small"
                effect="light"
              >
                {{ getRefundStatusText(row.refundStatus) }}
              </el-tag>
              <span v-else class="text-secondary">-</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" fixed="right" width="220">
            <template #default="{ row }">
              <div class="action-buttons">
                <!-- 待支付：支付、取消 -->
                <el-button 
                  v-if="row.orderStatus === 1" 
                  type="primary"
                  link
                  @click="handlePay(row)"
                >
                  支付
                </el-button>
                <el-button 
                  v-if="row.orderStatus === 1" 
                  type="danger" 
                  link
                  @click="cancelOrder(row)"
                >
                  取消
                </el-button>
                
                <!-- 待接单、已接单、服务中：申请退款（未申请退款时才显示） -->
                <el-button 
                  v-if="(row.orderStatus === 2 || row.orderStatus === 3 || row.orderStatus === 4) && (!row.refundStatus || row.refundStatus === 0)" 
                  type="warning" 
                  link
                  @click="handleRefund(row)"
                >
                  申请退款
                </el-button>
                
                <!-- 已完成：评价 -->
                <el-button 
                  v-if="row.orderStatus === 5 && (!row.reviewStatus || row.reviewStatus === false)" 
                  type="primary"
                  link
                  @click="handleReview(row)"
                >
                  评价
                </el-button>
                <el-button 
                  v-if="row.orderStatus === 5 && row.reviewStatus && row.reviewStatus !== false" 
                  type="info"
                  link
                  @click="handleReview(row)"
                >
                  查看评价
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            :current-page="currentPage"
            :model-value="currentPage"
            @update:model-value="currentPage = $event"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="currentReview ? '评价详情' : '服务评价'"
      width="500px"
      class="review-dialog"
    >
      <el-form
        v-if="!currentReview"
        ref="reviewFormRef"
        :model="reviewForm"
        label-width="100px"
      >
        <el-form-item label="技能评分">
          <el-rate
            v-model="reviewForm.skillRating"
            :max="5"
            show-score
          />
        </el-form-item>
        <el-form-item label="服务态度">
          <el-rate
            v-model="reviewForm.attitudeRating"
            :max="5"
            show-score
          />
        </el-form-item>
        <el-form-item label="综合体验">
          <el-rate
            v-model="reviewForm.experienceRating"
            :max="5"
            show-score
          />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容（选填，最多1000字）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 查看评价详情 -->
      <div v-else class="review-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="技能评分">
            <el-rate
              v-model="currentReview.skillRating"
              disabled
              show-score
            />
          </el-descriptions-item>
          <el-descriptions-item label="服务态度">
            <el-rate
              v-model="currentReview.attitudeRating"
              disabled
              show-score
            />
          </el-descriptions-item>
          <el-descriptions-item label="综合体验">
            <el-rate
              v-model="currentReview.experienceRating"
              disabled
              show-score
            />
          </el-descriptions-item>
          <el-descriptions-item label="总体评分">
            <el-rate
              v-model="currentReview.overallRating"
              disabled
              show-score
            />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容">
            {{ currentReview.content || '暂无评价内容' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">关闭</el-button>
          <el-button
            v-if="!currentReview"
            type="primary"
            @click="submitReview"
          >
            提交评价
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.my-orders-page {
  min-height: 100vh;
  background-color: #F8FAFC;
  padding: 40px 0;
  padding-top: calc(40px + var(--el-header-height, 60px));
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-size: 28px;
    color: #2C3E50;
    margin: 0;
    font-weight: 600;
  }
}

.orders-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

// 表格样式
:deep(.el-table) {
  border: none;
  
  &::before {
    display: none;
  }

  .el-table__row {
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(to right, #F8FAFC, white);
      transform: translateX(4px);
    }
  }
}

// 展开详情样式
.order-detail {
  padding: 20px;
  background: #F8FAFC;
}

.detail-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    color: #2C3E50;
    font-weight: 500;
    font-size: 16px;

    .el-icon {
      color: #4A90E2;
    }
  }
}

.staff-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 8px;

  .staff-avatar {
    background: #4A90E2;
    color: white;
  }

  .staff-info {
    h3 {
      margin: 0 0 8px;
      color: #2C3E50;
      font-size: 16px;
    }

    .staff-meta {
      display: flex;
      align-items: center;
      gap: 12px;

      .experience {
        color: #64748B;
        font-size: 14px;
      }
    }
  }
}

.service-info,
.order-info {
  .info-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 16px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .info-item {
    background: #F8FAFC;
    padding: 12px 16px;
    border-radius: 6px;

    .label {
      color: #64748B;
      font-size: 14px;
      margin-bottom: 4px;
      display: block;
    }

    .value {
      color: #2C3E50;
      font-size: 15px;
    }
  }

  .service-desc {
    background: #F8FAFC;
    padding: 12px 16px;
    border-radius: 6px;

    .value {
      margin: 8px 0 0;
      line-height: 1.6;
    }
  }
}

// 表格单元格样式
.cell-with-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2C3E50;

  .el-icon {
    color: #4A90E2;
  }
}

.staff-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-secondary {
  color: #909399;
  font-size: 14px;
}

.price {
  color: #4A90E2;
  font-weight: 500;
}

.status-tag {
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

// 分页样式
.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
  background: white;
  border-top: 1px solid #EDF2F7;
}

// 响应式调整
@media (max-width: 768px) {
  .info-row {
    grid-template-columns: 1fr !important;
  }
}
</style> 