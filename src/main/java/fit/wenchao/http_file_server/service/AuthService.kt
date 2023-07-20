package fit.wenchao.http_file_server.service

import fit.wenchao.http_file_server.interceptor.AuthExceptionThreadLocal
import mu.KotlinLogging
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.BearerToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.util.ThreadContext
import org.springframework.stereotype.Service

enum class AuthErrorCode {
    TOKEN_INVALID,
    TOKEN_EXPIRED,
    UNSUPPORTED_TOKEN_TYPE,
    AUTHC_ERROR,
    AUTHZ_ERROR,
    ENTITY_NOT_AUTHCED,
    ENTITY_NOT_EXISTED
}

open class AuthException(msg: String?, e: Throwable?) : RuntimeException(msg, e) {
}

class AuthcException(msg: String?, e: Throwable?) : AuthException(msg, e) {

}

class AuthzException(msg: String?, e: Throwable?) : AuthException(msg, e) {

}

interface AuthService {
    fun authenticate(token: AuthToken): Entity
    fun authorize(entity: Entity, permissionsOfUri: PermissionCollection)

    fun authorizeOr(entity: Entity, permissionsOfUri: PermissionCollection)

    fun authorizeAnd(entity: Entity, permissionsOfUri: PermissionCollection)
}

@Service
class AuthServiceImpl(var authExceptionThreadLocal: AuthExceptionThreadLocal, var securityManager: SecurityManager) :
    AuthService {
    override fun authenticate(token: AuthToken): Entity {
        // bind security manager to thread context, so it can be access globally
        ThreadContext.bind(securityManager)
        val subject = SecurityUtils.getSubject()
        var shiroToken: AuthenticationToken
        if (token.getTokenType() is TokenTokenType) {
            shiroToken = BearerToken(token.getCredential().value())
        } else if (token.getTokenType() is UsernamePasswordTokenType) {
            shiroToken = UsernamePasswordToken(token.getPrincipal().value(), token.getCredential().value())
        } else {
            throw AuthcException(AuthErrorCode.UNSUPPORTED_TOKEN_TYPE.name, null)
        }
        authExceptionThreadLocal.set(null)

        // perform authentication
        try {
            subject.login(shiroToken)
        } catch (e: Exception) {
            val get = authExceptionThreadLocal.get()
            get ?: throw AuthcException(AuthErrorCode.AUTHC_ERROR.name, e)
            throw AuthcException(get.message, null)
        }

        val principal = subject.principal as EntityIdentification

        return SimpleEntity(
            EntityPrincipalImpl().apply { this.value = principal.identification().toString() },
            token.getCredential()
        )
            .apply { this.authenticated = true }
    }

    override fun authorize(entity: Entity, permissionsOfUri: PermissionCollection) {

        if (!entity.authced()) {
            throw AuthzException(AuthErrorCode.ENTITY_NOT_AUTHCED.name, null)
        }

        val shiroPermissions = permissionsOfUri.getShiroPermissions()

        val subject = SecurityUtils.getSubject()
        authExceptionThreadLocal.set(null)
        try {
            subject.checkPermissions(shiroPermissions)
        } catch (e: Exception) {
            val get = authExceptionThreadLocal.get()
            get ?: throw AuthzException(AuthErrorCode.AUTHZ_ERROR.name, e)
            throw AuthzException(get.message, null)
        }
    }

    private val log = KotlinLogging.logger {}
    override fun authorizeOr(entity: Entity, permissionsOfUri: PermissionCollection) {
        if (!entity.authced()) {
            throw AuthzException(AuthErrorCode.ENTITY_NOT_AUTHCED.name, null)
        }

        val shiroPermissions = permissionsOfUri.getShiroPermissions()

        val subject = SecurityUtils.getSubject()

        shiroPermissions.forEach {
            authExceptionThreadLocal.set(null)
            try {
                val permitted = subject.isPermitted(it)
                if (permitted) {
                    log.debug { "Permission ${it} passed, abort" }
                    return;
                }
            } catch (e: Exception) {
                log.warn { "Permission ${it} not passed" }
            }
        }

        throw AuthzException(AuthErrorCode.AUTHZ_ERROR.name, null)
    }

    override fun authorizeAnd(entity: Entity, permissionsOfUri: PermissionCollection) {
        if (!entity.authced()) {
            throw AuthzException(AuthErrorCode.ENTITY_NOT_AUTHCED.name, null)
        }

        val shiroPermissions = permissionsOfUri.getShiroPermissions()

        val subject = SecurityUtils.getSubject()

        shiroPermissions.forEach{
            authExceptionThreadLocal.set(null)
            try {
                val permitted = subject.isPermitted(it)
                if(permitted) {
                    log.debug { "Permission ${it} passed" }
                    return@forEach
                }
            } catch (e: Exception) {
                log.warn { "Permission ${it} not passed, abort" }
                val get = authExceptionThreadLocal.get()
                get?:throw AuthzException(AuthErrorCode.AUTHZ_ERROR.name, e)
                throw AuthzException( get.message, null)
            }
        }

        log.debug { "All permissions passed" }
    }

}