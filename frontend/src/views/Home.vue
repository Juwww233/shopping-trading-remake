<template>
  <div class="home-container">
    <!-- 左侧分类栏（固定） -->
    <aside class="sidebar">
      <div class="sidebar-header">商品分类</div>
      <nav class="category-nav">
        <div
            class="category-item"
            v-for="item in categoryOptions"
            :key="item.value"
            :class="{ active: currentCategory === item.value }"
            @click="handleCategoryChange(item.value)"
        >
          {{ item.label }}
        </div>
      </nav>
    </aside>

    <!-- 右侧内容区域 -->
    <main class="main-content">
      <!-- 1. 首页初始展示：猜你喜欢 + 二手专区 -->
      <div v-if="currentCategory === 'home'" class="home-sections">
        <!-- 猜你喜欢区域 -->
        <section class="goods-section">
          <h2 class="section-title">猜你喜欢</h2>
          <div class="goods-grid">
            <div
                class="goods-card"
                v-for="goods in guessYouLikeList"
                :key="goods.id"
                @click="goToGoodDetail(goods.id)"
            >
              <img
                  :src="goods.img || '/images/default-goods.png'"
                  alt="商品图片"
                  class="goods-img"
                  @error="(e) => e.target.src = '/images/default-goods.png'"
              >
              <div class="goods-info">
                <h3 class="goods-name">{{ goods.name }}</h3>
                <p class="goods-price">¥{{ goods.price.toFixed(2) }}</p>
                <p class="goods-address" v-if="goods.address">📍{{ goods.address }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 二手专区区域 -->
        <section class="goods-section">
          <h2 class="section-title">二手好物专区</h2>
          <div class="goods-grid">
            <div
                class="goods-card"
                v-for="goods in secondHandList"
                :key="goods.id"
                @click="goToGoodDetail(goods.id)"
            >
              <img
                  :src="goods.img || '/images/default-goods.png'"
                  alt="商品图片"
                  class="goods-img"
                  @error="(e) => e.target.src = '/images/default-goods.png'"
              >
              <div class="goods-info">
                <h3 class="goods-name">{{ goods.name }}</h3>
                <p class="goods-price">¥{{ goods.price.toFixed(2) }}</p>
                <p class="goods-status" v-if="goods.content">
                  {{ goods.content.match(/\d+成新/)?.[0] || '二手商品' }}
                </p>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 2. 分类商品展示 -->
      <div v-else class="category-goods">
        <section class="goods-section">
          <h2 class="section-title">{{ getCategoryLabel(currentCategory) }}</h2>
          <div v-if="categoryGoodsList.length > 0" class="goods-grid">
            <div
                class="goods-card"
                v-for="goods in categoryGoodsList"
                :key="goods.id"
                @click="goToGoodDetail(goods.id)"
            >
              <img
                  :src="goods.img || '/images/default-goods.png'"
                  alt="商品图片"
                  class="goods-img"
                  @error="(e) => e.target.src = '/images/default-goods.png'"
              >
              <div class="goods-info">
                <h3 class="goods-name">{{ goods.name }}</h3>
                <p class="goods-price">¥{{ goods.price.toFixed(2) }}</p>
                <p class="goods-address" v-if="goods.address">📍{{ goods.address }}</p>
                <p class="goods-status" v-if="currentCategory === '二手物品' && goods.content">
                  {{ goods.content.match(/\d+成新/)?.[0] || '成色未知' }}
                </p>
              </div>
            </div>
          </div>
          <div v-else class="empty-tip">
            暂无{{ getCategoryLabel(currentCategory) }}类商品~
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  getGuessYouLikeGoods,
  getSecondHandGoods,
  getGoodsByCategory
} from '@/api/goods';

const router = useRouter();

const categoryOptions = ref([
  { label: '首页', value: 'home' },
  { label: '电子产品', value: '电子产品' },
  { label: '美食', value: '美食' },
  { label: '服装', value: '服装' },
  { label: '生活', value: '生活' },
  { label: '二手物品', value: '二手物品' }
]);

const currentCategory = ref('home');
const guessYouLikeList = ref([]);
const secondHandList = ref([]);
const categoryGoodsList = ref([]);

const goToGoodDetail = (id) => {
  if (!id) {
    console.warn('商品ID不存在，无法跳转');
    return;
  }
  router.push(`/good/${id}`);
};

const handleCategoryChange = (categoryValue) => {
  currentCategory.value = categoryValue;
};

const getCategoryLabel = (value) => {
  const target = categoryOptions.value.find(item => item.value === value);
  return target ? target.label : '未知分类';
};

const fetchCategoryGoods = async (category) => {
  if (category === 'home') return;
  try {
    const res = await getGoodsByCategory(category);
    if (res.code === 200) {
      categoryGoodsList.value = res.data;
    } else {
      categoryGoodsList.value = [];
    }
  } catch (error) {
    categoryGoodsList.value = [];
    console.error(`获取${getCategoryLabel(category)}商品失败：`, error);
  }
};

const initHomeData = async () => {
  try {
    const [guessRes, secondRes] = await Promise.all([
      getGuessYouLikeGoods(),
      getSecondHandGoods()
    ]);
    if (guessRes.code === 200) guessYouLikeList.value = guessRes.data;
    if (secondRes.code === 200) secondHandList.value = secondRes.data;
  } catch (error) {
    console.error('初始化首页数据失败：', error);
  }
};

onMounted(() => {
  initHomeData();
});

watch(
    () => currentCategory.value,
    (newCategory) => {
      fetchCategoryGoods(newCategory);
    },
    { immediate: true }
);
</script>

<style scoped>
/* 全局布局 */
.home-container {
  display: flex;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  /* 关键修改：减去 Header 的高度 (70px)，防止内容被遮挡或出现双滚动条 */
  min-height: calc(100vh - 70px);
}

/* 左侧分类栏 */
.sidebar {
  width: 220px;
  background-color: #f5f7fa;
  border-right: 1px solid #e5e6eb;
  position: sticky;
  top: 0;
  /* 关键修改：高度自适应或设为视口高度减 header */
  height: calc(100vh - 70px);
  padding: 20px 0;
  overflow-y: auto;
}

.sidebar-header {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  padding: 0 20px 15px;
  border-bottom: 1px solid #e5e6eb;
  margin-bottom: 10px;
}

.category-nav {
  padding: 10px 0;
}

.category-item {
  padding: 12px 20px;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.category-item:hover {
  background-color: #e8f3ff;
  color: #1890ff;
}

.category-item.active {
  background-color: #1890ff;
  color: #fff;
}

/* 右侧主内容区 */
.main-content {
  flex: 1;
  padding: 24px;
  background-color: #fff;
}

.goods-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 2px solid #1890ff;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.goods-card {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #fff;
}

.goods-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
  border-color: #1890ff;
}

.goods-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  pointer-events: none;
}

.goods-info {
  padding: 12px 16px;
}

.goods-name {
  font-size: 14px;
  color: #1d2129;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 40px;
}

.goods-price {
  font-size: 16px;
  color: #f53f3f;
  font-weight: 600;
  margin-bottom: 4px;
}

.goods-address, .goods-status {
  font-size: 12px;
  color: #86909c;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #86909c;
  font-size: 16px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
  }

  .category-nav {
    display: flex;
    flex-wrap: wrap;
  }

  .category-item {
    padding: 8px 12px;
    flex: 1;
    text-align: center;
  }

  .goods-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
}
</style>