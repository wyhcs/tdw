import request from '@/utils/request'

export function listQualityProjects(query) {
  return request({
    url: '/tdw/quality/project/list',
    method: 'get',
    params: query
  })
}

export function createQualityProject(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/tdw/quality/project',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function getQualityProjectDetail(bidId) {
  return request({
    url: '/tdw/quality/project/' + bidId,
    method: 'get'
  })
}

export function uploadQualityFramework(bidId, file) {
  const formData = new FormData()
  formData.append('bidId', bidId)
  formData.append('file', file)
  return request({
    url: '/tdw/quality/framework/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 180000
  })
}

export function extractQualityFramework(bidId) {
  return request({
    url: '/tdw/quality/framework/extract/' + bidId,
    method: 'post',
    timeout: 600000
  })
}

export function listQualityItems(query) {
  return request({
    url: '/tdw/quality/item/list',
    method: 'get',
    params: query
  })
}

export function addQualityItem(data) {
  return request({
    url: '/tdw/quality/item',
    method: 'post',
    data
  })
}

export function updateQualityItem(data) {
  return request({
    url: '/tdw/quality/item',
    method: 'put',
    data
  })
}

export function deleteQualityItem(ids) {
  return request({
    url: '/tdw/quality/item/' + ids,
    method: 'delete'
  })
}

export function createQualityVersion(data) {
  const formData = new FormData()
  Object.keys(data).forEach(key => {
    if (data[key] !== undefined && data[key] !== null) {
      formData.append(key, data[key])
    }
  })
  return request({
    url: '/tdw/quality/version',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 600000
  })
}

export function listQualityTasks(query) {
  return request({
    url: '/tdw/quality/task/list',
    method: 'get',
    params: query
  })
}

export function getQualityTask(id) {
  return request({
    url: '/tdw/quality/task/' + id,
    method: 'get'
  })
}

export function exportQualityReport(taskId) {
  return request({
    url: '/tdw/quality/task/' + taskId + '/exportReport',
    method: 'post'
  })
}

export function deleteQualityTask(ids) {
  return request({
    url: '/tdw/quality/task/' + ids,
    method: 'delete'
  })
}

export function listQualityResults(query) {
  return request({
    url: '/tdw/quality/result/list',
    method: 'get',
    params: query
  })
}

export function downloadQualityFramework(frameworkId) {
  return request({
    url: '/tdw/quality/framework/' + frameworkId + '/export',
    method: 'get',
    responseType: 'blob'
  })
}
