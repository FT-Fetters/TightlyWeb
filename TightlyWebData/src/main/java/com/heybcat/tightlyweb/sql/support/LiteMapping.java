package com.heybcat.tightlyweb.sql.support;

import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.sql.exception.SqlConnectException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fetters
 */
@Slf4j
public class LiteMapping extends BaseMapping {

    private static final String DB_FILE_SUFFIX = ".db";

    private LiteMapping(String db, String basePackage, Boolean dbLog, IocManager iocManager) {
        super(DataDriver.SQLITE.getDriverClass(), DataDriver.SQLITE.getType(), db, null, null, basePackage, dbLog,
            iocManager);
    }

    public static LiteMapping getMapping(String db, String basePackage, Boolean dbLog, IocManager iocManager) {
        return new LiteMapping(checkDatabasePath(db), basePackage, dbLog, iocManager);
    }


    @Override
    public boolean tableExist(String tableName) {
        List<String> result = execute(String.class,
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?", true, tableName.replace("`", ""));
        if (result.isEmpty()) {
            return false;
        } else {
            return result.get(0).equals(tableName.replace("`", ""));
        }
    }

    @Override
    public String getTableDesc(String tableName) {
        String findTableStructureSql =
            "SELECT sql FROM sqlite_master WHERE type='table' AND name=" + "'" + tableName.replace("`", "") + "'";
        List<String> result = execute(String.class, findTableStructureSql, true);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    private static String checkDatabasePath(String db) {
        if (!db.endsWith(DB_FILE_SUFFIX)) {
            db = db + ".db";
        }
        if (!db.contains("/") && !db.contains("\\")) {
            Path path = Paths.get(new File("").getAbsolutePath());
            db = path.resolve("db").resolve(db).toString();
            if (!new File(db).exists()) {
                log.info("DB file not exist, create file: {}", db);
            }
            try {
                Files.createDirectories(path.resolve("db"));
            } catch (IOException e) {
                throw new SqlConnectException("create db dir fail", e);
            }
        }
        return db;
    }
}
