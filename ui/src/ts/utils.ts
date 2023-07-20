import Cookies from 'js-cookie'

import defaultSettings from '@/settings'

const title = defaultSettings.title || 'Vue Element Admin'

export function getPageTitle(pageTitle: string | null) {
    if (pageTitle) {
        return `${pageTitle} - ${title}`
    }
    return `${title}`
}

export class PathBuilder {
    str: string
    constructor() {
        this.str = ''
    }

    static of() {
        return new PathBuilder()
    }

    remove_heading_slash(path: string) {
        while (path.charAt(0) === '/') {
            path = path.substring(1)
        }
        return path
    }

    remove_tailing_slash(path: string) {
        while (path.charAt(path.length - 1) === '/') {
            path = path.substring(0, path.length - 1)
        }
        return path
    }

    trim_slash(path: string) {
        path = this.remove_tailing_slash(path)
        path = this.remove_heading_slash(path)
        return path
    }

    l(latter: string) {
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
export function get_display_file_size(bytes: number) {
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

export function get_display_time(updateTime: number) {
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

// region: cookie

/**
 * 获取cookie值
 * @param {*} key key
 * @returns 如果value是string，直接返回字符串本身；如果value是object,返回一个对象；只有在key不存在时返回undefined
 */
export function get_from_cookie(key) {
    let origin = Cookies.get(key)
    if (origin == undefined) {
        return origin
    }
    try {
        return JSON.parse(origin)
    } catch (e) {
        // console.log(e)
        return origin
    }
}

/**
 * 新建cookie. 除了undefined，其他数据一律转为string后存入
 * @param {*} key key
 * @param {*} value value
 * @param {*} expiresTime cookie保存时间
 */
export function set_cookie(key, value, expiresTime) {
    if (value == undefined) {
        return Cookies.remove(key)
    }
    if (typeof value === 'object') {
        value = JSON.stringify(value)
    }
    if (expiresTime == undefined) {
        return Cookies.set(key, value)
    }
    let seconds = expiresTime
    let expires = new Date(new Date().getTime() + seconds * 1000)

    return Cookies.set(key, value, { expires: expires })
}

// endregion
