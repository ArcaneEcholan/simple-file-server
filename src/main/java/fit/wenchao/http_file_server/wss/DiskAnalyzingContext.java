package fit.wenchao.http_file_server.wss;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DiskAnalyzingContext {

    volatile boolean scanning;

    volatile boolean doingDiskAnalyzing;


    public synchronized void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    private Map<String, DirInfo> map = null;

    public synchronized DirInfo getResult(String path) {
        if (map == null) {
            return null;
        }
        else {
            return map.get(path);
        }
    }


    public synchronized void startAnalyzing() {
        if (this.doingDiskAnalyzing) {
            throw new RuntimeException("analyzing");
        }
        this.doingDiskAnalyzing = true;
        this.map = new HashMap<>();
    }

    public synchronized void endAnalyzing() {
        this.interfereScan();
        this.doingDiskAnalyzing = false;
        this.map = null;
    }

    public synchronized boolean analyzing() {
        return this.doingDiskAnalyzing;
    }

    public synchronized void startScanning() {
        this.interfere = false;
        this.scanning = true;
    }

    public synchronized void endScanning() {
        this.scanning = false;
    }

    public synchronized boolean scanning() {
        return this.scanning;
    }

    private boolean isFile(File f) throws IOException {
        return isSymbolicLink(f) || f.isFile();
    }

    private boolean isSymbolicLink(File f) throws IOException {
        return !f.getAbsolutePath()
                 .equals(f.getCanonicalPath());
    }

    private volatile boolean interfere = false;

    public void interfereScan() {
        this.interfere = true;
    }

    private boolean isDirectory(File f) throws IOException {
        return isFile(f);
    }

    public boolean scanFileWithPrefix(String path) {
        return !path.startsWith("/proc")
                && !path.startsWith("/dev")
                && !path.startsWith("/sys");
    }

    private DirInfo scan(File targetDir) throws IOException {
        if (interfere) {
            throw new RuntimeException("interfere scanning");
        }
        File[] files = null;
        ;
        if (!scanFileWithPrefix(targetDir.getAbsolutePath())
                || isFile(targetDir)
                || (files = targetDir.listFiles()) == null
        ) {
            return null;
        }


        DirInfo dirInfo = new DirInfo();
        long curDirSize = 0;
        long fileSize = 0;
        for (File item : files) {

            if (isFile(item)) {

                long length = item.length();

                curDirSize += length;
                fileSize++;

                continue;
            }

            DirInfo nestDirInfo = scan(item);
            if (nestDirInfo != null) {
                map.put(item.getAbsolutePath(), nestDirInfo);
                long nestDirSize = nestDirInfo.getLength();
                long nestFileSize = nestDirInfo.getNumberOfFiles();
                curDirSize += nestDirSize;
                fileSize += nestFileSize;
            }
        }

        dirInfo.setLength(curDirSize);
        dirInfo.setNumberOfFiles(fileSize);
        return dirInfo;
    }

    public synchronized DirInfo analyze(String dirPath) throws IOException {
        this.startScanning();
        File file = new File(dirPath);
        DirInfo scan = null;
        try {
            scan = scan(file);
        }
        catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        this.endScanning();
        return scan;
    }

    public synchronized void putOne(String path, DirInfo dirInfo) {
        this.map
                .put(path, dirInfo);
    }

    public static void main(String[] args) throws IOException {
        DiskAnalyzingContext diskAnalyzingContext = new DiskAnalyzingContext();

        if (!diskAnalyzingContext.analyzing()) {
            diskAnalyzingContext.startAnalyzing();

            DirInfo analyze = diskAnalyzingContext.analyze("/lib");

            System.out.println(analyze);
            System.out.println(diskAnalyzingContext.getResult("/lib/aspell"));
            diskAnalyzingContext.endAnalyzing();
            //diskAnalyzingContext.endAnalyzing();

            System.out.println(diskAnalyzingContext.getResult("/var/lib"));

        }

    }
}