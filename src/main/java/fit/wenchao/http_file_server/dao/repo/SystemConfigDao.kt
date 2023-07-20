package fit.wenchao.http_file_server.dao.repo

import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import fit.wenchao.http_file_server.dao.mapper.SystemConfigMapper
import fit.wenchao.http_file_server.dao.po.SystemConfigPO
import org.springframework.stereotype.Repository

interface SystemConfigDao : IService<SystemConfigPO> {
}

@Repository
class SystemConfigDaoImpl : ServiceImpl<SystemConfigMapper, SystemConfigPO>(), SystemConfigDao {
}

