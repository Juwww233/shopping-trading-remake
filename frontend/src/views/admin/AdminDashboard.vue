<template>
  <div class="admin-page">
    <div class="top"><h2>⚙️ 管理后台</h2></div>
    <div class="tabs">
      <button v-for="t in tabs" :key="t.k" :class="{on:tab===t.k}" @click="switchTab(t.k)">{{ t.icon }} {{ t.label }}</button>
    </div>

    <div v-if="tab==='goods'" class="panel">
      <div v-if="gl" class="ld">加载中...</div>
      <div v-else-if="!pending.length" class="empty">🎉 暂无待审核商品</div>
      <div v-else class="rlist">
        <div v-for="g in pending" :key="g.id" class="ritem">
          <img :src="g.img" @error="e=>e.target.style.display='none'"/>
          <div class="rinfo"><h4>{{ g.name }}</h4><p>¥{{ g.price }} | 库存{{ g.stock }} | 卖家ID:{{ g.userId }} | {{ g.category }}</p></div>
          <div class="racts"><button class="ok" @click="review(g.id,'审核通过')">✓ 通过</button><button class="no" @click="review(g.id,'审核驳回')">✕ 驳回</button></div>
        </div>
      </div>
    </div>

    <div v-if="tab==='categories'" class="panel">
      <div class="arow"><input v-model="nc" placeholder="新分类名称" @keyup.enter="addCat"/><button class="ok" @click="addCat">添加</button></div>
      <div v-if="!cats.length" class="empty">暂无分类</div>
      <div v-else class="clist"><div v-for="c in cats" :key="c.id" class="crow"><input v-model="c.name"/><button class="save" @click="editCat(c)">保存</button><button class="no" @click="delCat(c.id)">删除</button></div></div>
    </div>

    <div v-if="tab==='notices'" class="panel">
      <div class="arow"><input v-model="nt" placeholder="标题" style="max-width:180px"/><input v-model="nct" placeholder="内容"/><button class="ok" @click="addN">发布</button></div>
      <div v-if="!notices.length" class="empty">暂无通知</div>
      <div v-else class="nlist"><div v-for="n in notices" :key="n.id" class="nitem"><div><h4>{{ n.title }}</h4><p>{{ n.content }}</p><span class="time">{{ n.time }}</span></div><button class="no" @click="delN(n.id)">删除</button></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api/category';
import { getNoticeList, addNotice, deleteNotice } from '@/api/notice';
import { reviewGoods } from '@/api/goodsManage';
import service from '@/api/index';

const tabs=[{k:'goods',icon:'📦',label:'商品审核'},{k:'categories',icon:'📂',label:'分类管理'},{k:'notices',icon:'📢',label:'通知管理'}];
const tab=ref('goods'); const gl=ref(false); const pending=ref([]);
const cats=ref([]); const notices=ref([]);
const nc=ref(''); const nt=ref(''); const nct=ref('');

const switchTab = (k) => { tab.value=k; if(k==='categories')fetchC(); if(k==='notices')fetchN(); };

const fetchP = async () => {
  gl.value=true;
  try { const r=await service.get('/goods/manage/my-list'); if(r.code===200)pending.value=(r.data||[]).filter(g=>g.status==='待审核'); } catch(e){} finally {gl.value=false;}
};
const review = async (id,st) => {
  try { const r=await reviewGoods(id,st); if(r.code===200){pending.value=pending.value.filter(g=>g.id!==id);alert('审核完成');} else alert(r.msg); } catch(e){}
};
const fetchC = async () => { try { const r=await getAllCategories(); if(r.code===200)cats.value=r.data; } catch(e){} };
const addCat = async () => { if(!nc.value.trim())return; try{await addCategory({name:nc.value.trim()});nc.value='';fetchC();}catch(e){} };
const editCat = async (c) => { try{await updateCategory(c);fetchC();}catch(e){} };
const delCat = async (id) => { if(!confirm('确认删除？'))return; try{await deleteCategory(id);fetchC();}catch(e){} };
const fetchN = async () => { try { const r=await getNoticeList(); if(r.code===200)notices.value=r.data; } catch(e){} };
const addN = async () => { if(!nt.value.trim()||!nct.value.trim())return; try{await addNotice({title:nt.value,content:nct.value,user:'admin'});nt.value='';nct.value='';fetchN();}catch(e){} };
const delN = async (id) => { if(!confirm('确认删除？'))return; try{await deleteNotice(id);fetchN();}catch(e){} };

fetchP();
</script>

<style scoped>
.admin-page { max-width: var(--max-width); margin: 0 auto; padding: 24px; }
.top { margin-bottom: 20px; }
.top h2 { font-size: 24px; font-weight: 700; }
.tabs { display: flex; gap: 6px; margin-bottom: 20px; }
.tabs button { padding: 10px 18px; border-radius: var(--radius); font-size: 14px; font-weight: 500; background: #fff; color: var(--text-light); box-shadow: var(--shadow); transition: all .15s; }
.tabs button.on { background: var(--primary); color: #fff; }
.panel { background: #fff; border-radius: var(--radius-lg); padding: 22px; box-shadow: var(--shadow); }
.ld, .empty { text-align: center; padding: 60px 0; color: var(--text-muted); }
.rlist { display: flex; flex-direction: column; }
.ritem { display: flex; align-items: center; gap: 14px; padding: 14px 0; border-bottom: 1px solid var(--border); }
.ritem img { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; background: #f0f0f0; }
.rinfo { flex: 1; }
.rinfo h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.rinfo p { font-size: 12px; color: var(--text-light); }
.racts { display: flex; gap: 8px; flex-shrink: 0; }
.racts button { padding: 6px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; }
button.ok { background: var(--success); color: #fff; }
button.no { background: var(--danger); color: #fff; }
button.save { background: var(--primary); color: #fff; }

.arow { display: flex; gap: 8px; margin-bottom: 16px; }
.arow input { flex: 1; padding: 9px 12px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 14px; }
.arow input:focus { border-color: var(--primary); }
.arow button { padding: 9px 18px; border-radius: var(--radius); font-size: 14px; font-weight: 600; flex-shrink: 0; }
.clist { display: flex; flex-direction: column; gap: 8px; }
.crow { display: flex; gap: 8px; align-items: center; }
.crow input { flex: 1; padding: 8px 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; }
.crow input:focus { border-color: var(--primary); }
.nlist { display: flex; flex-direction: column; gap: 8px; }
.nitem { display: flex; justify-content: space-between; align-items: center; padding: 14px; border-bottom: 1px solid var(--border); }
.nitem h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.nitem p { font-size: 13px; color: var(--text-light); }
.time { font-size: 11px; color: var(--text-muted); }
</style>
