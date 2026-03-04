<template>
  <div class="person-info">
    <el-row :gutter="20">
      <!-- 基本信息卡片 -->
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

      <!-- 密码修改卡片 -->
      <el-col :span="10">
        <el-card class="pwd-card">
          <template #header>
            <div class="card-header">
              <h2>修改密码</h2>
              <el-button type="primary" @click="handleUpdatePassword" :loading="pwdLoading">
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
      </el-col>
    </el-row>
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
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (value && !emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const infoRules = {
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  name: [{ max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }],
  address: [{ max: 200, message: '地址长度不能超过200个字符', trigger: 'blur' }],
  age: [{ type: 'number', min: 0, max: 150, message: '年龄必须在0到150之间', trigger: 'blur' }]
}

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
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
        // 更新 store 中的用户信息
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
        // 使用 store 的 logout 方法清理用户状态
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

<style lang="scss" scoped>
.person-info {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h2 {
    font-size: 18px;
    color: #2c3e50;
    margin: 0;
    font-weight: 600;
  }
}

.info-card, .pwd-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.avatar-uploader {
  :deep(.el-upload) {
    width: 100px;
    height: 100px;
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.upload-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader-icon {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  color: #8c939d;
  
  .upload-tip {
    font-size: 12px;
    margin-top: 8px;
  }
}

.avatar {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

:deep(.el-input-number) {
  width: 180px;
}

:deep(.el-radio-group) {
  width: 180px;
  display: flex;
  gap: 30px;
}

:deep(.el-form-item.avatar-uploader) {
  margin-bottom: 30px;
  .el-form-item__content {
    display: flex;
    justify-content: center;
  }
}
</style> 