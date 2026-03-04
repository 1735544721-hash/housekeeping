<template>
  <div class="banner-list">
    <!-- 搜索和操作区域 -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="left">
          <h2 class="page-title">轮播图管理</h2>
          <el-tag type="info" effect="plain" class="banner-count">
            {{ total }} 个轮播图
          </el-tag>
        </div>
        <div class="right">
          <el-input
            v-model="searchForm.title"
            placeholder="搜索轮播图标题"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增轮播图
          </el-button>
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>

      <!-- 表格区域 -->
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        border
      >
        <el-table-column prop="imageUrl" label="图片" width="120">
          <template #default="{ row }">
            <el-image 
              :src="fileUtils.getImageUrl(row.imageUrl)" 
              :preview-src-list="[fileUtils.getImageUrl(row.imageUrl)]"
              fit="cover"
              class="banner-image"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="tag" label="标签" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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

    <!-- 轮播图表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="680px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <div class="upload-container">
            <el-upload
              class="banner-uploader"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              @change="handleImageSelect"
            >
              <el-image
                v-if="form.imageUrl"
                :src="fileUtils.getImageUrl(form.imageUrl)"
                fit="cover"
                class="banner-preview"
              />
              <div v-else class="upload-trigger">
                <el-icon><Plus /></el-icon>
                <span>点击上传图片</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            rows="3"
            placeholder="请输入图片描述" 
          />
        </el-form-item>
        <el-form-item label="标签" prop="tag">
          <el-input v-model="form.tag" placeholder="请输入标签" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 裁剪对话框 -->
    <el-dialog
      v-model="cropperVisible"
      title="图片裁剪"
      width="800px"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
    >
      <div class="cropper-container">
        <VueCropper
          ref="cropperRef"
          :img="cropperImage"
          :autoCrop="true"
          :fixedBox="true"
          :fixed="true"
          :fixedNumber="[21, 9]"
          :centerBox="true"
          :info="true"
          :outputSize="1"
          :outputType="'png'"
          :canMove="true"
          :canMoveBox="true"
          :canScale="true"
          :high="true"
          :mode="'contain'"
          style="height: 400px; width: 100%"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cropperVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCropUpload">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Plus } from '@element-plus/icons-vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'
import fileUtils from '@/utils/fileUtils'

// 搜索表单
const searchForm = reactive({
  title: '',
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
const dialogTitle = ref('新增轮播图')
const formRef = ref(null)
const form = reactive({
  id: '',
  title: '',
  imageUrl: '',
  description: '',
  tag: '',
  status: 1
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入轮播图标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请上传轮播图片', trigger: 'change' }
  ],
  description: [
    { max: 255, message: '描述长度不能超过255个字符', trigger: 'blur' }
  ],
  tag: [
    { max: 50, message: '标签长度不能超过50个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取轮播图列表
const fetchBanners = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await request.get('/banner/page', params, {
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

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchBanners()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBanners()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBanners()
}

// 新增轮播图
const handleAdd = () => {
  dialogTitle.value = '新增轮播图'
  dialogVisible.value = true
  form.id = ''
}

// 编辑轮播图
const handleEdit = (row) => {
  dialogTitle.value = '编辑轮播图'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除轮播图
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该轮播图吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/banner/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchBanners()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除轮播图失败:', error)
    }
  }
}

// 图片裁剪相关
const cropperVisible = ref(false)
const cropperImage = ref('')
const cropperRef = ref(null)
const currentFile = ref(null)

// 图片选择
const handleImageSelect = (file) => {
  if (!file) return
  
  if (!fileUtils.isImage(file.raw)) {
    ElMessage.error('只能上传图片文件!')
    return
  }
  
  if (!fileUtils.checkFileSize(file.raw, 2)) {
    ElMessage.error('图片大小不能超过 2MB!')
    return
  }

  currentFile.value = file.raw
  
  // 读取图片文件
  const reader = new FileReader()
  reader.onload = () => {
    cropperImage.value = ''  // 先清空，确保重新渲染
    nextTick(() => {
      cropperImage.value = reader.result
      cropperVisible.value = true
    })
  }
  reader.readAsDataURL(file.raw)
}

// 处理裁剪并上传
const handleCropUpload = () => {
  if (!cropperRef.value || !currentFile.value) return

  try {
    cropperRef.value.getCropBlob(async (blob) => {
      const file = new File([blob], currentFile.value.name, { type: 'image/png' })
      const formData = new FormData()
      formData.append('file', file)

      await request.post('/file/upload/img', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        successMsg: '图片上传成功',
        onSuccess: (res) => {
          form.imageUrl = res
          cropperVisible.value = false
        }
      })
    })
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/banner/${form.id}`, form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/banner', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchBanners()
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
    imageUrl: '',
    description: '',
    tag: '',
    status: 1
  })
}

// 刷新
const handleRefresh = () => {
  fetchBanners()
  ElMessage.success('刷新成功')
}

onMounted(() => {
  fetchBanners()
})
</script>

<style lang="scss" scoped>
.banner-list {
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

.banner-count {
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

// 表格中的图片样式
.banner-image {
  width: 105px;
  height: 45px;
  border-radius: 4px;
  cursor: pointer;
}

// 上传组件样式
.upload-container {
  width: 100%;
  
  .banner-uploader {
    :deep(.el-upload) {
      width: 420px;
      height: 180px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px dashed var(--el-border-color);
      border-radius: 4px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }
  }

  .upload-trigger {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: var(--el-text-color-secondary);
    
    .el-icon {
      font-size: 28px;
      margin-bottom: 8px;
    }
  }

  .banner-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

// 分页样式
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

// 表格样式优化
:deep(.el-table) {
  .el-table__header-wrapper th {
    background: #f8fafc;
    color: #1e293b;
    font-weight: 600;
  }
  
  .el-table__row {
    .el-button {
      padding: 4px 8px;
    }
  }
}

// 裁剪组件样式
.cropper-container {
  width: 100%;
  height: 400px;
  background-color: #f5f7fa;
  overflow: hidden;

  :deep(.vue-cropper) {
    width: 100%;
    height: 100%;
    
    .cropper-box {
      background-color: #f5f7fa;
    }
  }
}
</style> 