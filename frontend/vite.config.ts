import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget =
    process.env.VITE_DEV_API_TARGET ||
    env.VITE_DEV_API_TARGET ||
    'http://localhost:8082'
  const fePort = Number(process.env.FE_PORT || env.FE_PORT || 5173)

  return {
    plugins: [react()],
    server: {
      port: fePort,
      strictPort: false,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path,
        },
      },
    },
  }
})