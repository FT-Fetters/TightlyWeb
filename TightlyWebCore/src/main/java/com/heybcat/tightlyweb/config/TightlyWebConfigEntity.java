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

    @ConfigValue(key = "server.resource.enable")
    private Boolean enableResourceMapping;

    @ConfigValue(key = "server.resource.path")
    private String resourcePath;

    @ConfigValue(key = "server.web.redirect")
    private String redirect;

    @ConfigValue(key = "server.db.name")
    private String dbName;

    @ConfigValue(key = "server.db.basePackage")
    private String dbBasePackage;

    @ConfigValue(key = "server.db.log")
    private Boolean dbLog;

    @ConfigValue(key = "server.crossOrigin.enable")
    private Boolean crossOriginEnable;

    @ConfigValue(key = "server.crossOrigin.allowOrigin")
    private String crossOriginAllowOrigin;

}
