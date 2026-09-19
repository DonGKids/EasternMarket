<template>
  <nav class="navbar">
    <div class="nav-left">
      <!-- 用户头像 + 下拉菜单（最左侧） -->
      <div v-if="isLoggedIn" class="user-menu" @click.stop>
        <div class="user-avatar" @click="toggleDropdown">
          <img v-if="userAvatar" :src="userAvatar" alt="头像">
          <span v-else class="avatar-placeholder">{{ avatarText }}</span>
        </div>
        <span class="user-nickname" @click="toggleDropdown">{{ displayName }}</span>

        <!-- 下拉菜单 -->
        <div v-if="dropdownOpen" class="dropdown">
          <div class="dropdown-header">
            <span class="user-mail">{{ userMail }}</span>
          </div>
          <div class="dropdown-item" @click="goProfile">个人中心</div>
          <div class="dropdown-item" @click="goChangePassword">修改密码</div>
          <div class="dropdown-item" @click="openThemePanel">自定义主题</div>
          <div class="dropdown-divider"></div>
          <div class="dropdown-item danger" @click="handleLogout">退出</div>
        </div>
      </div>

      <div v-else class="user-menu">
        <router-link to="/login" class="nav-link">登录</router-link>
      </div>
    </div>

    <div class="nav-right">
      <!-- 导航链接 -->
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-link"
        active-class="active"
      >{{ item.label }}</router-link>
    </div>
  </nav>

  <!-- 主题选择面板（放在 nav 外面，避免被 stacking context 遮挡） -->
  <Teleport to="body">
    <div v-if="themePanelOpen" class="theme-overlay" @click="closeThemePanel">
      <div class="theme-panel" @click.stop>
        <div class="theme-panel-header">
          <span class="theme-panel-title">自定义主题</span>
          <span class="theme-panel-close" @click="closeThemePanel">✕</span>
        </div>
        <div class="theme-panel-body">
          <div
            v-for="t in themes"
            :key="t.key"
            class="theme-card"
            :class="{ active: currentTheme === t.key }"
            @click="selectTheme(t.key)"
          >
            <div class="theme-card-swatch" :style="t.swatchStyle">
              <span class="theme-card-name">{{ t.name }}</span>
            </div>
            <div class="theme-card-desc">{{ t.desc }}</div>
            <div v-if="currentTheme === t.key" class="theme-card-badge">当前</div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { resolveImgUrl } from '../utils/img'

const router = useRouter()

const navItems = [
  { path: '/home', label: '首页' },
  { path: '/market', label: '市场' },
  { path: '/activity', label: '活动中心' },
  { path: '/tutorial', label: '使用教程' }
]

// ---------- 主题切换 ----------
const themes = [
  {
    key: 'celadon',
    name: '青瓷',
    desc: '清雅淡蓝，温润如玉',
    swatchStyle: 'background: linear-gradient(135deg, #1e3a8a 0%, #3b5fc4 50%, #eef3fc 100%);'
  },
  {
    key: 'bamboo',
    name: '竹叶',
    desc: '青翠欲滴，自然清新',
    swatchStyle: 'background: linear-gradient(135deg, #2d6a4f 0%, #52b788 50%, #e8f5e9 100%);'
  },
  {
    key: 'peach',
    name: '桃夭',
    desc: '灼灼其华，浪漫甜蜜',
    swatchStyle: 'background: linear-gradient(135deg, #c04060 0%, #e88c9f 50%, #fdeef2 100%);'
  }
]

const themePanelOpen = ref(false)
const currentTheme = ref('celadon')

function openThemePanel() {
  dropdownOpen.value = false
  themePanelOpen.value = true
}

function closeThemePanel() {
  themePanelOpen.value = false
}

function selectTheme(key) {
  // 只有登录后才能保存主题选择
  if (!isLoggedIn.value) return
  currentTheme.value = key
  document.documentElement.setAttribute('data-theme', key)
  localStorage.setItem('theme', key)
  // 通知其他组件主题已变更
  window.dispatchEvent(new CustomEvent('theme-changed', { detail: key }))
}

function initTheme() {
  const saved = isLoggedIn.value
    ? (localStorage.getItem('theme') || 'celadon')
    : 'celadon'
  currentTheme.value = saved
  document.documentElement.setAttribute('data-theme', saved)
}

// 用一个响应式 key 强制刷新读取 localStorage
const refreshKey = ref(0)

const isLoggedIn = computed(() => { refreshKey.value; return !!localStorage.getItem('token') })
const userMail = computed(() => { refreshKey.value; return localStorage.getItem('mail') || '' })
const userAvatar = computed(() => {
  refreshKey.value
  const raw = localStorage.getItem('avatar') || ''
  return resolveImgUrl(raw)
})
const userNickname = computed(() => { refreshKey.value; return localStorage.getItem('nickname') || '' })

const displayName = computed(() => userNickname.value || userMail.value || '用户')

const avatarText = computed(() => {
  const text = userNickname.value || userMail.value
  if (!text) return 'U'
  return text.charAt(0).toUpperCase()
})

function onStorageChange() {
  refreshKey.value++
  // 强制重新读取 localStorage 中的头像 URL（兼容旧格式需要 vite 代理）
  userAvatar.value
}

const dropdownOpen = ref(false)

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function closeDropdown() {
  dropdownOpen.value = false
}

function handleClickOutside() {
  if (dropdownOpen.value) dropdownOpen.value = false
}

function goProfile() {
  closeDropdown()
  router.push('/profile')
}

function goChangePassword() {
  closeDropdown()
  router.push('/changePassword')
}

function handleLogout() {
  closeDropdown()
  localStorage.removeItem('token')
  localStorage.removeItem('mail')
  localStorage.removeItem('avatar')
  localStorage.removeItem('nickname')
  // 退出后重置主题为青瓷（未登录不允许使用自定义主题）
  currentTheme.value = 'celadon'
  document.documentElement.setAttribute('data-theme', 'celadon')
  router.push('/login')
}

onMounted(() => {
  initTheme()
  window.addEventListener('storage', onStorageChange)
  window.addEventListener('avatar-updated', onStorageChange)
  window.addEventListener('nickname-updated', onStorageChange)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('storage', onStorageChange)
  window.removeEventListener('avatar-updated', onStorageChange)
  window.removeEventListener('nickname-updated', onStorageChange)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 36px;
  height: 68px;
  background: var(--color-bg-grad);
  box-shadow: 0 4px 24px var(--shadow-primary);
  border-bottom: 2px solid;
  border-image: var(--color-border-grad) 1;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: flex-end;
}

/* 导航链接 */
.nav-link {
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  text-decoration: none;
  border-radius: 8px;
  transition: color 0.2s ease, background 0.2s ease;
  cursor: pointer;
}

.nav-link:hover {
  color: var(--color-primary);
  background: var(--color-soft);
}

.nav-link.active {
  color: var(--color-primary);
  background: var(--color-soft);
  font-weight: 700;
}

/* 用户菜单 */
.user-menu {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  border: 2px solid var(--color-border);
  transition: border-color 0.25s ease, transform 0.2s ease;
  background: var(--color-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar:hover {
  border-color: var(--color-primary);
  transform: scale(1.05);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: var(--color-primary);
  font-size: 15px;
  font-weight: 700;
}

.user-nickname {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  cursor: pointer;
  transition: color 0.2s ease;
  user-select: none;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 1;
  min-width: 0;
}

.user-nickname:hover {
  color: var(--color-primary);
}

/* 下拉菜单 */
.dropdown {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  min-width: 220px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.22);
  overflow: hidden;
  z-index: 1000;
  animation: dropIn 0.18s cubic-bezier(.22,1,.36,1);
}

@keyframes dropIn {
  from {
    opacity: 0;
    transform: translateY(-8px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dropdown-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, var(--color-soft) 0%, var(--color-surface) 100%);
  border-bottom: 1px solid var(--color-border);
}

.user-mail {
  font-size: 13px;
  color: var(--color-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 260px;
}

.dropdown-item {
  padding: 11px 18px;
  font-size: 14px;
  color: #000;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, padding 0.2s ease;
}

.dropdown-item:hover {
  background: var(--color-soft);
  color: var(--color-primary);
  padding-left: 22px;
}

.dropdown-item.danger {
  color: var(--color-danger);
}

.dropdown-item.danger:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
  margin: 4px 0;
}

/* 主题选择面板 */
.theme-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.theme-panel {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
  width: 520px;
  max-width: 92vw;
  overflow: hidden;
  animation: panelIn 0.25s cubic-bezier(.22, 1, .36, 1);
}

@keyframes panelIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.theme-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.theme-panel-title {
  font-size: 18px;
  font-weight: 800;
  color: #000;
}

.theme-panel-close {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}

.theme-panel-close:hover {
  background: #f0f0f0;
  color: #000;
}

.theme-panel-body {
  padding: 24px;
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.theme-card {
  width: 140px;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
  position: relative;
  background: #f9f9f9;
}

.theme-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.theme-card.active {
  border-color: var(--color-primary);
}

.theme-card-swatch {
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-card-name {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.theme-card-desc {
  padding: 10px 12px;
  font-size: 12px;
  color: #666;
  text-align: center;
}

.theme-card-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 780px) {
  .navbar {
    padding: 0 18px;
  }
  .nav-link {
    padding: 6px 10px;
    font-size: 13px;
  }
  .nav-left {
    gap: 16px;
  }
}
</style>
