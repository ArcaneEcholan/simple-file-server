package fit.wenchao.http_file_server.utils;

import com.sun.imageio.plugins.jpeg.JPEGStreamMetadataFormat;
import org.omg.SendingContext.RunTime;
import org.springframework.aop.scope.ScopedProxyUtils;

import java.io.FileNotFoundException;

public class ExceptionUtils {

    public static Throwable getRootCause(Throwable ex) {
        Throwable cause = ex.getCause();

        Throwable result = null;
        while (cause != null) {
            result = cause;
            cause = cause.getCause();
        }

        return result;
    }

    public static void main(String[] args) {
        RuntimeException runtimeException = new RuntimeException(new FileNotFoundException());
        System.out.println(getRootCause(runtimeException));

        runtimeException = new RuntimeException();
        System.out.println(getRootCause(runtimeException));
    }
}
