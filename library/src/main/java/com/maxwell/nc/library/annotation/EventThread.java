package com.maxwell.nc.library.annotation;

/**
 * 时间线程
 */
public enum EventThread {

    /**
     * UI线程
     */
    MAIN,

    /**
     * 单独先线程
     */
    NEW_THREAD,

    /**
     * 计算用的线程（复用）
     */
    COMPUTE,

    /**
     * 发送所在线程立即操作
     */
    IMMEDIATE

}
