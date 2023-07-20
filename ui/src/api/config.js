import request from '@/utils/request'

export function updateConfig(key, value, token) {
    return request({
        url: '/config',
        method: 'put',
        params: {
            key,
            value
        },
        headers: {'entity-token': token},
    })
}

export function listConfig(token) {
    return request({
        url: '/config/list',
        method: 'get',
        headers: {'entity-token': token},
    })
}

export function getConfigValue(key, token) {
    return request({
        url: '/config',
        method: 'get',
        params: {key},
        headers: {'entity-token': token},
    })
}
