package com.maxwell.nc.library;

/**
 * 用于生成java代码的接口
 */
public interface ReflectSubscriber<T> {

    /**
     * 注册事件
     */
    void register(T source);

    /**
     * 取消注册事件
     */
    void unRegister();

}

