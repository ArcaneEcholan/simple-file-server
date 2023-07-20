package fit.wenchao.http_file_server.rest

import fit.wenchao.http_file_server.ConfigFile
import fit.wenchao.http_file_server.constants.API_PREFIX
import fit.wenchao.http_file_server.constants.CommonConsts
import fit.wenchao.http_file_server.constants.PermissionConstants
import fit.wenchao.http_file_server.exception.BackendException
import fit.wenchao.http_file_server.exception.JsonResult
import fit.wenchao.http_file_server.exception.RespCode
import fit.wenchao.http_file_server.model.ConfigPO
import fit.wenchao.http_file_server.service.AuthLogin
import fit.wenchao.http_file_server.service.PermissionRequired
import fit.wenchao.http_file_server.utils.ExceptionUtils
import fit.wenchao.http_file_server.utils.ResponseEntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotBlank

@Validated
@RestController
@RequestMapping(API_PREFIX)
class ConfigController {

    @Autowired
    lateinit var configFile: ConfigFile

    @AuthLogin
    @PermissionRequired([PermissionConstants.SYSTEM_CONFIG])
    @GetMapping("/config/list")
    fun getConfigList(): ResponseEntity<JsonResult> {
        val configMap = try {
            configFile.listConfigurations()
        } catch (e: Exception) {
            val rootCause = ExceptionUtils.getRootCause(e)
            if (rootCause is java.nio.file.NoSuchFileException) {
                throw BackendException(CommonConsts.CONFIG_FILE_NAME, RespCode.CONFIG_FILE_NOT_FOUND)
            }
            throw e
        }

        val configPOList = mutableListOf<ConfigPO>()
        for ((key, value) in configMap) {
            val conf = ConfigPO().apply {
                this.key = key
                this.value = value
            }
            configPOList.add(conf)
        }

        return ResponseEntityUtils.ok(JsonResult.ok(configPOList))
    }
    @AuthLogin
    @PutMapping("/config")
    fun setConfigValue(@NotBlank key: String, @NotBlank value: String): ResponseEntity<JsonResult> {
        if ("max-upload-size" == key) {
            val valueLong = try {
                value.toLong()
            } catch (e: NumberFormatException) {
                throw BackendException(value, RespCode.FRONT_END_PARAMS_ERROR)
            }

            if (valueLong > 2048 || valueLong <= 0) {
                throw BackendException(value, RespCode.MAX_UPLOAD_SIZE_TOO_LARGE)
            }
        }

        try {
            configFile.setProp(key, value)
        } catch (e: Exception) {
            val rootCause = ExceptionUtils.getRootCause(e)
            if (rootCause is java.nio.file.NoSuchFileException) {
                throw BackendException(CommonConsts.CONFIG_FILE_NAME, RespCode.CONFIG_FILE_NOT_FOUND)
            }
            throw e
        }

        return ResponseEntityUtils.ok(JsonResult.ok())
    }
    @AuthLogin
    @GetMapping("/config")
    fun getConfigValue(@NotBlank key: String): ResponseEntity<JsonResult> {
        val prop: String? = try {
            configFile.getProp(key)
        } catch (e: Exception) {
            val rootCause = ExceptionUtils.getRootCause(e)
            if (rootCause is java.nio.file.NoSuchFileException) {
                throw BackendException(CommonConsts.CONFIG_FILE_NAME, RespCode.CONFIG_FILE_NOT_FOUND)
            }
            throw e
        }

        if (prop == null) {
            throw BackendException(key, RespCode.NO_CONFIG_KEY)
        }

        return ResponseEntityUtils.ok(JsonResult.ok(prop))
    }
}
