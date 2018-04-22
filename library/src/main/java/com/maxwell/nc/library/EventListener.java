package com.maxwell.nc.library;

/**
 * 事件监听器
 */
public interface EventListener<T> {
    void onEvent(T event);
}
