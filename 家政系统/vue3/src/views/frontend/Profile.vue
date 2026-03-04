<template>
  <div class="profile">
    <div class="profile-container">
      <h1 class="page-title">个人信息</h1>
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="基本信息" name="info">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <h2>基本信息</h2>
                <el-button type="primary" @click="handleUpdateInfo" :loading="loading" class="save-button">
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
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-card class="pwd-card">
            <template #header>
              <div class="card-header">
                <h2>修改密码</h2>
                <el-button type="primary" @click="handleUpdatePassword" :loading="pwdLoading" class="save-button">
                  确认修改
                </el-button>
              </div>
            </template>

            <el-form 
              ref="pwdFormRef"
              :model="pwdForm"
              :rules="pwdRules"
              label-width="100px"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input 
                  v-model="pwdForm.oldPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="pwdForm.newPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="pwdForm.confirmPassword"
                  type="password"
                  show-password
                />
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload as UploadIcon } from '@element-plus/icons-vue'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

// 加载状态
const loading = ref(false)
const pwdLoading = ref(false)

// 当前活动的选项卡
const activeTab = ref('info')

// 基本信息表单
const infoForm = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  name: '',
  address: '',
  avatar: '',
  gender: 1,
  age: null
})

// 密码表单
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 上传URL
const uploadUrl = fileUtils.getUploadUrl('img')

// 头像URL计算属性
const avatarUrl = computed(() => {
  return fileUtils.getImageUrl(infoForm.avatar)
})

// 基本信息验证规则
const infoRules = {
  email: [{ required: true, type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ required: true, message: '手机号格式不正确', trigger: 'blur' }],
  name: [{ max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }],
  address: [{ max: 200, message: '地址长度不能超过200个字符', trigger: 'blur' }],
  age: [{ type: 'number', min: 0, max: 150, message: '年龄必须在0到150之间', trigger: 'blur' }]
}

// 密码验证规则
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const userInfo = userStore.userInfo
    Object.assign(infoForm, userInfo)
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 更新基本信息
const handleUpdateInfo = () => {
  infoFormRef.value?.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        await request.put('/user/info', infoForm, {
          successMsg: '个人信息更新成功'
        })
        userStore.updateUserInfo({ ...userStore.userInfo, ...infoForm })
      } finally {
        loading.value = false
      }
    }
  })
}

// 更新密码
const handleUpdatePassword = () => {
  pwdFormRef.value?.validate(async valid => {
    if (valid) {
      pwdLoading.value = true
      try {
        await request.put(`/user/password/${infoForm.id}`, pwdForm, {
          successMsg: '密码修改成功，请重新登录'
        })
        await userStore.logout()
        router.push('/login')
      } finally {
        pwdLoading.value = false
      }
    }
  })
}

// 头像上传相关
const handleAvatarSuccess = (response) => {
  infoForm.avatar = response.data
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload = (file) => {
  const isImage = fileUtils.isImage(file)
  const isLt2M = fileUtils.checkFileSize(file, 2)

  if (!isImage) {
    ElMessage.error('头像必须是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
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
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid #e0e0e0;
  transition: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.card-header h2 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.save-button {
  border-radius: 8px;
  padding: 8px 16px;
  background-color: #4A90E2;
  color: white;
  border: none;
  transition: background-color 0.3s;
}

.save-button:hover {
  background-color: #6C5CE7;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4a5568 !important;
}

:deep(.el-input__inner) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.avatar-uploader :deep(.el-upload) {
  width: 120px;
  height: 120px;
  border: 2px dashed #cbd5e0;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #4A90E2;
  background: rgba(74, 144, 226, 0.05);
}

.avatar {
  border-radius: 50%;
  width: 120px;
  height: 120px;
  object-fit: cover;
  border: 2px solid #4A90E2;
  background-color: #f0f0f0;
}

.avatar-uploader-icon {
  background: rgba(74, 144, 226, 0.1);
  border-radius: 50%;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
  box-shadow: none;
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