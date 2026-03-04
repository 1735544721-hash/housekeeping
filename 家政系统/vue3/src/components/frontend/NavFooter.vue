<template>
  <footer class="nav-footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-logo">
          <Logo />
        </div>
        <div class="footer-description">
          <p>专业、贴心的家政服务平台，为您的生活增添便利与舒适</p>
        </div>
        <div class="footer-contact">
          <div class="contact-info">
            <span>客服热线：400-888-8888</span>
            <span>邮箱：service@homecare.com</span>
          </div>
        </div>
      </div>
      <div class="copyright">
        版权所有 © {{ new Date().getFullYear() }} 家政服务平台 | 京ICP备12345678号
      </div>
    </div>
  </footer>
</template>

<script setup>
import Logo from '@/components/common/Logo.vue'
</script>

<style scoped>
.nav-footer {
  background: linear-gradient(135deg, #4A90E2 0%, #6C5CE7 50%);
  color: white;
  padding: 20px 0;
  margin-top: auto;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.footer-logo {
  display: flex;
  align-items: center;
}

/* 修改Logo组件的样式，使其适应深色背景 */
.footer-logo :deep(.logo-icon) {
  background: rgba(255, 255, 255, 0.2);
}

.footer-logo :deep(.title) {
  color: white !important;
  background-image: none;
}

.footer-description {
  max-width: 300px;
  font-size: 14px;
  opacity: 0.8;
}

.footer-contact {
  display: flex;
  flex-direction: column;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14px;
}

.copyright {
  text-align: center;
  font-size: 12px;
  opacity: 0.6;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 10px;
}
</style> 