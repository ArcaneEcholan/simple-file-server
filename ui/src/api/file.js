import request from '@/utils/request'

export function getFileList(query) {
    return request({
        url: `/file-list?${query}`,
        method: 'get',
    })
}

export function upload(formdata) {
    return request({
        url: `/file`,
        method: 'post',
        data: formdata,
        timeout: 0,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
