package fit.wenchao.http_file_server.dao.po
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.IdType
import java.io.Serializable
@TableName("`role_permission`")
data class RolePermissionPO (
@TableId(value="id", type=IdType.AUTO)
var id: Int ?,
var roleId: Int ?,
var permissionId: Int ?
)
: Serializable 