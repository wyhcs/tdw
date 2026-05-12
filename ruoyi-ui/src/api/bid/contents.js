import request from '@/utils/request'

// 根据 outline_id 查询内容块列表
export function listContentsByOutline(outlineId) {
  return request({
    url: '/tdw/contents/byOutline/' + outlineId,
    method: 'get'
  })
}

// 生成内容块
export function generateContentBlocks(data) {
  return request({
    url: '/tdw/contents/generate',
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
