import request from '@/utils/request'

export function listTender(query) {
  return request({
    url: '/tdw/tender/list',
    method: 'get',
    params: query
  })
}

export function createTender(data) {
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

export function uploadTenderFile(bidId, file) {
  const formData = new FormData()
  formData.append('bidId', bidId)
  formData.append('file', file)
  return request({
    url: '/tdw/tender/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function parseTenderFile(tenderFileId) {
  return request({
    url: '/tdw/tender/parse/' + tenderFileId,
    method: 'post'
  })
}

export function listTenderFiles(bidId) {
  return request({
    url: '/tdw/tender/files/' + bidId,
    method: 'get'
  })
}

export function getTenderReport(id) {
  return request({
    url: '/tdw/tender/report/' + id,
    method: 'get'
  })
}

export function getTenderReportByFile(tenderFileId) {
  return request({
    url: '/tdw/tender/report/byFile/' + tenderFileId,
    method: 'get'
  })
}

export function getLatestTenderReport(bidId) {
  return request({
    url: '/tdw/tender/report/latest/' + bidId,
    method: 'get'
  })
}
