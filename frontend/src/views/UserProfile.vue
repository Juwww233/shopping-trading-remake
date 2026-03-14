<template>
  <div class="user-profile-container">
    <!-- 顶部标题 + 返回键 -->
    <div class="page-header">
      <div class="back-btn" @click="goBack" title="返回上一页">← 返回</div>
      <div class="header-title">
        <h2>个人中心</h2>
        <p class="subtitle">管理您的个人信息与账户安全</p>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 基本信息卡片 -->
      <div class="card profile-card">
        <div class="card-header"><h3>基本资料</h3></div>
        <div class="card-body">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerAvatarUpload" title="点击更换头像">

              <!-- 状态 1: 加载中 -->
              <div v-if="avatarLoading" class="avatar-placeholder loading-placeholder">
                <span>⏳</span>
              </div>

              <!-- 状态 2: 有头像 URL (显示图片) -->
              <img
                  v-show="!avatarLoading && userInfo.avatar"
                  :key="userInfo.avatar"
                  :src="userInfo.avatar"
                  alt="头像"
                  class="avatar-image"
                  @load="avatarLoading = false"
                  @error="handleAvatarError"
              >

              <!-- 状态 3: 无头像 (显示色块) -->
              <div
                  v-show="!avatarLoading && !userInfo.avatar"
                  class="avatar-placeholder"
                  :style="{ backgroundColor: avatarColor }"
              >
                {{ (userInfo.username || username || 'U').charAt(0).toUpperCase() }}
              </div>

              <div class="avatar-overlay"><span>📷 更换</span></div>
            </div>

            <input type="file" ref="avatarInput" style="display:none" accept="image/*" @change="handleAvatarUpload">
            <div class="avatar-tips">点击头像可上传新图片</div>
          </div>

          <form class="info-form" @submit.prevent="saveProfile">
            <div class="form-row">
              <div class="form-item">
                <label>用户名</label>
                <input type="text" :value="username" disabled class="disabled-input">
                <span class="input-tip">用户名不可修改</span>
              </div>
              <div class="form-item">
                <label>用户身份</label>
                <input type="text" :value="userInfo.role === 'seller' ? '商家' : '普通用户'" disabled class="disabled-input">
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>真实姓名 <span class="required">*</span></label>
                <input v-model="form.name" type="text" placeholder="请输入真实姓名" :disabled="!editing" class="std-input">
              </div>
              <div class="form-item">
                <label>联系电话 <span class="required">*</span></label>
                <input v-model="form.phone" type="tel" placeholder="请输入手机号码" :disabled="!editing" class="std-input">
              </div>
            </div>
            <div class="action-bar">
              <button v-if="!editing" type="button" class="btn btn-primary" @click="enterEditMode">✏️ 编辑资料</button>
              <template v-else>
                <button type="button" class="btn btn-secondary" @click="cancelEdit">取消</button>
                <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving ? '保存中...' : '💾 保存修改' }}</button>
              </template>
            </div>
          </form>
        </div>
      </div>

      <!-- 账户安全卡片 -->
      <div class="card security-card">
        <div class="card-header">
          <h3>🔒 账户安全</h3>
          <div class="security-level">
            <span class="level-label">安全等级:</span>
            <span class="level-badge high" v-if="userInfo.phone && userInfo.avatar">高</span>
            <span class="level-badge mid" v-else-if="userInfo.phone || userInfo.avatar">中</span>
            <span class="level-badge low" v-else>低</span>
          </div>
        </div>
        <div class="card-body">
          <div class="password-form-wrapper">
            <div class="form-item">
              <label>原密码 <span class="required">*</span></label>
              <div class="password-input-group">
                <input v-model="passwordForm.oldPassword" :type="showOldPwd ? 'text' : 'password'" placeholder="请输入当前密码" class="std-input">
                <span class="toggle-icon" @click="showOldPwd = !showOldPwd">{{ showOldPwd ? '🙈' : '👁️' }}</span>
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>新密码 <span class="required">*</span></label>
                <div class="password-input-group">
                  <input v-model="passwordForm.newPassword" :type="showNewPwd ? 'text' : 'password'" placeholder="6-20位字母或数字" class="std-input">
                  <span class="toggle-icon" @click="showNewPwd = !showNewPwd">{{ showNewPwd ? '🙈' : '👁️' }}</span>
                </div>
              </div>
              <div class="form-item">
                <label>确认新密码 <span class="required">*</span></label>
                <input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" class="std-input" :class="{ 'error-border': passwordMismatch }">
                <span v-if="passwordMismatch" class="error-msg">两次密码不一致</span>
              </div>
            </div>
            <div class="action-bar">
              <button type="button" class="btn btn-secondary" @click="resetPasswordForm">重置</button>
              <button type="button" class="btn btn-danger" @click="handleChangePassword" :disabled="changingPassword">{{ changingPassword ? '修改中...' : '🔑 确认修改密码' }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <transition name="toast-fade">
      <div v-if="toast.visible" :class="['toast', toast.type]">
        <span class="toast-icon">{{ toast.icon }}</span>
        <span class="toast-message">{{ toast.message }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getCurrentUser, updateUserInfo, uploadAvatar } from '@/api/user';
import api from '@/api/index';

const router = useRouter();

// ========== 配置 ==========
// 从环境变量获取后端地址，如果没有则默认为 localhost:8080
const BACKEND_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// ========== 状态定义 ==========
const editing = ref(false);
const saving = ref(false);
const changingPassword = ref(false);
const avatarLoading = ref(false); // 头像加载状态
const showOldPwd = ref(false);
const showNewPwd = ref(false);
const avatarInput = ref(null);

const form = ref({ name: '', phone: '' });
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' });
const userInfo = ref({});

// 从 LocalStorage 获取用户名（用于生成默认色块）
const username = computed(() => localStorage.getItem('username') || 'User');

// 动态头像颜色
const avatarColor = computed(() => {
  const colors = ['#8a2be2', '#ff6347', '#40e0d0', '#ffa500', '#2ecc71'];
  return colors[username.value.length % colors.length];
});

const passwordMismatch = computed(() => {
  const { newPassword, confirmPassword } = passwordForm.value;
  return newPassword && confirmPassword && newPassword !== confirmPassword;
});

const toast = ref({ visible: false, message: '', type: 'info', icon: 'ℹ️' });

// ========== 方法定义 ==========

const goBack = () => router.back();

const showToast = (message, type = 'info') => {
  const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };
  toast.value = { visible: true, message, type, icon: icons[type] || 'ℹ️' };
  setTimeout(() => { toast.value.visible = false; }, 3000);
};

// 【核心修复】辅助函数：确保头像 URL 是完整的绝对路径
const processAvatarUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  // 如果是相对路径，拼接后端地址
  // 处理 url 开头的斜杠，避免双斜杠
  const path = url.startsWith('/') ? url.substring(1) : url;
  return `${BACKEND_BASE_URL}/${path}`;
};

const fetchUserInfo = async () => {
  // 1. 增加调试日志，看清到底取到了什么
  const userId = localStorage.getItem('userId');
  const userInfoStr = localStorage.getItem('userInfo');

  console.log('🔍 [UserProfile] 检查登录状态:', {
    userId,
    hasUserInfo: !!userInfoStr,
    rawUserInfo: userInfoStr
  });

  if (!userId) {
    // 2. 只有真的没 ID 才跳转
    console.warn('⚠️ [UserProfile] 未找到 userId，准备跳转登录页');
    showToast('请先登录', 'warning');

    // 使用 replace 而不是 push，防止用户点浏览器的“后退”按钮又回到这个死循环页面
    router.replace('/auth');
    return;
  }

  try {
    // 3. 发起请求
    const res = await getCurrentUser(userId);

    // 4. 处理响应 (保持你原有的逻辑，但增加对 res.data 的防御)
    if (res.success || res.code === 200) {
      const data = res.data || res;

      // 处理头像路径
      if (data.avatar) {
        data.avatar = processAvatarUrl(data.avatar);
      }

      userInfo.value = data;
      form.value = { name: data.name || '', phone: data.phone || '' };
      avatarLoading.value = false;

      console.log('✅ [UserProfile] 用户信息加载成功:', data.username);
    } else {
      // 如果后端返回 401/403 (比如 Session 失效)，也要清除本地缓存并跳转
      if (res.code === 401 || res.code === 403) {
        console.warn('⚠️ [UserProfile] 后端验证失败 (Session 过期)，清除缓存');
        localStorage.removeItem('userId');
        localStorage.removeItem('userInfo');
        router.replace('/auth');
        return;
      }
      showToast(res.message || '获取信息失败', 'error');
    }
  } catch (error) {
    console.error('❌ [UserProfile] 网络请求异常:', error);
    // 网络错误不一定代表没登录，不要盲目跳转，只提示错误
    showToast('网络连接异常，请稍后重试', 'error');
    avatarLoading.value = false;
  }
};

const triggerAvatarUpload = () => {
  if (!editing.value) {
    showToast('请先点击「编辑资料」', 'warning');
    return;
  }
  avatarInput.value?.click();
};

const handleAvatarError = () => {
  console.warn('头像加载失败 (404)，显示默认色块');
  avatarLoading.value = false;
  // 注意：这里不要清空 userInfo.value.avatar，否则下次刷新又得重新请求
  // 只是让 v-show 逻辑切换到色块显示
};

const handleAvatarUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件', 'warning');
    e.target.value = '';
    return;
  }
  if (file.size > 2 * 1024 * 1024) {
    showToast('图片不能超过 2MB', 'warning');
    e.target.value = '';
    return;
  }

  try {
    avatarLoading.value = true; // 开始上传loading

    const formData = new FormData();
    formData.append('avatar', file);
    formData.append('userId', localStorage.getItem('userId'));

    const res = await uploadAvatar(formData);

    if (res.success || res.code === 200) {
      let rawUrl = res.data?.avatar || res.data;

      // 【关键】处理返回的 URL
      const fullUrl = processAvatarUrl(rawUrl);

      console.log('头像上传成功，完整URL:', fullUrl);

      // 更新视图
      userInfo.value = { ...userInfo.value, avatar: fullUrl };

      // 同步更新 LocalStorage (为了 Header 也能立即显示新头像)
      const localUser = JSON.parse(localStorage.getItem('userInfo') || '{}');
      localUser.avatar = fullUrl;
      localStorage.setItem('userInfo', JSON.stringify(localUser));

      showToast('头像更新成功', 'success');

      // 这里的 avatarLoading 会在 img 标签触发 @load 时变为 false
      // 如果图片缓存立即加载，可能瞬间完成；如果需要强制刷新，可以在这里 setTimeout
    } else {
      showToast(res.message || '上传失败', 'error');
      avatarLoading.value = false;
    }
  } catch (error) {
    console.error('上传失败:', error);
    showToast('上传失败，请检查后端', 'error');
    avatarLoading.value = false;
  } finally {
    e.target.value = '';
  }
};

const enterEditMode = () => editing.value = true;

const cancelEdit = () => {
  form.value = { name: userInfo.value.name || '', phone: userInfo.value.phone || '' };
  editing.value = false;
};

const saveProfile = async () => {
  if (!form.value.name.trim() || !form.value.phone.trim()) {
    showToast('请填写完整信息', 'warning');
    return;
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    showToast('手机号格式错误', 'warning');
    return;
  }

  saving.value = true;
  try {
    const res = await updateUserInfo({
      id: localStorage.getItem('userId'),
      name: form.value.name,
      phone: form.value.phone
    });

    if (res.success || res.code === 200) {
      userInfo.value = { ...userInfo.value, name: form.value.name, phone: form.value.phone };
      editing.value = false;
      showToast('保存成功', 'success');
    } else {
      showToast(res.message || '保存失败', 'error');
    }
  } catch (error) {
    showToast('保存失败', 'error');
  } finally {
    saving.value = false;
  }
};

const resetPasswordForm = () => {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
};

const handleChangePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value;
  if (!oldPassword || newPassword.length < 6 || newPassword !== confirmPassword) {
    showToast('密码格式错误或不一致', 'warning');
    return;
  }

  changingPassword.value = true;
  try {
    const sessionId = localStorage.getItem('sessionId');
    const res = await api.put('/user/changePassword',
        { oldPassword, newPassword },
        { headers: { 'X-Session-Id': sessionId, 'Content-Type': 'application/json' } }
    );

    if (res.success || res.code === 200) {
      showToast('密码修改成功，请重新登录', 'success');
      setTimeout(() => {
        localStorage.clear();
        router.push('/auth');
      }, 1500);
    } else {
      showToast(res.message || '修改失败', 'error');
    }
  } catch (error) {
    showToast(error.response?.data?.message || '原密码错误', 'error');
  } finally {
    changingPassword.value = false;
  }
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
/*
  注意：在 scoped 样式中，:root 可能无法正确全局注入变量。
  为了确保变量在当前组件可用，我们直接在 :deep 或根容器定义，
  或者为了简单起见，既然你后面都用了具体色值，这里主要做重置和基础设定。
*/

/* 基础重置 */
* {
  box-sizing: border-box;
}

.user-profile-container {
  /* 局部变量定义 (仅在 .user-profile-container 内部有效) */
  --primary-color: #8a2be2;
  --danger-color: #ff4d4f;
  --text-main: #1d2129;
  --text-sub: #86909c;
  --border-color: #e5e6eb;
  --bg-light: #f7f8fa;

  max-width: 900px;
  margin: 20px auto;
  padding: 0 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: var(--text-main);
  line-height: 1.5;
}

/* 顶部标题布局 */
.page-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap; /* 防止小屏幕换行挤压 */
}

/* 返回按钮 */
.back-btn {
  padding: 6px 12px;
  border-radius: 6px;
  background-color: var(--bg-light);
  border: 1px solid var(--border-color);
  color: #4e5969;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  user-select: none;
}

.back-btn:hover {
  background-color: var(--primary-color);
  color: #fff;
  border-color: var(--primary-color);
}

.back-btn:active {
  transform: scale(0.98);
}

/* 标题容器 */
.header-title {
  flex: 1;
  min-width: 200px; /* 防止标题过窄 */
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--text-main);
}

.subtitle {
  color: var(--text-sub);
  font-size: 14px;
  margin: 0;
}

/* 内容包装器 */
.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

@media (min-width: 768px) {
  .content-wrapper {
    flex-direction: row;
    align-items: flex-start;
  }
  .profile-card { flex: 1.2; }
  .security-card { flex: 0.8; }
}

/* 卡片通用样式 */
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fafafa;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}

.card-body {
  padding: 24px;
}

/* 头像区域 */
.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 10px;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  transition: transform 0.2s ease;
  border: 2px solid #f0f0f0; /* 增加边框让轮廓更清晰 */
}

.avatar-wrapper:hover {
  transform: scale(1.05);
  border-color: var(--primary-color);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  font-weight: bold;
  user-select: none;
}

.loading-placeholder {
  background-color: #f0f0f0 !important;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block; /* 消除图片底部默认间隙 */
}

.avatar-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
  backdrop-filter: blur(2px); /* 增加毛玻璃效果 */
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-tips {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 4px;
}

/* 表单样式 */
.info-form, .password-form-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: #4e5969;
}

.required { color: var(--danger-color); margin-left: 4px; }

.std-input, .disabled-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s ease;
  box-sizing: border-box;
  font-family: inherit;
}

.std-input:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.15);
}

.disabled-input {
  background-color: var(--bg-light);
  color: var(--text-sub);
  cursor: not-allowed;
}

.input-tip {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 4px;
}

.password-input-group {
  position: relative;
}

.toggle-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 16px;
  user-select: none;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.toggle-icon:hover {
  opacity: 1;
}

.error-border {
  border-color: var(--danger-color) !important;
}

.error-msg {
  font-size: 12px;
  color: var(--danger-color);
  margin-top: 4px;
  display: block;
}

/* 按钮样式 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-family: inherit;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-primary {
  background-color: var(--primary-color);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  background-color: #7b27cc;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(138, 43, 226, 0.3);
}

.btn-secondary {
  background-color: #f2f3f5;
  color: #4e5969;
  border: 1px solid var(--border-color);
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e5e6eb;
  color: #1d2129;
}

.btn-danger {
  background-color: var(--danger-color);
  color: #fff;
}

.btn-danger:hover:not(:disabled) {
  background-color: #ff3333;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

/* 安全等级徽章 */
.security-level {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.level-badge {
  padding: 2px 8px;
  border-radius: 10px;
  color: #fff;
  font-weight: bold;
  font-size: 11px;
}

.level-badge.high { background-color: #52c41a; }
.level-badge.mid { background-color: #faad14; }
.level-badge.low { background-color: var(--danger-color); }

/* Toast 动画 */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}

.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  /* transform 在 enter-from 中处理，这里保持居中 */
  padding: 12px 24px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  pointer-events: none; /* 防止遮挡点击 */
}

.toast.success { background-color: #52c41a; }
.toast.error { background-color: var(--danger-color); }
.toast.warning { background-color: #faad14; }
.toast.info { background-color: #1890ff; }
</style>