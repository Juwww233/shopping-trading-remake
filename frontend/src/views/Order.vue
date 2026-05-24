<template>
  <div class="order-page">
    <div class="top"><button class="back" @click="$router.back()">← 返回</button><h2>📋 我的订单</h2></div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!list.length" class="empty">📭 暂无订单<router-link to="/home">去购物</router-link></div>
    <div v-else class="list">
      <div v-for="o in list" :key="o.id" class="order">
        <div class="head">
          <span class="no">订单号：{{ o.orderNo?.substring(0,18) }}...</span>
          <span :class="['tag', tagClass(o.status)]">{{ o.status }}</span>
        </div>
        <div class="body" @click="$router.push(`/good/${o.goodsId}`)">
          <img :src="o.goodsImg" @error="e=>e.target.style.display='none'"/>
          <div class="detail"><h4>{{ o.goodsName }}</h4><p>¥{{ o.goodsPrice }} × {{ o.count }}</p></div>
          <div class="total"><span>实付</span><strong>¥{{ o.total }}</strong></div>
        </div>
        <div class="foot">
          <span class="time">{{ o.time }}</span>
          <div class="acts">
            <button v-if="o.status==='待支付'" class="btn-pay" @click="pay(o.orderNo)">立即支付</button>
            <button v-if="o.status==='待支付'" class="btn-cancel" @click="cancel(o.orderNo)">取消</button>
            <button v-if="o.status==='已发货'" class="btn-confirm" @click="confirm(o.orderNo)">确认收货</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getOrderList } from '@/api/order';
import service from '@/api/index';
const router = useRouter();
const list = ref([]); const loading = ref(true);
const fetch = async () => {
  try { const r = await getOrderList(); if(r.code===200) list.value=r.data; } catch(e){} finally { loading.value=false; }
};
const tagClass = s => ({'待支付':'warn','已支付':'info','已发货':'accent','已收货':'ok','已完成':'ok','已取消':'muted','失败':'err'}[s]||'');
const pay = async (no) => { try { const r = await service.post(`/order/${no}/pay`); if(r.code===200){alert('支付成功');fetch();} else alert(r.msg); } catch(e){} };
const cancel = async (no) => { if(!confirm('确认取消？'))return; try { const r = await service.post(`/order/${no}/cancel`); if(r.code===200){alert('已取消');fetch();} } catch(e){} };
const confirm = async (no) => { try { const r = await service.post(`/order/${no}/receive`); if(r.code===200){alert('收货成功');fetch();} } catch(e){} };
onMounted(fetch);
</script>

<style scoped>
.order-page { max-width: 960px; margin: 0 auto; padding: 24px; }
.top { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.back { background: none; font-size: 14px; color: var(--text-light); cursor: pointer; }
.back:hover { color: var(--primary); }
.top h2 { font-size: 24px; font-weight: 700; }
.loading, .empty { text-align: center; padding: 80px 0; color: var(--text-muted); font-size: 16px; }
.empty a { color: var(--primary); margin-left: 8px; }
.list { display: flex; flex-direction: column; gap: 14px; }
.order { background: #fff; border-radius: var(--radius-lg); box-shadow: var(--shadow); overflow: hidden; }
.head { display: flex; justify-content: space-between; align-items: center; padding: 12px 18px; background: #fafafa; border-bottom: 1px solid var(--border); }
.no { font-size: 13px; color: var(--text-light); }
.tag { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.tag.warn { background: #fff8e1; color: #f57f17; }
.tag.info { background: #e3f2fd; color: #1565c0; }
.tag.accent { background: #f3e5f5; color: #7b1fa2; }
.tag.ok { background: #e8f5e9; color: #2e7d32; }
.tag.muted { background: #f5f5f5; color: #9e9e9e; }
.tag.err { background: #ffebee; color: #c62828; }
.body { display: flex; align-items: center; gap: 14px; padding: 14px 18px; cursor: pointer; transition: background .15s; }
.body:hover { background: #fafafa; }
.body img { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; background: #f0f0f0; flex-shrink: 0; }
.detail { flex: 1; }
.detail h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.detail p { font-size: 13px; color: var(--text-light); }
.total { text-align: right; flex-shrink: 0; }
.total span { display: block; font-size: 12px; color: var(--text-muted); }
.total strong { font-size: 18px; font-weight: 700; }
.foot { display: flex; justify-content: space-between; align-items: center; padding: 10px 18px; border-top: 1px solid var(--border); }
.time { font-size: 12px; color: var(--text-muted); }
.acts { display: flex; gap: 8px; }
.acts button { padding: 6px 16px; border-radius: 6px; font-size: 13px; font-weight: 600; }
.btn-pay { background: var(--primary); color: #fff; }
.btn-cancel { background: #fff; color: var(--text-light); border: 1px solid var(--border); }
.btn-cancel:hover { border-color: var(--danger); color: var(--danger); }
.btn-confirm { background: var(--success); color: #fff; }

@media (max-width: 600px) { .total { display: none; } }
</style>
