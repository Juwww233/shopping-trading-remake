import { createRouter, createWebHistory } from 'vue-router';

// 导入页面组件
import AuthPage from '@/views/AuthPage.vue'; // 登录/注册页
import Home from '@/views/Home.vue';         // 首页
import Category from '@/views/Category.vue'; // 分类页（预留，匹配changeCategory跳转）
import GoodInfo from '@/views/GoodInfo.vue'; // 商品详情页（预留，匹配goToGoodDetail跳转）

// 定义路由规则
const routes = [
    // 默认路由：访问根路径跳登录页
    {
        path: '/',
        redirect: '/auth'
    },
    // 登录/注册页
    {
        path: '/auth',
        name: 'Auth',
        component: AuthPage,
        meta: {
            requiresAuth: false // 无需登录即可访问
        }
    },
    // 首页
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: {
            requiresAuth: true // 需要登录才能访问
        }
    },
    // 分类页（匹配Home.vue里的changeCategory跳转）
    {
        path: '/category/:type', // 动态路由参数：分类类型（电子产品/美食等）
        name: 'Category',
        component: Category,
        meta: {
            requiresAuth: true
        }
    },
    // 商品详情页（匹配Home.vue里的goToGoodDetail跳转）
    {
        path: '/good/:id', // 动态路由参数：商品ID
        name: 'GoodInfo',
        component: GoodInfo,
        meta: {
            requiresAuth: true
        }
    },
    // 404页面（可选，处理无效路由）
    {
        path: '/:pathMatch(.*)*',
        redirect: '/auth'
    }
];

// 创建路由实例
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL), // Vue3 Vite环境，若用Vue CLI则改为process.env.BASE_URL
    routes
});

// 路由守卫：全局前置守卫，检查登录状态
router.beforeEach((to, from, next) => {
    // 判断当前路由是否需要登录
    if (to.meta.requiresAuth) {
        // 从localStorage获取登录标识（匹配登录接口返回的sessionId）
        const sessionId = localStorage.getItem('sessionId');
        if (sessionId) {
            // 已登录，放行
            next();
        } else {
            // 未登录，跳转到登录页，并记录目标路径（登录后可跳转回去）
            next({
                path: '/auth',
                query: { redirect: to.fullPath } // 传递目标路径参数
            });
        }
    } else {
        // 无需登录的路由，直接放行
        next();
    }
});

export default router;