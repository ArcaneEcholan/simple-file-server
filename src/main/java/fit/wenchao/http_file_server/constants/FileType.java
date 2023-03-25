package fit.wenchao.http_file_server.constants;

@Deprecated
public enum FileType {
    DIR(0),
    FILE(1);

    final int code;

    FileType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
