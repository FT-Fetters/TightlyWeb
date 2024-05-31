package com.heybcat.tightlyweb.common.resource;

import java.io.InputStream;

/**
 * @author Fetters
 */
public class Resource {

    private final String uri;

    private final InputStream inputStream;

    public Resource(String uri, InputStream inputStream) {
        this.uri = uri;
        this.inputStream = inputStream;
    }

    public String getUri() {
        return uri;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
