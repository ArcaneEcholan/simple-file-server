package fit.wenchao.http_file_server;

import fit.wenchao.db.generator.Generator;
import fit.wenchao.http_file_server.constants.CommonConsts;
import fit.wenchao.http_file_server.utils.FilePathBuilder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConfigFile {

    // path = java running path + config file name
    private static String configFilePath =
            FilePathBuilder.ofPath()
                           .ct(SystemProperty.getSingleton()
                                            .getCurDir())
                           .ct(CommonConsts.CONFIG_FILE_NAME)
                           .build();

    public boolean exists() {
        return Files.exists(Paths.get(configFilePath));
    }

    /**
     * Create config file if it's not exists.
     */
    public synchronized void create() {
        File configFile = new File(configFilePath);

        // create config file in the java running directory if the config
        // file is not exists
        if (!configFile.exists()) {
            try {
                Files.createFile(Paths.get(configFilePath));
            }
            catch (IOException e) {
                // create config file failed
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * List all configurations in the config file.
     *
     * @return A map containing all configs.Empty map if there is nothing in
     * the config file.
     */
    public synchronized Map<String, String> listConfigurations() {
        Map<String, String> map = new HashMap<>();
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(configFilePath))))
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                Pair<String, String> lineKeyAndValue =
                        getLineKeyAndValue(line);
                if (lineKeyAndValue != null) {
                    map.put(lineKeyAndValue.getKey(), lineKeyAndValue.getValue());
                }
            }
            return map;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class Pair<K, V> {
        K key;
        V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public Pair() {
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    private synchronized Pair<String, String> getLineKeyAndValue(String line) {
        Pair<String, String> pair = null;
        String pattern = "([-a-zA-Z]+)\\s+(.*)";
        String mat = line;
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(mat);
        int groupCount = matcher.groupCount();

        if (matcher.find()) {
            String prop = null;
            String value = null;
            for (int i = 0; i < groupCount; i++) {
                if (i == 0) {
                    prop = matcher.group(i + 1);
                }
                else if (i == 1) {
                    value = matcher.group(i + 1);
                }
            }
            if (prop != null && !prop.isEmpty() && value != null && !value.isEmpty()) {
                pair = new Pair<>();
                pair.setKey(prop);
                pair.setValue(value);
            }
        }
        return pair;
    }

    /**
     * Get the value of key from config file.
     *
     * @param key Key
     * @return Value of key if the key is correctly configured (empty string
     * or key not exists) in the config file, null otherwise.
     */
    public String getProp(String key) {
        Map<String, String> configMap = listConfigurations();
        //return null;
        String s = configMap.get(key);
        if (s == null) {
            s = "";
        }
        return configMap.get(key);
    }

    public synchronized void setProp(String key, String value) {
        String exists = getProp(key);

        // key not exists, append new line
        if (exists == null) {
            appendLineToFile(configFilePath, key.trim() + " " + value.trim());
        }

        // key exists, replace key value
        replaceKeyValue(key, value);

    }

    private void replaceKeyValue(String key, String value) {
        StringBuilder configFileLineBuffer = new StringBuilder();
        // read all lines into memory buffer and replace the target line with
        // new value
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(Files.newInputStream(Paths.get(configFilePath))))
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                Pair<String, String> lineKeyAndValue = getLineKeyAndValue(line);

                // useless line
                if (lineKeyAndValue == null) {
                    configFileLineBuffer.append(line)
                                        .append("\n");
                    continue;
                }

                String lineKey = lineKeyAndValue.getKey();

                // if current line is the target line, change the value and
                // put the line into buffer
                if (key.equals(lineKey)) {
                    line = key + " " + value;
                }
                configFileLineBuffer.append(line)
                                    .append("\n");
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        // write all buffered lines back to config file
        try (FileWriter writer = new FileWriter(configFilePath)) {
            writer.write(configFileLineBuffer.toString());
            writer.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendLineToFile(String fileName, String content) {
        try (RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");) {
            // 文件长度，字节数
            long fileLength = randomFile.length();
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        ConfigFile configFile = new ConfigFile();
        System.out.println(configFile.listConfigurations());

        //configFile.create();
        //
        //
        //System.out.println(configFile.getProp("root1"));
        //
        //configFile.setProp("PidFile", "/var/run/sshd");
        //System.out.println(configFile.getProp("PidFile"));
    }

}
