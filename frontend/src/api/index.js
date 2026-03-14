import axios from 'axios';

const service = axios.create({
    baseURL: 'http://localhost:8080', // 你的后端地址
    timeout: 5000
});

// 请求拦截器：自动注入 SessionId
service.interceptors.request.use(
    config => {
        const sessionId = localStorage.getItem('sessionId');
        if (sessionId) {
            config.headers['X-Session-Id'] = sessionId;
        }
        return config;
    },
    error => Promise.reject(error)
);

// 响应拦截器：统一处理 401 (未登录)
service.interceptors.response.use(
    response => response.data,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.clear();
            window.location.href = '/auth'; // 强制跳登录
        }
        return Promise.reject(error);
    }
);

export default service;