package fit.wenchao.http_file_server.dao.po
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
@TableName("`role`")
data class RolePO (
@TableId(value="id", type=IdType.AUTO)
var id: Int ?,
var name: String ?,
@TableField("`desc`")
var desc: String ?
)
: Serializable 