package com.heybcat.tightlyweb.config;

import com.heybcat.tightlyweb.annoation.ConfigValue;
import lombok.Data;

/**
 * @author Fetters
 */
@Data
public class TightlyWebConfigEntity {

    @ConfigValue(key = "server.port")
    private Integer port;

}
