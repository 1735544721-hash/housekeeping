<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 30, 50]"
      :background="true"
      :layout="layout"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  // 总条目数
  total: {
    type: Number,
    required: true
  },
  // 当前页码
  page: {
    type: Number,
    default: 1
  },
  // 每页条数
  limit: {
    type: Number,
    default: 10
  },
  // 分页布局
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  }
})

const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

// 当前页码
const currentPage = ref(props.page)
// 每页条数
const pageSize = ref(props.limit)

// 监听父组件传入的page和limit变化
watch(() => props.page, (val) => {
  currentPage.value = val
})

watch(() => props.limit, (val) => {
  pageSize.value = val
})

// 每页条数改变
const handleSizeChange = (val) => {
  emit('update:limit', val)
  emit('pagination', { page: currentPage.value, limit: val })
}

// 当前页改变
const handleCurrentChange = (val) => {
  emit('update:page', val)
  emit('pagination', { page: val, limit: pageSize.value })
}
</script>

<style lang="scss" scoped>
.pagination-container {
  padding: 15px 0;
  text-align: right;
}
</style> 