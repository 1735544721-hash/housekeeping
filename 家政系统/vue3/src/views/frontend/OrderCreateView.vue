<template>
  <div class="order-create">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">服务预约</h1>
        <p class="page-desc">请确认服务信息并选择服务时间</p>
      </div>

      <el-card class="order-card">
        <!-- 服务人员信息 -->
        <div class="staff-info">
          <div class="staff-header">
            <el-avatar 
              :size="64" 
              :src="fileUtils.getImageUrl(staff?.user?.avatar)"
            >{{ staff?.user?.name?.charAt(0) }}</el-avatar>
            <div class="staff-meta">
              <h3>{{ staff?.user?.name }}</h3>
              <div class="staff-rating">
                <el-rate v-model="staff.rating" disabled show-score />
                <span class="experience">{{ staff.experience }}年经验</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 服务项目信息 -->
        <div class="service-info">
          <h4 class="section-title">服务项目</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="服务名称">
              {{ service.title }}
            </el-descriptions-item>
            <el-descriptions-item label="服务价格">
              ¥{{ service.price }}/小时
            </el-descriptions-item>
            <el-descriptions-item label="服务类别" :span="2">
              {{ service.category?.categoryName }}
            </el-descriptions-item>
            <el-descriptions-item label="服务说明" :span="2">
              {{ service.description || '暂无说明' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 预约表单 -->
        <div class="order-form">
          <h4 class="section-title">预约信息</h4>
          <el-form 
            ref="formRef"
            :model="orderForm"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item label="服务时间" prop="serviceDate">
              <el-date-picker
                v-model="orderForm.serviceDate"
                type="datetime"
                placeholder="选择服务时间"
                :disabled-date="disabledDate"
                :disabled-hours="disabledHours"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
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

            <el-form-item label="联系电话" prop="phone">
              <el-input 
                v-model="orderForm.phone" 
                placeholder="请输入联系电话"
                :disabled="!!userStore.userInfo?.phone"
              />
            </el-form-item>

            <el-form-item label="服务地址" prop="address">
              <el-input 
                v-model="orderForm.address" 
                placeholder="请输入服务地址"
                :disabled="!!userStore.userInfo?.address"
              />
            </el-form-item>

            <el-form-item label="备注说明" prop="remark">
              <el-input 
                v-model="orderForm.remark"
                type="textarea"
                rows="3"
                placeholder="请输入其他要求或说明"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 订单金额 -->
        <div class="order-amount">
          <div class="amount-detail">
            <span class="label">订单金额：</span>
            <span class="value">¥{{ calculateTotalAmount }}</span>
          </div>
          <div class="amount-desc">
            {{ orderForm.duration }}小时 × ¥{{ service.price }}/小时
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="handleCancel">取消预约</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认预约
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const staff = ref({})
const service = ref({})
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const orderForm = reactive({
  serviceDate: '',
  duration: 2,
  phone: userStore.userInfo?.phone || '',
  address: userStore.userInfo?.address || '',
  remark: ''
})

// 表单验证规则
const rules = {
  serviceDate: [
    { required: true, message: '请选择服务时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请选择服务时长', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入服务地址', trigger: 'blur' }
  ]
}

// 计算订单总金额
const calculateTotalAmount = computed(() => {
  if (!service.value?.price || !orderForm.duration) return 0
  return (service.value.price * orderForm.duration).toFixed(2)
})

// 日期限制：只能选择今天之后的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
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

// 获取服务人员和服务项目信息
const fetchInitialData = async () => {
  try {
    const [staffRes, serviceRes] = await Promise.all([
      request.get(`/staff/${route.query.staffId}`),
      request.get(`/service/${route.query.serviceId}`)
    ])
    staff.value = staffRes
    service.value = serviceRes
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败，请重试')
    router.push('/staff')
  }
}

// 提交订单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
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

    submitting.value = true
    
    // 直接使用Element Plus组件格式化后的时间字符串
    const serviceDateTime = orderForm.serviceDate
    
    // 构建附加信息为JSON格式，使用中文key
    const remarkData = {
      联系电话: orderForm.phone,
      服务地址: orderForm.address,
      备注说明: orderForm.remark || ''
    }
    
    // 构建订单数据
    const orderData = {
      userId: userStore.userInfo.id,
      staffId: staff.value.id,
      serviceId: service.value.id,
      serviceTime: serviceDateTime,
      duration: orderForm.duration,
      totalAmount: calculateTotalAmount.value,
      remark: JSON.stringify(remarkData)
    }

    // 创建订单
    const result = await request.post('/order', orderData, {
      successMsg: '订单创建成功，请完成支付'
    })

    // 跳转到支付页面（result 就是订单对象）
    router.push({
      path: '/payment',
      query: { orderId: result.id }
    })
  } catch (error) {
    if (error.type === 'cancel') {
      // 用户取消确认，不做处理
      return
    }
    
    // 处理业务错误
    if (error.type === 'business') {
      // 检查是否是服务关联错误
      if (error.message && error.message.includes('不提供此服务项目')) {
        ElMessage.error({
          message: '预约失败：该服务人员不提供此服务项目，请返回重新选择',
          duration: 5000,
          showClose: true
        })
        // 延迟跳转回服务人员列表
        setTimeout(() => {
          router.push('/staff')
        }, 2000)
      } else if (error.message && error.message.includes('时间段已被预约')) {
        ElMessage.error({
          message: '预约失败：该服务人员在所选时间段已被预约，请选择其他时间',
          duration: 5000,
          showClose: true
        })
      } else {
        ElMessage.error(error.message || '创建订单失败，请重试')
      }
    } else {
      console.error('创建订单失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 取消预约
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  if (!route.query.staffId || !route.query.serviceId) {
    ElMessage.error('参数错误')
    router.push('/staff')
    return
  }
  fetchInitialData()
})
</script>

<style lang="scss" scoped>
.order-create {
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

.order-card {
  padding: 24px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px;
    padding-left: 10px;
    border-left: 3px solid var(--el-color-primary);
  }
}

.staff-info {
  margin-bottom: 32px;
  
  .staff-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .staff-meta {
    h3 {
      font-size: 18px;
      color: #303133;
      margin: 0 0 8px;
    }

    .staff-rating {
      display: flex;
      align-items: center;
      gap: 12px;

      .experience {
        color: #909399;
        font-size: 13px;
      }
    }
  }
}

.service-info {
  margin-bottom: 32px;
  
  :deep(.el-descriptions) {
    padding: 16px;
    background-color: #f8fafc;
    border-radius: 8px;
  }
}

.order-form {
  margin-bottom: 32px;
}

.order-amount {
  margin: 24px 0;
  padding: 16px;
  background-color: #fff9f9;
  border-radius: 8px;

  .amount-detail {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-bottom: 8px;

    .label {
      font-size: 16px;
      color: #303133;
    }

    .value {
      font-size: 24px;
      font-weight: 600;
      color: #f56c6c;
      margin-left: 8px;
    }
  }

  .amount-desc {
    text-align: right;
    color: #909399;
    font-size: 13px;
  }
}

.order-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-light);
}
</style> 