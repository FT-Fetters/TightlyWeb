package com.heybcat.tightlyweb.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Fetters
 */
public class FileUtil {

    private FileUtil() {
    }

    public static List<File> match(String path, String exp) {
        File pf = new File(path);
        return match(pf, exp);
    }

    public static List<File> match(File pf, String exp) {
        if (!pf.exists() || !pf.isDirectory()) {
            return Collections.emptyList();
        }
        File[] subFiles = pf.listFiles();
        if (subFiles == null) {
            return Collections.emptyList();
        }
        List<File> r = new ArrayList<>();
        for (File subFile : subFiles) {
            if (Pattern.matches(exp, subFile.getName())) {
                r.add(subFile);
            }
        }
        return r;
    }

    public static boolean isDir(File file) {
        return file.exists() && file.isDirectory();
    }

    public static boolean isFile(File file) {
        return file.exists() && file.isFile();
    }

    /**
     * 获取运行jar目录
     */
    public static String getJarPath() {
        String path = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return path.substring(0, path.lastIndexOf("/")).replaceFirst("/", "");
    }

    /**
     * 读取文件
     */
    public static String readFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 保存文件
     */
    public static void saveFile(String filePath, String content) throws IOException {
        try(FileWriter fw = new FileWriter(filePath)) {
            fw.write(content);
        }
    }

    /**
     * 追加文件
     */

    public static void appendFile(String filePath, String content) throws IOException {
        try(FileWriter fw = new FileWriter(filePath, true)) {

            fw.write(content);
        }
    }


}
