package fit.wenchao.http_file_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class HttpFileServerApplication {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        SpringApplication.run(HttpFileServerApplication.class, args);
    }

}
