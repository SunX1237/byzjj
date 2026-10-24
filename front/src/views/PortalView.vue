<template>
  <div class="portal-page">
    <!-- 门户顶部栏 -->
    <header class="portal-header">
      <div class="portal-brand">
        <div class="portal-brand-icon">
          <el-icon :size="22"><House /></el-icon>
        </div>
        <div>
          <div class="portal-brand-title">住建业务演示</div>
          <div class="portal-brand-subtitle">综合业务功能</div>
        </div>
      </div>

      <div class="portal-header-right">
        <span class="portal-date">{{ today }}</span>
        <el-tag :type="healthTagType" effect="light" size="small">
          {{ healthText }}
        </el-tag>
      </div>
    </header>

    <main class="portal-main">
      <!-- 门户标题区 -->
      <section class="portal-hero">
        <span class="portal-hero-badge">演示环境</span>
        <h1 class="portal-hero-title">住建局综合业务功能</h1>
        <p class="portal-hero-subtitle">统一门户 · 请选择业务模块</p>
      </section>

      <!-- 四个业务模块入口 -->
      <section class="portal-grid">
        <router-link
          v-for="module in modules"
          :key="module.path"
          :to="module.path"
          class="module-card"
          :aria-label="`进入${module.title}`"
          :style="{
            '--module-color': module.color,
            '--module-bg': module.bgColor
          }"
        >
          <div class="module-card-header">
            <div class="module-icon">
              <el-icon :size="26">
                <component :is="module.icon" />
              </el-icon>
            </div>
            <el-tag type="info" effect="plain" size="small">功能建设中</el-tag>
          </div>

          <h2 class="module-title">{{ module.title }}</h2>
          <p class="module-description">{{ module.description }}</p>

          <div class="module-action">
            <span>进入模块</span>
            <el-icon class="module-arrow"><ArrowRight /></el-icon>
          </div>
        </router-link>
      </section>
    </main>

    <footer class="portal-footer">演示环境 · 仅用于功能展示</footer>
  </div>
</template>

<script setup>
import {
  ArrowRight,
  Document,
  House,
  MapLocation,
  Money,
  OfficeBuilding
} from '@element-plus/icons-vue'
import { useBackendHealth } from '../composables/useBackendHealth'

// 顶栏与门户页共用同一套后端健康状态逻辑
const { healthText, healthTagType } = useBackendHealth()

// 顶栏展示的当前日期
const today = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
}).format(new Date())

// 四个业务模块的门户入口配置
const modules = [
  {
    path: '/funds',
    title: '住宅专项维修资金',
    description: '资金交存、使用、结息与余额查询',
    icon: Money,
    color: '#1f5fae',
    bgColor: '#e8f1fb'
  },
  {
    path: '/lease',
    title: '国有资产租赁更新',
    description: '资产台账、租赁合同与续租管理',
    icon: OfficeBuilding,
    color: '#1f8a70',
    bgColor: '#e6f5f0'
  },
  {
    path: '/housing',
    title: '直管公房一张图',
    description: '公房点位分布与空间信息一张图展示',
    icon: MapLocation,
    color: '#c47b1a',
    bgColor: '#fdf2e3'
  },
  {
    path: '/documents',
    title: '政务公文接收',
    description: '政务公文收文、签收与办理跟踪',
    icon: Document,
    color: '#6b4fbb',
    bgColor: '#f0ecfb'
  }
]
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: #f5f7fb;
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 64px;
  padding: 0 32px;
  color: #ffffff;
  background: #081d38;
}

.portal-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.portal-brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
}

.portal-brand-title {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.portal-brand-subtitle {
  margin-top: 2px;
  font-size: 11px;
  color: #9fb8d8;
  letter-spacing: 2px;
}

.portal-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.portal-date {
  font-size: 13px;
  color: #c9d8ef;
}

.portal-main {
  flex: 1;
}

.portal-hero {
  padding: 56px 24px 104px;
  text-align: center;
  color: #ffffff;
  background: linear-gradient(135deg, #0b2647 0%, #1f5fae 100%);
}

.portal-hero-badge {
  display: inline-block;
  margin-bottom: 18px;
  padding: 4px 12px;
  font-size: 12px;
  color: #dce9f8;
  letter-spacing: 1px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 999px;
}

.portal-hero-title {
  margin: 0;
  font-size: 36px;
  font-weight: 600;
  letter-spacing: 2px;
}

.portal-hero-subtitle {
  margin: 14px 0 0;
  font-size: 15px;
  color: #c9d8ef;
  letter-spacing: 1px;
}

.portal-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 26px;
  width: 100%;
  max-width: 1120px;
  margin: -60px auto 0;
  padding: 0 24px 48px;
}

.module-card {
  display: flex;
  flex-direction: column;
  min-height: 240px;
  padding: 28px;
  color: inherit;
  text-decoration: none;
  background: #ffffff;
  border-top: 4px solid var(--module-color);
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(15, 39, 71, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 39, 71, 0.16);
}

.module-card:focus-visible {
  outline: 3px solid var(--module-color);
  outline-offset: 3px;
}

.module-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.module-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  color: var(--module-color);
  background: var(--module-bg);
  border-radius: 14px;
}

.module-title {
  margin: 22px 0 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
  word-break: break-word;
}

.module-description {
  flex: 1;
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #6b7a90;
}

.module-action {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 22px;
  font-size: 14px;
  font-weight: 500;
  color: var(--module-color);
}

.module-arrow {
  transition: transform 0.2s ease;
}

.module-card:hover .module-arrow {
  transform: translateX(4px);
}

.portal-footer {
  padding: 20px 24px;
  font-size: 13px;
  text-align: center;
  color: #8a97a8;
}

@media (max-width: 900px) {
  .portal-grid {
    grid-template-columns: 1fr;
    max-width: 640px;
  }
}

@media (max-width: 600px) {
  .portal-header {
    padding: 0 16px;
  }

  .portal-date {
    display: none;
  }

  .portal-hero {
    padding: 40px 16px 88px;
  }

  .portal-hero-title {
    font-size: 26px;
    letter-spacing: 1px;
  }

  .portal-hero-subtitle {
    font-size: 14px;
  }

  .portal-grid {
    gap: 18px;
    margin-top: -44px;
    padding: 0 16px 40px;
  }

  .module-card {
    min-height: 0;
    padding: 22px;
  }

  .module-title {
    font-size: 18px;
  }
}
</style>
