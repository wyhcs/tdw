import request from '@/utils/request'

export function listKnowledge(query) {
  return request({ url: '/tdw/knowledge/list', method: 'get', params: query })
}

export function getKnowledge(knowledgeId) {
  return request({ url: '/tdw/knowledge/' + knowledgeId, method: 'get' })
}

export function addKnowledge(data) {
  return request({ url: '/tdw/knowledge', method: 'post', data })
}

export function renameKnowledge(data) {
  return request({ url: '/tdw/knowledge/rename', method: 'put', data })
}

export function delKnowledge(knowledgeId) {
  return request({ url: '/tdw/knowledge/' + knowledgeId, method: 'delete' })
}

export function listKnowledgeFiles(query) {
  return request({ url: '/tdw/knowledge/file/list', method: 'get', params: query })
}

export function uploadKnowledgeFile(data) {
  return request({
    url: '/tdw/knowledge/file/upload',
    method: 'post',
    headers: {
      repeatSubmit: false,
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function parseKnowledgeFile(knowledgeFileId) {
  return request({ url: '/tdw/knowledge/file/' + knowledgeFileId + '/parse', method: 'post' })
}

export function extractKnowledgeImages(knowledgeFileId) {
  return request({ url: '/tdw/knowledge/file/' + knowledgeFileId + '/extractImages', method: 'post' })
}

export function extractKnowledgeUpload(data) {
  return request({
    url: '/tdw/knowledge/file/extractUpload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false },
    data
  })
}

export function listTemplateFiles(query) {
  return request({ url: '/tdw/knowledge/templateFiles', method: 'get', params: query })
}

export function listKnowledgeChunks(query) {
  return request({ url: '/tdw/knowledge/chunks', method: 'get', params: query })
}
