<template>
  <el-dialog v-model="dialogVisible" :title="category?.categoryName || '服务列表'" width="800px" :close-on-click-modal="false" destroy-on-close
    class="service-list-dialog">
    <div class="service-list" v-loading="loading">
      <template v-if="services.length">
        <div v-for="service in services" :key="service.id" class="service-item" @click="handleServiceClick(service.id)">
          <div class="service-info">
            <el-icon :class="{'is-favorited': service.isFavorited}" @click.stop="toggleFavorite(service.id)" class="favorite-icon">
              <Star />
            </el-icon>
            <div class="service-header">
              <span class="service-title">{{ service.title }}</span>
              <el-tag size="small" :type="service.status === 1 ? 'success' : 'info'" effect="plain">
                {{ service.status === 1 ? '可预约' : '暂停预约' }}
              </el-tag>
            </div>
            <div class="service-category" v-if="service.category">
              <el-tag size="small" type="warning" effect="plain">
                {{ service.category.categoryName }}
              </el-tag>
            </div>
            <div class="service-desc" v-if="service.description">
              {{ service.description }}
            </div>
          </div>
          <div class="service-price-box">
            <span class="price-label">服务价格</span>
            <span class="price-value">¥{{ service.price }}</span>
            <span class="price-unit">/小时</span>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无服务项目" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { Star } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  categoryId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits({
  'update:modelValue': (value) => typeof value === 'boolean',
  'close': () => true
})

const router = useRouter()
const userStore = useUserStore()
const dialogVisible = ref(false)
const loading = ref(false)
const services = ref([])
const category = ref(null)

// 获取服务列表
const fetchServices = async () => {
  loading.value = true
  try {
    const res = await request.get(`/service/category/${props.categoryId}`, null, {
      showDefaultMsg: false
    })
    services.value = res || []
    if (res.length > 0) {
      category.value = res[0].category
      // 获取收藏状态
      await checkFavoriteStatus() // 确保在获取服务后立即检查收藏状态
    }
  } catch (error) {
    console.error('获取服务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!userStore.isLoggedIn) return;

  const userId = userStore.userInfo.id;
  try {
    for (const service of services.value) {
      const res = await request.get(`/favorite/check?userId=${userId}&serviceId=${service.id}`, null, {
        showDefaultMsg: false,
        onSuccess: (res) => {
          service.isFavorited = res;
        }
      });
    }
  } catch (error) {
    console.error('获取收藏状态失败:', error);
  }
}

// 收藏服务
const toggleFavorite = async (serviceId) => {
  if (!userStore.isLoggedIn) {
    console.error('用户未登录，无法收藏服务')
    return
  }

  const userId = userStore.userInfo.id;
  try {
    const service = services.value.find(s => s.id === serviceId);
    if (service.isFavorited) {
      // 取消收藏
      await request.delete(`/favorite?userId=${userId}&serviceId=${serviceId}`);
      service.isFavorited = false;
    } else {
      // 添加收藏
      await request.post('/favorite', { userId, serviceId });
      service.isFavorited = true;
    }
  } catch (error) {
    console.error('收藏服务失败:', error);
  }
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val && props.categoryId) {
    fetchServices()
  }
}, { immediate: true })

// 监听对话框关闭
watch(dialogVisible, (val) => {
  if (val !== props.modelValue) {
    emit('update:modelValue', val)
  }
  if (!val) {
    emit('close')
    services.value = []
    category.value = null
  }
})

// 处理服务点击
const handleServiceClick = (serviceId) => {
  router.push(`/service/${serviceId}`)
  handleClose()
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style lang="scss" scoped>
.service-list-dialog {
  :deep(.el-dialog__headerbtn) {
    .el-dialog__close {
      font-size: 16px;
    }
  }

  .service-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: 600px;
    overflow-y: auto;
    padding: 4px;

    .service-item {
      position: relative;
      display: flex;
      justify-content: space-between;
      align-items: stretch;
      padding: 16px 20px;
      border: 2px solid var(--el-border-color-light);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background-color: var(--el-bg-color);
      
      &:hover {
        border-color: var(--el-color-primary-light-5);
        background-color: var(--el-color-primary-light-9);
        transform: translateX(4px);
      }

      .service-info {
        flex: 1;
        margin-right: 20px;
        display: flex;
        flex-direction: column;
        gap: 8px;

        .service-header {
          display: flex;
          align-items: center;
          gap: 12px;

          .service-title {
            font-size: 16px;
            font-weight: 500;
            color: var(--el-text-color-primary);
          }
        }

        .service-category {
          display: flex;
          gap: 8px;
        }

        .service-desc {
          font-size: 13px;
          color: var(--el-text-color-secondary);
          line-height: 1.5;
          margin-top: 4px;
        }

        .favorite-icon {
          position: absolute;
          top: 10px;
          right: 10px;
          cursor: pointer;
          z-index: 1;
          font-size: 24px;
          padding: 8px;
          border-radius: 50%;
          background-color: rgba(255, 255, 255, 0.8);
          transition: all 0.3s ease;

          &:hover {
            transform: scale(1.1);
            background-color: white;
          }

          &.is-favorited {
            color: #ff4949;
          }
        }
      }

      .service-price-box {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 12px 20px;
        background-color: var(--el-color-primary-light-9);
        border-radius: 6px;
        min-width: 120px;

        .price-label {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-bottom: 4px;
        }

        .price-value {
          font-size: 20px;
          font-weight: 600;
          color: var(--el-color-danger);
          line-height: 1.2;
        }

        .price-unit {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-top: 2px;
        }
      }
    }
  }
}
</style> 