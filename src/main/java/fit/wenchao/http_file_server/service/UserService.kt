package fit.wenchao.http_file_server.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import fit.wenchao.http_file_server.dao.po.UserPO
import fit.wenchao.http_file_server.dao.repo.PermissionDao
import fit.wenchao.http_file_server.dao.repo.UserDao
import fit.wenchao.http_file_server.rest.PermissionVO
import fit.wenchao.http_file_server.rest.UserVO
import org.springframework.stereotype.Service

interface UserService {
    fun getUser(principal: String): UserPO?
    fun getUserVO(principal: String): UserVO?
}

@Service
class UserServiceImpl : UserService {

    private final val userDao: UserDao

    var permissionDao: PermissionDao

    private final val authInfoAggregator: AuthInfoAggregator

    constructor(
        userDao: UserDao,
        authInfoAggregator: AuthInfoAggregator,
        permissionDao: PermissionDao
    ) {
        this.userDao = userDao
        this.authInfoAggregator = authInfoAggregator
        this.permissionDao = permissionDao
    }

    override fun getUser(principal: String): UserPO? {
       return userDao.getOne(QueryWrapper<UserPO>().eq("username", principal), false)
    }

    override fun getUserVO(principal: String): UserVO? {
        this.getUser(principal)?.let {
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
                                this. id = it.id
                                this.name = it.name
                                this.desc = it.desc
                            }
                        }
                    }.filterNotNull().toMutableList()
                perms = toMutableList
            }
        } ?: return null
    }
}
