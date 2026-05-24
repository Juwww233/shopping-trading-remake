<template>
  <div class="pub-page">
    <div class="top"><button class="back" @click="$router.back()">← 返回</button><h2>{{ isEdit?'编辑商品':'发布商品' }}</h2></div>
    <form class="form" @submit.prevent="submit">
      <div class="fg"><label>名称 <em>*</em></label><input v-model="f.name" placeholder="商品名称" required /></div>
      <div class="row">
        <div class="fg"><label>价格 <em>*</em></label><input v-model="f.price" type="number" step="0.01" min="0" placeholder="0.00" required /></div>
        <div class="fg"><label>库存 <em>*</em></label><input v-model="f.stock" type="number" min="1" placeholder="数量" required /></div>
      </div>
      <div class="fg"><label>分类 <em>*</em></label><select v-model="f.category" required><option value="">选择分类</option><option v-for="c in cats" :key="c.id" :value="c.name">{{ c.name }}</option></select></div>
      <div class="fg"><label>发货地址 <em>*</em></label><input v-model="f.address" placeholder="发货地址" required /></div>
      <div class="fg"><label>图片URL <em>*</em></label><input v-model="f.img" placeholder="图片链接" required /><img v-if="f.img" :src="f.img" class="preview" @error="e=>e.target.style.display='none'"/></div>
      <div class="fg"><label>描述 <em>*</em></label><textarea v-model="f.content" rows="6" placeholder="商品描述..." required></textarea></div>
      <div class="btns"><button type="submit" class="btn-sub" :disabled="sub">{{ sub?'提交中...':(isEdit?'保存修改':'发布') }}</button><button type="button" class="btn-c" @click="$router.back()">取消</button></div>
      <p v-if="msg" :class="mt">{{ msg }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { publishGoods, updateGoods, getMyGoods } from '@/api/goodsManage';
import { getAllCategories } from '@/api/category';
const router = useRouter(); const route = useRoute();
const isEdit = ref(false); const sub = ref(false); const msg = ref(''); const mt = ref('');
const cats = ref([]);
const f = ref({name:'',price:'',stock:'',category:'',address:'',img:'',content:''});
onMounted(async () => {
  try { const r = await getAllCategories(); if(r.code===200) cats.value=r.data; } catch(e) {}
  if(route.params.id) { isEdit.value=true;
    try { const r = await getMyGoods(); if(r.code===200){ const g=r.data.find(x=>x.id==route.params.id); if(g) f.value={name:g.name,price:g.price,stock:g.stock,category:g.category,address:g.address,img:g.img,content:g.content}; } } catch(e){} }
});
const submit = async () => {
  sub.value=true; msg.value='';
  try { const r = isEdit.value?await updateGoods(route.params.id,f.value):await publishGoods(f.value);
    if(r.code===200){msg.value='操作成功！';mt.value='ok';setTimeout(()=>router.push('/home'),1200);}
    else {msg.value=r.msg||'失败';mt.value='err';}
  } catch(e) {msg.value='操作失败';mt.value='err';} finally {sub.value=false;}
};
</script>

<style scoped>
.pub-page { max-width: 700px; margin: 0 auto; padding: 24px; }
.top { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.back { background: none; font-size: 14px; color: var(--text-light); cursor: pointer; }
.back:hover { color: var(--primary); }
.top h2 { font-size: 22px; font-weight: 700; }
.form { background: #fff; padding: 28px; border-radius: var(--radius-lg); box-shadow: var(--shadow); }
.fg { margin-bottom: 18px; }
.fg label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 6px; color: var(--text); }
.fg label em { color: var(--danger); font-style: normal; }
.fg input, .fg select, .fg textarea { width: 100%; padding: 10px 14px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 14px; box-sizing: border-box; transition: border-color .2s; }
.fg input:focus, .fg select:focus, .fg textarea:focus { border-color: var(--primary); }
.fg textarea { resize: vertical; }
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.preview { margin-top: 8px; max-width: 150px; border-radius: 6px; }
.btns { display: flex; gap: 12px; margin-top: 24px; }
.btn-sub { flex: 1; padding: 12px; background: var(--primary); color: #fff; border-radius: var(--radius); font-size: 15px; font-weight: 600; }
.btn-sub:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-c { padding: 12px 24px; background: #f5f5f5; border-radius: var(--radius); font-size: 15px; color: var(--text-light); }
.ok { color: var(--success); text-align: center; margin-top: 12px; }
.err { color: var(--danger); text-align: center; margin-top: 12px; }

@media(max-width:480px){ .row{grid-template-columns:1fr;} .form{padding:18px;} }
</style>
