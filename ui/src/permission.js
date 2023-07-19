import router from '@/router/index'
import store from '@/store/index'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style

import {
    get_token,
    parse_user_info_response,
    process_user_menu,
} from '@/ts/auth'
import Client from '@/request/client'
import {
    USER_RESET_USER_STATE,
    USER_SET_LOGIN_INFO,
} from '@/store/modules/user'
import {getPageTitle} from '@/ts/utils';

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login', '/'] // no redirect whitelist

NProgress.configure({ showSpinner: false }) // NProgress Configuration

router.beforeEach(async (to, from, next) => {
    let logTag = 'router.beforeEach(): \n'
    console.log(logTag, from, 'to', to)
    debugger
    // start progress bar
    NProgress.start()

    // set page title
    document.title = getPageTitle(to.meta?.title)

    let white_list_idx = whiteList.findIndex((elem) => elem === to.fullPath)
    if (white_list_idx !== -1) {
        next()
        return
    }

    const token = get_token()
    if (token == null || token === '') {
        store.commit(USER_RESET_USER_STATE)
        next({ path: '/login' })
        return
    }

    // var login_info = get_lo_gin_info();
    if (store.state.user.login_info) {
        console.log(logTag, 'user info exists, do not process menu')
        if (to.matched.length === 0) {
            next({ path: to.fullPath })
        } else {
            next()
        }
        return
    }

    // user info not exists, fetch from user
    console.log(logTag, 'user info does not exist, fetch user info')
    Client.fetch_user_info(token).then((resp) => {
        let user_info = resp.data
        let login_info = parse_user_info_response(user_info)

        // store user info
        store.commit(USER_SET_LOGIN_INFO, login_info)

        // generate routes against user menus
        process_user_menu(login_info)

        if (to.matched.length === 0) {
            next({ path: to.fullPath })
        } else {
            next()
        }
        return
    })
})

router.afterEach(() => {
    // finish progress bar
    NProgress.done()
})