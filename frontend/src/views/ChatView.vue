<template>
  <div class="chat-container">
    <div class="message-list" ref="messageList">
      <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message-bubble', msg.senderId === buyerId ? 'sent' : 'received']"
      >
        {{ msg.content }}
      </div>
    </div>

    <div class="input-area">
      <input
          v-model="newMessage"
          @keyup.enter="sendMessage"
          placeholder="输入消息..."
      />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const sellerId = route.query.targetId;
const messages = ref([]);
const newMessage = ref('');
const buyerId = ref(null);

onMounted(async () => {
  // 修正：使用不同变量名（loginSessionId）
  const loginSessionId = localStorage.getItem('sessionId');
  if (!loginSessionId) {
    alert('未登录');
    return;
  }

  try {
    const response = await axios.get('/api/user/info', {
      headers: { 'X-Session-Id': loginSessionId }
    });
    buyerId.value = response.data.id;

    const sessionRes = await axios.post('/api/chat/session', { sellerId });
    const chatSessionId = sessionRes.data.id; // 用 chatSessionId 代替 sessionId

    const historyRes = await axios.get('/api/chat/messages', { params: { sessionId: chatSessionId } });
    messages.value = historyRes.data;

    setupWebSocket(chatSessionId); // 传递 chatSessionId
  } catch (error) {
    console.error('初始化失败:', error);
    alert('聊天初始化失败');
  }
});

// 修正：函数参数名改为 chatSessionId
const setupWebSocket = (chatSessionId) => {
  const socket = new WebSocket(`ws://${window.location.host}/ws-chat`);

  socket.onopen = () => {
    socket.send(JSON.stringify({
      destination: '/app/chat/session/' + chatSessionId,
      message: 'SUBSCRIBE'
    }));
  };

  socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    messages.value.push(message);
    scrollBottom();
  };
};

const sendMessage = async () => {
  if (!newMessage.value.trim()) return;

  try {
    // 修正：使用 chatSessionId 代替 sessionId
    const chatSessionId = messages.value[0]?.sessionId;
    await axios.post('/api/chat/send', {
      sessionId: chatSessionId, // 传入聊天会话ID
      content: newMessage.value
    });
    newMessage.value = '';
  } catch (error) {
    console.error('发送失败:', error);
    alert('消息发送失败');
  }
};

const scrollBottom = () => {
  const container = document.querySelector('.message-list');
  container.scrollTop = container.scrollHeight;
};
</script>

<style scoped>
.chat-container { height: 100vh; display: flex; flex-direction: column; }
.message-list { flex: 1; overflow-y: auto; padding: 10px; }
.message-bubble { padding: 8px 12px; margin: 5px; max-width: 70%; border-radius: 15px; }
.sent { background: #e3f2fd; margin-left: auto; }
.received { background: #f0f0f0; margin-right: auto; }
.input-area { padding: 10px; display: flex; gap: 5px; }
input { flex: 1; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
button { padding: 8px 15px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; }
</style>