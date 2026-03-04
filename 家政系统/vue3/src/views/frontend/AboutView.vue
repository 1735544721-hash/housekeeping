<script setup>
import { Star, User, Check, Service, Phone, Clock, Location, Message } from '@element-plus/icons-vue'
</script>

<template>
  <div class="about-page">
    <div class="container">
      <div class="page-header">
        <h1>关于我们</h1>
      </div>
      
      <div class="about-content">
        <!-- 公司简介 -->
        <section class="about-section">
          <div class="section-header">
            <el-icon><Service /></el-icon>
            <h2>公司简介</h2>
          </div>
          <div class="section-content">
            <p>我们是一家专业的家政服务平台，致力于为用户提供优质、便捷的家政服务。平台汇集了众多经验丰富的家政服务人员，提供包括家庭保洁、母婴护理、老人照护等多种服务。</p>
          </div>
        </section>

        <!-- 服务理念 -->
        <section class="about-section">
          <div class="section-header">
            <el-icon><Star /></el-icon>
            <h2>服务理念</h2>
          </div>
          <div class="values-grid">
            <div class="value-item">
              <el-icon><Check /></el-icon>
              <h3>专业品质</h3>
              <p>严格筛选服务人员，确保服务质量</p>
            </div>
            <div class="value-item">
              <el-icon><Service /></el-icon>
              <h3>贴心服务</h3>
              <p>以客户需求为中心，提供个性化解决方案</p>
            </div>
            <div class="value-item">
              <el-icon><User /></el-icon>
              <h3>安全保障</h3>
              <p>完善的保险体系，保障双方权益</p>
            </div>
          </div>
        </section>

        <!-- 服务范围 -->
        <section class="about-section">
          <div class="section-header">
            <el-icon><Service /></el-icon>
            <h2>服务范围</h2>
          </div>
          <div class="services-grid">
            <div class="service-item">
              <h4>家庭保洁</h4>
              <p>日常保洁、深度清洁、开荒保洁等</p>
            </div>
            <div class="service-item">
              <h4>母婴护理</h4>
              <p>月嫂服务、育儿嫂、产后护理等</p>
            </div>
            <div class="service-item">
              <h4>老人照护</h4>
              <p>生活照料、康复护理、陪诊服务等</p>
            </div>
            <div class="service-item">
              <h4>家电维护</h4>
              <p>家电清洗、维修、保养服务等</p>
            </div>
          </div>
        </section>

        <!-- 联系我们 -->
        <section class="about-section">
          <div class="section-header">
            <el-icon><Phone /></el-icon>
            <h2>联系我们</h2>
          </div>
          <div class="contact-grid">
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <div class="contact-info">
                <h4>客服电话</h4>
                <p>400-123-4567</p>
              </div>
            </div>
            <div class="contact-item">
              <el-icon><Clock /></el-icon>
              <div class="contact-info">
                <h4>工作时间</h4>
                <p>周一至周日 9:00-21:00</p>
              </div>
            </div>
            <div class="contact-item">
              <el-icon><Location /></el-icon>
              <div class="contact-info">
                <h4>公司地址</h4>
                <p>北京市朝阳区xxx大厦</p>
              </div>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <div class="contact-info">
                <h4>电子邮箱</h4>
                <p>service@example.com</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.about-page {
  min-height: 100vh;
  background-color: #F8FAFC;
  padding: 40px 0;
  padding-top: calc(40px + var(--el-header-height, 60px));
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-size: 28px;
    color: #2C3E50;
    margin: 0;
    font-weight: 600;
  }
}

.about-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  .section-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;

    h2 {
      font-size: 20px;
      color: #2C3E50;
      margin: 0;
      font-weight: 600;
    }

    .el-icon {
      font-size: 24px;
      color: #4A90E2;
    }
  }

  .section-content {
    color: #64748B;
    line-height: 1.8;
    font-size: 15px;
  }
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;

  .value-item {
    background: #F8FAFC;
    padding: 24px;
    border-radius: 8px;
    text-align: center;
    transition: transform 0.3s ease;

    &:hover {
      transform: translateY(-4px);
    }

    .el-icon {
      font-size: 32px;
      color: #4A90E2;
      margin-bottom: 16px;
    }

    h3 {
      font-size: 18px;
      color: #2C3E50;
      margin: 0 0 12px;
    }

    p {
      color: #64748B;
      margin: 0;
      line-height: 1.6;
    }
  }
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;

  .service-item {
    background: #F8FAFC;
    padding: 20px;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #4A90E2 0%, #6C5CE7 100%);
      color: white;

      h4, p {
        color: white;
      }
    }

    h4 {
      font-size: 16px;
      color: #2C3E50;
      margin: 0 0 8px;
    }

    p {
      color: #64748B;
      margin: 0;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;

  .contact-item {
    display: flex;
    align-items: center;
    gap: 16px;
    background: #F8FAFC;
    padding: 20px;
    border-radius: 8px;

    .el-icon {
      font-size: 24px;
      color: #4A90E2;
    }

    .contact-info {
      h4 {
        font-size: 16px;
        color: #2C3E50;
        margin: 0 0 4px;
      }

      p {
        color: #64748B;
        margin: 0;
        font-size: 14px;
      }
    }
  }
}

@media (max-width: 768px) {
  .values-grid,
  .services-grid,
  .contact-grid {
    grid-template-columns: 1fr;
  }

  .about-section {
    padding: 20px;
  }
}
</style> 