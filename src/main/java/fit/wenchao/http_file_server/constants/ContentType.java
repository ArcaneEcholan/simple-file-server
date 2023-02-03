package fit.wenchao.http_file_server.constants;

public class ContentType {
    private ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private String value;
    public static final ContentType TEXT = new ContentType("text");
    public static final ContentType OBJECT = new ContentType("object");
    public static final ContentType FILE  = new ContentType("value");

    @Override
    public String toString() {
        return value;
    }
}
