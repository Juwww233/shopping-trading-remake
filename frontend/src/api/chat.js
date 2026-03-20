import service from './index'; // 引入带拦截器的 axios 实例

// 创建聊天会话
export function createSession(sellerId) {
    return service.post('/chat/session', { sellerId });
}

// 发送消息
export function sendMessage(sessionId, content) {
    return service.post('/chat/send', { sessionId, content });
}

// 获取消息历史
export function getMessages(sessionId) {
    return service.get('/chat/messages', { params: { sessionId } });
}