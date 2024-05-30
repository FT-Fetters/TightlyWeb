package com.heybcat.tightlyweb.common.util;

import com.heybcat.tightlyweb.common.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */

public class YamlUtil {

    private YamlUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger log = LoggerFactory.getLogger(YamlUtil.class);
    private static final int EACH_TAB_LEN = 2;
    private static final String COMMENT_FLAG = "#";
    private static final String ENV_VALUE_PREFIX = "${";
    private static final String ENV_VALUE_SUFFIX = "}";
    private static final String KEV_VALUE_SPLIT = ":";

    public static Map<String, String> loadConfigMap(Resource resource) {
        String resourceContent = getResourceContent(resource);
        Deque<String> propertyStack = new ArrayDeque<>();
        Map<String, String> configMap = new HashMap<>(8);
        String[] lines = resourceContent.split("\r\n");
        for (String line : lines) {
            // handle comment first
            line = handleComment(line);

            if (StringUtil.isBlank(line.trim())) {
                continue;
            }

            if (line.trim().endsWith(":")) {
                if (!handleNotValueNode(line, propertyStack)) {
                    return new HashMap<>(0);
                }
            } else if (line.trim().contains(":")) {
                handleWithValueNode(line, propertyStack, configMap);
            } else {
                log.error("yaml config load error, unknown format, line: {}", line);
                return new HashMap<>(0);
            }
        }
        return configMap;
    }

    private static String handleComment(String line) {
        if (line.contains(COMMENT_FLAG)) {
            int startIndex = line.indexOf(COMMENT_FLAG);
            return line.substring(0, startIndex);
        } else {
            return line;
        }
    }

    private static void handleWithValueNode(String line, Deque<String> propertyStack,
        Map<String, String> configMap) {
        // if line contains : and not end with :, it is a key with value
        String key = line.substring(0, line.indexOf(":")).trim();
        String value = line.substring(line.indexOf(":") + 1).trim();
        value = envValue(value);
        if (!propertyStack.isEmpty()) {
            // if property stack is not empty, combine all nodes
            StringBuilder sb = new StringBuilder();
            for (String s : propertyStack) {
                sb.append(s).append(".");
            }
            configMap.put(sb + key, value);
        } else {
            configMap.put(key, value);
        }
    }

    private static String envValue(String value) {
        if (value.startsWith(ENV_VALUE_PREFIX) && value.endsWith(ENV_VALUE_SUFFIX)) {
            // if is env value
            String content = value.substring(2, value.length() - 1);
            if (content.contains(KEV_VALUE_SPLIT)) {
                // judge whether it has key and value
                String k = content.split(KEV_VALUE_SPLIT)[0].trim();
                String v = content.split(KEV_VALUE_SPLIT)[1].trim();
                String entValue = System.getenv(k);
                return Objects.requireNonNullElse(entValue, v);
            } else {
                // only have env key
                return System.getenv(content);
            }
        }
        return value;
    }

    private static boolean handleNotValueNode(String line,
        Deque<String> propertyStack) {
        // if end with :, it is not a key with value
        int spaceCount = StrUtil.countLeadingSpaces(line);
        if (spaceCount % EACH_TAB_LEN != 0) {
            // if prefix space char is not multiples of 2, throw out error
            log.error("yaml config load error, bad format, line: {}", line);
            return false;
        }
        // use space count to calculate level
        int level = spaceCount / 2;
        if (level == 0) {
            // if level is 0, mean it is a new root, clear stack
            propertyStack.clear();
            propertyStack.push(line.trim().replace(":", ""));
        } else if (level == propertyStack.size()) {
            // if level is equal to stack size, it is a new child
            propertyStack.push(line.trim());
        } else if (level < propertyStack.size()) {
            // if level is less than stack size, it is a child of parent
            // pop stack until level is equal to stack size
            while (propertyStack.size() > level) {
                propertyStack.pop();
            }
            propertyStack.push(line.trim());
        } else {
            // bad format
            log.error("yaml config load error, line: {}", line);
            return false;
        }
        return true;
    }

    private static String getResourceContent(Resource resource) {
        InputStream inputStream = resource.getInputStream();
        byte[] buf = new byte[1024];
        try {
            StringBuilder sb = new StringBuilder();
            int read = inputStream.read(buf);
            // get resource input stream and read byte to string
            while (read != -1) {
                sb.append(new String(buf, 0, read));
                read = inputStream.read(buf);
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("load config fail", e);
            return "";
        }
    }


}
