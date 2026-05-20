import request from '@/utils/request'
import { streamPost } from '@/utils/aiStream'

export function generateContent(id, knowledgeFileIds, knowledgeChunkIds) {
  return request({
    url: '/tdw/contents/generateContent',
    method: 'post',
    params: { id, knowledgeFileIds, knowledgeChunkIds }
  })
}

export function generateContentBlocks(data, handlers = {}) {
  let result
  return streamPost('/tdw/contents/generate/stream', data, {
    onEvent: (eventName, payload) => {
      if (eventName === 'content' && handlers.onContent) {
        handlers.onContent(normalizeContentPayload(payload))
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

function normalizeContentPayload(payload) {
  if (!payload || typeof payload !== 'string') {
    return payload || {}
  }
  const outlineIdMatch = payload.match(/"outlineId"\s*:\s*"?(\d+)"?/) || payload.match(/outlineId\s*=\s*(\d+)/)
  const outlineId = outlineIdMatch ? outlineIdMatch[1] : undefined
  if (!outlineId) return {}
  const contentMatch = payload.match(/"content"\s*:\s*"([\s\S]*)"\s*}?$/)
  const content = contentMatch ? contentMatch[1] : ''
  return {
    outlineId,
    content: content.replace(/\\"/g, '"').replace(/\\n/g, '\n').replace(/\\r/g, '').replace(/\\t/g, '\t').replace(/\\\\/g, '\\')
  }
}

export function listContentsByOutline(outlineId) {
  return request({
    url: '/tdw/contents/byOutline/' + outlineId,
    method: 'get'
  })
}
