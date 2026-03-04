import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router, { initDynamicRoutes } from './router'
// 导入 Element Plus
import ElementPlus from 'element-plus'
// 导入自定义主题色配置
import './styles/element-variables.scss'
import 'element-plus/dist/index.css'
// 导入 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入初始化样式
import './assets/global.css'
import '@/assets/styles/menu.scss'
// 中文
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './styles/element-plus.scss'

const app = createApp(App)

// 先初始化Pinia
app.use(createPinia())
// 然后初始化路由
app.use(router)

// 在Pinia和路由都初始化后，再初始化动态路由
initDynamicRoutes()

app.use(ElementPlus)
// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
