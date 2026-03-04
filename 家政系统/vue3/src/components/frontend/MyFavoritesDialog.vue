<template>
  <el-dialog 
    v-model="dialogVisible" 
    title="我的收藏" 
    width="800px" 
    :close-on-click-modal="false" 
    destroy-on-close
    class="favorites-dialog"
  >
    <div class="favorites-list" v-loading="loading">
      <template v-if="favorites.length">
        <div 
          v-for="favorite in favorites" 
          :key="favorite.id"
          class="service-item"
          @click="handleServiceClick(favorite.serviceItem.id)"
        >
          <div class="service-info">
            <el-icon 
              class="favorite-icon is-favorited"
              @click.stop="cancelFavorite(favorite.serviceItem.id)"
            >
              <Star />
            </el-icon>
            <div class="service-header">
              <span class="service-title">{{ favorite.serviceItem.title }}</span>
              <el-tag 
                size="small" 
                :type="favorite.serviceItem.status === 1 ? 'success' : 'info'"
                effect="plain"
              >
                {{ favorite.serviceItem.status === 1 ? '可预约' : '暂停预约' }}
              </el-tag>
            </div>
            <div class="service-category" v-if="favorite.serviceItem.category">
              <el-tag size="small" type="warning" effect="plain">
                {{ favorite.serviceItem.category?.categoryName }}
              </el-tag>
            </div>
            <div class="service-desc" v-if="favorite.serviceItem.description">
              {{ favorite.serviceItem.description }}
            </div>
          </div>
          <div class="service-price-box">
            <span class="price-label">服务价格</span>
            <span class="price-value">¥{{ favorite.serviceItem.price }}</span>
            <span class="price-unit">/小时</span>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无收藏服务" />
    </div>

    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
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
const favorites = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 获取收藏列表
const fetchFavorites = async () => {
  if (!userStore.isLoggedIn) return
  
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    const params = {
      userId,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }   
    await request.get('/favorite/list', params, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        favorites.value = res.records || []
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 取消收藏
const cancelFavorite = async (serviceId) => {
  try {
    const userId = userStore.userInfo.id
    await request.delete(`/favorite?userId=${userId}&serviceId=${serviceId}`)
    // 重新获取收藏列表
    await fetchFavorites()
  } catch (error) {
    console.error('取消收藏失败:', error)
  }
}

// 处理服务点击
const handleServiceClick = (serviceId) => {
  router.push(`/service/${serviceId}`)
  handleClose()
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchFavorites()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchFavorites()
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    fetchFavorites()
  }
})

// 监听对话框关闭
watch(dialogVisible, (val) => {
  if (val !== props.modelValue) {
    emit('update:modelValue', val)
  }
  if (!val) {
    emit('close')
    favorites.value = []
    total.value = 0
  }
})

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style lang="scss" scoped>
.favorites-dialog {
  :deep(.el-dialog__headerbtn) {
    .el-dialog__close {
      font-size: 16px;
    }
  }

  .favorites-list {
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

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style> 