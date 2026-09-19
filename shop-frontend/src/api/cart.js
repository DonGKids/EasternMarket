/**
 * 购物车 API 封装
 * 与后端 shop-cart 微服务（端口 7075）对接
 * 购物车在用户登录态下持久化，未登录时由调用方决定如何降级（一般提示登录）
 */

/**
 * 加入购物车（已存在则累加数量）
 */
export async function apiAddToCart(userId, productId, quantity = 1) {
  const params = new URLSearchParams()
  params.set('userId', userId)
  params.set('productId', productId)
  params.set('quantity', quantity)
  const res = await fetch('/shop/cart', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  })
  return res.json()
}

/**
 * 修改数量（绝对值）
 */
export async function apiUpdateCartQty(userId, productId, quantity) {
  const params = new URLSearchParams()
  params.set('userId', userId)
  params.set('productId', productId)
  params.set('quantity', quantity)
  const res = await fetch('/shop/cart', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  })
  return res.json()
}

/**
 * 删除单个购物车条目
 */
export async function apiRemoveFromCart(userId, productId) {
  const params = new URLSearchParams()
  params.set('userId', userId)
  params.set('productId', productId)
  const res = await fetch('/shop/cart', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  })
  return res.json()
}

/**
 * 批量删除（下单成功后清空已下单商品）
 * @param {number} userId
 * @param {number[]} productIds
 */
export async function apiBatchDeleteCart(userId, productIds) {
  const params = new URLSearchParams()
  params.set('userId', userId)
  const res = await fetch(`/shop/cart/batchDelete?${params.toString()}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(productIds)
  })
  return res.json()
}

/**
 * 清空购物车
 */
export async function apiClearCart(userId) {
  const params = new URLSearchParams()
  params.set('userId', userId)
  const res = await fetch(`/shop/cart/clear?${params.toString()}`, {
    method: 'POST'
  })
  return res.json()
}

/**
 * 查询购物车列表（含商品快照）
 */
export async function apiListCart(userId) {
  const res = await fetch(`/shop/cart?userId=${userId}`)
  return res.json()
}
