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

export function createPlanWithMaterial(data) {
  const formData = new FormData()
  Object.keys(data).forEach(key => {
    if (data[key] !== undefined && data[key] !== null) {
      formData.append(key, data[key])
    }
  })
  return request({
    url: '/tdw/tender/create',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadPlanMaterial(bidId, file, stage) {
  const formData = new FormData()
  formData.append('bidId', bidId)
  formData.append('file', file)
  if (stage) {
    formData.append('stage', stage)
  }
  return request({
    url: '/tdw/tender/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function parsePlanMaterial(fileId) {
  return request({
    url: '/tdw/tender/parse/' + fileId,
    method: 'post'
  })
}

export function listPlanMaterials(bidId) {
  return request({
    url: '/tdw/tender/files/' + bidId,
    method: 'get'
  })
}

export function getLatestPlanReport(bidId) {
  return request({
    url: '/tdw/tender/report/latest/' + bidId,
    method: 'get'
  })
}
