import service from './index'; // 引入统一的 axios 实例

/**
 * 创建订单
 * @param {Object} data - 业务参数对象
 * @param {Number} data.goodsId - 商品ID
 * @param {Number} data.buyCount - 购买数量
 * @param {String} data.address - 收货地址
 * @param {String} data.phone - 联系电话
 * @param {String} data.userName - 收货人姓名
 * @returns {Promise}
 */
export function createOrder(data) {
    // 只需要传递业务参数 (params 或 data)
    // X-Session-Id 将由 axios 拦截器自动从 localStorage 获取并添加到 Header
    return service.post('/order/create', null, {
        params: data
        // 注意：后端接口定义的是 @RequestParam，所以这里用 params 将对象序列化为查询字符串
        // 如果后端改为 @RequestBody，则改为 data: data
    });
}

/**
 * 根据订单号查询订单详情
 * @param {String} orderNo - 订单号
 * @returns {Promise}
 */
export function queryOrder(orderNo) {
    return service.get(`/order/query/${orderNo}`);
}

/**
 * 获取当前用户的订单列表
 * @returns {Promise}
 */
export function getOrderList() {
    return service.get('/order/list');
}

// 【关键】默认导出 axios 实例本身
// 这样在其他地方如果需要特殊操作，可以直接 import service from '@/api/order' 使用
export default service;