package com.heybcat.tightlyweb.sql.support;

import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.common.util.ReflectionUtil;
import com.heybcat.tightlyweb.common.util.StrUtil;
import com.heybcat.tightlyweb.sql.DataMapping;
import com.heybcat.tightlyweb.sql.MapperMethodRef;
import com.heybcat.tightlyweb.sql.annotation.LiteMapper;
import com.heybcat.tightlyweb.sql.annotation.Table;
import com.heybcat.tightlyweb.sql.annotation.TableId;
import com.heybcat.tightlyweb.sql.entity.Page;
import com.heybcat.tightlyweb.sql.entity.PageInfo;
import com.heybcat.tightlyweb.sql.exception.ExecuteSqlException;
import com.heybcat.tightlyweb.sql.exception.SqlConnectException;
import com.heybcat.tightlyweb.sql.support.proxy.LiteMapperMethodInterceptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxyFactory;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxySupport;
import xyz.ldqc.tightcall.util.PackageUtil;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class LiteMapping implements DataMapping {


    private static final Logger log = LoggerFactory.getLogger(LiteMapping.class);
    private final Connection connection;

    private final MapperManager mapperManager;

    private final TableManager tableManager;

    private final ThreadLocal<PageInfo> pageInfoThreadLocal = new ThreadLocal<>();

    private static final String DB_FILE_SUFFIX = ".db";

    private final Class<?> bootClass;

    private final boolean dbLog;

    private LiteMapping(String db, String basePackage, Boolean dbLog, IocManager iocManager) {
        try {
            // load sqlite driver
            Class.forName("org.sqlite.JDBC");
            // get connection
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + checkDatabasePath(db));
            basePackage = StringUtil.isBlank(basePackage) ? "" : basePackage;
            this.bootClass = iocManager.getBootClass();
            this.mapperManager = new MapperManager(basePackage, iocManager);
            this.tableManager = new TableManager(this, basePackage);
            this.dbLog = Objects.requireNonNullElse(dbLog, false);
        } catch (SQLException e) {
            throw new SqlConnectException("connect sqlite database " + db + " fail", e);
        } catch (ClassNotFoundException e) {
            throw new SqlConnectException("driver load fail", e);
        }
    }

    public static LiteMapping getMapping(String db, String basePackage, Boolean dbLog, IocManager iocManager) {
        return new LiteMapping(db, basePackage, dbLog, iocManager);
    }

    public <T> List<T> select(Class<T> clazz, String sql, Object... params) {
        return execute(clazz, sql, true, params);
    }

    private String checkDatabasePath(String db) {
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

    @SuppressWarnings("unchecked")
    private <T> List<T> execute(Class<T> clazz, String sql, boolean isQuery, Object... params) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql,
            Statement.RETURN_GENERATED_KEYS)) {
            ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
            // count params
            int parameterCount = parameterMetaData.getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            if (dbLog) {
                log.info("execute sql: {}", sql);
                if (parameterCount > 0) {
                    log.info("execute params: {}", Arrays.toString(params));
                }
            }
            if (isQuery) {
                // if is query operation, read result set and write to target class object list
                ResultSet resultSet = preparedStatement.executeQuery();
                List<T> resultList = new ArrayList<>();
                while (resultSet.next()) {
                    resultSet2ResultTarget(clazz, resultList, resultSet);
                }
                return resultList;
            } else {
                // insert/delete/update
                // Execute update
                int affectedRows = preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                List<T> generatedKeysList = new ArrayList<>();
                while (generatedKeys.next()) {
                    // Assuming ID is a long
                    generatedKeysList.add((T) Long.valueOf(generatedKeys.getLong(1)));
                }
                return generatedKeysList;
            }


        } catch (SQLException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new ExecuteSqlException("execute sql fail", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void resultSet2ResultTarget(Class<T> clazz, List<T> resultList, ResultSet resultSet)
        throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (clazz.equals(String.class)) {
            // if type is String, return string result
            resultList.add((T) resultSet.getString(1));
        }else if (clazz.equals(Long.class)){
            resultList.add((T) Long.valueOf(resultSet.getLong(1)));
        }else {
            T entity = clazz.getConstructor().newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();
            // iterate through every field and set query result value
            for (Field declaredField : declaredFields) {
                Method setterMethod = ReflectionUtil.setterMethod(clazz, declaredField);
                setterMethod.invoke(entity,
                    resultSet.getObject(StrUtil.camelCase2Underline(declaredField.getName())));
            }
            resultList.add(entity);
        }
    }


    @Override
    public boolean save(Object obj) {
        if (obj == null) {
            return false;
        }

        String tableName;

        // 1. get table name from table annotation
        // 2. get table name from class name
        if (obj.getClass().isAnnotationPresent(Table.class)) {
            tableName = obj.getClass().getAnnotation(Table.class).name();
        } else {
            tableName = obj.getClass().getSimpleName();
            // camel case to underline split
            tableName = StrUtil.camelCase2Underline(tableName);
        }
        if (StringUtil.isBlank(tableName)) {
            return false;
        }

        Field[] declaredFields = obj.getClass().getDeclaredFields();
        Map<String, String> fieldMap = new HashMap<>(declaredFields.length);
        for (Field declaredField : declaredFields) {
            try {
                Method method = ReflectionUtil.getterMethod(obj.getClass(),
                    declaredField.getName());
                Object getterRet = method.invoke(obj);
                if (getterRet == null) {
                    continue;
                }
                fieldMap.put(StrUtil.camelCase2Underline(declaredField.getName()),
                    getterRet.toString());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(
                    "No getter method found for field: " + declaredField.getName());
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new ExecuteSqlException("save fail", e);
            }
        }
        String sql = concatenateInsert(fieldMap, tableName);
        List<Long> result = execute(null, sql, false, fieldMap.values().toArray());
        // assign the inserted id
        String tableIdField = getTableIdField(obj);
        if (StringUtil.isNotBlank(tableIdField)) {
            try {
                Field declaredIdField = obj.getClass().getDeclaredField(tableIdField);
                Method setterMethod = ReflectionUtil.setterMethod(obj.getClass(), declaredIdField);
                if (declaredIdField.getType().equals(Integer.class)) {
                    Integer integerId = (result.get(0)).intValue();
                    setterMethod.invoke(obj, integerId);
                } else {
                    setterMethod.invoke(obj, result.get(0));
                }

            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(
                    "No setter method found for field: " + tableIdField);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new ExecuteSqlException("save fail", e);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                    "No field found for field: " + tableIdField);
            }
        }
        return !result.isEmpty();
    }

    @Override
    public <T> T getById(Class<T> clazz, Object id) {
        if (clazz == null || id == null || !clazz.isAnnotationPresent(Table.class) ||
            StringUtil.isBlank(clazz.getAnnotation(Table.class).name())) {
            return null;
        }
        String tableName = clazz.getAnnotation(Table.class).name();
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            sqlBuilder.append(StrUtil.camelCase2Underline(declaredField.getName())).append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" FROM ").append(tableName).append(" WHERE ")
            .append(getTableIdField(clazz)).append(" = ?");
        List<T> result = execute(clazz, sqlBuilder.toString(), true, id);
        if (result.isEmpty()) {
            return null;
        }
        if (result.size() > 1) {
            throw new ExecuteSqlException("more than one result");
        }
        return result.get(0);
    }

    @Override
    public <T> Page<T> selectPage(int size, int current, MapperMethodRef<T> ref) {
        pageInfoThreadLocal.set(new PageInfo(current, size, 0));
        List<T> pageRecords = ref.query();
        int total = pageInfoThreadLocal.get().getTotal();
        pageInfoThreadLocal.remove();
        return new Page<>(size, current, pageRecords, total);
    }

    @Override
    public PageInfo getPageInfo() {
        return pageInfoThreadLocal.get();
    }

    @Override
    public boolean delete(Object obj) {
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        String id = null;
        Object idValue = null;
        for (Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(TableId.class)) {
                continue;
            }
            id = declaredField.getName();
            idValue = ReflectionUtil.getFieldValue(obj, id);
        }
        Table table = obj.getClass().getAnnotation(Table.class);
        return doDelete(idValue, id, table);
    }

    @Override
    public boolean deleteById(Class<?> clazz, Object id) {
        Field[] declaredFields = clazz.getDeclaredFields();
        String idField = null;
        for (Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(TableId.class)) {
                continue;
            }
            idField = declaredField.getName();
        }
        Table table = clazz.getAnnotation(Table.class);
        return doDelete(id, idField, table);
    }

    private boolean doDelete(Object idValue, String idField, Table table) {
        if (StringUtil.isBlank(idField) || table == null || idValue == null) {
            return false;
        }
        String tableName = table.name();
        String sqlBuilder = "DELETE FROM " + tableName
            + " WHERE " + StrUtil.camelCase2Underline(idField) + " = ?";
        return !execute(Integer.class, sqlBuilder, false, idValue).isEmpty();
    }

    private String concatenateInsert(Map<String, String> fieldMap, String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ").append(tableName).append("(");
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(") VALUES (");
        stringBuilder.append("?,".repeat(fieldMap.size()));
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private String getTableIdField(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(TableId.class)) {
                return declaredField.getName();
            }
        }
        return "id";
    }

    private String getTableIdField(Object o) {
        return getTableIdField(o.getClass());
    }


    private class MapperManager {

        private final String mapper;

        private final IocManager iocManager;

        public MapperManager(String mapper, IocManager iocManager) {
            this.mapper = mapper;
            this.iocManager = iocManager;
            loadAndRegisterMapper();
        }

        private void loadAndRegisterMapper() {
            List<Class<?>> packageClasses = PackageUtil.getPackageClasses(mapper, bootClass);
            final ProxyFactory proxyFactory = new ProxyFactory();
            packageClasses.forEach(clazz -> {
                if (!clazz.isAnnotationPresent(LiteMapper.class)) {
                    return;
                }
                if (!clazz.isInterface()) {
                    log.error("{} class is not interface, mapper class must be interface", clazz.getName());
                    return;
                }
                ProxySupport proxySupport = new ProxySupport();
                proxySupport.setTargetClass(clazz);
                proxySupport.setInterceptor(new LiteMapperMethodInterceptor(LiteMapping.this));

                Object proxyObj = proxyFactory.getProxy(proxySupport);
                iocManager.register(clazz.getName(), proxyObj);
            });

        }

    }

    private static class TableManager {

        private final LiteMapping liteMapping;

        private final String basePackage;

        public TableManager(LiteMapping liteMapping, String basePackage) {
            this.liteMapping = liteMapping;
            this.basePackage = basePackage;
            checkTableExistOrCreate();
            checkTableChangeAndUpdate();
        }

        private void checkTableExistOrCreate() {
            List<Class<?>> tableClassList = listAllTableClass();
            for (Class<?> tableClass : tableClassList) {
                Table table = tableClass.getAnnotation(Table.class);
                if (!tableExist(table.name())) {
                    String createTableSql = generateCreateTableSql(tableClass);
                    liteMapping.execute(String.class, createTableSql, false);
                    log.info("table {} not exist, created table", table.name());
                }
            }
        }

        private void checkTableChangeAndUpdate() {
            List<Class<?>> tableClassList = listAllTableClass();
            for (Class<?> tableClass : tableClassList) {
                Table table = tableClass.getAnnotation(Table.class);
                String findTableStructureSql =
                    "SELECT sql FROM sqlite_master WHERE type='table' AND name=" + "'" + table.name().replace("`","") + "'";
                List<String> result = liteMapping.execute(String.class, findTableStructureSql, true);
                if (result.isEmpty()) {
                    continue;
                }
                String createSql = result.get(0);
                Map<String, String> createSqlFieldMap = parseCreateSql2FieldMap(createSql);
                Map<String, Class<?>> tableClassFiledMap = parseTableClass2FieldMap(tableClass);
                Map<String, Class<?>> addFieldMap = new HashMap<>(8);
                Map<String, String> deleteFieldMap = new HashMap<>(8);
                tableClassFiledMap.forEach((fieldName, fieldType) -> {
                    if (!createSqlFieldMap.containsKey(fieldName)) {
                        addFieldMap.put(fieldName, fieldType);
                    }
                });
                createSqlFieldMap.forEach((fieldName, fieldType) -> {
                    if (!tableClassFiledMap.containsKey(fieldName)) {
                        deleteFieldMap.put(fieldName, fieldType);
                    }
                });
                List<String> sqlList = generateCheckUpdateSql(addFieldMap, deleteFieldMap, table.name());
                if (!sqlList.isEmpty()) {
                    log.info("table {} is change, del field {}, add field: {}", table.name(), deleteFieldMap, addFieldMap);
                    sqlList.forEach(sql -> liteMapping.execute(String.class, sql, false));
                }
            }
        }

        private Map<String, String> parseCreateSql2FieldMap(String createSql) {
            String filedDefinition = createSql.substring(createSql.indexOf("(") + 1, createSql.lastIndexOf(")"));
            return Arrays.stream(filedDefinition.split(","))
                .map(String::trim)
                .map(s -> s.split(" "))
                .collect(Collectors.toMap(s -> s[0].replace("`", ""), s -> s[1]));
        }

        private Map<String, Class<?>> parseTableClass2FieldMap(Class<?> clazz) {
            return Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(f -> StrUtil.camelCase2Underline(f.getName()), Field::getType));
        }

        private List<String> generateCheckUpdateSql(Map<String, Class<?>> addFieldMap,
            Map<String, String> deleteFieldMap, String tableName) {
            List<String> sqlList = new ArrayList<>();
            addFieldMap.forEach((fieldName, fieldType) -> {
                fieldName = StrUtil.camelCase2Underline(fieldName.replace("`", ""));
                String sql =
                    "ALTER TABLE " + tableName + " ADD COLUMN `" + fieldName  + "` " + javaType2SqlType(fieldType);
                sqlList.add(sql);
            });
            deleteFieldMap.forEach((fieldName, fieldType) -> {
                fieldName = fieldName.replace("`", "");

                String sql = "ALTER TABLE " + tableName + " DROP COLUMN `" + fieldName + "`";
                sqlList.add(sql);
            });
            return sqlList;
        }

        private List<Class<?>> listAllTableClass() {
            List<Class<?>> packageClasses = PackageUtil.getPackageClasses(basePackage, this.liteMapping.bootClass);
            return packageClasses.stream().filter(clazz -> clazz.isAnnotationPresent(Table.class))
                .collect(Collectors.toList());
        }

        private boolean tableExist(String tableName) {
            List<String> result = liteMapping.execute(String.class,
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?", true, tableName.replace("`", ""));
            if (result.isEmpty()) {
                return false;
            } else {
                return result.get(0).equals(tableName.replace("`", ""));
            }
        }

        private String generateCreateTableSql(Class<?> clazz) {
            StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            Table table = clazz.getAnnotation(Table.class);
            if (table == null) {
                return null;
            }
            stringBuilder.append(table.name()).append(" (");
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(TableId.class)) {
                    stringBuilder.append(StrUtil.camelCase2Underline(declaredField.getName()))
                        .append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
                } else {
                    Class<?> type = declaredField.getType();
                    stringBuilder.append(StrUtil.camelCase2Underline(declaredField.getName())).append(" ")
                        .append(javaType2SqlType(type)).append(",");
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append(")");
            return stringBuilder.toString();
        }

        private String javaType2SqlType(Class<?> type) {
            if (type == String.class) {
                return "TEXT";
            } else if (type == Integer.class || type == int.class) {
                return "INTEGER";
            } else if (type == Long.class || type == long.class) {
                return "BIGINT";
            } else {
                return null;
            }
        }
    }

}
