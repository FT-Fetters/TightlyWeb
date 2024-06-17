package com.heybcat.tightlyweb.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

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

    public static List<File> listAllFilesInDir(String dirPath) {
        return listAllFilesInDir(new File(dirPath));
    }

    private static List<File> listAllFilesInDir(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return Collections.emptyList();
            }
            List<File> r = new ArrayList<>();
            for (File f : files) {
                r.addAll(listAllFilesInDir(f));
            }
            return r;
        } else {
            return Collections.singletonList(file);
        }
    }

    public static List<String> listResourceFiles(Class<?> bootClass, String path) {
        URL url = bootClass.getClassLoader().getResource(path);
        if (url == null) {
            return Collections.emptyList();
        }

        if (url.getProtocol().equals("file")) {
            // 处理文件系统中的资源
            try {
                return Files.walk(Paths.get(url.toURI()))
                    .filter(Files::isRegularFile)
                    .map(p -> p.toFile().getAbsolutePath().substring(p.toFile().getAbsolutePath().indexOf(path)))
                    .collect(Collectors.toList());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        } else if (url.getProtocol().equals("jar")) {
            // 处理JAR文件中的资源
            String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath)) {
                return jar.stream()
                    .filter(e -> e.getName().startsWith(path) && !e.isDirectory())
                    .map(ZipEntry::getName)
                    .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }


}
