<template>
  <el-dialog 
    v-model="dialogVisible" 
    title="服务人员详情" 
    width="800px" 
    :close-on-click-modal="false" 
    destroy-on-close 
    class="staff-detail-dialog"
  >
    <el-descriptions v-loading="loading" :column="2" border class="staff-detail">
      <el-descriptions-item label="联系电话">
        {{ staff.user?.phone }}
      </el-descriptions-item>
      <el-descriptions-item label="工作经验">
        {{ staff.experience }}年
      </el-descriptions-item>

      <el-descriptions-item label="完成订单">
        {{ staff.totalOrders }}单
      </el-descriptions-item>
      <el-descriptions-item label="完成率">
        {{ staff.completionRate }}%
      </el-descriptions-item>
      <el-descriptions-item label="服务区域">
        {{ staff.workArea }}
      </el-descriptions-item>
      <el-descriptions-item label="服务类型" :span="2">
        <el-tag v-for="type in staff.serviceType" :key="type" class="detail-tag" effect="plain">
          {{ type }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="个人简介" :span="2">
        {{ staff.description || '暂无简介' }}
      </el-descriptions-item>
      <el-descriptions-item label="证书信息" :span="2">
        <template v-if="staff.certificates?.length">
          <div v-for="cert in staff.certificates" :key="cert.code" class="cert-item">
            <span>{{ cert.name }}</span>
            <span class="cert-code">证书编号：{{ cert.code }}</span>
            <span>发证日期：{{ cert.issueDate }}</span>
          </div>
        </template>
        <span v-else>暂无证书信息</span>
      </el-descriptions-item>
    </el-descriptions>

    <!-- 评价列表 -->
    <div class="review-section">
      <div class="section-title">
        <span class="title-text">用户评价</span>
        <el-tag type="primary" effect="plain" class="review-count" v-if="reviews?.length">
          共 {{ totalReviews }} 条评价
        </el-tag>
      </div>

      <div class="reviews-container" v-loading="reviewsLoading">
        <template v-if="reviews?.length">
          <div class="reviews-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="user-info">
                  <el-avatar 
                    :size="36" 
                    :src="review.user?.avatar ? fileUtils.getImageUrl(review.user.avatar) : ''"
                  >
                    {{ review.user?.name ? review.user.name.charAt(0) : '?' }}
                  </el-avatar>
                  <div class="user-details">
                    <div class="user-name">{{ review.user?.name }}</div>
                    <div class="review-time">{{ formatDate(review.createTime) }}</div>
                  </div>
                </div>
                <div class="rating-info">
                  <el-rate
                    v-model="review.overallRating"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                </div>
              </div>
              <div class="review-content">
                {{ review.content || '该用户未填写评价内容' }}
              </div>
              <div class="rating-details">
                <div class="rating-item">
                  <span class="rating-label">技能评分：</span>
                  <el-rate
                    v-model="review.skillRating"
                    disabled
                    size="small"
                  />
                </div>
                <div class="rating-item">
                  <span class="rating-label">服务态度：</span>
                  <el-rate
                    v-model="review.attitudeRating"
                    disabled
                    size="small"
                  />
                </div>
                <div class="rating-item">
                  <span class="rating-label">综合体验：</span>
                  <el-rate
                    v-model="review.experienceRating"
                    disabled
                    size="small"
                  />
                </div>
              </div>
              <div v-if="review.reply" class="staff-reply">
                <div class="reply-header">服务人员回复：</div>
                <div class="reply-content">{{ review.reply }}</div>
              </div>
            </div>
          </div>

          <!-- 分页器 -->
          <div class="pagination-container">
            <el-pagination
              :current-page="reviewPageNum"
              :model-value="reviewPageNum"
              @update:model-value="reviewPageNum = $event"
              :page-size="reviewPageSize"
              :total="totalReviews"
              layout="prev, pager, next"
              @current-change="handleReviewPageChange"
              background
              hide-on-single-page
            />
          </div>
        </template>
        <el-empty v-else description="暂无评价" />
      </div>
    </div>

    <!-- 服务项目列表 -->
    <div class="service-selection" v-if="showServiceSelection">
      <div class="section-title">
        <span class="title-text">可预约服务</span>
        <el-tag type="success" effect="plain" class="service-count" v-if="staffServices?.length">
          {{ staffServices?.length }} 个服务项目
        </el-tag>
      </div>

      <div class="service-list">
        <template v-if="staffServices?.length">
          <div 
            v-for="service in staffServices" 
            :key="service.id"
            class="service-item"
            :class="{ 'is-selected': selectedServiceId === service.id }"
            @click="selectService(service)"
          >
            <div class="service-info">
              <div class="service-header">
                <span class="service-title">{{ service.title }}</span>
                <el-tag 
                  size="small" 
                  :type="service.status === 1 ? 'success' : 'info'"
                  effect="plain"
                >
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
        <el-empty v-else description="暂无可预约服务" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleOrder" 
          :disabled="!selectedServiceId"
        >
          立即预约
        </el-button>
      </div>
    </template>

  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import fileUtils from '@/utils/fileUtils'
import { formatDate } from '@/utils/dateUtils'

// 保存 props 的返回值
const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  staffId: {
    type: [String, Number],
    default: null
  }
})

// 保存 emits 的返回值
const emit = defineEmits({
  'update:modelValue': (value) => typeof value === 'boolean',
  'close': () => true
})

const router = useRouter()
const userStore = useUserStore()
const dialogVisible = ref(false)
const loading = ref(false)
const staff = ref({})
const staffServices = ref([])
const selectedServiceId = ref(null)
const showServiceSelection = ref(true)

// 评价相关变量
const reviews = ref([])
const reviewsLoading = ref(false)
const reviewPageNum = ref(1)
const reviewPageSize = ref(5)
const totalReviews = ref(0)


// 获取服务人员详情
const fetchStaffDetail = async () => {
  loading.value = true
  try {
    const [staffRes, servicesRes] = await Promise.all([
      request.get(`/staff/${props.staffId}`),
      request.get(`/staff/service-items/list?staffId=${props.staffId}`, null, {
        showDefaultMsg: false,
        onSuccess: (res) => {
          
        }
      })
    ])
    staff.value = staffRes
    if(servicesRes){
    staffServices.value = servicesRes.map(item => ({
      id: item.serviceId,
      title: item.serviceItem?.title || '未知服务',
      price: item.serviceItem?.price || 0,
      status: item.serviceItem?.status || 0,
      category: item.serviceItem?.category || null,
      description: item.serviceItem?.description || ''
    }))
    }else{
      staffServices.value = []
    }

    
    // 加载评价数据
    fetchStaffReviews()
  } catch (error) {
    console.error('获取服务人员详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取服务人员评价列表
const fetchStaffReviews = async () => {
  reviewsLoading.value = true
  try {
    const response = await request.get(`/review/staff/${props.staffId}`, {
      pageNum: reviewPageNum.value,
      pageSize: reviewPageSize.value
    }, {
      showDefaultMsg: false
    })
    if(response){
      reviews.value = response.records
      totalReviews.value = response.total
    }else{
      reviews.value = []
      totalReviews.value = 0
    }
  } catch (error) {
    console.error('获取服务人员评价失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 处理评价分页变化
const handleReviewPageChange = (page) => {
  reviewPageNum.value = page
  fetchStaffReviews()
}

// 重置数据
const resetData = () => {
  staff.value = {}
  staffServices.value = []
  selectedServiceId.value = null
  reviews.value = []
  reviewPageNum.value = 1
  totalReviews.value = 0
}

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val && props.staffId) {
    fetchStaffDetail()
  }
}, { immediate: true })

// 监听对话框关闭
watch(dialogVisible, (val) => {
  if (val !== props.modelValue) {
    emit('update:modelValue', val)
  }
  if (!val) {
    emit('close')
    resetData()
  }
})

// 选择服务
const selectService = (service) => {
  selectedServiceId.value = service.id === selectedServiceId.value ? null : service.id
}

// 处理预约
const handleOrder = () => {
  if (!selectedServiceId.value) {
    ElMessage.warning('请选择服务项目')
    return
  }

  // 检查是否登录
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('请先登录后再进行预约', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    })
    return
  }

  // 验证服务人员是否提供该服务（前端二次确认）
  const selectedService = staffServices.value.find(s => s.id === selectedServiceId.value)
  if (!selectedService) {
    ElMessage.error('所选服务项目无效，请重新选择')
    return
  }

  // 跳转到预约页面
  router.push({
    path: '/order/create',
    query: {
      staffId: staff.value.id,
      serviceId: selectedServiceId.value
    }
  })
  handleClose()
}


// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}


</script>

<style lang="scss" scoped>
.staff-detail-dialog {
  .staff-detail {
    padding: 20px 0;
  }

  .detail-header {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-bottom: 32px;
  }

  .staff-avatar-large {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    border: 3px solid #4a90e2;
  }

  .staff-avatar-large img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .detail-info {
    flex: 1;
  }

  .detail-info h2 {
    margin: 0 0 12px;
    font-size: 24px;
    color: #303133;
  }

  .rating-large {
    transform: scale(1.2);
    transform-origin: left;
  }

  .detail-tag {
    margin: 0 8px 8px 0;
  }

  .cert-item {
    display: flex;
    gap: 16px;
    margin-bottom: 8px;
    color: #606266;
    font-size: 14px;
  }

  .cert-code {
    color: #909399;
  }

  :deep(.el-descriptions__label) {
    width: 120px;
    justify-content: flex-end;
  }

  .dialog-footer {
    padding-top: 20px;
    text-align: right;
  }

  /* 评价列表样式 */
  .review-section {
    margin-top: 24px;
    border-top: 1px solid var(--el-border-color-light);
    padding-top: 24px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      .title-text {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }

    .reviews-container {
      padding: 4px;
    }

    .reviews-list {
      max-height: 400px;
      overflow-y: auto;
      padding-right: 8px;
      margin-bottom: 16px;
      
      /* 美化滚动条样式 */
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-thumb {
        background-color: var(--el-border-color);
        border-radius: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background-color: var(--el-fill-color-lighter);
        border-radius: 6px;
      }
    }

    .review-item {
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
      background-color: var(--el-bg-color-page);
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      }

      .review-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;

          .user-details {
            display: flex;
            flex-direction: column;

            .user-name {
              font-weight: 500;
              color: var(--el-text-color-primary);
            }

            .review-time {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              margin-top: 4px;
            }
          }
        }

        .rating-info {
          margin-left: auto;
        }
      }

      .review-content {
        margin: 12px 0;
        line-height: 1.6;
        color: var(--el-text-color-primary);
        font-size: 14px;
      }

      .rating-details {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        margin: 16px 0;
        padding: 12px;
        background-color: var(--el-fill-color-lighter);
        border-radius: 4px;

        .rating-item {
          display: flex;
          align-items: center;
          gap: 8px;

          .rating-label {
            font-size: 13px;
            color: var(--el-text-color-secondary);
            white-space: nowrap;
          }
        }
      }

      .staff-reply {
        margin-top: 16px;
        padding: 12px;
        background-color: var(--el-color-primary-light-9);
        border-radius: 4px;
        border-left: 3px solid var(--el-color-primary);

        .reply-header {
          font-weight: 500;
          color: var(--el-color-primary);
          margin-bottom: 8px;
          font-size: 13px;
        }

        .reply-content {
          color: var(--el-text-color-primary);
          font-size: 14px;
          line-height: 1.6;
        }
      }
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 24px;
    }
  }

  .service-selection {
    margin-top: 24px;
    border-top: 1px solid var(--el-border-color-light);
    padding-top: 24px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      .title-text {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .service-count {
        font-size: 13px;
      }
    }

    .service-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-height: 400px;
      overflow-y: auto;
      padding: 4px;

      .service-item {
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
        
        &.is-selected {
          background-color: var(--el-color-primary);
          border-color: var(--el-color-primary);
          transform: translateX(4px);
          box-shadow: 0 4px 16px rgba(var(--el-color-primary-rgb), 0.2);
          
          .service-info {
            .service-title,
            .service-desc {
              color: #ffffff;
            }
          }

          .service-price-box {
            background-color: rgba(255, 255, 255, 0.1);
            
            .price-label,
            .price-value,
            .price-unit {
              color: #ffffff;
            }
          }
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
}
</style> 