<template>
  <div class="pay-result">
    <div class="result-card">
      <div class="result-icon">✓</div>
      <h2>支付完成</h2>
      <p v-if="orderNo" class="order-no">订单号：{{ orderNo }}</p>
      <p class="tip">订单状态将在几秒内自动更新为「已付款」</p>
      <p class="countdown">{{ seconds }} 秒后返回首页...</p>
      <button @click="goHome">立即返回</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
// 支付宝同步跳转回时会带 out_trade_no 等参数
const orderNo = ref(route.query.out_trade_no || '')
const seconds = ref(3)
let timer = null

function goHome() {
  if (timer) { clearInterval(timer); timer = null }
  router.replace('/home')
}

onMounted(() => {
  timer = setInterval(() => {
    seconds.value--
    if (seconds.value <= 0) goHome()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.pay-result {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 48px 64px;
  text-align: center;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
}
.result-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #52c41a;
  color: #fff;
  font-size: 36px;
  line-height: 64px;
}
h2 { margin: 0 0 12px; color: #333; }
.order-no { color: #666; font-size: 14px; margin: 4px 0; }
.tip { color: #999; font-size: 13px; margin: 4px 0 12px; }
.countdown { color: #999; font-size: 13px; margin-bottom: 16px; }
button {
  padding: 8px 24px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
button:hover { background: #0958d9; }
</style>
