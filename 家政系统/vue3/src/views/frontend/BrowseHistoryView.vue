<template>
  <div class="browse-history-page">
    <div class="container">
      <div class="page-header">
        <h1>浏览历史</h1>
        <el-button type="danger" plain @click="handleClearHistory">
          <el-icon><Delete /></el-icon>
          清空记录
        </el-button>
      </div>

      <div class="history-list" v-loading="loading">
        <template v-if="historyList.length">
          <div v-for="item in historyList" 
            :key="item.id" 
            class="history-item"
            @click="handleServiceClick(item.serviceItem.id)"
          >
            <div class="history-content">
              <div class="service-info">
                <div class="main-row">
                  <div class="text-content">
                    <h3>{{ item.serviceItem.title }}</h3>
                    <p class="service-desc">{{ item.serviceItem.description || '暂无描述' }}</p>
                  </div>
                  <div class="service-price">
                    <span class="price-value">¥{{ item.serviceItem.price }}</span>
                    <span class="price-unit">/小时</span>
                  </div>
                </div>
                <div class="browse-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatDate(item.lastBrowseTime) }}
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无浏览记录" />
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getBrowseHistoryList, clearBrowseHistory } from '@/api/browseHistory'
import { formatDate } from '@/utils/dateUtils'
import { Delete, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const historyList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 获取浏览历史列表
const fetchHistoryList = async () => {
  loading.value = true
  try {
    const res = await getBrowseHistoryList({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    // API返回的是带有分页信息的对象
    if (res.records) {
      historyList.value = res.records
      total.value = res.total
    } else {
      historyList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取浏览历史失败:', error)
    ElMessage.error('获取浏览历史失败')
  } finally {
    loading.value = false
  }
}

// 清空历史记录
const handleClearHistory = () => {
  ElMessageBox.confirm(
    '确定要清空所有浏览历史吗？此操作不可恢复。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await clearBrowseHistory()
      ElMessage.success('清空成功')
      historyList.value = []
      total.value = 0
    } catch (error) {
      console.error('清空浏览历史失败:', error)
      ElMessage.error('清空浏览历史失败')
    }
  })
}

// 点击服务项目跳转到详情页
const handleServiceClick = (serviceId) => {
  router.push(`/service/${serviceId}`)
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchHistoryList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchHistoryList()
}

onMounted(() => {
  fetchHistoryList()
})
</script>

<style lang="scss" scoped>
.browse-history-page {
  min-height: 100vh;
  background-color: #f8fafc;
  padding: 40px 0;
  padding-top: calc(40px + var(--el-header-height, 60px));
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 28px;
    color: #2c3e50;
    margin: 0;
    font-weight: 600;
  }

  .el-button {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 10px 20px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

.history-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.history-item {
  padding: 24px;
  transition: all 0.3s ease;
  border-bottom: 1px solid #edf2f7;
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: linear-gradient(to right, #f8fafc, white);
    transform: translateX(4px);
  }

  .history-content {
    .service-info {
      .main-row {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .text-content {
          flex: 1;
          margin-right: 24px;
          display: flex;
          align-items: baseline;
          gap: 12px;

          h3 {
            font-size: 18px;
            color: #2c3e50;
            margin: 0;
            font-weight: 500;
            white-space: nowrap;
          }

          .service-desc {
            flex: 1;
            color: #64748b;
            font-size: 14px;
            line-height: 1.6;
            margin: 0;
            display: -webkit-box;
            -webkit-line-clamp: 1;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
        }

        .service-price {
          flex-shrink: 0;
          text-align: right;

          .price-value {
            font-size: 22px;
            color: #4A90E2;
            font-weight: 600;
          }

          .price-unit {
            font-size: 14px;
            color: #64748b;
            margin-left: 2px;
          }
        }
      }

      .browse-time {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #94a3b8;
        font-size: 13px;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 0 24px;
}

:deep(.el-empty) {
  padding: 60px 0;
  background: linear-gradient(to bottom, white, #f8fafc);
  border-radius: 12px;
}
</style> 