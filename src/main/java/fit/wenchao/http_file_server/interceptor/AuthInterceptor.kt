package fit.wenchao.http_file_server.interceptor

import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.exception.RespCode
import fit.wenchao.http_file_server.service.*
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthExceptionThreadLocal {
    private val tl: ThreadLocal<Throwable> = ThreadLocal()
    fun get(): Throwable? {
        return tl.get()
    }

    fun set(e: Throwable?) {
        return tl.set(e)
    }
}

const val THREAD_LOCAL_AUTH_EXCEPTION = "THREAD_LOCAL_AUTH_EXCEPTION"

@Component
class AuthcInterceptor : HandlerInterceptor {


    private val log = KotlinLogging.logger {}

    var authService: AuthService

    var threadAuthContext: ThreadAuthContext

    constructor(
        authService: AuthService,
        threadAuthContext: ThreadAuthContext,
    ) {
        this.authService = authService
        this.threadAuthContext = threadAuthContext
    }


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        try {
            // auth first
            val token = getTokenFromRequestQueryOrHeader(request)

            var entity: Entity = authService.authenticate(token);

            threadAuthContext.bind(entity)

            var entryPoint = SpringHandlerMethodRequestEntryPoint(handlerMethod = handler as HandlerMethod)

            // authorize then
            authService.authorize(entity, entryPoint.getRequiredPermissions())

            log.info { "Authentication succeeded, entity id: ${entity.getPrincipal()}" }

            return true
        } catch (e: AuthcException) {
            if (e.message == AuthErrorCode.TOKEN_INVALID.name)
                throw BackendException(e, null, RespCode.AUTH_FAILED)
            else if (e.message == AuthErrorCode.TOKEN_EXPIRED.name)
                throw BackendException(e, null, RespCode.TOKEN_EXPIRED)
            else if (e.message == AuthErrorCode.UNSUPPORTED_TOKEN_TYPE.name)
                throw BackendException(e, null, RespCode.TOKEN_INVALID)
            else if (e.message == AuthErrorCode.ENTITY_NOT_EXISTED.name)
                throw BackendException(e, null, RespCode.USER_NOT_FOUND)
            else if (e.message == AuthErrorCode.AUTHC_ERROR.name)
                throw BackendException(e, null, RespCode.AUTH_FAILED)
            else
                throw BackendException(e, null, RespCode.AUTH_FAILED)
        } catch (e: AuthzException) {
            if (e.message == AuthErrorCode.ENTITY_NOT_AUTHCED.name)
                throw BackendException(e, null, RespCode.SERVER_ERROR)
            else
                throw BackendException(e, null, RespCode.PERMISSION_INADEQUATE)
        }

    }

    /*
     * get token from request
     */
    private fun getToken(request: HttpServletRequest): AuthToken {
        var token: String? = request.getParameter("token")
        token ?: run { throw RuntimeException("token missing") }
        return TokenToken().apply { this.token = token }
    }


    // get token from request
    private fun getTokenFromRequestQueryOrHeader(request: HttpServletRequest): TokenToken {

        // this code is used to be compatible with poor design of multiple entity token authentication

        var tokenKey = "entity-token"

        var entityTokenInQuery: String? = request.getParameter(tokenKey)
        var entityTokenInHeader: String? = request.getHeader(tokenKey)

        var token = entityTokenInQuery ?: entityTokenInHeader


        token ?: run { throw RuntimeException("token missing") }
        return TokenToken().apply { this.token = token }
    }

}

