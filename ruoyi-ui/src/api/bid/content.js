import request from '@/utils/request'
import { streamPost } from '@/utils/aiStream'

export function generateContent(id, knowledgeFileIds, knowledgeChunkIds) {
  return request({
    url: '/tdw/contents/generateContent',
    method: 'post',
    params: { id, knowledgeFileIds, knowledgeChunkIds }
  })
}

export function generateContentBlocks(data) {
  let result
  return streamPost('/tdw/contents/generate/stream', data, {
    onDone: payload => {
      result = payload
    },
    onError: payload => {
      throw new Error(payload || '正文生成失败')
    }
  }).then(() => ({
    code: 200,
    data: result
  }))
}

export function listContentsByOutline(outlineId) {
  return request({
    url: '/tdw/contents/byOutline/' + outlineId,
    method: 'get'
  })
}
