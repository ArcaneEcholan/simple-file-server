package fit.wenchao.http_file_server.service

import fit.wenchao.http_file_server.model.vo.FileInfo

interface FileService {
    fun listFiles(filePath: String): MutableList<FileInfo>


}