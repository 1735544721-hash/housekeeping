<template>
  <div class="menu-list">
    <!-- 搜索和操作区域 - 合并成一个卡片 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">菜单管理</h2>
          <el-tag type="info" effect="plain" class="menu-count">
            {{ tableData.length }} 个菜单项
          </el-tag>
        </div>
        <div class="right">
          <el-input
            v-model="searchForm.name"
            placeholder="搜索菜单名称"
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增菜单
          </el-button>
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>

      <!-- 表格区域 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        row-key="id" 
        border 
        default-expand-all 
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column prop="path" label="路由路径" min-width="180" />
        <el-table-column prop="component" label="组件路径" min-width="180" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序" width="80" />
        <el-table-column prop="hidden" label="显示" width="80">
          <template #default="{ row }">
            <el-tag :type="row.hidden ? 'info' : 'success'">
              {{ row.hidden ? '隐藏' : '显示' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.level < 2 && !row.component" 
              type="primary" 
              link 
              @click="handleAdd(row)"
            >新增子菜单</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 菜单表单对话框 - 优化布局 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="580px" 
      @close="resetForm"
      class="menu-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <div class="form-grid">
          <el-form-item label="上级菜单" class="span-full">
            <el-tree-select 
              v-model="form.pid" 
              :data="menuTree" 
              :props="{ label: 'name', value: 'id' }" 
              placeholder="请选择上级菜单" 
              clearable 
              check-strictly 
            />
          </el-form-item>
          <el-form-item label="菜单名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入菜单名称" />
          </el-form-item>
          <el-form-item label="排序号" prop="sortNum">
            <el-input-number v-model="form.sortNum" :min="0" :max="999" />
          </el-form-item>
          <el-form-item label="路由路径" prop="path" class="span-full">
            <el-input v-model="form.path" placeholder="请输入路由路径" />
          </el-form-item>
          <el-form-item label="组件路径" prop="component" class="span-full">
            <el-input v-model="form.component" placeholder="请输入组件路径" />
          </el-form-item>
          <el-form-item label="菜单图标" class="span-full">
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
          <el-form-item label="菜单描述" class="span-full">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              rows="3"
              placeholder="请输入菜单描述" 
            />
          </el-form-item>
          <el-form-item label="菜单状态" class="span-full">
            <el-switch
              v-model="form.hidden"
              active-text="隐藏"
              inactive-text="显示"
              inline-prompt
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

    <!-- 图标选择器优化 -->
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

<style scoped>
.menu-list {
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

.menu-count {
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

.menu-table {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table) {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  width: 100% !important;
  max-width: 100%;
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

.menu-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: rgba(74, 144, 226, 0.1);
  border-radius: 6px;
}

.menu-icon {
  font-size: 16px;
  color: #4A90E2;
}

.menu-name {
  font-weight: 500;
  color: #1e293b;
}

.hidden-tag {
  background: #f1f5f9;
  color: #64748b;
  border: none;
  padding: 2px 8px;
}

.path-cell {
  display: flex;
  align-items: center;
}

.path-tag {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: #475569;
  background: #f8fafc;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
}

.component-cell {
  display: flex;
  align-items: center;
}

.component-path {
  font-family: 'Fira Code', monospace;
  color: #64748b;
  font-size: 13px;
}

.sort-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.sort-num {
  font-weight: 600;
  color: #4A90E2;
  background: rgba(74, 144, 226, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

:deep(.el-table__row:hover) .action-buttons {
  opacity: 1;
}

.action-button {
  padding: 4px 8px;
  height: auto;
}

.action-button .el-icon {
  margin-right: 4px;
  font-size: 14px;
}

:deep(.el-table__indent) {
  padding-left: 24px !important;
}

:deep(.el-table__expand-icon) {
  color: #94a3b8;
}

:deep(.el-table .el-table__row--level-1) {
  background-color: #fafbfc;
}

/* 表单样式 */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.span-full {
  grid-column: 1 / -1;
}

.icon-select {
  display: flex;
  gap: 8px;
}

/* 图标选择器 */
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
}

.icon-item:hover {
  background: #f0f7ff;
}

.icon-item.active {
  background: #e6f0ff;
  color: #4A90E2;
}

.icon-item .el-icon {
  font-size: 24px;
}

.icon-item span {
  font-size: 12px;
  text-align: center;
  word-break: break-all;
}
</style>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as ElementPlusIcons from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  name: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const menuTree = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)
const form = reactive({
  id: '',
  pid: null,
  name: '',
  path: '',
  component: '',
  icon: '',
  description: '',
  sortNum: 1,
  hidden: false
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入路由路径', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (form.pid === null && !value.startsWith('/')) {
          callback(new Error('顶级菜单路径必须以/开头'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  component: [],
  sortNum: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 0, max: 999, message: '排序号必须在0-999之间', trigger: 'blur' }
  ]
}

// 获取菜单树
const fetchMenuTree = async () => {
  loading.value = true
  try {
    const res = await request.get('/menu', {
      showDefaultMsg: false
    })
    // 处理菜单树数据，添加层级标识
    const processMenuTree = (menus, level = 0) => {
      return menus
        .filter(menu => !menu.component) // 只返回没有组件路径的菜单
        .map(menu => ({
          ...menu,
          level,
          children: menu.children && level < 1 ? processMenuTree(menu.children, level + 1) : [] // 只处理一级以下的子菜单
        }))
    }
    const processedData = processMenuTree(res)
    tableData.value = res.map(menu => ({  // 表格数据保持原样
      ...menu,
      level: 0,
      children: menu.children ? menu.children.map(child => ({
        ...child,
        level: 1,
        children: child.children ? child.children.map(grandChild => ({
          ...grandChild,
          level: 2
        })) : []
      })) : []
    }))
    
    // 构建菜单选择树（只包含可作为父级的菜单）
    menuTree.value = [{
      id: 0,
      name: '顶级菜单',
      children: processedData,
      level: -1
    }]
  } catch (error) {
    console.error('获取菜单树失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchMenuTree()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchMenuTree()
  ElMessage.success('刷新成功')
}

// 新增菜单
const handleAdd = (row = null) => {
  dialogTitle.value = row ? '新增子菜单' : '新增菜单'
  form.pid = row ? row.id : null
  dialogVisible.value = true
}

// 编辑菜单
const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除菜单
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该菜单吗？删除后将无法恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/menu/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchMenuTree()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除菜单失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 检查菜单层级
    if (form.pid) {
      const parentMenu = findMenuById(form.pid)
      if (parentMenu && parentMenu.level >= 2) {
        ElMessage.error('不能在二级菜单下创建子菜单')
        return
      }
    }

    if (form.id) {
      await request.put(`/menu/${form.id}`, form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/menu', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchMenuTree()
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
    pid: null,
    name: '',
    path: '',
    component: '',
    icon: '',
    description: '',
    sortNum: 1,
    hidden: false
  })
}

// 添加查找菜单的辅助函数
const findMenuById = (id) => {
  const find = (menus) => {
    for (const menu of menus) {
      if (menu.id === id) return menu
      if (menu.children) {
        const found = find(menu.children)
        if (found) return found
      }
    }
    return null
  }
  return find(tableData.value)
}

// 图标选择相关
const iconDialogVisible = ref(false)
const iconList = ref(Object.keys(ElementPlusIcons))
const iconSearch = ref('')
const filteredIconList = computed(() => {
  return iconList.value.filter(icon => 
    icon.toLowerCase().includes(iconSearch.value.toLowerCase())
  )
})

const openIconSelector = () => {
  iconDialogVisible.value = true
}

const selectIcon = (icon) => {
  form.icon = icon
  iconDialogVisible.value = false
}

onMounted(() => {
  fetchMenuTree()
})
</script> 