import request from './request'

/**
 * 查询后端健康状态。
 *
 * @returns {Promise<{status: string, service: string, timestamp: string}>} 健康信息
 */
export function getHealth() {
  return request.get('/health')
}
