<template>
  <div class="page-shell profile-bg">
    <Navbar />
    <div class="page-content">
      <div class="formBox">
          <form @submit.prevent="handleSave">
            <!-- 头像上传 -->
            <div class="avatar-box">
              <div class="avatar-preview">
                <img v-if="avatarUrl" :src="avatarUrl" alt="头像">
                <div v-else class="avatar-placeholder">未设置</div>
              </div>
              <div class="avatar-actions">
                <label class="upload-btn">
                  <input type="file" accept="image/*" @change="handleUpload" hidden>
                  {{ uploading ? '上传中...' : '上传头像' }}
                </label>
                <span class="avatar-tip">支持 jpg/png，建议正方形，≤5MB</span>
              </div>
            </div>

            <p>邮箱</p>
            <input type="email" :value="mail" disabled class="disabled-input">

            <p>昵称</p>
            <input type="text" v-model="nickname" placeholder="请输入昵称" maxlength="20">

            <p>年龄</p>
            <input type="number" v-model="age" placeholder="请输入年龄" min="0" max="150">

            <p>性别</p>
            <div class="gender-row">
              <label>
                <input type="radio" v-model="gender" :value="1"> 男
              </label>
              <label>
                <input type="radio" v-model="gender" :value="0"> 女
              </label>
            </div>

            <input type="submit" value="保存资料" class="submit-btn" :disabled="saving">

            <div v-if="tipMsg" :class="['tip-msg', tipType]">{{ tipMsg }}</div>
          </form>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import { resolveImgUrl, withCacheBuster } from '../utils/img'

const router = useRouter()

const mail = ref(localStorage.getItem('mail') || '')
const nickname = ref('')
const age = ref(null)
const gender = ref(null)
const avatarUrl = ref('')   // 用来在 <img> 上显示的 URL（已解析 + 加时间戳）
const avatarPath = ref('') // 用来提交给后端的原始存储值

const uploading = ref(false)
const saving = ref(false)
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

// 进入页面时读取已保存的资料
onMounted(async () => {
  if (!mail.value) {
    showTip('未检测到登录信息，请先登录', 'err')
    setTimeout(() => router.push('/login'), 1500)
    return
  }
  try {
    const res = await fetch(`/shop/user/getProfile?mail=${encodeURIComponent(mail.value)}`)
    const data = await res.json()
    if (data.success && data.data) {
      nickname.value = data.data.nickname || ''
      age.value = data.data.age ?? null
      gender.value = data.data.gender ?? null
      if (data.data.avatar) {
        avatarPath.value = data.data.avatar
        avatarUrl.value = withCacheBuster(resolveImgUrl(data.data.avatar))
      }
    }
  } catch (e) {
    showTip('读取资料失败：' + e.message, 'err')
  }
})

// 上传头像
async function handleUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  // 前端简单校验
  if (file.size > 5 * 1024 * 1024) {
    showTip('图片不能大于 5MB', 'err')
    return
  }
  const allowTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/gif', 'image/webp']
  if (!allowTypes.includes(file.type)) {
    showTip('仅支持 jpg/png/gif/webp 格式', 'err')
    return
  }

  clearTip()
  uploading.value = true

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await fetch('/shop/user/uploadAvatar', {
      method: 'POST',
      body: formData
    })
    const data = await res.json()
    if (data.success) {
      avatarPath.value = data.url
      // 加时间戳避免浏览器缓存旧头像
      avatarUrl.value = withCacheBuster(resolveImgUrl(data.url))
      showTip('头像上传成功，记得点击"保存资料"', 'ok')
    } else {
      showTip(data.msg || '上传失败', 'err')
    }
  } catch (err) {
    showTip('上传请求失败：' + err.message, 'err')
  } finally {
    uploading.value = false
    // 清掉 input 的值，否则选同一张图不会触发 change
    e.target.value = ''
  }
}

// 保存资料
async function handleSave() {
  clearTip()

  if (!nickname.value || !nickname.value.trim()) {
    showTip('请输入昵称', 'err')
    return
  }
  if (age.value === null || age.value === '' || isNaN(Number(age.value))) {
    showTip('请输入年龄', 'err')
    return
  }
  if (Number(age.value) < 0 || Number(age.value) > 150) {
    showTip('年龄请填 0-150 之间', 'err')
    return
  }
  if (gender.value === null) {
    showTip('请选择性别', 'err')
    return
  }

  saving.value = true
  try {
    const params = new URLSearchParams()
    params.append('mail', mail.value)
    params.append('nickname', nickname.value.trim())
    params.append('age', age.value ?? '')
    params.append('gender', gender.value)
    params.append('avatar', avatarPath.value)

    const res = await fetch('/shop/user/saveProfile', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params
    })
    const data = await res.json()
    if (data.success) {
      showTip('资料保存成功', 'ok')
      // 同步到 localStorage，Navbar 可立即更新
      localStorage.setItem('avatar', avatarPath.value)
      localStorage.setItem('nickname', nickname.value.trim())
      window.dispatchEvent(new Event('avatar-updated'))
    } else {
      showTip(data.msg || '保存失败', 'err')
    }
  } catch (err) {
    showTip('保存请求失败：' + err.message, 'err')
  } finally {
    saving.value = false
  }
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
  width: 100%;
  padding: 36px 32px;
  box-sizing: border-box;
  background: #fff;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  border-radius: 14px;
  box-shadow: 0 15px 40px rgba(30, 58, 138, 0.18);
}

.formBox form {
  width: 100%;
}

.formBox h2 {
  margin: 0 0 24px;
  color: #000;
  font-size: 22px;
  font-weight: 700;
}

.formBox p {
  margin: 0 0 8px;
  font-weight: bold;
  color: var(--color-primary);
  font-family: 'Jost', sans-serif;
  font-size: 15px;
}

.formBox input[type="email"],
.formBox input[type="text"],
.formBox input[type="number"] {
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

.formBox input:focus {
  border-bottom: 2px solid var(--color-primary);
}

.disabled-input {
  background: var(--color-soft) !important;
  color: #9aa6bf !important;
  cursor: not-allowed;
}

/* 头像 */
.avatar-box {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.avatar-preview {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--color-soft);
  border: 2px dashed var(--color-border);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: #9aa6bf;
  font-size: 13px;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.upload-btn {
  display: inline-block;
  padding: 8px 16px;
  background: var(--color-primary-gradient);
  color: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  box-shadow: 0 4px 12px var(--shadow-primary-md);
  transition: transform 0.18s ease, box-shadow 0.25s ease;
  text-align: center;
  width: fit-content;
}

.upload-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(59, 95, 196, 0.4);
}

.avatar-tip {
  font-size: 12px;
  color: #9aa6bf;
}

/* 性别 */
.gender-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.gender-row label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #000;
}

.gender-row input[type="radio"] {
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
  cursor: pointer;
}

/* 提交 */
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
    width: 95%;
  }

  .formBox {
    padding: 30px 20px;
  }

  .avatar-box {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
