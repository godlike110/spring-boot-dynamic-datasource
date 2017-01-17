package com.gop.dynamicdatasource.Annotations;

import com.gop.dynamicdatasource.enums.DatasourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wenzhiwei on 17/1/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.METHOD})
public @interface Datasource {

    DatasourceType value();

}
