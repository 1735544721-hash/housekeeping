<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-header">
        <h1 class="title">用户注册</h1>
      </div>
      <el-form 
        :model="registerForm" 
        :rules="rules" 
        ref="registerFormRef" 
        class="auth-form"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="avatar" class="avatar-uploader">
          <el-upload
            class="avatar-upload"
            action="/api/file/upload/img"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <div class="upload-content">
              <img v-if="registerForm.avatar" :src="'api'+registerForm.avatar" class="avatar" />
              <div v-else class="avatar-uploader-icon">
                <el-icon><UploadIcon /></el-icon>
                <div class="upload-tip">点击上传头像</div>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username"
            :prefix-icon="User"
            placeholder="请输入用户名">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password"
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword"
            :prefix-icon="Lock"
            type="password"
            placeholder="请确认密码"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email"
            :prefix-icon="Message"
            placeholder="请输入邮箱">
          </el-input>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input 
            v-model="registerForm.phone"
            :prefix-icon="Phone"
            placeholder="请输入手机号">
          </el-input>
        </el-form-item>
        <el-form-item prop="name">
          <el-input 
            v-model="registerForm.name"
            :prefix-icon="UserFilled"
            placeholder="请输入姓名">
          </el-input>
        </el-form-item>
        <el-form-item prop="idCard">
          <el-input 
            v-model="registerForm.idCard"
            :prefix-icon="Document"
            placeholder="请输入身份证号">
          </el-input>
        </el-form-item>
        <el-form-item prop="address">
          <el-input 
            v-model="registerForm.address"
            :prefix-icon="Location"
            placeholder="请输入地址">
          </el-input>
        </el-form-item>
        <el-form-item prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="age">
          <el-input-number 
            v-model="registerForm.age" 
            :min="0" 
            :max="150"
            placeholder="请输入年龄">
          </el-input-number>
        </el-form-item>
        <el-form-item prop="roleCode">
          <el-select 
            v-model="registerForm.roleCode" 
            placeholder="请选择身份"
            :prefix-icon="UserFilled"
            style="width: 100%"
            @change="handleRoleChange">
            <el-option 
              v-for="role in roleOptions" 
              :key="role.code" 
              :label="role.name" 
              :value="role.code">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="inviteCode" v-if="isAdminRole">
          <el-input 
            v-model="registerForm.inviteCode"
            :prefix-icon="Key"
            placeholder="请输入管理员邀请码">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleRegister" 
            class="auth-button"
          >
            {{ loading ? '注册中' : '注册' }}
          </el-button>
        </el-form-item>
        <div class="auth-link">
          已有账号？<router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, 
  Lock, 
  Message, 
  Phone, 
  UserFilled, 
  Document, 
  Location,
  Upload as UploadIcon,
  Key
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const isAdminRole = ref(false)
// 管理员角色代码列表
const adminRoles = ['ADMIN', 'SUPER_ADMIN']

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  name: '',
  roleCode: 'USER',
  idCard: '',
  address: '',
  avatar: '',
  gender: 1,
  age: null,
  status: 1,
  inviteCode: ''
})

const roleOptions = ref([])

// 获取所有角色
const fetchRoles = async () => {
  try {
    const response = await request.get('/role/all',null,{
      onSuccess:(data)=>{
        roleOptions.value = data
      }
    })
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
}

onMounted(() => {
  fetchRoles()
})

// 处理角色变更
const handleRoleChange = (value) => {
  isAdminRole.value = adminRoles.includes(value)
  
  // 如果切换回普通用户，清空邀请码
  if (!isAdminRole.value) {
    registerForm.inviteCode = ''
  }
  
  // 重新验证表单
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
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

const validateIdCard = (rule, value, callback) => {
  const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
  if (value && !idCardRegex.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

// 验证邀请码
const validateInviteCode = (rule, value, callback) => {
  if (isAdminRole.value && !value) {
    callback(new Error('管理员注册需要邀请码'))
  } else if (isAdminRole.value && value !== 'ADMIN123') {
    callback(new Error('邀请码不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  name: [
    { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  idCard: [
    { validator: validateIdCard, trigger: 'blur' }
  ],
  address: [
    { max: 200, message: '地址长度不能超过200个字符', trigger: 'blur' }
  ],
  age: [
    { type: 'number', min: 0, max: 150, message: '年龄必须在0到150之间', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ],
  inviteCode: [
    { validator: validateInviteCode, trigger: 'blur' }
  ]
}

const handleRegister = () => {
  registerFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        // 移除后端邀请码验证，使用表单验证规则代替
        const { confirmPassword, inviteCode, ...registerData } = registerForm
        await request.post("/user/register", registerData, {
          successMsg: "注册成功",
          showDefaultMsg: true,
          onSuccess: () => {
            router.push('/login')
          }
        })
      } finally {
        loading.value = false
      }
    }
  })
}

const handleAvatarSuccess = (response) => {
  registerForm.avatar = response.data
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

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
</script>

<style lang="scss" scoped>
@import '@/assets/styles/auth.scss';

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-radio-group) {
  width: 100%;
  display: flex;
  justify-content: space-around;
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

:deep(.el-form-item.avatar-uploader) {
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style> 