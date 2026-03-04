<template>
  <div class="home">
    <!-- 响应式数据和事件绑定示例 -->
    <div class="counter-section">
      <h2>计数器示例</h2>
      <el-button type="primary" @click="count++">点击次数: {{ count }}</el-button>
    </div>

    <!-- 计算属性和监听器示例 -->
    <div class="calc-section">
      <h2>计算属性示例</h2>
      <el-input v-model="firstName" placeholder="名" style="width: 200px" />
      <el-input v-model="lastName" placeholder="姓" style="width: 200px" class="ml-2" />
      <p>全名: {{ fullName }}</p>
    </div>

    <!-- 列表渲染和条件渲染示例 -->
    <div class="todo-section">
      <h2>待办事项示例</h2>
      <div class="add-todo">
        <el-input 
          v-model="newTodo" 
          placeholder="输入待办事项" 
          @keyup.enter="addTodo"
          style="width: 300px"
        >
          <template #append>
            <el-button @click="addTodo">添加</el-button>
          </template>
        </el-input>
      </div>

      <el-empty v-if="!todos.length" description="暂无待办事项" />
      
      <el-card v-else class="todo-list">
        <template v-for="todo in todos" :key="todo.id">
          <div class="todo-item">
            <el-checkbox v-model="todo.completed">
              <span :class="{ completed: todo.completed }">{{ todo.text }}</span>
            </el-checkbox>
            <el-button 
              type="danger" 
              size="small" 
              @click="removeTodo(todo.id)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-card>
    </div>

    <!-- 基础表单组件 -->
    <el-card class="section">
      <template #header>
        <h3>基础表单组件</h3>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="输入框" prop="input">
          <el-input v-model="form.input" placeholder="请输入内容" />
        </el-form-item>
        
        <el-form-item label="选择器" prop="select">
          <el-select v-model="form.select" placeholder="请选择">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期时间" prop="datetime">
          <el-date-picker
            v-model="form.datetime"
            type="datetime"
            placeholder="选择日期时间"
          />
        </el-form-item>
        
        <el-form-item label="开关" prop="switch">
          <el-switch v-model="form.switch" />
        </el-form-item>
        
        <el-form-item label="滑块" prop="slider">
          <el-slider v-model="form.slider" />
        </el-form-item>
        
        <el-form-item label="单选框" prop="radio">
          <el-radio-group v-model="form.radio">
            <el-radio :label="1">选项1</el-radio>
            <el-radio :label="2">选项2</el-radio>
            <el-radio :label="3">选项3</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="多选框" prop="checkbox">
          <el-checkbox-group v-model="form.checkbox">
            <el-checkbox label="选项1" />
            <el-checkbox label="选项2" />
            <el-checkbox label="选项3" />
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格组件 -->
    <el-card class="section">
      <template #header>
        <div class="table-header">
          <h3>表格组件</h3>
          <el-button type="primary" @click="addRow">添加行</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="date" label="日期" width="180" />
        <el-table-column prop="name" label="姓名" width="180" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              size="small" 
              @click="handleEdit(scope.$index, scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 对话框组件 -->
    <el-card class="section">
      <template #header>
        <h3>对话框和消息提示</h3>
      </template>
      
      <el-space wrap>
        <el-button @click="dialogVisible = true">打开对话框</el-button>
        <el-button @click="showMessage('success')">成功消息</el-button>
        <el-button @click="showMessage('warning')">警告消息</el-button>
        <el-button @click="showMessage('error')">错误消息</el-button>
        <el-button @click="showNotification">显示通知</el-button>
      </el-space>
    </el-card>

    <!-- 对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="对话框"
      width="30%"
    >
      <span>这是一个对话框的内容</span>
      <template #footer>
        <span class="dialog-footer">
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
import { ref, computed, watch, reactive } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

// 响应式数据
const count = ref(0)

// 计算属性示例
const firstName = ref('')
const lastName = ref('')
const fullName = computed(() => {
  if (firstName.value && lastName.value) {
    return lastName.value + firstName.value
  }
  return ''
})

// 待办事项功能
const newTodo = ref('')
const todos = ref([])

// 添加待办事项
const addTodo = () => {
  if (!newTodo.value.trim()) {
    ElMessage.warning('请输入待办事项内容')
    return
  }
  
  todos.value.push({
    id: Date.now(),
    text: newTodo.value,
    completed: false
  })
  newTodo.value = ''
  ElMessage.success('添加成功')
}

// 删除待办事项
const removeTodo = (id) => {
  todos.value = todos.value.filter(todo => todo.id !== id)
  ElMessage.success('删除成功')
}

// 监听器示例
watch(todos, (newTodos) => {
  console.log('待办事项已更新:', newTodos)
}, { deep: true })

// 表单相关
const formRef = ref(null)
const form = reactive({
  input: '',
  select: '',
  datetime: '',
  switch: false,
  slider: 0,
  radio: 1,
  checkbox: []
})

const rules = {
  input: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  select: [{ required: true, message: '请选择选项', trigger: 'change' }]
}

const options = [
  { value: '1', label: '选项1' },
  { value: '2', label: '选项2' },
  { value: '3', label: '选项3' }
]

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    ElMessage.success('提交成功')
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}

// 表格相关
const tableData = ref([
  {
    date: '2024-03-20',
    name: '张三',
    address: '北京市朝阳区'
  },
  {
    date: '2024-03-21',
    name: '李四',
    address: '上海市浦东新区'
  }
])

const handleEdit = (index, row) => {
  ElMessageBox.alert(`编辑第 ${index + 1} 行: ${row.name}`, '编辑')
}

const handleDelete = (index, row) => {
  ElMessageBox.confirm(
    `确定要删除 ${row.name} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    tableData.value.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const addRow = () => {
  tableData.value.push({
    date: new Date().toISOString().split('T')[0],
    name: '新用户',
    address: '待填写'
  })
}

// 对话框相关
const dialogVisible = ref(false)

// 消息提示
const showMessage = (type) => {
  ElMessage({
    type,
    message: `这是一条${type}消息`
  })
}

// 通知
const showNotification = () => {
  ElNotification({
    title: '通知',
    message: '这是一条通知消息',
    type: 'success',
    duration: 2000
  })
}
</script>

<style scoped>
.home {
  padding: 20px;
}

.counter-section,
.calc-section,
.todo-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.todo-item:last-child {
  border-bottom: none;
}

.completed {
  text-decoration: line-through;
  color: #999;
}

.ml-2 {
  margin-left: 8px;
}

.todo-list {
  margin-top: 20px;
}

.add-todo {
  margin-bottom: 20px;
}

.section {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-form-item__label) {
  font-weight: bold;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
