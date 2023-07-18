package fit.wenchao.http_file_server.dao.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import fit.wenchao.http_file_server.dao.po.SystemConfigPO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface SystemConfigMapper : BaseMapper<SystemConfigPO> {
}
