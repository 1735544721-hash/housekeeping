<template>
    <div class="frontend-layout">
      <NavHeader />
      <main class="main-content">
        <router-view />
      </main>
      <NavFooter />
    </div>
  </template>
  
  <script setup>
  import NavHeader from '@/components/frontend/NavHeader.vue'
  import NavFooter from '@/components/frontend/NavFooter.vue'
  </script>
  
  <style scoped>
  .frontend-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background: linear-gradient(135deg, #4A90E2 0%, #6C5CE7 100%);
  }
  
  .main-content {
    flex: 1;
    background: #f5f7fa;
  }
  </style>