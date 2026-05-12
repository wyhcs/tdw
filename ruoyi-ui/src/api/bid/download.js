import request from '@/utils/request'

export function listDownloadRecords(query) {
  return request({
    url: '/tdw/download/list',
    method: 'get',
    params: query
  })
}

export function getDownloadRecord(id) {
  return request({
    url: '/tdw/download/' + id,
    method: 'get'
  })
}

export function exportModuleReport(sourceModule, sourceId) {
  return request({
    url: '/tdw/download/report/' + sourceModule + '/' + sourceId,
    method: 'post'
  })
}

export function deleteDownloadRecord(ids) {
  return request({
    url: '/tdw/download/' + ids,
    method: 'delete'
  })
}

export function downloadRecordUrl(id) {
  return process.env.VUE_APP_BASE_API + '/tdw/download/file/' + id
}
