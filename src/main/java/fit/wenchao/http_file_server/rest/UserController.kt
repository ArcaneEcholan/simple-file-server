package fit.wenchao.http_file_server.rest

import fit.wenchao.http_file_server.constants.API_PREFIX
import fit.wenchao.http_file_server.constants.PermissionConstants
import fit.wenchao.http_file_server.exception.JsonResult
import fit.wenchao.http_file_server.service.PermissionRequired
import fit.wenchao.http_file_server.service.ThreadAuthContext
import fit.wenchao.http_file_server.service.UserAccessDirectoryService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


class AssignUserAccessDirectoryRequest {
    @NotNull
    var userId: Long = -1

    @NotEmpty
    var directory: String = ""
}

interface UserAccessDirectoryController {

    fun getUserAccessDirectory(): String

    @PermissionRequired([PermissionConstants.USER_ACCESS_DIRECTORY])
    fun assignDirectoryTo(assignUserAccessDirectoryRequest: AssignUserAccessDirectoryRequest):
            Any
}

interface UserController {

    @PermissionRequired([PermissionConstants.USER])
    fun getUsers(): List<UserVO>

    @PermissionRequired([PermissionConstants.USER])
    fun getUserRoles(): List<RoleVO>

    @PermissionRequired([PermissionConstants.USER])
    fun getUserPermissions(): List<PermissionVO>

}


interface RoleController {
    @PermissionRequired([PermissionConstants.ROLE])
    fun getRoles(): List<RoleVO>

    @PermissionRequired([PermissionConstants.ROLE])
    fun getRolePermission(): List<PermissionVO>
}


interface PermissionController {
    @PermissionRequired([PermissionConstants.PERMISSION])
    fun getPermissions(): List<PermissionVO>
}


class RoleVO {
    var id: Long? = null
    var name: String? = null
    var desc: String? = null
}

const val GET_USER_ACCESS_DIRECTORY = "/user-access-directory"
const val ASSIGN_USER_ACCESS_DIRECTORY = "/user-access-directory"

@RestController
@Validated
@RequestMapping(API_PREFIX)
class UserAccessDirectoryControllerImpl(
    var userAccessDirectoryService: UserAccessDirectoryService,
    var threadAuthContext: ThreadAuthContext,
) : UserAccessDirectoryController {

    @GetMapping(GET_USER_ACCESS_DIRECTORY)
    override fun getUserAccessDirectory(): String {
        val entity = threadAuthContext.getEntity()
        val principal = entity!!.getPrincipal().value()
        val userAccessDirectory = userAccessDirectoryService.getUserAccessDirectory(principal)
        return userAccessDirectory ?: ""
    }

    @PutMapping(ASSIGN_USER_ACCESS_DIRECTORY)
    override fun assignDirectoryTo(
        @RequestBody assignUserAccessDirectoryRequest: AssignUserAccessDirectoryRequest,
    ): Any {
        userAccessDirectoryService.assignDirectoryTo(
            assignUserAccessDirectoryRequest.userId,
            assignUserAccessDirectoryRequest.directory
        )

        return JsonResult.ok()
    }
}

