package com.heybcat.tightlyweb.http.core;

import com.heybcat.tightlyweb.common.resource.DefaultResourceLoader;
import com.heybcat.tightlyweb.common.resource.Resource;
import com.heybcat.tightlyweb.http.entity.ResourceDispatchResult;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import xyz.ldqc.tightcall.buffer.SimpleByteData;
import xyz.ldqc.tightcall.protocol.http.ContentTypeEnum;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;

/**
 * @author Fetters
 */
public class ResourceDispatchHandler {

    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    public ResourceDispatchResult dispatch(HttpNioRequest request) throws IOException {
        URI uri = request.getUri();
        String path = uri.getPath();

        Resource resource = resourceLoader.load(path);
        InputStream inputStream = resource.getInputStream();
        if (inputStream == null) {
            return new ResourceDispatchResult(new byte[0], ContentTypeEnum.TEXT_PLAIN.getValue());
        }
        int len = inputStream.available();
        byte[] buffer = new byte[8196];
        int read;
        SimpleByteData byteData = new SimpleByteData(len);
        while ((read = inputStream.read(buffer)) != -1) {
            byteData.writeBytes(buffer, 0, read);
        }
        inputStream.close();
        String[] split = path.split("\\.");
        ContentTypeEnum contentTypeByExtension = ContentTypeEnum.getContentTypeByExtension(
            split[split.length - 1]);
        if (contentTypeByExtension == null){
            contentTypeByExtension = ContentTypeEnum.TEXT_PLAIN;
        }
        return new ResourceDispatchResult(byteData.readBytes(), contentTypeByExtension.getValue());
    }

}
