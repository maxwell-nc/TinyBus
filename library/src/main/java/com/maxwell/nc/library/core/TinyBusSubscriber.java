package com.maxwell.nc.library.core;

import com.github.maxwell.nc.reactivelib.FlowSubscriber;
import com.maxwell.nc.library.annotation.EventThread;

/**
 * 事件订阅者
 */
public abstract class TinyBusSubscriber<T> extends FlowSubscriber<T> {

    /**
     * 事件标记，用于匹配事件
     */
    private String eventTag;

    private EventThread thread;

    private boolean isSticky;

    public TinyBusSubscriber(String eventTag, EventThread thread, boolean isSticky) {
        this.eventTag = eventTag;
        this.thread = thread;
        this.isSticky = isSticky;
    }

    public String getEventTag() {
        return eventTag;
    }

    public EventThread getThread() {
        return thread;
    }

    public boolean isSticky() {
        return isSticky;
    }

}
