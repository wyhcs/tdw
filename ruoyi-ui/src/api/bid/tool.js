import request from '@/utils/request'

export function listGalleries(query) {
  return request({
    url: '/tdw/tool/gallery/list',
    method: 'get',
    params: query
  })
}

export function getGallery(id) {
  return request({
    url: '/tdw/tool/gallery/' + id,
    method: 'get'
  })
}

export function addGallery(data) {
  return request({
    url: '/tdw/tool/gallery',
    method: 'post',
    data
  })
}

export function updateGallery(data) {
  return request({
    url: '/tdw/tool/gallery',
    method: 'put',
    data
  })
}

export function deleteGallery(ids) {
  return request({
    url: '/tdw/tool/gallery/' + ids,
    method: 'delete'
  })
}

export function listGalleryImages(query) {
  return request({
    url: '/tdw/tool/image/list',
    method: 'get',
    params: query
  })
}

export function getGalleryImage(id) {
  return request({
    url: '/tdw/tool/image/' + id,
    method: 'get'
  })
}

export function uploadGalleryImage(data) {
  return request({
    url: '/tdw/tool/image/upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function updateGalleryImage(data) {
  return request({
    url: '/tdw/tool/image',
    method: 'put',
    data
  })
}

export function deleteGalleryImage(ids) {
  return request({
    url: '/tdw/tool/image/' + ids,
    method: 'delete'
  })
}
