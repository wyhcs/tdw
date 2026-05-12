import request from '@/utils/request'

export function generateContent(id, knowledgeFileIds, knowledgeChunkIds) {
  return request({
    url: '/tdw/contents/generateContent',
    method: 'post',
    params: { id, knowledgeFileIds, knowledgeChunkIds }
  })
}

export function generateContentBlocks(data) {
  return request({
    url: '/tdw/contents/generate',
    method: 'post',
    data
  })
}

export function listContentsByOutline(outlineId) {
  return request({
    url: '/tdw/contents/byOutline/' + outlineId,
    method: 'get'
  })
}
