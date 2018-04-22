package com.maxwell.nc.library.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记事件的注解<br>
 * com.maxwell.nc.library.annotation.TinyEvent
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface TinyEvent {

    /**
     * 事件执行线程
     */
    EventThread thread() default EventThread.MAIN;

    /**
     * 是否为Sticky事件
     */
    boolean sticky() default false;

}
