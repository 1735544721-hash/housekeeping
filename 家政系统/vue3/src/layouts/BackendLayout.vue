<template>
  <div class="backend-layout">
    <!-- 侧边栏 -->
    <Sidebar class="sidebar" />

    <div class="main-container">
      <!-- 顶部导航栏 -->
      <Navbar class="navbar" />
      
      <!-- 主要内容区域 -->
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from '@/components/backend/Sidebar.vue'
import Navbar from '@/components/backend/Navbar.vue'
</script>

<style lang="scss" scoped>
.backend-layout {
  display: flex;
  min-height: 100vh;
  background: #fafafa;
  width: 100%;
  overflow-x: hidden;

  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1001;
    width: 220px;
  }

  .main-container {
    flex: 1;
    min-height: 100vh;
    margin-left: 220px;
    position: relative;
    width: calc(100% - 220px);
    
    .navbar {
      position: fixed;
      top: 0;
      right: 0;
      left: 220px;
      height: 64px;
      z-index: 1000;
      background: #ffffff;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .main-content {
      padding: 84px 24px 24px;
      min-height: calc(100vh - 64px);
      width: 100%;
      box-sizing: border-box;
      overflow-x: hidden;
    }
  }
}
</style>