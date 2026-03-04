<template>
  <div class="order-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">订单管理</h2>
          <el-tag type="info" effect="plain" class="order-count">
            {{ total }} 个订单
          </el-tag>
        </div>
        <div class="right">
          <el-button 
            v-if="selectedRows.length > 0" 
            type="danger" 
            @click="handleBatchDelete">
            批量删除
          </el-button>
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>

      <!-- 可折叠的搜索区域 -->
      <el-card class="search-card" shadow="never">
        <template #header>
          <div class="search-header" @click="toggleSearch">
            <div class="title">
              <el-icon><Search /></el-icon>
              <span>筛选条件</span>
            </div>
            <div class="arrow" :class="{ 'is-active': showSearch }">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
        </template>
        <div class="search-content" :class="{ collapsed: !showSearch }" v-show="showSearch">
          <el-form :model="searchForm" ref="searchFormRef" :inline="true" class="search-form">
            <template v-if="isAdmin">
              <el-form-item label="用户ID" prop="userId">
                <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
              </el-form-item>
              <el-form-item label="服务人员ID" prop="staffId">
                <el-input v-model="searchForm.staffId" placeholder="请输入服务人员ID" clearable />
              </el-form-item>
            </template>
            <el-form-item label="订单状态" prop="status">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px">
                <el-option v-for="(label, value) in orderStatusMap" 
                  :key="value" 
                  :label="label" 
                  :value="Number(value)" 
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>查询
              </el-button>
              <el-button @click="resetSearch">
                <el-icon><Refresh /></el-icon>重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 表格区域 -->
      <div class="table-container">
        <el-table 
          v-loading="loading" 
          :data="tableData" 
          border 
          @selection-change="handleSelectionChange"
          style="width: 100%"
          row-key="id"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div v-if="row.review" class="expand-review">
                <div class="review-header">
                  <el-icon><StarFilled /></el-icon>
                  <span>评价信息</span>
                </div>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="服务技能">
                    <div class="rating-item">
                      <el-rate v-model="row.review.skillRating" disabled />
                      <span class="rating-score">{{ row.review.skillRating }}分</span>
                    </div>
                  </el-descriptions-item>
                  <el-descriptions-item label="服务态度">
                    <div class="rating-item">
                      <el-rate v-model="row.review.attitudeRating" disabled />
                      <span class="rating-score">{{ row.review.attitudeRating }}分</span>
                    </div>
                  </el-descriptions-item>
                  <el-descriptions-item label="服务体验">
                    <div class="rating-item">
                      <el-rate v-model="row.review.experienceRating" disabled />
                      <span class="rating-score">{{ row.review.experienceRating }}分</span>
                    </div>
                  </el-descriptions-item>
                  <el-descriptions-item label="综合评分">
                    <div class="rating-item">
                      <el-rate v-model="row.review.overallRating" disabled allow-half />
                      <span class="rating-score">{{ row.review.overallRating }}分</span>
                    </div>
                  </el-descriptions-item>
                  <el-descriptions-item label="评价内容" :span="2">
                    {{ row.review.content }}
                  </el-descriptions-item>
                  <el-descriptions-item label="评价时间" :span="2">
                    {{ formatDate(row.review.createTime) }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div v-else class="no-review">
                <el-empty description="暂无评价信息" />
              </div>
            </template>
          </el-table-column>
          <el-table-column type="selection" width="55" />
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="订单号" min-width="120" />
          <el-table-column v-if="isAdmin" label="用户信息" min-width="150">
            <template #default="{ row }">
              {{ row.user?.name }} (ID: {{ row.userId }})
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="服务人员" min-width="150">
            <template #default="{ row }">
              {{ row.staff?.name }} (ID: {{ row.staffId }})
            </template>
          </el-table-column>
          <el-table-column prop="serviceItem?.title" label="服务项目" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.serviceItem?.title }}
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="订单金额" min-width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column label="支付信息" min-width="140">
            <template #default="{ row }">
              <div v-if="row.paymentTime" class="payment-info">
                <el-tag size="small" type="success">{{ getPaymentMethodName(row.paymentMethod) }}</el-tag>
                <div class="payment-amount">¥{{ row.paidAmount }}</div>
              </div>
              <el-tag v-else size="small" type="info">未支付</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="退款状态" min-width="100">
            <template #default="{ row }">
              <el-tag v-if="row.refundStatus === 1" size="small" type="warning">退款中</el-tag>
              <el-tag v-else-if="row.refundStatus === 2" size="small" type="success">已退款</el-tag>
              <el-tag v-else-if="row.refundStatus === 3" size="small" type="danger">退款失败</el-tag>
              <el-tag v-else size="small" type="info">无退款</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="serviceTime" label="服务时间" min-width="160">
            <template #default="{ row }">
              {{ formatDate(row.serviceTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderStatus" label="订单状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="getOrderStatusType(row.orderStatus)" effect="plain">
                {{ orderStatusMap[row.orderStatus] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
              <el-button 
                v-if="canUpdateStatus(row)"
                type="warning" 
                link 
                @click="handleUpdateStatus(row)"
              >变更状态</el-button>
              <el-button 
                v-if="canDelete(row)"
                type="danger" 
                link 
                @click="handleDelete(row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="val => currentPage = val"
          @update:page-size="val => pageSize = val"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      title="订单详情"
      v-model="detailVisible"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder?.id }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getOrderStatusType(currentOrder?.orderStatus)">
            {{ orderStatusMap[currentOrder?.orderStatus] }}
          </el-tag>
        </el-descriptions-item>
        <template v-if="isAdmin">
          <el-descriptions-item label="用户信息">
            {{ currentOrder?.user?.name }} (ID: {{ currentOrder?.userId }})
          </el-descriptions-item>
          <el-descriptions-item label="服务人员">
            {{ currentOrder?.staff?.name }} (ID: {{ currentOrder?.staffId }})
          </el-descriptions-item>
        </template>
        <el-descriptions-item label="服务项目">
          {{ currentOrder?.serviceItem?.title }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          ¥{{ currentOrder?.totalAmount }}
        </el-descriptions-item>
        <el-descriptions-item label="支付方式" v-if="currentOrder?.paymentMethod">
          {{ getPaymentMethodName(currentOrder?.paymentMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="支付时间" v-if="currentOrder?.paymentTime">
          {{ formatDate(currentOrder?.paymentTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额" v-if="currentOrder?.paidAmount">
          ¥{{ currentOrder?.paidAmount }}
        </el-descriptions-item>
        <el-descriptions-item label="退款状态" v-if="currentOrder?.refundStatus">
          <el-tag v-if="currentOrder.refundStatus === 1" type="warning">退款中</el-tag>
          <el-tag v-else-if="currentOrder.refundStatus === 2" type="success">已退款 ¥{{ currentOrder.refundAmount }}</el-tag>
          <el-tag v-else-if="currentOrder.refundStatus === 3" type="danger">退款失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务时间">
          {{ formatDate(currentOrder?.serviceTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="服务时长">
          {{ currentOrder?.duration }} 小时
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(currentOrder?.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 备注信息单独使用一个 el-descriptions -->
      <el-descriptions :column="1" border style="margin-top: 20px">
        <el-descriptions-item label="备注">
          <div v-if="parseRemark(currentOrder?.remark).length">
            <div v-for="([key, value]) in parseRemark(currentOrder?.remark)" :key="key" class="remark-item">
              <strong>{{ key }}:</strong> {{ value }}
            </div>
          </div>
          <div v-else>暂无信息</div>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 取消原因等信息也单独一个 el-descriptions -->
      <el-descriptions v-if="currentOrder?.orderStatus === 6" :column="1" border style="margin-top: 20px">
        <el-descriptions-item label="取消原因">
          {{ currentOrder?.cancelReason || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="取消时间">
          {{ formatDate(currentOrder?.cancelTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 评价信息表格 -->
      <div v-if="currentOrder?.review" class="review-section">
        <div class="review-header">
          <el-icon><StarFilled /></el-icon>
          <span>评价信息</span>
        </div>
        <el-card class="review-card" shadow="never">
          <div class="review-ratings">
            <div class="rating-item">
              <span class="rating-label">服务技能</span>
              <div class="rating-content">
                <el-rate v-model="currentOrder.review.skillRating" disabled />
                <span class="rating-score">{{ currentOrder.review.skillRating }}分</span>
              </div>
            </div>
            <div class="rating-item">
              <span class="rating-label">服务态度</span>
              <div class="rating-content">
                <el-rate v-model="currentOrder.review.attitudeRating" disabled />
                <span class="rating-score">{{ currentOrder.review.attitudeRating }}分</span>
              </div>
            </div>
            <div class="rating-item">
              <span class="rating-label">服务体验</span>
              <div class="rating-content">
                <el-rate v-model="currentOrder.review.experienceRating" disabled />
                <span class="rating-score">{{ currentOrder.review.experienceRating }}分</span>
              </div>
            </div>
            <div class="rating-item overall">
              <span class="rating-label">综合评分</span>
              <div class="rating-content">
                <el-rate v-model="currentOrder.review.overallRating" disabled allow-half />
                <span class="rating-score">{{ currentOrder.review.overallRating }}分</span>
              </div>
            </div>
          </div>
          <div class="review-divider"></div>
          <div class="review-comment">
            <div class="comment-label">评价内容：</div>
            <div class="comment-content">{{ currentOrder.review.content || '未填写评价内容' }}</div>
            <div class="review-time">评价时间：{{ formatDate(currentOrder.review.createTime) }}</div>
          </div>
        </el-card>
      </div>
    </el-dialog>

    <!-- 状态更新对话框 -->
    <el-dialog
      title="更新订单状态"
      v-model="statusDialogVisible"
      width="500px"
    >
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="getOrderStatusType(currentOrder?.orderStatus)">
            {{ orderStatusMap[currentOrder?.orderStatus] }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择新状态">
            <el-option 
              v-for="status in getAvailableStatuses(currentOrder?.orderStatus)"
              :key="status"
              :label="orderStatusMap[status]"
              :value="status"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="取消原因" prop="reason" v-if="statusForm.status === 6">
          <el-input 
            v-model="statusForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusUpdate">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ArrowDown, StarFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

// 搜索表单
const searchForm = reactive({
  userId: '',
  staffId: '',
  status: '',
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

// 搜索区域显示控制
const showSearch = ref(true)
const searchFormRef = ref(null)

// 订单状态映射
const orderStatusMap = {
  1: '待支付',
  2: '待接单',
  3: '已接单',
  4: '服务中',
  5: '已完成',
  6: '已取消',
  // 7: '已关闭'
}

// 状态标签类型映射
const statusTypeMap = {
  1: 'warning',   // 待支付
  2: 'info',      // 待接单
  3: 'primary',   // 已接单
  4: 'primary',   // 服务中
  5: 'success',   // 已完成
  6: 'danger',    // 已取消
  // 7: 'info'       // 已关闭
}

// 方法定义
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const handleRefresh = () => {
  searchForm.userId = ''
  searchForm.staffId = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchOrders()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

const resetSearch = () => {
  searchFormRef.value?.resetFields()
  handleSearch()
}

// 格式化日期
const formatDate = (date) => {
  return date ? dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss') : ''
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  return statusTypeMap[status] || 'info'
}

// 获取订单列表
const fetchOrders = async () => {
  let staffId = undefined;
  if(userStore.isStaff){
    staffId = userStore.staffInfo?.id
  }else{
    staffId = searchForm.staffId
  }
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm,
      staffId: staffId
    }

    await request.get('/order/list', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } finally {
    loading.value = false
  }
}

// 解析备注信息
const parseRemark = (remark) => {
  try {
    const parsed = JSON.parse(remark);
    return Object.entries(parsed); // 转换为键值对数组
  } catch (error) {
    console.error('解析备注失败:', error);
    return [];
  }
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

// 详情对话框
const detailVisible = ref(false)
const currentOrder = ref(null)

// 状态更新对话框
const statusDialogVisible = ref(false)
const statusFormRef = ref(null)
const statusForm = reactive({
  status: '',
  reason: ''
})

const statusRules = {
  status: [
    { required: true, message: '请选择目标状态', trigger: 'change' }
  ],
  reason: [
    { 
      required: true, 
      message: '请输入取消原因', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (statusForm.status === 6 && (!value || value.trim() === '')) {
          callback(new Error('取消订单时必须填写取消原因'))
        } else {
          callback()
        }
      }
    },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ]
}

// 判断是否可以更新状态
const canUpdateStatus = (row) => {
  // 已完成、已取消、已关闭的订单不能更新状态
  if ([5, 6, 7].includes(row.orderStatus)) {
    return false
  }
  
  // 如果是管理员，可以操作所有订单
  if (isAdmin.value) return true
  
  // 服务人员只能处理自己的订单
  return row.staffId === userStore.staffInfo?.id
}

// 判断是否可以删除订单
const canDelete = (row) => {
  // 只有已完成、已取消、已关闭的订单可以删除
  return [5, 6, 7].includes(row.orderStatus)
}

// 删除订单
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该订单吗？此操作将不可逆！',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.delete(`/order/${row.id}`, {
        successMsg: '删除成功'
      })
      fetchOrders()
    } catch (error) {
      console.error('删除订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 批量删除订单
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的订单')
    return
  }
  
  // 检查选中的订单是否都可以删除
  const nonDeletable = selectedRows.value.filter(row => !canDelete(row))
  if (nonDeletable.length > 0) {
    ElMessage.warning('只能删除已完成、已取消或已关闭的订单')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个订单吗？此操作将不可逆！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const ids = selectedRows.value.map(row => row.id)
      await request.delete('/order/batch', {
        data: ids,
        successMsg: '批量删除成功'
      })
      fetchOrders()
    } catch (error) {
      console.error('批量删除订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 获取可用的目标状态
const getAvailableStatuses = (currentStatus) => {
  const statusMap = {
    1: [2, 6],    // 待支付 -> 待接单、已取消
    2: [3, 6],    // 待接单 -> 已接单、已取消
    3: [4, 6],    // 已接单 -> 服务中、已取消
    4: [5, 6]     // 服务中 -> 已完成、已取消
  }
  return statusMap[currentStatus] || []
}

// 更新状态
const handleUpdateStatus = (row) => {
  currentOrder.value = row
  statusForm.status = ''
  statusForm.reason = ''
  statusDialogVisible.value = true
}

// 提交状态更新
const submitStatusUpdate = async () => {
  if (!statusFormRef.value) return
  
  try {
    await statusFormRef.value.validate()
    
    // 如果是取消订单，调用专门的取消接口
    if (statusForm.status === 6) {
      await request.put(`/order/${currentOrder.value.id}/cancel?reason=${statusForm.reason}`, {
        successMsg: '订单取消成功'
      })
    } else {
      // 其他状态更新
      await request.put(`/order/${currentOrder.value.id}/status?status=${statusForm.status}`, {
        successMsg: '状态更新成功'
      })
    }
    
    statusDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 查看详情
const handleDetail = async (row) => {
  try {
    await request.get(`/order/${row.id}`, null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        currentOrder.value = res
        detailVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchOrders() // 加载订单列表
})
</script>

<style lang="scss" scoped>
.order-list {
  padding: 16px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .action-card {
    background: #fff;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden;
    flex: 1;

    :deep(.el-card__body) {
      padding: 20px;
      width: 100%;
      max-width: 100%;
      box-sizing: border-box;
      overflow: hidden;
    }

    .action-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .left {
        display: flex;
        align-items: center;
        gap: 12px;

        .page-title {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
      }

      .right {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }

  .search-card {
    margin-bottom: 16px;
    border: none;
    background-color: #f8fafc;

    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;
      user-select: none;

      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        color: #475569;
        font-weight: 500;
      }

      .arrow {
        transition: transform 0.3s;

        &.is-active {
          transform: rotate(180deg);
        }
      }
    }

    .search-content {
      transition: all 0.3s;
      
      &.collapsed {
        margin-top: -200px;
        opacity: 0;
      }
    }

    .search-form {
      padding: 16px 0 0;
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
    }
  }

  .table-container {
    width: 100%;
    overflow-x: auto;
    
    :deep(.el-table) {
      width: 100% !important;

      .el-table__header,
      .el-table__body {
        width: 100% !important;
        table-layout: fixed !important;
      }

      .el-table__cell {
        white-space: nowrap;
      }

      .el-table__fixed {
        height: 100% !important;
      }

      .el-table__body-wrapper {
        overflow-x: auto !important;
      }
    }
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .review-section {
    margin-top: 24px;

    .review-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 500;
      color: #475569;

      .el-icon {
        color: #f59e0b;
      }
    }

    .review-card {
      background-color: #f8fafc;
      border: 1px solid #e2e8f0;

      .review-ratings {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 16px;
        padding: 16px;

        .rating-item {
          display: flex;
          flex-direction: column;
          gap: 8px;

          &.overall {
            grid-column: 1 / -1;
            border-top: 1px dashed #e2e8f0;
            padding-top: 16px;
            margin-top: 8px;

            .rating-label {
              color: #1e293b;
              font-weight: 500;
            }

            .rating-score {
              color: #f59e0b;
              font-size: 16px;
              font-weight: 600;
            }
          }

          .rating-label {
            color: #64748b;
            font-size: 14px;
          }

          .rating-content {
            display: flex;
            align-items: center;
            gap: 8px;

            :deep(.el-rate) {
              .el-rate__icon {
                font-size: 18px;
              }
            }

            .rating-score {
              color: #f59e0b;
              font-weight: 500;
            }
          }
        }
      }

      .review-divider {
        height: 1px;
        background-color: #e2e8f0;
        margin: 0 16px;
      }

      .review-comment {
        padding: 16px;

        .comment-label {
          color: #64748b;
          font-size: 14px;
          margin-bottom: 8px;
        }

        .comment-content {
          color: #1e293b;
          line-height: 1.6;
          margin-bottom: 12px;
          padding: 12px;
          background-color: #fff;
          border-radius: 4px;
          border: 1px solid #e2e8f0;
        }

        .review-time {
          color: #94a3b8;
          font-size: 13px;
          text-align: right;
        }
      }
    }
  }

  // 支付信息样式
  .payment-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .payment-amount {
      font-size: 14px;
      font-weight: 600;
      color: #67c23a;
    }
  }
}
</style> 