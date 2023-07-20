package fit.wenchao.http_file_server.dao.po

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`permission`")
data class PermissionPO(
    @TableId(value = "id", type = IdType.AUTO)
    var id: Long?,
    var name: String?,
    @TableField("`desc`")
    var desc: String?,
) : Serializable