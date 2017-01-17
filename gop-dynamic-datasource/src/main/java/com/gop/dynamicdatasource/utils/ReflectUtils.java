package com.gop.dynamicdatasource.utils;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by wenzhiwei on 16/11/8.
 */
public class ReflectUtils {

    public static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * 获得一个对象各个属性的字节流
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getObjectMap(Object entityName)
            throws Exception {
        Class c = entityName.getClass();
        Field field[] = c.getDeclaredFields();
        Map<String, Object> map = Maps.newHashMap();
        for (Field f : field) {
            Object v = ReflectUtils.invokeMethod(entityName, f.getName(), null);
            map.put(f.getName(), v);
        }
        return map;
    }

    /**
     * 获得对象属性的值
     */
    @SuppressWarnings("unchecked")
    private static Object invokeMethod(Object owner, String methodName,
            Object[] args) throws Exception {
        Class ownerClass = owner.getClass();
        methodName = methodName.substring(0, 1).toUpperCase()
            + methodName.substring(1);
        Method method = null;
        try {
            method = ownerClass.getMethod("get" + methodName);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            ReflectUtils.logger.error("invokeMethod error!", e.getMessage());
            return null;
        }
        return method.invoke(owner);
    }

}
