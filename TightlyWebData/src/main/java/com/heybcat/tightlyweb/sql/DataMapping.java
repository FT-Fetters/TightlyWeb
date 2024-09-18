package com.heybcat.tightlyweb.sql;

import com.heybcat.tightlyweb.sql.entity.Page;
import com.heybcat.tightlyweb.sql.entity.PageInfo;
import java.util.List;

/**
 * @author Fetters
 */
public interface DataMapping {

    /**
     * save obj to database, obj require @Table annotation
     * @param obj table obj
     * @return save result
     */
    boolean save(Object obj);

    <T> T getById(Class <T> clazz, Object id);

    <T> Page<T> selectPage(int size, int current, MapperMethodRef<T> ref);

    PageInfo getPageInfo();

    boolean delete(Object obj);

    boolean deleteById(Class<?> clazz, Object id);

    <T> List<T> execute(Class<T> clazz, String sql, boolean isQuery, Object... params);

    <T> List<T> select(Class<T> clazz, String sql, Object... params);

    boolean tableExist(String tableName);

    String getTableDesc(String tableName);

}
