import request from '@/utils/request'
import { streamPost } from '@/utils/aiStream'

// 根据 outline_id 查询内容块列表
export function listContentsByOutline(outlineId) {
  return request({
    url: '/tdw/contents/byOutline/' + outlineId,
    method: 'get'
  })
}

export function listContentsByOutlines(outlineIds) {
  return request({
    url: '/tdw/contents/byOutlines',
    method: 'post',
    data: outlineIds || []
  })
}

// 生成内容块
export function generateContentBlocks(data, handlers = {}) {
  let result
  return streamPost('/tdw/contents/generate/stream', data, {
    onEvent: (eventName, payload) => {
      if (eventName === 'content' && handlers.onContent) {
        handlers.onContent(payload || {})
      }
      if (eventName === 'generated' && handlers.onGenerated) {
        handlers.onGenerated(payload || [])
      }
      if (handlers.onEvent) {
        handlers.onEvent(eventName, payload)
      }
    },
    onDone: payload => {
      result = payload
      if (handlers.onDone) {
        handlers.onDone(payload)
      }
    },
    onError: payload => {
      if (handlers.onError) {
        handlers.onError(payload)
      }
      throw new Error(payload || '正文生成失败')
    }
  }).then(() => ({
    code: 200,
    data: result
  }))
}

// 保存大纲节点富文本内容
export function saveRichContent(data) {
  return request({
    url: '/tdw/contents/rich',
    method: 'post',
    data
  })
}

// 选中文本AI处理：扩写、缩写、改写、总结图、总结表
export function applySelectionAi(data) {
  return request({
    url: '/tdw/contents/selection/ai',
    method: 'post',
    data
  })
}

// 新增内容块
export function addContent(data) {
  return request({
    url: '/tdw/contents',
    method: 'post',
    data
  })
}

// 修改内容块
export function updateContent(data) {
  return request({
    url: '/tdw/contents',
    method: 'put',
    data
  })
}

// 删除内容块
export function deleteContent(contentId) {
  return request({
    url: '/tdw/contents/' + contentId,
    method: 'delete'
  })
}

// 调整内容块排序
export function sortContents(data) {
  return request({
    url: '/tdw/contents/sort',
    method: 'put',
    data
  })
}
