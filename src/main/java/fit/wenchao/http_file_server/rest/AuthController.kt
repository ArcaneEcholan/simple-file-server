package fit.wenchao.http_file_server.rest

import fit.wenchao.http_file_server.config.PATH_AUTH_USER
import fit.wenchao.http_file_server.constants.API_PREFIX
import fit.wenchao.http_file_server.constants.EntityType
import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.exception.RespCode
import fit.wenchao.http_file_server.service.*
import fit.wenchao.http_file_server.utils.JwtUtils
import mu.KotlinLogging
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.NotEmpty

const val GET_USER_INFO = "/user-info"


class UserVO {
    var id: Long? = null

    /**
     * user account, unique
     */
    var username: String? = null

    var perms: MutableList<PermissionVO>? = null

    var accDir: String? = null
}

class PermissionVO {
    var id: Long? = null

    /**
     * permission name, unique
     */
    var name: String? = null

    /**
     * permission description
     */
    var desc: String? = null
}

@RestController
@Validated
@RequestMapping(API_PREFIX)
class AuthController {

    private val log = KotlinLogging.logger {}

    private final val authService: AuthService

    private final val userService: UserService

    var threadAuthContext: ThreadAuthContext

    constructor(
        authService: AuthService,
        userService: UserService,
        threadAuthContext: ThreadAuthContext,
    ) {
        this.authService = authService
        this.userService = userService
        this.threadAuthContext = threadAuthContext
    }

    @GetMapping(PATH_AUTH_USER)
    fun authUser(@NotEmpty username: String, @NotEmpty password: String): Any {
        var token = UsernamePasswordToken(username, password)
        try {
            val entity = authService.authenticate(token)
            val principal = entity.getPrincipal()
            log.debug { "Authentication successfully, principal: ${principal.value()}" }
            val genToken = JwtUtils.genToken(principal.value(), EntityType.WEB_USER)
            return genToken
        }catch (e: AuthcException) {
            if(e.message == AuthErrorCode.TOKEN_INVALID.name)
                throw BackendException(e, null, RespCode.AUTH_FAILED)
            else if(e.message == AuthErrorCode.TOKEN_EXPIRED.name)
                throw BackendException(e, null, RespCode.TOKEN_EXPIRED)
            else if(e.message == AuthErrorCode.UNSUPPORTED_TOKEN_TYPE.name)
                throw BackendException(e, null, RespCode.TOKEN_INVALID)
            else if(e.message == AuthErrorCode.ENTITY_NOT_EXISTED.name)
                throw BackendException(e, null, RespCode.USER_NOT_FOUND)
            else if(e.message == AuthErrorCode.AUTHC_ERROR.name)
                throw BackendException(e, null, RespCode.AUTH_FAILED)
            else
                throw BackendException(e, null, RespCode.AUTH_FAILED)
        }
    }

    @AuthLogin
    @GetMapping(GET_USER_INFO)
    fun getUserInfo(httpServletRequest: HttpServletRequest): Any {
        val entity: Entity = threadAuthContext.getEntity()!!
        val userVO: UserVO? = userService.getUserVOById(entity.getPrincipal().value())
        userVO ?: throw BackendException(null, RespCode.USER_NOT_FOUND)
        return userVO
    }
}