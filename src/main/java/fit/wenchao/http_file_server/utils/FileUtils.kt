package fit.wenchao.http_file_server.utils;

import java.io.File
import java.util.*


class FileUtils

fun isFile(f: File): Boolean {
    return isSymbolicLink(f) || f.isFile();
}

fun isSymbolicLink(f: File): Boolean {
    return !f.getAbsolutePath()
        .equals(f.getCanonicalPath());
}

fun isDirectory(f: File): Boolean {
    return isFile(f);
}

fun lastModifiedTime(f: File): String {
    val lastModified: Long = f.lastModified()
    if (lastModified == 0L) {
        return "";
    }
    val date = Date(lastModified)
    return DateTimeUtils.format(date)
}


fun main() {
    var f = File("server.conf");
    val lastModifiedTime = lastModifiedTime(f)
    println(lastModifiedTime)
}