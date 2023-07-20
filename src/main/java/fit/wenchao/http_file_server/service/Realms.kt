package fit.wenchao.http_file_server.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import fit.wenchao.http_file_server.dao.po.RolePO
import fit.wenchao.http_file_server.dao.po.RolePermissionPO
import fit.wenchao.http_file_server.dao.po.UserPO
import fit.wenchao.http_file_server.dao.po.UserRolePO
import fit.wenchao.http_file_server.dao.repo.*
import fit.wenchao.http_file_server.interceptor.AuthExceptionThreadLocal
import fit.wenchao.http_file_server.utils.JwtUtils
import io.jsonwebtoken.ExpiredJwtException
import mu.KotlinLogging
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.BearerToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Provide authentication information to shiro api
 */
interface AuthInfoAggregator {

    /**
     * Check if the entity exists, if exists, return the identification of the entity, otherwise return null
     *
     * @param id the identification of the entity
     * @return the identification of the entity if exists, otherwise null
     */
    fun entityExists(id: EntityIdentification): EntityIdentification?

    /**
     * Check if the entity exists, if exists, return the identification of the entity, otherwise return null
     *
     * @param username the username of the entity
     * @param password the password of the entity
     * @return the identification of the entity if exists, otherwise null
     */
    fun entityExists(username: String, password: String): EntityIdentification?

    /**
     * Get the roles of the entity
     *
     * @param id the identification of the entity
     * @return the roles of the entity
     */
    fun getEntityRoles(id: EntityIdentification): List<Role>

    /**
     * Get the permissions of the entity
     *
     * @param id the identification of the entity
     * @return the permissions of the entity
     */
    fun getEntityPermissions(id: EntityIdentification): PermissionCollection


}

@Component
class AuthInfoAggregatorImpl : AuthInfoAggregator {
    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var userRoleDao: UserRoleDao

    @Autowired
    lateinit var rolePermissionDao: RolePermissionDao

    @Autowired
    lateinit var roleDao: RoleDao

    @Autowired
    lateinit var permissionDao: PermissionDao

    override fun entityExists(id: EntityIdentification): EntityIdentification? {
        userDao.getById(id.identification())?.let { return id } ?: return null
    }

    override fun entityExists(username: String, password: String): EntityIdentification? {
        userDao.getOne(QueryWrapper<UserPO>().eq("username", username).eq("password", password), false)
            ?.let { return NumberEntityIdentification(it.id!!.toLong()) } ?: return null
    }

    override fun getEntityRoles(id: EntityIdentification): List<Role> {
        if (id.entityType().getName() == "user") {
            userDao.getById(id.identification())
                ?.let { user ->
                    return userRoleDao.list(QueryWrapper<UserRolePO>().eq("user_id", user.id))
                        .mapNotNull { userRolePO ->
                            roleDao.getById(userRolePO.roleId)
                        }.map { SimpleRole().apply { name = it.name ?: "" } }
                        .toList()
                } ?: return listOf()
        } else {
            throw RuntimeException("unsupported entity type")
        }
    }

    override fun getEntityPermissions(id: EntityIdentification): PermissionCollection {
        val entityRoles = this.getEntityRoles(id)
        val isSuperAdmin = entityRoles.filter { it.toString() == "super-admin" }.size == 1
        if (isSuperAdmin) {
            return getAllPermissions()
        }

        var permCollection = PermissionCollectionImpl()
        getEntityRoles(id).forEach { role ->
            var perms: PermissionCollection = roleDao.getOne(QueryWrapper<RolePO>().eq("name", role.toString()), false)
                ?.let { rolePO ->
                    var permsOfOneRole =
                        rolePermissionDao.list(QueryWrapper<RolePermissionPO>().eq("role_id", rolePO.id))
                            .mapNotNull { userRolePO ->
                                permissionDao.getById(userRolePO.permissionId)
                            }.mapNotNull { SimplePermission(it.name!!) }.toMutableList()
                    return@let PermissionCollectionImpl().apply { this.list.addAll(permsOfOneRole) }
                } ?: PermissionCollectionImpl()
            permCollection.merge(perms)
        }

        return permCollection
    }

    fun getAllPermissions(): PermissionCollection {
        permissionDao.list().mapNotNull { SimplePermission(it.name!!) }.toMutableList().let {
            return PermissionCollectionImpl().apply { this.list.addAll(it) }
        }
    }

}

abstract class BaseRealm(var authInfoAggregator: AuthInfoAggregator) : AuthorizingRealm() {
    abstract override fun doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo

    override fun doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo {

        var entityIdentification = principals.primaryPrincipal as EntityIdentification

        val entityPermissions = authInfoAggregator.getEntityPermissions(entityIdentification)

        val entityRoles = authInfoAggregator.getEntityRoles(entityIdentification)

        val authorizationInfo = SimpleAuthorizationInfo()
        authorizationInfo.roles = entityRoles.map { it.toString() }.toSet()
        authorizationInfo.stringPermissions = entityPermissions.getPermissions().map { it.toString() }.toSet()

        return authorizationInfo
    }
}

/**
 * Realm for username and password authentication
 */
class UsernamePasswordRealm(
    var authExceptionThreadLocal: AuthExceptionThreadLocal,
    authInfoAggregator: AuthInfoAggregator,
) : BaseRealm(authInfoAggregator) {

    override fun supports(token: AuthenticationToken?): Boolean {
        return token != null && token is org.apache.shiro.authc.UsernamePasswordToken
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo {
        var usernamePasswordToken = token as org.apache.shiro.authc.UsernamePasswordToken
        var entityId: EntityIdentification? =
            authInfoAggregator.entityExists(usernamePasswordToken.username, String(usernamePasswordToken.password))
        entityId?.let {
            return SimpleAuthenticationInfo(entityId, usernamePasswordToken.password, name)
        } ?: run {
            authExceptionThreadLocal.set(AuthcException(AuthErrorCode.ENTITY_NOT_EXISTED.name, null))
            throw RuntimeException()
        }
    }

}

/**
 * Realm for token authentication
 */
class TokenRealm(var authExceptionThreadLocal: AuthExceptionThreadLocal, authInfoAggregator: AuthInfoAggregator) :
    BaseRealm(authInfoAggregator) {

    private val log = KotlinLogging.logger {}

    override fun supports(token: AuthenticationToken?): Boolean {
        return token != null && token is BearerToken
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo {
        var bearerToken = token as BearerToken

        var entityId: EntityIdentification = getEntityIdFromToken(bearerToken)

        var entityId1 = authInfoAggregator.entityExists(entityId)
        entityId1?.let {
            log.debug { "Authentication passed, entityId: $entityId1" }
            return SimpleAuthenticationInfo(entityId, bearerToken.token, name)
        } ?: run {
            authExceptionThreadLocal.set(AuthcException(AuthErrorCode.ENTITY_NOT_EXISTED.name, null))
            throw RuntimeException()
        }
    }

    private fun getEntityIdFromToken(token: BearerToken): EntityIdentification {

        val tokenValue = token.token
        try {
            val userUid = JwtUtils.getIdFromToken(tokenValue)
            userUid ?: run {
                authExceptionThreadLocal.set(AuthcException(AuthErrorCode.AUTHC_ERROR.name, null))
                throw RuntimeException()
            }

            var numberId: Long? = null;
            try {
                numberId = userUid.toLong()
            } catch (e: NumberFormatException) {
                authExceptionThreadLocal.set(AuthcException(AuthErrorCode.TOKEN_INVALID.name, null))
                throw RuntimeException()
            }
            log.debug { "Entity identification: $numberId" }
            return NumberEntityIdentification(numberId)
        } catch (e: ExpiredJwtException) {
            authExceptionThreadLocal.set(AuthcException(AuthErrorCode.TOKEN_EXPIRED.name, null))
            throw RuntimeException()
        }
    }
}


