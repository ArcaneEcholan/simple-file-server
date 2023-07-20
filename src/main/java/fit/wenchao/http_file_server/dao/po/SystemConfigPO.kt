package fit.wenchao.http_file_server.dao.po

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`system_config`")
data class SystemConfigPO(
    @TableId(value = "id", type = IdType.AUTO)
    var id: Long?,

    /**
     * must not be null
     */
    @TableField("`key`")
    var key: String?,

    @TableField("`value`")
    /**
     * must not be null
     */
    var value: String?,
) : Serializable {
    // default
    constructor() : this(null, null, null)
}