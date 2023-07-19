package fit.wenchao.http_file_server.interceptor

import fit.wenchao.http_file_server.service.*
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse





@Component
class AuthcInterceptor: HandlerInterceptor {

    private val log = KotlinLogging.logger {}

    lateinit var authService: AuthService

    lateinit var threadAuthContext: ThreadAuthContext

    constructor(
        authService: AuthService,
        threadAuthContext: ThreadAuthContext,
    ) {
        this.authService = authService
        this.threadAuthContext = threadAuthContext
    }


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // auth first
        val token = getTokenFromRequestQueryOrHeader(request)

        var entity: Entity = authService.authenticate(token);

        threadAuthContext.bind(entity)

        var entryPoint = SpringHandlerMethodRequestEntryPoint(handlerMethod = handler as HandlerMethod)

        // authorize then
        authService.authorize(entity, entryPoint.getRequiredPermissions())

        log.info { "Authentication succeeded, entity id: ${entity.getPrincipal()}" }

        return true
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

        var entityTokenInQuery:String? = request.getParameter(tokenKey)
        var entityTokenInHeader:String? = request.getHeader(tokenKey)

        var token =  entityTokenInQuery ?: entityTokenInHeader


        token ?: run { throw RuntimeException("token missing") }
        return TokenToken().apply { this.token = token }
    }

}

