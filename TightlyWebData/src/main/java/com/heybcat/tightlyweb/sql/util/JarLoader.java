package com.heybcat.tightlyweb.sql.util;

import com.heybcat.tightlyweb.common.resource.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fetters
 */
public class JarLoader {

    private JarLoader(){
        throw new UnsupportedOperationException();
    }

    private static final Map<String, URLClassLoader> JAR_LOADER_MAP = new ConcurrentHashMap<>();


    public static URLClassLoader load(String jarPath){
        // TODO load by jar path、
        return null;
    }

    public static URLClassLoader load(Resource resource) throws IOException {
        InputStream inputStream = resource.getInputStream();
        if (inputStream == null){
            throw new IllegalArgumentException("resource is null");
        }
        File tmpJarFile = File.createTempFile("tightly-web-tmp-jar", ".jar");
        tmpJarFile.deleteOnExit();

        // copy resource file to tmpJarFile
        Files.copy(inputStream, tmpJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        URL jarUrl = tmpJarFile.toURI().toURL();

        // create urlClassLoader
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarUrl}, JarLoader.class.getClassLoader());
        JAR_LOADER_MAP.put(resource.getUri(), urlClassLoader);
        return urlClassLoader;
    }


}
