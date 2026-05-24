<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/home" class="logo">
        <span class="logo-dot"></span>
        NJUST SHOP
      </router-link>

      <div class="search-box" v-if="currentUser">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        <input v-model="kw" placeholder="搜索商品..." @keyup.enter="doSearch" />
      </div>

      <nav class="nav">
        <router-link to="/home" class="nav-item">首页</router-link>
        <router-link to="/publish" class="nav-item" v-if="currentUser && (currentUser.role==='merchant'||currentUser.role==='seller'||currentUser.role==='admin')">发布</router-link>
        <template v-if="!currentUser">
          <router-link to="/auth" class="btn-outline">登录</router-link>
          <router-link to="/auth" class="btn-primary">注册</router-link>
        </template>
        <div v-else class="user-area" ref="avatarRef">
          <div class="user-trigger" @click="open=!open">
            <Avatar :username="currentUser.username" size="md" />
            <span>{{ currentUser.username }}</span>
            <span class="arrow">▾</span>
          </div>
          <div v-if="open" class="drop">
            <router-link to="/profile" class="drop-item" @click="open=false">个人中心</router-link>
            <router-link to="/order" class="drop-item" @click="open=false">我的订单</router-link>
            <router-link to="/collect" class="drop-item" @click="open=false">我的收藏</router-link>
            <router-link v-if="currentUser.role==='admin'" to="/admin" class="drop-item" @click="open=false">管理后台</router-link>
            <div class="drop-split"></div>
            <div class="drop-item danger" @click="logout">退出登录</div>
          </div>
        </div>
      </nav>
    </div>
  </header>

  <div :style="{ height: '60px' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import service from '@/api/user';
import Avatar from '@/components/Avatar.vue';

const router = useRouter();
const kw = ref('');
const open = ref(false);
const currentUser = ref(null);
const avatarRef = ref(null);

const init = () => {
  try {
    const s = localStorage.getItem('userInfo');
    if (s) { const u = JSON.parse(s); if (u?.username) currentUser.value = u; }
  } catch(e) {}
};

const doSearch = () => {
  const q = kw.value.trim();
  if (q) { router.push(`/search?q=${encodeURIComponent(q)}`); kw.value = ''; }
};

const logout = async () => {
  if (!confirm('确定退出？')) return;
  try { await service.post('/user/logout'); } catch(e) {}
  localStorage.clear();
  currentUser.value = null;
  open.value = false;
  router.push('/auth');
};

const clickOut = (e) => { if (avatarRef.value && !avatarRef.value.contains(e.target)) open.value = false; };

onMounted(() => { init(); document.addEventListener('click', clickOut); });
onUnmounted(() => document.removeEventListener('click', clickOut));
</script>

<style scoped>
.header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
  height: 60px; background: #fff; border-bottom: 1px solid var(--border);
  display: flex; align-items: center;
}
.header-inner {
  width: 100%; max-width: var(--max-width); margin: 0 auto;
  padding: 0 24px; display: flex; align-items: center; gap: 24px;
}
.logo {
  font-size: 20px; font-weight: 800; color: var(--primary);
  display: flex; align-items: center; gap: 8px; user-select: none; flex-shrink: 0;
}
.logo-dot {
  width: 10px; height: 10px; border-radius: 50%; background: var(--primary);
}

.search-box {
  flex: 1; max-width: 480px; display: flex; align-items: center;
  background: var(--bg); border: 1px solid var(--border); border-radius: 24px;
  padding: 0 16px; transition: border-color .2s;
}
.search-box:focus-within { border-color: var(--primary); }
.search-icon { width: 18px; height: 18px; color: var(--text-muted); flex-shrink: 0; }
.search-box input {
  flex: 1; padding: 10px 12px; border: none; background: transparent;
  font-size: 14px; color: var(--text);
}
.search-box input::placeholder { color: var(--text-muted); }

.nav { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.nav-item {
  padding: 8px 14px; border-radius: 6px; font-size: 14px; color: var(--text-light);
  transition: all .15s;
}
.nav-item:hover, .nav-item.router-link-active { color: var(--primary); background: rgba(108,92,231,0.06); }

.btn-outline {
  padding: 7px 18px; border-radius: 6px; font-size: 14px; color: var(--text-light);
  border: 1px solid var(--border); transition: all .15s;
}
.btn-outline:hover { border-color: var(--primary); color: var(--primary); }
.btn-primary {
  padding: 7px 18px; border-radius: 6px; font-size: 14px; color: #fff;
  background: var(--primary); transition: all .15s;
}
.btn-primary:hover { background: var(--primary-dark); }

.user-area { position: relative; }
.user-trigger {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 12px 4px 4px; border-radius: 24px; transition: background .15s;
}
.user-trigger:hover { background: var(--bg); }
.arrow { font-size: 10px; color: var(--text-muted); }

.drop {
  position: absolute; top: 48px; right: 0; width: 170px;
  background: #fff; border-radius: var(--radius-lg); box-shadow: var(--shadow-hover);
  border: 1px solid var(--border); padding: 6px; z-index: 100;
}
.drop-item {
  display: block; padding: 10px 14px; border-radius: 6px; font-size: 14px;
  color: var(--text); transition: background .15s;
}
.drop-item:hover { background: var(--bg); }
.drop-item.danger { color: var(--danger); }
.drop-item.danger:hover { background: #fef2f2; }
.drop-split { height: 1px; background: var(--border); margin: 4px 0; }
</style>
