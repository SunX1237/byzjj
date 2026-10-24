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
  (error) => Promise.reject(error)
)

export default request
