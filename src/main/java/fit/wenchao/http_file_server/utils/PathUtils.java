package fit.wenchao.http_file_server.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathUtils {
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

    public static List<String> splitPathStringToNodes(String path) {

        path = trimTailSeparator(path);

        path = trimHeadSeparator(path);

        if ("".equals(path)) {
            return Collections.emptyList();
        }

        if (!path.contains(File.separator)) {
            return Arrays.asList(new String[]{path});
        }


        String[] split = path.split(File.separator);
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            String node = split[i];
            result.add(node);
        }

        return result;
    }

    private static String trimSeparator(String path) {
        path = trimTailSeparator(path);
        path = trimHeadSeparator(path);
        return path;
    }

    public static String resolveFileName(String path) {
        path = trimSeparator(path);

        int lastIndexOfSplash = path.lastIndexOf("/");

        if (lastIndexOfSplash == -1) {
            return path;
        }
        return path.substring(lastIndexOfSplash + 1);
    }

    public static String relativeToDir(String dirPath, String filePath) {
        Path relativePath =
                Paths.get(dirPath).relativize(Paths.get(filePath));

        return relativePath.toString();
    }


    public static void main(String[] args) {
        System.out.println(relativeToDir("/", "/home"));
        List<String> strings = splitPathStringToNodes("/test/home/dir");
        System.out.println(strings);
        strings = splitPathStringToNodes("test/home/dir");
        System.out.println(strings);
        strings = splitPathStringToNodes("test/home/dir/");
        System.out.println(strings);
        strings = splitPathStringToNodes("/test/home/dir/");
        System.out.println(strings);

        strings = splitPathStringToNodes("test");
        System.out.println(strings);
        strings = splitPathStringToNodes("/test");
        System.out.println(strings);
        strings = splitPathStringToNodes("/test/");
        System.out.println(strings);
        strings = splitPathStringToNodes("test/");
        System.out.println(strings);

        String filename = resolveFileName("/home/cw");
        System.out.println(filename);
        filename = resolveFileName("home");
        System.out.println(filename);
        filename = resolveFileName("home/cw");
        System.out.println(filename);

        System.out.println(getParentPath("/"));
        System.out.println(getParentPath("/ou"));
        System.out.println(getParentPath("/ou/test"));
        System.out.println(getParentPath("/ou/test/path"));
    }


    /**
     * /的父路径是""，/ou的父路径是/,/ou/test的父亲路径是/ou
     */
    public static String getParentPath(String path) {
        // /特殊处理
        if (path.equals("/")) {
            return "";
        }

        int i = path.lastIndexOf("/");

        String parentString = path.substring(0, i);

        // 处理/ou的情况
        if(parentString.length() == 0) {
            return "/";
        }

        return parentString;
    }
}
