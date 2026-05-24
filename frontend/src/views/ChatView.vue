<template>
  <div class="chat-shell">
    <div class="top">
      <button class="back" @click="$router.back()">←</button>
      <div class="partner"><div class="av">👤</div><div><h4>聊天</h4><span class="on">在线</span></div></div>
    </div>
    <div class="msgs" ref="box">
      <div v-if="!msgs.length" class="empty">💬 开始对话吧</div>
      <div v-for="m in msgs" :key="m.id" :class="['bub', m.senderId===buyer?'me':'them']">
        <div class="txt">{{ m.content }}</div>
        <div class="t">{{ ft(m.createTime) }}</div>
      </div>
    </div>
    <div class="bar">
      <input v-model="nm" placeholder="输入消息..." @keyup.enter="send"/>
      <button @click="send" :disabled="sending">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted,onBeforeUnmount,nextTick } from 'vue';
import { useRoute } from 'vue-router';
import service from '@/api/index';
const route=useRoute();
const sellerId=route.query.targetId;
const msgs=ref([]); const nm=ref(''); const buyer=ref(null); const sending=ref(false); const box=ref(null);
let sid=null; let ws=null;

onMounted(async()=>{
  const uid=localStorage.getItem('userId'); const ss=localStorage.getItem('sessionId');
  if(!ss||!uid){alert('请先登录');return;}
  try{
    const u=await service.get(`/user/info/${uid}`); buyer.value=u.data?.id;
    const s=await service.post('/chat/session',{sellerId}); sid=s.data?.id||s.id;
    const h=await service.get('/chat/messages',{params:{sessionId:sid}}); msgs.value=h.data||h||[];
    setupWS(sid); await nextTick(); scroll();
  }catch(e){}
});
onBeforeUnmount(()=>{if(ws)ws.close();});

const setupWS=(id)=>{
  const p=location.protocol==='https:'?'wss:':'ws:';
  ws=new WebSocket(`${p}//${location.host}/ws-chat`);
  ws.onopen=()=>{ws.send(JSON.stringify({destination:'/app/chat/session/'+id,message:'SUBSCRIBE'}));};
  ws.onmessage=(e)=>{try{const m=JSON.parse(e.data); if(!msgs.value.some(x=>x.id===m.id)){msgs.value.push(m);nextTick(()=>scroll());}}catch(e){}};
};
const send=async()=>{if(!nm.value.trim()||sending.value)return; sending.value=true;
  try{const r=await service.post('/chat/send',{sessionId:sid,content:nm.value.trim()}); const d=r.data||r; if(d){msgs.value.push(d);nm.value='';nextTick(()=>scroll());}}catch(e){} finally{sending.value=false;}
};
const scroll=()=>{if(box.value)box.value.scrollTop=box.value.scrollHeight;};
const ft=t=>t?new Date(t).toLocaleTimeString('zh-CN',{hour:'2-digit',minute:'2-digit'}):'';
</script>

<style scoped>
.chat-shell { max-width: 800px; margin: 0 auto; height: calc(100vh - 60px); display: flex; flex-direction: column; background: #fff; border-left:1px solid var(--border); border-right:1px solid var(--border); }
.top { display: flex; align-items: center; gap: 12px; padding: 12px 18px; border-bottom: 1px solid var(--border); flex-shrink: 0; }
.back { background: none; font-size: 16px; color: var(--text-light); cursor: pointer; }
.partner { display: flex; align-items: center; gap: 8px; }
.av { width: 34px; height: 34px; border-radius: 50%; background: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 16px; }
.partner h4 { font-size: 15px; font-weight: 600; }
.on { font-size: 11px; color: var(--success); }
.msgs { flex: 1; overflow-y: auto; padding: 18px; background: var(--bg); display: flex; flex-direction: column; gap: 12px; }
.empty { text-align: center; padding: 80px 0; color: var(--text-muted); }
.bub { max-width: 70%; animation: up .3s; }
.bub.me { align-self: flex-end; }
.bub.them { align-self: flex-start; }
.txt { padding: 10px 14px; border-radius: 16px; font-size: 14px; line-height: 1.5; word-break: break-word; }
.bub.me .txt { background: var(--primary); color: #fff; border-bottom-right-radius: 6px; }
.bub.them .txt { background: #fff; color: var(--text); border-bottom-left-radius: 6px; box-shadow: var(--shadow); }
.t { font-size: 11px; color: var(--text-muted); margin-top: 4px; padding: 0 4px; }
.bub.me .t { text-align: right; }
.bar { display: flex; gap: 10px; padding: 12px 18px; border-top: 1px solid var(--border); flex-shrink: 0; }
.bar input { flex: 1; padding: 10px 16px; border: 1px solid var(--border); border-radius: 20px; font-size: 14px; }
.bar input:focus { border-color: var(--primary); }
.bar button { padding: 10px 22px; background: var(--primary); color: #fff; border-radius: 20px; font-size: 14px; font-weight: 600; }
.bar button:disabled { opacity: 0.5; }
@keyframes up { from{opacity:0;transform:translateY(6px)} to{opacity:1;transform:translateY(0)} }
</style>
