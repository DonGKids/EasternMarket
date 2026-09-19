<template>
  <div class="page-shell tutorial">
    <Navbar />
    <div class="page-content">
      <div class="card">
        <h2 class="page-title">使用教程</h2>
        <p class="page-desc">了解如何使用购物平台</p>

        <div class="tutorial-list">
          <div class="tutorial-item">
            <div class="tutorial-num">1</div>
            <div class="tutorial-info">
              <h4>注册账号</h4>
              <p>使用邮箱注册账号，接收验证码完成注册流程</p>
            </div>
          </div>

          <div class="tutorial-item">
            <div class="tutorial-num">2</div>
            <div class="tutorial-info">
              <h4>浏览商品</h4>
              <p>进入市场页面，浏览所有商品，查看商品详情</p>
            </div>
          </div>

          <div class="tutorial-item">
            <div class="tutorial-num">3</div>
            <div class="tutorial-info">
              <h4>加入购物车</h4>
              <p>在商品详情页点击"加入购物车"按钮，将商品加入购物车清单</p>
            </div>
          </div>

          <div class="tutorial-item">
            <div class="tutorial-num">4</div>
            <div class="tutorial-info">
              <h4>下单支付</h4>
              <p>在购物车页面确认商品数量和金额，点击"下单"完成购买</p>
            </div>
          </div>

          <div class="tutorial-item">
            <div class="tutorial-num">5</div>
            <div class="tutorial-info">
              <h4>管理订单</h4>
              <p>在首页查看订单状态，待付款订单可继续支付，待收货订单可确认收货</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import Navbar from '../components/Navbar.vue'
</script>

<style scoped>
.page-content {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  box-shadow: 0 8px 28px var(--shadow-primary);
  padding: 36px 32px;
}

.page-title {
  margin: 0 0 8px;
  color: #000;
  font-size: 24px;
  font-weight: 800;
}

.page-desc {
  margin: 0 0 28px;
  color: #8b8676;
  font-size: 14px;
}

.tutorial-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tutorial-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: var(--color-bg);
  transition: background 0.2s ease, transform 0.2s ease;
}

.tutorial-item:hover {
  background: var(--color-soft);
  transform: translateX(4px);
}

.tutorial-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary-gradient);
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 10px var(--shadow-primary-md);
}

.tutorial-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: #000;
}

.tutorial-info p {
  margin: 0;
  font-size: 14px;
  color: #4a5a78;
  line-height: 1.5;
}
</style>
