<template>
  <div class="logo-section">
    <div class="logo-icon">
      <span class="emoji">🏠</span>
    </div>
    <span class="title">家政服务系统</span>
  </div>
</template>

<style scoped>
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
  font-family: 'HarmonyOS Sans', sans-serif;
  background-image: linear-gradient(120deg, var(--el-color-primary), var(--el-color-primary-light-3));
  -webkit-background-clip: text;
  color: transparent;
}
</style> 