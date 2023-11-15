import axios from 'axios'
import { Message } from 'element-ui'
import { PageLocation } from './dynamicLocation'

// create an axios instance
const service = axios.create({
    baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
    // withCredentials: true, // send cookies when cross-domain requests
    timeout: 0 // request timeout
})

// request interceptor\
service.interceptors.request.use(
    config => {
    // do something before request is sent

        const pageLocation = new PageLocation()
        config.baseURL = pageLocation.baseURL
        console.log(pageLocation.baseURL)
        console.log('=========> request ' + config.url, config)
        return config
    },
    error => {
    // do something with request error
        console.log(error) // for debug
        return Promise.reject(error)
    }
)

// response interceptor
service.interceptors.response.use(
    /**
   * If you want to get http information such as headers or status
   * Please return  response => response
  */

    /**
   * Determine the request status by custom code
   * Here is just an example
   * You can also judge the status by HTTP Status Code
   */
    response => {
        const res = response.data

        if (res.code === 'NO_CONFIG_KEY') {
            return res
        }

        if (res.code !== 'SUCCESS') {
            console.log(res)
            return Promise.reject(res)
        }

        return res
    },
    error => {
        console.log('err' + error) // for debug
        Message({
            message: error.message,
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    }
)

export default service
