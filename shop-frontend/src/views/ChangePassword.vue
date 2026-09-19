<template>
  <div class="page-shell page-bg">
    <Navbar />
    <div class="page-content">
      <div class="formBox">
        <h2>修改密码</h2>
        <form @submit.prevent="handleSubmit">
          <p>当前密码</p>
          <input type="password" v-model="oldPassword" placeholder="请输入当前密码" required>

          <p>新密码</p>
          <input type="password" v-model="newPassword" placeholder="至少 6 位" required>

          <p>确认新密码</p>
          <input type="password" v-model="confirmPassword" placeholder="再次输入新密码" required>

          <input type="submit" value="确认修改" class="submit-btn" :disabled="submitting">

          <div v-if="tipMsg" :class="['tip-msg', tipType]">{{ tipMsg }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../components/Navbar.vue'

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const submitting = ref(false)
const tipMsg = ref('')
const tipType = ref('')

function showTip(msg, type) {
  tipMsg.value = msg
  tipType.value = type
}

function clearTip() {
  tipMsg.value = ''
  tipType.value = ''
}

async function handleSubmit() {
  clearTip()

  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    showTip('请填写完整', 'err')
    return
  }
  if (newPassword.value.length < 6) {
    showTip('新密码至少 6 位', 'err')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    showTip('两次输入的新密码不一致', 'err')
    return
  }
  if (oldPassword.value === newPassword.value) {
    showTip('新密码不能与当前密码相同', 'err')
    return
  }

  submitting.value = true
  try {
    const mail = localStorage.getItem('mail')
    // TODO: 调用真实修改密码接口
    // 示例：
    // const params = new URLSearchParams()
    // params.append('mail', mail)
    // params.append('oldPassword', oldPassword.value)
    // params.append('newPassword', newPassword.value)
    // const res = await fetch('/shop/user/changePassword', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    //   body: params
    // })
    // const data = await res.json()
    // if (data.success) { showTip('密码修改成功', 'ok'); resetForm() }
    // else { showTip(data.msg, 'err') }

    showTip('密码修改成功', 'ok')
    resetForm()
  } catch (e) {
    showTip('请求失败：' + e.message, 'err')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
}
</script>

<style scoped>
.page-content {
  max-width: 480px;
  margin: 40px auto;
  padding: 0 20px;
}

/* 瓷白卡片 + 顶部青花蓝色装饰带 */
.formBox {
  background: #fff;
  padding: 36px 32px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  border-radius: 14px;
  box-shadow: 0 15px 40px rgba(30, 58, 138, 0.18);
}

.formBox h2 {
  margin: 0 0 24px;
  color: #000;
  font-size: 22px;
  font-weight: 700;
}

.formBox p {
  margin: 0 0 6px;
  font-weight: bold;
  color: var(--color-primary);
  font-size: 14px;
}

.formBox input[type="password"] {
  width: 100%;
  border: none;
  border-bottom: 2px solid var(--color-border);
  outline: none;
  height: 36px;
  font-size: 14px;
  margin-bottom: 20px;
  padding: 0;
  background: transparent;
  transition: border-color 0.25s ease;
}

.formBox input[type="password"]:focus {
  border-bottom: 2px solid var(--color-primary);
}

.submit-btn {
  width: 100%;
  border: none;
  outline: none;
  height: 40px;
  color: #fff;
  background: var(--color-primary-gradient);
  border-radius: 10px;
  cursor: pointer;
  margin-top: 10px;
  font-size: 14px;
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

.tip-msg {
  margin-top: 14px;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 13px;
  text-align: center;
}
.tip-msg.ok { background: #e6f7e6; color: #2e7d32; }
.tip-msg.err { background: #fdecea; color: #c62828; }
</style>
