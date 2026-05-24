<template>
  <div :class="['avatar-wrapper', sizeClass]" :style="avatarStyle">
    {{ initial }}
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  username: { type: String, default: 'U' },
  size: { type: String, default: 'md' }, // sm, md, lg
  bgColor: { type: String, default: '' }
});

const initial = computed(() => {
  if (!props.username) return 'U';
  return props.username.charAt(0).toUpperCase();
});

const sizeClass = computed(() => `avatar-${props.size}`);

const colors = [
  '#6c5ce7', '#00b894', '#e17055', '#0984e3', '#fdcb6e',
  '#e84393', '#00cec9', '#d63031', '#a29bfe', '#55a3e8'
];

const bgColor = computed(() => {
  if (props.bgColor) return props.bgColor;
  const idx = props.username.charCodeAt(0) % colors.length;
  return colors[idx];
});

const avatarStyle = computed(() => ({
  backgroundColor: bgColor.value
}));
</script>

<style scoped>
.avatar-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  font-weight: 700;
  user-select: none;
  flex-shrink: 0;
}
.avatar-sm { width: 28px; height: 28px; font-size: 13px; }
.avatar-md { width: 32px; height: 32px; font-size: 14px; }
.avatar-lg { width: 48px; height: 48px; font-size: 20px; }
.avatar-xl { width: 100px; height: 100px; font-size: 36px; }
</style>
