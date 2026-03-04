<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-header">
        <div class="login-logo" style="margin-bottom: 40px;">
          <Logo />
        </div>

      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="auth-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" :prefix-icon="User" placeholder="请输入用户名">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" :prefix-icon="Lock" type="password" placeholder="请输入密码" show-password>
          </el-input>
        </el-form-item>
        <el-form-item>
          <div class="remember-row">
            <el-checkbox v-model="rememberAccount">记住账号密码</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" class="auth-button">
            {{ loading ? '登录中' : '登录' }}
          </el-button>
        </el-form-item>
        <div class="auth-links">
          <div  class="forget-link" @click.prevent="handleForgetPassword">忘记密码</div>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { User, Lock } from '@element-plus/icons-vue'
import { setBackRoutes } from '@/router'
import { ElMessage } from 'element-plus'
import Logo from '@/components/common/Logo.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberAccount = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

// 页面加载时检查本地存储
onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  const savedPassword = localStorage.getItem('rememberedPassword')
  const remembered = localStorage.getItem('rememberAccount')

  if (remembered === 'true' && savedUsername) {
    loginForm.username = savedUsername
    loginForm.password = savedPassword || ''
    rememberAccount.value = true
  }
})

const handleLogin = () => {
  loginFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        await request.post("/user/login", loginForm, {
          successMsg: null,
          onSuccess: async (data) => {
            // 处理记住账号密码
            if (rememberAccount.value) {
              localStorage.setItem('rememberedUsername', loginForm.username)
              localStorage.setItem('rememberedPassword', loginForm.password)
              localStorage.setItem('rememberAccount', 'true')
            } else {
              localStorage.removeItem('rememberedUsername')
              localStorage.removeItem('rememberedPassword')
              localStorage.setItem('rememberAccount', 'false')
            }

            // 如果是服务人员，先检查员工信息
            if (data.roleCode === 'STAFF') {
              let staffInfo = null
              try {
                const staffRes = await request.get(`/staff/user/${data.id}`, null, {
                  showDefaultMsg: false
                })
                staffInfo = staffRes
              } catch (error) {
                console.error('获取员工信息失败:', error)
              }

              if (!staffInfo) {
                ElMessage.error('获取员工信息失败，请联系管理员注册身份！')
                loading.value = false
                return
              }

              data.staffInfo = staffInfo
            }

            // 保存用户信息
            await userStore.setUserInfo(data)

            // 根据角色进行不同的路由跳转
            if (data.roleCode === 'USER') {
              // 普通用户跳转到前台首页
              router.push(route.query.redirect || '/')
            } else {
              // 其他角色（管理员、员工等）跳转到后台
              userStore.setMenus(data.menuList)
              await setBackRoutes()
              const targetPath = route.query.redirect || '/back/dashboard'
              // 确保路由准备就绪后再跳转
              await router.isReady()
              await router.push(targetPath)
            }
          },
          onError: (error) => {
            console.error('登录失败:', error)
            loading.value = false
          }
        })
      } catch (error) {
        loading.value = false
      }
    }
  })
}

const handleForgetPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style lang="scss" scoped>
@import "@/assets/styles/auth.scss";

.login-logo {
  transform: scale(1.5); // 放大1.5倍
  margin-bottom: 20px; // 增加底部间距
}

// 记住账号样式
.remember-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: -8px;

  :deep(.el-checkbox__label) {
    color: #606266;
    font-size: 14px;
  }
}

// 移除动画效果，保持简洁
:deep(.el-input__suffix) {
  color: #909399;
}
.forget-link{
  cursor: pointer;
  color: #909399;
  // xi
  font-size: 14px;
}
</style> 