package fit.wenchao.http_file_server.utils;

import java.io.File;

public class FilePathBuilder {
    StringBuilder sb;

    public FilePathBuilder() {
        this.sb = new StringBuilder();
    }

    public static FilePathBuilder ofPath() {
        return new FilePathBuilder();
    }

    public FilePathBuilder(String initPathStr) {
        String systemSpecificPath = convertSeparatorOfPath(initPathStr);
        this.sb = new StringBuilder(systemSpecificPath);
    }

    public FilePathBuilder ct(String rowPath) {

        String systemSpecificPath = convertSeparatorOfPath(rowPath);

        systemSpecificPath = trimHeadSeparator(systemSpecificPath);
        systemSpecificPath = trimTailSeparator(systemSpecificPath);

        if ("".equals(sb.toString())) {
            sb.append(File.separator);
            sb.append(systemSpecificPath);
            return this;
        }

        if(!sb.substring(sb.length() - 1, sb.length()).equals(File.separator)) {
            sb.append(File.separator);
            sb.append(systemSpecificPath);
            return this;
        }

        sb.append(systemSpecificPath);
        return this;
    }

    private static String trimTailSeparator(String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith(File.separator)) {
            while (path.endsWith(File.separator)) {
                path = path.substring(0, path.length() - 1);
            }
            return path;
        }
        return path;
    }

    private static String trimHeadSeparator(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith(File.separator)) {
            while (path.startsWith(File.separator)) {
                path = path.substring(1);
            }
            return path;
        }
        return path;
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
        trimTailSeparator(path);
        return path;
    }
}