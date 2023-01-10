package fit.wenchao.http_file_server.exception;

public class TOTPEmptyKeyException  extends  RuntimeException{
    public TOTPEmptyKeyException(String message) {
        super(message);
    }
}
