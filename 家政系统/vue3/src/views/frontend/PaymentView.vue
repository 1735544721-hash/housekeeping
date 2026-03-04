<template>
  <div class="payment-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">订单支付</h1>
        <p class="page-desc">请选择支付方式完成支付</p>
      </div>

      <el-card class="payment-card" v-loading="loading">
        <!-- 订单信息 -->
        <div class="order-info">
          <h3 class="section-title">订单信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">
              {{ order.id }}
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(order.orderStatus)">
                {{ getStatusText(order.orderStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="服务人员">
              {{ order.staff?.user?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="服务项目">
              {{ order.serviceItem?.title }}
            </el-descriptions-item>
            <el-descriptions-item label="服务时间">
              {{ order.serviceTime }}
            </el-descriptions-item>
            <el-descriptions-item label="服务时长">
              {{ order.duration }}小时
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 支付金额 -->
        <div class="payment-amount">
          <div class="amount-label">应付金额：</div>
          <div class="amount-value">¥{{ order.totalAmount }}</div>
        </div>

        <!-- 支付方式选择 -->
        <div class="payment-methods">
          <h3 class="section-title">选择支付方式</h3>
          <el-radio-group v-model="selectedPaymentMethod" class="method-group">
            <el-radio-button value="WECHAT" class="method-item">
              <div class="method-content">
                <el-icon :size="32" color="#09BB07">
                  <ChatDotRound />
                </el-icon>
                <span class="method-name">微信支付</span>
              </div>
            </el-radio-button>
            <el-radio-button value="ALIPAY" class="method-item">
              <div class="method-content">
                <el-icon :size="32" color="#1677FF">
                  <Wallet />
                </el-icon>
                <span class="method-name">支付宝</span>
              </div>
            </el-radio-button>
            <el-radio-button value="BALANCE" class="method-item">
              <div class="method-content">
                <el-icon :size="32" color="#F56C6C">
                  <CreditCard />
                </el-icon>
                <span class="method-name">余额支付</span>
              </div>
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 支付说明 -->
        <div class="payment-notice">
          <el-alert
            title="支付说明"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <ul>
                <li>请在30分钟内完成支付，超时订单将自动取消</li>
                <li>支付成功后，服务人员将尽快接单</li>
                <li>如需退款，请在订单详情页申请</li>
              </ul>
            </template>
          </el-alert>
        </div>

        <!-- 操作按钮 -->
        <div class="payment-actions">
          <el-button size="large" @click="handleCancel">取消支付</el-button>
          <el-button 
            type="primary" 
            size="large" 
            @click="handlePay"
            :loading="paying"
            :disabled="!selectedPaymentMethod"
          >
            立即支付
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Wallet, CreditCard } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const paying = ref(false)
const order = ref({})
const selectedPaymentMethod = ref('WECHAT')

// 获取订单信息
const fetchOrderInfo = async () => {
  try {
    loading.value = true
    const orderId = route.query.orderId
    
    if (!orderId) {
      ElMessage.error('订单ID不存在')
      router.push('/my-orders')
      return
    }

    const res = await request.get(`/order/${orderId}`)
    order.value = res

    // 检查订单状态
    if (res.orderStatus !== 1) {
      ElMessage.warning('该订单不是待支付状态')
      router.push('/my-orders')
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
    ElMessage.error('获取订单信息失败')
    router.push('/my-orders')
  } finally {
    loading.value = false
  }
}

// 处理支付
const handlePay = async () => {
  if (!selectedPaymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认使用${getPaymentMethodName(selectedPaymentMethod.value)}支付 ¥${order.value.totalAmount}？`,
      '确认支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    paying.value = true

    // 如果选择支付宝支付，跳转到支付宝沙箱
    if (selectedPaymentMethod.value === 'ALIPAY') {
      const baseAPI = process.env.VUE_APP_BASE_API || '/api'
      const alipayUrl = `${baseAPI}/payment/alipay/pay?orderId=${order.value.id}`
      window.open(alipayUrl, '_blank')
      
      ElMessage.success('已打开支付宝支付页面，请在新窗口完成支付')
      
      // 提示用户支付完成后手动返回
      setTimeout(() => {
        ElMessageBox.confirm(
          '支付完成后，请点击"已完成支付"按钮返回订单列表',
          '等待支付',
          {
            confirmButtonText: '已完成支付',
            cancelButtonText: '取消支付',
            type: 'info'
          }
        ).then(() => {
          router.push('/my-orders')
        }).catch(() => {
          paying.value = false
        })
      }, 1000)
    } else {
      // 其他支付方式使用模拟支付
    await request.post('/payment/mock', null, {
        orderId: order.value.id,
        paymentMethod: selectedPaymentMethod.value
      },{
        successMsg: '支付成功',
        onSuccess: () => {
    // 支付成功，跳转到订单列表
    ElMessageBox.alert(
      '支付成功！服务人员将尽快接单，请耐心等待。',
      '支付成功',
      {
        confirmButtonText: '查看订单',
        type: 'success',
        callback: () => {
          router.push('/my-orders')
        }
      }
    )
        }
      })
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
    }
  } finally {
    if (selectedPaymentMethod.value !== 'ALIPAY') {
    paying.value = false
    }
  }
}

// 取消支付
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确认取消支付？取消后订单将被关闭。',
      '取消支付',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '继续支付',
        type: 'warning'
      }
    )

    await request.post(`/payment/cancel/${order.value.id}`, null, {
      successMsg: '已取消支付'
    })

    router.push('/my-orders')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消支付失败:', error)
    }
  }
}

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const names = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'BALANCE': '余额支付'
  }
  return names[method] || method
}

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '待支付',
    2: '待接单',
    3: '已接单',
    4: '服务中',
    5: '已完成',
    6: '已取消',
    7: '已关闭'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'info',
    3: 'primary',
    4: 'primary',
    5: 'success',
    6: 'info',
    7: 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  fetchOrderInfo()
})
</script>

<style lang="scss" scoped>
.payment-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 0;
  padding-top: calc(40px + var(--el-header-height, 60px));
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 15px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.page-desc {
  color: #909399;
  font-size: 14px;
}

.payment-card {
  padding: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
}

.order-info {
  margin-bottom: 32px;
}

.payment-amount {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  padding: 24px;
  background-color: #fff9f9;
  border-radius: 8px;
  margin-bottom: 32px;

  .amount-label {
    font-size: 16px;
    color: #303133;
    margin-right: 12px;
  }

  .amount-value {
    font-size: 32px;
    font-weight: 600;
    color: #f56c6c;
  }
}

.payment-methods {
  margin-bottom: 32px;

  .method-group {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .method-item {
    flex: 1;
    min-width: 150px;
    height: auto;

    :deep(.el-radio-button__inner) {
      width: 100%;
      height: 100px;
      padding: 16px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .method-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;

      .method-name {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

.payment-notice {
  margin-bottom: 32px;

  ul {
    margin: 0;
    padding-left: 20px;

    li {
      margin: 8px 0;
      color: #606266;
      font-size: 14px;
    }
  }
}

.payment-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-light);
}
</style>
