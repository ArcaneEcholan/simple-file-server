package fit.wenchao.http_file_server.exception;

import fit.wenchao.http_file_server.constants.RespCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BackendException extends RuntimeException{
    private Object data;
    private String code;
    private String msg;

    public BackendException (Object data, RespCode respCode) {
        this.data = data;
        this.code = respCode.getCode();
        this.msg = respCode.getMsg();
    }

    public BackendException (Throwable cause, Object data, RespCode respCode) {
        super(cause);
        this.data = data;
        this.code = respCode.getCode();
        this.msg = respCode.getMsg();
    }
}
