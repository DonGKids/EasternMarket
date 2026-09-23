<template>
  <div class="page-shell market">
    <Navbar />

    <div class="layout">
      <!-- 左侧栏：搜索 + 购物车 -->
      <aside class="left-col">
        <!-- 搜索区 -->
        <section class="panel search-panel">
          <h3 class="panel-title">搜索</h3>
          <div class="search-box">
            <input
              v-model="keyword"
              type="text"
              class="search-input"
              placeholder="搜索商品名称…"
              @keyup.enter="onSearch"
            />
            <button class="search-btn" @click="onSearch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                <circle cx="11" cy="11" r="7"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              搜索
            </button>
          </div>
        </section>

        <!-- 购物车区 -->
        <section class="panel cart-panel">
          <div class="cart-header">
            <h3 class="panel-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="9" cy="21" r="1"/>
                <circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
              购物车
            </h3>
            <span class="cart-count" v-if="cartTotalCount > 0">{{ cartTotalCount }} 件</span>
          </div>

          <div v-if="cart.length === 0" class="cart-empty">
            <p>购物车还是空的</p>
            <small>从右侧挑选好物加入吧</small>
          </div>

          <div v-else class="cart-list">
            <div
              v-for="item in cart"
              :key="item.id"
              class="cart-item"
            >
              <ProductImage :src="item.image" :name="item.name" size="thumb" />
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
              </div>
              <div class="item-qty">
                <button class="qty-btn" @click="decreaseQty(item)">−</button>
                <span class="qty-num">{{ item.qty }}</span>
                <button class="qty-btn" @click="increaseQty(item)">+</button>
              </div>
              <button class="remove-btn" @click="removeFromCart(item.id)" title="移除">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
          </div>

          <div v-if="cart.length > 0" class="cart-summary">
            <div class="summary-row">
              <span>商品数量</span>
              <span>{{ cartTotalCount }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品金额</span>
              <span>¥{{ subtotal.toFixed(2) }}</span>
            </div>
            <div class="summary-row coupon-row">
              <span>优惠券</span>
              <select
                v-if="availableCoupons.length > 0"
                v-model="selectedCouponId"
                class="coupon-select"
                @change="onCouponChange"
              >
                <option :value="null">不使用</option>
                <option
                  v-for="c in availableCoupons"
                  :key="c.id"
                  :value="c.id"
                >{{ couponOptionLabel(c) }}</option>
              </select>
              <span v-else class="no-coupon">暂无可用</span>
            </div>
            <div class="summary-row">
              <span>优惠</span>
              <span class="discount">-¥{{ discountAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-row total">
              <span>应付合计</span>
              <span class="total-price">¥{{ payable.toFixed(2) }}</span>
            </div>
          </div>

          <button
            class="checkout-btn"
            :disabled="cart.length === 0 || submitting"
            @click="handleCheckout"
          >
            <span v-if="!submitting">
              下单
              <small v-if="cart.length > 0">（¥{{ payable.toFixed(2) }}）</small>
            </span>
            <span v-else>提交中…</span>
          </button>
        </section>
      </aside>

      <!-- 右侧商品区 -->
      <main class="right-col">
        <section
          v-for="sec in productSections"
          :key="sec.key"
          class="product-section"
        >
          <header class="section-head">
            <h2 class="section-title">{{ sec.title }}</h2>
            <div class="title-underline"></div>
          </header>

          <div class="product-grid">
            <article
              v-for="p in sec.filtered"
              :key="p.id"
              class="product-card product-card-clickable"
              :class="{ selected: isInCart(p.id) }"
              @click="openDetail(p)"
            >
              <div class="product-cover">
                <div class="cover-badge flash-badge" v-if="p.isFlash">限时</div>
                <div class="cover-badge" v-else-if="p.badge">{{ p.badge }}</div>
                <ProductImage :src="p.image" :name="p.name" size="cover" />
              </div>
              <div class="product-body product-body-simple">
                <h3 class="product-name product-name-simple">{{ p.name }}</h3>
                <div class="price-row price-row-center">
                  <span class="currency">¥</span>
                  <span class="price">{{ p.price.toFixed(2) }}</span>
                  <span class="original" v-if="p.originalPrice">¥{{ p.originalPrice.toFixed(2) }}</span>
                </div>
              </div>
            </article>
            <div v-if="sec.filtered.length === 0" class="empty-hint">暂无符合条件的商品</div>
          </div>
        </section>
      </main>
    </div>

    <!-- ===== 商品详情弹窗 ===== -->
    <div v-if="showDetail" class="detail-mask" @click.self="closeDetail">
      <div class="detail-modal">
        <button class="detail-close" @click="closeDetail" aria-label="关闭">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" width="20" height="20">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>

        <div class="detail-layout">
          <!-- 左：大图 -->
          <div class="detail-cover">
            <div class="detail-badge flash-badge" v-if="currentProduct?.isFlash">限时</div>
            <div class="detail-badge" v-else-if="currentProduct?.badge">{{ currentProduct.badge }}</div>
            <ProductImage :src="currentProduct?.image || ''" :name="currentProduct?.name || ''" size="cover" />
          </div>

          <!-- 右：信息 + 操作 -->
          <div class="detail-info">
            <h3 class="detail-name">{{ currentProduct?.name }}</h3>

            <div class="detail-price-block">
              <div class="detail-price-now">
                <span class="detail-currency">¥</span>
                <span class="detail-price-value">{{ currentProduct ? currentProduct.price.toFixed(2) : '0.00' }}</span>
              </div>
              <span class="detail-original" v-if="currentProduct?.originalPrice">
                ¥{{ currentProduct.originalPrice.toFixed(2) }}
              </span>
              <span class="detail-discount" v-if="currentProduct?.originalPrice">
                约{{ (currentProduct.price / currentProduct.originalPrice * 10).toFixed(1) }}折
              </span>
            </div>

            <div class="detail-section">
              <div class="detail-section-title">商品介绍</div>
              <p class="detail-desc">{{ currentProduct?.desc || '暂无商品介绍' }}</p>
            </div>

            <div class="detail-section">
              <div class="detail-section-title">活动说明</div>
              <ul class="detail-rules">
                <li v-if="currentProduct?.isFlash">七夕限时价，数量有限先到先得</li>
                <li v-else>活动期间享专属折扣价</li>
                <li>可在购物车使用领取的优惠券叠加抵扣</li>
                <li>下单后 48 小时内发货，支持 7 天无理由退换</li>
              </ul>
            </div>

            <div class="detail-section">
              <div class="detail-section-title">数量</div>
              <div class="detail-qty">
                <button class="qty-btn" :disabled="detailQuantity <= 1" @click="detailQuantity--">－</button>
                <span class="qty-num">{{ detailQuantity }}</span>
                <button class="qty-btn" @click="detailQuantity++">＋</button>
                <span class="detail-subtotal">
                  小计：<b>¥{{ currentProduct ? (currentProduct.price * detailQuantity).toFixed(2) : '0.00' }}</b>
                </span>
              </div>
            </div>

            <div class="detail-actions">
              <button class="btn-cart" @click="addDetailToCart">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <circle cx="9" cy="21" r="1.5"/><circle cx="19" cy="21" r="1.5"/>
                  <path d="M3 3h2l2.4 12.3a2 2 0 0 0 2 1.7h9.7a2 2 0 0 0 2-1.6L22 8H6"/>
                </svg>
                加入购物车
              </button>
              <button class="btn-buy" @click="buyNow">
                立即购买
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, defineComponent, h } from 'vue'
import Navbar from '../components/Navbar.vue'
import { resolveImgUrl } from '../utils/img'
import {
  apiAddToCart,
  apiUpdateCartQty,
  apiRemoveFromCart,
  apiBatchDeleteCart,
  apiListCart
} from '../api/cart'

const ProductImage = defineComponent({
  name: 'ProductImage',
  props: {
    src: { type: String, required: true },
    name: { type: String, default: '' },
    size: { type: String, default: 'cover' }
  },
  setup(props) {
    const loadFailed = ref(false)
    const currentSrc = ref(resolveImgUrl(props.src))
    const triedPng = ref(false)

    // 当 src prop 变化时同步更新（如切换商品或刷新后数据变更）
    watch(() => props.src, (newSrc) => {
      currentSrc.value = resolveImgUrl(newSrc)
      loadFailed.value = false
      triedPng.value = false
    })

    function onError() {
      if (!triedPng.value && currentSrc.value.toLowerCase().endsWith('.jpg')) {
        triedPng.value = true
        currentSrc.value = currentSrc.value.replace(/\.jpg$/i, '.png')
        return
      }
      loadFailed.value = true
    }

    function firstChar(name) {
      return name ? name.slice(0, 1) : ''
    }

    return () => {
      const wrapperClass = props.size === 'thumb' ? 'item-thumb' : 'cover-inner product-cover-img'
      return h('div', { class: wrapperClass }, [
        loadFailed.value
          ? h('div', { class: 'pimg-placeholder' }, firstChar(props.name))
          : h('img', {
              src: currentSrc.value,
              alt: props.name,
              class: 'pimg-real',
              onError
            })
      ])
    }
  }
})

// ---------- 搜索 ----------
const keyword = ref('')
const searchKeyword = computed(() => keyword.value.trim())

// ---------- 商品数据（从后端 API 加载） ----------
const products = ref([])
const productLoading = ref(false)

// 后端 Product -> 前端展示对象
function mapProduct(p) {
  return {
    id: p.id,
    name: p.name,
    desc: p.description,
    price: Number(p.price),
    originalPrice: p.originalPrice != null ? Number(p.originalPrice) : null,
    badge: p.badge,
    isFlash: Number(p.isFlash) === 1,
    image: p.imageUrl,
    stock: p.stock,
    sales: p.sales
  }
}

// 后端购物车 CartItemVO -> 前端 cart 项（与原 cart 内部结构保持一致）
// 后端字段 quantity 对应前端 qty；后端 productId 对应前端 id
function mapCartItem(c) {
  return {
    id: c.productId,
    name: c.name,
    desc: c.desc,
    price: Number(c.price),
    originalPrice: c.originalPrice != null ? Number(c.originalPrice) : null,
    badge: c.badge,
    isFlash: Number(c.isFlash) === 1,
    image: c.image,
    stock: c.stock,
    sales: c.sales,
    qty: Number(c.quantity)
  }
}

async function loadProducts() {
  productLoading.value = true
  try {
    const params = new URLSearchParams()
    if (searchKeyword.value) params.set('keyword', searchKeyword.value)
    const res = await fetch(`/shop/product/onSale?${params.toString()}`)
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      products.value = data.data.map(mapProduct)
    } else {
      products.value = []
    }
  } catch (e) {
    products.value = []
  } finally {
    productLoading.value = false
  }
}

const flashProducts = computed(() => products.value.filter(p => p.isFlash))
const normalProducts = computed(() => products.value.filter(p => !p.isFlash))

// 搜索关键词变化时从后端重新加载（后端已支持关键词过滤）
watch(searchKeyword, () => {
  loadProducts()
})

function matchKeyword(p) {
  if (!searchKeyword.value) return true
  const k = searchKeyword.value.toLowerCase()
  return p.name.toLowerCase().includes(k) || (p.desc || '').toLowerCase().includes(k)
}

const filteredFlashProducts = computed(() => flashProducts.value.filter(matchKeyword))
const filteredNormalProducts = computed(() => normalProducts.value.filter(matchKeyword))

function onSearch() {
  loadProducts()
}

// 商品分区（驱动模板循环渲染，消除重复模板代码）
const productSections = computed(() => [
  { key: 'flash', title: '七夕限时 · 情侣好物', filtered: filteredFlashProducts.value },
  { key: 'normal', title: '常规商品', filtered: filteredNormalProducts.value }
].filter(s => s.filtered.length > 0))

// ---------- 购物车（登录态持久化，后端 shop-cart 微服务） ----------
const cart = ref([])
const submitting = ref(false)

const cartTotalCount = computed(() =>
  cart.value.reduce((sum, it) => sum + it.qty, 0)
)

const subtotal = computed(() =>
  cart.value.reduce((sum, it) => sum + it.price * it.qty, 0)
)

// 从后端拉取购物车（含商品快照），登录态下使用
async function loadCart() {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    cart.value = []
    return
  }
  try {
    const data = await apiListCart(userId)
    if (data.success && Array.isArray(data.data)) {
      cart.value = data.data.map(mapCartItem)
    } else {
      cart.value = []
    }
  } catch (e) {
    cart.value = []
  }
}

// ---------- 优惠券 ----------
const availableCoupons = ref([])
const selectedCouponId = ref(null)

const discountAmount = computed(() => {
  if (selectedCouponId.value == null) return 0
  const c = availableCoupons.value.find(it => it.id === selectedCouponId.value)
  return c ? Number(c.discountAmount || 0) : 0
})

const payable = computed(() => {
  const v = subtotal.value - discountAmount.value
  return v > 0 ? v : 0
})

function couponOptionLabel(c) {
  if (c.type === 2) {
    const zhe = (Number(c.discount) * 10).toFixed(1).replace(/\.0$/, '')
    return `${c.name}（${zhe}折，减¥${Number(c.discountAmount || 0).toFixed(2)}）`
  }
  return `${c.name}（减¥${Number(c.discountAmount || 0).toFixed(2)}）`
}

function onCouponChange() {}

async function loadAvailableCoupons() {
  const userId = localStorage.getItem('userId')
  if (!userId || cart.value.length === 0) {
    availableCoupons.value = []
    selectedCouponId.value = null
    return
  }
  try {
    const res = await fetch(`/shop/coupon/user/available?userId=${userId}&orderAmount=${subtotal.value}`)
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      availableCoupons.value = data.data
      if (selectedCouponId.value != null
          && !data.data.find(it => it.id === selectedCouponId.value)) {
        selectedCouponId.value = null
      }
    } else {
      availableCoupons.value = []
      selectedCouponId.value = null
    }
  } catch (e) {
    availableCoupons.value = []
    selectedCouponId.value = null
  }
}

watch(subtotal, () => {
  loadAvailableCoupons()
})

function isInCart(id) {
  return cart.value.some(it => it.id === id)
}

// 加入购物车：登录后走后端持久化；未登录提示登录
async function addToCart(p, qty = 1) {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('请先登录后再加入购物车')
    return false
  }
  // 乐观更新：先改本地，失败再回滚
  const exist = cart.value.find(it => it.id === p.id)
  const beforeQty = exist ? exist.qty : 0
  if (exist) {
    exist.qty += qty
  } else {
    cart.value.push({ ...p, qty })
  }
  try {
    const data = await apiAddToCart(userId, p.id, qty)
    if (!data.success) {
      // 回滚
      if (exist) {
        exist.qty = beforeQty
      } else {
        const idx = cart.value.findIndex(it => it.id === p.id)
        if (idx >= 0) cart.value.splice(idx, 1)
      }
      alert(data.msg || '加入购物车失败')
      return false
    }
    return true
  } catch (e) {
    // 回滚
    if (exist) {
      exist.qty = beforeQty
    } else {
      const idx = cart.value.findIndex(it => it.id === p.id)
      if (idx >= 0) cart.value.splice(idx, 1)
    }
    alert('加入购物车请求失败：' + e.message)
    return false
  }
}

// ---------- 商品详情弹窗 ----------
const showDetail = ref(false)
const currentProduct = ref(null)
const detailQuantity = ref(1)

function openDetail(p) {
  currentProduct.value = p
  detailQuantity.value = 1
  showDetail.value = true
  document.body.style.overflow = 'hidden'
}

function closeDetail() {
  showDetail.value = false
  currentProduct.value = null
  document.body.style.overflow = ''
}

async function addDetailToCart() {
  if (!currentProduct.value) return
  const ok = await addToCart(currentProduct.value, detailQuantity.value)
  if (ok) closeDetail()
}

async function buyNow() {
  if (!currentProduct.value) return
  const ok = await addToCart(currentProduct.value, detailQuantity.value)
  if (!ok) return
  closeDetail()
  setTimeout(() => {
    document.querySelector('.cart-panel')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, 50)
}

// 删除购物车条目：先乐观移除本地，失败再恢复
async function removeFromCart(id) {
  const userId = localStorage.getItem('userId')
  if (!userId) return
  const idx = cart.value.findIndex(it => it.id === id)
  if (idx < 0) return
  const backup = cart.value[idx]
  cart.value.splice(idx, 1)
  try {
    const data = await apiRemoveFromCart(userId, id)
    if (!data.success) {
      cart.value.splice(idx, 0, backup)
      alert(data.msg || '移除失败')
    }
  } catch (e) {
    cart.value.splice(idx, 0, backup)
    alert('移除请求失败：' + e.message)
  }
}

// 增加数量：乐观更新本地 + 后端 setQty(原值+1)
async function increaseQty(item) {
  const userId = localStorage.getItem('userId')
  if (!userId) return
  const before = item.qty
  item.qty += 1
  try {
    const data = await apiUpdateCartQty(userId, item.id, item.qty)
    if (!data.success) {
      item.qty = before
      alert(data.msg || '修改数量失败')
    }
  } catch (e) {
    item.qty = before
    alert('修改数量请求失败：' + e.message)
  }
}

// 减少数量：qty>1 走 updateQty；qty==1 走删除
async function decreaseQty(item) {
  if (item.qty <= 1) {
    await removeFromCart(item.id)
    return
  }
  const userId = localStorage.getItem('userId')
  if (!userId) return
  const before = item.qty
  item.qty -= 1
  try {
    const data = await apiUpdateCartQty(userId, item.id, item.qty)
    if (!data.success) {
      item.qty = before
      alert(data.msg || '修改数量失败')
    }
  } catch (e) {
    item.qty = before
    alert('修改数量请求失败：' + e.message)
  }
}

async function handleCheckout() {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('请先登录')
    return
  }
  if (cart.value.length === 0) {
    alert('购物车为空')
    return
  }
  submitting.value = true
  try {
    // 收货地址（从 localStorage 读取默认地址，无则传空）
    let consignee = '', phone = '', address = ''
    try {
      const raw = localStorage.getItem('defaultAddress')
      if (raw) {
        const a = JSON.parse(raw)
        consignee = a.consignee || ''
        phone = a.phone || ''
        address = a.address || ''
      }
    } catch (e) { /* 忽略解析异常 */ }

    // 调用后端 shop-order 下单
    // 优惠券两阶段：此处仅校验不核销，支付成功后由后端统一核销
    const body = {
      userId: Number(userId),
      items: cart.value.map(it => ({ productId: it.id, quantity: it.qty })),
      userCouponId: selectedCouponId.value,
      consignee,
      phone,
      address
    }
    const res = await fetch('/shop/order', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.success) {
      const orderNo = data.data
      // 通知首页重新从后端加载订单
      window.dispatchEvent(new CustomEvent('order-created'))
      // 下单成功后，同步清空后端购物车中已下单商品
      const boughtProductIds = cart.value.map(it => it.id)
      try {
        await apiBatchDeleteCart(userId, boughtProductIds)
      } catch (e) { /* 后端清空失败不影响下单结果提示 */ }
      cart.value = []
      availableCoupons.value = []
      selectedCouponId.value = null
      alert(`下单成功！订单号 ${orderNo}，共 ${cartTotalCount.value} 件，应付 ¥${payable.value.toFixed(2)}（优惠 ¥${discountAmount.value.toFixed(2)}）`)
    } else {
      alert(data.msg || '下单失败')
    }
  } catch (e) {
    alert('下单请求失败：' + e.message)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProducts()
  loadCart()
  loadAvailableCoupons()
  window.addEventListener('coupon-released', onCouponReleased)
})

onUnmounted(() => {
  window.removeEventListener('coupon-released', onCouponReleased)
})

function onCouponReleased() {
  loadAvailableCoupons()
}
</script>

<style scoped>
/* ---------- 左右分栏 ---------- */
.layout {
  max-width: 1520px;
  margin: 0 auto;
  padding: 32px 36px 60px;
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 32px;
  align-items: start;
}

.left-col {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-col {
  display: flex;
  flex-direction: column;
  gap: 36px;
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .left-col {
    position: static;
  }
}

/* ---------- 通用面板 ---------- */
.panel {
  background: #fff;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 4px 20px var(--shadow-primary);
  border: 1px solid var(--color-surface);
}

.panel-title {
  font-size: 16px;
  font-weight: 800;
  color: #000;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.panel-title svg {
  width: 18px;
  height: 18px;
  color: var(--color-secondary);
}

/* ---------- 搜索面板 ---------- */
.search-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.search-box {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
}

.search-input {
  flex: 1 1 auto;
  min-width: 0;
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
  background: #fbfcff;
}

.search-input::placeholder {
  font-size: 13px;
}

.search-input:focus {
  border-color: var(--color-secondary);
  background: #fff;
}

.search-btn {
  flex-shrink: 0;
  height: 42px;
  padding: 0 14px;
  background: var(--color-primary-gradient);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  white-space: nowrap;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  box-shadow: 0 4px 12px var(--shadow-primary-md);
}

.search-btn svg {
  width: 14px;
  height: 14px;
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px var(--shadow-secondary);
}

/* ---------- 购物车 ---------- */
.cart-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--color-border);
}

.cart-count {
  font-size: 12px;
  background: var(--color-primary-gradient);
  color: #fff;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.cart-empty {
  padding: 28px 16px;
  text-align: center;
  color: #8b8676;
}

.cart-empty p {
  font-size: 14px;
  margin: 0 0 4px;
  font-weight: 600;
  color: #000;
}

.cart-empty small {
  font-size: 12px;
  color: #b2ac9b;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 380px;
  overflow-y: auto;
  padding-right: 4px;
}

.cart-list::-webkit-scrollbar {
  width: 5px;
}
.cart-list::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 5px;
}

.cart-item {
  display: grid;
  grid-template-columns: 48px 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  transition: background 0.2s ease;
}

.cart-item:hover {
  background: var(--color-soft);
}

.item-thumb {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  background: #eef0f4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-name {
  font-size: 13px;
  font-weight: 600;
  color: #000;
  line-height: 1.3;
  margin-bottom: 3px;
}

.item-price {
  font-size: 13px;
  color: #c0392b;
  font-weight: 700;
}

.item-qty {
  display: flex;
  align-items: center;
  gap: 2px;
  background: var(--color-soft);
  border-radius: 8px;
  padding: 2px;
}

.qty-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: var(--color-primary);
  font-size: 16px;
  font-weight: 700;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease, color 0.15s ease;
}

.qty-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}

.qty-num {
  min-width: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  color: #000;
}

.remove-btn {
  width: 26px;
  height: 26px;
  border: none;
  background: transparent;
  color: #b2ac9b;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease, color 0.15s ease;
}

.remove-btn svg {
  width: 14px;
  height: 14px;
}

.remove-btn:hover {
  background: #fdecea;
  color: #c62828;
}

.cart-summary {
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #000;
}

.summary-row.total {
  margin-top: 4px;
  padding-top: 10px;
  border-top: 1px solid var(--color-surface);
  font-size: 14px;
  font-weight: 700;
}

.summary-row .discount {
  color: var(--color-secondary);
  font-weight: 600;
}

.coupon-select {
  padding: 4px 8px;
  font-size: 12px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: #fff;
  color: var(--color-primary);
  font-weight: 600;
  cursor: pointer;
  max-width: 170px;
  outline: none;
}

.coupon-select:hover,
.coupon-select:focus {
  border-color: var(--color-secondary);
}

.no-coupon {
  font-size: 12px;
  color: #b2ac9b;
}

.total-price {
  font-size: 20px;
  font-weight: 900;
  color: #c0392b;
  letter-spacing: -0.5px;
}

.checkout-btn {
  width: 100%;
  height: 50px;
  margin-top: 4px;
  background: var(--color-primary-gradient);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: 0 8px 24px var(--shadow-primary);
  transition: transform 0.18s ease, box-shadow 0.25s ease, opacity 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.checkout-btn small {
  font-size: 13px;
  opacity: 0.88;
  font-weight: 600;
}

.checkout-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(59, 95, 196, 0.4);
}

.checkout-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  box-shadow: none;
}

/* ---------- 商品区（限时 + 常规） ---------- */
.product-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 4px;
}

.section-title {
  font-size: 22px;
  font-weight: 800;
  color: #000;
  letter-spacing: 1px;
  margin: 0;
}

.title-underline {
  width: 100%;
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-secondary) 100%, transparent 100%);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
}

@media (max-width: 1280px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}

.empty-hint {
  grid-column: 1 / -1;
  padding: 40px 0;
  text-align: center;
  color: #8b8676;
  font-size: 14px;
  background: #fff;
  border-radius: 14px;
  border: 1px dashed var(--color-border);
}

/* ---------- 商品卡片 ---------- */
.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 18px rgba(40, 36, 20, 0.07);
  transition: transform 0.25s cubic-bezier(.22,1,.36,1), box-shadow 0.25s ease, border-color 0.25s ease;
  border: 2px solid transparent;
  display: flex;
  flex-direction: column;
}

.product-card-clickable {
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 36px rgba(40, 36, 20, 0.14);
}

.product-card.selected {
  border-color: var(--color-secondary);
  box-shadow: 0 10px 32px rgba(59, 95, 196, 0.18);
}

.product-cover {
  position: relative;
  aspect-ratio: 5 / 4;
  overflow: hidden;
}

.cover-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.5s ease;
  background: #ffffff;
  overflow: hidden;
}

.product-card:hover .cover-inner {
  transform: scale(1.05);
}

.pimg-real {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  display: block;
  padding: 8px;
  background: #ffffff;
  box-sizing: border-box;
  transition: transform 0.5s ease;
}

.product-card:hover .pimg-real {
  transform: scale(1.03);
}

.pimg-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #eceff3 0%, #dfe3ea 100%);
  color: #95a0b0;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.05em;
  user-select: none;
}

.cover-inner.product-cover-img .pimg-placeholder {
  font-size: 48px;
}

.item-thumb .pimg-placeholder {
  font-size: 22px;
}

.cover-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  background: var(--color-primary-gradient);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  border-radius: 999px;
  letter-spacing: 1px;
  box-shadow: 0 4px 10px rgba(30, 58, 138, 0.35);
}

.cover-badge.flash-badge {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  box-shadow: 0 4px 10px rgba(239, 68, 68, 0.35);
}

.product-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.product-body-simple {
  padding: 12px 14px 14px;
  gap: 6px;
}

.product-name {
  font-size: 15px;
  font-weight: 700;
  color: #000;
  line-height: 1.4;
  margin: 0;
}

.product-name-simple {
  font-size: 14px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-align: center;
}

.product-desc {
  font-size: 12px;
  color: #8b8676;
  line-height: 1.5;
  margin: 0;
  flex: 1;
}

.product-foot {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 10px;
  margin-top: 2px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex-wrap: wrap;
}

.price-row-center {
  justify-content: center;
}

.currency {
  font-size: 12px;
  font-weight: 700;
  color: #c0392b;
}

.price {
  font-size: 20px;
  font-weight: 900;
  color: #c0392b;
  letter-spacing: -0.5px;
}

.original {
  font-size: 11px;
  color: #b2ac9b;
  text-decoration: line-through;
  margin-left: 4px;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 12px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
  flex-shrink: 0;
}

.add-btn svg {
  width: 13px;
  height: 13px;
}

.add-btn:hover {
  background: var(--color-secondary);
  transform: translateY(-1px);
}

/* ===== 商品详情弹窗 ===== */
.detail-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 15, 35, 0.55);
  backdrop-filter: blur(3px);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  animation: maskIn 0.18s ease;
}
@keyframes maskIn { from { opacity: 0 } to { opacity: 1 } }

.detail-modal {
  position: relative;
  width: 100%;
  max-width: 920px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 40px 80px rgba(0, 0, 0, 0.3);
  animation: modalIn 0.22s cubic-bezier(0.2, 0.9, 0.3, 1.2);
}
@keyframes modalIn {
  from { opacity: 0; transform: translateY(16px) scale(0.97) }
  to   { opacity: 1; transform: translateY(0)    scale(1) }
}

.detail-close {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 2;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.06);
  color: #444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease, color 0.15s ease;
}
.detail-close:hover {
  background: #c0392b;
  color: #fff;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  min-height: 520px;
}

.detail-cover {
  position: relative;
  background: #fafafa;
  min-height: 400px;
}
.detail-cover :deep(.cover-inner),
.detail-cover :deep(.item-thumb) {
  width: 100%;
  height: 100%;
  min-height: 520px;
  background: #fff;
  overflow: hidden;
}
.detail-cover :deep(.pimg-real) {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 32px;
  background: #fff;
  box-sizing: border-box;
}

.detail-badge {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 2;
  padding: 5px 14px;
  background: var(--color-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  letter-spacing: 0.5px;
}
.detail-badge.flash-badge {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.detail-info {
  padding: 36px 38px 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-name {
  font-size: 22px;
  font-weight: 800;
  color: #111;
  line-height: 1.35;
  margin: 0;
  padding-right: 30px;
}

.detail-price-block {
  background: linear-gradient(135deg, #fff5f2, #fff);
  border: 1px solid #ffe0d5;
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}
.detail-price-now {
  display: flex;
  align-items: baseline;
}
.detail-currency {
  font-size: 16px;
  font-weight: 700;
  color: #c0392b;
}
.detail-price-value {
  font-size: 30px;
  font-weight: 900;
  color: #c0392b;
  letter-spacing: -0.5px;
}
.detail-original {
  font-size: 14px;
  color: #b2ac9b;
  text-decoration: line-through;
}
.detail-discount {
  background: #c0392b;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
  margin-left: auto;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.detail-section-title {
  font-size: 13px;
  font-weight: 700;
  color: #666;
  padding-left: 10px;
  border-left: 3px solid var(--color-primary);
}
.detail-desc {
  font-size: 14px;
  line-height: 1.7;
  color: #333;
  margin: 0;
  background: #f7f8fa;
  padding: 12px 16px;
  border-radius: 10px;
}
.detail-rules {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.9;
  color: #555;
}

.detail-qty {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.qty-btn {
  width: 34px;
  height: 34px;
  border: 1px solid #d8dce6;
  background: #fff;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #333;
  cursor: pointer;
  transition: all 0.15s ease;
}
.qty-btn:not(:disabled):hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.qty-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.qty-num {
  font-size: 16px;
  font-weight: 700;
  min-width: 40px;
  text-align: center;
  color: #111;
}
.detail-subtotal {
  margin-left: auto;
  font-size: 13px;
  color: #666;
}
.detail-subtotal b {
  color: #c0392b;
  font-size: 16px;
  font-weight: 800;
  margin-left: 4px;
}

.detail-actions {
  margin-top: auto;
  padding-top: 10px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.btn-cart,
.btn-buy {
  height: 48px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}
.btn-cart {
  background: #fff;
  border: 2px solid var(--color-primary);
  color: var(--color-primary);
}
.btn-cart:hover {
  background: var(--color-primary);
  color: #fff;
}
.btn-buy {
  background: linear-gradient(135deg, #c0392b, #e74c3c);
  color: #fff;
  box-shadow: 0 8px 24px rgba(192, 57, 43, 0.35);
}
.btn-buy:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 32px rgba(192, 57, 43, 0.45);
}

@media (max-width: 720px) {
  .detail-layout { grid-template-columns: 1fr }
  .detail-cover :deep(.cover-inner) { min-height: 300px }
  .detail-info { padding: 24px 20px 28px }
  .detail-name { font-size: 18px }
  .detail-price-value { font-size: 26px }
  .detail-actions { grid-template-columns: 1fr }
}

@media (max-width: 1100px) {
  .layout {
    padding: 24px 20px 40px;
  }
}
</style>