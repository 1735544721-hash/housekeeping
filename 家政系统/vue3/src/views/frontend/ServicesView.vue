<template>
  <div class="services-page">
    <!-- 页面加载动画 -->
    <el-skeleton :loading="loading" animated :count="3">
      <template #default>
        <div class="container animate__animated animate__fadeIn">
          <!-- 搜索框部分 -->
          <div class="search-section animate__animated animate__fadeIn">
            <div class="search-container">
              <el-autocomplete
                v-model="searchQuery"
                :fetch-suggestions="querySearch"
                placeholder="搜索服务、服务人员或服务分类..."
                :trigger-on-focus="true"
                class="global-search"
                @select="handleSearchSelect"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
                <template #default="{ item }">
                  <div class="search-item">
                    <el-icon><Folder /></el-icon>
                    <span>{{ item.value }}</span>
                    <el-tag size="small" type="info" effect="plain">分类</el-tag>
                  </div>
                </template>
              </el-autocomplete>
            </div>
          </div>

          <div class="page-header">
            <h1 class="page-title">全部服务分类</h1>
            <p class="page-desc">为您提供专业的家政服务解决方案</p>
          </div>

          <div class="category-list">
            <div v-for="(category, index) in categories" 
              :key="category.id" 
              class="category-item animate__animated animate__fadeInUp"
              :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <div class="category-header">
                <div class="title-wrapper">
                  <el-icon>
                    <component :is="getIcon(category.icon)" />
                  </el-icon>
                  <h2>{{ category.categoryName }}</h2>
                </div>
                <el-button 
                  v-if="!category.children?.length"
                  type="primary" 
                  link
                  @click="handleCategoryClick(category.id)"
                >
                  查看详情 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>

              <div class="sub-categories" v-if="category.children?.length">
                <el-card
                  v-for="subCategory in category.children"
                  :key="subCategory.id"
                  class="sub-category"
                  shadow="hover"
                  @click="handleCategoryClick(subCategory.id)"
                >
                  <div class="sub-category-content">
                    <div class="icon-wrapper">
                      <el-icon>
                        <component :is="getIcon(subCategory.icon)" />
                      </el-icon>
                    </div>
                    <h3>{{ subCategory.categoryName }}</h3>
                    <p v-if="subCategory.description" class="text-ellipsis">
                      {{ subCategory.description }}
                    </p>
                    <div class="hover-tip">
                      <el-icon><ArrowRight /></el-icon>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>

    <!-- 添加服务列表对话框 -->
    <service-list-dialog
      v-model="serviceListVisible"
      :category-id="selectedCategoryId"
      @close="handleDialogClose"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ArrowRight, Search, List, User, Folder } from '@element-plus/icons-vue'
import 'animate.css'
import ServiceListDialog from '@/components/frontend/ServiceListDialog.vue'

const router = useRouter()
const categories = ref([])
const loading = ref(true)
const serviceListVisible = ref(false)
const selectedCategoryId = ref(null)
const searchQuery = ref('')

// 获取ElementPlus图标
const getIcon = (iconName) => {
  return iconName ? ElementPlusIconsVue[iconName] : ElementPlusIconsVue.Grid
}

const getCategories = async () => {
  try {
    const res = await request.get('/category/tree', null, {
      showDefaultMsg: false
    })
    categories.value = res
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const handleCategoryClick = (id) => {
  selectedCategoryId.value = id
  serviceListVisible.value = true
}

const handleDialogClose = () => {
  selectedCategoryId.value = null
}

// 搜索建议
const querySearch = async (queryString, cb) => {
  if (queryString.length === 0) {
    cb([])
    return
  }

  try {
    // 使用全局搜索接口，但只处理分类结果
    const response = await request.get('/search/global', { keyword: queryString }, {
      showDefaultMsg: false
    })
    
    // 只处理服务分类结果
    const categories = (response?.categories || []).map(category => ({
      value: category.categoryName,
      type: 'category',
      id: category.id
    }))

    cb(categories)
  } catch (error) {
    console.error('搜索失败:', error)
    cb([])
  }
}

// 处理搜索选择
const handleSearchSelect = (item) => {
  // 只处理分类选择
  selectedCategoryId.value = item.id
  serviceListVisible.value = true
  searchQuery.value = ''
}

onMounted(async () => {
  loading.value = true
  try {
    await getCategories()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.services-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 0;
  padding-top: calc(40px + var(--el-header-height, 60px));
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
  background: linear-gradient(180deg, rgba(255,255,255,0) 0%, #fff 100%);
  padding: 32px 0;
  border-radius: 8px;
}

.page-title {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.page-desc {
  color: #909399;
  font-size: 16px;
}

.category-item {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #ebeef5;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-wrapper .el-icon {
  font-size: 24px;
  color: #4A90E2;
}

.category-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.sub-categories {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.sub-category {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
}

.sub-category:hover {
  transform: translateY(-2px);
}

.sub-category-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px;
  position: relative;
  min-height: 180px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.sub-category:hover .icon-wrapper {
  background: #4A90E2;
}

.icon-wrapper .el-icon {
  font-size: 24px;
  color: #4A90E2;
  transition: all 0.3s ease;
}

.sub-category:hover .icon-wrapper .el-icon {
  color: white;
}

.sub-category h3 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 12px;
}

.sub-category p {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
  flex-grow: 1;
}

.hover-tip {
  position: absolute;
  right: 16px;
  bottom: 16px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.sub-category:hover .hover-tip {
  opacity: 1;
  transform: translateX(0);
}

.hover-tip .el-icon {
  font-size: 16px;
  color: #4A90E2;
}

/* 动画持续时间 */
.animate__animated {
  animation-duration: 0.8s;
}

.search-section {
  padding: 20px 0;
  background-color: #ffffff00;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.00);
  margin-bottom: 20px;
}

.search-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

.global-search {
  width: 500px;
}

:deep(.el-input__wrapper) {
  border-radius: 24px;
  padding: 4px 12px;
  height: 48px;
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-input__wrapper:hover) {
  background-color: var(--el-color-primary-light-8);
}

:deep(.el-input__wrapper.is-focus) {
  background-color: white;
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.search-item .el-icon {
  font-size: 16px;
  color: var(--el-text-color-secondary);
}

.search-item span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style> 