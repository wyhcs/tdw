import request from '@/utils/request'

export function listDuplicateTasks(query) {
  return request({
    url: '/tdw/duplicate/task/list',
    method: 'get',
    params: query
  })
}

export function getDuplicateTask(id) {
  return request({
    url: '/tdw/duplicate/task/' + id,
    method: 'get'
  })
}

export function addDuplicateTask(data) {
  return request({
    url: '/tdw/duplicate/task',
    method: 'post',
    data
  })
}

export function createDuplicateTask(data) {
  return request({
    url: '/tdw/duplicate/task/create',
    method: 'post',
    data,
    timeout: 600000,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false }
  })
}

export function uploadDuplicateFile(taskId, data) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/upload',
    method: 'post',
    data,
    timeout: 600000,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false }
  })
}

export function uploadDuplicateFiles(taskId, data) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/uploadBatch',
    method: 'post',
    data,
    timeout: 600000,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false }
  })
}

export function updateDuplicateLibraries(taskId, data) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/libraries',
    method: 'post',
    data
  })
}

export function runDuplicateTask(taskId, data) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/run',
    method: 'post',
    data,
    timeout: 600000
  })
}

export function deleteDuplicateTask(ids) {
  return request({
    url: '/tdw/duplicate/task/' + ids,
    method: 'delete'
  })
}

export function listDuplicateFiles(query) {
  return request({
    url: '/tdw/duplicate/file/list',
    method: 'get',
    params: query
  })
}

export function deleteDuplicateFile(ids) {
  return request({
    url: '/tdw/duplicate/file/' + ids,
    method: 'delete'
  })
}

export function listDuplicateResults(query) {
  return request({
    url: '/tdw/duplicate/result/list',
    method: 'get',
    params: query
  })
}

export function getDuplicateReport(taskId) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/report',
    method: 'get'
  })
}

export function exportDuplicateReport(taskId) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/exportReport',
    method: 'post',
    timeout: 600000
  })
}
