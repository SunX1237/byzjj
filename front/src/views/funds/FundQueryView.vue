<template>
  <div class="fund-page" v-loading="treeLoading">
    <div class="page-header">
      <div>
        <h1 class="page-title">住宅专项维修资金</h1>
        <p class="page-subtitle">按项目、楼幢、单元、房屋实时查询资金交存、使用、结息与余额</p>
      </div>
      <el-tag type="success" effect="plain">数据实时查询</el-tag>
    </div>

    <el-alert
      v-if="treeError"
      :title="treeError"
      type="error"
      :closable="false"
      show-icon
      class="page-alert"
    >
      <template #default>
        <el-button size="small" @click="loadTree">重新加载</el-button>
      </template>
    </el-alert>

    <!-- 级联筛选：任一条件变化都会实时触发查询 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <span class="filter-label">项目</span>
          <el-select
            v-model="projectId"
            placeholder="请选择项目"
            :disabled="treeLoading || !projects.length"
            @change="onProjectChange"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="filter-label">楼幢</span>
          <el-select
            v-model="buildingId"
            placeholder="全部楼幢"
            clearable
            :disabled="treeLoading || !projectId"
            @change="onBuildingChange"
          >
            <el-option
              v-for="building in buildings"
              :key="building.id"
              :label="building.name"
              :value="building.id"
            />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="filter-label">单元</span>
          <el-select
            v-model="unitId"
            placeholder="全部单元"
            clearable
            :disabled="treeLoading || !buildingId"
            @change="onUnitChange"
          >
            <el-option
              v-for="unit in units"
              :key="unit.id"
              :label="unit.name"
              :value="unit.id"
            />
          </el-select>
        </div>

        <div class="filter-item">
          <span class="filter-label">房屋</span>
          <el-select
            v-model="houseId"
            placeholder="全部房屋"
            clearable
            :disabled="treeLoading || !unitId"
            @change="onHouseChange"
          >
            <el-option
              v-for="house in houses"
              :key="house.id"
              :label="`${house.name}（${house.ownerName}）`"
              :value="house.id"
            />
          </el-select>
        </div>

        <div class="filter-actions">
          <el-button :icon="RefreshLeft" :disabled="treeLoading" @click="resetFilters">
            重置
          </el-button>
        </div>
      </div>

      <div v-if="result" class="scope-line">
        当前范围：<strong>{{ result.scopeName }}</strong>
      </div>
    </el-card>

    <el-alert
      v-if="queryError"
      :title="queryError"
      type="error"
      :closable="false"
      show-icon
      class="page-alert"
    >
      <template #default>
        <el-button size="small" @click="loadQuery">重试</el-button>
      </template>
    </el-alert>

    <div v-if="result" v-loading="loading" class="result-area">
      <!-- 汇总卡 -->
      <el-row :gutter="16" class="summary-grid">
        <el-col
          v-for="card in summaryCards"
          :key="card.key"
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">{{ card.label }}</div>
            <div class="summary-value" :class="card.className">
              ¥ {{ formatMoney(card.value) }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 项目/楼幢/单元层级：房屋列表 -->
      <el-card
        v-if="result.scope !== 'HOUSE'"
        shadow="never"
        class="content-card"
      >
        <template #header>
          <div class="card-header">
            <span class="card-title">房屋资金汇总</span>
            <span class="card-subtitle">共 {{ houseRows.length }} 户</span>
          </div>
        </template>

        <div class="table-scroll">
          <el-table
            :data="houseRows"
            stripe
            border
            empty-text="暂无房屋数据"
            class="house-table"
            @row-click="drillDown"
          >
            <el-table-column prop="buildingName" label="楼幢" width="90" />
            <el-table-column prop="unitName" label="单元" width="90" />
            <el-table-column prop="roomNo" label="房屋" width="100" />
            <el-table-column prop="ownerName" label="业主" width="100" />
            <el-table-column label="建筑面积" width="120" align="right">
              <template #default="{ row }">{{ formatArea(row.area) }}</template>
            </el-table-column>
            <el-table-column label="累计交存" width="130" align="right">
              <template #default="{ row }">¥ {{ formatMoney(row.depositTotal) }}</template>
            </el-table-column>
            <el-table-column label="累计使用" width="130" align="right">
              <template #default="{ row }">¥ {{ formatMoney(row.usageTotal) }}</template>
            </el-table-column>
            <el-table-column label="累计结息" width="130" align="right">
              <template #default="{ row }">¥ {{ formatMoney(row.interestTotal) }}</template>
            </el-table-column>
            <el-table-column label="当前余额" width="140" align="right">
              <template #default="{ row }">
                <span class="balance-text">¥ {{ formatMoney(row.balance) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="drillDown(row)">
                  查看明细
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 房屋层级：房屋信息 + 资金流水 -->
      <template v-if="result.scope === 'HOUSE'">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">房屋信息</span>
              <el-button link type="primary" @click="backToUnit">
                返回房屋列表
              </el-button>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="项目">
              {{ result.house.projectName }}
            </el-descriptions-item>
            <el-descriptions-item label="楼幢">
              {{ result.house.buildingName }}
            </el-descriptions-item>
            <el-descriptions-item label="单元">
              {{ result.house.unitName }}
            </el-descriptions-item>
            <el-descriptions-item label="房屋">
              {{ result.house.roomNo }}
            </el-descriptions-item>
            <el-descriptions-item label="业主">
              {{ result.house.ownerName }}
            </el-descriptions-item>
            <el-descriptions-item label="建筑面积">
              {{ formatArea(result.house.area) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">资金流水</span>
              <span class="card-subtitle">共 {{ transactions.length }} 笔</span>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane name="all">
              <template #label>全部流水（{{ tabCounts.all }}）</template>
            </el-tab-pane>
            <el-tab-pane name="DEPOSIT">
              <template #label>交存（{{ tabCounts.DEPOSIT }}）</template>
            </el-tab-pane>
            <el-tab-pane name="USAGE">
              <template #label>使用（{{ tabCounts.USAGE }}）</template>
            </el-tab-pane>
            <el-tab-pane name="INTEREST">
              <template #label>结息（{{ tabCounts.INTEREST }}）</template>
            </el-tab-pane>
          </el-tabs>

          <div class="table-scroll">
            <el-table
              :data="filteredTransactions"
              stripe
              border
              empty-text="暂无流水"
              class="ledger-table"
            >
              <el-table-column label="日期" width="120">
                <template #default="{ row }">{{ formatDate(row.occurredAt) }}</template>
              </el-table-column>
              <el-table-column label="类型" width="90">
                <template #default="{ row }">
                  <el-tag :type="tagType(row.type)" effect="plain" size="small">
                    {{ row.typeLabel }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="业务说明" min-width="180" />
              <el-table-column label="发生金额" width="140" align="right">
                <template #default="{ row }">
                  <span :class="amountClass(row)">{{ amountText(row) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="余额" width="150" align="right">
                <template #default="{ row }">
                  <span class="balance-text">¥ {{ formatMoney(row.balanceAfter) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </template>
    </div>

    <el-empty
      v-else-if="!treeLoading && !loading && !treeError && !queryError"
      description="请选择查询范围"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RefreshLeft } from '@element-plus/icons-vue'
import { getFundTree, queryFunds } from '../../api/funds'
import { formatArea, formatDate, formatMoney } from '../../utils/format'

const projects = ref([])
const projectId = ref(null)
const buildingId = ref(null)
const unitId = ref(null)
const houseId = ref(null)

const result = ref(null)
const treeLoading = ref(false)
const loading = ref(false)
const treeError = ref('')
const queryError = ref('')
const activeTab = ref('all')

// 查询序号用于丢弃过期响应，避免快速切换筛选时旧数据覆盖新数据
let querySequence = 0

const selectedProject = computed(
  () => projects.value.find((project) => project.id === projectId.value) || null
)
const buildings = computed(() => selectedProject.value?.buildings || [])
const selectedBuilding = computed(
  () => buildings.value.find((building) => building.id === buildingId.value) || null
)
const units = computed(() => selectedBuilding.value?.units || [])
const selectedUnit = computed(
  () => units.value.find((unit) => unit.id === unitId.value) || null
)
const houses = computed(() => selectedUnit.value?.houses || [])

const summary = computed(
  () => result.value?.summary || {
    depositTotal: 0,
    usageTotal: 0,
    interestTotal: 0,
    balance: 0
  }
)

const summaryCards = computed(() => [
  {
    key: 'deposit',
    label: '累计交存',
    value: summary.value.depositTotal,
    className: 'amount-increase'
  },
  {
    key: 'usage',
    label: '累计使用',
    value: summary.value.usageTotal,
    className: 'amount-usage'
  },
  {
    key: 'interest',
    label: '累计结息',
    value: summary.value.interestTotal,
    className: 'amount-interest'
  },
  {
    key: 'balance',
    label: '当前余额',
    value: summary.value.balance,
    className: 'balance-value'
  }
])

const houseRows = computed(() => result.value?.houses || [])
const transactions = computed(() => result.value?.transactions || [])

const tabCounts = computed(() => {
  const list = transactions.value
  return {
    all: list.length,
    DEPOSIT: list.filter((item) => item.type === 'DEPOSIT').length,
    USAGE: list.filter((item) => item.type === 'USAGE').length,
    INTEREST: list.filter((item) => item.type === 'INTEREST').length
  }
})

const filteredTransactions = computed(() => {
  if (activeTab.value === 'all') return transactions.value
  return transactions.value.filter((item) => item.type === activeTab.value)
})

/**
 * 加载项目、楼幢、单元、房屋层级树，并默认查询第一个项目。
 */
async function loadTree() {
  treeLoading.value = true
  treeError.value = ''
  queryError.value = ''
  result.value = null
  projects.value = []

  try {
    const data = await getFundTree()
    projects.value = data.projects || []

    if (!projects.value.length) {
      treeError.value = '暂无项目数据，请稍后重试'
      return
    }

    projectId.value = projects.value[0].id
    buildingId.value = null
    unitId.value = null
    houseId.value = null
    await loadQuery()
  } catch (error) {
    treeError.value = error.message || '项目层级加载失败'
  } finally {
    treeLoading.value = false
  }
}

/**
 * 按当前筛选条件实时查询维修资金。
 */
async function loadQuery() {
  if (!projectId.value) return

  const sequence = ++querySequence
  loading.value = true
  queryError.value = ''
  activeTab.value = 'all'

  try {
    const data = await queryFunds({
      projectId: projectId.value,
      buildingId: buildingId.value || undefined,
      unitId: unitId.value || undefined,
      houseId: houseId.value || undefined
    })

    if (sequence !== querySequence) return
    result.value = data
  } catch (error) {
    if (sequence !== querySequence) return
    result.value = null
    queryError.value = error.message || '查询失败'
  } finally {
    if (sequence === querySequence) {
      loading.value = false
    }
  }
}

/**
 * 项目变化时清空下级筛选并重新查询。
 */
function onProjectChange() {
  buildingId.value = null
  unitId.value = null
  houseId.value = null
  loadQuery()
}

/**
 * 楼幢变化时清空单元、房屋并重新查询。
 */
function onBuildingChange() {
  unitId.value = null
  houseId.value = null
  loadQuery()
}

/**
 * 单元变化时清空房屋并重新查询。
 */
function onUnitChange() {
  houseId.value = null
  loadQuery()
}

/**
 * 房屋变化时重新查询。
 */
function onHouseChange() {
  loadQuery()
}

/**
 * 重置楼幢、单元、房屋筛选，回到项目层级。
 */
function resetFilters() {
  buildingId.value = null
  unitId.value = null
  houseId.value = null
  loadQuery()
}

/**
 * 从房屋列表下钻到指定房屋，并同步回填级联筛选。
 *
 * @param {object} row 房屋汇总行
 */
function drillDown(row) {
  if (!row?.buildingId || !row?.unitId) return
  buildingId.value = row.buildingId
  unitId.value = row.unitId
  houseId.value = row.houseId
  loadQuery()
}

/**
 * 从房屋流水返回当前单元房屋列表。
 */
function backToUnit() {
  houseId.value = null
  loadQuery()
}

/**
 * 获取流水类型对应的标签样式。
 *
 * @param {string} type 流水类型代码
 * @returns {string} Element Plus 标签类型
 */
function tagType(type) {
  const map = {
    DEPOSIT: 'success',
    USAGE: 'danger',
    INTEREST: 'warning'
  }
  return map[type] || 'info'
}

/**
 * 格式化带正负号的流水金额。
 *
 * @param {object} row 流水行
 * @returns {string} 带符号金额
 */
function amountText(row) {
  return `${row.type === 'USAGE' ? '-' : '+'}${formatMoney(row.amount)}`
}

/**
 * 获取流水金额颜色样式。
 *
 * @param {object} row 流水行
 * @returns {string} 样式类名
 */
function amountClass(row) {
  if (row.type === 'USAGE') return 'amount-usage'
  if (row.type === 'INTEREST') return 'amount-interest'
  return 'amount-increase'
}

onMounted(loadTree)
</script>

<style scoped>
.fund-page {
  box-sizing: border-box;
  min-height: 100%;
  padding: 24px;
  background: #f5f7fb;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.page-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #7a8699;
}

.page-alert {
  margin-bottom: 16px;
}

.filter-card,
.summary-card,
.content-card {
  border: 1px solid #e8eef5;
  border-radius: 10px;
}

.filter-card {
  margin-bottom: 18px;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-item {
  display: flex;
  flex: 1 1 180px;
  flex-direction: column;
  gap: 6px;
  max-width: 240px;
  min-width: 180px;
}

.filter-label {
  font-size: 13px;
  color: #5f6f85;
}

.filter-actions {
  padding-bottom: 1px;
}

.scope-line {
  margin-top: 14px;
  font-size: 13px;
  color: #6b7a90;
}

.result-area {
  min-height: 200px;
}

.summary-grid {
  margin-bottom: 18px;
}

.summary-card {
  height: 100%;
}

.summary-label {
  font-size: 13px;
  color: #7a8699;
}

.summary-value {
  margin-top: 10px;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.amount-increase {
  color: #2e9e5b;
}

.amount-usage {
  color: #d9534f;
}

.amount-interest {
  color: #b7791f;
}

.balance-value {
  color: #1f5fae;
}

.balance-text {
  font-weight: 600;
  color: #1f2d3d;
}

.content-card {
  margin-bottom: 18px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.card-subtitle {
  font-size: 13px;
  color: #8a97a8;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

.house-table {
  min-width: 1100px;
}

.ledger-table {
  min-width: 720px;
}

.house-table :deep(.el-table__row) {
  cursor: pointer;
}

@media (max-width: 900px) {
  .filter-item {
    max-width: none;
  }
}

@media (max-width: 600px) {
  .fund-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
  }

  .page-title {
    font-size: 20px;
  }

  .filter-item {
    flex-basis: 100%;
    min-width: 0;
  }

  .summary-value {
    font-size: 19px;
  }
}
</style>
