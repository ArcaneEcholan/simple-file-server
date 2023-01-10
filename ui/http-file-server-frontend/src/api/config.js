import request from '@/utils/request'

export function updateConfig(key, value) {
  return request({
    url: '/config',
    method: 'put',
    params: {
      key,
      value
    }
  })
}

export function listConfig() {
  return request({
    url: '/config/list',
    method: 'get'
  })
}

export function getConfigValue(key) {
  return request({
    url: '/config',
    method: 'get',
    params: { key }
  })
}
