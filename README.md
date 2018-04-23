# TinyBus

基于[我的轻量级ReactiveX框架](https://github.com/maxwell-nc/ReactiveLite)开发的事件总线类库，方便项目开发。

A Tiny ReactiveX EventBus base on [my lite reactive lib](https://github.com/maxwell-nc/ReactiveLite).

## 特点 Feature

- 轻量级（含库依赖不超过50kb）
- 快速（使用编译时注解）

## 依赖 Gradle

目前部署在JCenter方便使用，直接修改build.gradle文件：

```
    compile 'com.maxwell.nc:ReactiveLite:1.3'//基于此库开发
    compile 'com.maxwell.nc:TinyBus:1.0'
    annotationProcessor 'com.maxwell.nc:TinyBus-Processor:1.1'//编译时注解处理器
```

## 用法 Usage

1.注册TinyBus，可以在任意类（包括内部类）中进行注册，下面以Activity为例：

```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TinyBus.register(this);//注册
    }

    @Override
    protected void onDestroy() {
        TinyBus.unRegister(this);//解除注册
        super.onDestroy();
    }
```

2.增加事件接收方法：

- 方法名随意(Processor1.1支持接收方法重载)
- 方法必须为public、不能为static（效率考虑）
- T为事件类型,用于匹配事件
- 加上@TinyEvent注解


```java
    @TinyEvent
    public void onEvent(T event) {
		//处理接收事件
    }
```

3.发送事件：

```java
    TinyBus.post(event);
```

## Sticky事件

Sticky事件用于事件发送时，订阅者还没注册，等到订阅者注册时需要触发已经发送的事件的情况。

对于Sticky的事件只匹配设置了sticky = true属性（默认为false）的接收器：

```java
    @TinyEvent(sticky = true)
    public void onEvent(T event) {
	//处理接收事件
    }
```

Sticky事件需要使用postSticky方法发送，事件发送后会一直缓存，需要手动移除：

```java
    TinyBus.postSticky(event);
    TinyBus.removeSticky(event);//移除单个
    TinyBus.removeStickyAll(Event.class);//移除同类型所有
```

## 线程控制 Thread Controller

订阅者执行的线程可以通过注解参数设置：

```java
    @TinyEvent(thread = EventThread.IMMEDIATE)
```

其中EventThread提供了4种线程模式：

- **MAIN：** 主线程运行
- **NEW_THREAD：** 每个在单独线程运行
- **COMPUTE：** 在一定大小的线程池中运行
- **IMMEDIATE：** 在发送的线程中执行

