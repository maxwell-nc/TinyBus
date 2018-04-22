package com.maxwell.nc.library.core;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.scheduler.Scheduler;
import com.github.maxwell.nc.reactivelib.scheduler.Schedulers;
import com.maxwell.nc.library.annotation.EventThread;
import com.maxwell.nc.library.model.ListMap;

import java.util.List;

/**
 * 事件管理发布者
 */
public class TinyBusPublisher extends Publisher {

    private final ListMap<TinyBusSubscriber> subscriberMap = ListMap.newMap();
    private final ListMap<Object> eventMap = ListMap.newMap();

    @Override
    protected void subscribeActual(Subscriber subscriber) {
    }

    public <T> void addSubscriber(TinyBusSubscriber<T> tinyBusSubscriber) {
        //触发Sticky事件
        if (tinyBusSubscriber.isSticky()) {

            //noinspection unchecked
            List<T> events = (List<T>) eventMap.get(tinyBusSubscriber.getEventTag());

            if (events != null) {
                for (T event : events) {
                    emmitActual(event, tinyBusSubscriber);
                }
            }
        }

        subscriberMap.put(tinyBusSubscriber.getEventTag(), tinyBusSubscriber);
    }

    public <T> void removeSubscriber(TinyBusSubscriber<T> tinyBusSubscriber) {
        if (tinyBusSubscriber == null) {
            return;
        }
        subscriberMap.remove(tinyBusSubscriber.getEventTag(), tinyBusSubscriber);
    }

    public <T> void removeStickyAll(Class<T> event) {
        eventMap.remove(event.getName());
    }

    public <T> void removeSticky(T event) {
        eventMap.remove(getEventTag(event), event);
    }

    private <T> String getEventTag(T event) {
        //获取泛型类型
        Class clazz = (Class) event.getClass().getGenericSuperclass();
        if (clazz == Object.class) {//不是内部类
            clazz = event.getClass();
        }
        return clazz.getName();
    }

    /**
     * 发送事件到订阅者
     */
    public <T> void emmitEvent(boolean isSticky, final T event) {
        String eventTag = getEventTag(event);

        //存储Sticky事件
        if (isSticky) {
            eventMap.put(eventTag, event);
        }

        List<TinyBusSubscriber> tinyBusSubscribers = subscriberMap.get(eventTag);
        if (tinyBusSubscribers == null) {
            return;
        }

        //noinspection unchecked
        for (TinyBusSubscriber<T> tinyBusSubscriber : tinyBusSubscribers) {

            //判断Sticky
            if (isSticky != tinyBusSubscriber.isSticky()) {
                continue;
            }
            emmitActual(event, tinyBusSubscriber);
        }

    }

    private <T> void emmitActual(T event, TinyBusSubscriber<T> tinyBusSubscriber) {

        //判断事件回调执行线程
        EventThread thread = tinyBusSubscriber.getThread();
        Scheduler observeOnSchedulers;
        switch (thread) {
            default:
            case MAIN:
                observeOnSchedulers = Schedulers.mainThread();
                break;
            case NEW_THREAD:
                observeOnSchedulers = Schedulers.newThread();
                break;
            case COMPUTE:
                observeOnSchedulers = Schedulers.parallel();
                break;
            case IMMEDIATE:
                observeOnSchedulers = null;
                break;
        }

        Publisher<T> eventPublisher = just(event);
        if (observeOnSchedulers == null) {//立即处理
            eventPublisher.subscribe(tinyBusSubscriber);
        } else {
            eventPublisher
                    .subscribeOn(Schedulers.parallel())
                    .observeOn(observeOnSchedulers)
                    .subscribe(tinyBusSubscriber);
        }

    }

}
