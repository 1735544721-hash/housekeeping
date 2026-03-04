<template>
  <div class="review-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">服务评价管理</h2>
          <el-tag type="info" effect="plain" class="review-count">
            {{ total }} 条评价
          </el-tag>
        </div>
        <div class="right">
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
          <el-form :inline="false" class="search-form">
            <el-form-item label="订单编号">
              <el-input v-model="searchForm.orderId" placeholder="请输入订单编号" clearable @keyup.enter="handleSearch" />
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
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="orderId" label="订单编号" width="180" fixed="left" />
        <el-table-column prop="user.name" label="用户名" width="120" />
        <el-table-column prop="staff.user.name" label="服务人员" width="120" />
        <el-table-column prop="skillRating" label="技能评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.skillRating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="attitudeRating" label="态度评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.attitudeRating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="experienceRating" label="体验评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.experienceRating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="overallRating" label="总体评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.overallRating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="评价时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 评价详情对话框 -->
    <el-dialog
      title="评价详情"
      v-model="detailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单编号">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.user?.name }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ detail.staff?.user?.name }}</el-descriptions-item>
        <el-descriptions-item label="技能评分">
          <el-rate v-model="detail.skillRating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="态度评分">
          <el-rate v-model="detail.attitudeRating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="体验评分">
          <el-rate v-model="detail.experienceRating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="总体评分">
          <el-rate v-model="detail.overallRating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ formatDate(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'

// 搜索表单
const searchForm = reactive({
  orderId: '',
  overallRating: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 评价详情
const detailDialogVisible = ref(false)
const detail = reactive({})

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    await request.get('/review/list', params, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } finally {
    loading.value = false
  }
}

// 查看评价详情
const handleView = async (row) => {
  try {
    await request.get(`/review/${row.id}`, null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        Object.assign(detail, res)
        detailDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取评价详情失败:', error)
  }
}

// 删除评价
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该评价吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/review/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchReviews()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除评价失败:', error)
    }
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReviews()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchReviews()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchReviews()
}

// 刷新
const handleRefresh = () => {
  fetchReviews()
  ElMessage.success('刷新成功')
}

// 搜索区域展开/收起
const showSearch = ref(true)
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

onMounted(() => {
  fetchReviews()
})
</script>

<style lang="scss" scoped>
.review-list {
  padding: 16px;
}

.action-card {
  background: #fff;
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.review-count {
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

// 搜索卡片样式
.search-card {
  margin-bottom: 20px;
  border: none !important;
  background-color: #f8fafc;

  :deep(.el-card__header) {
    padding: 16px 20px;
    border: none;
  }

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
    }

    .arrow {
      transition: transform 0.3s;
      
      &.is-active {
        transform: rotate(-180deg);
      }
    }
  }

  .search-content {
    transition: all 0.3s;
    
    &.collapsed {
      height: 0;
      padding: 0;
      overflow: hidden;
    }
  }

  .search-form {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    padding: 20px 0;

    .search-buttons {
      display: flex;
      align-items: flex-end;
      gap: 12px;
    }
  }
}

// 分页样式
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 