import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src') // 配置 @ 指向 src 目录
    }
  },
  server: {
    port: 5173, // 前端端口，避免和后端 8080 冲突
    open: true // 启动后自动打开浏览器
  }
})