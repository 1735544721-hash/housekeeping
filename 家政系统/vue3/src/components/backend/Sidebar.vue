<template>
  <div class="sidebar-container">
    <div class="logo">
      <span class="logo-icon">🏠</span>
      <span class="logo-text">家政服务系统</span>
    </div>
    <div class="menu-wrapper">
      <el-menu :default-active="activeMenu" mode="vertical" class="sidebar-menu"
        text-color="#bfcbd9" active-text-color="#409EFF" router>
        <template v-for="menu in menus" :key="menu.path">
          <!-- 没有子菜单 -->
          <el-menu-item v-if="!menu.children?.length" :index="'/back' + menu.path">
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <template #title>{{ menu.name }}</template>
          </el-menu-item>
          
          <!-- 有子菜单 -->
          <el-sub-menu v-else :index="'/back' + menu.path">
            <template #title>
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </template>
            
            <!-- 递归渲染子菜单 -->
            <template v-for="child in menu.children" :key="child.path">
              <template v-if="!child.children?.length">
                <el-menu-item :index="'/back' + child.path">
                  <el-icon v-if="child.icon">
                    <component :is="child.icon" />
                  </el-icon>
                  <template #title>{{ child.name }}</template>
                </el-menu-item>
              </template>
              
              <el-sub-menu v-else :index="'/back' + child.path">
                <template #title>
                  <el-icon v-if="child.icon">
                    <component :is="child.icon" />
                  </el-icon>
                  <span>{{ child.name }}</span>
                </template>
                
                <el-menu-item v-for="grandChild in child.children" 
                  :key="grandChild.path" 
                  :index="'/back' + grandChild.path">
                  <el-icon v-if="grandChild.icon">
                    <component :is="grandChild.icon" />
                  </el-icon>
                  <template #title>{{ grandChild.name }}</template>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

// 获取菜单数据
const menus = computed(() => userStore.menus)

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return '/back' + meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  width: 220px;
  border-right: 1px solid #f0f0f0;
  
  .logo {
    height: 64px;
    width: 220px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding: 0 24px;
    overflow: hidden;
    
    .logo-icon {
      font-size: 28px;
      margin-right: 16px;
      color: #4A90E2;
      flex-shrink: 0;
    }
    
    .logo-text {
      color: #333333;
      font-size: 18px;
      font-weight: 600;
      font-family: 'HarmonyOS Sans SC', sans-serif;
      white-space: nowrap;
      letter-spacing: 1px;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    background: transparent;

    .el-menu-item, .el-sub-menu__title {
      height: 48px;
      line-height: 48px;
      color: #666666;
      font-family: 'HarmonyOS Sans SC', sans-serif;
      font-size: 14px;
      margin: 4px 12px;
      border-radius: 4px;
      padding: 0 16px !important;
      transition: all 0.2s ease;
      
      .el-icon {
        width: 24px;
        text-align: center;
        font-size: 18px;
        margin-right: 12px;
      }
    }

    .el-menu-item.is-active {
      background: #4A90E2 !important;
      color: #ffffff !important;
      font-weight: 500;
      
      .el-icon {
        color: #ffffff;
      }
    }

    .el-sub-menu {
      &.is-opened {
        > .el-sub-menu__title {
          color: #4A90E2;
          background: rgba(74, 144, 226, 0.08);
          
          .el-icon {
            color: #4A90E2;
          }
        }
      }

      .el-menu {
        background: transparent;
        padding: 0 12px;
        
        .el-menu-item {
          background: transparent;
          min-width: auto;
          margin: 4px 0;
          
          &:hover {
            color: #4A90E2;
            background: rgba(74, 144, 226, 0.08) !important;
          }
          
          &.is-active {
            background: #4A90E2 !important;
            color: #ffffff !important;
          }
        }
      }
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #e8e8e8;
      border-radius: 2px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }
}
</style> 
