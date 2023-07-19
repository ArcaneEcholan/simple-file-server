package fit.wenchao.http_file_server.dao.repo

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.mapper.PermissionMapper
import fit.wenchao.http_file_server.dao.po.PermissionPO
import org.springframework.stereotype.Repository

interface PermissionDao : IService<PermissionPO> {
    fun getOneByName(name: String): PermissionPO?
}

@Repository
class PermissionDaoImpl(var permissionMapper: PermissionMapper) :
    ServiceImpl<PermissionMapper, PermissionPO>(), PermissionDao {

    override fun getOneByName(name: String): PermissionPO? {
        val one = permissionMapper.selectOne(QueryWrapper<PermissionPO>().eq("name", name))
        return one
    }
}
