package fit.wenchao.http_file_server.rest;

import fit.wenchao.http_file_server.ConfigFile;
import fit.wenchao.http_file_server.constants.CommonConsts;
import fit.wenchao.http_file_server.constants.RespCode;
import fit.wenchao.http_file_server.exception.BackendException;
import fit.wenchao.http_file_server.model.ConfigPO;
import fit.wenchao.http_file_server.model.JsonResult;
import fit.wenchao.http_file_server.utils.ExceptionUtils;
import fit.wenchao.http_file_server.utils.ResponseEntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fit.wenchao.http_file_server.utils.FileUtilsKt.isFile;


@Validated
@RestController
@RequestMapping("/API")
public class ConfigController {

    @Autowired
    ConfigFile configFile;


    @GetMapping("/config/list")
    public ResponseEntity<JsonResult> getConfigList() {

        Map<String, String> configMap = null;
        try {
            configMap = configFile.listConfigurations();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof java.nio.file.NoSuchFileException) {
                throw new BackendException(CommonConsts.CONFIG_FILE_NAME,
                        RespCode.CONFIG_FILE_NOT_FOUND);
            }
            throw e;
        }

        List<ConfigPO> configPOList = new ArrayList<>();
        for (String key : configMap.keySet()) {
            String value = configMap.get(key);
            ConfigPO conf = ConfigPO.builder()
                    .key(key)
                    .value(value)
                    .build();
            configPOList.add(conf);
        }
        return ResponseEntityUtils.ok(JsonResult.ok(configPOList));
    }

    @PutMapping("/config")
    public ResponseEntity<JsonResult> setConfigValue(@NotBlank String key,
                                                     @NotBlank String value) {

        if ("max-upload-size".equals(key)) {
            long valueLong = 0;
            try {
                valueLong = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new BackendException(value, RespCode.FRONT_END_PARAMS_ERROR);
            }

            if (valueLong > 2048 || valueLong <= 0) {
                throw new BackendException(value,
                        RespCode.MAX_UPLOAD_SIZE_TOO_LARGE);
            }
        }

        try {
            configFile.setProp(key, value);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof java.nio.file.NoSuchFileException) {
                throw new BackendException(CommonConsts.CONFIG_FILE_NAME,
                        RespCode.CONFIG_FILE_NOT_FOUND);
            }
            throw e;
        }
        return ResponseEntityUtils.ok(JsonResult.ok());
    }

    @GetMapping("/config")
    public ResponseEntity<JsonResult> getConfigValue(@NotBlank String key) {
        String prop = null;
        try {
            prop = configFile.getProp(key);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof java.nio.file.NoSuchFileException) {
                throw new BackendException(CommonConsts.CONFIG_FILE_NAME,
                        RespCode.CONFIG_FILE_NOT_FOUND);
            }
            throw e;
        }
        if (prop == null) {
            throw new BackendException(key, RespCode.NO_CONFIG_KEY);
        }
        return ResponseEntityUtils.ok(JsonResult.ok(prop));
    }


}
