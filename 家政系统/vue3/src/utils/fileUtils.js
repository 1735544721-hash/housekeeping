import request from '@/utils/request'

/**
 * 文件工具类
 */
export default {
  /**
   * 获取完整的图片URL
   * @param {string} url 图片相对路径
   * @returns {string} 完整的图片URL
   */
  getImageUrl(url) {
    if (!url) return ''
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    return `/api${url}`
  },

  /**
   * 获取文件上传URL
   * @param {'img'|'file'} type 文件类型
   * @returns {string} 上传URL
   */
  getUploadUrl(type = 'img') {
    return `/api/file/upload/${type}`
  },

  /**
   * 获取文件名从URL
   * @param {string} url 文件URL
   * @returns {string} 文件名
   */
  getFileName(url) {
    if (!url) return ''
    return url.substring(url.lastIndexOf('/') + 1)
  },

  /**
   * 检查文件类型是否为图片
   * @param {File} file 文件对象
   * @returns {boolean} 是否为图片
   */
  isImage(file) {
    return file?.type?.startsWith('image/') || false
  },

  /**
   * 检查文件大小
   * @param {File} file 文件对象
   * @param {number} maxSize 最大大小(MB)
   * @returns {boolean} 是否在限制范围内
   */
  checkFileSize(file, maxSize) {
    return file?.size ? (file.size / 1024 / 1024 < maxSize) : false
  }
} 