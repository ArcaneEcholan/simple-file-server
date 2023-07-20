package fit.wenchao.http_file_server.dao.repo
import fit.wenchao.http_file_server.dao.po.UserRolePO
import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.mapper.UserRoleMapper
import org.springframework.stereotype.Repository

interface UserRoleDao : IService<UserRolePO> {
}

@Repository
class UserRoleDaoImpl : ServiceImpl<UserRoleMapper, UserRolePO>() , UserRoleDao {
}
