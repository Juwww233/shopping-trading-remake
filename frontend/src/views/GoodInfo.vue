<template>
  <div class="good-info-container">
    <!-- 顶部返回导航 -->
    <div class="back-nav">
      <button class="back-btn" @click="goBack">← 返回列表</button>
      <span class="page-title">商品详情</span>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else-if="error" class="error-state">
      {{ error }}
      <button @click="fetchData">重试</button>
    </div>

    <div v-else-if="goods" class="detail-content">
      <!-- 左侧：商品图片 -->
      <div class="left-section">
        <div class="image-wrapper">
          <img
              :src="goods.img || '/images/default-goods.png'"
              alt="商品主图"
              class="main-img"
              @error="(e) => e.target.src = '/images/default-goods.png'"
          >
        </div>
      </div>

      <!-- 右侧：商品信息 -->
      <div class="right-section">
        <h1 class="goods-title">{{ goods.name }}</h1>

        <div class="price-block">
          <span class="price-label">价格</span>
          <span class="price-value">¥{{ goods.price.toFixed(2) }}</span>
        </div>

        <div class="meta-info">
          <div class="info-item">
            <span class="label">分类</span>
            <span class="value">{{ goods.category }}</span>
          </div>
          <div class="info-item">
            <span class="label">发布时间</span>
            <span class="value">{{ formatDate(goods.date) }}</span>
          </div>
          <div class="info-item">
            <span class="label">所在地</span>
            <span class="value">📍 {{ goods.address || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">浏览人数</span>
            <span class="value">{{ goods.readCount || 0 }} 人</span>
          </div>
        </div>

        <!-- 二手商品特有：成色展示 -->
        <div v-if="goods.category === '二手物品' && goods.content" class="condition-tag">
          成色提示：{{ goods.content.match(/\d+成新/)?.[0] || '详见描述' }}
        </div>

        <div class="description-block">
          <h3>商品描述</h3>
          <p class="desc-text">{{ goods.content || '暂无详细描述' }}</p>
        </div>

        <!-- 操作栏 -->
        <div class="action-bar">
          <button class="btn-contact" @click="handleContact">
            💬 联系卖家
          </button>
          <button class="btn-buy" @click="handleBuy">
            🛒 立即购买
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getGoodsDetail } from '@/api/goods';

const route = useRoute();
const router = useRouter();

const goods = ref(null);
const loading = ref(true);
const error = ref(null);

// 获取商品ID
const goodsId = route.params.id;

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

// 获取数据
const fetchData = async () => {
  loading.value = true;
  error.value = null;
  try {
    const res = await getGoodsDetail(goodsId);
    if (res.code === 200) {
      goods.value = res.data;
      // 可选：这里可以触发后端浏览量+1的逻辑，或者前端直接展示
    } else {
      error.value = res.msg || '获取商品详情失败';
    }
  } catch (err) {
    console.error(err);
    error.value = '网络异常，请稍后重试';
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

const handleContact = () => {
  if (!goods.value.userId) {
    alert('卖家信息缺失');
    return;
  }
  // 跳转到聊天页面，携带卖家ID
  router.push(`/chat?targetId=${goods.value.userId}`);
};

// 立即购买
const handleBuy = () => {
  // TODO: 创建订单逻辑
  alert(`正在生成订单：${goods.value.name} - ¥${goods.value.price}`);
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.good-info-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.back-nav {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.back-btn {
  background: none;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 15px;
  color: #666;
}

.back-btn:hover {
  background-color: #f5f5f5;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.loading-state, .error-state {
  text-align: center;
  padding: 60px 0;
  color: #666;
}

.detail-content {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.left-section {
  flex: 1;
  max-width: 500px;
}

.image-wrapper {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
}

.main-img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

.right-section {
  flex: 1.5;
  display: flex;
  flex-direction: column;
}

.goods-title {
  font-size: 24px;
  color: #1d2129;
  margin-bottom: 20px;
  line-height: 1.4;
}

.price-block {
  background-color: #fff7f7;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price-label {
  color: #f53f3f;
  font-size: 14px;
}

.price-value {
  color: #f53f3f;
  font-size: 28px;
  font-weight: bold;
}

.meta-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #4e5969;
}

.info-item .label {
  color: #86909c;
  margin-right: 8px;
}

.condition-tag {
  background-color: #e8f3ff;
  color: #1890ff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
  margin-bottom: 20px;
  display: inline-block;
  width: fit-content;
}

.description-block {
  margin-bottom: 30px;
  flex-grow: 1;
}

.description-block h3 {
  font-size: 16px;
  color: #1d2129;
  margin-bottom: 10px;
  border-left: 4px solid #1890ff;
  padding-left: 10px;
}

.desc-text {
  color: #4e5969;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符 */
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
}

.action-bar {
  display: flex;
  gap: 20px;
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-contact, .btn-buy {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-contact {
  background-color: #f5f7fa;
  color: #1d2129;
  border: 1px solid #dee0e3;
}

.btn-buy {
  background-color: #1890ff;
  color: #fff;
}

.btn-contact:hover, .btn-buy:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
    padding: 15px;
  }
  .left-section {
    max-width: 100%;
  }
  .meta-info {
    grid-template-columns: 1fr;
  }
}
</style>