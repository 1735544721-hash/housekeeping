<template>
  <div class="refund-list-page">
    <div class="page-header">
      <h2>退款管理</h2>
      <p class="page-desc">管理和审核用户的退款申请</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="退款状态">
          <el-select v-model="filterForm.refundStatus" placeholder="全部状态" clearable>
            <el-option label="待审核" :value="1" />
            <el-option label="审核通过" :value="2" />
            <el-option label="审核拒绝" :value="3" />
            <el-option label="退款中" :value="4" />
            <el-option label="已退款" :value="5" />
            <el-option label="退款失败" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="filterForm.userId" placeholder="输入用户ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 退款列表 -->
    <el-card class="table-card">
      <el-table 
        :data="refundList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="退款ID" width="80" />
        
        <el-table-column prop="orderId" label="订单ID" width="100" />
        
        <el-table-column label="用户信息" width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div>{{ row.user?.name }}</div>
              <div class="text-secondary">{{ row.user?.phone }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="订单信息" min-width="200">
          <template #default="{ row }">
            <div class="order-info">
              <div>服务人员：{{ row.order?.staff?.user?.name }}</div>
              <div>服务项目：{{ row.order?.serviceItem?.title }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="refundAmount" label="退款金额" width="120">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.refundAmount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="退款类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getRefundTypeColor(row.refundType)">
              {{ getRefundTypeName(row.refundType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="退款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.refundStatus)">
              {{ getStatusText(row.refundStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.refundStatus === 1"
              type="primary" 
              link 
              @click="handleAudit(row, 2)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.refundStatus === 1"
              type="danger" 
              link 
              @click="handleAudit(row, 3)"
            >
              拒绝
            </el-button>
            <el-button 
              type="info" 
              link 
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 退款详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="退款详情"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border v-if="currentRefund">
        <el-descriptions-item label="退款ID">
          {{ currentRefund.id }}
        </el-descriptions-item>
        <el-descriptions-item label="订单ID">
          {{ currentRefund.orderId }}
        </el-descriptions-item>
        <el-descriptions-item label="用户姓名">
          {{ currentRefund.user?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentRefund.user?.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <span class="amount-text">¥{{ currentRefund.refundAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="退款类型">
          <el-tag :type="getRefundTypeColor(currentRefund.refundType)">
            {{ getRefundTypeName(currentRefund.refundType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <el-tag :type="getStatusType(currentRefund.refundStatus)">
            {{ getStatusText(currentRefund.refundStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDate(currentRefund.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">
          {{ currentRefund.refundReason }}
        </el-descriptions-item>
        
        <template v-if="currentRefund.auditUserId">
          <el-descriptions-item label="审核人">
            {{ currentRefund.auditUser?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDate(currentRefund.auditTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">
            {{ currentRefund.auditRemark || '无' }}
          </el-descriptions-item>
        </template>

        <template v-if="currentRefund.refundTime">
          <el-descriptions-item label="退款完成时间" :span="2">
            {{ formatDate(currentRefund.refundTime) }}
          </el-descriptions-item>
        </template>
      </el-descriptions>

      <!-- 订单信息 -->
      <el-divider content-position="left">订单信息</el-divider>
      <el-descriptions :column="2" border v-if="currentRefund?.order">
        <el-descriptions-item label="服务人员">
          {{ currentRefund.order.staff?.user?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="服务项目">
          {{ currentRefund.order.serviceItem?.title }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          ¥{{ currentRefund.order.totalAmount }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          ¥{{ currentRefund.order.paidAmount }}
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ getPaymentMethodName(currentRefund.order.paymentMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">
          {{ formatDate(currentRefund.order.paymentTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentRefund?.refundStatus === 1"
            type="danger"
            @click="handleAudit(currentRefund, 3)"
          >
            拒绝退款
          </el-button>
          <el-button 
            v-if="currentRefund?.refundStatus === 1"
            type="primary"
            @click="handleAudit(currentRefund, 2)"
          >
            通过退款
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditResult === 2 ? '通过退款' : '拒绝退款'"
      width="500px"
    >
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="退款金额">
          <span class="amount-text">¥{{ currentRefund?.refundAmount }}</span>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="4"
            :placeholder="auditResult === 2 ? '请输入审核通过的备注（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmAudit"
            :loading="auditing"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { formatDate } from '@/utils/dateUtils'

const userStore = useUserStore()

const loading = ref(false)
const auditing = ref(false)
const refundList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterForm = reactive({
  refundStatus: null,
  userId: ''
})

const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const currentRefund = ref(null)
const auditResult = ref(2) // 2:通过, 3:拒绝

const auditForm = reactive({
  auditRemark: ''
})

// 获取退款列表
const fetchRefundList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      refundStatus: filterForm.refundStatus,
      userId: filterForm.userId || null
    }

    const res = await request.get('/refund/list', params)
    refundList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取退款列表失败:', error)
    ElMessage.error('获取退款列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRefundList()
}

// 重置
const handleReset = () => {
  filterForm.refundStatus = null
  filterForm.userId = ''
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRefundList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRefundList()
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await request.get(`/refund/${row.id}`)
    currentRefund.value = res
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取退款详情失败:', error)
    ElMessage.error('获取退款详情失败')
  }
}

// 审核退款
const handleAudit = (row, result) => {
  currentRefund.value = row
  auditResult.value = result
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

// 确认审核
const confirmAudit = async () => {
  try {
    const action = auditResult.value === 2 ? '通过' : '拒绝'
    
    await ElMessageBox.confirm(
      `确认${action}该退款申请？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    auditing.value = true

    await request.post(`/refund/audit/${currentRefund.value.id}`, null, {
      params: {
        auditUserId: userStore.userInfo.id,
        auditResult: auditResult.value,
        auditRemark: auditForm.auditRemark || ''
      },
      successMsg: `退款申请已${action}`
    })

    auditDialogVisible.value = false
    detailDialogVisible.value = false
    fetchRefundList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  } finally {
    auditing.value = false
  }
}

// 获取退款状态文本
const getStatusText = (status) => {
  const map = {
    1: '待审核',
    2: '审核通过',
    3: '审核拒绝',
    4: '退款中',
    5: '已退款',
    6: '退款失败'
  }
  return map[status] || '未知'
}

// 获取退款状态类型
const getStatusType = (status) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'primary',
    5: 'success',
    6: 'danger'
  }
  return map[status] || 'info'
}

// 获取退款类型名称
const getRefundTypeName = (type) => {
  const map = {
    1: '用户取消',
    2: '服务人员取消',
    3: '超时未接单',
    4: '服务纠纷'
  }
  return map[type] || '未知'
}

// 获取退款类型颜色
const getRefundTypeColor = (type) => {
  const map = {
    1: 'info',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  }
  return map[type] || 'info'
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

onMounted(() => {
  fetchRefundList()
})
</script>

<style lang="scss" scoped>
.refund-list-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: #303133;
  }

  .page-desc {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  .user-info,
  .order-info {
    font-size: 13px;

    div {
      line-height: 1.6;
    }

    .text-secondary {
      color: #909399;
    }
  }

  .amount-text {
    font-size: 16px;
    font-weight: 600;
    color: #f56c6c;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
