// 导入axios实例
import service from './index';

// 登录接口
export function login(data) {
    return service.post('/user/login', data);
}

// 注册接口
export function register(data) {
    return service.post('/user/register', data);
}

// 可选：默认导出，兼容其他调用方式
export default {
    login,
    register
};