<template>
  <div class="navbar-container">
    <div class="navbar fixed-top">
      <div class="left-menu">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/back/dashboard' }">首页</el-breadcrumb-item>
          <template v-for="(item, index) in breadcrumbs" :key="index">
            <el-breadcrumb-item :to="item.path">{{ item.title }}</el-breadcrumb-item>
          </template>
        </el-breadcrumb>
      </div>
      
      <div class="right-menu">
        <div class="current-time">
          <el-icon><Clock /></el-icon>
          <span>{{ currentTime }}</span>
        </div>
        
        <div class="right-menu-item" @click="toggleFullScreen">
          <el-icon :size="20">
            <component :is="isFullscreen ? Aim : FullScreen" />
          </el-icon>
        </div>
        
        <el-dropdown trigger="click" popper-class="is-customized">
          <div class="avatar-wrapper">
            <el-avatar :size="32" :src="avatarUrl">
              {{ userInfo?.name?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="user-name">{{ userInfo?.name || userInfo?.username }}</span>
            <el-icon class="el-icon--right">
              <ArrowDown />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="user-info-panel">
                <div class="user-info-header">
                  <el-avatar :size="48" :src="avatarUrl">
                    {{ userInfo?.name?.charAt(0)?.toUpperCase() }}
                  </el-avatar>
                  <div class="user-info-main">
                    <div class="username">{{ userInfo?.name || userInfo?.username }}</div>
                    <div class="role-tags">
                      <el-tag size="small" :type="roleTagType">
                        {{ roleLabel }}
                      </el-tag>
                      <el-tag 
                        v-if="isStaffWithInfo" 
                        size="small" 
                        type="success"
                      >
                        员工ID: {{ staffInfo?.id }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                <div class="user-info-detail">
                  <div class="info-item">
                    <el-icon><Message /></el-icon>
                    <span>{{ userInfo?.email || '未设置邮箱' }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Iphone /></el-icon>
                    <span>{{ userInfo?.phone || '未设置手机' }}</span>
                  </div>
                  <template v-if="isStaff">
                    <div v-if="staffInfo" class="staff-info">
                      <div class="info-item">
                        <el-icon><Location /></el-icon>
                        <span>服务区域: {{ staffInfo.workArea || '未设置' }}</span>
                      </div>
             
                      <div class="info-item">
                        <el-icon><Star /></el-icon>
                        <span>评分: {{ staffInfo.rating || '暂无' }}</span>
                      </div>
                    </div>
                    <div v-else class="info-item">
                      <el-icon><Warning /></el-icon>
                      <span>正在加载员工信息...</span>
                    </div>
                  </template>
                </div>
              </div>
              <el-divider />
              <el-dropdown-item @click="handleProfile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="navbar-placeholder"></div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import fileUtils from '@/utils/fileUtils'
import { 
  ArrowDown, 
  User, 
  SwitchButton, 
  FullScreen, 
  Aim, 
  Clock,
  Message,
  Iphone,
  Location,
  Money,
  Star,
  Warning
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const isFullscreen = ref(false)
const currentTime = ref('')
let timer = null

const breadcrumbs = computed(() => {
  const matched = route.matched.slice(1)
  return matched.map(item => ({
    title: item.meta.title,
    path: item.path
  }))
})

const avatarUrl = computed(() => {
  return fileUtils.getImageUrl(userInfo.value?.avatar)
})

const staffInfo = computed(() => {
  console.log('Staff Info from store:', userStore.staffInfo)
  return userStore.staffInfo
})

const isStaff = computed(() => {
  console.log('Is Staff:', userStore.isStaff, 'Role Code:', userStore.userInfo?.roleCode)
  return userStore.isStaff
})

const isStaffWithInfo = computed(() => isStaff.value && staffInfo.value)

const roleTagType = computed(() => {
  if (userStore.isAdmin) return 'danger'
  if (userStore.isStaff) return 'warning'
  return 'info'
})

const roleLabel = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isStaff) return '服务人员'
  return '普通用户'
})

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

document.addEventListener('fullscreenchange', () => {
  isFullscreen.value = !!document.fullscreenElement
})

const handleProfile = () => {
  router.push('/back/user/person')
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

const updateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}


onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style lang="scss" scoped>
.navbar-container {
  width: 100%;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 24px 0 30px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 1000;

  &.fixed-top {
    position: fixed;
    top: 0;
    left: 200px;
    right: 0;
    width: calc(100% - 200px);
    transition: all 0.3s;
  }

  .left-menu {
    display: flex;
    align-items: center;
    gap: 16px;
    
    :deep(.el-breadcrumb__item) {
      .el-breadcrumb__inner {
        font-weight: normal;
        color: #606266;
        
        &.is-link {
          font-weight: normal;
          
          &:hover {
            color: var(--el-color-primary);
          }
        }
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 16px;
    height: 100%;
    
    .current-time {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
      font-size: 14px;
      margin-right: 8px;
      padding-right: 16px;
      border-right: 1px solid #e0e0e0;
    }

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #666;
      border-radius: 4px;
      transition: all 0.3s;
      height: 32px;
      width: 32px;
      
      &:hover {
        background: #f6f6f6;
        color: #333;
      }
    }
    
    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 8px;
      height: 32px;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s;
      
      &:hover {
        background: #f6f6f6;
      }
      
      :deep(.el-avatar) {
        flex-shrink: 0;
        width: 32px;
        height: 32px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 50%;
        }
      }
      
      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #666;
        line-height: 32px;
        white-space: nowrap;
      }

      .el-icon {
        color: #999;
        display: flex;
        align-items: center;
        flex-shrink: 0;
      }
    }
  }
}

.user-info-panel {
  .user-info-header {
    :deep(.el-avatar) {
      flex-shrink: 0;
      width: 48px;
      height: 48px;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
      }
    }
  }

  .role-tags {
    display: flex;
    gap: 8px;
    margin-top: 4px;
  }

  .user-info-detail {
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      color: #666;
      font-size: 13px;

      .el-icon {
        font-size: 16px;
      }

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.navbar-placeholder {
  height: 60px;
}

@media (max-width: 992px) {
  .navbar.fixed-top {
    left: 64px;
    width: calc(100% - 64px);
  }
}

@media (max-width: 768px) {
  .navbar.fixed-top {
    left: 0;
    width: 100%;
  }
}
</style> 