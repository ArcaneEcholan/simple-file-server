package fit.wenchao.http_file_server.dao.repo
import fit.wenchao.http_file_server.dao.po.RolePermissionPO
import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.mapper.RolePermissionMapper
import org.springframework.stereotype.Repository

interface RolePermissionDao : IService<RolePermissionPO> {
}

@Repository
class RolePermissionDaoImpl : ServiceImpl<RolePermissionMapper,RolePermissionPO>() , RolePermissionDao {
}

