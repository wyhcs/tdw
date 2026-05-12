import request from '@/utils/request'

// 查询完整三级大纲树
export function getOutlineTree(bidId) {
  return request({
    url: '/tdw/outlines/tree/' + bidId,
    method: 'get'
  })
}

// 生成三级大纲
export function generateOutline(data, knowledgeFileIds) {
  if (typeof data === 'object') {
    return request({
      url: '/tdw/outlines/generateOutline',
      method: 'post',
      data
    })
  }
  return request({
    url: '/tdw/outlines/generateOutline',
    method: 'post',
    params: { id: data, knowledgeFileIds }
  })
}

// 插入大纲节点
export function insertOutline(data) {
  return request({
    url: '/tdw/outlines/insert',
    method: 'post',
    data
  })
}

// 修改大纲标题
export function updateOutlineTitle(data) {
  return request({
    url: '/tdw/outlines/title',
    method: 'put',
    data
  })
}

// 删除大纲节点
export function deleteOutline(outlineId) {
  return request({
    url: '/tdw/outlines/' + outlineId,
    method: 'delete'
  })
}

// 移动大纲节点
export function moveOutline(data) {
  return request({
    url: '/tdw/outlines/move',
    method: 'put',
    data
  })
}

// 调整同级大纲排序
export function sortOutlines(data) {
  return request({
    url: '/tdw/outlines/sort',
    method: 'put',
    data
  })
}
