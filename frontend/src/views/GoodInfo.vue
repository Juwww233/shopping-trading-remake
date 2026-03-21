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

        <!-- ✅ 新增：库存显示 -->
        <div class="stock-block" :class="{ 'low-stock': goods.stock <= 10 }">
          <span class="stock-label">库存</span>
          <span class="stock-value">{{ goods.stock }} 件</span>
          <span v-if="goods.stock === 0" class="sold-out-tag">已售罄</span>
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

        <div v-if="goods.category === '二手物品' && goods.content" class="condition-tag">
          成色提示：{{ goods.content.match(/\d+成新/)?.[0] || '详见描述' }}
        </div>

        <div class="description-block">
          <h3>商品描述</h3>
          <p class="desc-text">{{ goods.content || '暂无详细描述' }}</p>
        </div>

        <!-- 订单状态提示 -->
        <div v-if="orderMsg" class="order-tip" :class="orderStatus">
          {{ orderMsg }}
        </div>

        <!-- 操作栏 -->
        <div class="action-bar">
          <button class="btn-contact" @click="handleContact">
            💬 联系卖家
          </button>
          <button
              class="btn-buy"
              @click="handleBuy"
              :disabled="buyLoading || goods.stock <= 0"
          >
            <span v-if="buyLoading">处理中...</span>
            <span v-else-if="goods.stock <= 0">已售罄</span>
            <span v-else>🛒 立即购买</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 收货信息弹窗 -->
    <div v-if="showBuyModal" class="modal-mask">
      <div class="modal-content">
        <h3>填写收货信息</h3>

        <div class="form-item">
          <label>购买数量 <span class="hint">(剩余: {{ goods.stock }})</span></label>
          <!-- ✅ 增加 max 限制和 min 限制 -->
          <input
              v-model.number="buyCount"
              type="number"
              min="1"
              :max="goods.stock"
              @input="validateCount"
          >
        </div>

        <div class="form-item">
          <label>收货人</label>
          <input v-model="userName" type="text" placeholder="请输入姓名">
        </div>
        <div class="form-item">
          <label>联系电话</label>
          <input v-model="phone" type="text" placeholder="请输入手机号">
        </div>
        <div class="form-item">
          <label>收货地址</label>
          <input v-model="address" type="text" placeholder="请输入详细地址">
        </div>

        <div class="modal-btns">
          <button @click="showBuyModal=false">取消</button>
          <button @click="submitOrder" :disabled="submitLoading">
            {{ submitLoading ? '提交中...' : '确认购买' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getGoodsDetail } from '@/api/goods';
import { createOrder } from '@/api/order';
import { Stomp } from '@stomp/stompjs';  // ✅ 只导入 Stomp

const route = useRoute();
const router = useRouter();

const goods = ref(null);
const loading = ref(true);
const error = ref(null);
const goodsId = route.params.id;

// 购买相关
const showBuyModal = ref(false);
const buyCount = ref(1);
const userName = ref('');
const phone = ref('');
const address = ref('');
const buyLoading = ref(false);
const submitLoading = ref(false);
const orderMsg = ref('');
const orderStatus = ref('');

// STOMP 实例
let stompClient = null;

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

// 验证数量不超过库存
const validateCount = () => {
  if (goods.value && buyCount.value > goods.value.stock) {
    buyCount.value = goods.value.stock;
  }
  if (buyCount.value < 1) {
    buyCount.value = 1;
  }
};

// 获取商品数据
const fetchData = async () => {
  loading.value = true;
  error.value = null;
  try {
    const res = await getGoodsDetail(goodsId);
    if (res.code === 200) {
      goods.value = res.data;
      buyCount.value = 1;
      if (goods.value.stock < 1) buyCount.value = 0;
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

// 返回
const goBack = () => {
  router.back();
};

// 联系卖家
const handleContact = () => {
  if (!goods.value || !goods.value.userId) {
    alert('卖家信息缺失');
    return;
  }
  router.push(`/chat?targetId=${goods.value.userId}`);
};

// 打开购买弹窗
const handleBuy = () => {
  if (!goods.value || goods.value.stock <= 0) {
    alert('商品已售罄');
    return;
  }
  showBuyModal.value = true;
};

// 提交订单
const submitOrder = async () => {
  if (!userName.value || !phone.value || !address.value) {
    alert('请填写完整收货信息');
    return;
  }
  if (buyCount.value < 1 || buyCount.value > goods.value.stock) {
    alert('购买数量无效');
    return;
  }

  submitLoading.value = true;
  orderMsg.value = '';

  try {
    const res = await createOrder({
      goodsId: goodsId,
      buyCount: buyCount.value,
      address: address.value,
      phone: phone.value,
      userName: userName.value
    });

    if (res.code === 200) {
      const { orderNo, msg } = res.data;
      orderMsg.value = msg;
      orderStatus.value = 'success';
      showBuyModal.value = false;

      goods.value.stock -= buyCount.value;

      initStompListen(orderNo);
    } else {
      orderMsg.value = res.msg;
      orderStatus.value = 'error';
      fetchData();
    }
  } catch (err) {
    console.error(err);
    orderMsg.value = '订单创建失败，请重试';
    orderStatus.value = 'error';
    fetchData();
  } finally {
    submitLoading.value = false;
  }
};

// STOMP 监听订单处理结果（使用原生 WebSocket）
const initStompListen = (orderNo) => {
  // ✅ 直接使用原生 WebSocket，不需要 SockJS
  stompClient = Stomp.over(() => new WebSocket('ws://localhost:8080/ws-chat'));

  stompClient.heartbeat.outgoing = 0;
  stompClient.heartbeat.incoming = 0;

  stompClient.connect({}, () => {
    console.log('STOMP 连接成功');
    stompClient.subscribe(`/order/${orderNo}`, (message) => {
      const result = JSON.parse(message.body);
      console.log('收到订单推送:', result);

      if (result.status === '已完成' || result.status === 'SUCCESS') {
        orderMsg.value = `✅ 订单${result.orderNo}处理完成！`;
        orderStatus.value = 'success';
      } else {
        orderMsg.value = `❌ 订单${result.orderNo}处理失败！库存已回滚。`;
        orderStatus.value = 'error';
        fetchData();
      }

      setTimeout(() => {
        if (stompClient) {
          stompClient.disconnect();
        }
        router.push('/order');
      }, 2000);
    });
  }, (error) => {
    console.error('STOMP 连接失败:', error);
    orderMsg.value = '订单监听连接失败，可前往订单页查看状态';
    orderStatus.value = 'error';
  });
};

// 页面销毁断开连接
onUnmounted(() => {
  if (stompClient) {
    stompClient.disconnect();
  }
});

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
/* ... (保持你原有的 CSS 不变，仅新增以下样式) ... */

.stock-block {
  background-color: #f6ffed;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
  gap: 10px;
  border: 1px solid #b7eb8f;
}
.stock-block.low-stock {
  background-color: #fff2f0;
  border-color: #ffccc7;
}
.stock-label {
  color: #52c41a;
  font-size: 14px;
}
.low-stock .stock-label {
  color: #f5222d;
}
.stock-value {
  color: #333;
  font-size: 18px;
  font-weight: bold;
}
.sold-out-tag {
  background: #f5222d;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 10px;
}
.hint {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

/* 其余原有 CSS 保持不变... */
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
  white-space: pre-wrap;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
}
.order-tip {
  padding: 12px 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-weight: 500;
}
.order-tip.success {
  background: #e6ffed;
  color: #00b42a;
}
.order-tip.error {
  background: #fff2f0;
  color: #f53f3f;
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
.btn-buy:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #ccc;
}
.btn-contact:hover, .btn-buy:hover:not(:disabled) {
  opacity: 0.9;
}
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-content {
  background: #fff;
  width: 90%;
  max-width: 500px;
  padding: 25px;
  border-radius: 8px;
}
.modal-content h3 {
  margin-bottom: 20px;
  text-align: center;
}
.form-item {
  margin-bottom: 15px;
}
.form-item label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}
.form-item input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
.modal-btns {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}
.modal-btns button {
  flex: 1;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
}
.modal-btns button:first-child {
  border: 1px solid #ddd;
  background: #fff;
}
.modal-btns button:last-child {
  background: #1890ff;
  color: #fff;
  border: none;
}
.modal-btns button:disabled {
  opacity: 0.6;
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