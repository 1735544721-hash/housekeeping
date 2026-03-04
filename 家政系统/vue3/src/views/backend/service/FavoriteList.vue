<template>
  <div class="favorite-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">服务收藏管理</h2>
          <el-tag type="info" effect="plain" class="favorite-count">
            {{ total }} 条收藏
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
        <div class="search-content" v-show="showSearch">
          <el-form :inline="false" class="search-form">
            <el-form-item label="用户ID">
              <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="服务ID">
              <el-input v-model="searchForm.serviceId" placeholder="请输入服务ID" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>查询
              </el-button>
              <el-button @click="resetSearch">重置</el-button>
            </div>
          </el-form>
        </div>
      </el-card>

      <!-- 表格区域 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        border 
        style="width: 100%"
        :max-height="600"
      >
        <el-table-column 
          type="index" 
          label="序号" 
          width="60" 
          align="center"
        />
        <el-table-column 
          prop="id" 
          label="ID" 
          width="80" 
          align="center"
        />
        <el-table-column 
          prop="userId" 
          label="用户ID" 
          width="120" 
          align="center"
        />
        <el-table-column 
          prop="user.name" 
          label="用户名" 
          width="120" 
          align="center" 
        />
        <el-table-column 
          prop="serviceId" 
          label="服务ID" 
          width="120" 
          align="center" 
        />
        <el-table-column 
          prop="serviceItem.title" 
          label="服务名称" 
          min-width="200" 
          show-overflow-tooltip 
        />
        <el-table-column 
          prop="serviceItem.price" 
          label="服务价格" 
          width="120" 
          align="center"
        >
          <template #default="{ row }">
            ¥{{ row.serviceItem.price }}
          </template>
        </el-table-column>
        <el-table-column 
          prop="createTime" 
          label="收藏时间" 
          width="180" 
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column 
          label="操作" 
          width="120" 
          fixed="right" 
          align="center"
        >
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

    <!-- 收藏详情对话框 -->
    <el-dialog
      title="收藏详情"
      v-model="detailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.user?.name }}</el-descriptions-item>
        <el-descriptions-item label="服务ID">{{ detail.serviceId }}</el-descriptions-item>
        <el-descriptions-item label="服务名称">{{ detail.serviceItem?.title }}</el-descriptions-item>
        <el-descriptions-item label="服务价格">
          ¥{{ detail.serviceItem?.price }}
        </el-descriptions-item>
        <el-descriptions-item label="收藏时间">{{ formatDate(detail.createTime) }}</el-descriptions-item>
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
  userId: '',
  serviceId: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 收藏详情
const detailDialogVisible = ref(false)
const detail = reactive({})

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取收藏列表
const fetchFavorites = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    await request.get('/favorite/list', params, {
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

// 查看收藏详情
const handleView = async (row) => {
  try {
    await request.get(`/favorite/${row.id}`, null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        Object.assign(detail, res)
        detailDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取收藏详情失败:', error)
  }
}

// 删除收藏
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该收藏记录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete('/favorite', {
      params: {
        userId: row.userId,
        serviceId: row.serviceId
      },
      successMsg: '删除成功'
    })
    fetchFavorites()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除收藏失败:', error)
    }
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchFavorites()
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
  fetchFavorites()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchFavorites()
}

// 刷新
const handleRefresh = () => {
  fetchFavorites()
  ElMessage.success('刷新成功')
}

// 搜索区域展开/收起
const showSearch = ref(true)
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
// 样式部分与 List.vue 保持一致
.favorite-list {
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

.favorite-count {
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
    padding: 20px 0;
    border-top: 1px dashed #e2e8f0;
    transition: all 0.3s ease;
  }

  .search-form {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;

    :deep(.el-form-item) {
      margin-bottom: 0;

      .el-form-item__label {
        font-weight: 500;
      }

      .el-input,
      .el-select {
        width: 100%;
      }
    }

    .search-buttons {
      display: flex;
      align-items: flex-end;
      gap: 12px;
      min-width: 200px;
    }
  }
}

// 分页样式
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 优化表格样式 */
:deep(.el-table) {
  // 移除可能导致问题的样式设置
  &::before {
    display: none;
  }
  
  .el-table__inner-wrapper {
    padding-bottom: 1px; // 修复边框显示问题
  }

  // 表头和表体对齐
  th.el-table__cell,
  td.el-table__cell {
    padding: 8px 0;
    
    .cell {
      padding: 0 8px;
      line-height: 23px;
    }
  }

  // 修复固定列样式
  .el-table__fixed {
    height: 100% !important;
    bottom: 0 !important;
    
    .el-table__fixed-header-wrapper,
    .el-table__fixed-body-wrapper {
      background-color: var(--el-bg-color);
    }
  }

  // 确保滚动条不会影响布局
  .el-table__body-wrapper {
    overflow-x: auto;
    overflow-y: auto;
    
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 3px;
    }
  }
}
</style> 