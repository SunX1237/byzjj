import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 开发服务器配置：固定 4000 端口，并将 /api 请求代理到 Java 后端
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 4000,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true
      }
    }
  }
})
