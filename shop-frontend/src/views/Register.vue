<template>
  <div class="register-bg">
    <div class="register-overlay">
      <!-- 标题 -->
      <div class="title">
        <h1>欢迎使用！请输入一个邮箱以继续</h1>
      </div>

      <!-- 主容器：左右分栏 -->
      <div class="container">
        <div class="right">
          <div class="formBox">
            <form @submit.prevent="handleSubmit">
              <p>email</p>
              <label>
                <input
                  type="email"
                  v-model="email"
                  placeholder="请输入邮箱地址"
                  required
                >
              </label>

              <!-- 邮件验证码 -->
              <p>邮件验证码</p>
              <div class="code-row">
                <input
                  type="text"
                  v-model="mailCode"
                  placeholder="6位数字验证码"
                  maxlength="6"
                  class="mail-code-input"
                >
                <button
                  type="button"
                  class="send-btn"
                  :disabled="sending || countdown > 0"
                  @click="handleSendMail"
                >
                  {{ sendBtnText }}
                </button>
              </div>

              <p style="margin-top: 20px;">password</p>
              <label>
                <input
                  type="password"
                  v-model="password"
                  placeholder="请输入密码"
                  required
                >
              </label>

              <p>确认密码</p>
              <label>
                <input
                  type="password"
                  v-model="confirmPassword"
                  placeholder="请再次输入密码"
                  required
                >
              </label>

              <input type="submit" value="注册" class="submit-btn" :disabled="submitting">

              <router-link to="/login" class="login-link">
                已经有账号了？去登录
              </router-link>

              <div v-if="tipMsg" :class="['tip-msg', tipType]">{{ tipMsg }}</div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- 图形验证码弹窗 -->
    <div v-if="showCaptchaModal" class="mask" @click.self="closeCaptchaModal">
      <div class="modal">
        <h3>请输入图形验证码</h3>
        <div class="captcha-row">
          <input
            ref="captchaInputRef"
            v-model="captchaCode"
            type="text"
            placeholder="图中字符"
            maxlength="4"
            autocomplete="off"
            @keydown.enter="handleSubmitCaptcha"
          >
          <img
            v-if="captchaImgSrc"
            :src="captchaImgSrc"
            class="captcha-img"
            title="点击刷新"
            alt="验证码"
            @click="refreshCaptchaImg"
          >
        </div>
        <div class="tip">看不清？点击图片换一张 · 回车提交</div>
        <div :class="['modal-msg', modalMsgType]">{{ modalMsg }}</div>
        <button type="button" class="modal-btn" :disabled="submittingCaptcha" @click="handleSubmitCaptcha">
          {{ submittingCaptcha ? '发送中...' : '确定' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const email = ref('')
const mailCode = ref('')
const password = ref('')
const confirmPassword = ref('')

// 提交/发送状态
const submitting = ref(false)
const sending = ref(false)
const submittingCaptcha = ref(false)

// 提示
const tipMsg = ref('')
const tipType = ref('') // 'ok' | 'err'

// 邮件验证码倒计时
const countdown = ref(0)
let countdownTimer = null
const sendBtnText = computed(() => {
  if (sending.value) return '发送中...'
  if (countdown.value > 0) return `${countdown.value}秒后重发`
  return '发送验证码'
})

// 图形验证码弹窗
const showCaptchaModal = ref(false)
const captchaCode = ref('')
const captchaImgSrc = ref('')
const captchaInputRef = ref(null)
const modalMsg = ref('')
const modalMsgType = ref('') // '' | 'ok' | 'err'

function clearTip() {
  tipMsg.value = ''
  tipType.value = ''
}

function showTip(msg, type) {
  tipMsg.value = msg
  tipType.value = type
}

// 构建图形验证码 URL（携带 mail，加时间戳防缓存）
function buildCaptchaUrl() {
  const mail = encodeURIComponent(email.value.trim())
  const t = Date.now()
  return `/shop/user/verify?mail=${mail}&t=${t}`
}

// 刷新图形验证码
function refreshCaptchaImg() {
  captchaImgSrc.value = buildCaptchaUrl()
  captchaCode.value = ''
  modalMsg.value = ''
  modalMsgType.value = ''
}

// 打开图形验证码弹窗
function openCaptchaModal() {
  showCaptchaModal.value = true
  refreshCaptchaImg()
  nextTick(() => {
    captchaInputRef.value && captchaInputRef.value.focus()
  })
}

// 关闭图形验证码弹窗
function closeCaptchaModal() {
  showCaptchaModal.value = false
  captchaCode.value = ''
  modalMsg.value = ''
}

// 开始倒计时
function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 点击"发送验证码" → 校验邮箱 → 弹图形验证码
function handleSendMail() {
  clearTip()
  if (!email.value) {
    showTip('请先输入邮箱', 'err')
    return
  }
  const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!reg.test(email.value)) {
    showTip('邮箱格式不正确', 'err')
    return
  }
  openCaptchaModal()
}

// 图形验证码弹窗内提交（回车或点确定）
async function handleSubmitCaptcha() {
  if (!captchaCode.value) {
    modalMsg.value = '请输入图形验证码'
    modalMsgType.value = 'err'
    return
  }
  submittingCaptcha.value = true
  modalMsg.value = '发送中...'
  modalMsgType.value = ''

  try {
    const mail = encodeURIComponent(email.value.trim())
    const code = encodeURIComponent(captchaCode.value)
    const res = await fetch(`/shop/user/sendMail?mail=${mail}&code=${code}`)
    const data = await res.json()

    if (data.success) {
      modalMsg.value = data.msg || '邮件已发送'
      modalMsgType.value = 'ok'
      startCountdown()
      showTip('邮件验证码已发送，请查收', 'ok')
      setTimeout(() => closeCaptchaModal(), 1200)
    } else {
      modalMsg.value = data.msg || '发送失败'
      modalMsgType.value = 'err'
      // 失败/过期 → 刷新图形验证码
      refreshCaptchaImg()
    }
  } catch (e) {
    modalMsg.value = '请求失败：' + e.message
    modalMsgType.value = 'err'
    refreshCaptchaImg()
  } finally {
    submittingCaptcha.value = false
  }
}

// 注册提交
async function handleSubmit() {
  clearTip()

  if (!email.value || !mailCode.value || !password.value || !confirmPassword.value) {
    showTip('请填写完整', 'err')
    return
  }
  const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!reg.test(email.value)) {
    showTip('邮箱格式不正确', 'err')
    return
  }
  if (mailCode.value.length !== 6 || !/^\d{6}$/.test(mailCode.value)) {
    showTip('邮件验证码应为 6 位数字', 'err')
    return
  }
  if (password.value.length < 6) {
    showTip('密码至少 6 位', 'err')
    return
  }
  if (password.value !== confirmPassword.value) {
    showTip('两次输入密码不一致', 'err')
    return
  }

  submitting.value = true
  try {
    const params = new URLSearchParams()
    params.append('mail', email.value)
    params.append('code', mailCode.value)
    params.append('password', password.value)

    const res = await fetch('/shop/user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params
    })
    const data = await res.json()
    if (data.success) {
      showTip('注册成功', 'ok')
      setTimeout(() => router.push('/login'), 1200)
    } else {
      showTip(data.msg || '注册失败', 'err')
    }
  } catch (e) {
    showTip('注册请求失败：' + e.message, 'err')
  } finally {
    submitting.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped>
/* ---- 背景层：图片 + 半透明白色遮罩 + 模糊 ---- */
.register-bg {
  min-height: 100vh;
  background: var(--page-bg) center / cover no-repeat;
  font-family: 'Noto Sans SC', 'Helvetica Neue', Arial, sans-serif;
  position: relative;
}

.register-overlay {
  min-height: 100vh;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px);
}

/* ---- 标题 ---- */
.title {
  text-align: center;
  padding: 36px 0 16px;
}

.title h1 {
  margin: 0;
  padding: 0;
  color: var(--color-primary);
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* ---- 主容器 ---- */
.container {
  width: 38%;
  min-width: 320px;
  min-height: 340px;
  background: #fff;
  margin: 0 auto;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  border-radius: 14px;
  box-shadow: 0 15px 40px rgba(30, 58, 138, 0.18);
  overflow: hidden;
}

/* 右侧表单区 */
.right {
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
}

/* ---- 表单 ---- */
.formBox {
  width: 100%;
  padding: 30px 28px;
  box-sizing: border-box;
  min-height: 340px;
  background: #fff;
  display: flex;
  align-items: center;
}

.formBox form {
  width: 100%;
}

.formBox p {
  margin: 0 0 6px;
  font-weight: bold;
  color: var(--color-primary);
  font-family: 'Jost', sans-serif;
  font-size: 14px;
}

.formBox input[type="email"],
.formBox input[type="text"],
.formBox input[type="password"] {
  width: 100%;
  border: none;
  border-bottom: 2px solid var(--color-border);
  outline: none;
  height: 32px;
  font-size: 13px;
  margin-bottom: 16px;
  padding: 0;
  background: transparent;
  transition: border-color 0.25s ease;
}

.formBox input[type="email"]:focus,
.formBox input[type="text"]:focus,
.formBox input[type="password"]:focus {
  border-bottom: 2px solid var(--color-primary);
}

/* 邮件验证码行 */
.code-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 16px;
}

.mail-code-input {
  margin-bottom: 0 !important;
  flex: 1;
}

.send-btn {
  flex-shrink: 0;
  border: 1px solid var(--color-primary);
  outline: none;
  height: 32px;
  padding: 0 12px;
  color: var(--color-primary);
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  border-radius: 6px;
  white-space: nowrap;
  transition: background 0.25s ease, color 0.25s ease;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary);
  color: #fff;
}

.send-btn:disabled {
  border-color: var(--color-border);
  background: var(--color-soft);
  color: #9aa6bf;
  cursor: not-allowed;
}

.submit-btn {
  width: 100%;
  border: none;
  outline: none;
  height: 38px;
  color: #fff;
  background: var(--color-primary-gradient);
  border-radius: 10px;
  cursor: pointer;
  margin-top: 8px;
  font-size: 14px;
  font-family: 'Noto Sans SC', sans-serif;
  letter-spacing: 2px;
  box-shadow: 0 6px 18px rgba(30, 58, 138, 0.28);
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

.login-link {
  color: #000;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
  margin-top: 14px;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: var(--color-primary);
}

/* 页面提示 */
.tip-msg {
  margin-top: 12px;
  padding: 7px 10px;
  border-radius: 6px;
  font-size: 12px;
  text-align: center;
}
.tip-msg.ok { background: #e6f7e6; color: #2e7d32; }
.tip-msg.err { background: #fdecea; color: #c62828; }

/* 弹窗遮罩 */
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px 18px;
  width: 300px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  box-shadow: 0 8px 30px var(--shadow-primary-md);
}

.modal h3 {
  color: var(--color-primary);
  margin-bottom: 14px;
  font-size: 15px;
  text-align: center;
  font-weight: 700;
}

.modal .captcha-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.modal .captcha-row input {
  flex: 1;
  padding: 7px 8px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  margin-bottom: 0;
  background: #fff;
  height: auto;
  transition: border-color 0.2s ease;
}

.modal .captcha-row input:focus {
  border-color: var(--color-primary);
}

.captcha-img {
  width: 100px;
  height: 50px;
  cursor: pointer;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-soft);
  flex-shrink: 0;
}

.modal .tip {
  font-size: 11px;
  color: #9aa6bf;
  text-align: center;
  margin-top: 5px;
}

.modal-msg {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  min-height: 16px;
}
.modal-msg.ok { color: #2e7d32; }
.modal-msg.err { color: #c62828; }

.modal-btn {
  width: 100%;
  margin-top: 12px;
  border: none;
  outline: none;
  height: 34px;
  color: #fff;
  background: var(--color-primary-gradient);
  cursor: pointer;
  font-size: 13px;
  border-radius: 8px;
  box-shadow: 0 4px 12px var(--shadow-primary-md);
  transition: transform 0.18s ease, box-shadow 0.25s ease, opacity 0.2s ease;
}
.modal-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(59, 95, 196, 0.4);
}
.modal-btn:disabled { background: var(--color-border); cursor: not-allowed; box-shadow: none; }

/* ---- 响应式 ---- */
@media (max-width: 768px) {
  .container {
    width: 90%;
  }

  .title h1 {
    font-size: 22px;
  }

  .code-row {
    flex-direction: column;
    align-items: stretch;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
