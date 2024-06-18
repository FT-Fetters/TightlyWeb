package com.heybcat.tightlyweb.http.entity;

import lombok.Getter;

/**
 * @author Fetters
 */
@Getter
public class ResourceDispatchResult {

    private byte[] data;

    private String contentType;

    public ResourceDispatchResult(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

}
