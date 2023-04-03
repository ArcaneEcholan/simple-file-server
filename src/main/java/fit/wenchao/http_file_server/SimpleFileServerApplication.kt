package fit.wenchao.http_file_server;

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleFileServerApplication

fun main(args: Array<String>) {
    runApplication<SimpleFileServerApplication>(*args)
}
