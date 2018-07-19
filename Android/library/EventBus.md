# EventBus

[TOC]

## 概述

### 使用场景

1. 简化组件间的通信 

- 对发送和接受事件解耦 
- 可以在Activity，Fragment，和后台线程间执行 
- 避免了复杂的和容易出错的依赖和生命周期问题 

1. 让你的代码更简洁 
2. 更快 
3. 更轻量（jar包小于50K） 
4. 事实证明已经有一亿多的APP中集成了EventBus 
5. 拥有先进的功能比如线程分发，用户优先级等等

### 三要素

- Event  事件
  - 可以是任意类型。
- Subscriber 事件订阅者
  - 3.0之前我们必须定义以onEvent开头的那几个方法，分别是onEvent、onEventMainThread、onEventBackgroundThread和onEventAsync
  - 3.0之后事件处理的方法名可以随意取，不过需要加上注解@subscribe()，并且指定线程模型，默认是POSTING。
- Publisher 事件的发布者
  - 可以在任意线程里发布事件，一般情况下，使用EventBus.getDefault()就可以得到一个EventBus对象，然后再调用post(Object)方法即可。

### 线程模型

- POSTING (默认)
  - 事件处理函数的线程跟发布事件的线程在同一个线程。
- MAIN
  - 事件处理函数的线程在主线程(UI)线程
  - 不能进行耗时操作
- MAIN_ORDERED
  - 与MAIN类似，不同之处在于事件的处理顺序更为严格
- BACKGROUND 
  - 事件处理函数的线程在后台线程
    - 如果发布事件的线程是主线程(UI线程)，那么事件处理函数将会开启**唯一**一个后台线程
    - 如果发布事件的线程是在后台线程，那么事件处理函数就使用该线程，按顺序分发事件
  - 不能进行UI操作
- ASYNC
  - 无论事件发布的线程是哪一个，事件处理函数始终会新建一个子线程运行
  - 不能进行UI操作。

### 原理



![img](http://i.imgur.com/U9B8Xtv.png)





2.x 是采用反射的方式对整个注册的类的所有方法进行扫描来完成注册，当然会有性能上的影响。

3.0中EventBus提供了EventBusAnnotationProcessor注解处理器来在编译期通过读取@Subscribe()注解并解析、处理其中所包含的信息，然后生成java类来保存所有订阅者关于订阅的信息，这样就比在运行时使用反射来获得这些订阅者的信息速度要快



![img](https://upload-images.jianshu.io/upload_images/1485091-8bf39ad48834f39c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)



![img](https://upload-images.jianshu.io/upload_images/1485091-b7b63f83d65903d1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)



## 使用

### 添加依赖

```
compile 'org.greenrobot:eventbus:3.0.0'

```

### 定义消息事件类

```java
public class MessageEvent{
    private String message;
    public  MessageEvent(String message){
        this.message=message;
    }
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
}
```

### 注册和解除注册

分别在FirstActivity的onCreate()方法和onDestory()方法里，进行注册EventBus和解除注册。

```java
public class FirstActivity extends AppCompatActivity {
    private Button mButton;
    private TextView mText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);
        mButton = (Button) findViewById(R.id.btn1);
        mText = (TextView) findViewById(R.id.tv1); 
        mText.setText("今天是星期三"); 
        EventBus.getDefault().register(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (FirstActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        mText.setText(messageEvent.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

}
```

### 事件处理

```java
public class SecondActivity extends AppCompatActivity {
    private Button mButton2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        mButton2=(Button) findViewById(R.id.btn2);
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent("欢迎大家浏览我写的博客"));
                finish();
            }
        });
    }
}
```

### 效果

在FirstActivity中，左边是一个按钮，点击之后可以跳转到SecondActivity，在按钮的右边是一个TextView，用来进行结果的验证



![img](https://upload-images.jianshu.io/upload_images/8744053-3b7a7efff24c5ca3.PNG?imageMogr2/auto-orient/strip%7CimageView2/2/w/415)



这是SecondActivity，在页面的左上角，是一个按钮，当点击按钮，就会发送了一个事件，最后这个Activity就会销毁掉



![img](https://upload-images.jianshu.io/upload_images/8744053-11bec3513bf037e1.PNG?imageMogr2/auto-orient/strip%7CimageView2/2/w/411)



此时我们可以看到，FirstActivity里的文字已经变成了，我们在SecondActivity里设置的文字



![img](https://upload-images.jianshu.io/upload_images/8744053-4cd09837aa12c8fd.PNG?imageMogr2/auto-orient/strip%7CimageView2/2/w/411)



### 粘性事件

除了上面讲的普通事件外，EventBus还支持发送黏性事件，就是在发送事件之后再订阅该事件也能收到该事件，跟黏性广播类似。为了验证粘性事件我们修改以前的代码：

#### 订阅粘性事件

在FirstActivity中我们将注册事件添加到button的点击事件中：

```java
bt_subscription.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //注册事件
        EventBus.getDefault().register(MainActivity.this);
    }
});
```

#### 订阅者处理粘性事件

在FirstActivity中新写一个方法用来处理粘性事件：

```java
@Subscribe(threadMode = ThreadMode.POSTING，sticky = true)
public void onStickyEvent(MessageEvent messageEvent){
    tv_message.setText(messageEvent.getMessage());
}
```

#### 发送黏性事件

在SecondActivity中我们定义一个Button来发送粘性事件：

```java
bt_subscription.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        EventBus.getDefault().postSticky(new MessageEvent("粘性事件"));
        finish();
    }
});
```

好了运行代码再来看看效果，首先我们在FirstActivity中并没有订阅事件，而是直接跳到SecondActivity中点击发送粘性事件按钮，这时界面回到FirstActivity，我们看到TextView仍旧显示着FirstActivity的字段，这是因为我们现在还没有订阅事件。



![这里写图片描述](https://img-blog.csdn.net/20160816165158464)————————–> ![这里写图片描述](https://img-blog.csdn.net/20160816165339217)



接下来我们点击订阅事件，TextView发生改变显示“粘性事件”



![这里写图片描述](https://img-blog.csdn.net/20160816165645143)



#### 移除黏性事件

当subscriber注册后，最后一个sticky event会自动匹配。但是，有些时候，主动去检查sticky event会更方便，**并且 sticky event 需要remove,阻断继续传递。**

```java
//返回的是之前的sticky event
MessageEvent stickyEvent = EventBus.getDefault().removeStickyEvent(MessageEvent.class);
// Better check that an event was actually posted before
if(stickyEvent != null) {
    // Now do something with it
}
```



### 设置订阅者的优先级

**如果不设置优先级，所有的订阅者都会收到消息，随机顺序**

使用注解参数priority，数字越大，优先级越高

```java
@Subscribe(priority = 1); //默认的是0
public void onEvent(MessageEvent event) {
}
```

### 停止事件传递

```java
// Called in the same thread (default)
@Subscribe
public void onEvent(MessageEvent event){
    // Process the event

    EventBus.getDefault().cancelEventDelivery(event) ;
}
```

### 个性化配置EventBus

当默认的EventBus不足以满足需求时，EventBusBuilder就上场了，EventBusBuilder允许配置各种需求的EventBus

当没有subscribers的时候，eventbus保持静默

```java
EventBus eventBus = EventBus.builder().logNoSubscriberMessages(false)
    .sendNoSubscriberEvent(false).build();
```

默认情况下，eventbus捕获onevent抛出的异常，并且发送一个SubscriberExceptionEvent 可能不必处理

```java
EventBus eventBus = EventBus.builder().throwSubscriberException(true).build();
```

配置单例

**官方推荐：在application类中，配置eventbus单例，保证eventbus的统一**

例如：配置eventbus 只在DEBUG模式下，抛出异常，便于自测，同时又不会导致release环境的app崩溃

注意`installDefaultEventBus()`必须在第一次使用前调用，否则会抛出异常

```java
EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();
```

[EventBus 3.0 源码分析](https://www.jianshu.com/p/f057c460c77e)

[Android 消息传递之 EventBus 3.0 使用详解](https://juejin.im/entry/57d5f5b47db2a200683e05d1)

[EventBus 3.0使用详解](https://www.jianshu.com/p/f9ae5691e1bb)

[Android事件总线（一）EventBus3.0用法全解析](https://blog.csdn.net/itachi85/article/details/52205464)

[#Android# 学EventBus，你可以参考下我的笔记](https://www.jianshu.com/p/4a3d953d1319)

[EventBus Documentation](http://greenrobot.org/eventbus/documentation/)