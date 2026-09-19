import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 1160,
    open: true,
    proxy: {
      '/shop/user': {
        target: 'http://localhost:7071',
        changeOrigin: true
      },
      '/shop/coupon': {
        target: 'http://localhost:7072',
        changeOrigin: true
      },
      '/shop/product': {
        target: 'http://localhost:7073',
        changeOrigin: true
      },
      '/shop/order': {
        target: 'http://localhost:7074',
        changeOrigin: true
      },
      '/shop/cart': {
        target: 'http://localhost:7075',
        changeOrigin: true
      },
      '/avatars': {
        target: 'http://localhost:7071',
        changeOrigin: true
      }
    }
  }
})
