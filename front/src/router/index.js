import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import PlaceholderView from '../views/PlaceholderView.vue'

// 四个业务模块当前仅展示占位页，后续可逐一替换为真实模块页面
const moduleRoutes = [
  {
    path: 'funds',
    name: 'Funds',
    component: PlaceholderView,
    meta: {
      title: '住宅专项维修资金',
      description: '本模块功能暂未实现。'
    }
  },
  {
    path: 'lease',
    name: 'Lease',
    component: PlaceholderView,
    meta: {
      title: '国有资产租赁更新',
      description: '本模块功能暂未实现。'
    }
  },
  {
    path: 'housing',
    name: 'Housing',
    component: PlaceholderView,
    meta: {
      title: '直管公房一张图',
      description: '本模块功能暂未实现。'
    }
  },
  {
    path: 'documents',
    name: 'Documents',
    component: PlaceholderView,
    meta: {
      title: '政务公文接收',
      description: '本模块功能暂未实现。'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      redirect: '/funds',
      children: moduleRoutes
    },
    {
      // 未知路径统一回到第一个模块
      path: '/:pathMatch(.*)*',
      redirect: '/funds'
    }
  ]
})

export default router
