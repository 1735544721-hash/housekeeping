<template>
  <div>
    <div class="nav-wrapper">
      <el-menu
        class="nav-header"
        mode="horizontal"
        :ellipsis="false"
        router
      >
        <div class="logo-container">
          <el-menu-item index="/" class="logo-menu-item">
            <Logo />
          </el-menu-item>
        </div>

        <el-menu-item index="/">
          <el-icon><House /></el-icon>首页
        </el-menu-item>
        <el-menu-item index="/services">
          <el-icon><List /></el-icon>服务分类
        </el-menu-item>
        <el-menu-item index="/staff">
          <el-icon><User /></el-icon>服务人员
        </el-menu-item>
        <!-- <el-menu-item index="/ai-chat">
          <el-icon><ChatDotRound /></el-icon>智能客服
        </el-menu-item> -->
        <el-menu-item index="/about">
          <el-icon><InfoFilled /></el-icon>关于我们
        </el-menu-item>

        <div class="flex-grow" />
        
        <div class="search-container">
          <el-autocomplete
            v-model="searchQuery"
            :fetch-suggestions="querySearch"
            placeholder="搜索服务或服务人员..."
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

        <div class="auth-section">
          <template v-if="isLoggedIn">
            <el-dropdown trigger="click">
              <div class="user-profile-link">
                <el-avatar 
                  :size="32" 
                  :src="avatarUrl" 
                  class="user-avatar"
                >
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || '个人中心' }}</span>
                <el-icon class="el-icon--right"><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">
                    <el-icon><User /></el-icon>
                    <span>个人信息</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my-orders')">
                    <el-icon><Document /></el-icon>
                    <span>我的订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/browse-history')">
                    <el-icon><Clock /></el-icon>
                    <span>浏览历史</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click="showFavorites">
                    <el-icon><Star /></el-icon>
                    <span>我的收藏</span>
                  </el-dropdown-item>

                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button 
              link 
              class="login-btn"
              @click="$router.push('/login')"
            >
              登录
            </el-button>
            <el-button 
              type="primary" 
              class="register-btn"
              @click="$router.push('/register')"
            >
              注册
            </el-button>
          </template>
        </div>
      </el-menu>
    </div>

    <MyFavoritesDialog
      v-model="favoritesDialogVisible"
      @close="favoritesDialogVisible = false"
    />
    
    <StaffDetailDialog
      v-model="staffDetailVisible"
      :staff-id="selectedStaffId"
    />

    <ServiceListDialog
      v-model="serviceListVisible"
      :category-id="selectedCategoryId"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import fileUtils from '@/utils/fileUtils'
import request from '@/utils/request'
import { 
  User, 
  UserFilled,
  CaretBottom, 
  List, 
  InfoFilled,
  Document,
  SwitchButton,
  Star,
  Clock,
  Message,
  Search,
  House,
  Folder
} from '@element-plus/icons-vue'
import Logo from '@/components/common/Logo.vue'
import MyFavoritesDialog from './MyFavoritesDialog.vue'
import StaffDetailDialog from './StaffDetailDialog.vue'
import ServiceListDialog from './ServiceListDialog.vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const isLoggedIn = computed(() => !!userStore.token)
const favoritesDialogVisible = ref(false)
const staffDetailVisible = ref(false)
const selectedStaffId = ref(null)
const serviceListVisible = ref(false)
const selectedCategoryId = ref(null)

// 计算头像URL
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  return avatar ? fileUtils.getImageUrl(avatar) : ''
})

const handleLogout = () => {
  userStore.clearUserInfo()
  router.push('/login')
}

const showFavorites = () => {
  favoritesDialogVisible.value = true
}

const searchQuery = ref('')
const loading = ref(false)

// 搜索建议
const querySearch = async (queryString, cb) => {
  if (queryString.length === 0) {
    cb([])
    return
  }

  loading.value = true
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
  } finally {
    loading.value = false
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
    serviceListVisible.value = true
  }
  searchQuery.value = ''
}
</script>

<style scoped>
.nav-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: #fff;
}

.nav-header {
  height: 64px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  padding: 0 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo-menu-item {
  padding: 0 20px !important;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--el-color-primary-light-9);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.05);
}

.emoji {
  font-size: 24px;
  line-height: 1;
}

.title {
  font-size: 18px;
  font-weight: 600;
  background-image: linear-gradient(120deg, var(--el-color-primary), var(--el-color-primary-light-3));
  -webkit-background-clip: text;
  color: transparent;
}

.flex-grow {
  flex-grow: 1;
}

.auth-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: 24px;
  height: 100%;
}

:deep(.el-menu-item) {
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 4px;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}

.user-profile-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 6px;
  transition: all 0.3s;
}

.user-profile-link:hover {
  background: var(--el-color-primary-light-9);
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.user-avatar {
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  flex-shrink: 0;
  width: 32px;
  height: 32px;
}

/* 添加头像图片样式 */
.user-avatar :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-btn {
  font-size: 14px;
}

.register-btn {
  padding: 8px 20px;
  border-radius: 6px;
}

.search-container {
  display: flex;
  align-items: center;
  margin: 0 16px;
  height: 100%;
}

.global-search {
  width: 220px;
}

:deep(.el-input__wrapper) {
  border-radius: 20px;
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