<template>
  <div class="profile">
    <div class="profile-container">
      <h1 class="page-title">个人信息</h1>
      <el-row justify="center">
        <el-col :span="14">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <h2>基本信息</h2>
                <el-button type="primary" @click="handleUpdateInfo" :loading="loading">
                  保存修改
                </el-button>
              </div>
            </template>
            
            <el-form 
              ref="infoFormRef"
              :model="infoForm"
              :rules="infoRules"
              label-width="100px"
            >
              <el-form-item label="头像" prop="avatar" class="avatar-uploader">
                <el-upload
                  class="avatar-upload"
                  :action="uploadUrl"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <div class="upload-content">
                    <img v-if="infoForm.avatar" :src="avatarUrl" class="avatar" />
                    <div v-else class="avatar-uploader-icon">
                      <el-icon><UploadIcon /></el-icon>
                      <div class="upload-tip">点击上传头像</div>
                    </div>
                  </div>
                </el-upload>
              </el-form-item>

              <el-form-item label="用户名">
                <el-input v-model="infoForm.username" disabled />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="infoForm.email" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="infoForm.phone" />
              </el-form-item>

              <el-form-item label="姓名" prop="name">
                <el-input v-model="infoForm.name" />
              </el-form-item>

              <el-form-item label="地址" prop="address">
                <el-input v-model="infoForm.address" type="textarea" rows="2" />
              </el-form-item>

              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="infoForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="0">女</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="infoForm.age" :min="0" :max="150" />
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
// ... script 部分保持不变 ...
</script>

<style lang="scss" scoped>
.profile {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 100px 20px 40px;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  animation: fadeIn 0.6s ease;
}

.page-title {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
  position: relative;
  padding-bottom: 10px;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4A90E2 0%, #6C5CE7 100%);
  border-radius: 2px;
}

.info-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: none; /* 去掉阴影效果 */
  border: none;
  transition: transform 0.3s ease;
}

.info-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px 12px 0 0;
}

.card-header h2 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
  letter-spacing: 0.5px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4a5568 !important;
}

:deep(.el-input__inner) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus) {
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.avatar-uploader {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;

  :deep(.el-upload) {
    width: 120px;
    height: 120px;
    border: 2px dashed #cbd5e0;
    border-radius: 50%; /* 确保头像区域为圆形 */
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #4A90E2;
      background: rgba(74, 144, 226, 0.05);
    }
  }
}

.avatar {
  border-radius: 50%; /* 确保头像为圆形 */
  width: 100%; /* 适应上传区域 */
  height: 100%; /* 适应上传区域 */
  object-fit: cover; /* 保持图片比例 */
}

.avatar-uploader-icon {
  background: rgba(74, 144, 226, 0.1);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 120px;
  height: 120px;
}

.avatar-uploader-icon .upload-tip {
  color: #4A90E2;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #4A90E2 0%, #6C5CE7 100%);
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: none; /* 去掉按钮阴影效果 */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-container {
    padding: 0 15px;
  }
  
  .el-col {
    width: 100% !important;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 