package fit.wenchao.http_file_server.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;

public
class ResponseEntityUtils {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/Users/cw/Downloads/Docker (1).dmg");
        InputStream inputStream = Files.newInputStream(path);
        System.out.println(inputStream.available());
    }

    public static ResponseEntity<StreamingResponseBody> getFileEntity(String fileName, String fileSize, String absoluteFilePath) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Type", "application/octet-stream");
        headers.add(CONTENT_LENGTH, fileSize);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(outputStream -> {
                    Path path = Paths.get(absoluteFilePath);
                    try (InputStream inputStream = Files.newInputStream(path)) {
                        IOUtils.copy(inputStream, outputStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

    }


    public static ResponseEntity<StreamingResponseBody> getFileEntity(String fileName, String fileSize, StreamingResponseBody streamingResponseBody) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"),"iso-8859-1"));

        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Type", "application/octet-stream");
        headers.add(CONTENT_LENGTH, fileSize);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(streamingResponseBody);

    }
    public static ResponseEntity<StreamingResponseBody> getFileEntity(String fileName, String fileSize, InputStream inputStream) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"),"iso-8859-1"));

        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Type", "application/octet-stream");
        headers.add(CONTENT_LENGTH, fileSize);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(outputStream -> {
                    IOUtils.copy(inputStream, outputStream);
                });

    }

    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}