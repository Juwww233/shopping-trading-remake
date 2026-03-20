import { createRouter, createWebHistory } from 'vue-router';

// 1. 导入页面组件
import AuthPage from '@/views/AuthPage.vue';
import Home from '@/views/Home.vue';
import Category from '@/views/Category.vue';
import GoodInfo from '@/views/GoodInfo.vue';
import UserProfile from '@/views/UserProfile.vue';
import ChatView from "@/views/ChatView.vue";

const routes = [
    {
        path: '/',
        redirect: '/auth'
    },
    {
        path: '/auth',
        name: 'Auth',
        component: AuthPage,
        meta: { requiresAuth: false }
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true }
    },
    {
        path: '/category/:type',
        name: 'Category',
        component: Category,
        meta: { requiresAuth: true }
    },
    {
        path: '/good/:id',
        name: 'GoodInfo',
        component: GoodInfo,
        meta: { requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'UserProfile',
        component: UserProfile,
        meta: { requiresAuth: true }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/auth'
    },
    {
        path: '/chat',
        name: 'Chat',
        component: () => import('../views/ChatView.vue')
    }
];

const baseUrl = typeof import.meta !== 'undefined' && import.meta.env
    ? import.meta.env.BASE_URL
    : (process.env.BASE_URL || '/');

const router = createRouter({
    history: createWebHistory(baseUrl),
    routes
});

router.beforeEach((to, from, next) => {
    console.log('🔍 路由跳转:', from.path, '→', to.path);

    if (to.path === '/auth') {
        next();
        return;
    }
    if (to.meta.requiresAuth) {
        const sessionId = localStorage.getItem('sessionId');
        console.log('🔐 SessionId:', sessionId);
        if (sessionId) {
            next();
        } else {
            console.warn('⚠️ 未登录，重定向到 auth');
            next({ path: '/auth', query: { redirect: to.fullPath } });
        }
    } else {
        next();
    }
});

export default router;