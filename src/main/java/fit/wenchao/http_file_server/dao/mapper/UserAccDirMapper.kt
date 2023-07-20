package fit.wenchao.http_file_server.dao.mapper
import fit.wenchao.http_file_server.dao.po.UserAccDirPO
import org.apache.ibatis.annotations.Mapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
@Mapper
interface UserAccDirMapper : BaseMapper<UserAccDirPO> {
}
