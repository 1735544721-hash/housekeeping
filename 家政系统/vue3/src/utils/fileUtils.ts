import { baseURL } from '@/utils/request'

/**
 * 文件工具类
 */
export default {
  /**
   * 获取完整的图片URL
   * @param url 图片相对路径
   * @returns 完整的图片URL
   */
  getImageUrl(url: string): string {
    if (!url) return ''
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    return `${baseURL}/api${url}`
  },

  /**
   * 获取文件上传URL
   * @param type 文件类型(img/file)
   * @returns 上传URL
   */
  getUploadUrl(type: 'img' | 'file' = 'img'): string {
    return `${baseURL}/api/file/upload/${type}`
  },

  /**
   * 获取文件名从URL
   * @param url 文件URL
   * @returns 文件名
   */
  getFileName(url: string): string {
    if (!url) return ''
    return url.substring(url.lastIndexOf('/') + 1)
  },

  /**
   * 检查文件类型是否为图片
   * @param file 文件对象
   * @returns 是否为图片
   */
  isImage(file: File): boolean {
    return file.type.startsWith('image/')
  },

  /**
   * 检查文件大小
   * @param file 文件对象
   * @param maxSize 最大大小(MB)
   * @returns 是否在限制范围内
   */
  checkFileSize(file: File, maxSize: number): boolean {
    return file.size / 1024 / 1024 < maxSize
  }
} 