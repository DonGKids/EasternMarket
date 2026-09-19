<template>
  <div class="login-bg">
    <div class="login-overlay">
      <div class="title">
        <h1>欢迎回来！请登录</h1>
      </div>

      <div class="container">
        <div class="right">
          <div class="formBox">
            <form @submit.prevent="handleLogin">
              <p>email</p>
              <label>
                <input type="email" v-model="email" placeholder="请输入邮箱地址" required>
              </label>

              <p>password</p>
              <label>
                <input type="password" v-model="password" placeholder="请输入密码" required>
              </label>

              <input type="submit" value="登录" class="submit-btn" :disabled="submitting">

              <router-link to="/register" class="register-link">
                还没有账号？去注册
              </router-link>

              <div v-if="tipMsg" :class="['tip-msg', tipType]">{{ tipMsg }}</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { resolveImgUrl } from '../utils/img'

const router = useRouter()
const email = ref('')
const password = ref('')
const submitting = ref(false)
const tipMsg = ref('')
const tipType = ref('') // 'ok' | 'err'

function clearTip() {
  tipMsg.value = ''
  tipType.value = ''
}

function showTip(msg, type) {
  tipMsg.value = msg
  tipType.value = type
}

async function handleLogin() {
  clearTip()

  if (!email.value || !password.value) {
    showTip('请填写邮箱和密码', 'err')
    return
  }
  const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!reg.test(email.value)) {
    showTip('邮箱格式不正确', 'err')
    return
  }

  submitting.value = true
  try {
    const params = new URLSearchParams()
    params.append('mail', email.value)
    params.append('password', password.value)

    const res = await fetch('/shop/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params
    })
    const data = await res.json()
    if (data.success) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('mail', data.mail)
      if (data.userId != null) {
        localStorage.setItem('userId', data.userId)
      }

      // 登录成功后立即获取用户资料（头像、昵称等）并存储
      try {
        const profileRes = await fetch(`/shop/user/getProfile?mail=${encodeURIComponent(email.value)}`)
        const profileData = await profileRes.json()
        if (profileData.success && profileData.data) {
          if (profileData.data.userId != null) {
            localStorage.setItem('userId', profileData.data.userId)
          }
          if (profileData.data.avatar) {
            localStorage.setItem('avatar', profileData.data.avatar)
          }
          if (profileData.data.nickname) {
            localStorage.setItem('nickname', profileData.data.nickname)
          }
        }
      } catch (e) {
        // 获取资料失败不影响登录主流程
      }

      window.dispatchEvent(new Event('avatar-updated'))
      window.dispatchEvent(new Event('nickname-updated'))
      showTip('登录成功', 'ok')
      setTimeout(() => router.push('/home'), 800)
    } else {
      showTip(data.msg || '登录失败', 'err')
    }
  } catch (e) {
    showTip('请求失败：' + e.message, 'err')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  background: var(--page-bg) center / cover no-repeat;
  font-family: 'Noto Sans SC', 'Helvetica Neue', Arial, sans-serif;
  position: relative;
}

.login-overlay {
  min-height: 100vh;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(5px);
}

.title {
  text-align: center;
  padding: 50px 0 20px;
}

.title h1 {
  margin: 0;
  color: var(--color-primary);
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* 瓷白卡片 + 顶部青花蓝色装饰带 */
.container {
  width: 45%;
  min-width: 360px;
  min-height: 400px;
  background: #fff;
  margin: 0 auto;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  border-radius: 14px;
  box-shadow: 0 15px 40px rgba(30, 58, 138, 0.18);
  overflow: hidden;
}

.right {
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
}

.formBox {
  width: 100%;
  padding: 45px 40px;
  box-sizing: border-box;
  min-height: 400px;
  background: #fff;
  display: flex;
  align-items: center;
}

.formBox form {
  width: 100%;
}

.formBox p {
  margin: 0 0 8px;
  font-weight: bold;
  color: var(--color-primary);
  font-family: 'Jost', sans-serif;
  font-size: 15px;
}

.formBox input[type="email"],
.formBox input[type="password"] {
  width: 100%;
  border: none;
  border-bottom: 2px solid var(--color-border);
  outline: none;
  height: 36px;
  font-size: 14px;
  margin-bottom: 24px;
  padding: 0;
  background: transparent;
  transition: border-color 0.25s ease;
}

.formBox input[type="email"]:focus,
.formBox input[type="password"]:focus {
  border-bottom: 2px solid var(--color-primary);
}

.submit-btn {
  width: 100%;
  border: none;
  outline: none;
  height: 42px;
  color: #fff;
  background: var(--color-primary-gradient);
  border-radius: 10px;
  cursor: pointer;
  margin-top: 10px;
  font-size: 15px;
  font-family: 'Noto Sans SC', sans-serif;
  letter-spacing: 2px;
  box-shadow: 0 6px 18px var(--shadow-primary-md);
  transition: transform 0.18s ease, box-shadow 0.25s ease, opacity 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(59, 95, 196, 0.4);
}

.submit-btn:disabled {
  background: var(--color-border);
  cursor: not-allowed;
  box-shadow: none;
}

.register-link {
  color: #000;
  font-size: 13px;
  font-weight: 500;
  display: inline-block;
  margin-top: 18px;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: var(--color-primary);
}

/* 页面提示 */
.tip-msg {
  margin-top: 14px;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 13px;
  text-align: center;
}
.tip-msg.ok { background: #e6f7e6; color: #2e7d32; }
.tip-msg.err { background: #fdecea; color: #c62828; }

@media (max-width: 768px) {
  .container {
    width: 90%;
  }

  .title h1 {
    font-size: 22px;
  }
}
</style>
