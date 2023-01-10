package fit.wenchao.http_file_server.model;

import fit.wenchao.http_file_server.constants.RespCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonResult {

    private Object data;

    private String code;
    private String msg;

    public static JsonResult of(Object data, RespCode respCode) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.data = data;
        jsonResult.code = respCode.getCode();
        jsonResult.msg = respCode.getMsg();
        return jsonResult;
    }

    public static JsonResult of(Object data, String code, String msg) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.data = data;
        jsonResult.code = code;
        jsonResult.msg = msg;
        return jsonResult;
    }

    public static JsonResult ok() {
        return new JsonResult(null, RespCode.SUCCESS.getCode(), RespCode.SUCCESS.getMsg());
    }

    public static JsonResult ok(Object data) {
        return new JsonResult(data, RespCode.SUCCESS.getCode(), RespCode.SUCCESS.getMsg());
    }
}
