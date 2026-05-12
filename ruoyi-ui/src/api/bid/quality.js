import request from '@/utils/request'

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

export function addQualityTask(data) {
  return request({
    url: '/tdw/quality/task',
    method: 'post',
    data
  })
}

export function runQualityTask(taskId) {
  return request({
    url: '/tdw/quality/task/' + taskId + '/run',
    method: 'post'
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
