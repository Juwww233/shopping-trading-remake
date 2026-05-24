<template>
  <div class="detail-page">
    <div class="top"><button class="back" @click="$router.back()">← 返回</button><span>商品详情</span></div>

    <div v-if="loading" class="sk-wrap"><div class="sk-big"/></div>
    <div v-else-if="err" class="err">{{ err }}<button @click="fetch">重试</button></div>

    <div v-else-if="goods" class="main">
      <div class="img-col">
        <div class="img-box"><img :src="goods.img" @error="e=>e.target.src='/default.png'"/></div>
      </div>
      <div class="info-col">
        <h1>{{ goods.name }}</h1>
        <div class="price-line">
          <span class="big-price">¥{{ goods.price?.toFixed(2) }}</span>
          <span :class="['stock', {low: goods.stock<=10 && goods.stock>0, zero: goods.stock<=0}]">
            {{ goods.stock > 0 ? `库存 ${goods.stock} 件` : '已售罄' }}
          </span>
        </div>
        <div class="meta">
          <div class="m-item"><span>分类</span><strong>{{ goods.category }}</strong></div>
          <div class="m-item"><span>发布</span><strong>{{ fmtDate(goods.date) }}</strong></div>
          <div class="m-item"><span>所在地</span><strong>{{ goods.address || '未知' }}</strong></div>
          <div class="m-item"><span>浏览</span><strong>{{ goods.readCount||0 }}次</strong></div>
        </div>
        <div class="desc"><h3>商品描述</h3><p>{{ goods.content||'暂无描述' }}</p></div>

        <div v-if="msg" :class="['tip', tipType]">{{ msg }}</div>

        <div class="actions">
          <button class="a-contact" @click="contact">💬 联系卖家</button>
          <button :class="['a-collect',{on:collected}]" @click="toggleCollect">{{ collected?'❤️ 已收藏':'🤍 收藏' }}</button>
          <button class="a-buy" :disabled="goods.stock<=0" @click="openBuy">{{ goods.stock<=0?'已售罄':'🛒 立即购买' }}</button>
        </div>

        <div class="comments">
          <h3>商品评论 ({{ comments.length }})</h3>
          <div v-if="!comments.length" class="no-c">暂无评论</div>
          <div v-for="c in comments" :key="c.id" class="c-item">
            <div class="c-head"><span>用户{{ c.userId }}</span><span class="c-time">{{ c.time }}</span></div>
            <p>{{ c.content }}</p>
          </div>
          <div class="c-input" v-if="currentUser">
            <textarea v-model="nc" rows="2" placeholder="写下评价..."></textarea>
            <button @click="doComment" :disabled="!nc.trim()">发表</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Buy Modal -->
    <div v-if="showModal" class="modal" @click.self="showModal=false">
      <div class="modal-box">
        <h3>确认订单</h3>
        <div class="modal-goods"><img :src="goods.img"/><div><h4>{{ goods.name }}</h4><span>¥{{ goods.price?.toFixed(2) }}</span></div></div>
        <div class="m-field"><label>数量 (剩余{{ goods.stock }})</label><input v-model.number="bc" type="number" min="1" :max="goods.stock" @input="vCount"/></div>
        <div class="m-field"><label>收货人</label><input v-model="un" placeholder="姓名"/></div>
        <div class="m-field"><label>电话</label><input v-model="ph" placeholder="手机号"/></div>
        <div class="m-field"><label>地址</label><input v-model="ad" placeholder="详细地址"/></div>
        <div class="m-btns"><button @click="showModal=false">取消</button><button class="ok" @click="doOrder" :disabled="submitting">{{ submitting?'提交中...':'确认购买' }}</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getGoodsDetail } from '@/api/goods';
import { createOrder } from '@/api/order';
import { Stomp } from '@stomp/stompjs';
import { checkCollect, addCollect, removeCollect } from '@/api/collect';
import { getComments, addComment as postComment } from '@/api/comment';

const route = useRoute(); const router = useRouter();
const gid = route.params.id;
const goods = ref(null); const loading = ref(true); const err = ref(null);
const showModal = ref(false); const bc = ref(1); const un = ref(''); const ph = ref(''); const ad = ref('');
const submitting = ref(false); const msg = ref(''); const tipType = ref('');
const collected = ref(false); const comments = ref([]); const nc = ref('');
const currentUser = ref(null);
try { const u = localStorage.getItem('userInfo'); if(u) currentUser.value = JSON.parse(u); } catch(e) {}
let stomp = null;

const fmtDate = s => { if(!s) return ''; const d=new Date(s); return isNaN(d.getTime())?s:d.toLocaleDateString(); };
const vCount = () => { if(goods.value && bc.value>goods.value.stock) bc.value=goods.value.stock; if(bc.value<1) bc.value=1; };

const fetch = async () => {
  loading.value=true; err.value=null;
  try {
    const r = await getGoodsDetail(gid);
    if(r.code===200) { goods.value=r.data; bc.value=1; if(goods.value.stock<1) bc.value=0; checkC(); fetchC(); }
    else err.value=r.msg;
  } catch(e) { err.value='网络异常'; } finally { loading.value=false; }
};

const contact = () => { if(goods.value?.userId) router.push(`/chat?targetId=${goods.value.userId}`); else alert('卖家信息缺失'); };
const openBuy = () => { if(!goods.value||goods.value.stock<=0) return; showModal.value=true; };

const doOrder = async () => {
  if(!un.value||!ph.value||!ad.value) return alert('请填写完整信息');
  submitting.value=true; msg.value='';
  try {
    const r = await createOrder({ goodsId:gid, buyCount:bc.value, address:ad.value, phone:ph.value, userName:un.value });
    if(r.code===200) { const {orderNo,msg:m}=r.data; msg.value=m; tipType.value='ok'; showModal.value=false; goods.value.stock-=bc.value; initStomp(orderNo); }
    else { msg.value=r.msg; tipType.value='err'; }
  } catch(e) { msg.value='创建失败'; tipType.value='err'; } finally { submitting.value=false; }
};

const initStomp = (ono) => {
  stomp = Stomp.over(()=>new WebSocket('ws://localhost:8080/ws-chat'));
  stomp.heartbeat.outgoing=0; stomp.heartbeat.incoming=0;
  stomp.connect({},()=>{
    stomp.subscribe(`/order/${ono}`,(m)=>{
      const d=JSON.parse(m.body);
      if(d.status==='已完成'||d.status==='已发货'){msg.value='✅ '+(d.msg||'处理完成');tipType.value='ok';}
      else {msg.value='❌ '+(d.msg||'失败');tipType.value='err';fetch();}
      setTimeout(()=>{if(stomp)stomp.disconnect();router.push('/order');},2000);
    });
  },()=>{msg.value='连接失败';tipType.value='err';});
};
onUnmounted(()=>{if(stomp)stomp.disconnect();});

const toggleCollect = async () => {
  try { const r = collected.value ? await removeCollect(gid) : await addCollect(gid); if(r.code===200) collected.value=!collected.value; } catch(e) {}
};
const checkC = async () => { try { const r=await checkCollect(gid); if(r.code===200) collected.value=r.data; } catch(e){} };
const fetchC = async () => { try { const r=await getComments(gid); if(r.code===200) comments.value=r.data; } catch(e){} };
const doComment = async () => {
  if(!nc.value.trim()) return;
  try { const r=await postComment({goodsId:parseInt(gid),content:nc.value.trim()}); if(r.code===200){comments.value.unshift(r.data);nc.value='';} } catch(e){}
};

onMounted(fetch);
</script>

<style scoped>
.detail-page { max-width: var(--max-width); margin: 0 auto; padding: 24px; }
.top { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.back { background: none; font-size: 14px; color: var(--text-light); cursor: pointer; }
.back:hover { color: var(--primary); }
.top span { font-size: 16px; font-weight: 600; }
.sk-wrap { padding: 40px 0; }
.sk-big { padding-bottom: 50%; background: linear-gradient(90deg,#eee 25%,#f5f5f5 50%,#eee 75%); background-size:200% 100%; animation:shim 1.5s infinite; border-radius: var(--radius-lg); }
@keyframes shim { 0%{background-position:200% 0}100%{background-position:-200% 0} }
.err { text-align: center; padding: 80px 0; color: var(--text-muted); }
.err button { margin-top: 12px; padding: 8px 20px; background: var(--primary); color: #fff; border-radius: 6px; }

.main { display: flex; gap: 36px; background: #fff; border-radius: var(--radius-lg); padding: 28px; box-shadow: var(--shadow); }
@media(max-width:768px){ .main{flex-direction:column;padding:16px;} }
.img-col { flex: 1; max-width: 480px; }
.img-box { border-radius: var(--radius); overflow: hidden; background: #f0f0f0; }
.img-box img { width: 100%; display: block; }
.info-col { flex: 1.3; min-width: 0; display: flex; flex-direction: column; gap: 18px; }
.info-col h1 { font-size: 24px; font-weight: 700; line-height: 1.3; }
.price-line { display: flex; align-items: center; gap: 14px; }
.big-price { font-size: 30px; font-weight: 800; color: var(--danger); }
.stock { padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; background: #e8f5e9; color: #2e7d32; }
.stock.low { background: #fff8e1; color: #f57f17; }
.stock.zero { background: #ffebee; color: #c62828; }

.meta { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; padding: 14px; background: #fafafa; border-radius: var(--radius); }
.m-item { display: flex; flex-direction: column; gap: 2px; }
.m-item span { font-size: 11px; color: var(--text-muted); }
.m-item strong { font-size: 14px; font-weight: 600; }

.desc h3 { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.desc p { font-size: 14px; color: var(--text-light); line-height: 1.8; background: #fafafa; padding: 14px; border-radius: var(--radius); white-space: pre-wrap; }

.tip { padding: 10px 14px; border-radius: var(--radius); font-weight: 500; font-size: 14px; }
.tip.ok { background: #e8f5e9; color: #2e7d32; }
.tip.err { background: #ffebee; color: #c62828; }

.actions { display: flex; gap: 10px; flex-wrap: wrap; }
.a-contact { padding: 10px 18px; background: #f5f5f5; border-radius: var(--radius); font-size: 14px; font-weight: 600; color: var(--text); border: 1px solid var(--border); }
.a-contact:hover { background: #e0e0e0; }
.a-collect { padding: 10px 18px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 14px; font-weight: 600; background: #fff; color: var(--text-light); }
.a-collect:hover { border-color: var(--danger); color: var(--danger); }
.a-collect.on { border-color: #fecaca; background: #fff5f5; color: var(--danger); }
.a-buy { padding: 10px 20px; background: #6c5ce7 !important; color: #fff !important; border-radius: var(--radius); font-size: 15px; font-weight: 700; border: none !important; }
.a-buy:hover { background: #5a4bd1 !important; }
.a-buy:disabled { opacity: 0.5; cursor: not-allowed; }

.comments { border-top: 1px solid var(--border); padding-top: 18px; }
.comments h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.no-c { color: var(--text-muted); text-align: center; padding: 20px 0; font-size: 14px; }
.c-item { background: #fafafa; padding: 12px 14px; border-radius: var(--radius); margin-bottom: 8px; }
.c-head { display: flex; justify-content: space-between; margin-bottom: 4px; }
.c-head span { font-size: 13px; font-weight: 600; color: var(--text-light); }
.c-time { font-size: 11px; color: var(--text-muted); font-weight: 400; }
.c-item p { font-size: 14px; color: var(--text); margin: 0; }
.c-input { display: flex; gap: 8px; margin-top: 12px; }
.c-input textarea { flex: 1; padding: 8px 12px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 13px; resize: none; }
.c-input textarea:focus { border-color: var(--primary); }
.c-input button { padding: 8px 16px; background: var(--primary); color: #fff; border-radius: 6px; font-size: 13px; font-weight: 600; white-space: nowrap; }
.c-input button:disabled { opacity: 0.5; }

/* Modal */
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-box { background: #fff; border-radius: var(--radius-lg); width: 90%; max-width: 440px; padding: 24px; box-shadow: var(--shadow-hover); }
.modal-box h3 { font-size: 18px; font-weight: 700; text-align: center; margin-bottom: 18px; }
.modal-goods { display: flex; gap: 12px; padding: 12px; background: #fafafa; border-radius: var(--radius); margin-bottom: 16px; }
.modal-goods img { width: 64px; height: 64px; border-radius: 6px; object-fit: cover; }
.modal-goods h4 { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.modal-goods span { font-size: 18px; font-weight: 700; color: var(--danger); }
.m-field { margin-bottom: 12px; }
.m-field label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 4px; color: var(--text-light); }
.m-field input { width: 100%; padding: 9px 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; box-sizing: border-box; }
.m-field input:focus { border-color: var(--primary); }
.m-btns { display: flex; gap: 10px; margin-top: 18px; }
.m-btns button { flex: 1; padding: 10px; border-radius: 6px; font-size: 14px; font-weight: 600; }
.m-btns button:first-child { background: #f5f5f5; color: var(--text-light); }
.m-btns button.ok { background: #6c5ce7 !important; color: #fff !important; border: none !important; }
.m-btns button.ok:hover { background: #5a4bd1 !important; }
.m-btns button.ok:disabled { opacity: 0.5; }
</style>
