import service from './index';

export const addCollect = (goodsId) => service.post('/collect', null, { params: { goodsId } });
export const removeCollect = (goodsId) => service.delete('/collect', { params: { goodsId } });
export const getCollectList = () => service.get('/collect/list');
export const checkCollect = (goodsId) => service.get('/collect/check', { params: { goodsId } });
