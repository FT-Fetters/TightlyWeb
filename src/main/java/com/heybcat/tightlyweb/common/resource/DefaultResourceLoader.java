package com.heybcat.tightlyweb.common.resource;

import com.heybcat.tightlyweb.common.exception.ConfigLoaderException;
import com.heybcat.tightlyweb.common.util.StrUtil;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Loader
 *
 * @author Fetters
 */
public class DefaultResourceLoader implements ResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(DefaultResourceLoader.class);

    private final ClassLoader classLoader;

    public DefaultResourceLoader() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public Resource load(String path) {
        InputStream resourceStream = this.classLoader.getResourceAsStream(path);
        if (resourceStream == null) {
            throw new ConfigLoaderException(StrUtil.format("No such path - {0}", path));
        }
        log.info("load resource: \"{}\"", path);
        return new Resource(path, resourceStream);
    }
}
