/**
 * Created by PanJiaChen on 16/11/18.
 */

import { Message } from 'element-ui'

/**
  * Parse the time to string
  * @param {(Object|string|number)} time
  * @param {string} cFormat
  * @returns {string | null}
  */
export function parseTime(time, cFormat) {
    if (arguments.length === 0 || !time) {
        return null
    }
    const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}'
    let date
    if (typeof time === 'object') {
        date = time
    } else {
        if (typeof time === 'string') {
            if (/^[0-9]+$/.test(time)) {
                // support "1548221490638"
                time = parseInt(time)
            } else {
                // support safari
                // https://stackoverflow.com/questions/4310953/invalid-date-in-safari
                time = time.replace(new RegExp(/-/gm), '/')
            }
        }

        if (typeof time === 'number' && time.toString().length === 10) {
            time = time * 1000
        }
        date = new Date(time)
    }
    const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
        a: date.getDay()
    }
    const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
        const value = formatObj[key]
        // Note: getDay() returns 0 on Sunday
        if (key === 'a') {
            return ['日', '一', '二', '三', '四', '五', '六'][value]
        }
        return value.toString().padStart(2, '0')
    })
    return time_str
}

/**
  * @param {number} time
  * @param {string} option
  * @returns {string}
  */
export function formatTime(time, option) {
    if (('' + time).length === 10) {
        time = parseInt(time) * 1000
    } else {
        time = +time
    }
    const d = new Date(time)
    const now = Date.now()

    const diff = (now - d) / 1000

    if (diff < 30) {
        return '刚刚'
    } else if (diff < 3600) {
    // less 1 hour
        return Math.ceil(diff / 60) + '分钟前'
    } else if (diff < 3600 * 24) {
        return Math.ceil(diff / 3600) + '小时前'
    } else if (diff < 3600 * 24 * 2) {
        return '1天前'
    }
    if (option) {
        return parseTime(time, option)
    } else {
        return (
            d.getMonth() +
             1 +
             '月' +
             d.getDate() +
             '日' +
             d.getHours() +
             '时' +
             d.getMinutes() +
             '分'
        )
    }
}

/**
  * @param {string} url
  * @returns {Object}
  */
export function param2Obj(url) {
    const search = decodeURIComponent(url.split('?')[1]).replace(/\+/g, ' ')
    if (!search) {
        return {}
    }
    const obj = {}
    const searchArr = search.split('&')
    searchArr.forEach((v) => {
        const index = v.indexOf('=')
        if (index !== -1) {
            const name = v.substring(0, index)
            const val = v.substring(index + 1, v.length)
            obj[name] = val
        }
    })
    return obj
}

export function set_map(map, key, value, vue) {
    map.set(key, value)
    vue.$forceUpdate()
}

export function str_empty(str) {
    return str == undefined || str == ''
}

export function when_unknow_err(code, callback) {
    if (code != undefined) {
        console.log('未知错误，错误码:', code)
        Message.warning(`未知错误，错误码：${code}`)
    }

    if (callback != undefined && typeof callback === 'function') {
        callback()
    }
}

function process_case(value, ...args) {
    if (args.length == 0) {
        console.log(value)
        return
    }

    var process_func = () => { }
    for (var i = 0; i < args.length; i += 2) {
        var candidate = args[i]
        if (candidate === value) {
            process_func = args[i + 1]
            if (typeof process_func === 'function') {
                process_func(value)
                return
            }
        }
    }

    var args_num = args.length
    if (args_num % 2 == 0) {
    // 没有提供默认函数，输出
        console.log(value)
        return
    }

    var last = args[args.length - 1]
    if (last == undefined || typeof last !== 'function') {
        console.log(value)
        return
    }

    // 提供了默认函数，执行
    last(value)
}

export class ArrayUtils {
    static remove(array, predicate) {
        let idx = 0
        for (let i = 0; i < array.length; i++) {
            const item = array[i]
            if (predicate(item) === true) {
                idx = i
            }
        }

        array.splice(idx, 1)
    }

    static getFirst(array, predicate) {
        for (let i = 0; i < array.length; i++) {
            const item = array[i]
            if (predicate(item) === true) {
                return item
            }
        }

        return null
    }
}

export function get_display_file_size(bytes) {
    const g = bytes / 1024.0 / 1024.0 / 1024.0
    if (g >= 1) {
        return `${g.toFixed(2)} G`
    }
    const m = bytes / 1024.0 / 1024.0
    if (m >= 1) {
        return `${m.toFixed(2)} M`
    }
    const k = bytes / 1024.0
    if (k >= 1) {
        return `${k.toFixed(2)} k`
    }
    const b = bytes
    if (b >= 1) {
        return `${b.toFixed(2)} b`
    }
    return '0 b'
}

export function convert_res_2_blob(response) {
    const content_diposition = response.headers['content-disposition']
    const reg_result = content_diposition.match(/fileName=(.*)/)
    if (reg_result.length < 2) {
        Message.error('文件下载错误')
        return
    }
    const fileName = reg_result[1]
    // 将二进制流转为blob
    const blob = new Blob([response.data], {
        type: 'application/octet-stream'
    })
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
    // 兼容IE，window.navigator.msSaveBlob：以本地方式保存文件
        window.navigator.msSaveBlob(blob, decodeURI(fileName))
    } else {
    // 创建新的URL并指向File对象或者Blob对象的地址
        const blobURL = window.URL.createObjectURL(blob)
        // 创建a标签，用于跳转至下载链接
        const tempLink = document.createElement('a')
        tempLink.style.display = 'none'
        tempLink.href = blobURL
        tempLink.setAttribute('download', decodeURI(fileName))
        // 兼容：某些浏览器不支持HTML5的download属性
        if (typeof tempLink.download === 'undefined') {
            tempLink.setAttribute('target', '_blank')
        }
        // 挂载a标签
        document.body.appendChild(tempLink)
        tempLink.click()
        document.body.removeChild(tempLink)
        // 释放blob URL地址
        window.URL.revokeObjectURL(blobURL)
    }
}

export function get_display_time(updateTime) {
    if (updateTime === null) {
        return ''
    }

    const now = new Date().getTime()
    const second = Math.floor((now - updateTime) / 1000)
    const minute = Math.floor(second / 60)
    const hour = Math.floor(minute / 60)
    const day = Math.floor(hour / 24)
    const month = Math.floor(day / 31)
    const year = Math.floor(month / 12)

    if (year > 0) {
        return year + '年前'
    } else if (month > 0) {
        return month + '月前'
    } else if (day > 0) {
        let ret = day + '天前'
        if (day >= 7 && day < 14) {
            ret = '1周前'
        } else if (day >= 14 && day < 21) {
            ret = '2周前'
        } else if (day >= 21 && day < 28) {
            ret = '3周前'
        } else if (day >= 28 && day < 31) {
            ret = '4周前'
        }
        return ret
    } else if (hour > 0) {
        return hour + '小时前'
    } else if (minute > 0) {
        return minute + '分钟前'
    } else if (second > 0) {
        return second + '秒前'
    } else {
        return '刚刚'
    }
}

export class PathBuilder {
    constructor() {
        this.str = ''
    }

    static of() {
        return new PathBuilder()
    }

    remove_heading_slash(path) {
        while (path.charAt(0) === '/') {
            path = path.substring(1)
        }
        return path
    }

    remove_tailing_slash(path) {
        while (path.charAt(path.length - 1) === '/') {
            path = path.substring(0, path.length - 1)
        }
        return path
    }

    trim_slash(path) {
        path = this.remove_tailing_slash(path)
        path = this.remove_heading_slash(path)
        return path
    }

    l(latter) {
        this.str = this.trim_slash(this.str)
        latter = this.trim_slash(latter)
        let path = this.str + '/' + latter
        path = this.trim_slash(path)
        this.str = '/' + path
        return this
    }

    end() {
        return this.str
    }
}

export class StringUtils {
    static is_empty(str) {
        if (typeof str === 'number') {
            str = str.toString()
        }
        if (typeof str === 'string') {
            str = str.trim()
            return str == undefined || str === ''
        } else if (str == undefined) {
            return true
        } else {
            throw new Error('StringUtils只接受number和string')
        }
    }
}

export function isPrimitiveValue(value) {
    if (
        typeof value === 'string' ||
         typeof value === 'number' ||
         value == null ||
         typeof value === 'boolean' ||
         Number.isNaN(value)
    ) {
        return true
    }

    return false
}

export
function uuidv4() {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
        (
            c ^
          (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
        ).toString(16)
    )
}

export function copy(text) {
    console.log(navigator)
    if (navigator.clipboard) {
        // clipboard api 复制
        navigator.clipboard.writeText(text)
        console.log('use clipboard', text)
    } else {
        console.log('use command', text)

        var textarea = document.createElement('textarea')
        document.body.appendChild(textarea)
        // 隐藏此输入框
        textarea.style.position = 'fixed'
        textarea.style.clip = 'rect(0 0 0 0)'
        textarea.style.top = '10px'
        // 赋值
        textarea.value = text
        // 选中
        textarea.select()
        // 复制
        document.execCommand('copy')
        // // 移除输入框
        // document.body.removeChild(textarea);
    }
}
