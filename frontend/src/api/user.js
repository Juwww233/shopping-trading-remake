import service from './index'; // 引入 axios 实例

// 1. 登录
export function login(data) {
    return service.post('/user/login', data);
}

// 2. 注册
export function register(data) {
    return service.post('/user/register', data);
}

// 3. 退出登录
export function logout() {
    // 直接调用 service.post，拦截器会自动带上 sessionId
    return service.post('/user/logout');
}

// 4. 获取详情 (移除手动 Header，依赖拦截器)
export function getCurrentUser(userId) {
    // 拦截器会自动添加 X-Session-Id，无需手动传 headers
    return service.get(`/user/info/${userId}`);
}

// 5. 更新资料 (移除手动 Header)
export function updateUserInfo(data) {
    return service.put('/user/update', data);
}

// 6. 上传头像 (移除手动 Header，但保留 Content-Type)
// 注意：axios 发送 FormData 时通常会自动设置 Content-Type 为 multipart/form-data 并带上 boundary
// 手动设置有时会导致 boundary 丢失，建议让 axios 自动处理，除非后端强制要求
export function uploadAvatar(formData) {
    return service.post('/user/avatar', formData, {
        // 通常不需要手动设 Content-Type，axios 会处理。
        // 如果后端报错，再尝试取消下面这行的注释
        // headers: { 'Content-Type': 'multipart/form-data' }
    });
}

// 7. 修改密码 (移除手动 Header)
export function changePassword(data) {
    return service.put('/user/changePassword', data);
}

// 【关键修改】默认导出 axios 实例本身，而不是方法对象
// 这样 Header.vue 就可以 import service from '@/api/user' 然后调用 service.post()
export default service;