<template>
  <div class="user-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">用户管理</h2>
          <el-tag type="info" effect="plain" class="user-count">
            {{ total }} 个用户
          </el-tag>
        </div>
        <div class="right">
          <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增用户
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
          <el-form :inline="false" class="search-form">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="searchForm.roleCode" placeholder="请选择角色" clearable style="width: 100%">
                <el-option label="管理员" value="admin" />
                <el-option label="普通用户" value="user" />
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

      <!-- 表格区域 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        border 
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="avatar" label="头像" width="60">
          <template #default="{ row }">
            <el-avatar :size="32" :src="fileUtils.getImageUrl(row.avatar)">
              {{ row.name?.charAt(0)?.toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="100" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" width="80" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'primary' : 'info'" effect="plain">
              {{ row.gender === 1 ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="phone" label="手机号" width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="120" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色" min-width="100">
          <template #default="{ row }">
            <el-tag 
              size="small"
              :type="row.roleCode === 'ADMIN' ? 'danger' : 'info'"
              effect="plain"
            >{{ getRoleName(row.roleCode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
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
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination :current-page="currentPage" :page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" @close="resetForm" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item label="用户名" prop="username" class="span-full">
            <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!form.id" class="span-full">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input-number v-model="form.age" :min="0" :max="150" style="width: 100%" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email" class="span-full">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard" class="span-full">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="地址" prop="address" class="span-full">
            <el-input v-model="form.address" placeholder="请输入地址" />
          </el-form-item>
          <el-form-item label="头像" prop="avatar" class="span-full">
            <div class="avatar-uploader">
              <el-upload
                class="upload-trigger"
                :auto-upload="false"
                :show-file-list="false"
                accept="image/*"
                @change="handleAvatarChange"
              >
                <template #default>
                  <el-avatar 
                    v-if="form.avatar" 
                    :src="fileUtils.getImageUrl(form.avatar)" 
                    :size="100"
                  />
                  <div v-else class="avatar-uploader-icon">
                    <el-icon><Plus /></el-icon>
                  </div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="角色" prop="roleCode">
            <el-select 
              v-model="form.roleCode" 
              placeholder="请选择角色" 
              style="width: 100%"
            >
              <el-option 
                v-for="role in roleOptions" 
                :key="role.code" 
                :label="role.name" 
                :value="role.code" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Plus, ArrowDown, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'
import fileUtils from '@/utils/fileUtils'

// 搜索表单
const searchForm = reactive({
  username: '',
  roleCode: ''
})

// 控制搜索面板的展开和折叠
const showSearch = ref(true)
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const form = reactive({
  id: '',
  username: '',
  password: '',
  confirmPassword: '',
  roleCode: '',
  email: '',
  phone: '',
  name: '',
  idCard: '',
  address: '',
  avatar: '',
  gender: 1,
  age: 0,
  status: 1
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  name: [
    { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  idCard: [
    {
      pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
      message: '请输入正确的身份证号格式', trigger: 'blur'
    }
  ],
  address: [
    { max: 200, message: '地址长度不能超过200个字符', trigger: 'blur' }
  ],
  age: [
    { type: 'number', min: 0, max: 150, message: '年龄必须在0到150之间', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,

      ...searchForm
    }
    const res = await request.get('/user/list', params, {
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
  fetchUsers()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUsers()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

// 新增用户
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
  form.id = ''
}

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/user/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchUsers()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

// 重置密码
const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认重置该用户的密码吗？', 
      '重置密码确认', 
      {
      type: 'warning',
        confirmButtonText: '确定重置',
        cancelButtonText: '取消'
      }
    )

    const res = await request.put(`/user/${row.id}/reset-password`, null, {
      showDefaultMsg: false,
      onError: (error) => {
        ElMessage.error(error.message)
      },
      onSuccess: (res) => {
        ElMessageBox.alert(
          `密码重置成功！新密码为：${res}`, 
          '重置成功', 
          {
            type: 'success',
      confirmButtonText: '确定',
            callback: () => {
              fetchUsers()
            }
          }
        )
      }
    })
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('重置密码失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put('/user/info', form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/user/register', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchUsers()
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
    username: '',
    password: '',
    confirmPassword: '',
    roleCode: '',
    email: '',
    phone: '',
    name: '',
    idCard: '',
    address: '',
    avatar: '',
    gender: 1,
    age: 0,
    status: 1
  })
}

// 刷新
const handleRefresh = () => {
  fetchUsers()
  ElMessage.success('刷新成功')
}

// 头像上传相关
const handleAvatarChange = async (file) => {
  if (!file) return
  
  // 验证文件
  if (!fileUtils.isImage(file.raw)) {
    ElMessage.error('头像只能是图片格式!')
    return
  }
  
  if (!fileUtils.checkFileSize(file.raw, 2)) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return
  }

  try {
    // 创建表单数据
    const formData = new FormData()
    formData.append('file', file.raw)

    // 发送上传请求
    const res = await request.post('/file/upload/img', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      successMsg: '头像上传成功',
      onSuccess: (res) => {
        form.avatar = res
      }
    })

  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
}

// 批量删除用户
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 个用户吗？删除后将无法恢复！`,
      '批量删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const ids = selectedRows.value.map(row => row.id)
    await request.delete('/user/batch', {
      data: ids,
      successMsg: '批量删除成功'
    })

    // 刷新列表并清空选择
    selectedRows.value = []
    fetchUsers()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('批量删除用户失败:', error)
    }
  }
}

// 角色选项
const roleOptions = ref([])

// 获取所有角色
const fetchRoles = async () => {
  try {
    await request.get('/role/all', null, {
      onSuccess: (res) => {
        roleOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 添加获取角色名称的方法
const getRoleName = (roleCode) => {
  const role = roleOptions.value.find(r => r.code === roleCode)
  return role ? role.name : '未知角色'
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style lang="scss" scoped>
.user-list {
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

.user-count {
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
.user-dialog {
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  .span-full {
    grid-column: 1 / -1;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}

// 头像上传样式
.avatar-uploader {
  width: 100px;
  height: 100px;
  
  .upload-trigger {
    width: 100%;
    height: 100%;
    
    :deep(.el-upload) {
      width: 100%;
      height: 100%;
      border: 1px dashed var(--el-border-color);
      border-radius: 50%;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
        
        .avatar-uploader-icon {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .avatar-uploader-icon {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 28px;
    color: var(--el-text-color-secondary);
  }

  :deep(.el-avatar) {
    width: 100%;
    height: 100%;
  }
}

// 添加批量删除按钮样式
.right {
  .el-button--danger {
    margin-right: 8px;

    &:disabled {
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
}

.role-tag {
  margin: 2px 4px;
}
</style> 