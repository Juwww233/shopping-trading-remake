<template>
  <header class="global-header">
    <div class="header-container">
      <!-- 左侧 Logo -->
      <div class="logo-area" @click="$router.push('/home')">
        <span class="logo-text">NJUST SHOP</span>
      </div>

      <!-- 中间导航 -->
      <nav class="main-nav">
        <router-link to="/home" class="nav-item">首页</router-link>
        <router-link to="/goods" class="nav-item">全部商品</router-link>
        <router-link to="/publish" class="nav-item" v-if="currentUser && currentUser.role === 'merchant'">
          发布商品
        </router-link>
      </nav>

      <!-- 右侧用户区域 -->
      <div class="user-area">
        <!-- 未登录状态 -->
        <div v-if="!currentUser" class="guest-actions">
          <router-link to="/auth" class="btn-text">登录</router-link>
          <router-link to="/auth" class="btn-primary">注册</router-link>
        </div>

        <!-- 已登录状态 -->
        <div v-else class="logged-in-actions">
          <span class="welcome-msg">Hi, {{ currentUser.username }}</span>

          <div class="avatar-wrapper" @click="toggleDropdown" ref="avatarRef">
            <img
                :src="currentUser.avatar || '/images/default-avatar.png'"
                alt="Avatar"
                class="user-avatar"
                @error="(e) => e.target.src = '/images/default-avatar.png'"
            >
            <span class="dropdown-arrow">▼</span>

            <transition name="slide-fade">
              <div v-if="isDropdownOpen" class="dropdown-menu">
                <div class="menu-item" @click="goToProfile">
                  <span class="icon">👤</span> 个人中心
                </div>
                <div class="menu-divider"></div>
                <div class="menu-item logout" @click="handleLogout">
                  <span class="icon">🚪</span> 退出登录
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import service from '@/api/user';

const router = useRouter();
const currentUser = ref(null);
const isDropdownOpen = ref(false);
const avatarRef = ref(null);

// 【优化】增加 try-catch 防止 JSON 解析错误导致页面卡死
const initUser = () => {
  try {
    const userStr = localStorage.getItem('userInfo');
    if (userStr) {
      const parsed = JSON.parse(userStr);
      if (parsed && parsed.username) {
        currentUser.value = parsed;
        // 确保 userId 也同步存储
        if (parsed.id && !localStorage.getItem('userId')) {
          localStorage.setItem('userId', parsed.id);
        }
      } else {
        localStorage.removeItem('userInfo');
      }
    }
  } catch (e) {
    console.error('用户信息解析失败，清除缓存', e);
    localStorage.removeItem('userInfo');
    localStorage.removeItem('sessionId');
    localStorage.removeItem('userId');
  }
};

const goToProfile = () => {
  const uid = localStorage.getItem('userId');

  if (!uid) {
    console.warn('⚠️ 未找到 userId，跳转登录页');
    router.push('/auth');
    return;
  }

  console.log('✅ 跳转个人中心，userId:', uid);
  isDropdownOpen.value = false;
  router.push('/profile');  // ✅ 现在可以不带参数了
}

const toggleDropdown = (e) => {
  e.stopPropagation();
  isDropdownOpen.value = !isDropdownOpen.value;
};

const handleClickOutside = (event) => {
  if (avatarRef.value && !avatarRef.value.contains(event.target)) {
    isDropdownOpen.value = false;
  }
};

const handleLogout = async () => {
  if (!confirm('确定要退出登录吗？')) return;

  try {
    await service.post('/user/logout');
  } catch (error) {
    console.warn('退出接口调用失败，执行本地清理', error);
  } finally {
    localStorage.removeItem('userInfo');
    localStorage.removeItem('sessionId');
    currentUser.value = null;
    isDropdownOpen.value = false;

    // 强制跳转，避免路由残留
    if (router.currentRoute.value.path !== '/auth') {
      router.push('/auth');
    } else {
      // 如果已经在 auth 页，刷新一下确保状态重置
      window.location.reload();
    }
  }
};

onMounted(() => {
  initUser();
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* ... (保持之前的 style 代码不变) ... */
.global-header {
  height: 70px;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
}
.header-container {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo-area { cursor: pointer; display: flex; align-items: center; }
.logo-text {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, #7f00c2, #bd3fff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}
.main-nav { display: flex; gap: 24px; }
.nav-item {
  text-decoration: none;
  color: #4e5969;
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s;
  position: relative;
  padding: 22px 0;
}
.nav-item:hover, .nav-item.router-link-active { color: #7f00c2; }
.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0;
  width: 100%; height: 3px;
  background-color: #7f00c2;
  border-radius: 2px;
}
.user-area { display: flex; align-items: center; }
.guest-actions { display: flex; gap: 16px; }
.btn-text {
  text-decoration: none;
  color: #4e5969;
  font-size: 14px;
  padding: 6px 12px;
}
.btn-text:hover { color: #7f00c2; }
.btn-primary {
  text-decoration: none;
  background: linear-gradient(135deg, #7f00c2, #9d4edd);
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(127, 0, 194, 0.3);
}
.logged-in-actions { display: flex; align-items: center; gap: 12px; }
.welcome-msg { font-size: 14px; color: #4e5969; margin-right: 8px; }
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 4px 8px 4px 4px;
  border-radius: 30px;
  transition: background 0.2s;
}
.avatar-wrapper:hover { background-color: #f5f7fa; }
.user-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e5e6eb;
}
.dropdown-arrow {
  font-size: 10px;
  color: #86909c;
  margin-left: 6px;
  transition: transform 0.2s;
}
.avatar-wrapper:hover .dropdown-arrow { transform: rotate(180deg); }
.dropdown-menu {
  position: absolute;
  top: 50px; right: 0;
  width: 160px;
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 8px;
  z-index: 1001;
}
.menu-item {
  padding: 10px 16px;
  font-size: 14px;
  color: #1d2129;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.menu-item:hover { background-color: #f5f7fa; color: #7f00c2; }
.menu-item.logout { color: #f53f3f; }
.menu-item.logout:hover { background-color: #fff1f0; }
.menu-divider { height: 1px; background-color: #f0f0f0; margin: 6px 0; }
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.2s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translateY(-10px); }
</style>