package fit.wenchao.http_file_server.rest;

import fit.wenchao.http_file_server.ConfigFile;
import fit.wenchao.http_file_server.constants.CommonConsts;
import fit.wenchao.http_file_server.constants.RespCode;
import fit.wenchao.http_file_server.exception.BackendException;
import fit.wenchao.http_file_server.model.JsonResult;
import fit.wenchao.http_file_server.utils.ExceptionUtils;
import fit.wenchao.http_file_server.utils.ResponseEntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
@Validated
@RestController
public class ConfigController {

    @Autowired
    ConfigFile configFile;

    @GetMapping("/config/list")
    public ResponseEntity<JsonResult> getConfigList() {

        Map<String, String> configMap = null;
        try {
            configMap = configFile.listConfigurations();
        }
        catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if(rootCause instanceof java.nio.file.NoSuchFileException) {
                throw new BackendException(CommonConsts.CONFIG_FILE_NAME,
                        RespCode.CONFIG_FILE_NOT_FOUND );
            }
            throw e;
        }
        return ResponseEntityUtils.ok(JsonResult.ok(configMap));
    }

    @PutMapping("/config")
    public ResponseEntity<JsonResult> setConfigValue(@NotBlank String key,
                                                     @NotBlank String value) {

        try {
            configFile.setProp(key, value);
        }
        catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if(rootCause instanceof java.nio.file.NoSuchFileException) {
                throw new BackendException(CommonConsts.CONFIG_FILE_NAME,
                        RespCode.CONFIG_FILE_NOT_FOUND );
            }
            throw e;
        }
        return ResponseEntityUtils.ok(JsonResult.ok());
    }


}
