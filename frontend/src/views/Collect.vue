<template>
  <div class="collect-page">
    <div class="top"><h2>❤️ 我的收藏</h2><span v-if="list.length">{{ list.length }} 件</span></div>
    <div v-if="loading" class="grid">
      <div v-for="i in 4" :key="i" class="sk"><div class="sk-img"/><div class="sk-l"/></div>
    </div>
    <div v-else-if="!list.length" class="empty">💝 还没有收藏，<router-link to="/home">去逛逛</router-link></div>
    <div v-else class="grid">
      <div v-for="item in list" :key="item.id" class="card" @click="$router.push(`/good/${item.goodsId}`)">
        <div class="img"><img :src="item.goodsImg||'/default.png'" @error="e=>e.target.src='/default.png'"/><button class="del" @click.stop="remove(item.goodsId)">✕</button><span class="price">¥{{ item.goodsPrice }}</span></div>
        <div class="info"><h4>{{ item.goodsName }}</h4><span :class="{ zero: item.goodsStock<=0 }">{{ item.goodsStock>0?`库存 ${item.goodsStock}`:'售罄' }}</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getCollectList, removeCollect } from '@/api/collect';
const list = ref([]); const loading = ref(true);
const fetch = async () => {
  loading.value=true;
  try { const r=await getCollectList(); if(r.code===200) list.value=r.data; } catch(e){} finally { loading.value=false; }
};
const remove = async (id) => { try { const r=await removeCollect(id); if(r.code===200) list.value=list.value.filter(c=>c.goodsId!==id); } catch(e){} };
onMounted(fetch);
</script>

<style scoped>
.collect-page { max-width: var(--max-width); margin: 0 auto; padding: 24px; }
.top { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.top h2 { font-size: 24px; font-weight: 700; }
.top span { font-size: 14px; color: var(--text-muted); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card { background: #fff; border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow); transition: all .25s; position: relative; }
.card:hover { transform: translateY(-4px); box-shadow: var(--shadow-hover); }
.img { position: relative; width: 100%; padding-bottom: 100%; background: #f0f0f0; overflow: hidden; }
.img img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform .4s; }
.card:hover .img img { transform: scale(1.06); }
.del { position: absolute; top: 8px; right: 8px; width: 24px; height: 24px; border-radius: 50%; background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity .2s; }
.card:hover .del { opacity: 1; }
.del:hover { background: var(--danger); }
.price { position: absolute; bottom: 8px; left: 8px; background: rgba(0,0,0,0.7); color: #fff; padding: 3px 10px; border-radius: 4px; font-size: 14px; font-weight: 700; }
.info { padding: 12px 14px; }
.info h4 { font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; }
.info span { font-size: 12px; color: var(--text-muted); }
.info span.zero { color: var(--danger); }
.sk { background: #fff; border-radius: var(--radius-lg); overflow: hidden; }
.sk-img { padding-bottom: 100%; background: linear-gradient(90deg,#eee 25%,#f5f5f5 50%,#eee 75%); background-size:200% 100%; animation: shim 1.5s infinite; }
.sk-l { height: 14px; margin: 12px 14px; background: #eee; border-radius: 4px; }
@keyframes shim { 0%{background-position:200% 0}100%{background-position:-200% 0} }
.empty { text-align: center; padding: 80px 0; color: var(--text-muted); font-size: 16px; }
.empty a { color: var(--primary); margin-left: 6px; }
</style>
