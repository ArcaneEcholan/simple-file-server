package fit.wenchao.http_file_server.dao.po
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
@TableName("`user`")
data class UserPO (
@TableId(value="id", type=IdType.AUTO)
var id: Long ?,
var username: String ?,
var password: String ?,
var ctime: String ?,
var mtime: String ?
)
: Serializable 