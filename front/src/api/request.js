import axios from 'axios'

// 创建 Axios 实例，所有请求统一以 /api 为前缀，由 Vite 代理到后端
const request = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 响应拦截器：解包后端统一响应结构 { code, message, data }
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 0) {
      return body.data
    }
    return Promise.reject(new Error(body?.message || '请求失败'))
  },
  (error) => {
    const body = error.response?.data
    if (body?.message) {
      return Promise.reject(new Error(body.message))
    }
    if (error.response) {
      return Promise.reject(new Error(`请求失败（${error.response.status}）`))
    }
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请稍后重试'))
    }
    return Promise.reject(new Error('无法连接后端服务，请确认服务已启动'))
  }
)

export default request
