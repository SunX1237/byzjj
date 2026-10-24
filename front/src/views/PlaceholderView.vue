<template>
  <!-- 当前阶段四个模块共用的白色占位页，仅展示固定文本 -->
  <section class="placeholder-page">
    <div class="placeholder-content">
      <h1 class="placeholder-title">{{ title }}</h1>
      <p class="placeholder-description">{{ description }}</p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 模块标题与占位说明由路由 meta 提供
const title = computed(() => route.meta.title || '未命名模块')
const description = computed(() => route.meta.description || '本模块功能暂未实现。')
</script>

<style scoped>
.placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  padding: 48px;
  background: #ffffff;
}

.placeholder-content {
  text-align: center;
}

.placeholder-title {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #1f2d3d;
}

.placeholder-description {
  margin: 0;
  font-size: 15px;
  color: #7a8699;
}
</style>
