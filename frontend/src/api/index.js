import axios from 'axios';

// 创建axios实例（核心：能发起请求的对象）
const service = axios.create({
    baseURL: 'http://localhost:8080', // 后端接口根地址，必须和后端端口一致
    timeout: 5000, // 请求超时时间
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
});

// 请求拦截器：自动携带sessionId到请求头
service.interceptors.request.use(
    (config) => {
        const sessionId = localStorage.getItem('sessionId');
        if (sessionId) {
            config.headers.sessionId = sessionId; // 给请求头加sessionId
        }
        return config;
    },
    (error) => {
        console.error('请求拦截器错误：', error);
        return Promise.reject(error);
    }
);

// 响应拦截器：统一处理后端返回结果
service.interceptors.response.use(
    (response) => {
        const res = response.data;
        // 401=session过期/未登录，跳回登录页
        if (res.code === 401) {
            alert('登录已过期，请重新登录！');
            localStorage.removeItem('sessionId');
            window.location.href = '/login';
        }
        return res; // 只返回后端的data部分，前端直接用
    },
    (error) => {
        console.error('响应拦截器错误：', error);
        return Promise.reject(error); // 把错误抛出去，让业务代码处理提示
    }
);

// 导出axios实例，供user.js/goods.js使用
export default service;