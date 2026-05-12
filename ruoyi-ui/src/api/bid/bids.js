import request from '@/utils/request'

export function listBids(query) {
  return request({
    url: '/tdw/bids/list',
    method: 'get',
    params: query
  })
}

export function getBids(id) {
  return request({
    url: '/tdw/bids/' + id,
    method: 'get'
  })
}

export function addBids(data) {
  return request({
    url: '/tdw/bids/add',
    method: 'post',
    data
  })
}

export function updateBids(data) {
  return request({
    url: '/tdw/bids',
    method: 'put',
    data
  })
}

export function deleteBid(id) {
  return request({
    url: '/tdw/bids/deleteBid',
    method: 'delete',
    params: { id }
  })
}

export function exportPlanHtml(data) {
  return request({
    url: '/tdw/bids/exportHtml',
    method: 'post',
    data
  })
}
