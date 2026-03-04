<template>
  <div class="home">
    <!-- 轮播图部分 -->
    <div class="banner-section animate__animated animate__fadeIn">
      <el-carousel height="calc(1200px / 2.33)" :interval="5000">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <img :src="fileUtils.getImageUrl(banner.imageUrl)" :alt="banner.title" class="banner-img">
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 搜索框部分 -->
    <div class="search-section animate__animated animate__fadeIn">
      <div class="container">
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
                <el-icon v-if="item.type === 'service'"><List /></el-icon>
                <el-icon v-else-if="item.type === 'staff'"><User /></el-icon>
                <el-icon v-else-if="item.type === 'category'"><Folder /></el-icon>
                <span>{{ item.value }}</span>
                <el-tag 
                  size="small" 
                  :type="item.type === 'service' ? 'warning' : item.type === 'staff' ? 'success' : 'info'" 
                  effect="plain"
                >
                  {{ item.type === 'service' ? '服务' : item.type === 'staff' ? '服务人员' : '分类' }}
                </el-tag>
              </div>
            </template>
          </el-autocomplete>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="main-section animate__animated animate__fadeInUp">
        <!-- 左侧分类导航 -->
        <div class="category-nav">
          <h3>服务分类</h3>
          <el-menu class="category-menu" :default-active="activeCategory" :unique-opened="true">
            <el-sub-menu 
              v-for="category in categories.filter(c => c.children?.length)"
              :key="category.id"
              :index="category.id.toString()"
            >
              <template #title>
                <el-icon>
                  <component :is="getIcon(category.icon)" />
                </el-icon>
                <span>{{ category.categoryName }}</span>
              </template>
              <el-menu-item 
                v-for="child in category.children" 
                :key="child.id" 
                :index="child.id.toString()" 
                @click="handleCategoryClick(child.id)"
              >
                <el-icon>
                  <component :is="getIcon(child.icon)" />
                </el-icon>
                <span>{{ child.categoryName }}</span>
                <span v-if="child.description" class="category-desc">{{ child.description }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item 
              v-for="category in categories.filter(c => !c.children?.length)"
              :key="category.id"
              :index="category.id.toString()" 
              @click="handleCategoryClick(category.id)"
            >
              <el-icon>
                <component :is="getIcon(category.icon)" />
              </el-icon>
              <span>{{ category.categoryName }}</span>
              <span v-if="category.description" class="category-desc">{{ category.description }}</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 右侧内容区域 -->
        <div class="content-area">
          <!-- 热门推荐服务 -->
          <section class="section">
            <div class="section-header">
              <h2 class="section-title">热门推荐服务</h2>
              <el-button type="success" link @click="router.push('/services')">
                查看更多 <el-icon>
                  <ArrowRight />
                </el-icon>
              </el-button>
            </div>
            <div class="service-grid">
              <div v-for="(service, index) in recommendServices" 
                :key="service.id" 
                class="service-card animate__animated animate__fadeIn"
                :style="{ animationDelay: `${index * 0.1}s` }"
              >
                <div class="service-content" @click="handleServiceClick(service.id)">
                  <h3 class="service-title">{{ service.title }}</h3>
                  <p class="service-desc">{{ service.description }}</p>
                  <div class="service-footer">
                    <span class="price">¥{{ service.price }}</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click.stop="handleServiceClick(service.id)"
                    >
                      立即预约
                    </el-button>
                    <el-icon 
                      :class="{'is-favorited': service.isFavorited}" 
                      @click.stop="toggleFavorite(service.id)"
                      class="favorite-icon"
                    >
                      <Star />
                    </el-icon>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 优质服务人员 -->
          <section class="section">
            <div class="section-header">
              <h2 class="section-title">优质服务人员</h2>
              <el-button type="success" link @click="router.push('/staff')">
                查看更多 <el-icon>
                  <ArrowRight />
                </el-icon>
              </el-button>
            </div>
            <div class="staff-grid">
              <div v-for="(staff, index) in topStaff" 
                :key="staff.id" 
                class="staff-card animate__animated animate__fadeIn"
                :style="{ animationDelay: `${index * 0.1}s` }"
              >
                <div class="staff-avatar">
                  <template v-if="staff.user?.avatar">
                    <img :src="fileUtils.getImageUrl(staff.user.avatar)" :alt="staff.user?.name">
                  </template>
                  <div v-else class="avatar-fallback">
                    {{ staff.user?.name?.charAt(0) || '未' }}
                  </div>
                </div>
                <div class="staff-info">
                  <h3>{{ staff.user?.name }}</h3>
                  <div class="staff-rating">
                    <el-rate v-model="staff.rating" disabled show-score />
                  </div>
                  <p class="staff-exp">{{ staff.experience }}年经验</p>
                
                  <el-button type="primary" @click="handleStaffClick(staff.id)">
                    查看详情
                  </el-button>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- 添加服务人员详情对话框 -->
    <staff-detail-dialog
      v-model="staffDetailVisible"
      :staff-id="selectedStaffId"
      @close="handleStaffDialogClose"
    />

    <!-- 添加服务列表对话框 -->
    <service-list-dialog
      v-model="serviceListDialogVisible"
      :category-id="selectedCategoryId"
      @close="handleServiceListDialogClose"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ArrowRight, Star, Search, List, User, Folder } from '@element-plus/icons-vue'
import 'animate.css'
import StaffDetailDialog from '@/components/frontend/StaffDetailDialog.vue'
import ServiceListDialog from '@/components/frontend/ServiceListDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const banners = ref([])
const categories = ref([])
const recommendServices = ref([])
const topStaff = ref([])
const activeCategory = ref('1')
const staffDetailVisible = ref(false)
const selectedStaffId = ref(null)
const serviceListDialogVisible = ref(false)
const selectedCategoryId = ref(null)
const searchQuery = ref('')

// 获取轮播图
const getBanners = async () => {
  try {
    const res = await request.get('/banner/active', null, {
      showDefaultMsg: false
    })
    banners.value = res
  } catch (error) {
    console.error('获取轮播图失败:', error)
  }
}

// 获取分类树
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

// 获取推荐服务
const getRecommendServices = async () => {
  try {
    // 只有登录用户才获取个性化推荐
    if (userStore.isLoggedIn) {
      const userId = userStore.userInfo.id
      const res = await request.get(`/recommend/content-based/${userId}?limit=4`, null,{
        showDefaultMsg: false
      })
      recommendServices.value = res
    } else {
      // 未登录用户获取随机推荐
      const res = await request.get('/service/list?pageNum=1&pageSize=4', null,{
        showDefaultMsg: false,
        onSuccess:(data)=>{
          recommendServices.value = data.records
        }
      })
    }
    
    // 获取到推荐服务后立即检查收藏状态
    if (recommendServices.value.length > 0) {
      await checkFavoriteStatus()
    }
  } catch (error) {
    console.error('获取推荐服务失败:', error)
  }
}

// 获取优质服务人员
const getTopStaff = async () => {
  try {
    const res = await request.get('/staff/top-rated', null, {
      showDefaultMsg: false
    })
    topStaff.value = res.slice(0, 4)
  } catch (error) {
    console.error('获取优质服务人员失败:', error)
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!userStore.isLoggedIn) return;

  const userId = userStore.userInfo.id;
  try {
    for (const service of recommendServices.value) {
      const res = await request.get(`/favorite/check?userId=${userId}&serviceId=${service.id}`, null, {
        showDefaultMsg: false,
        onSuccess: (res) => {
          // 直接使用返回的布尔值
          service.isFavorited = res;
        }
      });
    }
  } catch (error) {
    console.error('获取收藏状态失败:', error);
  }
}

// 获取 ElementPlus 图标组件
const getIcon = (iconName) => {
  return iconName ? ElementPlusIconsVue[iconName] : ElementPlusIconsVue.Document
}

// 点击处理函数
const handleCategoryClick = (id) => {
  selectedCategoryId.value = id
  serviceListDialogVisible.value = true
}

const handleServiceClick = (serviceId) => {
  router.push(`/service/${serviceId}`)
}

const handleStaffClick = (staffId) => {
  selectedStaffId.value = staffId
  staffDetailVisible.value = true
}

// 处理对话框关闭
const handleStaffDialogClose = () => {
  selectedStaffId.value = null
  staffDetailVisible.value = false
}

// 处理服务列表对话框关闭
const handleServiceListDialogClose = () => {
  selectedCategoryId.value = null
  serviceListDialogVisible.value = false
}

// 收藏服务
const toggleFavorite = async (serviceId) => {
  if (!userStore.isLoggedIn) {
    console.error('用户未登录，无法收藏服务')
    return
  }

  const userId = userStore.userInfo.id; // 获取用户ID
  try {
    const service = recommendServices.value.find(s => s.id === serviceId);
    if (service.isFavorited) {
      // 取消收藏
      await request.delete(`/favorite?userId=${userId}&serviceId=${serviceId}`);
      service.isFavorited = false;
    } else {
      // 添加收藏
      await request.post('/favorite', { userId, serviceId });
      service.isFavorited = true;
    }
  } catch (error) {
    console.error('收藏服务失败:', error);
  }
}

// 搜索建议
const querySearch = async (queryString, cb) => {
  if (queryString.length === 0) {
    cb([])
    return
  }

  try {
    // 使用全局搜索接口
    const response = await request.get('/search/global', { keyword: queryString }, {
      showDefaultMsg: false
    })
    
    // 处理服务项目结果
    const services = (response?.services || []).map(service => ({
      value: service.title,
      type: 'service',
      id: service.id
    }))

    // 处理服务人员结果
    const staff = (response?.staff || []).map(staff => ({
      value: staff.user?.name || '未知用户',
      type: 'staff',
      id: staff.id
    }))

    // 处理服务分类结果
    const categories = (response?.categories || []).map(category => ({
      value: category.categoryName,
      type: 'category',
      id: category.id
    }))

    cb([...services, ...staff, ...categories])
  } catch (error) {
    console.error('搜索失败:', error)
    cb([])
  }
}

// 处理搜索选择
const handleSearchSelect = (item) => {
  if (item.type === 'service') {
    // 跳转到服务详情页面
    router.push(`/service/${item.id}`)
  } else if (item.type === 'staff') {
    // 打开服务人员详情对话框
    selectedStaffId.value = item.id
    staffDetailVisible.value = true
  } else if (item.type === 'category') {
    // 打开服务分类列表对话框
    selectedCategoryId.value = item.id
    serviceListDialogVisible.value = true
  }
  searchQuery.value = ''
}

onMounted(async () => {
  try {
    await Promise.all([
      getBanners(),
      getCategories(),
      getRecommendServices(),
      getTopStaff(),
    ])
  } catch (error) {
    console.error('加载首页数据失败:', error)
  }
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 15px;
}

.banner-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.main-section {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

.category-nav {
  width: 250px;
  background: white;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.category-nav h3 {
  margin: 0;
  padding: 20px;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #eee;
}

.category-menu {
  border-right: none;
  padding: 8px;
}

.category-menu :deep(.el-menu-item),
.category-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  padding: 0 16px;
  border-radius: 4px;
  margin: 4px 0;
}

.category-menu :deep(.el-menu-item:hover),
.category-menu :deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
}

.category-menu :deep(.el-menu-item.is-active) {
  background: #4a90e2;
  color: white;
}

.category-desc {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-area {
  flex: 1;
}

.section {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  color: #333;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #4a90e2;
  border-radius: 2px;
}

.service-grid,
.staff-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.service-card,
.staff-card {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.service-card:hover,
.staff-card:hover {
  border-color: #4a90e2;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(74, 144, 226, 0.1);
}

.service-card::after,
.staff-card::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(74, 144, 226, 0.1);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s ease-out, height 0.6s ease-out;
  pointer-events: none;
}

.service-card:hover::after,
.staff-card:hover::after {
  width: 300%;
  height: 300%;
}

.service-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 180px;
  cursor: pointer;
  padding: 10px;
  transition: background-color 0.3s ease;
}

.service-content:hover {
  background-color: var(--el-fill-color-light);
}

.service-title {
  font-size: 16px;
  color: #303133;
  margin: 0;
}

.service-desc {
  color: #606266;
  font-size: 14px;
  margin: 12px 0;
  flex-grow: 1;
  line-height: 1.5;
}

.service-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price {
  color: #ff6b6b;
  font-size: 16px;
  font-weight: 500;
}

.price::before {
  content: "起价";
  font-size: 12px;
  color: #909399;
  margin-right: 4px;
}

.staff-avatar {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #4a90e2;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  background: #4a90e2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.staff-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.staff-info {
  text-align: center;
}

.staff-rating {
  margin: 12px 0;
}

.staff-exp {
  color: #666;
  margin: 8px 0;
}

.staff-price {
  color: #ff6b6b;
  font-weight: bold;
  margin: 8px 0 16px;
}

:deep(.el-button) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button:hover) {
  transform: translateY(-1px);
}

:deep(.el-button:active) {
  transform: translateY(1px);
}

.category-menu :deep(.el-menu-item),
.category-menu :deep(.el-sub-menu__title) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.category-menu :deep(.el-sub-menu__title:hover .el-icon),
.category-menu :deep(.el-menu-item:hover .el-icon) {
  transform: rotate(15deg);
  transition: transform 0.3s ease;
}

.staff-avatar {
  transition: transform 0.3s ease, border-color 0.3s ease;
}

.staff-card:hover .staff-avatar {
  transform: scale(1.05);
  border-color: #6C5CE7;
}

.staff-rating :deep(.el-rate__icon) {
  transition: transform 0.2s ease;
}

.staff-rating:hover :deep(.el-rate__icon) {
  transform: scale(1.2);
}

.animate__animated {
  animation-duration: 0.8s;
}

.content-area {
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.favorite-icon {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
}

.is-favorited {
  color: red;
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