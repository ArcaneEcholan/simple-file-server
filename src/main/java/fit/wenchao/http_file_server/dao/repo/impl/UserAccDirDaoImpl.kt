package fit.wenchao.http_file_server.dao.repo.impl
import org.springframework.stereotype.Repository
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.po.UserAccDirPO
import fit.wenchao.http_file_server.dao.mapper.UserAccDirMapper
import fit.wenchao.http_file_server.dao.repo.UserAccDirDao
@Repository
class UserAccDirDaoImpl : ServiceImpl<UserAccDirMapper,UserAccDirPO>() , UserAccDirDao {
}
