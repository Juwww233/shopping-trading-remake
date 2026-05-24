<template>
  <div class="cat-page">
    <div class="top">
      <button class="back" @click="$router.push('/home')">← 返回</button>
      <h2>{{ name }}</h2>
      <span class="count">{{ list.length }} 件</span>
    </div>
    <div v-if="loading" class="grid">
      <div v-for="i in 6" :key="i" class="sk"><div class="sk-img"/><div class="sk-l"/></div>
    </div>
    <div v-else-if="!list.length" class="empty">📦 暂无商品</div>
    <div v-else class="grid">
      <div v-for="g in list" :key="g.id" class="card" @click="$router.push(`/good/${g.id}`)">
        <div class="img"><img :src="g.img" :alt="g.name" @error="e=>e.target.src='/default.png'"/><span class="price">¥{{ g.price }}</span></div>
        <div class="info"><h4>{{ g.name }}</h4><span>{{ g.address }}</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getGoodsByCategory } from '@/api/goods';
const route = useRoute();
const name = ref(route.params.type);
const list = ref([]);
const loading = ref(true);
const fetch = async () => {
  loading.value = true;
  try { const r = await getGoodsByCategory(route.params.type); list.value = r.code===200?r.data:[]; } catch(e) {}
  finally { loading.value = false; }
};
onMounted(fetch);
watch(()=>route.params.type, ()=>{ name.value=route.params.type; fetch(); });
</script>

<style scoped>
.cat-page { max-width: var(--max-width); margin: 0 auto; padding: 24px; }
.top { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.back { background: none; font-size: 14px; color: var(--text-light); cursor: pointer; }
.back:hover { color: var(--primary); }
.top h2 { font-size: 24px; font-weight: 700; }
.count { font-size: 14px; color: var(--text-muted); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card { background: #fff; border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow); transition: all .25s; }
.card:hover { transform: translateY(-4px); box-shadow: var(--shadow-hover); }
.img { position: relative; width: 100%; padding-bottom: 100%; background: #f0f0f0; overflow: hidden; }
.img img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform .4s; }
.card:hover .img img { transform: scale(1.06); }
.price { position: absolute; bottom: 8px; left: 8px; background: rgba(0,0,0,0.7); color: #fff; padding: 3px 10px; border-radius: 4px; font-size: 14px; font-weight: 700; }
.info { padding: 12px 14px; }
.info h4 { font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; }
.info span { font-size: 12px; color: var(--text-muted); }
.sk { background: #fff; border-radius: var(--radius-lg); overflow: hidden; }
.sk-img { padding-bottom: 100%; background: linear-gradient(90deg,#eee 25%,#f5f5f5 50%,#eee 75%); background-size:200% 100%; animation: shim 1.5s infinite; }
.sk-l { height: 14px; margin: 12px 14px; background: #eee; border-radius: 4px; }
@keyframes shim { 0%{background-position:200% 0}100%{background-position:-200% 0} }
.empty { text-align: center; padding: 80px 0; color: var(--text-muted); }
</style>
