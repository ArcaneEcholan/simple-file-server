package fit.wenchao.http_file_server.constants;

public enum RespCode {
    SUCCESS("10000", "success"),


    NO_FILE("10001", "file not found"),


    FRONT_END_PARAMS_ERROR("10002", "front end param error"),


    ///////////////////////// file upload ////////////////////////////
    UPLOAD_FILE_SIZE_EXCEED_UPPER_LIMIT("10003", "upload file size too large"),
    UPLOAD_FILE_UNKNOWN_ERROR("10004", "unknown error happened when uploading" +
            " file"),
    UPLOAD_FILE_MISSING("10005", "upload file missing"),

    DOWNLOAD_NOT_SUPPORT("", "file does not support download operation"),
    UPLOAD_DEST_ERROR("", "upload destination can only be dir"),
    FILE_ACCESS_DENIED("", "file access denied"), FILE_UPLOAD_ERROR("", "upload file error"),
    CONFIG_FILE_NOT_FOUND("", "config file not found");




    @Deprecated
    private String code;

    private String msg;

    public String getCode() {
        return name();
    }

    public String getMsg() {
        return msg;
    }

    RespCode(String code, String message) {
        this.code = code;
        this.msg = message;
    }


}
