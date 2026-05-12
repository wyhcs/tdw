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
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadDuplicateFile(taskId, data) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function runDuplicateTask(taskId) {
  return request({
    url: '/tdw/duplicate/task/' + taskId + '/run',
    method: 'post'
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

export function listDuplicateResults(query) {
  return request({
    url: '/tdw/duplicate/result/list',
    method: 'get',
    params: query
  })
}
