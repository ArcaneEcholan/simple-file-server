package fit.wenchao.http_file_server.rest;

import fit.wenchao.http_file_server.SystemProperty;
import fit.wenchao.http_file_server.constants.FileType;
import fit.wenchao.http_file_server.constants.RespCode;
import fit.wenchao.http_file_server.exception.BackendException;
import fit.wenchao.http_file_server.model.JsonResult;
import fit.wenchao.http_file_server.model.vo.FileInfo;
import fit.wenchao.http_file_server.model.vo.UploadFileInfo;
import fit.wenchao.http_file_server.utils.ResponseEntityUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fit.wenchao.http_file_server.utils.StrUtils.filePathBuilder;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;

@Validated
@RestController
public class FileController {

    @GetMapping("/file-list")
    public ResponseEntity<JsonResult> getFileList(@NotBlank String path) {

        path = filePathBuilder()
                .ct(SystemProperty.getSingleton()
                                  .getCurDir())
                .ct(path)
                .build();

        File file = new File(path);

        // path is not valid
        if (!file.exists()) {
            throw new BackendException(path, RespCode.NO_FILE);
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

    @GetMapping("/file")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            String path) throws UnsupportedEncodingException {

        path = filePathBuilder()
                .ct(SystemProperty.getSingleton()
                                  .getCurDir())
                .ct(path)
                .build();

        File file = new File(path);

        // path is not valid
        if (!file.exists()) {
            throw new BackendException(path, RespCode.NO_FILE);
        }

        if (file.isDirectory()) {
            throw new BackendException(path, RespCode.DOWNLOAD_NOT_SUPPORT);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition",
                "attachment; filename=" + new String(file.getName()
                                                         .getBytes("UTF-8"), "iso-8859-1"));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Type", "application/octet-stream");
        headers.add(CONTENT_LENGTH, String.valueOf(file.length()));

        String finalPath = path;
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(outputStream -> {
                    try (InputStream inputStream = Files.newInputStream(Paths.get(finalPath))) {
                        IOUtils.copyLarge(inputStream, outputStream);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    @PostMapping("/file")
    public ResponseEntity<JsonResult> uploadFile(
            @RequestPart("file-body") MultipartFile multipartFile,
            @RequestPart("file-info") @Validated UploadFileInfo uploadFileInfo
    ) throws Exception {

        String filename = multipartFile.getOriginalFilename();
        if (filename == null) {
            throw new BackendException(null, RespCode.FILE_UPLOAD_ERROR);
        }



        String path = uploadFileInfo.getPath();

        path = filePathBuilder()
                .ct(SystemProperty.getSingleton()
                                  .getCurDir())
                .ct(path)
                .build();

        File file = new File(path);

        if (file.isFile()) {
            throw new BackendException(path, RespCode.UPLOAD_DEST_ERROR);
        }

        Path uploadPath = Paths.get(path);


        // make upload dir
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            }
            catch (java.nio.file.AccessDeniedException accessDeniedException) {
                throw new BackendException(path, RespCode.FILE_ACCESS_DENIED);
            }
        }

        Path uploadFilePath = uploadPath.resolve(filename);
        // check if file exists already
        boolean exists = Files.exists(uploadFilePath);
        if (exists) {
            throw new FileAlreadyExistsException(uploadFilePath.toString());
        }

        try {
            // copy file from multipart
            Files.createFile(uploadFilePath);
            Files.copy(
                    multipartFile.getInputStream(),
                    uploadFilePath,
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (java.nio.file.AccessDeniedException accessDeniedException) {
            throw new BackendException(path, RespCode.FILE_ACCESS_DENIED);
        }


        return ResponseEntityUtils.ok(JsonResult.ok());

    }
}