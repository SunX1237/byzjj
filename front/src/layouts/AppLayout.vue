<template>
  <el-container class="app-layout" direction="vertical">
    <!-- 模块页顶部栏：不显示侧边栏，仅保留返回门户入口 -->
    <el-header class="app-header">
      <div class="app-header-left">
        <router-link to="/portal" class="app-back" title="返回门户首页">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回门户</span>
        </router-link>

        <span class="app-header-divider"></span>

        <div class="app-header-brand">
          <el-icon :size="20"><House /></el-icon>
          <span class="app-header-title">住建局综合业务功能</span>
        </div>
        <el-tag size="small" effect="plain" class="app-demo-tag">演示环境</el-tag>
      </div>

      <div class="app-header-right">
        <span class="app-date">{{ today }}</span>
        <el-tag :type="healthTagType" effect="light" size="small">
          {{ healthText }}
        </el-tag>
      </div>
    </el-header>

    <!-- 模块内容区：全宽显示占位页或后续业务页面 -->
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ArrowLeft, House } from '@element-plus/icons-vue'
import { useBackendHealth } from '../composables/useBackendHealth'

// 后端健康状态由共享 composable 提供
const { healthText, healthTagType } = useBackendHealth()

// 顶栏展示的当前日期
const today = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
}).format(new Date())
</script>

<style scoped>
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
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
  min-width: 0;
}

.app-back {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  gap: 4px;
  padding: 6px 12px;
  font-size: 14px;
  color: #1f5fae;
  text-decoration: none;
  background: #eef4fc;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.app-back:hover {
  background: #dde9f8;
}

.app-back:focus-visible {
  outline: 2px solid #1f5fae;
  outline-offset: 2px;
}

.app-header-divider {
  width: 1px;
  height: 20px;
  background: #e2e9f2;
}

.app-header-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: #1f2d3d;
}

.app-header-title {
  overflow: hidden;
  font-size: 17px;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.app-demo-tag {
  flex-shrink: 0;
  color: #1f5fae;
  border-color: #b9d0ec;
}

.app-date {
  flex-shrink: 0;
  font-size: 13px;
  color: #7a8699;
}

.app-main {
  padding: 0;
  overflow: auto;
  background: #ffffff;
}

@media (max-width: 768px) {
  .app-date {
    display: none;
  }
}

@media (max-width: 600px) {
  .app-header {
    gap: 8px;
    padding: 0 12px;
  }

  .app-header-left,
  .app-header-right {
    gap: 8px;
  }

  .app-header-divider,
  .app-header-title,
  .app-demo-tag {
    display: none;
  }
}
</style>
