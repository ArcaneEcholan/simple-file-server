package fit.wenchao.http_file_server.dao.repo
import fit.wenchao.http_file_server.dao.po.UserPO
import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.mapper.UserMapper
import org.springframework.stereotype.Repository

interface UserDao : IService<UserPO> {
}

@Repository
class UserDaoImpl : ServiceImpl<UserMapper,UserPO>() , UserDao {
}

