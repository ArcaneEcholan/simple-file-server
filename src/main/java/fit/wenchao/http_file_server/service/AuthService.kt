package fit.wenchao.http_file_server.service

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.BearerToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.util.ThreadContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


open class AuthException(msg: String?, e: Throwable?) :RuntimeException(msg, e){
}

class AuthcException(msg: String?, e: Throwable?) : AuthException(msg, e) {

}

class AuthzException(msg: String?, e: Throwable?) :AuthException(msg, e) {

}

interface AuthService {
    fun authenticate(token: AuthToken): Entity
    fun authorize(entity: Entity, permissionsOfUri: PermissionCollection)
}

@Service
class AuthServiceImpl : AuthService {
    @Autowired
    lateinit var securityManager: SecurityManager
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
            throw AuthException("Unsupported token type", null)
        }

        // perform authentication
        try {
            subject.login(shiroToken)
        } catch (e: Exception) {
            throw AuthcException("Authentication failed", null)
        }

        return SimpleEntity(token.getPrincipal(), token.getCredential()).apply { this.authenticated = true }
    }

    override fun authorize(entity: Entity, permissionsOfUri: PermissionCollection) {

        if (!entity.authced()) {
            throw AuthzException("Please authenticate the entity before authorization", null)
        }

        val shiroPermissions = permissionsOfUri.getShiroPermissions()

        val subject = SecurityUtils.getSubject()
        try {
            subject.checkPermissions(shiroPermissions)
        } catch (e: Exception) {
            throw AuthzException("Authorization failed", e)
        }
    }

}