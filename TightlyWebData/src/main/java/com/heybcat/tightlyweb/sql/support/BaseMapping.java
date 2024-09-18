package com.heybcat.tightlyweb.sql.support;

import com.heybcat.tightlyweb.common.config.ConfigManager;
import com.heybcat.tightlyweb.common.ioc.IocManager;
import com.heybcat.tightlyweb.common.util.ReflectionUtil;
import com.heybcat.tightlyweb.common.util.StrUtil;
import com.heybcat.tightlyweb.sql.DataMapping;
import com.heybcat.tightlyweb.sql.MapperMethodRef;
import com.heybcat.tightlyweb.sql.annotation.DataMapper;
import com.heybcat.tightlyweb.sql.annotation.Table;
import com.heybcat.tightlyweb.sql.annotation.TableId;
import com.heybcat.tightlyweb.sql.entity.Page;
import com.heybcat.tightlyweb.sql.entity.PageInfo;
import com.heybcat.tightlyweb.sql.exception.ExecuteSqlException;
import com.heybcat.tightlyweb.sql.exception.SqlConnectException;
import com.heybcat.tightlyweb.sql.support.proxy.MapperMethodInterceptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public abstract class BaseMapping implements DataMapping {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Connection connection;

    protected Class<?> bootClass;

    protected MapperManager mapperManager;

    protected TableManager tableManager;

    protected Boolean dbLog;

    protected final ThreadLocal<PageInfo> pageInfoThreadLocal = new ThreadLocal<>();

    protected final String database;

    protected final DataDriver dataDriver;


    BaseMapping(String driverClass, String type, String target, String user, String password, String basePackage,
        Boolean dbLog, IocManager iocManager) {
        try {
            // load driver class
            Class.forName(driverClass);
            // get connection
            if (StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password)) {
                this.connection = DriverManager.getConnection("jdbc:" + type + ":" + target, user, password);
            } else {
                this.connection = DriverManager.getConnection("jdbc:" + type + ":" + target);
            }
            this.dataDriver = DataDriver.getDriver(type);
            basePackage = StringUtil.isBlank(basePackage) ? "" : basePackage;
            this.bootClass = iocManager.getBootClass();
            this.mapperManager = new MapperManager(basePackage, iocManager);
            this.tableManager = new TableManager(this, basePackage, iocManager);
            this.dbLog = Objects.requireNonNullElse(dbLog, false);
            this.database = getDataBaseByTarget(target);
        } catch (SQLException e) {
            throw new SqlConnectException("connect sqlite database " + target + " fail", e);
        } catch (ClassNotFoundException e) {
            throw new SqlConnectException("driver load fail", e);
        }
    }

    private String getDataBaseByTarget(String target) {
        String db = target;
        if (target.contains("/")) {
            db = target.substring(target.lastIndexOf("/") + 1);
        }
        if (target.contains("?")) {
            db = target.substring(0, target.indexOf("?"));
        }
        if (target.contains("\\.")) {
            db = db.substring(0, db.lastIndexOf("."));
        }
        return db;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> execute(Class<T> clazz, String sql, boolean isQuery, Object... params) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql,
            Statement.RETURN_GENERATED_KEYS)) {
            ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();

            // count params
            int parameterCount = parameterMetaData.getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            if (Boolean.TRUE.equals(dbLog)) {
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

    @Override
    public <T> List<T> select(Class<T> clazz, String sql, Object... params) {
        return execute(clazz, sql, true, params);
    }

    @SuppressWarnings("unchecked")
    private <T> void resultSet2ResultTarget(Class<T> clazz, List<T> resultList, ResultSet resultSet)
        throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (clazz.equals(String.class)) {
            // if type is String, return string result
            int columnCount = resultSet.getMetaData().getColumnCount();
            List<String> result = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                result.add(resultSet.getString(i + 1));
            }
            resultList.add((T) String.join(",", result));
        } else if (clazz.equals(Long.class)) {
            resultList.add((T) Long.valueOf(resultSet.getLong(1)));
        } else {
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
    public PageInfo getPageInfo() {
        return pageInfoThreadLocal.get();
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

    private String getTableIdField(Object o) {
        return getTableIdField(o.getClass());
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
                if (!clazz.isAnnotationPresent(DataMapper.class)) {
                    return;
                }
                if (!clazz.isInterface()) {
                    log.error("{} class is not interface, mapper class must be interface", clazz.getName());
                    return;
                }
                ProxySupport proxySupport = new ProxySupport();
                proxySupport.setTargetClass(clazz);
                proxySupport.setInterceptor(new MapperMethodInterceptor(BaseMapping.this));

                Object proxyObj = proxyFactory.getProxy(proxySupport);
                iocManager.register(clazz.getName(), proxyObj);
            });

        }

    }


    private class TableManager {

        private static final String CHECK_CONFIG = "server.db.check";

        private final DataMapping dataMapping;

        private final String basePackage;

        public TableManager(DataMapping dataMapping, String basePackage, IocManager iocManager) {
            this.dataMapping = dataMapping;
            this.basePackage = basePackage;
            ConfigManager configManager = iocManager.getCat(ConfigManager.class);
            if (configManager == null || configManager.get(CHECK_CONFIG) == null){
                return;
            }
            if (configManager.get(CHECK_CONFIG).equals(Boolean.TRUE.toString())) {
                checkTableExistOrCreate();
                checkTableChangeAndUpdate();
            }
        }

        private void checkTableExistOrCreate() {
            List<Class<?>> tableClassList = listAllTableClass();
            for (Class<?> tableClass : tableClassList) {
                Table table = tableClass.getAnnotation(Table.class);
                if (!tableExist(table.name())) {
                    String createTableSql = generateCreateTableSql(tableClass);
                    dataMapping.execute(String.class, createTableSql, false);
                    log.info("table {} not exist, created table", table.name());
                }
            }
        }

        private void checkTableChangeAndUpdate() {
            List<Class<?>> tableClassList = listAllTableClass();
            for (Class<?> tableClass : tableClassList) {
                Table table = tableClass.getAnnotation(Table.class);
                String createSql = getTableDesc(table.name());
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
                    log.info("table {} is change, del field {}, add field: {}", table.name(), deleteFieldMap,
                        addFieldMap);
                    sqlList.forEach(sql -> dataMapping.execute(String.class, sql, false));
                }
            }
        }

        private Map<String, String> parseCreateSql2FieldMap(String createSql) {
            Map<String, String> fields = new HashMap<>(8);

            // 正则表达式匹配字段定义
            Pattern pattern = Pattern.compile("`(\\w+)`\\s+(\\w+)");
            Matcher matcher = pattern.matcher(createSql);

            while (matcher.find()) {
                String fieldName = matcher.group(1);
                String fieldType = matcher.group(2);
                fields.put(fieldName, fieldType);
            }

            return fields;
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
                    "ALTER TABLE " + tableName + " ADD COLUMN `" + fieldName + "` " + javaType2SqlType(fieldType);
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
            List<Class<?>> packageClasses = PackageUtil.getPackageClasses(basePackage, bootClass);
            return packageClasses.stream().filter(clazz -> clazz.isAnnotationPresent(Table.class))
                .collect(Collectors.toList());
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
                        .append(" INTEGER PRIMARY KEY ").append(SpecialKeyword.AUTO_INCREMENT.getKeyword(dataDriver))
                        .append(",");
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
