package fit.wenchao.http_file_server.service

import fit.wenchao.http_file_server.constants.FileType
import fit.wenchao.http_file_server.constants.RespCode
import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.model.vo.FileInfo
import fit.wenchao.http_file_server.utils.lastModifiedTime
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileServiceImpl : FileService {
    override fun listFiles(filePath: String): MutableList<FileInfo> {
        val file = File(filePath)

        // path is not valid
        if (!file.exists()) {
            throw BackendException(filePath, RespCode.NO_FILE)
        }

        val files: Array<File>? = file.listFiles()

        val fileInfoList: MutableList<FileInfo> = ArrayList()

        // no files available
        files ?: return fileInfoList

        for (everyFile in files) {

            val lastModifiedTime: String = lastModifiedTime(everyFile)
            val fileType: String = if (everyFile.isFile) "file" else "folder";
            val name = everyFile.name
            val absolutePath = everyFile.absolutePath
            val length = everyFile.length()
            val directory = everyFile.isDirectory

            val fileInfo = FileInfo(
                fileType = fileType,
                name = name, path = absolutePath, length = length,
                type = if (directory) FileType.DIR.code else FileType.FILE.code,
                lastModifiedTime = lastModifiedTime
            )

            fileInfoList.add(fileInfo)
        }

        return fileInfoList
    }
}