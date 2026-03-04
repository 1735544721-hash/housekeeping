import request from '@/utils/request'
import { useUserStore } from '@/store/user'

// 记录浏览历史
export function recordBrowseHistory(serviceId) {
  const userStore = useUserStore()
  
  // 检查用户是否登录，未登录则直接返回一个已解决的Promise
  if (!userStore.userInfo || !userStore.userInfo.id) {
    console.log('用户未登录，不记录浏览历史')
    return Promise.resolve()
  }
  
  return request.post(`/browse-history?userId=${userStore.userInfo.id}&serviceId=${serviceId}`, null, {
    showDefaultMsg: false
  })
}

// 清除浏览历史
export function clearBrowseHistory() {
  const userStore = useUserStore()
  
  // 检查用户是否登录
  if (!userStore.userInfo || !userStore.userInfo.id) {
    console.log('用户未登录，不能清除浏览历史')
    return Promise.resolve()
  }
  
  return request.delete(`/browse-history?userId=${userStore.userInfo.id}`, {
    showDefaultMsg: false
  })
}

// 获取浏览历史列表
export function getBrowseHistoryList(params) {
  const userStore = useUserStore()
  
  // 检查用户是否登录
  if (!userStore.userInfo || !userStore.userInfo.id) {
    console.log('用户未登录，不能获取浏览历史')
    return Promise.resolve([])
  }
  
  return request.get('/browse-history/list', {
    ...params,
    userId: userStore.userInfo.id
  }, {
    showDefaultMsg: false
  })
}

// 获取浏览历史详情
export function getBrowseHistoryDetail(id) {
  const userStore = useUserStore()
  
  // 检查用户是否登录
  if (!userStore.userInfo || !userStore.userInfo.id) {
    console.log('用户未登录，不能获取浏览历史详情')
    return Promise.resolve(null)
  }
  
  return request.get(`/browse-history/${id}?userId=${userStore.userInfo.id}`, null, {
    showDefaultMsg: false
  })
} 