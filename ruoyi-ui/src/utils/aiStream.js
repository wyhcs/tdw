import { getToken } from '@/utils/auth'

function unescapeJsonText(value) {
  return String(value || '')
    .replace(/\\"/g, '"')
    .replace(/\\n/g, '\n')
    .replace(/\\r/g, '')
    .replace(/\\t/g, '\t')
    .replace(/\\\\/g, '\\')
}

function parseContentPayload(data) {
  const text = String(data || '')
  const outlineIdMatch = text.match(/"outlineId"\s*:\s*"?(\d+)"?/) || text.match(/outlineId\s*=\s*(\d+)/)
  const outlineId = outlineIdMatch ? outlineIdMatch[1] : undefined
  let content = ''

  const contentKey = text.indexOf('"content"')
  if (contentKey >= 0) {
    const colon = text.indexOf(':', contentKey)
    const firstQuote = text.indexOf('"', colon + 1)
    const lastQuote = text.lastIndexOf('"')
    if (colon >= 0 && firstQuote >= 0 && lastQuote > firstQuote) {
      content = text.slice(firstQuote + 1, lastQuote)
    }
  }
  if (!content && text.indexOf('content=') >= 0) {
    content = text.slice(text.indexOf('content=') + 8).replace(/}\s*$/, '')
  }

  if (!outlineId) return data
  return { outlineId, content: unescapeJsonText(content) }
}

function parseData(data, eventName) {
  if (!data) return ''
  try {
    return JSON.parse(data)
  } catch (e) {
    if (eventName === 'content') {
      return parseContentPayload(data)
    }
    return data
  }
}

function dispatchEvent(block, handlers) {
  const lines = block.split(/\r?\n/)
  let eventName = 'message'
  const dataLines = []
  lines.forEach(line => {
    if (line.indexOf('event:') === 0) {
      eventName = line.slice(6).trim()
    } else if (line.indexOf('data:') === 0) {
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  })
  const payload = parseData(dataLines.join('\n'), eventName)
  if (handlers.onEvent) handlers.onEvent(eventName, payload)
  if (eventName === 'message' && handlers.onMessage) handlers.onMessage(payload)
  if (eventName === 'done' && handlers.onDone) handlers.onDone(payload)
  if (eventName === 'error' && handlers.onError) handlers.onError(payload)
  return eventName
}

function yieldToBrowser() {
  return new Promise(resolve => {
    if (typeof window !== 'undefined' && typeof window.requestAnimationFrame === 'function') {
      window.requestAnimationFrame(() => resolve())
      return
    }
    setTimeout(resolve, 0)
  })
}

export async function streamPost(url, data, handlers = {}) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    let cursor = 0
    let buffer = ''
    let chain = Promise.resolve()

    const consume = final => {
      const chunk = xhr.responseText.slice(cursor)
      cursor = xhr.responseText.length
      buffer += chunk
      const blocks = buffer.split(/\r?\n\r?\n/)
      buffer = blocks.pop() || ''
      if (final && buffer.trim()) {
        blocks.push(buffer)
        buffer = ''
      }
      blocks.forEach(block => {
        chain = chain.then(() => {
          const eventName = dispatchEvent(block, handlers)
          return eventName === 'content' ? yieldToBrowser() : undefined
        })
      })
      return chain
    }

    xhr.open('POST', process.env.VUE_APP_BASE_API + url, true)
    xhr.setRequestHeader('Content-Type', 'application/json;charset=utf-8')
    xhr.setRequestHeader('Accept', 'text/event-stream')
    xhr.setRequestHeader('Authorization', 'Bearer ' + getToken())
    xhr.onprogress = () => {
      consume(false)
    }
    xhr.onload = () => {
      consume(true).then(() => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve()
        } else {
          reject(new Error(xhr.responseText || 'AI流式接口连接失败'))
        }
      })
    }
    xhr.onerror = () => reject(new Error('AI流式接口连接失败'))
    xhr.ontimeout = () => reject(new Error('AI流式接口连接超时'))
    xhr.send(JSON.stringify(data || {}))
  })
}
