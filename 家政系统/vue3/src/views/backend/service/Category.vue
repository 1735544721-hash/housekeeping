<template>
  <div class="category-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">服务分类管理</h2>
          <el-tag type="info" effect="plain" class="category-count">
            {{ total }} 个分类
          </el-tag>
        </div>
        <div class="right">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="list">列表模式</el-radio-button>
            <el-radio-button label="tree">树形模式</el-radio-button>
          </el-radio-group>
          <el-button type="primary" @click="handleAdd">
            <el-icon>
              <Plus />
            </el-icon>新增分类
          </el-button>
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>

      <!-- 搜索区域 - 仅列表模式显示 -->
      <el-card v-if="viewMode === 'list'" class="search-card" shadow="never">
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
          <el-form :inline="false" class="search-form">
            <el-form-item label="分类名称">
              <el-input v-model="searchForm.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch">
                <el-icon>
                  <Search />
                </el-icon>查询
              </el-button>
              <el-button @click="resetSearch">重置</el-button>
            </div>
          </el-form>
        </div>
      </el-card>

      <!-- 树形表格 -->
      <el-table v-if="viewMode === 'tree'" v-loading="loading" :data="treeData" row-key="id" border default-expand-all :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
        <el-table-column type="expand" width="50">
          <template #default="{ row }">
            <div class="sub-category-table" v-if="row.children && row.children.length > 0">
              <div class="sub-category-header">
                <el-tag size="small" effect="plain" type="info">
                  {{ row.children.length }} 个子分类
                </el-tag>
              </div>
              <el-table 
                :data="row.children" 
                border 
                size="small"
                style="width: 100%"
              >
                <el-table-column prop="categoryName" label="分类名称" min-width="180">
                  <template #default="{ row: subRow }">
                    <span class="category-name">
                      <el-icon v-if="subRow.icon">
                        <component :is="subRow.icon" />
                      </el-icon>
                      {{ subRow.categoryName }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="sortNum" label="排序" width="80" align="center" />
                <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="80" align="center">
                  <template #default="{ row: subRow }">
                    <el-tag :type="subRow.status === 1 ? 'success' : 'danger'">
                      {{ subRow.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="{ row: subRow }">
                    <el-button type="primary" link @click="handleEdit(subRow)">编辑</el-button>
                    <el-button type="danger" link @click="handleDelete(subRow)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else description="暂无子分类" />
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类名称" min-width="180">
          <template #default="{ row }">
            <span class="category-name">
              <el-icon v-if="row.icon">
                <component :is="row.icon" />
              </el-icon>
              {{ row.categoryName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序" width="80" align="center" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.parentId" type="primary" link @click="handleAdd(row)">新增子分类</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 列表表格 -->
      <el-table v-else v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="categoryName" label="分类名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" class="table-icon">
              <component :is="row.icon" />
            </el-icon>
            <span v-else class="no-icon">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序" width="80" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 - 仅列表模式显示 -->
      <div v-if="viewMode === 'list'" class="pagination">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 分类表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="category-form">
        <div class="form-body">
          <el-form-item label="名称" prop="categoryName">
            <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
          </el-form-item>
          <el-form-item label="父分类" prop="parentId">
            <el-select 
              v-model="form.parentId" 
              placeholder="请选择父分类" 
              clearable 
              style="width: 100%"
            >
              <el-option 
                label="顶级分类" 
                :value="null" 
              />
              <el-option
                v-for="item in topLevelCategories"
                :key="item.id"
                :label="item.categoryName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="排序" prop="sortNum">
            <el-input-number v-model="form.sortNum" :min="0" :max="999" style="width: 100%" />
          </el-form-item>
          <el-form-item label="图标" prop="icon">
            <div class="icon-select">
              <el-input 
                v-model="form.icon" 
                placeholder="请选择图标" 
                readonly 
                @click="openIconSelector"
              >
                <template #prepend>
                  <el-icon v-if="form.icon">
                    <component :is="form.icon" />
                  </el-icon>
                </template>
              </el-input>
              <el-button @click="openIconSelector">选择图标</el-button>
            </div>
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加图标选择器对话框 -->
    <el-dialog 
      v-model="iconDialogVisible" 
      title="选择图标" 
      width="720px"
      class="icon-dialog"
    >
      <el-input
        v-model="iconSearch"
        placeholder="搜索图标"
        clearable
        class="icon-search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="icon-grid">
        <div
          v-for="icon in filteredIconList"
          :key="icon"
          class="icon-item"
          :class="{ active: form.icon === icon }"
          @click="selectIcon(icon)"
        >
          <el-icon>
            <component :is="icon" />
          </el-icon>
          <span>{{ icon }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Plus, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'
import { resolveComponent } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  categoryName: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref(null)

// 视图模式
const viewMode = ref('list')
const treeData = ref([])
const categoryOptions = ref([])

// 表单数据
const form = reactive({
  id: '',
  categoryName: '',
  parentId: null,
  sortNum: 1,
  description: '',
  icon: '',
  status: 1
})

// 表单验证规则
const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称不能超过50个字符', trigger: 'blur' }
  ],
  sortNum: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', message: '排序号必须为数字', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ],
  icon: [
    { max: 255, message: '图标URL不能超过255个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    await request.get('/category/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCategories()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCategories()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCategories()
}

// 添加顶级分类列表
const topLevelCategories = ref([])

// 修改获取分类树的方法
const fetchCategoryTree = async () => {
  loading.value = true
  try {
    const res = await request.get('/category/tree', null, {
      showDefaultMsg: false
    })
    if (res) {
      // 处理图标组件
      const processTreeData = (data) => {
        return data.map(item => {
          const node = { ...item }
          if (node.icon) {
            try {
              const iconComponent = resolveComponent(node.icon)
              if (!iconComponent) {
                node.icon = null
              }
            } catch {
              node.icon = null
            }
          }
          if (node.children && node.children.length > 0) {
            node.children = processTreeData(node.children)
          }
          return node
        })
      }

      treeData.value = processTreeData(res)
      
      // 只获取顶级分类
      topLevelCategories.value = res.filter(item => !item.parentId)
    }
  } catch (error) {
    console.error('获取分类树失败:', error)
  } finally {
    loading.value = false
  }
}

// 修改编辑方法
const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  const editData = JSON.parse(JSON.stringify(row))
  Object.assign(form, editData)
  dialogVisible.value = true
}

// 修改新增方法
const handleAdd = (row = null) => {
  dialogTitle.value = row ? '新增子分类' : '新增分类'
  resetForm()
  if (row) {
    // 只有顶级分类才能添加子分类
    if (row.parentId) {
      ElMessage.warning('只能在顶级分类下添加子分类')
      return
    }
    form.parentId = row.id
  }
  dialogVisible.value = true
}

// 修改删除方法的提示
const handleDelete = async (row) => {
  try {
    const message = row.parentId 
      ? '确认删除该分类吗？' 
      : '确认删除该分类吗？删除后将同时删除其下所有子分类！'
    
    await ElMessageBox.confirm(message, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/category/${row.id}`, {
      successMsg: '删除成功'
    })
    if (viewMode.value === 'tree') {
      fetchCategoryTree()
    } else {
      fetchCategories()
    }
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除分类失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put('/category/update', form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/category/create', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchCategories()
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
    categoryName: '',
    parentId: null,
    sortNum: 1,
    description: '',
    icon: '',
    status: 1
  })
}

// 刷新
const handleRefresh = () => {
  if (viewMode.value === 'tree') {
    fetchCategoryTree()
  } else {
    fetchCategories()
  }
}

// 搜索区域展开/收起状态
const showSearch = ref(true)

// 切换搜索区域显示/隐藏
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

// 监听视图模式变化
watch(viewMode, (newVal) => {
  if (newVal === 'tree') {
    fetchCategoryTree()
  } else {
    fetchCategories()
  }
})

// 图标选择相关的响应式变量
const iconDialogVisible = ref(false)
const iconList = ref(Object.keys(ElementPlusIcons))
const iconSearch = ref('')

// 添加图标搜索计算属性
const filteredIconList = computed(() => {
  return iconList.value.filter(icon => 
    icon.toLowerCase().includes(iconSearch.value.toLowerCase())
  )
})

// 添加图标选择相关方法
const openIconSelector = () => {
  iconDialogVisible.value = true
}

const selectIcon = (icon) => {
  form.icon = icon
  iconDialogVisible.value = false
}

onMounted(() => {
  fetchCategoryTree()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.category-list {
  padding: 16px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

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
      color: #1e293b;
    }

    .category-count {
      font-size: 13px;
    }
  }

  .right {
    display: flex;
    align-items: center;
    gap: 12px;

    .el-radio-group {
      margin-right: 8px;
    }
  }
}

.search-card {
  margin-bottom: 20px;
  border: none !important;
  background-color: #f8fafc;
  transition: all 0.3s ease;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;

  :deep(.el-card__header) {
    padding: 16px 20px;
    border: none;
  }

  :deep(.el-card__body) {
    padding: 0 20px;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden;
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
      font-weight: 500;

      .el-icon {
        font-size: 16px;
        color: #64748b;
      }
    }

    .arrow {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      transition: all 0.3s ease;
      color: #64748b;

      &.is-active {
        transform: rotate(-180deg);
        background-color: #e2e8f0;
        color: #475569;
      }

      &:hover {
        background-color: #e2e8f0;
      }
    }
  }

  .search-content {
    padding: 20px 0;
    border-top: 1px dashed #e2e8f0;
    transition: all 0.3s ease;

    &.collapsed {
      padding: 0;
      height: 0;
      border-top: none;
    }
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

// 表格容器样式
:deep(.el-table-wrapper) {
  width: 100% !important;
  max-width: 100% !important;
  overflow: hidden !important;
}

:deep(.el-table) {
  width: 100% !important;
  max-width: 100% !important;
  table-layout: fixed !important;

  .el-table__inner-wrapper,
  .el-table__header-wrapper,
  .el-table__body-wrapper {
    width: 100% !important;
    max-width: 100% !important;
    overflow-x: hidden !important;
  }

  .cell {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .el-table__row {
    .cell {
      &.el-table__expand-icon {
        display: inline-block;
        margin-right: 8px;
      }
    }
  }
}

// 分页样式
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

// 表单对话框
.category-form {
  .form-body {
    padding: 20px 0;
  }

  :deep(.el-form-item) {
    margin-bottom: 22px;

    &:last-child {
      margin-bottom: 0;
    }

    .el-form-item__label {
      font-weight: 500;
    }

    .el-input,
    .el-select,
    .el-input-number {
      width: 100%;
    }
  }
}

.el-dialog {
  :deep(.el-dialog__body) {
    padding: 0 20px;
  }

  .dialog-footer {
    padding: 20px 0;
    text-align: right;
  }
}

// 状态标签样式
:deep(.el-tag) {
  border-radius: 4px;
  padding: 4px 8px;

  &.el-tag--success {
    background-color: #ecfdf5;
    border-color: #d1fae5;
    color: #059669;
  }

  &.el-tag--danger {
    background-color: #fef2f2;
    border-color: #fee2e2;
    color: #dc2626;
  }

  &.el-tag--info {
    background-color: #f8fafc;
    border-color: #f1f5f9;
    color: #64748b;
  }
}

// 修改树形表格样式
:deep(.el-table) {
  .el-table__row {
    .cell {
      &.el-table__expand-icon {
        display: inline-block;
        margin-right: 8px;
      }
    }
  }
}

// 修改分类名称单元格样式
.category-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .category-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 修改图标样式
.category-icon {
  flex-shrink: 0;
  font-size: 16px;
  color: #64748b;
}

// 修改标签样式
.parent-tag {
  flex-shrink: 0;
  font-size: 12px;
  padding: 0 4px;
  height: 20px;
  line-height: 18px;
}

// 添加树形选择器样式
:deep(.el-tree-select) {
  width: 100%;

  .el-select-dropdown__item {
    padding-right: 20px;
  }
}

// 修改分类名称样式
.category-name {
  display: inline-flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 16px;
    color: var(--el-color-primary);
  }
}

// 修改操作列样式
:deep(.el-table) {
  .el-button {
    padding: 4px 8px;

    & + .el-button {
      margin-left: 8px;
    }
  }
}

// 添加表格图标样式
.table-icon {
  font-size: 18px;
  color: var(--el-color-primary);
  transition: transform 0.2s ease;
  
  &:hover {
    transform: scale(1.2);
  }
}

.no-icon {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

// 修改子分类表格样式
.sub-category-table {
  padding: 16px 24px;
  
  .sub-category-header {
    margin-bottom: 16px;
  }
  
  :deep(.el-table) {
    // 子表格特有样式
    .el-table__header-wrapper th {
      background-color: var(--el-fill-color-light);
      font-size: 13px;
      padding: 8px 0;
    }
    
    .el-table__body-wrapper td {
      padding: 6px 0;
    }
    
    // 移除子表格的边框
    &::before,
    &::after {
      display: none;
    }
  }
}

// 添加图标选择器相关样式
.icon-select {
  display: flex;
  gap: 8px;
  
  .el-input {
    flex: 1;
    
    :deep(.el-input-group__prepend) {
      padding: 0 12px;
      
      .el-icon {
        font-size: 16px;
        color: var(--el-color-primary);
      }
    }
  }
}

.icon-dialog {
  .icon-search {
    margin-bottom: 16px;
  }

  .icon-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 12px;
    max-height: 400px;
    overflow-y: auto;
    padding: 8px;
  }

  .icon-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px 8px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--el-color-primary-light-9);
    }

    &.active {
      background: var(--el-color-primary-light-8);
      color: var(--el-color-primary);
    }

    .el-icon {
      font-size: 24px;
    }

    span {
      font-size: 12px;
      text-align: center;
      word-break: break-all;
    }
  }
}
</style> 