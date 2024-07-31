package com.heybcat.tightlyweb.sql;

import com.heybcat.tightlyweb.sql.entity.Page;
import com.heybcat.tightlyweb.sql.entity.PageInfo;

/**
 * @author Fetters
 */
public interface DataMapping {

    boolean save(Object obj);

    <T> T getById(Class <T> clazz, Object id);

    <T> Page<T> selectPage(int size, int current, MapperMethodRef<T> ref);

    PageInfo getPageInfo();
}
