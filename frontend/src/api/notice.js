import service from './index';

export const getNoticeList = () => service.get('/notice/list');
export const getNoticeById = (id) => service.get(`/notice/${id}`);
export const addNotice = (data) => service.post('/notice', data);
export const deleteNotice = (id) => service.delete(`/notice/${id}`);
