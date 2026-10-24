<template>
  <el-container class="app-layout">
    <!-- 左侧导航栏 -->
    <el-aside width="240px" class="app-aside">
      <div class="app-brand">
        <div class="app-brand-icon">
          <el-icon :size="24"><House /></el-icon>
        </div>
        <div>
          <div class="app-brand-title">住建业务演示</div>
          <div class="app-brand-subtitle">综合业务功能</div>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="app-menu"
        background-color="transparent"
        text-color="#c9d8ef"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/funds">
          <el-icon><Money /></el-icon>
          <span>住宅专项维修资金</span>
        </el-menu-item>
        <el-menu-item index="/lease">
          <el-icon><OfficeBuilding /></el-icon>
          <span>国有资产租赁更新</span>
        </el-menu-item>
        <el-menu-item index="/housing">
          <el-icon><MapLocation /></el-icon>
          <span>直管公房一张图</span>
        </el-menu-item>
        <el-menu-item index="/documents">
          <el-icon><Document /></el-icon>
          <span>政务公文接收</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="app-body">
      <!-- 顶部信息栏 -->
      <el-header class="app-header">
        <div class="app-header-left">
          <span class="app-header-title">住建局综合业务功能</span>
          <el-tag size="small" effect="plain" class="app-demo-tag">演示环境</el-tag>
        </div>
        <div class="app-header-right">
          <span class="app-date">{{ today }}</span>
          <el-tag :type="healthTagType" effect="light" size="small">
            {{ healthText }}
          </el-tag>
        </div>
      </el-header>

      <!-- 业务内容区：当前四个模块均为白色占位页 -->
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { House, Money, OfficeBuilding, MapLocation, Document } from '@element-plus/icons-vue'
import { getHealth } from '../api/health'

const route = useRoute()

// 当前激活菜单与路由路径保持一致
const activeMenu = computed(() => route.path)

// 后端健康状态：checking 检测中、online 正常、offline 未连接
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

// 顶栏展示的当前日期
const today = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
}).format(new Date())

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
</script>

<style scoped>
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.app-aside {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #123a6b 0%, #0b2647 100%);
  color: #ffffff;
}

.app-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.app-brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.14);
}

.app-brand-title {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}

.app-brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #9fb8d8;
  letter-spacing: 2px;
}

.app-menu {
  flex: 1;
  padding: 12px 0;
  border-right: none;
}

.app-menu :deep(.el-menu-item) {
  height: 48px;
  margin: 4px 10px;
  border-radius: 8px;
  line-height: 48px;
}

.app-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
}

.app-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.16);
  font-weight: 600;
}

.app-body {
  min-width: 0;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8eef5;
}

.app-header-left,
.app-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2d3d;
}

.app-demo-tag {
  color: #1f5fae;
  border-color: #b9d0ec;
}

.app-date {
  font-size: 13px;
  color: #7a8699;
}

.app-main {
  padding: 0;
  overflow: auto;
  background: #ffffff;
}
</style>
