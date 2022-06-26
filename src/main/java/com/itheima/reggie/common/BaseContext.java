package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类,用户保存和获取当前登陆用户id
 */
public class BaseContext {
    private static final ThreadLocal<Long> THREADLOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        THREADLOCAL.set(id);
    }

    public static Long getCurrentId(){
        return THREADLOCAL.get();
    }
}
