import api from './index.js';

// 猜你喜欢（方法名和Home.vue引入的保持一致）
export const getGuessYouLikeGoods = () => api.get('/goods/guessYouLike');

// 二手专区（原有方法名无需改，Home.vue里引入的名称匹配）
export const getSecondHandGoods = () => api.get('/goods/secondHand');

// 按分类查询商品（Home.vue必须用到）
export const getGoodsByCategory = (category) => api.get('/goods/category', {
    params: { category } // 传递分类参数给后端
});
// 商品详情查询
export const getGoodsDetail = (id) => api.get(`/goods/detail/${id}`);