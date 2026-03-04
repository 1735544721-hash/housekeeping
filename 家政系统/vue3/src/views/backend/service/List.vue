<template>
  <div class="service-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">服务项目管理</h2>
          <el-tag type="info" effect="plain" class="service-count">
            {{ total }} 个服务项目
          </el-tag>
        </div>
        <div class="right">
          <el-button type="primary" @click="handleAdd">
            <el-icon>
              <Plus />
            </el-icon>新增服务项目
          </el-button>
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>

      <!-- 可折叠的搜索区域 -->
      <el-card class="search-card" shadow="never">
        <template #header>
          <div class="search-header" @click="toggleSearch">
            <div class="title">
              <el-icon>
                <Search />
              </el-icon>
              <span>筛选条件</span>
            </div>
            <div class="arrow" :class="{ 'is-active': showSearch }">
              <el-icon>
                <ArrowDown />
              </el-icon>
            </div>
          </div>
        </template>
        <div class="search-content" :class="{ collapsed: !showSearch }" v-show="showSearch">
          <el-form :model="searchForm" ref="searchFormRef" :inline="true" class="search-form">
            <el-form-item label="服务标题" prop="title">
              <el-input v-model="searchForm.title" placeholder="请输入服务标题" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="服务类型" prop="categoryId">
              <el-tree-select v-model="searchForm.categoryId" :data="categoryOptions" node-key="id" :props="{
                  label: 'categoryName',
                  children: 'children'
                }" placeholder="请选择服务类型" clearable style="width: 220px" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select style="width: 220px" v-model="searchForm.status" placeholder="请选择状态" clearable>
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon>
                  <Search />
                </el-icon>查询
              </el-button>
              <el-button @click="resetSearch">
                <el-icon>
                  <Refresh />
                </el-icon>重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 表格区域 -->
      <el-table v-loading="loading" :data="tableData" border width="100%">
        <el-table-column type="index" label="序号"  align="center" fixed="left" />
        <el-table-column prop="title" label="服务标题" show-overflow-tooltip />
        <el-table-column label="所属分类" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.category?.categoryName }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="650px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="服务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入服务标题" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-tree-select v-model="form.categoryId" :data="categoryOptions" node-key="id" :props="{
              label: 'categoryName',
              children: 'children'
            }" placeholder="请选择所属分类" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入服务描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'
import fileUtils from '@/utils/fileUtils'  // 导入 fileUtils

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: '',
  status: ''
})

const searchFormRef = ref(null)

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 服务类型选项
const categoryOptions = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增服务项目')
const formRef = ref(null)
const form = reactive({
  id: '',
  title: '',
  categoryId: '',
  price: 0,
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入服务标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择所属分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ]
}

// 搜索区域显示控制
const showSearch = ref(true)

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取服务项目列表
const fetchServices = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    await request.get('/service/list', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } finally {
    loading.value = false
  }
}

// 获取服务类型选项
const fetchCategories = async () => {
  try {
    await request.get('/category/tree', null, {
      onSuccess: (res) => {
        categoryOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取服务类型失败:', error)
  }
}

// 切换搜索区域显示/隐藏
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 重置搜索
const resetSearch = () => {
  if (searchFormRef.value) {
    searchFormRef.value.resetFields()
  }
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchServices()
  ElMessage.success('刷新成功')
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增服务项目'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑服务项目'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await request.put(`/service/${row.id}/status?status=${newStatus}`, {
      successMsg: `${newStatus === 1 ? '上架' : '下架'}成功`
    })
    row.status = newStatus
  } catch (error) {
    console.error('修改状态失败:', error)
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该服务项目吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/service/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchServices()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除服务项目失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/service/${form.id}`, form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/service', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchServices()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: '',
    title: '',
    categoryId: '',
    price: 0,
    description: '',
    status: 1
  })
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchServices()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchServices()
}

onMounted(() => {
  fetchServices()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.service-list {
  padding: 16px;

  .action-card {
    background: #fff;

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
        gap: 8px;
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

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 