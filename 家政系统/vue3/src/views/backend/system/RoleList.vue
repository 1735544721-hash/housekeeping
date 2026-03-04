<template>
  <div class="role-list">
    <!-- 搜索和操作区域 - 合并成一个卡片 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">角色管理</h2>
          <el-tag type="info" effect="plain" class="role-count">
            {{ total }} 个角色
          </el-tag>
        </div>
        <div class="right">
          <el-input
            v-model="searchForm.name"
            placeholder="搜索角色名称"
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>

      <!-- 表格区域 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('code')" prop="code" label="角色编码" min-width="120" />
        <el-table-column v-if="isColumnVisible('name')" prop="name" label="角色名称" min-width="120" />
        <el-table-column v-if="isColumnVisible('description')" prop="description" label="角色描述" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('createdTime')" prop="createdTime" label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" v-if="row.code !== 'USER'" link @click="handleAssignMenus(row)">分配菜单</el-button>
            <el-button v-if="row.code !== 'STAFF'&&row.code !== 'USER'&&row.code !== 'ADMIN'" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 角色表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="580px"
      @close="resetForm"
      class="role-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <div class="form-grid">
          <el-form-item label="角色编码" prop="code" class="span-full">
            <el-input v-model="form.code" :disabled="!!form.id" placeholder="请输入角色编码" />
          </el-form-item>
          <el-form-item label="角色名称" prop="name" class="span-full">
            <el-input v-model="form.name" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色描述" prop="description" class="span-full">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              rows="3"
              placeholder="请输入角色描述" 
            />
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

    <!-- 分配菜单对话框 -->
    <el-dialog
      title="分配菜单"
      v-model="menuDialogVisible"
      width="480px"
      :close-on-click-modal="false"
      class="menu-dialog"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name' }"
        :default-checked-keys="selectedMenuIds"
        :check-strictly="true"
        default-expand-all
        @check="handleMenuCheck"
        class="menu-tree"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMenuAssignment">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 列设置抽屉 -->
    <el-drawer
      v-model="columnSettingVisible"
      title="列设置"
      direction="rtl"
      size="300px"
    >
      <el-checkbox-group v-model="visibleColumns" class="column-list">
        <el-checkbox v-for="col in allColumns" :key="col.prop" :label="col.prop">
          {{ col.label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting, Search, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'
import * as XLSX from 'xlsx'

// 搜索表单
const searchForm = reactive({
  code: '',
  name: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const menuTree = ref([])
const selectedMenuIds = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const form = reactive({
  id: '',
  code: '',
  name: '',
  description: ''
})

// 菜单对话框
const menuDialogVisible = ref(false)
const menuTreeRef = ref(null)
const currentRole = ref(null)

// 表单验证规则
const rules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取角色列表
const fetchRoles = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      code: searchForm.code,
      name: searchForm.name
    }
    const res = await request.get('/role/page', params)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 获取菜单树
const fetchMenuTree = async () => {
  try {
    const res = await request.get('/menu')
    menuTree.value = res
  } catch (error) {
    console.error('获取菜单树失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRoles()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRoles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRoles()
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
  form.id = ''
}

// 编辑角色
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除角色
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/role/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchRoles()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除角色失败:', error)
    }
  }
}

// 分配菜单
const handleAssignMenus = async (row) => {
  currentRole.value = row
  menuDialogVisible.value = true
  try {
    const res = await request.get(`/role/${row.id}/menuIds`, {
      showDefaultMsg: false
    })
    selectedMenuIds.value = res
  } catch (error) {
    console.error('获取角色菜单失败:', error)
  }
}

// 处理菜单选中事件
const handleMenuCheck = (node, { checkedKeys }) => {
  if (!menuTreeRef.value) return

  // 如果是选中操作，找到并选中所有父节点
  const findAndCheckParents = (tree, targetId, checked) => {
    for (const item of tree) {
      if (item.children && item.children.length > 0) {
        // 如果当前节点的子节点中包含目标节点，或者递归查找子节点返回true
        if (item.children.some(child => child.id === targetId) || 
            findAndCheckParents(item.children, targetId, checked)) {
          // 选中当前父节点
          menuTreeRef.value.setChecked(item.id, checked)
          return true
        }
      }
    }
    return false
  }

  // 当前节点被选中时，选中其所有父节点
  if (checkedKeys.includes(node.id)) {
    findAndCheckParents(menuTree.value, node.id, true)
  }
}

// 提交菜单分配
const submitMenuAssignment = async () => {
  if (!currentRole.value || !menuTreeRef.value) return
  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    await request.post(`/role/${currentRole.value.id}/menus`, checkedKeys, {
      successMsg: '菜单分配成功'
    })
    menuDialogVisible.value = false
  } catch (error) {
    console.error('分配菜单失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/role/${form.id}`, form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/role', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchRoles()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.keys(form).forEach(key => {
    form[key] = ''
  })
}

// 刷新
const handleRefresh = () => {
  fetchRoles()
  ElMessage.success('刷新成功')
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个角色吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/role/batch-delete', { ids }, {
      successMsg: '批量删除成功'
    })
    fetchRoles()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 导出
const handleExport = () => {
  try {
    loading.value = true
    
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    const exportData = tableData.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'createdTime') {
          row[col.label] = formatDate(item.createdTime)
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '角色列表')
    XLSX.writeFile(workbook, `角色列表_${dateUtils.format(new Date(), 'YYYY-MM-DD')}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 列设置
const STORAGE_KEY = 'roleListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'code', label: '角色编码' },
  { prop: 'name', label: '角色名称' },
  { prop: 'description', label: '角色描述' },
  { prop: 'createdTime', label: '创建时间' }
]

const visibleColumns = ref(
  JSON.parse(localStorage.getItem(STORAGE_KEY)) || allColumns.map(col => col.prop)
)

watch(visibleColumns, (newVal) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal))
}, { deep: true })

const isColumnVisible = (prop) => {
  return visibleColumns.value.includes(prop)
}

onMounted(() => {
  fetchRoles()
  fetchMenuTree()
})
</script>

<style scoped>
.role-list {
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

.role-count {
  font-size: 13px;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
}

/* 表头样式优化 */
:deep(.el-table__header) {
  background: linear-gradient(to right, #f8fafc, #ffffff);
  display: table-header-group;
  width: 100% !important;
}

:deep(.el-table__header-wrapper) {
  width: 100% !important;
}

:deep(.el-table__header-wrapper th) {
  background: transparent;
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  height: 48px;
  padding: 0;
  border-bottom: 2px solid #e2e8f0;
  transition: background-color 0.2s;
}

:deep(.el-table__header-wrapper th.is-leaf) {
  border-bottom: 2px solid #e2e8f0;
}

:deep(.el-table__header-wrapper th:hover) {
  background-color: rgba(241, 245, 249, 0.6);
}

:deep(.el-table__header .cell) {
  line-height: 48px;
  white-space: nowrap;
  padding: 0 12px;
}

/* 调整表格内容与表头对齐 */
:deep(.el-table__body td) {
  height: 48px;
  padding: 0;
}

:deep(.el-table__body td .cell) {
  padding: 0 12px;
  line-height: 48px;
}

/* 分页样式 */
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 表单样式 */
.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.span-full {
  grid-column: 1 / -1;
}

/* 菜单树样式 */
.menu-tree {
  height: 400px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 12px;
}

/* 列设置样式 */
.column-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

:deep(.el-checkbox__label) {
  font-size: 14px;
}
</style> 