/**
 * 图片 URL 解析工具
 * 统一处理历史数据（相对路径）和新数据（七牛云完整 URL）
 */

/**
 * 将存储在 DB 中的图片字段解析为浏览器可直接访问的 URL
 *
 * @param {string} raw  DB 中存储的原始值
 *                      - 旧格式：/avatars/xxx.jpg 或 /products/1.jpg
 *                      - 新格式：http://cdn.example.com/avatar/xxx.jpg（七牛云完整 URL）
 * @returns {string} 浏览器可直接访问的 URL
 */
export function resolveImgUrl(raw) {
  if (!raw || typeof raw !== 'string') return ''
  const v = raw.trim()
  if (!v) return ''

  // 已经是完整 URL（新格式，七牛云），直接返回
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('data:')) {
    return v
  }

  // 旧格式相对路径：/avatars/xxx.jpg 或 /products/1.jpg
  // 通过 vite 代理 /avatars -> shop-user 本地映射访问（兼容历史数据）
  // 这里直接返回相对路径，浏览器会基于当前 origin 解析
  if (v.startsWith('/')) {
    return v
  }

  // 其他情况，原样返回
  return v
}

/**
 * 给图片 URL 加时间戳，避免浏览器缓存
 * @param {string} url
 * @returns {string}
 */
export function withCacheBuster(url) {
  if (!url) return ''
  const sep = url.includes('?') ? '&' : '?'
  return url + sep + 't=' + Date.now()
}
