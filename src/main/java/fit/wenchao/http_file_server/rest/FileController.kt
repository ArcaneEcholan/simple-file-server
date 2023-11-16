package fit.wenchao.http_file_server.rest

import fit.wenchao.http_file_server.ConfigFile
import fit.wenchao.http_file_server.constants.API_PREFIX
import fit.wenchao.http_file_server.constants.DEFAULT_MAX_UPLOAD_FILE_SIZE
import fit.wenchao.http_file_server.constants.ONE_MB

import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.exception.JsonResult
import fit.wenchao.http_file_server.exception.RespCode
import fit.wenchao.http_file_server.model.vo.FileInfo
import fit.wenchao.http_file_server.model.vo.UploadFileInfo
import fit.wenchao.http_file_server.rest.fileFilters.*
import fit.wenchao.http_file_server.service.AuthLogin
import fit.wenchao.http_file_server.service.FileService
import fit.wenchao.http_file_server.service.ThreadAuthContext
import fit.wenchao.http_file_server.service.UserAccessDirectoryService
import fit.wenchao.http_file_server.utils.FilePathBuilder
import fit.wenchao.http_file_server.utils.ResponseEntityUtils
import fit.wenchao.http_file_server.utils.iflet
import org.apache.commons.io.IOUtils
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
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class FileListFilterOptsVO {

    @Valid
    var queryFilesOptions: MutableList<QueryFilesOptVO>? = mutableListOf()

    enum class FilterType {
        /**
         * value is a single entry, like: desc
         */
        SINGLE,

        /**
         * value is a list, like: 1, 2, 3
         */
        LIST
    }

    fun resolveFileListFilterOptions(): MutableList<QueryFilesOption> {
        var result = mutableListOf<QueryFilesOption>()
        queryFilesOptions?.forEach { filterConditionVO ->

            val key: String? = filterConditionVO.key
            val value: String? = filterConditionVO.value

            key ?: return@forEach
            value ?: return@forEach

            val type: String? = filterConditionVO.type
            // default type is SINGLE, Only support SINGLE now
            val confirmedType = type ?: FilterType.SINGLE.name

            if (confirmedType == FilterType.SINGLE.name) {
                val filterCondition = QueryFilesOption()
                filterCondition.key = key
                filterCondition.value = value
                filterCondition.type = confirmedType
                result.add(filterCondition)
            }

            // if (confirmedType == FilterType.LIST.name) {
            //
            // }

            // unknown filter type
            return@forEach
        }

        // we sort them in a preset way
        result = sortFileFilterOptions(result)

        return result
    }
}

data class QueryFilesOptVO(
    var type: String?,
    @NotNull
    var key: String?,
    @NotNull
    var value: String?,
) {

}

@Validated
@RestController
@RequestMapping(API_PREFIX)
class FileController(
    var appCtx: ApplicationContext,
    var configFile: ConfigFile,
    var fileService: FileService,
    var userAccessDirectoryService: UserAccessDirectoryService,
    var threadAuthContext: ThreadAuthContext
) {

    fun getRootPath(): String? {
        val entityId = threadAuthContext.getEntity()!!.getPrincipal().value()
        var root = userAccessDirectoryService.getUserAccessDirectory(entityId)
        return root
    }
    @AuthLogin
    @GetMapping("/file-list")
    fun getFileList(@RequestParam optsMap: MutableMap<String, String>, @NotBlank path: String): ResponseEntity<JsonResult> {

        // we get the filter options first
        var filterOptList = resolveFileListFilterOptions(optsMap)

        var filePath = path
        val rootPath = getRootPath()
        if (rootPath == "" || rootPath == null) {
            throw BackendException(rootPath, RespCode.ROOT_PATH_CONFIG_ERROR)
        }
        filePath = FilePathBuilder.ofPath()
            .ct(rootPath)
            .ct(filePath)
            .build()

        var fileInfos: MutableList<FileInfo>
        try {
            fileInfos = fileService.listFiles(filePath)
        }catch ( e: Exception){
            throw BackendException(null as Any?, rootPath, RespCode.ROOT_PATH_CONFIG_ERROR.getCode())
        }

        // we apply them to file list
        filterOptList.forEach { filterCondition ->
            val filter: Filter? = getFilter(appCtx, filterCondition)
            filter?.let {
                fileInfos = it.processFileList(fileInfos, filterCondition.value)
            }
        }

        return ResponseEntityUtils.ok(JsonResult.ok(fileInfos))


    }

    @AuthLogin
    @GetMapping("/file")
    fun downloadFile(
        @NotNull @NotBlank path: String
    ): ResponseEntity<StreamingResponseBody?>? {

        var filePath = path
        val rootPath = getRootPath()
        if (rootPath == "" || rootPath == null) {
            throw BackendException(rootPath, RespCode.ROOT_PATH_CONFIG_ERROR)
        }

        filePath = rootPath + filePath
//        filePath = FilePathBuilder.ofPath()
//            .ct(rootPath)
//            .ct(filePath)
//            .build()

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

    @AuthLogin
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
        if (rootPath == "" || rootPath == null) {
            throw BackendException(rootPath, RespCode.ROOT_PATH_CONFIG_ERROR)
        }
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
            throw BackendException(null ,"File Exists", "FILE_EXISTS");
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
