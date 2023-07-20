package fit.wenchao.http_file_server.model.vo;

data class FileInfo(
    var fileType: String = "file",
    var name: String = "",
    var path: String = "",
    var length: Long = 0,
    var type: Int = 0,
    var creationTime: String = "",
    var lastModifiedTime: String = "",
) {

}
