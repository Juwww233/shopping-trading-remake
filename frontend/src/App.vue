<template>
  <div id="app">
    <!-- 修改点：只要路径不是 /auth，就显示 Header -->
    <!-- 这样无论是登录还是注册（都在 /auth 下），都不会显示 Header(后续需要修改逻辑) -->
    <Header v-if="$route.path !== '/auth'" />

    <router-view />
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue';
</script>

<style>
/* 全局样式保持不变 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #f8f8f8;
  color: #1d2129;
  overflow-x: hidden;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>