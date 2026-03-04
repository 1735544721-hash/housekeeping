<template>
  <div class="payment-result-page">
    <div class="container">
      <el-card class="result-card">
        <div class="result-content">
          <!-- 成功图标 -->
          <div class="result-icon">
            <el-icon :size="80" color="#67C23A">
              <CircleCheck />
            </el-icon>
          </div>
          
          <!-- 标题 -->
          <h1 class="result-title">支付完成</h1>
          
          <!-- 描述 -->
          <p class="result-desc">
            您已完成支付宝支付操作，请稍后查看订单状态
          </p>
          
          <!-- 提示信息 -->
          <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <ul>
                <li>支付结果将在1-3分钟内到账</li>
                <li>如支付成功，订单状态将自动更新为"待接单"</li>
                <li>如有问题，请联系客服处理</li>
              </ul>
            </template>
          </el-alert>
          
          <!-- 操作按钮 -->
          <div class="result-actions">
            <el-button size="large" @click="router.push('/')">返回首页</el-button>
            <el-button type="primary" size="large" @click="router.push('/my-orders')">
              查看订单
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style lang="scss" scoped>
.payment-result-page {
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

.result-card {
  padding: 40px;
}

.result-content {
  text-align: center;
}

.result-icon {
  margin-bottom: 24px;
}

.result-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 16px;
}

.result-desc {
  font-size: 16px;
  color: #606266;
  margin-bottom: 32px;
}

.el-alert {
  margin-bottom: 32px;
  text-align: left;
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin: 8px 0;
    }
  }
}

.result-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>


