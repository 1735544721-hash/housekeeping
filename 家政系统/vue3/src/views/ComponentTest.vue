<template>
  <div class="component-test">
    <el-row :gutter="24">
      <!-- 左侧面板 -->
      <el-col :span="16">
        <el-card class="mb-4">
          <template #header>
            <div class="card-header">
              <h3>基础组件展示</h3>
              <el-switch v-model="darkMode" />
            </div>
          </template>
          
          <!-- 按钮展示 -->
          <div class="section">
            <h4>按钮 Buttons</h4>
            <el-row class="mb-4">
              <el-button>Default</el-button>
              <el-button type="primary">Primary</el-button>
              <el-button type="success">Success</el-button>
              <el-button type="warning">Warning</el-button>
              <el-button type="danger">Danger</el-button>
            </el-row>
          </div>

          <!-- 标签展示 -->
          <div class="section">
            <h4>标签 Tags</h4>
            <el-tag>Default</el-tag>
            <el-tag type="success">Success</el-tag>
            <el-tag type="warning">Warning</el-tag>
            <el-tag type="danger">Danger</el-tag>
          </div>

          <!-- 表单展示 -->
          <div class="section">
            <h4>表单 Form</h4>
            <el-form :model="form" label-position="top">
              <el-form-item label="用户名" required>
                <el-input v-model="form.username" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="选择服务">
                <el-select v-model="form.service" placeholder="请选择服务">
                  <el-option label="日常保洁" value="cleaning" />
                  <el-option label="深度保洁" value="deep-cleaning" />
                </el-select>
              </el-form-item>
              <el-form-item label="服务时间">
                <el-date-picker v-model="form.date" type="datetime" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary">提交</el-button>
                <el-button>取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧面板 -->
      <el-col :span="8">
        <el-card class="mb-4">
          <template #header>
            <h3>数据展示</h3>
          </template>
          
          <!-- 进度条 -->
          <div class="section">
            <h4>进度条 Progress</h4>
            <el-progress :percentage="50" />
            <el-progress :percentage="70" status="success" />
            <el-progress :percentage="100" status="warning" />
          </div>

          <!-- 评分 -->
          <div class="section">
            <h4>评分 Rate</h4>
            <el-rate v-model="rating" />
          </div>
        </el-card>

        <!-- 统计卡片 -->
        <el-card>
          <template #header>
            <h3>统计数据</h3>
          </template>
          <el-statistic title="活跃用户" :value="1234">
            <template #suffix>
              <el-icon><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 对话框示例 -->
    <el-button type="primary" @click="dialogVisible = true">
      打开对话框
    </el-button>
    <el-dialog
      v-model="dialogVisible"
      title="对话框示例"
      width="500px"
    >
      <p>这是一个现代风格的对话框示例</p>
      <template #footer>
        <span>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="dialogVisible = false">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User } from '@element-plus/icons-vue'

const darkMode = ref(false)
const rating = ref(4)
const dialogVisible = ref(false)

const form = reactive({
  username: '',
  service: '',
  date: ''
})
</script>

<style scoped>
.component-test {
  padding: 24px;
}

.mb-4 {
  margin-bottom: 16px;
}

.section {
  margin-bottom: 32px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

h4 {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 500;
  color: #4b5563;
}

.el-tag + .el-tag {
  margin-left: 8px;
}

.el-button + .el-button {
  margin-left: 12px;
}
</style> 