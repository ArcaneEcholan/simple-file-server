import request from '@/request'
import {AxiosPromise} from 'axios'

class Client {
    // region: system config
    static createSystemConfig(key: string, value: string): AxiosPromise<any> {
        return request({
            url: `/system-config`,
            method: 'post',
            data: {
                key,
                value,
            },
        })
    }

    static deleteSystemConfig(configId: number): AxiosPromise<any> {
        return request({
            url: `/system-config/${configId}`,
            method: 'delete',
        })
    }

    static updateSystemConfig(
        configId: number,
        key: string,
        value: string,
    ): AxiosPromise<any> {
        return request({
            url: `/system-config/${configId}`,
            method: 'put',
            data: {
                key,
                value,
            },
        })
    }

    static setSystemConfig(key: string, value: string) {
        return request({
            url: `/system-config`,
            method: 'put',
            data: {
                key,
                value,
            },
        })
    }

    static getSystemConfigList(): AxiosPromise<any> {
        return request({
            url: `/system-configs`,
            method: 'get',
        })
    }

    // endregion

    // region: user

    static fetch_user_info(token: string): AxiosPromise<any> {
        return request({
            url: '/user-info',
            method: 'get',
            headers: {'entity-token': token},
        })
    }

    // endregion
    static login(username: string, password: string): AxiosPromise<any> {
        return request({
            url: '/user-authentication',
            method: 'get',
            params: {
                username,
                password,
            },
        })
    }

    // region: file list

    static getFileList(query,token: string) : AxiosPromise<any>{
        return request({
            url: `/file-list?${query}`,
            method: 'get',
            headers: {'entity-token': token},
        })
    }

    static upload(formdata,token: string): AxiosPromise<any> {
        return request({
            url: `/file`,
            method: 'post',
            data: formdata,
            timeout: 0,
            headers: {
                'Content-Type': 'multipart/form-data',
                'entity-token': token
            }
        })
    }

    // endregion
    static assignDirectoryTo(userId: number, directory: string, token: string) {
        return request({
            url: `/user-access-directory`,
            method: 'put',
            data: {
                userId,
                directory,
            },
            headers: {
                'entity-token': token
            }
        })

    }

    static getUserList(token: string) {
        return request({
            url: `/users`,
            method: 'get',
            headers: {
                'entity-token': token
            }
        })
    }

    static addUser(username: string , password: string , token: string) {
        return request({
            url: `/user`,
            method: 'post',
            data: {
                username,
                password
            },
            headers: {
                'entity-token': token
            }
        })
    }
}

export default Client
