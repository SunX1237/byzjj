import { computed, onMounted, ref } from 'vue'
import { getHealth } from '../api/health'

/**
 * 后端健康状态组合式函数。
 * 组件挂载时请求 /api/health，并提供「检测中 / 正常 / 未连接」三态文案与标签类型。
 *
 * @returns {{
 *   healthStatus: import('vue').Ref<string>,
 *   healthText: import('vue').ComputedRef<string>,
 *   healthTagType: import('vue').ComputedRef<string>,
 *   checkBackendHealth: () => Promise<void>
 * }}
 */
export function useBackendHealth() {
  // checking 检测中、online 正常、offline 未连接
  const healthStatus = ref('checking')

  const healthText = computed(() => {
    if (healthStatus.value === 'online') return '后端服务正常'
    if (healthStatus.value === 'offline') return '后端未连接'
    return '后端检测中'
  })

  const healthTagType = computed(() => {
    if (healthStatus.value === 'online') return 'success'
    if (healthStatus.value === 'offline') return 'danger'
    return 'info'
  })

  /**
   * 查询后端健康状态。
   * 请求失败时只更新状态展示，不影响前端页面浏览。
   */
  async function checkBackendHealth() {
    try {
      const data = await getHealth()
      healthStatus.value = data?.status === 'UP' ? 'online' : 'offline'
    } catch {
      healthStatus.value = 'offline'
    }
  }

  onMounted(checkBackendHealth)

  return {
    healthStatus,
    healthText,
    healthTagType,
    checkBackendHealth
  }
}
