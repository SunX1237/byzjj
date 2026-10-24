import request from './request'

/**
 * 获取维修资金项目、楼幢、单元、房屋层级树。
 *
 * @returns {Promise<{projects: Array}>} 层级树数据
 */
export function getFundTree() {
  return request.get('/funds/tree')
}

/**
 * 按项目、楼幢、单元或房屋查询维修资金。
 *
 * @param {{projectId: number, buildingId?: number, unitId?: number, houseId?: number}} params 查询参数
 * @returns {Promise<object>} 汇总、房屋列表或流水
 */
export function queryFunds(params) {
  return request.get('/funds/query', { params })
}
