package fit.wenchao.http_file_server.dao.repo
import fit.wenchao.http_file_server.dao.po.RolePO
import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.mapper.RoleMapper
import org.springframework.stereotype.Repository

interface RoleDao : IService<RolePO> {
}

@Repository
class RoleDaoImpl : ServiceImpl<RoleMapper, RolePO>() , RoleDao {
}
