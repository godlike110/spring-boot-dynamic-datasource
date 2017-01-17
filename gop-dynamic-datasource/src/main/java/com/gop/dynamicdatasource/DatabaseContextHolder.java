package com.gop.dynamicdatasource;

import com.gop.dynamicdatasource.enums.DatasourceType;

/**
 * 保存一个线程安全的DatabaseType容器
 * Created by wenzhiwei on 17/1/16.
 */
public class DatabaseContextHolder {

    private static final ThreadLocal<DatasourceType> contextHolder = new ThreadLocal<DatasourceType>();

    public static void setDatabaseType(DatasourceType type) {
        contextHolder.set(type);
    }

    public static DatasourceType getDatabaseType() {
        return contextHolder.get();
    }

}
