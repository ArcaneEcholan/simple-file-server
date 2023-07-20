import router, { reset_sidebar_routes, resetRouter } from '@/router'
import { get_token, set_token } from '@/ts/auth'
import store from '@/store'

const getDefaultState = () => {
    return {
        token: get_token(),
        login_info: undefined,
    }
}

const state = getDefaultState()

export const USER_RESET_USER_STATE = 'user/reset_user_state'
export const USER_SET_TOKEN = 'user/set_token'
export const USER_SET_REFRESH_TOKEN = 'user/set_refresh_token'
export const USER_SET_LOGIN_INFO = 'user/set_login_info'
export const USER_LOGOUT = 'user/logout'

const mutations = {
    set_login_info: (state: any, login_info: any) => {
        state.login_info = login_info
    },

    set_token: (state: any, token: string) => {
        state.token = token
        set_token(token, 60 * 60 * 24 * 1)
    },

    // clear all user related infos, this is called when user logout
    reset_user_state: (state) => {
        state.token = undefined
        state.refreshToken = undefined
        state.login_info = undefined
        set_token(undefined, undefined)
    },

    logout: () => {
        resetRouter()
        reset_sidebar_routes()
        store.commit(USER_RESET_USER_STATE)
        router.push('/login')
    },
}

const actions = {}

export default {
    namespaced: true,
    state,
    mutations,
    actions,
}
