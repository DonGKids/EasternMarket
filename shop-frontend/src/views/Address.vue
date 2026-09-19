<template>
  <div class="page-shell page-bg">
    <Navbar />
    <div class="page-content">
      <div class="formBox">
        <h2>个人地址</h2>
        <p class="placeholder">功能开发中，敬请期待...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import Navbar from '../components/Navbar.vue'
</script>

<style scoped>
.page-content {
  max-width: 600px;
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
  margin: 0 0 16px;
  color: #000;
  font-size: 22px;
  font-weight: 700;
}

.placeholder {
  color: #9aa6bf;
  font-size: 14px;
  text-align: center;
  padding: 30px 0;
}
</style>
