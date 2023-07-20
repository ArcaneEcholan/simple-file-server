package fit.wenchao.http_file_server.dao.mapper
import fit.wenchao.http_file_server.dao.po.RolePO
import org.apache.ibatis.annotations.Mapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
@Mapper
interface RoleMapper : BaseMapper<RolePO> {
}
