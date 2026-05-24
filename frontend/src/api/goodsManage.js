import service from './index';

export const publishGoods = (data) => service.post('/goods/manage/publish', data);
export const updateGoods = (id, data) => service.put(`/goods/manage/${id}`, data);
export const toggleSaleStatus = (id, saleStatus) => service.put(`/goods/manage/${id}/sale-status`, null, { params: { saleStatus } });
export const reviewGoods = (id, status) => service.put(`/goods/manage/${id}/review`, null, { params: { status } });
export const getMyGoods = () => service.get('/goods/manage/my-list');
export const searchGoods = (keyword) => service.get('/goods/search', { params: { keyword } });
