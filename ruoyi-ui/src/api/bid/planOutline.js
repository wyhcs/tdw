import request from '@/utils/request'

export function getPlanOutlineOverview(bidId) {
  return request({
    url: `/tdw/plan/${bidId}/outline/overview`,
    method: 'get'
  })
}

export function applyPlanWordPreset(bidId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/word-preset`,
    method: 'post',
    data
  })
}

export function updateNodeWordLimit(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/word-limit`,
    method: 'put',
    data
  })
}

export function batchUpdateNodeWordLimit(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/word-limit/batch`,
    method: 'put',
    data
  })
}

export function updatePlanOutlineTitle(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/title`,
    method: 'put',
    data
  })
}

export function saveWritingDirection(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/writing-direction`,
    method: 'put',
    data
  })
}

export function saveWritingRequirement(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/writing-requirement`,
    method: 'put',
    data
  })
}

export function saveGlobalWritingRequirement(bidId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/writing-requirement`,
    method: 'put',
    data
  })
}

export function addOutlineSibling(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/sibling`,
    method: 'post',
    data
  })
}

export function addOutlineChild(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/child`,
    method: 'post',
    data
  })
}

export function addOutlineParagraph(bidId, outlineId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/${outlineId}/paragraph`,
    method: 'post',
    data
  })
}

export function deleteOutlineNodes(bidId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/delete`,
    method: 'post',
    data
  })
}

export function sortOutlineNodes(bidId, data) {
  return request({
    url: `/tdw/plan/${bidId}/outline/nodes/sort`,
    method: 'put',
    data
  })
}

export function finalizePlanOutline(bidId) {
  return request({
    url: `/tdw/plan/${bidId}/outline/finalize`,
    method: 'post'
  })
}
