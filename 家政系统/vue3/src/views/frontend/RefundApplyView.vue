<template>
  <div class="refund-apply-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">申请退款</h1>
        <p class="page-desc">请填写退款信息</p>
      </div>

      <el-card class="refund-card" v-loading="loading">
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
            <el-descriptions-item label="支付方式">
              {{ getPaymentMethodName(order.paymentMethod) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付时间">
              {{ order.paymentTime }}
            </el-descriptions-item>
            <el-descriptions-item label="实付金额" :span="2">
              <span class="amount-text">¥{{ order.paidAmount }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 退款表单 -->
        <div class="refund-form">
          <h3 class="section-title">退款信息</h3>
          <el-form
            ref="formRef"
            :model="refundForm"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item label="退款金额">
              <el-input
                :value="`¥${order.paidAmount}`"
                disabled
              >
                <template #append>全额退款</template>
              </el-input>
            </el-form-item>

            <el-form-item label="退款类型" prop="refundType">
              <el-radio-group v-model="refundForm.refundType">
                <el-radio :label="1">用户取消</el-radio>
                <el-radio :label="4">服务纠纷</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="退款原因" prop="refundReason">
              <el-input
                v-model="refundForm.refundReason"
                type="textarea"
                :rows="4"
                placeholder="请详细说明退款原因，以便我们更好地为您服务"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 退款说明 -->
        <div class="refund-notice">
          <el-alert
            title="退款说明"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              <ul>
                <li>退款申请提交后，将由管理员审核</li>
                <li>审核通过后，退款将在1-3个工作日内原路退回</li>
                <li>退款成功后，订单将自动取消</li>
                <li>如有疑问，请联系客服</li>
              </ul>
            </template>
          </el-alert>
        </div>

        <!-- 操作按钮 -->
        <div class="refund-actions">
          <el-button size="large" @click="handleCancel">取消</el-button>
          <el-button
            type="primary"
            size="large"
            @click="handleSubmit"
            :loading="submitting"
          >
            提交申请
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const submitting = ref(false)
const order = ref({})
const formRef = ref(null)

const refundForm = reactive({
  refundType: 1,
  refundReason: ''
})

const rules = {
  refundType: [
    { required: true, message: '请选择退款类型', trigger: 'change' }
  ],
  refundReason: [
    { required: true, message: '请填写退款原因', trigger: 'blur' },
    { min: 10, message: '退款原因至少10个字', trigger: 'blur' }
  ]
}

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
    if (res.orderStatus === 1) {
      ElMessage.warning('待支付订单无需退款，可直接取消')
      router.push('/my-orders')
      return
    }

    if (res.orderStatus === 5) {
      ElMessage.warning('已完成的订单不支持退款')
      router.push('/my-orders')
      return
    }

    if (res.orderStatus === 6) {
      ElMessage.warning('订单已取消')
      router.push('/my-orders')
      return
    }

    if (!res.paymentTime) {
      ElMessage.warning('订单未支付，无需退款')
      router.push('/my-orders')
      return
    }

    // 检查是否已有退款申请
    if (res.refundStatus && res.refundStatus !== 0) {
      ElMessage.warning('该订单已有退款申请')
      router.push('/my-orders')
      return
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
    ElMessage.error('获取订单信息失败')
    router.push('/my-orders')
  } finally {
    loading.value = false
  }
}

// 提交退款申请
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    await ElMessageBox.confirm(
      `确认申请退款 ¥${order.value.paidAmount}？`,
      '确认退款',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    await request.post('/refund/apply', null, {
      params: {
        orderId: order.value.id,
        userId: userStore.userInfo.id,
        refundReason: refundForm.refundReason,
        refundType: refundForm.refundType
      },
      successMsg: '退款申请已提交'
    })

    ElMessageBox.alert(
      '退款申请已提交，请等待管理员审核。审核结果将通过站内消息通知您。',
      '申请成功',
      {
        confirmButtonText: '查看订单',
        type: 'success',
        callback: () => {
          router.push('/my-orders')
        }
      }
    )
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交退款申请失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const names = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'BALANCE': '余额支付'
  }
  return names[method] || '未知'
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
.refund-apply-page {
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

.refund-card {
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

  .amount-text {
    font-size: 18px;
    font-weight: 600;
    color: #f56c6c;
  }
}

.refund-form {
  margin-bottom: 32px;
}

.refund-notice {
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

.refund-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-light);
}
</style>
