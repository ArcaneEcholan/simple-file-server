package fit.wenchao.http_file_server.utils;
import java.io.File;

public class StrUtils {

    public static void main(String[] args) {

    }

    public static String ft(String format, Object... args) {
        return new StringFormatter().formatString(format, args);
    }

    public static FilePathBuilder filePathBuilder(String initPathStr) {
        return new FilePathBuilder(initPathStr);
    }

    public static FilePathBuilder filePathBuilder() {
        return filePathBuilder("");
    }

    public static class FilePathBuilder {
        StringBuilder sb;

        public FilePathBuilder() {
            this.sb = new StringBuilder();
        }

        public FilePathBuilder(String initPathStr) {
            String systemSpecificPath = convertSeparatorOfPath(initPathStr);
            this.sb = new StringBuilder(systemSpecificPath);
        }

        public FilePathBuilder ct(String rowPath) {

            String systemSpecificPath = convertSeparatorOfPath(rowPath);


            if (systemSpecificPath.startsWith(File.separator)) {
                for (int i = 0; i < systemSpecificPath.length(); i++) {
                    char c = systemSpecificPath.charAt(i);
                    if (String.valueOf(c).equals(File.separator)) {
                        systemSpecificPath = systemSpecificPath.substring(1);
                        continue;
                    }
                    break;
                }
            }

            if (sb.toString().endsWith(File.separator)) {
                for (int i = sb.toString().length() - 1; i >= 0; i--) {
                    char c = sb.toString().charAt(i);
                    if (String.valueOf(c).equals(File.separator)) {
                        sb = new StringBuilder(sb.substring(0, i));
                        continue;
                    }
                    break;
                }
            }

            sb.append(File.separator);

            sb.append(systemSpecificPath);
            return this;
        }

        private String convertSeparatorOfPath(String path) {
            String systemString = System.getProperty("os.name").toLowerCase();
            if (systemString.contains("windows")) {
                path = path.replace("/", File.separator);
            } else if (systemString.contains("linux") || systemString.contains("mac")) {
                path = path.replace("\\", File.separator);
            }
            return path;
        }

        public String build() {
            String path = sb.toString();
            if (path.endsWith(File.separator)) {
                while (path.endsWith(File.separator)) {
                    path = path.substring(0, path.length() - 1);
                }
            }
            return path;
        }
    }


}
