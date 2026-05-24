<template>
  <div class="home">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-text">
          <h1>发现你的<span>心仪好物</span></h1>
          <p>品质好货 · 超值二手 · 尽在 NJUST SHOP</p>
        </div>
        <div class="hero-stats">
          <div class="stat"><strong>1000+</strong><span>精选商品</span></div>
          <div class="stat"><strong>500+</strong><span>满意用户</span></div>
          <div class="stat"><strong>24h</strong><span>快速发货</span></div>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <div class="main-area">
      <aside class="sidebar">
        <div class="sidebar-title">全部分类</div>
        <div
          v-for="item in categories"
          :key="item.value"
          :class="['cat', { on: cur === item.value }]"
          @click="switchCat(item.value)"
        >
          <span class="cat-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
          <span class="cat-arrow">›</span>
        </div>

        <!-- 公告区域 -->
        <div v-if="notices.length" class="notice-box">
          <div class="notice-title">📢 最新公告</div>
          <div class="notice-list">
            <div v-for="n in displayNotices" :key="n.id" class="notice-item">
              <span class="notice-dot"></span>
              <span class="notice-text">{{ n.title }}</span>
            </div>
          </div>
          <button v-if="notices.length > 2" class="notice-more" @click="showAllNotices = !showAllNotices">
            {{ showAllNotices ? '收起' : '更多' }}
          </button>
        </div>
      </aside>

      <div class="content">
        <!-- 首页：猜你喜欢 + 二手好物 -->
        <template v-if="cur === 'home'">
          <section class="sec">
            <div class="sec-head"><h2>猜你喜欢</h2><span>为你精选好物</span></div>
            <div v-if="loading" class="grid skel">
              <div v-for="i in 6" :key="i" class="sk-card"><div class="sk-img"/><div class="sk-l1"/><div class="sk-l2"/></div>
            </div>
            <div v-else class="grid">
              <div v-for="g in guessList" :key="g.id" class="card" @click="$router.push(`/good/${g.id}`)">
                <div class="card-img">
                  <img :src="g.img" :alt="g.name" @error="e=>e.target.src='/default.png'" />
                  <div class="card-price">¥{{ g.price }}</div>
                </div>
                <div class="card-info">
                  <h4>{{ g.name }}</h4>
                  <span class="card-addr">{{ g.address || '全国' }}</span>
                </div>
              </div>
            </div>
          </section>

          <section class="sec">
            <div class="sec-head"><h2>二手好物</h2><span>品质闲置</span></div>
            <div v-if="loading" class="grid skel">
              <div v-for="i in 6" :key="i" class="sk-card"><div class="sk-img"/><div class="sk-l1"/><div class="sk-l2"/></div>
            </div>
            <div v-else class="grid">
              <div v-for="g in secondList" :key="g.id" class="card" @click="$router.push(`/good/${g.id}`)">
                <div class="card-img">
                  <img :src="g.img" :alt="g.name" @error="e=>e.target.src='/default.png'" />
                  <div class="card-price">¥{{ g.price }}</div>
                </div>
                <div class="card-info">
                  <h4>{{ g.name }}</h4>
                  <span class="card-addr">{{ g.address || '全国' }}</span>
                </div>
              </div>
            </div>
          </section>
        </template>

        <!-- 分类商品 -->
        <template v-else>
          <section class="sec">
            <div class="sec-head"><h2>{{ curLabel }}</h2></div>
            <div v-if="catLoading" class="grid skel">
              <div v-for="i in 6" :key="i" class="sk-card"><div class="sk-img"/><div class="sk-l1"/><div class="sk-l2"/></div>
            </div>
            <div v-else-if="catList.length === 0" class="empty">
              <span class="empty-icon">📦</span><p>暂无商品</p>
            </div>
            <div v-else class="grid">
              <div v-for="g in catList" :key="g.id" class="card" @click="$router.push(`/good/${g.id}`)">
                <div class="card-img">
                  <img :src="g.img" :alt="g.name" @error="e=>e.target.src='/default.png'" />
                  <div class="card-price">¥{{ g.price }}</div>
                </div>
                <div class="card-info">
                  <h4>{{ g.name }}</h4>
                  <span class="card-addr">{{ g.address || '全国' }}</span>
                </div>
              </div>
            </div>
          </section>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getGuessYouLikeGoods, getSecondHandGoods, getGoodsByCategory } from '@/api/goods';
import { getAllCategories } from '@/api/category';
import { getNoticeList } from '@/api/notice';

const router = useRouter();
const icons = ['💻','🍔','👔','🏠','🔄','📱','🎮','📚','🎵','⚽','💄','🚗'];
const categories = ref([{ label:'首页', value:'home', icon:'🏠' }]);
const cur = ref('home');
const guessList = ref([]);
const secondList = ref([]);
const catList = ref([]);
const loading = ref(true);
const catLoading = ref(false);
const notices = ref([]);
const showAllNotices = ref(false);

const displayNotices = computed(() => {
  if (showAllNotices.value) return notices.value;
  return notices.value.slice(0, 2);
});

const curLabel = ref('');

const switchCat = (v) => {
  cur.value = v;
  const c = categories.value.find(x => x.value === v);
  curLabel.value = c ? c.label : v;
};

const fetchCat = async (cat) => {
  if (cat === 'home') return;
  catLoading.value = true;
  try {
    const r = await getGoodsByCategory(cat);
    catList.value = r.code === 200 ? r.data : [];
  } catch(e) { catList.value = []; }
  finally { catLoading.value = false; }
};

onMounted(async () => {
  try {
    const [g, s, cs, ns] = await Promise.all([
      getGuessYouLikeGoods(), getSecondHandGoods(), getAllCategories(), getNoticeList()
    ]);
    if (g.code === 200) guessList.value = g.data;
    if (s.code === 200) secondList.value = s.data;
    if (cs.code === 200) cs.data.forEach((c,i) => categories.value.push({ label:c.name, value:c.name, icon:icons[i%icons.length] }));
    if (ns.code === 200) notices.value = ns.data;
  } catch(e) { console.error(e); }
  finally { loading.value = false; }
});

watch(cur, fetchCat, { immediate: true });
</script>

<style scoped>
/* Hero */
.hero {
  background: linear-gradient(135deg, #2d1b69 0%, #6c5ce7 60%, #a29bfe 100%);
  color: #fff; position: relative; overflow: hidden;
}
.hero::after {
  content: ''; position: absolute; right: -80px; top: -60px;
  width: 400px; height: 400px; border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.hero-inner {
  max-width: var(--max-width); margin: 0 auto; padding: 40px 24px;
  display: flex; justify-content: space-between; align-items: center;
  position: relative; z-index: 1;
}
.hero-text h1 { font-size: 36px; font-weight: 800; }
.hero-text h1 span { color: #fd79a8; }
.hero-text p { font-size: 16px; opacity: 0.8; margin-top: 8px; }
.hero-stats { display: flex; gap: 32px; }
.stat { text-align: center; }
.stat strong { display: block; font-size: 28px; font-weight: 700; }
.stat span { font-size: 13px; opacity: 0.7; }

/* Main area */
.main-area {
  max-width: var(--max-width); margin: 0 auto; padding: 20px 24px;
  display: flex; gap: 24px; align-items: flex-start;
}

/* Sidebar */
.sidebar {
  width: 200px; flex-shrink: 0; background: #fff; border-radius: var(--radius-lg);
  box-shadow: var(--shadow); position: sticky; top: 80px; overflow: hidden;
}
.sidebar-title {
  padding: 14px 18px; font-weight: 700; font-size: 15px; color: var(--text);
  border-bottom: 1px solid var(--border);
}
.cat {
  display: flex; align-items: center; gap: 10px; padding: 12px 18px;
  cursor: pointer; font-size: 14px; color: var(--text-light); transition: all .15s;
}
.cat:hover { background: var(--bg); color: var(--text); }
.cat.on { background: rgba(108,92,231,0.08); color: var(--primary); font-weight: 600; }
.cat-icon { font-size: 16px; flex-shrink: 0; }
.cat-arrow { margin-left: auto; color: var(--text-muted); font-size: 16px; }
.cat.on .cat-arrow { color: var(--primary); }

/* 公告框 */
.notice-box {
  margin: 12px 12px 0;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
  border: 1px solid #ffe082;
  border-radius: var(--radius);
  padding: 12px;
}
.notice-title {
  font-size: 13px;
  font-weight: 700;
  color: #f57f17;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #ffe082;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  color: #6d5d17;
  line-height: 1.4;
}
.notice-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f57f17;
  flex-shrink: 0;
  margin-top: 4px;
}
.notice-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.notice-more {
  margin-top: 8px;
  width: 100%;
  padding: 4px 0;
  background: none;
  border: 1px dashed #ffe082;
  border-radius: 4px;
  font-size: 12px;
  color: #f57f17;
  cursor: pointer;
  text-align: center;
}
.notice-more:hover {
  background: rgba(245, 127, 23, 0.1);
}

/* Content */
.content { flex: 1; min-width: 0; }

/* Section */
.sec { margin-bottom: 32px; }
.sec-head {
  display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px;
}
.sec-head h2 { font-size: 22px; font-weight: 700; }
.sec-head span { font-size: 14px; color: var(--text-muted); }

/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.card {
  background: #fff; border-radius: var(--radius-lg); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow); transition: all .25s;
}
.card:hover { transform: translateY(-4px); box-shadow: var(--shadow-hover); }
.card-img {
  position: relative; width: 100%; height: 0; padding-bottom: 100%;
  background: #f0f0f0; overflow: hidden;
}
.card-img img {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  object-fit: cover; transition: transform .4s;
}
.card:hover .card-img img { transform: scale(1.06); }
.card-price {
  position: absolute; bottom: 8px; left: 8px;
  background: rgba(0,0,0,0.7); color: #fff; padding: 3px 10px;
  border-radius: 4px; font-size: 14px; font-weight: 700;
}
.card-info { padding: 12px 14px; }
.card-info h4 {
  font-size: 14px; font-weight: 600; color: var(--text);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px;
}
.card-addr { font-size: 12px; color: var(--text-muted); }

/* Skeleton */
.sk-card { background: #fff; border-radius: var(--radius-lg); overflow: hidden; }
.sk-img { width: 100%; padding-bottom: 100%; background: linear-gradient(90deg,#eee 25%,#f5f5f5 50%,#eee 75%); background-size:200% 100%; animation: shim 1.5s infinite; }
.sk-l1 { height: 16px; margin: 12px 14px 8px; background: #eee; border-radius: 4px; }
.sk-l2 { height: 14px; margin: 0 14px 12px; width: 50%; background: #eee; border-radius: 4px; }
@keyframes shim { 0%{background-position:200% 0}100%{background-position:-200% 0} }

.empty { text-align: center; padding: 80px 0; color: var(--text-muted); }
.empty-icon { font-size: 48px; }

@media (max-width: 768px) {
  .hero-inner { flex-direction: column; text-align: center; gap: 20px; }
  .hero-text h1 { font-size: 24px; }
  .main-area { flex-direction: column; }
  .sidebar { width: 100%; position: static; }
  .sidebar .cat { display: none; }
  .sidebar .cat.on, .sidebar .cat:first-of-type { display: flex; }
  .grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
}
</style>
