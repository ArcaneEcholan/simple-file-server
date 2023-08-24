import {PathBuilder} from '@/ts/utils'
import {get_from_cookie, set_cookie} from '@/ts/utils'
import router, {
    constantRoutes,
    dynamicRoutes,
    resetRouter,
    set_routes_for_sidebar,
} from '@/router'
import store from '@/store'
import {PERM_PAGE_MAPPING} from '@/ts/consts/permissionConstants'

const COOKIE_TOKEN_KEY = 'token_key'
const COOKIE_REFRESH_TOKEN_KEY = 'refresh_token_key'

// ======== access token from cookie ========

export function get_token() {
    return get_from_cookie(COOKIE_TOKEN_KEY)
}

export function isAdmin() {
    // get roles from store
    const roles = store.getters.roles

    // verify roles
    if (roles && roles.length > 0) {
        const isAdmin = is_admin(roles)
        return isAdmin
    }

    return false
}

export function hasPerm(perms) {
    if (isAdmin()) {
        return true
    }

    // get user perms
    const userPerms = store.getters.perms

    // verify perms
    if (perms && perms.length > 0) {
        const hasPerms = userPerms.some((perm) => {
            return perms.includes(perm.name)
        })
        return hasPerms
    }

    return false
}

export function set_token(token, expiresTime) {
    set_cookie(COOKIE_TOKEN_KEY, token, expiresTime)
}

export function get_refresh_token() {
    return get_from_cookie(COOKIE_REFRESH_TOKEN_KEY)
}

export function set_refresh_token(token, expiresTime) {
    set_cookie(COOKIE_REFRESH_TOKEN_KEY, token, expiresTime)
}

// =========================== menu generation function utils ========================
function is_admin(roles) {
    // roles为空则一定不是管理员
    if (roles == undefined || roles.length == 0) {
        return false
    }

    // 查找roles中是否包含管理员
    let super_admin_idx = roles.findIndex(
        (elem) => elem.value === 'super-admin',
    )

    if (super_admin_idx != -1) {
        return true
    }

    return false
}

// generate accessible routes map based on roles
function filterAllPermittedRoutesAgainstPermittedMenusFromDynamicRoutes(menus, is_admin) {
    let accessedRoutes
    if (is_admin) {
        accessedRoutes = dynamicRoutes || []
    } else {
        accessedRoutes = filterAsyncRoutes(dynamicRoutes, menus, '')
    }
    if (
        !accessedRoutes.some((route) => {
            return route.path === '*'
        })
    ) {
        accessedRoutes.push({
            path: '*',
            redirect: '/404',
            hidden: true,
            auth: false,
        })
    }
    return accessedRoutes
}

function get_child_absolute_path(child_relative_path, parent_absolute_path) {
    return PathBuilder.of().l(parent_absolute_path).l(child_relative_path).end()
}

function filterAsyncRoutes(dynamicRoutes, menus, p_path) {
    const res: any[] = []

    dynamicRoutes.forEach((route) => {
        let full_path = get_child_absolute_path(route.path, p_path)

        const tmp = {...route}
        if (hasPermission(menus, tmp, full_path)) {
            if (tmp.children) {
                tmp.children = filterAsyncRoutes(tmp.children, menus, full_path)
            }
            res.push(tmp)
        }
    })

    return res
}

function hasPermission(menus, route, routeFullPath) {
    if (menus == undefined || menus.length === 0) {
        return false
    }
    return menus.some((menu) => {
        let menuPath = menu.path
        let routeFullPathLength = routeFullPath.length
        // equals
        if (menuPath === routeFullPath) {
            return true
        }
        // starts with
        if (menuPath.startsWith(routeFullPath)) {
            // /path/destination/some  /path/dest
            let nextCharInMenuPath = menuPath.substring(
                routeFullPathLength,
                routeFullPathLength + 1,
            )
            if (nextCharInMenuPath === '/') {
                return true
            }
        }

        return false
    })
}

export function processSideBarRoutes(login_info) {
    let apiTag = 'process_user_menu(): \n'

    // menus user permitted
    let user_allow_menus = login_info.menus

    // if the user is super admin
    let is_super_admin = is_admin(login_info.roles)

    // permitted routes in the dynamic routes part
    let permittedRoutesOfDynamicRoutes = filterAllPermittedRoutesAgainstPermittedMenusFromDynamicRoutes(user_allow_menus, is_super_admin);

    // concat permission free routes with permitted dynamic routes
    let userPermittedRoutes = constantRoutes.concat(permittedRoutesOfDynamicRoutes)

    // give routes to sidebar
    set_routes_for_sidebar(userPermittedRoutes)

    // //
    //
    // console.log(apiTag, 'permitRoutes: ', permitRoutes)
    // resetRouter()
    // // permitRoutes.forEach((route) => {
    // //     router.addRoute(route)
    // // })
    // // router.addRoute()
    // router.addRoutes(permitRoutes)
}

export function parse_user_info_response(user_info_resp) {
    // map all perms to pages
    let pages = {} // use map to avoid duplicate pages
    let perms = user_info_resp.perms
    perms.forEach((perm) => {
        let permName = perm.name
        let permPages = PERM_PAGE_MAPPING[permName]
        if (permPages == undefined) {
            // if permission has no corresponding page, skip it
            return
        }

        // put all pages to menus
        permPages.forEach((page) => {
            pages[page] = page
        })
    })

    // wrap all pages to menus
    let menus: any[] = []
    for (let page in pages) {
        menus.push({path: page})
    }

    return {
        id: user_info_resp.id,
        roles: user_info_resp.roles,
        dn: user_info_resp.dn,
        mail: user_info_resp.mail,
        ouPathList: user_info_resp.ouPathList,
        userAccount: user_info_resp.userAccount,
        username: user_info_resp.username,
        menus: menus,
        perms: user_info_resp.perms,

        samAccountName: user_info_resp.samAccountName, // deprecate
        uid: user_info_resp.uid, // deprecate
        cn: user_info_resp.cn, // deprecate
    }
}
