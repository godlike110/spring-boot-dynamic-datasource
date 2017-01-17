package com.gop.dynamicdatasource;

/**
 * Created by wenzhiwei on 16/9/6.
 */

import com.alibaba.fastjson.JSON;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.gop.dynamicdatasource.utils.ReflectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wenzhiwei on 16/7/8.
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {
        MappedStatement.class, Object.class }),
    @Signature(type = Executor.class, method = "query", args = {
        MappedStatement.class, Object.class, RowBounds.class,
        ResultHandler.class }),
    @Signature(type = Executor.class, method = "query", args = {
        MappedStatement.class, Object.class, RowBounds.class,
        ResultHandler.class, CacheKey.class, BoundSql.class }) })
public class SQLMonitorInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory
        .getLogger(SQLMonitorInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation
            .getArgs()[0];

        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }

        String sql = mappedStatement.getBoundSql(parameter).getSql();

        SQLMonitorInterceptor.logger.info("Original sql:{},param:{}", sql,
            JSON.toJSON(parameter));
        if (parameter instanceof Map) {
            if (sql.startsWith("INSERT") || sql.startsWith("insert")) {
                SQLMonitorInterceptor.logger.info("Execute sql:{}",
                    SQLMonitorInterceptor.getInsertSql(sql, parameter));
            } else {
                SQLMonitorInterceptor.logger.info("Execute sql: {}",
                    SQLMonitorInterceptor.getSqlString(sql, (Map) parameter));
            }
        } else {
            if (sql.startsWith("INSERT") || sql.startsWith("insert")) {
                SQLMonitorInterceptor.logger.info("Execute sql:{}",
                    SQLMonitorInterceptor.getInsertSql(sql, parameter));
            } else {
                SQLMonitorInterceptor.logger.info("Execute sql: {}",
                    SQLMonitorInterceptor.getSqlStringFromObj(sql, parameter));
            }
        }
        try {
            long start = System.currentTimeMillis();
            Object object = invocation.proceed();
            long end = System.currentTimeMillis();
            if (end - start > 1000) {
                SQLMonitorInterceptor.logger.warn(
                    "Slow sql {} millis. sql: {}. parameter: {}", end - start,
                    sql, JSON.toJSON(parameter));
            }
            return object;
        } catch (Exception e) {
            SQLMonitorInterceptor.logger.error(
                "SQL Error: {}, SQL Parameter: {}, SQL Exception: ", sql,
                JSON.toJSON(parameter), e);
            throw e;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    public static String getInsertSql(String sql, Object object) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }
        if (null == object) {
            return sql;
        }

        if (object instanceof List) {
            return sql;
        }

        try {
            StringBuffer sb = new StringBuffer(sql.substring(0,
                sql.indexOf("(")));
            String paramReg = "\\([^\\?]*\\)";
            Pattern pattern = Pattern.compile(paramReg);
            Matcher matcher = pattern.matcher(sql);
            ArrayList<String> strs = new ArrayList<>();
            while (matcher.find()) {
                strs.add(matcher.group(0));
            }
            Map<String, Object> fieldMap = ReflectUtils.getObjectMap(object);
            for (String s : strs) {

                String[] fields = org.apache.commons.lang3.StringUtils.split(CharMatcher.anyOf(" ()")
                    .removeFrom(s), ',');
                StringBuffer fieldStr = new StringBuffer("");
                StringBuffer valueStr = new StringBuffer("");

                for (String field : fields) {
                    if (fieldStr.length() == 0) {
                        fieldStr.append("(").append(field);
                        valueStr.append("('").append(fieldMap.get(field))
                            .append("'");
                    } else {
                        fieldStr.append(",").append(field);
                        valueStr.append(",'").append(fieldMap.get(field))
                            .append("'");
                    }
                }

                fieldStr.append(")");
                valueStr.append(")");

                sb.append(fieldStr).append(" VALUES ").append(valueStr);

            }

            return sb.toString();

        } catch (Exception e) {
            //SQLMonitorInterceptor.logger.error(
            //    "SQLMonitorInterceptor getInsertSql error", e.getMessage());
        }

        return sql;

    }

    public static String getSqlStringFromObj(String sql, Object object) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }
        if (null == object) {
            return sql;
        }
        try {
            String paramReg = "\\s{1}\\S*\\s{0,}=\\s{0,}\\?{1}";
            Pattern pattern = Pattern.compile(paramReg);
            Matcher matcher = pattern.matcher(sql);
            ArrayList<String> strs = new ArrayList<>();
            while (matcher.find()) {
                strs.add(matcher.group(0));
            }
            Map<String, Object> fieldMap = ReflectUtils.getObjectMap(object);
            for (String s : strs) {
                Map<String, String> kvMap = Splitter.on(",")
                    .withKeyValueSeparator("=")
                    .split(CharMatcher.anyOf(" ").removeFrom(s));
                String key = kvMap.keySet().iterator().next();
                String value = String.valueOf(fieldMap.get(key));
                String replacedString = s.replace("?", "'" + value + "'");
                sql = sql.replace(s, replacedString);
            }
        } catch (Exception e) {
            //   SQLMonitorInterceptor.logger.error(
            //       "SQLMonitorInterceptor getSqlStringFromObj error",
            //       e.getMessage());
        }
        return sql;

    }

    /**
     * 获取sql String
     *
     * @param sql
     * @param param
     * @return
     */
    public static String getSqlString(String sql, Map param) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }
        if (null == param) {
            return sql;
        }
        try {
            String paramReg = "\\s{1}\\S*\\s{0,}=\\s{0,}\\?{1}";
            Pattern pattern = Pattern.compile(paramReg);
            Matcher matcher = pattern.matcher(sql);
            ArrayList<String> strs = new ArrayList<>();
            while (matcher.find()) {
                strs.add(matcher.group(0));
            }
            for (String s : strs) {
                Map<String, String> kvMap = Splitter.on(",")
                    .withKeyValueSeparator("=")
                    .split(CharMatcher.anyOf(" ").removeFrom(s));
                String key = kvMap.keySet().iterator().next();
                String value = String.valueOf(param.get(key));
                String replacedString = s.replace("?", "'" + value + "'");
                sql = sql.replace(s, replacedString);
            }
        } catch (Exception e) {
            //   SQLMonitorInterceptor.logger.error(
            //       "SQLMonitorInterceptor getSqlString error", e.getMessage());
        }
        return sql;

    }

    public static void main(String[] args) {
        System.out.println(CharMatcher.anyOf(" ()").removeFrom(
            "(a,b,c,d,e,f ) "));
        System.out.println(new StringBuffer("").length());
    }

}
