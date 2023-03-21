package fit.wenchao.http_file_server.rest

import fit.wenchao.http_file_server.ConfigFile
import fit.wenchao.http_file_server.constants.*
import fit.wenchao.http_file_server.eventListener.AEvent
import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.model.JsonResult
import fit.wenchao.http_file_server.model.vo.FileInfo
import fit.wenchao.http_file_server.model.vo.UploadFileInfo
import fit.wenchao.http_file_server.utils.FilePathBuilder
import fit.wenchao.http_file_server.utils.ResponseEntityUtils
import fit.wenchao.http_file_server.utils.iflet
import fit.wenchao.http_file_server.utils.lastModifiedTime
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Validated
@RestController
@RequestMapping("/API")
class FileController {

    @Autowired
    lateinit var appCtx: ApplicationContext

    @Autowired
    lateinit var configFile: ConfigFile

    fun getRootPath(): String {
        var root: String? = configFile.getProp("root")

        if ("" == root) {
            root = System.getProperty("user.home")
        }

        return root ?: ""
    }


    @GetMapping("/file-list")
    fun getFileList(@NotBlank path: String): ResponseEntity<JsonResult> {
        appCtx.publishEvent(AEvent("chaowen"))
        var filePath = path
        val rootPath = getRootPath()
        if (rootPath == "") {
            throw BackendException(rootPath, RespCode.ROOT_PATH_CONFIG_ERROR)
        }
        filePath = FilePathBuilder.ofPath()
            .ct(rootPath)
            .ct(filePath)
            .build()
        val file = File(filePath)

        // path is not valid
        if (!file.exists()) {
            throw BackendException(filePath, RespCode.NO_FILE)
        }
        val files: Array<File>? = file.listFiles()
        val fileInfoList: MutableList<FileInfo> = ArrayList()

        files?.let {
            for (everyFile in it) {

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
        }

        return ResponseEntityUtils.ok(JsonResult.ok(fileInfoList))
    }

    @GetMapping("/file")
    fun downloadFile(
        @NotNull @NotBlank path: String
    ): ResponseEntity<StreamingResponseBody?>? {
        var filePath = path
        val rootPath = getRootPath()
        if (rootPath == "") {
            throw BackendException(rootPath, RespCode.ROOT_PATH_CONFIG_ERROR)
        }
        filePath = FilePathBuilder.ofPath()
            .ct(rootPath)
            .ct(filePath)
            .build()

        val file = File(filePath)

        // path is not valid
        if (!file.exists()) {
            throw BackendException(filePath, RespCode.NO_FILE)
        }
        if (file.isDirectory) {
            throw BackendException(filePath, RespCode.DOWNLOAD_NOT_SUPPORT)
        }
        val headers = HttpHeaders()
        headers.add("Access-Control-Expose-Headers", "Content-Disposition")
        headers.add(
            "Content-Disposition",
            "attachment; filename=" + String(
                file.name
                    .toByteArray(charset("UTF-8")), Charsets.ISO_8859_1
            )
        )
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")
        headers.add("Content-Type", "application/octet-stream")
        headers.add(HttpHeaders.CONTENT_LENGTH, file.length().toString())
        val finalPath = filePath
        return ResponseEntity
            .ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(StreamingResponseBody { outputStream: OutputStream? ->
                try {
                    Files.newInputStream(Paths.get(finalPath)).use { inputStream ->
                        IOUtils.copyLarge(
                            inputStream,
                            outputStream
                        )
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            })
    }


    @PostMapping("/file")
    fun uploadFile(
        @RequestPart("file-body") multipartFile: MultipartFile,
        @RequestPart("file-info") @Validated uploadFileInfo: UploadFileInfo
    ): ResponseEntity<JsonResult> {

        // check if the size of upload file is valid
        val size = multipartFile.size
        val maxSizeValue: String? = configFile.getProp("max-upload-size")


        var maxSize: Long = 0L

        maxSizeValue.iflet {

            if (it == "") {
                maxSize = DEFAULT_MAX_UPLOAD_FILE_SIZE
            }

            maxSize = it.toLong()
            maxSize *= ONE_MB
        }.elselet {
            // config is not present, set default max size value, it can not
            // exceed configuration in application.yml
            maxSize = DEFAULT_MAX_UPLOAD_FILE_SIZE
        }

        if (size > maxSize) {
            throw BackendException(
                null,
                RespCode.UPLOAD_FILE_SIZE_EXCEED_UPPER_LIMIT
            )
        }

        // file must have a name
        val filename = multipartFile.originalFilename ?: throw BackendException(null, RespCode.FILE_UPLOAD_ERROR)

        // this is relative upload path
        var path = uploadFileInfo.path

        // build the absolute path
        val rootPath = getRootPath()
        path = FilePathBuilder.ofPath()
            .ct(rootPath)
            .ct(path)
            .build()
        val uploadDir = File(path)
        if (uploadDir.isFile) {
            throw BackendException(path, RespCode.UPLOAD_DEST_ERROR)
        }
        val uploadPath = Paths.get(path)

        // make upload dir if not exists
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath)
            } catch (accessDeniedException: AccessDeniedException) {
                throw BackendException(path, RespCode.FILE_ACCESS_DENIED)
            }
        }
        val uploadFilePath: Path = uploadPath.resolve(filename)
        // check if file exists already
        val exists = Files.exists(uploadFilePath)
        if (exists) {
            throw FileAlreadyExistsException(uploadFilePath.toString())
        }
        try {
            // copy file from multipart
            Files.createFile(uploadFilePath)
            Files.copy(
                multipartFile.inputStream,
                uploadFilePath,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (accessDeniedException: AccessDeniedException) {
            throw BackendException(path, RespCode.FILE_ACCESS_DENIED)
        }
        return ResponseEntityUtils.ok(JsonResult.ok())
    }
}

fun main() {

}