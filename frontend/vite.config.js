import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      // 定义 global 别名
      global: 'window'
    },
  },
  define: {
    // 定义全局变量
    'global': 'window'
  },
  server:{
    //代理配置
    proxy:{
      // 歌词代理规则
      '/proxy/lyric': {
        target: 'https://online-music-dating-1362513882.cos.ap-guangzhou.myqcloud.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/proxy\/lyric/, '')
      },
      // 匹配所有以 `/api`开头的请求路径
      '/api':{
        target:'http://localhost:8080', //代理目标的基础路径
        changeOrigin:true, //支持跨域
        rewrite:(path)=>path.replace(/^\/api/,'') //重写路径:去掉路径中开头的 `/api`
      }
    }
  }
})
