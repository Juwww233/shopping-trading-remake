import service from './index';

export const getComments = (goodsId) => service.get(`/comment/goods/${goodsId}`);
export const addComment = (data) => service.post('/comment', data);
export const deleteComment = (id) => service.delete(`/comment/${id}`);
