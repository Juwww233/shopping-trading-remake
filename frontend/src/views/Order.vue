<template>
  <div class="order-container">
    <div class="back-nav">
      <button class="back-btn" @click="goBack">← 返回</button>
      <span class="page-title">我的订单</span>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="orderList.length === 0" class="empty">暂无订单</div>

    <div v-else class="order-list">
      <div class="order-item" v-for="order in orderList" :key="order.id">
        <div class="order-header">
          <span>订单号：{{ order.orderNo }}</span>
          <span :class="['status', order.status]">{{ order.status }}</span>
        </div>
        <div class="order-goods">
          <img :src="order.goodsImg" alt="商品图片" class="goods-img">
          <div class="goods-info">
            <p class="goods-name">{{ order.goodsName }}</p>
            <p class="goods-price">¥{{ order.goodsPrice }} × {{ order.count }}</p>
          </div>
        </div>
        <div class="order-footer">
          <span>实付款：¥{{ order.total }}</span>
          <span>创建时间：{{ order.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getOrderList } from '@/api/order';

const router = useRouter();
const orderList = ref([]);
const loading = ref(true);

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true;
  try {
    const res = await getOrderList();
    if (res.code === 200) {
      orderList.value = res.data;
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// 返回
const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchOrderList();
});
</script>

<style scoped>
.order-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.loading, .empty {
  text-align: center;
  padding: 60px 0;
  color: #666;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
  margin-bottom: 15px;
}

.status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.status.待处理 {
  color: #ff7d00;
  background: #fff7e6;
}
.status.已完成 {
  color: #00b42a;
  background: #e6ffed;
}
.status.失败 {
  color: #f53f3f;
  background: #fff2f0;
}

.order-goods {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.goods-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.goods-name {
  font-weight: 500;
  margin-bottom: 5px;
}
.goods-price {
  color: #f53f3f;
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 14px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style>