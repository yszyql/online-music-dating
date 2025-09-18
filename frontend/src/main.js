// 导入createApp
import { createApp } from 'vue'

// 导入element-plus
import ElementPlus from 'element-plus'
// 导入element-plus样式
import 'element-plus/dist/index.css'
// 导入element-plus中文包
import locale from 'element-plus/dist/locale/zh-cn.js'
// 导入element-plus图标库
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入自定义的样式
// import './assets/main.scss' 
// 导入全局注册组件
import HeaderMenu from './components/pulbic/HeaderMenu.vue'
import Footer from "@/components/pulbic/Footer.vue";
import MusicPlay from "@/components/pulbic/MusicPlay.vue";
import AI from "@/components/pulbic/AI.vue";

// 导入路由
import router from './router/index.js'

// 导入pinia
import { createPinia } from 'pinia'
//导入持久化插件
import {createPersistedState} from'pinia-persistedstate-plugin'


// 导入App组件
import App from './App.vue'


// 创建应用实例对象
const app = createApp(App)

// 创建pinia实例对象
const pinia = createPinia()
// 创建持久化插件
const persist = createPersistedState()

// 应用ElementPlus
app.use(ElementPlus,{ locale })
// 全局注册element-plus图标库
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 全局注册组件，无需在每个页面单独导入
app.component('HeaderMenu', HeaderMenu)
app.component('Footer', Footer)
app.component('MusicPlay', MusicPlay)
app.component('AI', AI)

// 应用路由
app.use(router)

// 应用pinia
pinia.use(persist)
app.use(pinia)

// 挂载到id为app的元素上
app.mount('#app')