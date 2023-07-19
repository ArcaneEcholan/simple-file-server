import Vue from 'vue'
import VueRouter, {RouteConfig} from 'vue-router'

Vue.use(VueRouter)

/* Layout */
import Layout from '@/layout/index.vue'
import {ROUTE_PATHS} from '@/ts/consts/routerPathConstants'

export const constantRoutes: Array<RouteConfig> = [
    {
        path: `${ROUTE_PATHS.PATH_LOGIN}`,
        component: () => import('@/views/login.vue'),
    },
    {
        path: `${ROUTE_PATHS.PATH_FILELIST}`,
        component: Layout,
        children: [
            {
                path: '',
                component: () => import('@/views/filelist/list.vue'),
                name: 'FileList',
                meta: {title: 'FileList'},
            },
        ],
    },
    {
        path: '/',
        component: Layout,
        redirect: `${ROUTE_PATHS.PATH_LOGIN}`,
    },
]

export const dynamicRoutes = [
    {
        path: `${ROUTE_PATHS.PATH_SYSTEM_CONFIG}`,
        component: Layout,
        alwaysShow: false,
        meta: {
            title: `system config`,
        },
        children: [
            {
                path: ``,
                component: () => import(`@/views/system_config/index.vue`),
                meta: {
                    title: `SystemConfig`,
                },
            },
        ],
    },
]

export let routes_for_sidebar = []

export function get_routes_for_sidebar() {
    return routes_for_sidebar
    // return get_from_local('routes')
}

export function set_routes_for_sidebar(routes) {
    // debugger
    // remove_comp_attr(routes)
    routes_for_sidebar = routes
    // set_local('routes', routes)
    // set_cookie('routes', [{ key: 'hello', world: 'world' }], 1000000)
}

/**
 * 初始化sidebar路由为原路由
 */
export function reset_sidebar_routes() {
    set_routes_for_sidebar([])
}

const createRouter = () =>
    new VueRouter({
        mode: 'history',
        base: process.env.BASE_URL,
        routes: constantRoutes,
    })

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
    const newRouter = createRouter()
    // @ts-ignore
    router.matcher = newRouter.matcher // reset router
}

export default router
