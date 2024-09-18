package com.heybcat.tightlyweb.common.config;

import com.heybcat.tightlyweb.common.annoation.ConfigValue;
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

    @ConfigValue(key = "server.db.target")
    private String dbTarget;

    @ConfigValue(key = "server.db.basePackage")
    private String dbBasePackage;

    @ConfigValue(key = "server.db.type")
    private String dbType;

    @ConfigValue(key = "server.db.log")
    private Boolean dbLog;

    @ConfigValue(key = "server.db.user")
    private String dbUser;

    @ConfigValue(key = "server.db.password")
    private String dbPassword;

    @ConfigValue(key = "server.db.check")
    private Boolean dbCheck;

    @ConfigValue(key = "server.crossOrigin.enable")
    private Boolean crossOriginEnable;

    @ConfigValue(key = "server.crossOrigin.allowOrigin")
    private String crossOriginAllowOrigin;

}
