package fit.wenchao.http_file_server;

public class SystemProperty {
    private static final SystemProperty singleton
            = new SystemProperty();

    public static SystemProperty getSingleton() {
        return singleton;
    }

    private SystemProperty() {

    }

    public String getCurDir() {
        return System.getProperty("user.dir");
    }
    public String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public static void main(String[] args) {
        System.out.println(SystemProperty.getSingleton().getCurDir());
        System.out.println(SystemProperty.getSingleton().getTempDir());
    }
}
