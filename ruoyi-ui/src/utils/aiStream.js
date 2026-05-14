import { getToken } from '@/utils/auth'

function parseData(data) {
  if (!data) return ''
  try {
    return JSON.parse(data)
  } catch (e) {
    return data
  }
}

function dispatchEvent(block, handlers) {
  const lines = block.split(/\r?\n/).filter(Boolean)
  let eventName = 'message'
  const dataLines = []
  lines.forEach(line => {
    if (line.indexOf('event:') === 0) {
      eventName = line.slice(6).trim()
    } else if (line.indexOf('data:') === 0) {
      dataLines.push(line.slice(5).trim())
    }
  })
  const payload = parseData(dataLines.join('\n'))
  if (handlers.onEvent) handlers.onEvent(eventName, payload)
  if (eventName === 'message' && handlers.onMessage) handlers.onMessage(payload)
  if (eventName === 'done' && handlers.onDone) handlers.onDone(payload)
  if (eventName === 'error' && handlers.onError) handlers.onError(payload)
}

export async function streamPost(url, data, handlers = {}) {
  const response = await fetch(process.env.VUE_APP_BASE_API + url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=utf-8',
      'Authorization': 'Bearer ' + getToken()
    },
    body: JSON.stringify(data || {})
  })
  if (!response.ok || !response.body) {
    throw new Error('AI流式接口连接失败')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() || ''
    blocks.forEach(block => dispatchEvent(block, handlers))
  }
  if (buffer.trim()) {
    dispatchEvent(buffer, handlers)
  }
}
