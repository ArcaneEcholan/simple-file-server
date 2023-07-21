package fit.wenchao.http_file_server.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import fit.wenchao.http_file_server.dao.po.UserAccDirPO
import fit.wenchao.http_file_server.dao.po.UserPO
import fit.wenchao.http_file_server.dao.repo.PermissionDao
import fit.wenchao.http_file_server.dao.repo.UserAccDirDao
import fit.wenchao.http_file_server.dao.repo.UserDao
import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.exception.RespCode
import fit.wenchao.http_file_server.rest.PermissionVO
import fit.wenchao.http_file_server.rest.UserVO
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.Serializable


interface UserAccessDirectoryService {
    fun getUserAccessDirectory(userId: Serializable): String?
    fun userHasAccessTo(userId: Serializable, directory: String): Boolean
    fun assignDirectoryTo(userId: Serializable, directory: String)
}

@Component
class UserAccessDirectoryServiceImpl(var userAccDirDao: UserAccDirDao) : UserAccessDirectoryService {

    override fun getUserAccessDirectory(userId: Serializable): String? {
        val one: UserAccDirPO? = userAccDirDao.getOne(QueryWrapper<UserAccDirPO>().eq("user_id", userId), false)
        return one?.accDir
    }

    override fun userHasAccessTo(userId: Serializable, directory: String): Boolean {
        getUserAccessDirectory(userId)?.let {
            return it == directory
        } ?: return false
    }

    override fun assignDirectoryTo(userId: Serializable, directory: String) {
        val one = userAccDirDao.getOne(QueryWrapper<UserAccDirPO>().eq("user_id", userId), false)
        if (one == null) {
            userAccDirDao.save(UserAccDirPO(null, userId as Long, directory))
        } else {
            one.accDir = directory
            userAccDirDao.updateById(one)
        }
    }

}


interface UserService {
    fun getUserByUsername(principal: String): UserPO?
    fun getUserVOById(principal: Serializable): UserVO?
    fun getUserById(id: Serializable): UserPO?
    fun listAll(): List<UserVO>
     fun addUser(username: String, password: String): UserVO
}

@Service
class UserServiceImpl : UserService {

    private final val userDao: UserDao

    var permissionDao: PermissionDao


    var userAccessDirectoryService: UserAccessDirectoryService

    private final val authInfoAggregator: AuthInfoAggregator

    constructor(
        userDao: UserDao,
        authInfoAggregator: AuthInfoAggregator,
        permissionDao: PermissionDao,
        userAccessDirectoryService: UserAccessDirectoryService,
    ) {
        this.userDao = userDao
        this.authInfoAggregator = authInfoAggregator
        this.permissionDao = permissionDao
        this.userAccessDirectoryService = userAccessDirectoryService
    }

    override fun getUserByUsername(principal: String): UserPO? {
        return userDao.getOne(QueryWrapper<UserPO>().eq("username", principal), false)
    }

    override fun getUserById(id: Serializable): UserPO? = userDao.getOne(QueryWrapper<UserPO>().eq("id", id), false)

    override fun listAll(): List<UserVO> {
        val list = userDao.list(QueryWrapper<UserPO>().select("id"))
        list.map { it.id }.filterNotNull().map { getUserVOById(it) }.filterNotNull().let { return it }
    }

    override fun addUser(username: String, password: String): UserVO {
        userDao.getOne(QueryWrapper<UserPO>().eq("username", username), false)?.let {
            throw BackendException(null, RespCode.USER_EXISTS)
        }
        val userPO = UserPO().apply {
            this.username = username
            this.password = password
        }
        userDao.save(userPO)
        return getUserVOById(userPO.id!!)!!
    }

    override fun getUserVOById(principal: Serializable): UserVO? {
        this.getUserById(principal)?.let {
            return UserVO().apply {
                this.id = it.id
                username = it.username

                var identification = NumberEntityIdentification(it.id!!)
                val toMutableList = authInfoAggregator.getEntityPermissions(identification).getPermissions()
                    .map {
                        val name = it.toString()
                        val oneByName = permissionDao.getOneByName(name)
                        oneByName?.let {
                            PermissionVO().apply {
                                this.id = it.id
                                this.name = it.name
                                this.desc = it.desc
                            }
                        }
                    }.filterNotNull().toMutableList()
                perms = toMutableList

                this.accDir = userAccessDirectoryService.getUserAccessDirectory(it.id!!)
            }
        } ?: return null
    }
}
