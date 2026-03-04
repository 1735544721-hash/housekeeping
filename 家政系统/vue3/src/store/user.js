import { defineStore } from 'pinia'
import request from '@/utils/request'
// import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    staffInfo: JSON.parse(localStorage.getItem('staffInfo')) || null,
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    menus: JSON.parse(localStorage.getItem('menus')) || []
  }),

  getters: {
    // 判断是否登录
    isLoggedIn: (state) => !!state.token,
    // 判断是否是管理员
    isAdmin: (state) => state.userInfo?.roleCode === 'ADMIN',
    // 判断是否是普通用户
    isUser: (state) => state.userInfo?.roleCode === 'USER',
    isStaff: (state) => {
      console.log('Checking isStaff:', state.userInfo?.roleCode)  // 添加调试日志
      return state.userInfo?.roleCode === 'STAFF'
    }
  },

  actions: {
    // 更新用户信息
    updateUserInfo(data) {
      if (!data) return
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    
    // 设置服务人员信息
    setStaffInfo(staffInfo) {
      if (!staffInfo) return
      this.staffInfo = staffInfo
      localStorage.setItem('staffInfo', JSON.stringify(staffInfo))
    },

    // 获取服务人员信息
    async getStaffInfo() {
      if (!this.isStaff || !this.userInfo?.id) return null

      try {
        const staffRes = await request.get(`/staff/${this.userInfo.id}`, {
          showDefaultMsg: false
        })
        if (staffRes?.data) {
          this.setStaffInfo(staffRes.data)
          return staffRes.data
        }
        return null
      } catch (error) {
        console.error('Failed to fetch staff info:', error)
        return null
      }
    },

    // 更新服务人员信息
    async updateStaffInfo(staffInfo) {
      if (!this.isStaff || !this.userInfo?.id) return false

      try {
        await request.put(`/staff/info`, staffInfo, {
          successMsg: '更新成功'
        })
        this.setStaffInfo(staffInfo)
        return true
      } catch (error) {
        console.error('Failed to update staff info:', error)
        return false
      }
    },

    // 修改 setUserInfo 方法，使用新的 setStaffInfo 方法
    async setUserInfo(data) {
      if (!data) return
      
      this.userInfo = data.userInfo || data
      this.token = data.token
      this.role = data.roleCode
      
      // 如果是服务人员且有员工信息，使用 setStaffInfo 方法保存
      if (this.userInfo?.roleCode === 'STAFF' && data.staffInfo) {
        this.setStaffInfo(data.staffInfo)
      }
      
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('token', this.token || '')
      localStorage.setItem('role', this.role || '')
    },

    // 修改 getUserInfo 方法，使用新的 getStaffInfo 方法
    async getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      const staffInfo = JSON.parse(localStorage.getItem('staffInfo'))
      const menus = JSON.parse(localStorage.getItem('menus'))
      
      if (userInfo) {
        this.userInfo = userInfo
        this.menus = menus
        
        if (userInfo.roleCode === 'STAFF') {
          if (staffInfo) {
            this.setStaffInfo(staffInfo)
          } else {
            await this.getStaffInfo()
          }
        }
        
        return { userInfo, staffInfo: this.staffInfo, menus }
      }
      
      this.clearUserInfo()
      throw new Error('No cached user info')
    },

    clearUserInfo() {
      this.userInfo = null
      this.staffInfo = null
      this.token = ''
      this.role = ''
      this.menus = []
      // Clear LocalStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('staffInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('menus')
    },
    setMenus(menus) {
      if (!menus) return
      this.menus = menus
      localStorage.setItem('menus', JSON.stringify(menus))
    },
    // 登录
    async login(loginForm) {
      try {
        const res = await request.post('/user/login', loginForm)
        this.setUserInfo(res)
        return res
      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },
    // 注册

    // 退出登录
    async logout() {
      this.clearUserInfo()
      // try {
      //   await request.post('/user/logout')
      // } catch (error) {
      //   console.error('Logout failed:', error)
      // } finally {
      //   this.clearUserInfo()
      // }
    },
    // 检查登录状态
    checkLoginStatus() {
      return !!this.token
    }
  }
})