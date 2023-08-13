import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      /* this is your api address */
      '/api': {
        target: "ws://192.168.10.100:8080",
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      /* this is my test address */
      '/test': {
        target: "ws://192.168.10.161:2333",
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/test/, '')
      }
    }
  }
})
