package com.gop.dynamicdatasource.Aspects;

import com.gop.dynamicdatasource.Annotations.Datasource;
import com.gop.dynamicdatasource.DatabaseContextHolder;
import com.gop.dynamicdatasource.enums.DatasourceType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Component;

/**
 * Created by wenzhiwei on 17/1/16.
 */
@Aspect
@Component
public class DataSourceAspect {


    @Before("execution(* com.gop.*.dao..*.*(..))")
    public void setDataSourceKey(JoinPoint point){
        //连接点所属的类实例是ShopDao
        //Datasource datasource = point.getTarget().getClass().getAnnotation(Datasource.class);
        Datasource datasource = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(Datasource.class);

        if(null==datasource) {
            DatabaseContextHolder.setDatabaseType(DatasourceType.WRITE);
        } else {
            DatabaseContextHolder.setDatabaseType(datasource.value());
        }

    }

}
