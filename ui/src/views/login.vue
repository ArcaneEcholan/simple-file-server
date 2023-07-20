<template>
    <div class="">
        <el-input v-model="username"></el-input>
        <el-input v-model="password"></el-input>
        <el-button @click="onClickLogin">login</el-button>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Client from '@/request/client'
import {
    get_token,
    parse_user_info_response,
    processSideBarRoutes,
} from '@/ts/auth'
import store from '@/store'
import { USER_SET_LOGIN_INFO, USER_SET_TOKEN } from '@/store/modules/user'
import { ROUTE_PATHS } from '@/ts/consts/routerPathConstants'
import router from '@/router'

import { Notification } from 'element-ui'
import {isNavigationFailure, NavigationFailureType} from 'vue-router'
@Component({})
export default class LoginView extends Vue {
    username: string | null = null
    password: string | null = null
    loading = false

    onClickLogin() {
        if (this.username != null && this.password != null) {
            Client.login(this.username, this.password)
                .then((resp) => {
                    this.loading = false
                    var token = resp.data
                    store.commit(USER_SET_TOKEN, token)
                    router.push(`${ROUTE_PATHS.PATH_FILELIST}`).then(failure => {
                        if (isNavigationFailure(failure, NavigationFailureType.redirected)) {
                            // show a small notification to the user
                            alert('Login in order to access the admin panel')
                        }
                    })
                })
                .catch((resp) => {
                    this.loading = false
                    if (resp.code === 'AUTH_FAILED') {
                        Notification.warning('登录失败，请检查您的用户名和密码')
                    }
                })
        }
    }

    beforeRouteEnter(to, from, next) {
        const token = get_token()
        if (token == null || token == '') {
            next()
            return
        }

        if (store.state.user.login_info) {
            processSideBarRoutes(store.state.user.login_info)
            next(`${ROUTE_PATHS.PATH_FILELIST}`).catch();
            return
        }

        // 无论本地用户信息是否存在，都拉取用户信息，以保证用户信息是最新的
        Client.fetch_user_info(token).then((resp) => {
            var user_info = resp.data
            var login_info = parse_user_info_response(user_info)

            // 保存信息到全局和本地
            store.commit(USER_SET_LOGIN_INFO, login_info)
            processSideBarRoutes(login_info)
            next(`${ROUTE_PATHS.PATH_FILELIST}`).catch();
            return
        })
    }
}
</script>
<style lang="scss" scoped></style>
