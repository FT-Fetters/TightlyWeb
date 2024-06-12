package com.heybcat.tightlyweb.common.resource;

import java.io.InputStream;
import lombok.Getter;

/**
 * @author Fetters
 */
@Getter
public class Resource {

    private final String uri;

    private final InputStream inputStream;

    public Resource(String uri, InputStream inputStream) {
        this.uri = uri;
        this.inputStream = inputStream;
    }

}
