<template>
  <div class="staff-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">服务人员管理</h2>
          <el-tag type="info" effect="plain" class="staff-count">
            {{ total }} 个服务人员
          </el-tag>
        </div>
        <div class="right">
          <el-button type="primary" @click="handleAdd">
            <el-icon>
              <Plus />
            </el-icon>新增服务人员
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
            <el-form-item label="姓名" prop="name">
              <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="服务类型" prop="serviceType">
              <el-tree-select v-model="searchForm.serviceType" :data="categoryOptions" node-key="categoryName" :props="{
                  label: 'categoryName',
                  children: 'children'
                }" placeholder="请选择服务类型" clearable style="width: 220px" />
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
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="user.name" label="姓名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="user.phone" label="联系电话" width="120" show-overflow-tooltip />
        <el-table-column prop="serviceType" label="服务类型" min-width="150">
          <template #default="{ row }">
            <el-tag 
              v-for="type in (Array.isArray(row.serviceType) ? row.serviceType : [])" 
              :key="type"
              size="small"
              class="service-tag"
              type="success"
              effect="plain"
            >{{ type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="experience" label="工作经验" width="100" align="center">
          <template #default="{ row }">
            {{ row.experience }} 年
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled :max="5" :allow-half="true" />
          </template>
        </el-table-column>

        <el-table-column prop="workArea" label="服务区域" min-width="120" show-overflow-tooltip />
        <el-table-column prop="totalOrders" label="订单数" width="80" align="center" />
        <el-table-column prop="completionRate" label="完成率" width="100" align="center">
          <template #default="{ row }">
            {{ row.completionRate }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" >
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAssignServices(row)">分配服务</el-button>
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
        <el-form-item label="关联用户" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择用户" filterable style="width: 100%" :disabled="!!form.id">
            <el-option 
              v-for="user in staffUsers" 
              :key="user.id" 
              :label="`${user.name} (${user.phone})`" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-tree-select 
            v-model="form.serviceTypeArray" 
            :data="categoryOptions" 
            node-key="categoryName" 
            :props="{
              label: 'categoryName',
              children: 'children'
            }" 
            multiple 
            placeholder="请选择服务类型" 
            style="width: 100%" 
            @change="handleServiceTypeChange" 
          />
        </el-form-item>
        <el-form-item label="工作经验" prop="experience">
          <el-input-number v-model="form.experience" :min="0" :max="50" style="width: 100%" />
        </el-form-item>

        <el-form-item label="服务区域" prop="workArea">
          <el-input v-model="form.workArea" placeholder="请输入服务区域" />
        </el-form-item>
        <el-form-item label="个人描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入个人描述" />
        </el-form-item>
        <el-form-item label="证书信息" prop="certificates">
          <div class="certificate-list">
            <el-scrollbar>
              <el-table :data="form.certificates" size="small" border style="width: 100%">
                <el-table-column label="证书名称" min-width="120">
                  <template #default="{ row }">
                    <el-input v-model="row.name" placeholder="请输入证书名称" />
                  </template>
                </el-table-column>
                <el-table-column label="证书编号" min-width="120">
                  <template #default="{ row }">
                    <el-input v-model="row.code" placeholder="请输入证书编号" />
                  </template>
                </el-table-column>
                <el-table-column label="发证日期" width="160">
                  <template #default="{ row }">
                    <el-date-picker v-model="row.issueDate" type="date" placeholder="选择发证日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" align="center" fixed="right">
                  <template #default="{ $index }">
                    <el-button type="danger" link @click="removeCertificate($index)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-scrollbar>
            <div class="certificate-footer">
              <el-button type="primary" link @click="addCertificate">
                <el-icon>
                  <Plus />
                </el-icon>添加证书
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 服务项目分配对话框 -->
    <el-dialog
      title="分配服务项目"
      v-model="serviceDialogVisible"
      width="1000px"
      :close-on-click-modal="false"
      @close="resetServiceForm"
      class="service-dialog"
    >
      <el-form :model="serviceForm" label-width="100px">
        <el-form-item label="服务人员">
          <div class="staff-info">
            <el-avatar :size="32" :src="currentStaff?.user?.avatar">
              {{ currentStaff?.user?.name?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="staff-name">{{ currentStaff?.user?.name }}</span>
          </div>
        </el-form-item>
        <el-form-item label="服务项目">
          <div class="service-selection">
            <!-- 左侧类别树 -->
            <div class="category-tree">
              <div class="tree-header">服务类别</div>
              <el-scrollbar>
                <el-tree
                  ref="categoryTreeRef"
                  :data="categoryTree"
                  node-key="id"
                  :props="{
                    label: 'categoryName'
                  }"
                  highlight-current
                  @node-click="handleCategorySelect"
                  default-expand-all
                />
              </el-scrollbar>
            </div>
            <!-- 右侧服务项目列表 -->
            <div class="service-list">
              <div class="list-header">
                <span>服务项目列表</span>
                <span class="selected-count" v-if="selectedServiceIds.length">
                  已选择 {{ selectedServiceIds.length }} 项
                </span>
              </div>
              <el-scrollbar>
                <div v-loading="servicesLoading">
                  <el-row :gutter="16">
                    <el-col :span="6" v-for="service in categoryServices" :key="service.id">
                      <div 
                        class="service-card"
                        :class="{ 'is-selected': selectedServiceIds.includes(service.id) }"
                        @click="toggleServiceSelection(service.id)"
                      >
                        <span class="service-name">{{ service.title }}</span>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </el-scrollbar>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="serviceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitServices">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  name: '',
  serviceType: ''
})

const searchFormRef = ref(null)

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增服务人员')
const formRef = ref(null)

// 搜索区域显示控制
const showSearch = ref(true)

// 服务类型选项
const categoryOptions = ref([])

// STAFF角色用户列表
const staffUsers = ref([])

// 表单数据
const form = reactive({
  id: '',
  userId: '',
  serviceType: [],
  serviceTypeArray: [],
  experience: 0,

  description: '',
  certificates: [],
  workArea: ''
})

// 表单验证规则
const rules = {
  userId: [
    { required: true, message: '请选择关联用户', trigger: 'change' }
  ],
  serviceType: [
    {
      validator: (rule, value, callback) => {
        if (!form.serviceTypeArray || form.serviceTypeArray.length === 0) {
          callback(new Error('请选择服务类型'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],

  workArea: [
    { required: true, message: '请输入服务区域', trigger: 'blur' }
  ]
}

// 服务项目分配相关
const serviceDialogVisible = ref(false)
const categoryTreeRef = ref(null)
const serviceTableRef = ref(null)
const currentStaff = ref(null)
const categoryTree = ref([])
const categoryServices = ref([])
const servicesLoading = ref(false)
const selectedServiceIds = ref([])

const serviceForm = reactive({
  staffId: '',
  serviceIds: []
})

// 获取服务人员列表
const fetchStaffList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    await request.get('/staff/list', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } finally {
    loading.value = false
  }
}

// 获取STAFF角色用户列表
const fetchStaffUsers = async () => {
  try {
    await request.get('/user/role/STAFF', null, {
      onSuccess: (res) => {
        staffUsers.value = res.records || []
      }
    })
  } catch (error) {
    console.error('获取STAFF用户列表失败:', error)
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
  fetchStaffList()
}

// 重置搜索
const resetSearch = () => {
  if (searchFormRef.value) {
    searchFormRef.value.resetFields()
  }
  Object.assign(searchForm, {
    name: '',
    serviceType: ''
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchStaffList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增服务人员'
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑服务人员'
  try {
    await request.get(`/staff/${row.id}`, null, {
      onSuccess: (res) => {
        const data = { ...res }
        // 处理服务类型
        data.serviceTypeArray = Array.isArray(data.serviceType) ? data.serviceType : []
        // 确保证书是数组
        data.certificates = Array.isArray(data.certificates) ? data.certificates : []
        Object.assign(form, data)
      }
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取服务人员详情失败:', error)
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该服务人员吗？删除后将无法恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/staff/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchStaffList()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除服务人员失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put('/staff/update', form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/staff/create', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchStaffList()
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
    userId: '',
    serviceType: [],
    serviceTypeArray: [],
    experience: 0,

    description: '',
    certificates: [],
    workArea: ''
  })
}

// 添加证书
const addCertificate = () => {
  if (!Array.isArray(form.certificates)) {
    form.certificates = []
  }
  form.certificates.push({
    name: '',
    code: '',
    issueDate: ''
  })
}

// 删除证书
const removeCertificate = (index) => {
  if (Array.isArray(form.certificates)) {
    form.certificates.splice(index, 1)
  }
}

// 处理服务类型变化
const handleServiceTypeChange = (val) => {
  form.serviceType = val || []
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchStaffList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchStaffList()
}

// 获取类别树
const fetchCategoryTree = async () => {
  try {
    await request.get('/category/tree', null, {
      onSuccess: (res) => {
        categoryTree.value = res || []
      }
    })
  } catch (error) {
    console.error('获取服务类别失败:', error)
  }
}

// 获取类别下的服务项目
const fetchCategoryServices = async (categoryId) => {
  servicesLoading.value = true
  try {
    await request.get(`/service/category/${categoryId}`, null, {
      onSuccess: (res) => {
        categoryServices.value = res || []
        // 设置已选中的服务项目
        if (serviceTableRef.value) {
          categoryServices.value.forEach(service => {
            if (selectedServiceIds.value.includes(service.id)) {
              serviceTableRef.value.toggleRowSelection(service, true)
            }
          })
        }
      }
    })
  } catch (error) {
    console.error('获取类别服务项目失败:', error)
  } finally {
    servicesLoading.value = false
  }
}

// 处理类别选择
const handleCategorySelect = (category) => {
  fetchCategoryServices(category.id)
}

// 处理服务项目选择变化
const toggleServiceSelection = (serviceId, checked) => {
  const index = selectedServiceIds.value.indexOf(serviceId)
  if (typeof checked === 'undefined') {
    // 点击卡片时触发
    if (index > -1) {
      selectedServiceIds.value.splice(index, 1)
    } else {
      selectedServiceIds.value.push(serviceId)
    }
  } else {
    // checkbox 变化时触发
    if (checked && index === -1) {
      selectedServiceIds.value.push(serviceId)
    } else if (!checked && index > -1) {
      selectedServiceIds.value.splice(index, 1)
    }
  }
}

// 分配服务项目
const handleAssignServices = async (row) => {
  currentStaff.value = row
  serviceDialogVisible.value = true
  serviceForm.staffId = row.id
  
  try {
    // 获取当前服务人员的服务项目
    await request.get(`/staff/service-items/list?staffId=${row.id}`, null, {
      onSuccess: (res) => {
        selectedServiceIds.value = res.map(item => item.serviceId)
      }
    })
    // 获取第一个类别的服务项目
    if (categoryTree.value.length > 0) {
      fetchCategoryServices(categoryTree.value[0].id)
    }
  } catch (error) {
    console.error('获取服务人员服务项目失败:', error)
  }
}

// 提交服务项目分配
const handleSubmitServices = async () => {
  try {
    await request.post(`/staff/service-items/assign?staffId=${serviceForm.staffId}&serviceIds=${selectedServiceIds.value}`, {
      successMsg: '服务项目分配成功'
    })
    
    serviceDialogVisible.value = false
    fetchStaffList() // 刷新列表
  } catch (error) {
    console.error('分配服务项目失败:', error)
  }
}

// 重置服务表单
const resetServiceForm = () => {
  currentStaff.value = null
  serviceForm.staffId = ''
  selectedServiceIds.value = []
  categoryServices.value = []
}

onMounted(() => {
  fetchStaffList()
  fetchStaffUsers()
  fetchCategories()
  fetchCategoryTree()
})
</script>

<style lang="scss" scoped>
.staff-list {
  .action-card {
    .action-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

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

    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;

      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
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

      :deep(.el-form-item) {
        margin-bottom: 0;
        margin-right: 0;

        &.el-form-item--large {
          margin-bottom: 0;
        }
      }
    }
  }

  .service-tag {
    margin: 2px 4px;
  }

  .certificate-list {
    max-height: 300px;
    display: flex;
    flex-direction: column;

    .certificate-footer {
      margin-top: 8px;
      padding: 8px 0;
      text-align: center;
      border-top: 1px dashed var(--el-border-color-light);
      flex-shrink: 0;
    }

    :deep(.el-scrollbar) {
      flex: 1;

      .el-table {
        &::before,
        &::after {
          display: none;
        }

        .el-table__inner-wrapper {
          height: 100%;
        }

        .el-input,
        .el-date-picker {
          --el-input-border-color: transparent;

          &:hover,
          &:focus {
            --el-input-border-color: var(--el-input-hover-border-color);
          }
        }

        .el-input__wrapper {
          box-shadow: none;

          &:hover,
          &:focus-within {
            box-shadow: 0 0 0 1px var(--el-input-hover-border-color);
          }
        }

        .el-table__fixed-right {
          height: 100%;
          background-color: var(--el-bg-color);
        }
      }
    }
  }

  // 添加树形选择器样式
  :deep(.el-tree-select) {
    .el-select-dropdown__item {
      padding-right: 20px;
    }
  }

  .service-tree-container {
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    padding: 10px;
    max-height: 400px;
    overflow-y: auto;
  }

  .service-tree {
    :deep(.el-tree-node__content) {
      height: 32px;
      
      .el-tree-node__label {
        font-size: 14px;
      }
    }
    
    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background-color: var(--el-color-primary-light-9);
    }
  }

  .service-selection {
    display: flex;
    gap: 20px;
    height: 400px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;

    width: 100%;

    .category-tree {
      width: 180px;
      padding: 10px;
      border-right: 1px solid var(--el-border-color-light);
      overflow-y: auto;
    }

    .service-list {
      flex: 1;
      padding: 16px;
      overflow: hidden;

      :deep(.el-scrollbar) {
        height: 100%;
      }
      
      .el-row {
        margin: 0 -8px;
      }

      .el-col {
        padding: 8px;
        
      }

      .service-card {
        position: relative;
        height: 0;
        padding-bottom: 33.33%; // 1:3 比例
        border: 2px solid var(--el-border-color-light);
        border-radius: 4px;
        cursor: pointer;
        text-align: center;
        transition: all 0.3s;
        background-color: var(--el-bg-color);
        overflow: hidden;
        
        &:hover {
          border-color: var(--el-color-primary-light-5);
          background-color: var(--el-color-primary-light-9);
          transform: translateY(-2px);
        }
        
        &.is-selected {
          background-color: var(--el-color-primary);
          border-color: var(--el-color-primary);
          
          .service-name {
            color: #ffffff;
            font-weight: 600;
          }
        }
        
        .service-name {
          position: absolute;
          inset: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 8px;
          font-size: 14px;
          color: var(--el-text-color-primary);
          text-align: center;
          transition: all 0.3s;
          line-height: 1.4;
          // 文字超出显示省略号
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      }
    }
  }
}

.service-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }

  .staff-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .staff-name {
      font-size: 15px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  .service-selection {
    display: flex;
    gap: 20px;
    height: 480px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;
    background-color: var(--el-bg-color-page);
    width: 100%;

    .category-tree {
      width: 200px;
      border-right: 1px solid var(--el-border-color-light);
      display: flex;
      flex-direction: column;

      .tree-header {
        padding: 16px;
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        border-bottom: 1px solid var(--el-border-color-light);
      }

      :deep(.el-scrollbar) {
        flex: 1;
        padding: 12px;

        .el-tree-node__content {
          height: 36px;
          border-radius: 4px;
          margin: 2px 0;

          &:hover {
            background-color: var(--el-color-primary-light-9);
          }

          &.is-current {
            background-color: var(--el-color-primary-light-8);
            color: var(--el-color-primary);
            font-weight: 500;
          }
        }
      }
    }

    .service-list {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .list-header {
        padding: 16px;
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        border-bottom: 1px solid var(--el-border-color-light);
        display: flex;
        justify-content: space-between;
        align-items: center;

        .selected-count {
          font-size: 13px;
          color: var(--el-color-primary);
          font-weight: normal;
        }
      }

      :deep(.el-scrollbar) {
        flex: 1;
        padding: 16px;

        .el-row {
          margin: 0 -8px;
        }

        .el-col {
          padding: 8px;
        }
      }

      .service-card {
        position: relative;
        height: 0;
        padding-bottom: 33.33%;
        border: 2px solid var(--el-border-color-light);
        border-radius: 6px;
        cursor: pointer;
        text-align: center;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        background-color: var(--el-bg-color);
        overflow: hidden;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
        
        &:hover {
          border-color: var(--el-color-primary-light-5);
          background-color: var(--el-color-primary-light-9);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }
        
        &.is-selected {
          background-color: var(--el-color-primary);
          border-color: var(--el-color-primary);
          transform: translateY(-2px);
          box-shadow: 0 4px 16px rgba(var(--el-color-primary-rgb), 0.2);
          
          .service-name {
            color: #ffffff;
            font-weight: 500;
          }

          &:hover {
            background-color: var(--el-color-primary-dark-1);
          }
        }
        
        .service-name {
          position: absolute;
          inset: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 8px 12px;
          font-size: 14px;
          color: var(--el-text-color-primary);
          text-align: center;
          transition: all 0.3s;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      }
    }
  }
}
</style> 