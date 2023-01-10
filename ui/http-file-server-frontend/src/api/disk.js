import request from '@/utils/request'

export function scan(params) {
  return request({
    url: '/disk/scan',
    method: 'get',
    params
  })
}

export function filelist(params) {
  return request({
    url: '/disk/filelist',
    method: 'get',
    params
  })
}

export function dirinfo(params) {
  return request({
    url: '/disk/dirinfo',
    method: 'get',
    params
  })
}

