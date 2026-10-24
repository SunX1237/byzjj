const moneyFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

/**
 * 格式化金额，保留两位小数并添加千分位。
 *
 * @param {number|string} value 金额
 * @returns {string} 格式化后的金额
 */
export function formatMoney(value) {
  const number = Number(value ?? 0)
  return moneyFormatter.format(Number.isFinite(number) ? number : 0)
}

/**
 * 格式化建筑面积，保留两位小数并追加单位。
 *
 * @param {number|string} value 面积
 * @returns {string} 带单位的面积
 */
export function formatArea(value) {
  return `${formatMoney(value)} ㎡`
}

/**
 * 格式化日期，后端返回 YYYY-MM-DD 字符串。
 *
 * @param {string} value 日期字符串
 * @returns {string} 日期文本
 */
export function formatDate(value) {
  return value ? String(value).slice(0, 10) : '-'
}
