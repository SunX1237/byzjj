import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import PlaceholderView from '../views/PlaceholderView.vue'
import PortalView from '../views/PortalView.vue'
import FundQueryView from '../views/funds/FundQueryView.vue'

// 维修资金已实现查询页面，其余三个模块仍为占位页
const moduleRoutes = [
  {
    path: 'funds',
    name: 'Funds',
    component: FundQueryView,
    meta: {
      title: '住宅专项维修资金',
      description: '按项目、楼幢、单元、房屋查询资金交存、使用、结息与余额。'
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
      // 独立门户页，不套用后台侧边栏布局
      path: '/portal',
      name: 'Portal',
      component: PortalView,
      meta: { title: '门户首页' }
    },
    {
      path: '/',
      component: AppLayout,
      redirect: '/portal',
      children: moduleRoutes
    },
    {
      // 未知路径统一回到门户首页
      path: '/:pathMatch(.*)*',
      redirect: '/portal'
    }
  ]
})

export default router
