package com.maxwell.nc.library;

import com.maxwell.nc.library.annotation.EventThread;
import com.maxwell.nc.library.core.TinyBusPublisher;
import com.maxwell.nc.library.core.TinyBusSubscriber;

import java.util.WeakHashMap;

/**
 * 事件总线处理器
 */
public class TinyBus {

    private final WeakHashMap<Object, ReflectSubscriber> reflectSubscriberMap = new WeakHashMap<>();

    private final TinyBusPublisher publisher = new TinyBusPublisher();

    /**
     * 单例
     */
    private static TinyBus bus = new TinyBus();

    private TinyBus() {
    }

    /**
     * 获取总线
     */
    public synchronized static TinyBus getDefault() {
        if (bus == null) {
            bus = new TinyBus();
        }
        return bus;
    }

    /**
     * 获取生成的订阅者
     */
    private synchronized <T> ReflectSubscriber<T> getTargetSubscriber(T source) {
        //已经存在
        ReflectSubscriber reflectSubscriber = bus.reflectSubscriberMap.get(source);
        if (reflectSubscriber != null) {
            //noinspection unchecked
            return (ReflectSubscriber<T>) reflectSubscriber;
        }

        Class<?> subscriberClass = source.getClass();
        String packageName = subscriberClass.getPackage().getName();
        String simpleName = subscriberClass.getSimpleName();
        ClassLoader classLoader = subscriberClass.getClassLoader();
        try {
            return findSubscriberClass(classLoader, packageName, simpleName);
        } catch (Exception e) {
            //寻找失败，尝试寻找父类
            try {
                return findSubscriberClass(classLoader, packageName,
                        subscriberClass.getSuperclass().getSimpleName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private <T> ReflectSubscriber<T> findSubscriberClass(ClassLoader classLoader, String packageName, String simpleName)
            throws Exception {
        Class<?> loadClass = classLoader.loadClass(packageName + "." + simpleName + "$$BusSubscriber");
        //noinspection unchecked
        return (ReflectSubscriber<T>) loadClass.newInstance();
    }

    /**
     * 注册订阅事件
     */
    public static <T> void register(T source) {
        if (bus.reflectSubscriberMap.containsKey(source)) {
            return;//已经注册过
        }
        TinyBus bus = getDefault();
        ReflectSubscriber<T> targetSubscriber = bus.getTargetSubscriber(source);
        if (targetSubscriber != null) {
            targetSubscriber.register(source);
            bus.reflectSubscriberMap.put(source, targetSubscriber);
        }
    }

    /**
     * 取消注册订阅事件
     */
    public static <T> void unRegister(T source) {
        TinyBus bus = getDefault();
        ReflectSubscriber<T> targetSubscriber = bus.getTargetSubscriber(source);
        if (targetSubscriber != null) {
            targetSubscriber.unRegister();
        }
    }

    /**
     * 发送事件
     */
    public static <T> void post(T event) {
        getDefault().publisher.emmitEvent(false, event);
    }

    /**
     * 发送Sticky事件
     */
    public static <T> void postSticky(T event) {
        getDefault().publisher.emmitEvent(true, event);
    }

    /**
     * 移除所有同类型Sticky事件
     */
    public static <T> void removeStickyAll(Class<T> event) {
        getDefault().publisher.removeStickyAll(event);
    }

    /**
     * 移除Sticky事件（同类型不会被移除）
     */
    public static <T> void removeSticky(T event) {
        getDefault().publisher.removeSticky(event);
    }

    public <T> TinyBusSubscriber<T> subscribeEvent(String eventTag, EventThread thread, boolean isSticky,
                                                   final EventListener<T> eventListener) {

        if (thread == null) {
            thread = EventThread.MAIN;
        }

        //创建订阅者
        TinyBusSubscriber<T> tinyBusSubscriber = new TinyBusSubscriber<T>(eventTag, thread, isSticky) {

            @Override
            public void onNext(T event) {
                if (eventListener != null) {
                    eventListener.onEvent(event);
                }
            }

        };

        //添加到总线
        publisher.addSubscriber(tinyBusSubscriber);

        return tinyBusSubscriber;
    }

    public void unSubscribeEvent(TinyBusSubscriber subscriber) {
        publisher.removeSubscriber(subscriber);
    }


}

