package fit.wenchao.http_file_server.dao.po
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
@TableName("`user_acc_dir`")
data class UserAccDirPO (
@TableId(value="id", type=IdType.AUTO)
var id: Long ?,
var userId: Long ?,
var accDir: String ?
)
: Serializable 