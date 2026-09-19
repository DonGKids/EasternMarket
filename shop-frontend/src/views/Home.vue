<template>
  <div class="page-shell home">
    <Navbar />

    <div class="layout">
      <!-- 左侧：优惠券 + 地址 -->
      <aside class="left-col">
        <!-- 优惠券 -->
        <div class="card coupon-card">
          <div class="card-header">
            <h3 class="card-title">优惠券</h3>
            <span class="card-badge" v-if="coupons.length > 0">{{ coupons.length }} 张可用</span>
          </div>

          <div v-if="coupons.length === 0" class="empty-state">
            <p>暂无可用优惠券</p>
            <small>关注活动中心获取更多优惠</small>
          </div>

          <div v-else class="coupon-list">
            <div
              v-for="c in coupons"
              :key="c.id"
              class="coupon-item"
            >
              <div class="coupon-left">
                <span class="coupon-value">{{ c.value }}</span>
                <span class="coupon-condition">{{ c.condition }}</span>
              </div>
              <div class="coupon-right">
                <span class="coupon-name">{{ c.name }}</span>
                <span class="coupon-expire">{{ c.expire }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 地址 -->
        <div class="card address-card">
          <div class="card-header">
            <h3 class="card-title">收货地址</h3>
            <span class="card-action" @click="goAddress">管理</span>
          </div>

          <div v-if="addresses.length === 0" class="empty-state">
            <p>暂无收货地址</p>
            <small>添加地址方便快速下单</small>
          </div>

          <div v-else class="address-list">
            <div
              v-for="a in addresses"
              :key="a.id"
              class="address-item"
            >
              <div class="addr-name">
                {{ a.name }}
                <span class="addr-phone">{{ a.phone }}</span>
              </div>
              <div class="addr-detail">{{ a.detail }}</div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧：订单 -->
      <main class="right-col">
        <div class="card order-card">
          <div class="card-header">
            <h3 class="card-title">我的订单</h3>
            <div class="order-tabs">
              <span
                v-for="tab in orderTabs"
                :key="tab.value"
                class="order-tab"
                :class="{ active: activeTab === tab.value }"
                @click="activeTab = tab.value"
              >{{ tab.label }}</span>
            </div>
          </div>

          <div v-if="filteredOrders.length === 0" class="empty-state large">
            <p>暂无订单</p>
            <small>去市场逛逛挑选心仪的商品吧</small>
          </div>

          <div v-else class="order-list">
            <div
              v-for="o in filteredOrders"
              :key="o.id"
              class="order-item"
            >
              <div class="order-header">
                <span class="order-no">订单号：{{ o.no }}</span>
                <span class="order-status" :class="o.statusClass">{{ o.status }}</span>
              </div>
              <div class="order-products">
                <div
                  v-for="(p, idx) in o.products"
                  :key="idx"
                  class="order-product"
                >
                  <div class="product-thumb">
                    <img v-if="p.image" :src="resolveImgUrl(p.image)" :alt="p.name" @error="e => e.target.style.display = 'none'" />
                    <span v-else class="thumb-placeholder">{{ p.name?.charAt(0) }}</span>
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ p.name }}</div>
                    <div class="product-meta">
                      <span class="product-price">¥{{ Number(p.price).toFixed(2) }}</span>
                      <span class="product-qty">×{{ p.qty }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="order-footer">
                <span class="order-amount">
                  合计：<em>¥{{ o.amount.toFixed(2) }}</em>
                </span>
                <div class="order-actions">
                  <button v-if="o.canCancel" class="order-btn cancel" @click="cancelOrder(o)">取消订单</button>
                  <button v-if="o.canPay" class="order-btn pay" @click="payOrder(o)">立即付款</button>
                  <button v-if="o.canConfirm" class="order-btn confirm" @click="confirmOrder(o)">确认收货</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import { resolveImgUrl } from '../utils/img'

const router = useRouter()

// ---------- 优惠券（从 shop-coupon 拉取） ----------
const coupons = ref([])
const addresses = ref([])

const orderTabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 'pending' },
  { label: '待发货', value: 'paid' },
  { label: '待收货', value: 'shipped' },
  { label: '已完成', value: 'done' }
]
const activeTab = ref('all')

const orders = ref([])

// 从后端 shop-order 加载我的订单
async function loadOrders() {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    orders.value = []
    return
  }
  try {
    const res = await fetch(`/shop/order/mine?userId=${userId}`)
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      orders.value = data.data.map(formatOrder)
    } else {
      orders.value = []
    }
  } catch (e) {
    orders.value = []
  }
}

// 后端 OrderVO -> 前端订单卡片展示对象
// status: 0 待付款, 1 已付款, 2 已发货, 3 已完成, 4 已取消
function formatOrder(o) {
  const statusMap = {
    0: { text: '待付款', value: 'pending', cls: 'pending', canCancel: true, canPay: true, canConfirm: false },
    1: { text: '待发货', value: 'paid', cls: 'paid', canCancel: true, canPay: false, canConfirm: false },
    2: { text: '待收货', value: 'shipped', cls: 'shipped', canCancel: false, canPay: false, canConfirm: true },
    3: { text: '已完成', value: 'done', cls: 'done', canCancel: false, canPay: false, canConfirm: false },
    4: { text: '已取消', value: 'cancelled', cls: 'cancelled', canCancel: false, canPay: false, canConfirm: false }
  }
  const s = statusMap[o.status] || statusMap[0]
  return {
    id: o.id,
    no: o.orderNo,
    status: s.text,
    statusValue: s.value,
    statusClass: s.cls,
    products: (o.items || []).map(it => ({
      name: it.productName,
      price: Number(it.price || 0),
      qty: it.quantity,
      image: it.productImage || ''
    })),
    amount: Number(o.payAmount || 0),
    couponId: o.userCouponId,
    couponName: '',
    canCancel: s.canCancel,
    canPay: s.canPay,
    canConfirm: s.canConfirm
  }
}

function onOrderCreated() {
  loadOrders()
}

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.statusValue === activeTab.value)
})

function goAddress() {
  router.push('/address')
}

async function cancelOrder(o) {
  if (!confirm(`确定取消订单 ${o.no} 吗？`)) return
  try {
    // 后端统一处理：恢复库存 + 释放优惠券（仅已付款订单）+ 置为已取消
    const res = await fetch(`/shop/order/${o.id}/cancel`, { method: 'POST' })
    const data = await res.json()
    if (data.success) {
      alert(`订单 ${o.no} 已取消`)
      await loadOrders()
      // 通知优惠券页面刷新（取消已付款订单会释放券）
      window.dispatchEvent(new CustomEvent('coupon-released'))
    } else {
      alert(data.msg || '取消失败')
    }
  } catch (e) {
    alert('取消请求失败：' + e.message)
  }
}

async function payOrder(o) {
  try {
    // 后端调用 alipay.trade.page.pay 生成自动提交的支付表单 HTML
    const res = await fetch(`/shop/order/pay?orderNo=${o.no}`)
    const data = await res.json()
    if (data.success) {
      // 防御：确保 data.data 是字符串（支付表单 HTML），否则是旧代码或响应异常
      if (typeof data.data !== 'string' || !data.data.includes('<form')) {
        alert('支付接口返回异常，请重启 shop-order 服务后再试')
        return
      }
      // document.write 写入支付宝返回的 form HTML，浏览器自动提交跳转到支付宝收银台
      document.write(data.data)
    } else {
      alert(data.msg || '拉起支付失败')
    }
  } catch (e) {
    alert('支付请求失败：' + e.message)
  }
}

async function confirmOrder(o) {
  try {
    const res = await fetch(`/shop/order/${o.id}/confirm`, { method: 'PUT' })
    const data = await res.json()
    if (data.success) {
      alert('已确认收货')
      await loadOrders()
    } else {
      alert(data.msg || '确认失败')
    }
  } catch (e) {
    alert('确认请求失败：' + e.message)
  }
}

// 把后端 UserCouponVO 转成卡片展示用的对象
function formatCoupon(vo) {
  // type: 1 满减，2 折扣，3 立减
  let value, condition
  if (vo.type === 2) {
    // 折扣券：value 显示"85折"
    value = (Number(vo.discount) * 10).toFixed(1).replace(/\.0$/, '') + '折'
    condition = vo.threshold && Number(vo.threshold) > 0
      ? `满 ¥${Number(vo.threshold).toFixed(0)} 可用`
      : '无门槛'
  } else {
    // 满减 / 立减：value 显示 ¥面值
    value = '¥' + Number(vo.faceValue || 0).toFixed(0)
    if (vo.type === 3) {
      condition = '立减'
    } else {
      condition = vo.threshold && Number(vo.threshold) > 0
        ? `满 ¥${Number(vo.threshold).toFixed(0)} 可用`
        : '无门槛'
    }
  }
  // 过期时间格式化
  let expire = ''
  if (vo.expireTime) {
    const d = new Date(vo.expireTime)
    expire = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 到期`
  }
  return {
    id: vo.id,
    value,
    condition,
    name: vo.name || '优惠券',
    expire
  }
}

async function loadCoupons() {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    // 未登录，不拉取
    return
  }
  try {
    const res = await fetch(`/shop/coupon/user/mine?userId=${userId}&status=0`)
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      coupons.value = data.data.map(formatCoupon)
    }
  } catch (e) {
    // 拉取失败不影响页面其他模块
  }
}

onMounted(() => {
  loadOrders()
  loadCoupons()
  window.addEventListener('order-created', onOrderCreated)
})

onUnmounted(() => {
  window.removeEventListener('order-created', onOrderCreated)
})
</script>

<style scoped>
.layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 36px 60px;
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 28px;
  align-items: stretch;
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

/* ---------- 左侧列 ---------- */
.left-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
}

/* 让地址卡片自动填充剩余空间 */
.address-card {
  flex: 1;
}

.coupon-card {
  flex-shrink: 0;
}

/* ---------- 卡片通用 ---------- */
.card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  box-shadow: 0 8px 28px var(--shadow-primary);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid var(--color-soft);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #000;
  margin: 0;
}

.card-title svg {
  width: 18px;
  height: 18px;
  color: var(--color-secondary);
}

.card-badge {
  font-size: 12px;
  background: var(--color-primary-gradient);
  color: #fff;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.card-action {
  font-size: 13px;
  color: var(--color-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease;
}

.card-action:hover {
  color: var(--color-primary);
}

/* ---------- 空状态 ---------- */
.empty-state {
  padding: 36px 20px;
  text-align: center;
  color: #8b8676;
}

.empty-state.large {
  padding: 60px 20px;
}

.empty-state p {
  font-size: 14px;
  margin-bottom: 4px;
  font-weight: 600;
  color: #000;
}

.empty-state small {
  font-size: 12px;
  color: #b2ac9b;
}

/* ---------- 优惠券 ---------- */
.coupon-list {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-item {
  display: flex;
  border: 1px dashed var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  transition: box-shadow 0.2s ease;
}

.coupon-item:hover {
  box-shadow: 0 4px 12px rgba(30, 58, 138, 0.12);
}

.coupon-left {
  background: var(--color-primary-gradient);
  color: #fff;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 90px;
  position: relative;
}

.coupon-left::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background: #fff;
  border-radius: 50%;
}

.coupon-value {
  font-size: 22px;
  font-weight: 900;
  line-height: 1;
}

.coupon-condition {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 4px;
}

.coupon-right {
  flex: 1;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name {
  font-size: 14px;
  font-weight: 600;
  color: #000;
}

.coupon-expire {
  font-size: 12px;
  color: #9aa6bf;
  margin-top: 2px;
}

/* ---------- 地址 ---------- */
.address-list {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--color-bg);
  transition: background 0.2s ease;
}

.address-item:hover {
  background: var(--color-soft);
}

.addr-name {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  margin-bottom: 4px;
}

.addr-phone {
  font-size: 13px;
  font-weight: 400;
  color: #9aa6bf;
  margin-left: 8px;
}

.addr-detail {
  font-size: 13px;
  color: #4a5a78;
  line-height: 1.5;
}

/* ---------- 订单 ---------- */
.order-card {
  min-height: 500px;
}

.order-tabs {
  display: flex;
  gap: 4px;
}

.order-tab {
  padding: 5px 14px;
  font-size: 13px;
  color: #000;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
  font-weight: 500;
}

.order-tab:hover {
  color: var(--color-secondary);
}

.order-tab.active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 700;
}

.order-list {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  border: 1px solid var(--color-soft);
  border-radius: 12px;
  padding: 16px;
  transition: box-shadow 0.2s ease;
}

.order-item:hover {
  box-shadow: 0 4px 14px var(--shadow-primary);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--color-border);
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: #9aa6bf;
  font-weight: 500;
}

.order-status {
  font-size: 13px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 999px;
}

.order-status.pending { background: #FEF3C7; color: #B45309; }
.order-status.paid { background: #DBEAFE; color: #1E40AF; }
.order-status.shipped { background: #D1FAE5; color: #065F46; }
.order-status.done { background: #E5E7EB; color: #374151; }
.order-status.cancelled { background: #F3F4F6; color: #9CA3AF; }

.order-products {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
  overflow: hidden;
  background: var(--color-soft);
  border: 1px solid var(--color-border);
}

.product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 4px;
}

.thumb-placeholder {
  color: var(--color-primary);
  font-size: 18px;
  font-weight: 700;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-info .product-name {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  margin-bottom: 4px;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-price {
  font-size: 14px;
  color: #c0392b;
  font-weight: 700;
}

.product-qty {
  font-size: 13px;
  color: #9aa6bf;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
}

.order-amount {
  font-size: 13px;
  color: #000;
}

.order-amount em {
  font-size: 18px;
  font-weight: 900;
  color: #c0392b;
  font-style: normal;
  letter-spacing: -0.5px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.order-btn {
  padding: 6px 16px;
  font-size: 13px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: #fff;
  color: #000;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 600;
}

.order-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.order-btn.pay {
  background: var(--color-primary-gradient);
  color: #fff;
  border-color: transparent;
}

.order-btn.pay:hover {
  background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-primary) 100%);
  color: #fff;
  transform: translateY(-1px);
}

.order-btn.confirm {
  background: var(--color-primary);
  color: #fff;
  border-color: transparent;
}

.order-btn.confirm:hover {
  background: var(--color-secondary);
  color: #fff;
}

.order-btn.cancel:hover {
  border-color: #c62828;
  color: #c62828;
}

@media (max-width: 1100px) {
  .layout {
    padding: 24px 20px 40px;
  }
}
</style>
