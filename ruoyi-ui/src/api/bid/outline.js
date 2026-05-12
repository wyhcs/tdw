import request from '@/utils/request'

export function generateOutline(id, knowledgeFileIds) {
  return request({
    url: '/tdw/outlines/generateOutline',
    method: 'post',
    params: { id, knowledgeFileIds }
  })
}

export function getOutlineTree(bidId) {
  return request({
    url: '/tdw/outlines/tree/' + bidId,
    method: 'get'
  })
}
