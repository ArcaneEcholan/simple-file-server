package fit.wenchao.http_file_server.rest;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import fit.wenchao.http_file_server.constants.FileType;
import fit.wenchao.http_file_server.constants.RespCode;
import fit.wenchao.http_file_server.exception.BackendException;
import fit.wenchao.http_file_server.model.JsonResult;
import fit.wenchao.http_file_server.model.vo.FileInfo;
import fit.wenchao.http_file_server.utils.FilePathBuilder;
import fit.wenchao.http_file_server.utils.FileUtils;
import fit.wenchao.http_file_server.utils.ResponseEntityUtils;
import fit.wenchao.http_file_server.wss.DirInfo;
import fit.wenchao.http_file_server.wss.DiskAnalyzingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/disk")
public class DiskAnalyzingController {

    @Autowired
    DiskAnalyzingContext diskAnalyzingContext;

    @GetMapping("scan")
    public ResponseEntity<JsonResult> scan(@NotBlank @Validated String path) throws IOException {
        if (!diskAnalyzingContext.scanFileWithPrefix(path)) {
            throw new BackendException(null, RespCode.DIR_ANALYZE_NOT_SUPPORT);
        }

        if (!diskAnalyzingContext.analyzing()) {
            throw new BackendException(null, RespCode.DISK_SCAN_ERROR);
        }

        DirInfo result = diskAnalyzingContext.analyze(path);
        diskAnalyzingContext.putOne(path, result);

        return ResponseEntityUtils.ok(JsonResult.ok());

    }

    @GetMapping("dirinfo")
    public ResponseEntity<JsonResult> getDirAnalyzeResult(@NotBlank @Validated String path) throws IOException {
        if (!diskAnalyzingContext.analyzing()) {
            throw new BackendException(null, RespCode.DISK_SCAN_ERROR);
        }

        if (diskAnalyzingContext.scanning()) {
            throw new BackendException(null, RespCode.DISK_SCAN_ERROR);
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new BackendException(null, RespCode.NO_FILE);
        }

        if (file.isFile()) {
            throw new BackendException(null, RespCode.FILE_TYPE_ERROR);
        }

        if (!diskAnalyzingContext.scanFileWithPrefix(path)) {
            throw new BackendException(null, RespCode.DIR_ANALYZE_NOT_SUPPORT);
        }

        File[] files = file.listFiles();
        if (files == null) {
            return ResponseEntityUtils.ok(JsonResult.ok(new ArrayList<>()));
        }

        List<FileInfo> dirInfos = new ArrayList<>();

        DirInfo curDirInfo = diskAnalyzingContext.getResult(path);
        if (curDirInfo != null) {
            curDirInfo.setName(file.getName());
            curDirInfo.setPath(file.getAbsolutePath());
        }

        for (File item : files) {

            if (FileUtils.isFile(item)) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setType(FileType.FILE.getCode())
                        .setName(item.getName())
                        .setLength(item.length())
                        .setPath(item.getAbsolutePath());
                dirInfos.add(fileInfo);
            }
            else {
                DirInfo itemInfo = diskAnalyzingContext.getResult(item.getAbsolutePath());

                if (itemInfo == null) {
                    itemInfo = new DirInfo();
                }

                itemInfo.setName(item.getName())
                        .setPath(item.getAbsolutePath())
                        .setType(FileType.DIR.getCode())
                ;
                dirInfos.add(itemInfo);
            }

        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("files", dirInfos);
        jsonObject.put("curDirInfo", curDirInfo);
        return ResponseEntityUtils.ok(JsonResult.ok(jsonObject));
    }

    @GetMapping("/filelist")
    public ResponseEntity<JsonResult> getFileList(@NotBlank String path) {

        File file = new File(path);

        // path is not valid
        if (!file.exists()) {
            throw new BackendException(path, RespCode.NO_FILE);
        }


        if (file.isFile()) {
            throw new BackendException(path, RespCode.FILE_TYPE_ERROR);
        }


        File[] files = file.listFiles();


        List<FileInfo> fileInfoList = new ArrayList<>();


        for (File everyFile : files) {
            String name = everyFile.getName();
            String absolutePath = everyFile.getAbsolutePath();
            long length = everyFile.length();
            boolean directory = everyFile.isDirectory();

            FileInfo fileInfo = FileInfo.builder()
                                        .name(name)
                                        .path(absolutePath)
                                        .length(length)
                                        .type(directory ?
                                                FileType.DIR.getCode() :
                                                FileType.FILE.getCode())
                                        .build();

            fileInfoList.add(fileInfo);
        }

        return ResponseEntityUtils.ok(JsonResult.ok(fileInfoList));
    }

}
