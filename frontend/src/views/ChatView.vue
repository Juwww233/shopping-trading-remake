<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3>与卖家聊天</h3>
    </div>

    <div class="message-list" ref="messageList">
      <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message-bubble', msg.senderId === buyerId ? 'sent' : 'received']"
      >
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ formatTime(msg.createTime) }}</div>
      </div>
      <div v-if="messages.length === 0" class="no-messages">
        暂无消息，开始聊天吧~
      </div>
    </div>

    <div class="input-area">
      <input
          v-model="newMessage"
          @keyup.enter="sendMessage"
          placeholder="输入消息..."
      />
      <button @click="sendMessage" :disabled="sending">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import service from '@/api/index';

const route = useRoute();
const sellerId = route.query.targetId;
const messages = ref([]);
const newMessage = ref('');
const buyerId = ref(null);
const sending = ref(false);
let chatSessionId = null;
let ws = null;

onMounted(async () => {
  const userId = localStorage.getItem('userId');
  const sessionId = localStorage.getItem('sessionId');

  if (!sessionId || !userId) {
    alert('请先登录');
    window.location.href = '/login';
    return;
  }

  try {
    // 1. 获取当前用户信息
    // 注意：拦截器已返回 response.data (Result 对象)
    // Result 结构: { code: 200, data: User, msg: "success" }
    const userResponse = await service.get(`/user/info/${userId}`);
    console.log('用户信息响应:', userResponse);

    // 【修复】userResponse 已经是 Result 对象，直接取 .data
    const userData = userResponse.data;
    if (!userData) {
      throw new Error('用户数据为空');
    }
    buyerId.value = userData.id;

    // 2. 创建或获取聊天会话
    const sessionRes = await service.post('/chat/session', { sellerId });
    console.log('会话响应:', sessionRes);
    chatSessionId = sessionRes.data?.id || sessionRes.id;

    // 3. 获取历史消息
    const historyRes = await service.get('/chat/messages', {
      params: { sessionId: chatSessionId }
    });
    console.log('消息历史响应:', historyRes);
    messages.value = historyRes.data || historyRes || [];

    // 4. 初始化 WebSocket
    setupWebSocket(chatSessionId);

    // 5. 滚动到底部
    setTimeout(scrollBottom, 100);
  } catch (error) {
    console.error('初始化失败:', error);
    const status = error.response?.status;
    if (status === 401) {
      alert('登录已过期，请重新登录');
      window.location.href = '/login';
    } else {
      alert('聊天初始化失败: ' + (error.message || '未知错误'));
    }
  }
});

onBeforeUnmount(() => {
  if (ws) {
    ws.close();
  }
});

const setupWebSocket = (sessionId) => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  ws = new WebSocket(`${protocol}//${window.location.host}/ws-chat`);

  ws.onopen = () => {
    console.log('WebSocket 连接成功');
    ws.send(JSON.stringify({
      destination: '/app/chat/session/' + sessionId,
      message: 'SUBSCRIBE'
    }));
  };

  ws.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data);
      const exists = messages.value.some(m => m.id === message.id);
      if (!exists) {
        messages.value.push(message);
        scrollBottom();
      }
    } catch (e) {
      console.error('解析消息失败:', e);
    }
  };

  ws.onerror = (error) => {
    console.error('WebSocket 错误:', error);
  };

  ws.onclose = () => {
    console.log('WebSocket 连接关闭');
  };
};

const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value) return;

  sending.value = true;
  try {
    const response = await service.post('/chat/send', {
      sessionId: chatSessionId,
      content: newMessage.value.trim()
    });

    console.log('发送响应:', response);

    // 【修复】处理不同的响应格式
    const messageData = response.data || response;
    if (messageData) {
      messages.value.push(messageData);
    }

    newMessage.value = '';
    scrollBottom();
  } catch (error) {
    console.error('发送失败:', error);
    console.error('错误详情:', error.response?.data);
    alert('消息发送失败: ' + (error.response?.data?.msg || error.message || '未知错误'));
  } finally {
    sending.value = false;
  }
};

const scrollBottom = () => {
  const container = document.querySelector('.message-list');
  if (container) {
    container.scrollTop = container.scrollHeight;
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.chat-header {
  padding: 15px 20px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.chat-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
}

.no-messages {
  text-align: center;
  color: #999;
  padding: 40px;
  font-size: 14px;
}

.message-bubble {
  padding: 10px 15px;
  margin: 8px 0;
  max-width: 70%;
  border-radius: 12px;
  position: relative;
}

.message-bubble.sent {
  background: #007bff;
  color: #fff;
  margin-left: auto;
  border-bottom-right-radius: 4px;
}

.message-bubble.received {
  background: #fff;
  color: #333;
  margin-right: auto;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message-content {
  word-wrap: break-word;
  line-height: 1.4;
}

.message-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.7;
  text-align: right;
}

.input-area {
  padding: 15px 20px;
  background: #fff;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.input-area input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.input-area input:focus {
  border-color: #007bff;
}

.input-area button {
  padding: 12px 25px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.input-area button:hover {
  background: #0056b3;
}

.input-area button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>