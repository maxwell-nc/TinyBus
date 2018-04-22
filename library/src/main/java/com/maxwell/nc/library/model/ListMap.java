package com.maxwell.nc.library.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 列表型Map的封装
 */
public class ListMap<T> {

    private Map<String, List<T>> map;

    private ListMap(Map<String, List<T>> map) {
        this.map = map;
    }

    public static <T> ListMap<T> newConcurrent() {
        return new ListMap<>(new ConcurrentHashMap<String, List<T>>());
    }

    public static <T> ListMap<T> newMap() {
        return new ListMap<>(new HashMap<String, List<T>>());
    }

    /**
     * 获取列表数据
     */
    public List<T> get(String key) {
        return map.get(key);
    }

    /**
     * 添加一个元素
     */
    public void put(String key, T target) {
        List<T> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(target);
        map.put(key, list);
    }

    /**
     * 删除一个元素
     */
    public void remove(String key, T target) {
        List<T> list = map.get(key);
        if (list == null) {
            return;
        }
        list.remove(target);
        if (list.isEmpty()) {//已经没有元素了
            map.remove(key);
            return;
        }
        map.put(key, list);
    }

    /**
     * 删除key的所有元素
     */
    public void remove(String key) {
        map.remove(key);
    }

}
