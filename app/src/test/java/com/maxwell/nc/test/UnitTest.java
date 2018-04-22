package com.maxwell.nc.test;

import com.maxwell.nc.library.TinyBus;
import com.maxwell.nc.library.annotation.EventThread;
import com.maxwell.nc.library.annotation.TinyEvent;
import com.maxwell.nc.test.event.EventA;
import com.maxwell.nc.test.event.EventB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 单元测试
 */
@RunWith(MockitoJUnitRunner.class)
public class UnitTest {

    @Mock
    private static ReceiveClass receiveClass;

    @Before
    public void setup() throws Exception {
        TinyBus.register(receiveClass);
    }

    @After
    public void tearDown() throws Exception {
        TinyBus.unRegister(receiveClass);
    }

    /**
     * 测试多事件对多事件接收器情况
     */
    @Test
    public void testNormalEvent() throws Exception {
        EventA event1 = new EventA();
        EventA event2 = new EventA();
        TinyBus.post(event1);
        TinyBus.post(event2);
        verify(receiveClass, times(1)).onNormal1(event1);//匹配事件1一次
        verify(receiveClass, times(1)).onNormal1(event2);//匹配事件2一次
        verify(receiveClass, times(1)).onNormal2(event1);//匹配事件1一次
        verify(receiveClass, times(1)).onNormal2(event2);//匹配事件2一次
        verify(receiveClass, times(0)).onNormal3((EventA) any());//Sticky不匹配
        verify(receiveClass, times(0)).onSticky1((EventB) any());//事件不匹配
        verify(receiveClass, times(0)).onSticky2((EventB) any());//事件不匹配
    }

    /**
     * 测试Sticky事件匹配情况
     */
    @Test
    public void testStickyEvent() throws Exception {
        EventA event1 = new EventA();
        EventB event2 = new EventB();
        TinyBus.postSticky(event2);
        TinyBus.postSticky(event1);
        verify(receiveClass, times(0)).onNormal1((EventA) any());//Sticky不匹配
        verify(receiveClass, times(0)).onNormal2((EventA) any());//Sticky不匹配
        verify(receiveClass, times(1)).onNormal3(event1);//接收对应Sticky事件1一次
        verify(receiveClass, times(1)).onSticky1(event2);//接收对应Sticky事件2一次
        verify(receiveClass, times(1)).onSticky2(event2);//接收对应Sticky事件2一次

        //注意移除，防止影响后续
        TinyBus.removeSticky(event1);
        TinyBus.removeSticky(event2);
    }

    /**
     * 测试接收准确性
     */
    @Test
    public void testOrder() throws Exception {
        EventA event = new EventA();
        TinyBus.post(event);
        verify(receiveClass, times(1)).onNormal1(event);//匹配
        verify(receiveClass, times(1)).onNormal2(event);//匹配
        verify(receiveClass, times(0)).onNormal3(event);//Sticky不匹配
        verify(receiveClass, times(0)).onSticky1((EventB) any());
        verify(receiveClass, times(0)).onSticky2((EventB) any());

        TinyBus.postSticky(event);
        verify(receiveClass, times(1)).onNormal1(event);//加上前面的，相当于此处没执行
        verify(receiveClass, times(1)).onNormal2(event);//加上前面的，相当于此处没执行
        verify(receiveClass, times(1)).onNormal3(event);//Sticky匹配
        verify(receiveClass, times(0)).onSticky1((EventB) any());
        verify(receiveClass, times(0)).onSticky2((EventB) any());

        //测试移除同类型所有
        TinyBus.removeStickyAll(EventA.class);
    }

    public class ReceiveClass {

        @TinyEvent(thread = EventThread.IMMEDIATE)
        public void onNormal1(EventA event) {
            //测试普通事件
        }

        @TinyEvent(thread = EventThread.IMMEDIATE)
        public void onNormal2(EventA event) {
            //测试多个接收者接收情况
        }

        @TinyEvent(thread = EventThread.IMMEDIATE, sticky = true)
        public void onNormal3(EventA event) {
            //测试多个接收者中匹配Sticky情况
        }

        @TinyEvent(thread = EventThread.IMMEDIATE, sticky = true)
        public void onSticky1(EventB event) {
            //测试Sticky事件
        }

        @TinyEvent(thread = EventThread.IMMEDIATE, sticky = true)
        public void onSticky2(EventB event) {
            //测试多个Sticky事件接收者接收情况
        }

    }

}
