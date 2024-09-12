package com.heybcat.tightlyweb.sql.support.proxy;

import com.heybcat.tightlyweb.sql.DataMapping;
import com.heybcat.tightlyweb.sql.annotation.Insert;
import com.heybcat.tightlyweb.sql.annotation.Select;
import com.heybcat.tightlyweb.sql.entity.PageInfo;
import com.heybcat.tightlyweb.sql.parser.DefaultSqlParser;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import net.sf.cglib.proxy.MethodProxy;
import xyz.ldqc.tightcall.consumer.proxy.factory.ProxyMethodInterceptor;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
public class MapperMethodInterceptor implements ProxyMethodInterceptor {

    private final DataMapping dataMapping;

    public MapperMethodInterceptor(DataMapping dataMapping) {
        this.dataMapping = dataMapping;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy,
        Callable<?> callable) throws Throwable {
        // cglib not run here
        return null;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy)
        throws Throwable {
        Select selectAnnotation = method.getAnnotation(Select.class);
        if (selectAnnotation != null) {
            if (List.class.isAssignableFrom(method.getReturnType())) {
                return select(o, method, args, methodProxy);
            } else {
                List<?> selectResult = select(o, method, args, methodProxy);
                return selectResult.isEmpty() ? null : selectResult.get(0);
            }
        }
        Insert insertAnnotation = method.getAnnotation(Insert.class);
        if (insertAnnotation != null) {
            return insert(o, method, args, methodProxy);
        }

        return null;
    }

    private List<?> select(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        Select selectAnnotation = method.getAnnotation(Select.class);
        String sql = selectAnnotation.value();
        if (StringUtil.isBlank(sql)) {
            return Collections.emptyList();
        }
        Class<?> returnType = method.getReturnType();

        sql = DefaultSqlParser.parseSql(selectAnnotation.expressions(), method, sql, args);

        PageInfo pageInfo = dataMapping.getPageInfo();
        if (pageInfo != null) {
            String fromIndex = sql.substring(sql.toUpperCase().indexOf("FROM"));
            String countSql = "SELECT COUNT(*) " + fromIndex;
            List<Long> count = dataMapping.select(Long.class, countSql, args);
            if (count.isEmpty() || count.get(0) == 0 || count.get(0) < (long) pageInfo.getSize() * (
                pageInfo.getCurrent() - 1)) {
                return Collections.emptyList();
            }
            pageInfo.setTotal(count.get(0).intValue());
            if (!sql.toUpperCase().contains("LIMIT")) {
                int start = (pageInfo.getCurrent() - 1) * pageInfo.getSize();
                sql = String.format("SELECT * FROM (%s) AS t LIMIT %d, %d", sql, start, pageInfo.getSize());
            }
        }

        if (returnType.isArray()) {
            return dataMapping.select(returnType.getComponentType(), sql, args);
        }
        if (List.class.isAssignableFrom(returnType)) {
            Type genericReturnType = method.getGenericReturnType();
            Type actualTypeArgument = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
            return dataMapping.select((Class<?>) actualTypeArgument, sql, args);
        }
        return dataMapping.select(returnType, sql, args);
    }

    private long insert(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        Insert insetAnnotation = method.getAnnotation(Insert.class);
        String sql = insetAnnotation.value();
        if (StringUtil.isBlank(sql)) {
            return -1;
        }

        sql = DefaultSqlParser.parseSql(insetAnnotation.expressions(), method, sql, args);

        return 0;
    }


}
