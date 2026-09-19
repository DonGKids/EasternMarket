<template>
  <div class="page-shell activity">
    <Navbar />

    <div class="layout">
      <!-- 左侧：活动列表（从后端拉取，按 category 分常驻/限时） -->
      <aside class="sidebar">
        <div class="sidebar-section">
          <h3 class="section-label">常驻活动</h3>
          <div v-if="loading" class="sidebar-tip">加载中…</div>
          <button
            v-for="act in permanentActivities"
            :key="act.id"
            class="activity-btn"
            :class="{ active: selectedId === act.id }"
            @click="selectActivity(act.id)"
          >
            <span class="activity-name">{{ act.name }}</span>
          </button>
        </div>

        <div class="sidebar-section">
          <h3 class="section-label">限时活动</h3>
          <button
            v-for="act in limitedActivities"
            :key="act.id"
            class="activity-btn"
            :class="{ active: selectedId === act.id }"
            @click="selectActivity(act.id)"
          >
            <span class="activity-name">{{ act.name }}</span>
            <span v-if="getDateLabel(act)" class="date-tag">{{ getDateLabel(act) }}</span>
          </button>
        </div>
      </aside>

      <!-- 右侧：活动详情 + 领券 -->
      <main class="detail-panel">
        <div class="detail-card" v-if="selected">
          <div class="detail-header">
            <div class="detail-title-wrap">
              <span class="detail-badge">{{ categoryLabel(selected.category) }}</span>
              <h2 class="detail-title">{{ selected.name }}</h2>
            </div>
            <div class="detail-date" v-if="getDateLabel(selected)">
              <span class="date-label">活动时间</span>
              <span class="date-value">{{ getDateLabel(selected) }}</span>
            </div>
          </div>

          <p class="detail-desc">{{ selected.description }}</p>

          <!-- 可领优惠券（来自后端，按 get_way 分模块展示） -->
          <div class="rewards-section" v-if="!couponLoading && receivableCoupons.length > 0">
            <div
              v-for="group in couponGroups"
              :key="group.way"
              class="coupon-group"
            >
              <div class="group-header">
                <span class="group-icon">{{ group.icon }}</span>
                <h3 class="group-title">{{ group.title }}</h3>
                <span class="group-desc">{{ group.desc }}</span>
              </div>

              <div v-if="group.list.length === 0" class="rewards-tip">暂无</div>
              <div v-else class="rewards-grid">
                <div
                  v-for="c in group.list"
                  :key="c.id"
                  class="reward-item"
                  :class="{ 'is-claimed': isClaimed(c.id), 'is-soldout': isSoldOut(c) }"
                >
                  <div class="reward-amount">{{ faceValueText(c) }}</div>
                  <div class="reward-condition">{{ thresholdText(c) }}</div>
                  <div class="reward-name">{{ c.name }}</div>

                  <!-- 新人专享：不可主动领，注册自动发，按钮显示状态 -->
                  <button
                    v-if="group.way === 1"
                    class="reward-claim-btn"
                    :disabled="true"
                  >
                    <span v-if="isClaimed(c.id)">已领取</span>
                    <span v-else>注册自动发放</span>
                  </button>

                  <!-- 签到领取：占位（未接入签到逻辑） -->
                  <button
                    v-else-if="group.way === 4"
                    class="reward-claim-btn"
                    :disabled="true"
                  >
                    <span>签到领取</span>
                  </button>

                  <!-- 限时抢券 / 邀请奖励：可主动领取 -->
                  <button
                    v-else
                    class="reward-claim-btn"
                    :disabled="isClaimed(c.id) || isSoldOut(c) || claimingId === c.id"
                    @click="handleClaim(c)"
                  >
                    <span v-if="claimingId === c.id">领取中…</span>
                    <span v-else-if="isClaimed(c.id)">已领取</span>
                    <span v-else-if="isSoldOut(c)">已领完</span>
                    <span v-else>立即领取</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载 / 空状态 -->
          <div class="rewards-section" v-else>
            <h3 class="rewards-title">可领优惠券</h3>
            <div v-if="couponLoading" class="rewards-tip">加载中…</div>
            <div v-else class="rewards-tip">暂无可领优惠券</div>
          </div>

          <!-- 活动规则 -->
          <div class="rules-section" v-if="parsedRules.length > 0">
            <h4 class="rules-title">活动规则</h4>
            <ul class="rules-list">
              <li v-for="(rule, idx) in parsedRules" :key="idx">{{ rule }}</li>
            </ul>
          </div>
        </div>

        <div v-else class="detail-empty">
          <p>请在左侧选择一个活动查看详情</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Navbar from '../components/Navbar.vue'

// ---------- 活动列表（从后端拉取） ----------
const allActivities = ref([])
const loading = ref(false)
const selectedId = ref(null)

// 按分类拆分：1 常驻 / 2 限时
const permanentActivities = computed(() =>
  allActivities.value.filter(a => Number(a.category) === 1)
)
const limitedActivities = computed(() =>
  allActivities.value.filter(a => Number(a.category) === 2)
)

const selected = computed(() =>
  allActivities.value.find(a => a.id === selectedId.value)
)

// 分类文案
function categoryLabel(category) {
  return Number(category) === 2 ? '限时' : '常驻'
}

// 活动日期标签映射
function getDateLabel(act) {
  if (!act.endTime) {
    const name = act.name || ''
    if (name.includes('会员日')) return '每月11号'
    return ''
  }
  const fmt = d => d ? d.slice(5, 10).replace('-', '.') : ''
  return ` - `
}

// 解析活动规则（后端存的是 JSON 数组字符串）
const parsedRules = computed(() => {
  if (!selected.value || !selected.value.rules) return []
  try {
    const arr = JSON.parse(selected.value.rules)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
})

function selectActivity(id) {
  selectedId.value = id
}

async function loadActivities() {
  loading.value = true
  try {
    const res = await fetch('/shop/coupon/activity/list')
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      allActivities.value = data.data
      // 默认选第一个
      if (allActivities.value.length > 0 && selectedId.value == null) {
        selectedId.value = allActivities.value[0].id
      }
    } else {
      allActivities.value = []
    }
  } catch (e) {
    allActivities.value = []
  } finally {
    loading.value = false
  }
}

// ---------- 右侧领券（按选中活动关联的券展示） ----------
const receivableCoupons = ref([])
const couponLoading = ref(false)
const claimingId = ref(null)
const claimedTemplateIds = ref(new Set())

// 按 get_way 分组的配置
// get_way: 1 新人专享, 2 限时抢券, 3 邀请奖励, 4 签到领取
const groupConfig = [
  { way: 1, title: '新人专享', icon: '🎁', desc: '注册自动发放' },
  { way: 2, title: '限时抢券', icon: '⚡', desc: '手快有手慢无' },
  { way: 3, title: '邀请奖励', icon: '👥', desc: '邀好友得券' },
  { way: 4, title: '签到领取', icon: '📅', desc: '每日签到兑换' }
]

// 计算分组后的列表（过滤掉没有券的分组）
const couponGroups = computed(() => {
  return groupConfig
    .map(cfg => ({
      ...cfg,
      list: receivableCoupons.value.filter(c => Number(c.getWay) === cfg.way)
    }))
    .filter(g => g.list.length > 0)
})

function isClaimed(templateId) {
  return claimedTemplateIds.value.has(templateId)
}

function isSoldOut(t) {
  return t && t.totalCount !== 0 && t.receivedCount >= t.totalCount
}

function faceValueText(t) {
  if (!t) return ''
  if (t.type === 2) {
    return (Number(t.discount) * 10).toFixed(1).replace(/\.0$/, '') + '折'
  }
  return '¥' + Number(t.faceValue || 0).toFixed(0)
}

function thresholdText(t) {
  if (!t) return ''
  if (t.type === 3) return '无门槛立减'
  if (t.threshold && Number(t.threshold) > 0) {
    return `满 ${Number(t.threshold).toFixed(0)} 元可用`
  }
  return '无门槛'
}

// 拉取选中活动关联的券
async function loadCouponsByActivity(activityId) {
  if (activityId == null) {
    receivableCoupons.value = []
    return
  }
  couponLoading.value = true
  try {
    const res = await fetch(`/shop/coupon/activity/${activityId}/coupons`)
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      receivableCoupons.value = data.data
    } else {
      receivableCoupons.value = []
    }
  } catch (e) {
    receivableCoupons.value = []
  } finally {
    couponLoading.value = false
  }
}

// 选中活动变化时重新拉券
watch(selectedId, (id) => {
  loadCouponsByActivity(id)
})

async function loadClaimedStatus() {
  const userId = localStorage.getItem('userId')
  if (!userId) return
  try {
    const res = await fetch(`/shop/coupon/user/mine?userId=${userId}`)
    const data = await res.json()
    if (data.success && Array.isArray(data.data)) {
      claimedTemplateIds.value = new Set(data.data.map(uc => uc.templateId))
    }
  } catch (e) {
    // 忽略
  }
}

async function handleClaim(coupon) {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('请先登录')
    return
  }

  claimingId.value = coupon.id
  try {
    const res = await fetch(
      `/shop/coupon/user/receive?userId=${userId}&templateId=${coupon.id}`,
      { method: 'POST' }
    )
    const data = await res.json()
    if (data.success) {
      claimedTemplateIds.value.add(coupon.id)
      coupon.receivedCount = (coupon.receivedCount || 0) + 1
      alert(`领取成功：${coupon.name}，去首页查看吧`)
    } else {
      alert(data.msg || '领取失败')
      await loadCouponsByActivity(selectedId.value)
      await loadClaimedStatus()
    }
  } catch (e) {
    alert('请求失败：' + e.message)
  } finally {
    claimingId.value = null
  }
}

onMounted(() => {
  loadActivities()
  loadClaimedStatus()
})
</script>

<style scoped>
.layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 36px 60px;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 28px;
  align-items: start;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

/* ---------- 左侧活动列表 ---------- */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-section {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  box-shadow: 0 8px 28px var(--shadow-primary);
  padding: 18px 14px;
}

.section-label {
  font-size: 14px;
  font-weight: 700;
  color: #000;
  margin: 0 4px 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--color-border);
}

.activity-btn {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  margin-bottom: 6px;
  border: 1.5px solid transparent;
  border-radius: 10px;
  background: var(--color-bg);
  cursor: pointer;
  text-align: left;
  font-size: 14px;
  color: #000;
  font-weight: 600;
  transition: all 0.2s ease;
}

.activity-btn:last-child {
  margin-bottom: 0;
}

.activity-btn:hover {
  background: var(--color-soft);
  color: var(--color-primary);
}

.activity-btn.active {
  background: var(--color-primary-gradient);
  color: #fff;
  border-color: var(--color-primary);
  box-shadow: 0 4px 14px rgba(30, 58, 138, 0.3);
}

.activity-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.date-tag {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-soft);
  padding: 2px 8px;
  border-radius: 999px;
  flex-shrink: 0;
  white-space: nowrap;
}

.activity-btn.active .date-tag {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

/* ---------- 右侧详情 ---------- */
.detail-panel {
  min-height: 500px;
}

.detail-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  box-shadow: 0 8px 28px var(--shadow-primary);
  padding: 32px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px dashed var(--color-border);
  margin-bottom: 18px;
}

.detail-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 999px;
  background: var(--color-primary-gradient);
  color: #fff;
  letter-spacing: 0.5px;
}

.detail-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #000;
  letter-spacing: 0.5px;
}

.detail-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  background: var(--color-soft);
  color: var(--color-primary);
  padding: 6px 14px;
  border-radius: 8px;
  flex-shrink: 0;
}

.detail-date .date-label {
  font-size: 12px;
  opacity: 0.7;
  font-weight: 500;
}

.detail-date .date-value {
  font-weight: 700;
}

.detail-desc {
  font-size: 14px;
  color: #4a5a78;
  line-height: 1.7;
  margin: 0 0 24px;
}

/* ---------- 可领优惠券 ---------- */
.rewards-section {
  margin-bottom: 24px;
}

.rewards-title {
  font-size: 15px;
  font-weight: 700;
  color: #000;
  margin: 0 0 14px;
}

/* 分组容器 */
.coupon-group {
  margin-bottom: 22px;
  padding: 16px 18px;
  background: #fbfcfe;
  border: 1px solid #eaeef7;
  border-radius: 12px;
}

.coupon-group:last-child {
  margin-bottom: 0;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #dbe1ee;
}

.group-icon {
  font-size: 18px;
  line-height: 1;
}

.group-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-primary);
  margin: 0;
}

.group-desc {
  font-size: 12px;
  color: #8b8676;
  margin-left: auto;
}

.rewards-tip {
  font-size: 13px;
  color: #b2ac9b;
  padding: 20px 0;
  text-align: center;
}

.rewards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}

.reward-item {
  background: linear-gradient(135deg, var(--color-bg) 0%, var(--color-soft) 100%);
  border: 1px solid var(--color-surface);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.reward-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(30, 58, 138, 0.12);
}

.reward-item.is-claimed,
.reward-item.is-soldout {
  opacity: 0.6;
}

.reward-amount {
  font-size: 28px;
  font-weight: 900;
  color: var(--color-primary);
  letter-spacing: -0.5px;
  line-height: 1;
}

.reward-condition {
  font-size: 12px;
  color: #4a5a78;
  margin-top: 4px;
}

.reward-name {
  font-size: 13px;
  font-weight: 600;
  color: #000;
  margin-top: 6px;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reward-claim-btn {
  width: 100%;
  padding: 6px 0;
  border: none;
  border-radius: 8px;
  background: var(--color-primary-gradient);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.reward-claim-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3);
}

.reward-claim-btn:disabled {
  background: #b2ac9b;
  cursor: not-allowed;
  box-shadow: none;
}

/* ---------- 活动规则 ---------- */
.rules-section {
  background: var(--color-bg);
  border-radius: 12px;
  padding: 18px 20px;
}

.rules-title {
  font-size: 14px;
  font-weight: 700;
  color: #000;
  margin: 0 0 10px;
}

.rules-list {
  margin: 0;
  padding-left: 20px;
  color: #4a5a78;
  font-size: 13px;
  line-height: 1.8;
}

.rules-list li {
  margin-bottom: 2px;
}

/* ---------- 空状态 ---------- */
.detail-empty {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  box-shadow: 0 8px 28px var(--shadow-primary);
  padding: 80px 32px;
  text-align: center;
  color: #8b8676;
  font-size: 14px;
}

@media (max-width: 900px) {
  .layout {
    padding: 24px 20px 40px;
  }
  .detail-header {
    flex-direction: column;
  }
}
</style>