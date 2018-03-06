# Android开发面试题

[TOC]

# 触摸事件的分发

## 原理

事件分发流程图分为3层，从上往下依次是Activity、ViewGroup、View

![](https://upload-images.jianshu.io/upload_images/966283-b9cb65aceea9219b.png)

* 事件从左上角那个白色箭头开始，由Activity的dispatchTouchEvent做分发
* 箭头的上面字代表方法返回值，（return true、return false、return super.xxxxx(),super 的意思是调用父类实现）
* dispatchTouchEvent和 onTouchEvent的框里有个【**true---->消费**】的字，表示的意思是如果方法返回true，那么代表事件就此消费，不会继续往别的地方传了，事件终止
* 目前图中的事件是仅仅针对ACTION_DOWN的
* 只有return super.dispatchTouchEvent(ev) 才是往下走，返回true 或者 false 事件就被消费了（终止传递）

## 关键点

1. 默认实现流程：整个事件流向应该是从Activity---->ViewGroup--->View 从上往下调用dispatchTouchEvent方法，一直到叶子节点（View）的时候，再由View--->ViewGroup--->Activity从下往上调用onTouchEvent方法
2. 事件消费：dispatchTouchEvent 和 onTouchEvent 一旦return true,事件就停止传递了（到达终点）（没有谁能再收到这个事件）。看下图中只要return true事件就没再继续传下去了，对于return true我们经常说事件被消费了，消费了的意思就是事件走到这里就是终点，不会往下传，没有谁能再收到这个事件了。dispatchTouchEvent 和 onTouchEvent return false的时候事件都回传给父控件的onTouchEvent处理

> 对于dispatchTouchEvent 返回 false 的含义应该是：事件停止往子View传递和分发同时开始往父控件回溯（父控件的onTouchEvent开始从下往上回传直到某个onTouchEvent return true），事件分发机制就像递归，return false 的意义就是递归停止然后开始回溯。

3. dispatchTouchEvent、onTouchEvent、onInterceptTouchEvent：ViewGroup 和View的这些方法的默认实现就是会让整个事件按U型完整走完，所以 return super.xxxxxx() 就会让事件依照U型的方向，完整走完整个事件流动路径），中间不做任何改动，不回溯、不终止，每个环节都走到

4. onInterceptTouchEvent 的作用：每个ViewGroup每次在做分发的时候，问一问拦截器要不要拦截（也就是问问自己这个事件要不要自己来处理）如果要自己处理那就在onInterceptTouchEvent方法中 return true就会交给自己的onTouchEvent的处理，如果不拦截就是继续往子控件往下传。默认是不会去拦截的，因为子View也需要这个事件，所以onInterceptTouchEvent拦截器return super.onInterceptTouchEvent()相当于return false，是不会拦截的，事件会继续往子View的dispatchTouchEvent传递

   ViewGroup怎样通过dispatchTouchEvent方法能把事件分发到自己的onTouchEvent处理呢？return true和false 都不行，那么只能通过Interceptor把事件拦截下来给自己的onTouchEvent，所以ViewGroup dispatchTouchEvent方法的super默认实现就是去调用onInterceptTouchEvent，记住这一点


5. onTouchEvent消费事件的情况：在哪个View的onTouchEvent 返回true，那么ACTION_MOVE和ACTION_UP的事件从上往下传到这个View后就不再往下传递了，而直接传给自己的onTouchEvent 并结束本次事件传递过程

6. ACTION_MOVE、ACTION_UP总结：ACTION_DOWN事件在哪个控件消费了（return true）， 那么当ACTION_MOVE和ACTION_UP就会从上往下（通过dispatchTouchEvent）做事件分发往下传时，就只会传到这个控件，不会继续往下传。

   如果ACTION_DOWN事件是在dispatchTouchEvent消费，那么事件到此为止停止传递，如果ACTION_DOWN事件是在onTouchEvent消费的，那么会把ACTION_MOVE或ACTION_UP事件传给该控件的onTouchEvent处理并结束传递

## 总结

1. 触摸事件的处理涉及三个方法：dispatchTouchEvent()、onInterceptEvent()、onTouchEvent()
2. 从Activity的dispatch开始传递，如果没有拦截，则一直传递到子view。
3. 如果子View没有消费事件，事件会向上传递，这时父ViewGroup才可以消费事件。
4. 如果子View没有消费DOWN事件（没有返回 true），后续事件都不会再传递进来，直到下一次DOWN。
5. OnTouchListener的处理优先级高于onTouchEvent()

[图解 Android 事件分发机制](https://www.jianshu.com/p/e99b5e8bd67b)

# 序列化

## 原因

当两个进程在进行远程通信时，彼此可以发送各种类型的数据。无论是何种类型的数据，都会以二进制序列的形式在网络上传送。发送方需要把这个对象转换为字节序列，才能在网络上传送；接收方则需要把字节序列再恢复为对象。

把对象转换为字节序列的过程称为对象的序列化。

把字节序列恢复为对象的过程称为对象的反序列化。

说的再直接点，序列化的目的就是为了跨进程传递格式化数据

## Serializable和Parcelable的区别

1. S是Java的序列化方案，P是Android的。在使用内存的时候，Parcelable 类比Serializable性能高，所以推荐使用Parcelable类。
2. S在序列化的时候会产生大量的临时变量，导致频繁GC，P则不会。因此在内存中使用时（如网络中传递或进程间传递）推荐使用P。
3. P被设计为IPC通信数据序列化方案，不适用于保存在磁盘上，如果需要保存则应该用S。
4. S只需要继承Serializable接口即可，P则需要重写writeToParcel方法、重写describeContents方法、实例化Parcelable.Creator。

# startService和bindService的区别

1. startService启动的Service在调用者自己退出而没有调用stopService时会继续在后台运行。
2. bindService启动的Service生命周期会和调用者绑定，调用者退出时Service也会调用unBind()->onDestory()退出。
3. 先调用startService再调用bindService时Service也只会走一遍生命周期。
4. 除了startService和bindService，Service的生命周期只有三个方法：onCreate()、onStartCommand()、onDestroy()

# 线程与Looper

## Android系统是如何保证一个线程只有一个Looper的

Looper.prepare()使用了ThreadLocal来保证一个线程只有一个Looper

ThreadLocal实现了线程本地存储。所有线程共享同一个ThreadLocal对象，但不同线程仅能访问与其线程相关联的值，一个线程修改ThreadLocal对象对其他线程没有影响

可以将ThreadLocal理解为一块存储区，将这一大块存储区分割为多块小的存储区，每一个线程拥有一块属于自己的存储区，那么对自己的存储区操作就不会影响其他线程。对于ThreadLocal，则每一小块存储区中就保存了与特定线程关联的Looper

当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本

## Android中为什么主线程不会因为Looper.loop()方法造成阻塞

Android应用程序的主线程在进入消息循环过程前，会在内部创建一个Linux管道（Pipe），这个管道的作用是使得Android应用程序主线程在消息队列为空时可以进入空闲等待状态，并且使得当应用程序的消息队列有消息需要处理时唤醒应用程序的主线程

在代码ActivityThread.main()中

```java
public static void main(String[] args) {

  //创建Looper和MessageQueue对象，用于处理主线程的消息
  Looper.prepareMainLooper();

  //创建ActivityThread对象
  ActivityThread thread = new ActivityThread(); 

  //建立Binder通道 (创建新线程)
  thread.attach(false);

  Looper.loop(); //消息循环运行
  throw new RuntimeException("Main thread loop unexpectedly exited");
}
```

thread.attach(false) 会创建一个Binder线程（具体是指ApplicationThread，Binder的服务端，用于接收系统服务AMS发送来的事件），该Binder线程通过Handler将Message发送给主线程

ActivityThread实际上并非线程，不像HandlerThread类，ActivityThread并没有真正继承Thread类，只是往往运行在主线程，该人以线程的感觉，其实承载ActivityThread的主线程就是由Zygote fork而创建的进程。ActivityThread的内部类H继承于Handler，代码如下：

```java
public void handleMessage(Message msg) {
  if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
  switch (msg.what) {
    case LAUNCH_ACTIVITY:
      ...
    case RELAUNCH_ACTIVITY:
      ...
    case PAUSE_ACTIVITY:
      ...
    case PAUSE_ACTIVITY_FINISHING:
      ...
    case STOP_ACTIVITY_SHOW:
      ...
    case STOP_ACTIVITY_HIDE:
      ...
    case SHOW_WINDOW:
      ...
    case HIDE_WINDOW:
      ...
    case RESUME_ACTIVITY:
      ...
    case SEND_RESULT:
      ...
        
    ...
}
```

Activity的生命周期都是依靠主线程的Looper.loop，当收到不同Message时则采用相应措施：
在H.handleMessage(msg)方法中，根据App进程中的其他线程通过Handler发送给主线程的msg，执行相应的生命周期

[Android中为什么主线程不会因为Looper.loop()方法造成阻塞](http://blog.csdn.net/u013435893/article/details/50903082)

[Android中为什么主线程不会因为Looper.loop()里的死循环卡死](https://www.zhihu.com/question/34652589)

# ViewStub

1. ViewStub是一个轻量级的View，用于延迟加载布局和视图，避免资源的浪费，减少渲染时间
2. 不可见时不占布局位置，所占资源非常少。当可见时或调用ViewStub.inflate时它所指向的布局才会初始化
3. ViewStub只能被inflate一次
4. ViewStub只能用来inflate一个布局，不能inflate一个具体的View

# ANR

## 调试

1. DDMS输出的LOG可以判断ANR发生在哪个类，但无法确定在类中哪个位置
2. 在/data/anr/traces.txt文件中保存了ANR发生时的代码调用栈，可以跟踪到发生ANR的所有代码段
3. adb pull 来pull traces文件到电脑上

## 避免

任何在主线程中运行的，需要消耗大量时间的操作都会引发ANR

任何运行在主线程中的方法，都要尽可能的只做少量的工作。特别是活动生命周期中的重要方法如onCreate()和 onResume()等更应如此

耗时操作：

* 访问网络和数据库
* 开销很大的计算，比如改变位图的大小，需要在一个单独的子线程中完成

# Activity启动模式

```xml
<activity android:name=".xxActivity" android:launchMode="standard/singleTop/singleTask/singleInstance" android:taskAffinity="com.example.xxx.yyy"/>
```

## standard

默认的启动模式，每次启动一个Activity都会新建一个实例不管栈中是否已有该Activity的实例

## singleTop

1. 当前栈中已有该Activity的实例并且该实例位于栈顶时，不会新建实例，而是复用栈顶的实例，并且会将Intent对象传入，回调onNewIntent方法
2. 当前栈中已有该Activity的实例但是该实例不在栈顶时，其行为和standard启动模式一样，依然会创建一个新的实例
3. 当前栈中不存在该Activity的实例时，其行为同standard启动模式

> standard和singleTop启动模式都是在原任务栈中新建Activity实例，不会启动新的Task，即使你指定了taskAffinity属性

## singleTask

根据taskAffinity去寻找当前是否存在一个对应名字的任务栈

1. 如果不存在，则会创建一个新的Task，并创建新的Activity实例入栈到新创建的Task中去
2. 如果存在，则得到该任务栈，查找该任务栈中是否存在该Activity实例 
   * 如果存在实例，则将它上面的Activity实例都出栈，然后回调启动的Activity实例的onNewIntent方法 
   * 如果不存在该实例，则新建Activity，并入栈 

此外，我们可以将两个不同App中的Activity设置为相同的taskAffinity，这样虽然在不同的应用中，但是Activity会被分配到同一个Task中去

## singleInstance

除了具备singleTask模式的所有特性外，与它的区别就是，这种模式下的Activity会单独占用一个Task栈，具有全局唯一性，即整个系统中就这么一个实例，由于栈内复用的特性，后续的请求均不会创建新的Activity实例，除非这个特殊的任务栈被销毁了。以singleInstance模式启动的Activity在整个系统中是单例的，如果在启动这样的Activiyt时，已经存在了一个实例，那么会把它所在的任务调度到前台，重用这个实例

[彻底弄懂Activity四大启动模式](http://blog.csdn.net/mynameishuangshuai/article/details/51491074)

# Android进程间通信方法

## 文件共享

例如共同读写/sdcard目录下的某个文件

## Bundle

Bundle类是一个key-value对，两个activity之间的通讯可以通过bundle类来实现，通过Intent传递

```java
Intent intent = new Intent();    
intent.setClass(TestBundle.this, Target.class);    
Bundle mBundle = new Bundle();    
mBundle.putString("Data", "data from TestBundle"); 
intent.putExtras(mBundle);    
startActivity(intent);  
```

## AIDL

AIDL通过定义服务端暴露的接口，以提供给客户端来调用，AIDL使服务器可以并行处理，而Messenger封装了AIDL之后只能串行运行，所以Messenger一般用作消息传递

通过编写aidl文件来设计想要暴露的接口，编译后会自动生成响应的java文件，服务器将接口的具体实现写在Stub中，用iBinder对象传递给客户端，客户端bindService的时候，用asInterface的形式将iBinder还原成接口，再调用其中的方法

定义AIDL，新建一个`.aidl`文件

```java
package aidl;
interface IMyInterface {
  String getInfo(String s);
}
```

定义Service，用于接收并回复信息

```java
public class MyService extends Service { 
  public final static String TAG = "MyService";
  
  private IBinder binder = new IMyInterface.Stub() {
    @Override       
    public String getInfo(String s) throws RemoteException { 
      Log.i(TAG, s); 
      return "我是 Service 返回的字符串"; 
    }
  };
  
  @Override
  public void onCreate() {
    super.onCreate(); 
    Log.i(TAG, "onCreate");    
  }       
  
  @Override    
  public IBinder onBind(Intent intent) { 
    return binder;  
  }
}
```

定义`MyService`为一个新进程

```xml
<service
         android:name=".server.MyService"
         android:process="com.xxx.remote" />
```

定义Activity，用于发送消息和接收回复

```java
public class MainActivity extends AppCompatActivity {
  public final static String TAG = "MainActivity";
  private IMyInterface myInterface;
  
  private ServiceConnection serviceConnection = new ServiceConnection() {
    
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      myInterface = IMyInterface.Stub.asInterface(service);
      Log.i(TAG, "连接Service 成功");
      try {
        String s = myInterface.getInfo("我是Activity传来的字符串");
        Log.i(TAG, "从Service得到的字符串：" + s);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.e(TAG, "连接Service失败");
    }
  };
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    startAndBindService();
  }
  
  private void startAndBindService() {
    Intent service = new Intent(MainActivity.this, MyService.class);
    startService(service);
    bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
  }
}
```

[Android的进阶学习(四)--AIDL的使用与理解](https://www.jianshu.com/p/4e38cdc016c9)

## Messenger

Messager实现IPC通信，底层是使用了AIDL方式。和AIDL方式不同的是，Messager方式是利用Handler形式处理，因此，它是线程安全的，这也表示它不支持并发处理。相反，AIDL方式是非线程安全的，支持并发处理。服务端（被动方）提供一个Service来处理客户端（主动方）连接，维护一个Handler来创建Messenger，在onBind时返回Messenger的binder。双方用Messenger来发送数据，用Handler来处理数据。Messenger处理数据依靠Handler，所以是串行的，也就是说，Handler接到多个message时，就要排队依次处理

例如：在进程A中创建一个Message，将这个Message对象通过IMessenger.send(message)方法传递到进程B

> Message对象本身是无法被传递到进程B的，send(message)方法会使用一个Parcel对象对Message对象编集，再将Parcel对象传递到进程B中，然后解编集，得到一个和进程A中Message对象内容一样的对象），再把Message对象加入到进程B的消息队列里，Handler会去处理它

进程B（接收方）

```java
public class RemoteService extends Service {  
  public static final int GET_RESULT = 1;  
  private final Messenger mMessenger = new Messenger(new Handler() {  
    private int remoteInt = 1;//返回到进程A的值  
    @Override  
    public void handleMessage(Message msg) {  
      if (msg.what == GET_RESULT) {
        // 接收到来自A的消息，并回复remoteInt++的结果
        try {  
          msg.replyTo.send(Message.obtain(null, GET_RESULT, remoteInt++, 0));  
        } catch (RemoteException e) {  
          e.printStackTrace();  
        }  
      } else {  
        super.handleMessage(msg);  
      }  
    }  
  });  

  @Override  
  public IBinder onBind(Intent intent) {  
    return mMessenger.getBinder();  
  }  
}  
```

进程A（发送方）

```java
private Messenger mService;  
private boolean isBinding = false;  

private ServiceConnection mConnection = new ServiceConnection() {  
  @Override  
  public void onServiceConnected(ComponentName name, IBinder service) {  
    mService = new Messenger(service);  
    isBinding = true;  
  }  

  @Override public void onServiceDisconnected(ComponentName name) {  
    mService = null;  
    isBinding = false;  
  } 
};

private Messenger mMessenger = new Messenger(new Handler() {  
  @Override  
  public void handleMessage(Message msg) {
    // 处理B回复的消息
    if (msg.what == RemoteServiceProxy.GET_RESULT) {  
      Log.i("TAG", "Int form process B is " + msg.arg1);//msg.arg1就是remoteInt  
    } else {  
      super.handleMessage(msg);  
    }  
  }  
});
```

A绑定B的服务

```java
bindService(new Intent(this, RemoteService.class), mConnection, Context.BIND_AUTO_CREATE); 
```

A向B发送消息，并处理B回复的消息

```java
Message message = Message.obtain(null, RemoteServiceProxy.GET_RESULT);  
message.replyTo = mMessenger;  
try {  
  mService.send(message);  
} catch (RemoteException e) {  
  e.printStackTrace();  
}
```

[Android的进阶学习（五）--Messenger的使用和理解](https://www.jianshu.com/p/af8991c83fcb)

[Android IPC进程通信——Messager方式](http://blog.csdn.net/chenfeng0104/article/details/7010244)

## ContentProvider

系统四大组件之一，底层也是Binder实现，主要用来应用程序之间的数据共享，也就是说一个应用程序用ContentProvider将自己的数据暴露出来，其他应用程序通过ContentResolver来对其暴露出来的数据进行增删改查

Android内置的许多数据都是使用ContentProvider形式，供开发者调用的(如视频，音频，图片，通讯录等)

自定义的ContentProvider注册时要提供authorities属性，应用需要访问的时候将属性包装成Uri.parse("content://authorities")。还可以设置permission，readPermission，writePermission来设置权限。 ContentProvider有query，delete，insert等方法，看起来貌似是一个数据库管理类，但其实可以用文件，内存数据等等一切来充当数据源，query返回的是一个Cursor，可以自定义继承AbstractCursor的类来实现

[Android之ContentProvider详解](http://blog.csdn.net/x605940745/article/details/16118939)

## Socket

Android不允许在主线程中请求网络，而且请求网络必须要注意声明相应的permission。然后，在服务器中定义ServerSocket来监听端口，客户端使用Socket来请求端口，连通后就可以进行通信

[Android：这是一份很详细的Socket使用攻略](http://blog.csdn.net/carson_ho/article/details/53366856)

# Android线程间通信

## AsyncTask

相当于是Handler的封装

```java
class myAsync extends AsyncTask<String, Integer, String> {

  //下面这个方法在主线程中执行，在doInBackground函数执行前执行
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
  }

  //下面这个方法在子线程中执行，用来处理耗时行为
  @Override
  protected String doInBackground(String... arg0) {
    for (int i = 0; i < 20; i++) {
      try {
        Thread.sleep(1000); //暂停1秒，然后继续执行
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
      publishProgress(i);//实时更新当前进度
    }
    return "xxx";
  }

  //下面这个方法在主线程中执行，用于显示子线程任务执行的进度
  @Override
  protected void onProgressUpdate(Integer... values) {
    super.onProgressUpdate(values);
    tvTextView.setText(values[0]+"");
  }

  //下面这个方法在主线程中执行，在doinBackground方法执行完后执行
  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);
    tvTextView.setText(result);
  }
}
```

[AsyncTask 实现Android的线程通信](http://blog.csdn.net/qq_15267341/article/details/79056947)

## Handler&Message

### 原理

Handler 、 Looper 、Message 这三者都与Android异步消息处理线程相关的概念。Looper负责的就是创建一个MessageQueue，然后进入一个无限循环体不断从该MessageQueue中读取Message，而消息的创建者就是一个或多个Handler 

> 异步消息处理线程启动后会进入一个无限的循环体之中，每循环一次，从其内部的消息队列中取出一个消息，然后回调相应的消息处理函数，执行完成一个消息后则继续循环。若消息队列为空，线程则会阻塞等待。

### 主要过程

1. 首先Looper.prepare()在本线程中保存一个Looper实例，然后该实例中保存一个MessageQueue对象；因为Looper.prepare()在一个线程中只能调用一次，所以MessageQueue在一个线程中只会存在一个。
2. Looper.loop()会让当前线程进入一个无限循环，不断从MessageQueue的实例中读取消息，然后回调msg.target.dispatchMessage(msg)方法。
3. Handler的构造方法，会首先得到当前线程中保存的Looper实例，进而与Looper实例中的MessageQueue相关联。
4. Handler的sendMessage方法，会给msg的target赋值为handler自身，然后加入MessageQueue中。
5. 在构造Handler实例时，我们会重写handleMessage方法，也就是msg.target.dispatchMessage(msg)最终调用的方法。

好了，总结完成，大家可能还会问，那么在Activity中，我们并没有显示的调用Looper.prepare()和Looper.loop()方法，为啥Handler可以成功创建呢，这是因为在Activity的启动代码中，已经在当前UI线程调用了Looper.prepare()和Looper.loop()方法。

![](http://img.blog.csdn.net/20140805002935859?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbG1qNjIzNTY1Nzkx/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

[Android 异步消息处理机制 让你深入理解 Looper、Handler、Message三者关系](http://blog.csdn.net/lmj623565791/article/details/38377229)

### send和post的区别

1. post和sendMessage本质上是没有区别的，只是实际用法中有一点差别
2. post也没有独特的作用，post本质上还是用sendMessage实现的，post只是一中更方便的用法而已

## 共享内存

最简单的方式就是公共变量

# Activity生命周期

## 一个Activity的生命周期

![](http://hi.csdn.net/attachment/201109/1/0_1314838777He6C.gif)

完整生存期：onCreate() - onDestroy()，内存初始化和释放

可见生存期：onStart() - onStop()，活动可见，不一定可交互，资源加载和释放

前台生存期：onResume() - onPause()，运行状态，可交互

如果一个Activity没有被完全遮挡住，是不会触发onStop的

[基础总结篇之一：Activity生命周期](http://blog.csdn.net/liuhe688/article/details/6733407)

## 一个Activity调用另一个Activity的生命周期

1. A.onCreate()
2. A.onStart()
3. A.onResume()
4. 启动B
5. A.onPause()
6. B.onCreate()
7. B.onStart()
8. B.onResume()
9. A.onStop()
10. 返回A
11. B.onPause()
12. A.onRestart()
13. A.onStart()
14. A.onResume()
15. B.onStop()
16. B.onDestroy()

# 异常情况下Activity数据的保存和恢复

## 保存和恢复数据

可以通过onRestoreInstanceState和onCreate方法判读Activity是否被重建了，如果被重建了，那么我们就可以取出之前保存的数据并进行恢复，onRestoreInstanceState的调用时机在onStart之后。需要注意的是：在正常情况下Activity的创建和销毁不会调用onSaveInstanceState和onRestoreInstanceState方法

例如

```java
@Override
public void onSaveInstanceState(Bundle outState) {
  super.onSaveInstanceState(outState, outPersistentState);
  outState.putString("editText",myEdit.getText().toString());
}

@Override
public void onRestoreInstanceState(Bundle savedInstanceState) {
  super.onRestoreInstanceState(savedInstanceState, persistentState);
  String str = savedInstanceState.getString("editText");
  myEdit.setText(str);
}
```

## 防止Activity重建

在AndroidManifest.xml中对Activity的configChange属性进行配置。例如我们不希望屏幕旋转时重建，则需要设置为` android:configChanges="orientation"`

常用的配置选项还有

* orientation：屏幕方向发生了改变，例如横竖屏切换
* locale：设备的本地位置发生了改变，例如切换了系统语言
* keyboard：键盘类型发生了改变，例如插入了外接键盘
* keyboardHidden：键盘的可访问性发生了改变，例如移除了外接键盘

[异常情况下Activity数据的保存和恢复](http://blog.csdn.net/huaheshangxo/article/details/50829752#如何保存和恢复数据)

# ListView优化

1. convertView的复用

   在Adapter类的getView方法中通过判断convertView是否为null，是的话就需要在创建一个视图出来，然后给视图设置数据，最后将这个视图返回给底层，呈现给用户；如果不为null的话，其他新的view可以通过复用的方式使用已经消失的条目view，重新设置上数据然后展现出来

2. 使用内部类ViewHolder

   可以创建一个内部类ViewHolder，里面的成员变量和view中所包含的组件个数、类型相同，在convertview为null的时候，把findviewbyId找到的控件赋给ViewHolder中对应的变量，就相当于先把它们装进一个容器，下次要用的时候，直接从容器中获取

3. 分段分页加载

   分批加载大量数据，缓解一次性加载大量数据而导致OOM崩溃的情况

4. 减少变量的使用，减少逻辑判断和加载图片等耗时操作

   减少GC的执行，减少耗时操作造成的卡顿

```java
@Override
public View getView(int position, View convertView, ViewGroup parent) {
  ViewHolder holder;
  View itemView = null;
  if (convertView == null) {
    itemView = View.inflate(context, R.layout.item_news_data, null);
    holder = new ViewHolder(itemView);
    //用setTag的方法把ViewHolder与convertView "绑定"在一起
    itemView.setTag(holder);
  } else {
    //当不为null时，我们让itemView=converView，用getTag方法取出这个itemView对应的holder对象，就可以获取这个itemView对象中的组件
    itemView = convertView;
    holder = (ViewHolder) itemView.getTag();
  }

  NewsBean newsBean = newsListDatas.get(position);
  holder.tvNewsTitle.setText(newsBean.title);
  holder.tvNewsDate.setText(newsBean.pubdate);
  mBitmapUtils.display(holder.ivNewsIcon, newsBean.listimage);

  return itemView;
}

}

public class ViewHolder {
  @ViewInject(R.id.iv_item_news_icon)
  private ImageView ivNewsIcon;// 新闻图片
  @ViewInject(R.id.tv_item_news_title)
  private TextView tvNewsTitle;// 新闻标题
  @ViewInject(R.id.tv_item_news_pubdate)
  private TextView tvNewsDate;// 新闻发布时间
  @ViewInject(R.id.tv_comment_count)
  private TextView tvCommentIcon;// 新闻评论

  public ViewHolder(View itemView) {
    ViewUtils.inject(this, itemView);
  }
}
```

[ListView的四种优化方式](http://blog.csdn.net/xk632172748/article/details/51942479)

[Android性能优化之提高ListView性能的技巧](http://blog.csdn.net/xk632172748/article/details/51942479)

# okhttp

## 功能

* get,post请求
* 文件的上传下载
* 加载图片(内部会图片大小自动压缩)
* 支持请求回调，直接返回对象、对象集合
* 支持session的保持

## 优势

* 支持HTTP/2, HTTP/2通过使用多路复用技术在一个单独的TCP连接上支持并发, 通过在一个连接上一次性发送多个请求来发送或接收数据
* 如果HTTP/2不可用, 连接池复用技术也可以极大减少延时
* 支持GZIP, 可以压缩下载体积
* 响应缓存可以直接避免重复请求
* 会从很多常用的连接问题中自动恢复
* 如果您的服务器配置了多个IP地址, 当第一个IP连接失败的时候, OkHttp会自动尝试下一个IP
* OkHttp还处理了代理服务器问题和SSL握手失败问题

## 示例

```java
private final OkHttpClient client = new OkHttpClient();

public void run() throws Exception {
  Request request = new Request.Builder()
    .url("http://publicobject.com/helloworld.txt")
    .build();

  client.newCall(request).enqueue(new Callback() {
    @Override public void onFailure(Request request, Throwable throwable) {
      throwable.printStackTrace();
    }

    @Override public void onResponse(Response response) throws IOException {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      Headers responseHeaders = response.headers();
      for (int i = 0; i < responseHeaders.size(); i++) {
        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
      }

      System.out.println(response.body().string());
    }
  });
}
```

[OkHttp使用完全教程](https://www.jianshu.com/p/ca8a982a116b)

# App优化

常见的优化：App启动、布局、响应、内存、电池使用、网络

* 使用Systrace，Hierarchy Viewer，DDMS等性能分析工具进行分析
* 遵从16ms原则，避免过度绘制、频繁GC和阻塞网络请求等耗时操作造成卡顿或ANR
* 使用LeakCanary，MAT等工具避免内存泄露
* 通过优化网络请求，谨慎使用WakeLock，减少定位使用等方式减少电量使用
* 通过减少/压缩数据传输量，缓存响应，弱网不自动加载图片等方式优化网络请求

[Android App优化, 要怎么做?](https://www.jianshu.com/p/f7006ab64da7)

# View滑动冲突处理

* 确定冲突的有关控件
* 找准冲突发生的点
* 确定是用内部还是外部的方式

## 外部拦截

特点：子view代码无需修改，符合view事件分发机制

操作：需要在父ViewGroup，重写onInterceptTouchEvent方法，根据业务需要，判断哪些事件是父Viewgroup需要的，需要的话就对该事件进行拦截，然后交由onTouchEvent方法处理，若不需要，则不拦截，然后传递给子view或子viewGroup

```java
public boolean onInterceptTouchEvent(MotionEvent ev) {
  int y = (int) ev.getY();
  switch (ev.getAction()){
    case MotionEvent.ACTION_DOWN:
      yDown = y;
      isIntercept = false;
      break;
    case MotionEvent.ACTION_MOVE:
      yMove = y;
      if (yMove - yDown < 0){
        //根据业务需求更改判断条件，判断是时候需要拦截
        isIntercept = false;
      }else if(yMove - yDown > 0 && getChildAt(0).getScrollY() == 0){
        isIntercept = true;
      }else if(yMove - yDown > 0 && getChildAt(0).getScrollY() > 0){
        isIntercept = false;
      }
      break;
    case MotionEvent.ACTION_UP:
      isIntercept = false;
      break;
  }
  return isIntercept;         //返回true表示拦截，返回false表示不拦截
}
```

## 内部拦截

特点：父viewgroup需要重写onInterceptTouchEvent，不符合view事件分发机制

操作：在子view中拦截事件，父viewGroup默认是不拦截任何事件的，所以，当事件传递到子view时， 子view根据自己的实际情况来，如果该事件是需要子view来处理的，那么子view就自己消耗处理，如果该事件不需要由子view来处理，那么就调用getParent().requestDisallowInterceptTouchEvent()方法来通知父viewgroup来拦截这个事件，也就是说，叫父容器来处理这个事件，这刚好和view的分发机制相反

子View

```java
public boolean dispatchTouchEvent(MotionEvent ev) {
  int y = (int) ev.getY();
  switch (ev.getAction()) {
    case MotionEvent.ACTION_DOWN:
      getParent().requestDisallowInterceptTouchEvent(true);
      yDown = y;
      break;
    case MotionEvent.ACTION_MOVE:
      yMove = y;
      Log.e("mes", yMove + "！！！");
      int scrollY = getScrollY();
      if (scrollY == 0&&yMove-yDown>0) {
        //根据业务需求判断是否需要通知父viewgroup来拦截处理该事件
        //允许父View进行事件拦截
        Log.e("mes",yMove-yDown+"拦截");
        getParent().requestDisallowInterceptTouchEvent(false);
      }
      break;
    case MotionEvent.ACTION_UP:
      break;
  }
  return super.dispatchTouchEvent(ev);
}
```

父ViewGroup

```java
public boolean onInterceptTouchEvent(MotionEvent ev) {
  if (ev.getAction()==MotionEvent.ACTION_DOWN){
    return false;
  } else {
    return true;
  }
}
```

[View滑动冲突处理方法（外部拦截法、内部拦截法）](http://blog.csdn.net/z_l_p/article/details/53488085)

[Android事件冲突场景分析及一般解决思路](https://www.jianshu.com/p/c62fb2f25057)

# dp和px的关系

px = dp * (dpi / 160)，在每英寸160点的屏幕上，1dp = 1px

dp也就是dip:device independent pixels(设备独立像素)，是一种与密度无关的像素单位

px是像素，屏幕上的点

# Fragment

## 加载方式

* 依赖于Activity,不能单独存在，需要宿主
* 静态加载，在xml文件中，当做一个标签使用，这种方式频率比较低

```xml
<fragment
          android:id="@+id/left_fragment"
          android:name="com.example.fragmenttest.LeftFragment"
          android:layout_width="0dp"
          android:layout_height="match_parent"
          android:layout_weight="1"/>
```

* 动态加载，利用FragmentManager管理

```xml
<FrameLayout
             android:id="@+id/right_layout"
             android:layout_width="0dp"
             android:layout_height="match_parent"
             android:layout_weight="3">
</FrameLayout>
```

```java
void replaceFragment(Fragment fragment) {
  FragmentManager fragmentManager = getSupportFragmentManager();
  FragmentTransaction transaction = fragmentManager.beginTransaction();
  transaction.replace(R.id.right_layout, fragment);
  transaction.addToBackStack(null); // 实现返回栈
  transaction.commit();
}
```

## 生命周期

![](http://img.blog.csdn.net/20160712195637184)

## 常见问题

1. Fragment跟Activity如何传值

   使用`getActivity()`从Fragment获取Ativity的信息，就可以调用Ativity的方法了

2. FragmentPagerAdapter与FragmentStatePagerAdapter区别

   * FragmentPagerAdapter适合用于页面较少的情况，切换时不回收内存，只是把UI和Activity分离，页面少时对内存影响不明显
   * FragmentStatePagerAdapter适合用于页面较多的情况，切换时回收内存

3. Fragment的replace和add方法的区别

   * add的时候可以把Fragment 一层层添加到FrameLayout上面,而replace是删掉其他并替换掉最上层的fragment
   * 一个FrameLayout只能添加一个Fragment种类,多次添加会报异常,replace则随便替换 
   * 替换上一个fragment会->destroyView和destroy,新的Fragmetnon:三个Create(create+view+activity)->onStart->onResume)
   * 因FrameLayout容器对每个Fragment只能添加一次,所以达到隐藏效果可用fragment的hide和show方法结合

4. Fragment如何实现类似Activity的压栈和出栈效果`FragmentTransaction.addToBackStack`

   * 内部维持的是双向链表结构
   * 该结构可记录我们每次的add和replace我们的Fragment;
   * 当我们点击back按钮会自动帮我们实现退栈按钮

5. Fragment之间通信

   * 在fragment中调用activity中的方法getActivity();
   * 在Activity中调用Fragment中的方法，接口回调；
   * 在Fragment中调用Fragment中的方法findFragmentById/Tag

# 自定义View

## View的绘制过程

* Measure：View会先做一次测量，算出自己需要占用多大的面积。View的Measure过程给我们暴露了一个接口onMeasure

  ```java
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {}
  ```

* Layout：View给我们暴露了onLayout方法，用于调整布局

  ```java
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {}
  ```

* Draw：在画板canvas上画出我们需要的View样式

  ```java
  protected void onDraw(Canvas canvas) {}
  ```

## 自定义View

### 通常情况

大部分时候只需重写两个函数：onMeasure()、onDraw()。onMeasure负责对当前View的尺寸进行测量，onDraw负责把当前这个View绘制出来。

> 举个例子，比如我们希望我们的View是个正方形，如果在xml中指定宽高为`wrap_content`，如果使用View类提供的measure处理方式，显然无法满足我们的需求

### 3种方法

组合控件：将一些小的控件组合起来形成一个新的控件，这些小的控件多是系统自带的控件。比如很多应用中普遍使用的标题栏控件，其实用的就是组合控件

自绘控件：自绘控件的内容都是自己绘制出来的，在View的onDraw方法中完成绘制

继承控件：继承已有的控件，创建新控件，保留继承的父控件的特性，并且还可以引入新特性

## 自定义ViewGroup

### 操作过程

1. 首先得知道各个子View的大小，只有先知道子View的大小，我们才知道当前的ViewGroup该设置为多大去容纳它们
2. 根据子View的大小，以及我们的ViewGroup要实现的功能，决定出ViewGroup的大小
3. ViewGroup和子View的大小算出来了之后，接下来就是去摆放了吧，具体怎么去摆放得根据你定制的需求，比如，你想让子View按照垂直顺序一个挨着一个放，或者是按照先后顺序一个叠一个去放，这是你自己决定的
4. 决定了怎么摆放就是相当于把已有的空间"分割"成大大小小的空间，每个空间对应一个子View，我们接下来就是把子View对号入座了，把它们放进它们该放的地方去

### 实现自动换行的ViewGroup

效果图：![](http://www.jcodecraeer.com/uploads/allimg/130305/22540UU5-0.png)

自定义一个viewgroup,然后在onlayout文件里面自动检测view的右边缘的横坐标值，和你的view的parent view的况度判断是否换行显示view就可以了

```java
public class MyViewGroup extends ViewGroup { 
  private final static String TAG = "MyViewGroup"; 
  private final static int VIEW_MARGIN = 2; 
  public MyViewGroup(Context context) { 
    super(context); 
  } 

  @Override 
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
    Log.d(TAG, "widthMeasureSpec = " + widthMeasureSpec + " heightMeasureSpec" + heightMeasureSpec); 
    for (int index = 0; index < getChildCount(); index++) { 
      final View child = getChildAt(index); 
      // measure 
      child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED); 
    } 
    super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
  } 
  
  @Override 
  protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) { 
    Log.d(TAG, "changed = " + arg0 + " left = " + arg1 + " top = " + arg2 + " right = " + arg3 + " botom = " + arg4); 
    final int count = getChildCount(); 

    int row = 0;// which row lay you view relative to parent 
    int lengthX = arg1; // right position of child relative to parent 
    int lengthY = arg2; // bottom position of child relative to parent 

    for (int i = 0; i < count; i++) { 
      final View child = this.getChildAt(i); 

      int width = child.getMeasuredWidth(); 
      int height = child.getMeasuredHeight(); 

      lengthX += width + VIEW_MARGIN; 
      lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height 
        + arg2; 

      // if it can't drawing on a same line , skip to next line 
      if (lengthX > arg3) { 
        lengthX = width + VIEW_MARGIN + arg1; 
        row++; 
        lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height 
          + arg2; 
      } 

      child.layout(lengthX - width, lengthY - height, lengthX, lengthY); 
    } 
  } 
}
```

1. onMeasure() 在这个函数中，ViewGroup会接受childView的请求的大小，然后通过childView的 measure(newWidthMeasureSpec, heightMeasureSpec)函数存储到childView中，以便childView的getMeasuredWidth() andgetMeasuredHeight() 的值可以被后续工作得到。
2. onLayout() 在这个函数中，ViewGroup会拿到childView的getMeasuredWidth() andgetMeasuredHeight()，用来布局所有的childView。
3. View.MeasureSpec 与 LayoutParams 这两个类，是ViewGroup与childView协商大小用的。其中，View.MeasureSpec是ViewGroup用来部署 childView用的， LayoutParams是childView告诉ViewGroup 我需要多大的地方。
4. 在View 的onMeasure的最后要调用setMeasuredDimension()这个方法存储View的大小，这个方法决定了当前View的大小。

[教你搞定Android自定义View](https://www.jianshu.com/p/84cee705b0d3)

[Android自定义View的三种实现方式](https://www.cnblogs.com/jiayongji/p/5560806.html)

[自定义View，有这一篇就够了](https://www.jianshu.com/p/c84693096e41)

[Android 自动换行的LinearLayout](http://blog.csdn.net/sun_leilei/article/details/49740575)

[android之自定义ViewGroup实现自动换行布局](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2013/0305/969.html)

# JSON

## 基础结构

1. “名称/值”对的集合（A collection of name/value pairs）。不同的语言中，它被理解为对象（object），记录（record），结构（struct），字典（dictionary），哈希表（hash table），有键列表（keyed list），或者关联数组 （associative array）。
2. 值的有序列表（An ordered list of values）。在大部分语言中，它被理解为数组（array）。

例如

```json
{ "people": [
  { "firstName": "Brett", "lastName":"McLaughlin", "email": "aaaa" },
  { "firstName": "Jason", "lastName":"Hunter", "email": "bbbb"},
  { "firstName": "Elliotte", "lastName":"Harold", "email": "cccc" }
]}
```

# Binder通信机制

## 运行机制

Binder基于Client-Server通信模式，除了Client端和Server端，还有两角色一起合作完成进程间通信功能

* Client进程：使用服务的进程
* Server进程：提供服务的进程
* ServiceManager进程：ServiceManager的作用是将字符形式的Binder名字转化成Client中对该Binder的引用，使得Client能够通过Binder名字获得对Server中Binder实体的引用
* Binder驱动：驱动负责进程之间Binder通信的建立，Binder在进程之间的传递，Binder引用计数管理，数据包在进程之间的传递和交互等一系列底层支持。

![](https://upload-images.jianshu.io/upload_images/1685558-1754d79d2969841f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

Server进程向Service Manager进程注册服务（可访问的方法接口），Client进程通过Binder驱动可以访问到Server进程提供的服务。Binder驱动管理着Binder之间的数据传递，这个数据的具体格式由Binder协议定义（可以类比为网络传输的TCP协议）。并且Binder驱动持有每个Server在内核中的Binder实体，并给Client进程提供Binder的引用

## 线程管理

每个Binder的Server进程会创建很多线程来处理Binder请求，可以简单的理解为创建了一个Binder的线程池（虽然实际上并不完全是这样简单的线程管理方式），而真正管理这些线程并不是由这个Server端来管理的，而是由Binder驱动进行管理的。

一个进程的Binder线程数默认最大是16，超过的请求会被阻塞等待空闲的Binder线程。理解这一点的话，你做进程间通信时处理并发问题就会有一个底，比如使用ContentProvider时（又一个使用Binder机制的组件），你就很清楚它的CRUD（创建、检索、更新和删除）方法只能同时有16个线程在跑

[Android面试一天一题（Day 35：神秘的Binder机制）](https://www.jianshu.com/p/c7bcb4c96b38)

# Android内存泄露

## 原理

内存泄漏也称作“存储渗漏”，用动态存储分配函数动态开辟的空间，在使用完毕后未释放，结果导致一直占据该内存单元。直到程序结束。即所谓内存泄漏

简单地说就是申请了一块内存空间，使用完毕后没有释放掉。它的一般表现方式是程序运行时间越长，占用内存越多，最终用尽全部内存，整个系统崩溃。由程序申请的一块内存，且没有任何一个指针指向它，那么这块内存就泄露了

内存泄漏的堆积，这会最终消耗尽系统所有的内存。从这个角度来说，一次性内存泄漏并没有什么危害，因为它不会堆积，而隐式内存泄漏危害性则非常大，因为较之于常发性和偶发性内存泄漏它更难被检测到

## 常见内存泄露

1. 非静态内部类的静态实例容易造成内存泄漏

   ```java
   public class MainActivity extends Activity  
   {  
     static Demo sInstance = null;  

     @Override  
     public void onCreate(BundlesavedInstanceState)  
     {  
       super.onCreate(savedInstanceState);  
       setContentView(R.layout.activity_main);  
       if (sInstance == null)  
       {  
         sInstance= new Demo();  
       }  
     }  
     class Demo  
     {  
       void doSomething()  
       {  
         System.out.print("dosth.");  
       }  
     }  
   } 
   ```

   上面的代码中的sInstance实例类型为静态实例，在第一个MainActivity act1实例创建时，sInstance会获得并一直持有act1的引用。当MainAcitivity销毁后重建，因为sInstance持有act1的引用，所以act1是无法被GC回收的，进程中会存在2个MainActivity实例（act1和重建后的MainActivity实例），这个act1对象就是一个无用的但一直占用内存的对象，即无法回收的垃圾对象。所以，对于launchMode不是singleInstance的Activity， 应该避免在activity里面实例化其非静态内部类的静态实例

2. Activity使用静态成员

   ```java
   private static Drawable sBackground;    
   @Override    
   protected void onCreate(Bundle state) {    
     super.onCreate(state);    

     TextView label = new TextView(this);    
     label.setText("Leaks are bad");    

     if (sBackground == null) {    
       sBackground = getDrawable(R.drawable.large_bitmap);    
     }    
     label.setBackgroundDrawable(sBackground);    

     setContentView(label);    
   }   
   ```

   label .setBackgroundDrawable函数调用会将label赋值给sBackground的成员变量mCallback。上面代码意味着：sBackground（GC Root）会持有TextView对象，而TextView持有Activity对象。所以导致Activity对象无法被系统回收

3. 使用handler时的内存问题

   Handler通过发送Message与主线程交互，Message发出之后是存储在MessageQueue中的，有些Message也不是马上就被处理的。在Message中存在一个 target，是Handler的一个引用，如果Message在Queue中存在的时间过长，就会导致Handler无法被回收。如果Handler是非静态的，则会导致Activity或者Service不会被回收。 所以正确处理Handler等之类的内部类，应该将自己的Handler定义为静态内部类。

4. 注册某个对象后未反注册

   虽然有些系统程序，它本身好像是可以自动取消注册的(当然不及时)，但是我们还是应该在我们的程序中明确的取消注册，程序结束时应该把所有的注册都取消掉

   假设我们希望在锁屏界面(LockScreen)中，监听系统中的电话服务以获取一些信息(如信号强度等)，则可以在LockScreen中定义一个PhoneStateListener的对象，同时将它注册到TelephonyManager服务中。对于LockScreen对象，当需要显示锁屏界面的时候就会创建一个LockScreen对象，而当锁屏界面消失的时候LockScreen对象就会被释放掉。

   但是如果在释放LockScreen对象的时候忘记取消我们之前注册的PhoneStateListener对象，则会导致LockScreen无法被GC回收。如果不断的使锁屏界面显示和消失，则最终会由于大量的LockScreen对象没有办法被回收而引起OutOfMemory,使得system_process进程挂掉

5. 集合中对象没清理造成的内存泄露

   我们通常把一些对象的引用加入到了集合中，当我们不需要该对象时，如果没有把它的引用从集合中清理掉，这样这个集合就会越来越大。如果这个集合是static的话，那情况就更严重了

6. 资源对象没关闭造成的内存泄露

   资源性对象比如(Cursor，File文件等)往往都用了一些缓冲，我们在不使用的时候，应该及时关闭它们，以便它们的缓冲及时回收内存。它们的缓冲不仅存在于Java虚拟机内，还存在于Java虚拟机外。如果我们仅仅是把它的引用设置为null,而不关闭它们，往往会造成内存泄露

7. 一些不良代码成内存压力

   * 构造Adapter时，没有使用 convertView 重用 
   * Bitmap对象不再使用时，调用recycle()释放内存 
   * 对象被生命周期长的对象引用，如activity被静态集合引用导致activity不能释放

## 内存泄露的调试

1. 内存监测工具 DDMS --> Heap
2. 内存分析工具 MAT(Memory Analyzer Tool) 

[Android 内存泄露原理和检测](http://blog.csdn.net/gao878280390/article/details/55252732)

# Glide图片加载

## 准备

在app/build.gradle文件当中添加如下依赖

```
dependencies {  
compile 'com.github.bumptech.glide:glide:3.6.1'  
}
```

Glide中需要用到网络功能，因此还得在AndroidManifest.xml中声明一下网络权限

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 使用

### 基本用法

在layout中添加ImageView

```xml
<ImageView
           android:id="@+id/image_view"
           android:layout_width="match_parent"
           android:layout_height="match_parent" />
```

Context可以是Activity,Fragment等。它默认的Bitmap的格式RGB_565，同时他还可以指定图片大小；默认使用HttpUrlConnection下载图片，可以配置为OkHttp或者Volley下载，也可以自定义下载方式

```java
Glide.with(context)  
  .load("http://xxx.jpg")  
  .into(ImageView); 
```

* with()方法可以接收Context、Activity，Fragment类型或当前应用程序的ApplicationContext

> 如果传入的是Activity或者Fragment的实例，那么当这个Activity或Fragment被销毁的时候，图片加载也会停止。如果传入的是ApplicationContext，那么只有当应用程序被杀掉的时候，图片加载才会停止

* load()方法用于指定待加载的图片资源，包括网络图片、本地图片、应用资源、二进制流、Uri对象等
* into()方法接收ImageView类型的参数

### 缓存

Glide支持图片磁盘缓存，默认是内部存储。Glide默认缓存的是跟ImageView尺寸相同的。

缓存多种尺寸： `diskCacheStrategy(DiskCacheStrategy.ALL) `

这样不仅可以缓存ImageView大小尺寸还可以缓存其他尺寸。下次再加载ImageView的图片时，全尺寸的图片将会从缓存中取出，重新调整大小，然后再次缓存。这样加载图片显示会很快

禁用缓存： `diskCacheStrategy(DiskCacheStrategy.NONE)`

### 占位图

占位图就是指在图片的加载过程中，我们先显示一张临时的图片，等图片加载出来了再替换成要加载的图片

```java
Glide.with(this)
  .load(url)
  .placeholder(R.drawable.loading) // 加载过程中显示的图片
  .error(R.drawable.error) // 加载失败显示的图片
  .into(imageView);
```

### 指定图片格式和大小

`asBitmap()` 只允许加载静态图片

`asGif()` 只允许加载动态图片

`override(x, y)` 指定图片为x * y像素的尺寸

## 原理

Glide 收到加载及显示资源的任务，创建 Request 并将它交给RequestManager，Request 启动 Engine 去数据源获取资源(通过 Fetcher )，获取到后 Transformation 处理后交给 Target

![](http://www.trinea.cn/wp-content/uploads/2015/10/overall-design-glide.jpg?dc9529)

### 资源获取组件

* Model: 原始资源，比如Url，AndroidResourceId, File等
* Data: 中间资源，比如Stream，ParcelFileDescriptor等
* Resource：直接使用的资源，包括Bitmap，Drawable等

> ParcelFileDescriptor：ContentProvider共享文件时比较常用，其实就是操作系统的文件描述符的，里面有in out err三个取值。也有人说是链接建立好之后的句柄。

### 资源复用

Android的内存申请几乎都在new的时候发生，而new较大对象（比如Bitmap时），更加容易触发GC_FOR_ALLOW。所以Glide尽量的复用资源来防止不必要的GC_FOR_ALLOC引起卡顿。

LruResourceCache：第一次从网络或者磁盘上读取到Resource时，并不会保存到LruCache当中，当Resource被release时，也就是View不在需要此Resource时，才会进入LruCache当中

BitmapPool：Glide会尽量用图片池来获取到可以复用的图片，获取不到才会new，而当LruCache触发Evicted时会把从LruCache中淘汰下来的Bitmap回收，也会把transform时用到的中间Bitmap加以复用及回收

### 图片池

4.4以前是Bitmap复用必须长宽相等才可以复用
4.4及以后是Size>=所需就可以复用，只不过需要调用reconfigure来调整尺寸
Glide用AttributeStategy和SizeStrategy来实现两种策略
图片池在收到传来的Bitmap之后，通过长宽或者Size来从KeyPool中获取Key(对象复用到了极致，连Key都用到了Pool)，然后再每个Key对应一个双向链表结构来存储。每个Key下可能有很多个待用Bitmap
取出后要减少图片池中记录的当前Size等，并对Bitmap进行eraseColor(Color.TRANSPAENT)操作确保可用

### 加载流程

**with()**：调用单例RequestManagerRetriever的静态get()方法得到一个RequestManagerRetriever对象，负责管理当前context的所有Request

当传入Fragment、Activity时，当前页面对应的Activity的生命周期可以被RequestManager监控到，从而可以控制Request的pause、resume、clear。这其中采用的监控方法就是在当前activity中添加一个没有view的fragment，这样在activity发生onStart onStop onDestroy的时候，会触发此fragment的onStart onStop onDestroy

RequestManager用来跟踪众多当前页面的Request的是RequestTracker类，用弱引用来保存运行中的Request，用强引用来保存暂停需要恢复的Request

**load()**：创建需要的Request，Glide加载图片的执行单位

例如在加载图片url的RequestManager中，fromString()方法会返回一个DrawableTypeRequest对象，然后调用这个对象的load()方法，把图片的URL地址传进去

**into()**：调用Request的begin方法开始执行

> 如果并没有事先调用override(width, height)来指定所需要宽高，Glide则会尝试去获取imageview的宽和高，如果当前imageview并没有初始化完毕取不到高宽，Glide会通过view的ViewTreeObserver来等View初始化完毕之后再获取宽高再进行下一步

### 资源加载

* GlideBuilder在初始化Glide时，会生成一个执行机Engine，包含LruCache缓存及一个当前正在使用的active资源Cache（弱引用）
* activeCache辅助LruCache，当Resource从LruCache中取出使用时，会从LruCache中remove并进入acticeCache当中
* Cache优先级LruCache>activeCache
* Engine在初始化时要传入两个ExecutorService，即会有两个线程池，一个用来从DiskCache获取resource，另一个用来从Source中获取（通常是下载）
* 线程的封装单位是EngineJob，有两个顺序状态，先是CacheState，在此状态先进入DiskCacheService中执行获取，如果没找到则进入SourceState，进到SourceService中执行下载

[Glide加载图片原理----转载](http://blog.csdn.net/ss8860524/article/details/50668118)

[Android图片加载框架最全解析（一），Glide的基本用法](http://blog.csdn.net/guolin_blog/article/details/53759439)

[Android图片加载框架最全解析（二），从源码的角度理解Glide的执行流程](http://blog.csdn.net/guolin_blog/article/details/53939176)

# JNI和NDK

## 定义

JNI的全称是Java Native Interface（Java本地接口）是一层接口，是用来沟通Java代码和C/C++代码的，是Java和C/C++之间的桥梁。通过JNI，Java可以完成对外部C/C++编写的库函数的调用，相对的，外部C/C++也能调用Java中封装好的类和方法

NDK(Native Development Kit)是Android所提供的一个工具集合，通过NDL可以在Android更加方便地通过JNI来调用本地代码（C/C++）。NDK提供了交叉编译器，开发时只需要修改mk文件就能生成特定的CPU平台的动态库

## 原理

例如MediaRecorder: Java对应的是MediaRecorder.java，也就是我们应用开发中直接调用的类。JNI层对用的是libmedia_jni.so，它是一个JNI的动态库。Native层对应的是libmedia.so，这个动态库完成了实际的调用的功能

![](http://upload-images.jianshu.io/upload_images/1417629-6c97c443eb71c989.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 应用

实际中的驱动都是C/C++开发的,通过JNI,Java可以调用C开发好的驱动，从而扩展Java虚拟机的能力。另外，在高效率的数学运算、游戏的实时渲染、音视频的编码和解码等方面，一般都是用C开发的

## 一般步骤

在Java代码中中声明一个native方法

```java
public class TestHelloActivity extends Activity{
  public native String sayHello();
}
```

使用javah命令生成带有native方法的头文件

```shell
javah com.xxx.TestHelloActivity
```

> JDK1.7 需要在工程的src目录下执行上面的命令，JDK1.6 需要在工程的bin/classes目录下执行以上命令

创建JNI目录，并在jni目录中创建一个Hello.c文件，根据头文件实现C代码。写C代码时，结构体JNIEnv*对象对个别object对象很重要，在实现的C代码的方法中必须传入这两个参数

```c
jstring Java_com_xxx_TestHelloActivity_sayHello(JNIEnv* env,jobject obj){
  char* text = "hello from c!";
  return (**env).NewsStringUTF(env,text);
}
```

在JNI的目录下创建一个Android.mk文件,并根据需要编写里面的内容

```makefile
#LOCAL_PATH是所编译的C文件的根目录，右边的赋值代表根目录即为Android.mk所在的目录
LOCAL_PATH:=$(call my-dir)
#在使用NDK编译工具时对编译环境中所用到的全局变量清零
include $(CLEAR_VARS)
#最后声称库时的名字的一部分
LOCAL_MODULE:=hello
#要被编译的C文件的文件名
LOCAL_SRC_FILES:=Hello.c
#NDK编译时会生成一些共享库
include $(BUILD_SHARED_LIBRARY)
```

在工程的根目录下执行ndk_build命令，编译.so文件

在调用Native()方法前，加载.so的库文件

```java
System.loadLibrary("hello");
// 文件名个Android.mk文件中的LOCAL_MODULE属性指定的值相同
```

## 方法注册

静态注册多用于NDK开发，而动态注册多用于Framework开发

### 静态注册

编写Java文件

```java
package com.example;
public class MediaRecorder {
  static {
    System.loadLibrary("media_jni");
    native_init();
  }

  private static native final void native_init();
  public native void start() throws IllegalStateException;
}
```

接着进入项目的media/src/main/java目录中执行如下命令：

```shell
javac com.example.MediaRecorder.java
javah com.example.MediaRecorder
```

第二个命令会在当前目录中（media/src/main/java）生成com_example_MediaRecorder.h文件，如下所示。

```c
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_MediaRecorder */

#ifndef _Included_com_example_MediaRecorder
#define _Included_com_example_MediaRecorder
#ifdef __cplusplus
extern "C" {
  #endif
  /*
 * Class:     com_example_MediaRecorder
 * Method:    native_init
 * Signature: ()V
 */
  JNIEXPORT void JNICALL Java_com_example_MediaRecorder_native_1init
    (JNIEnv *, jclass);//1

  /*
 * Class:     com_example_MediaRecorder
 * Method:    start
 * Signature: ()V
 */
  JNIEXPORT void JNICALL Java_com_example_MediaRecorder_start
    (JNIEnv *, jobject);

  #ifdef __cplusplus
}
#endif
#endif
```

native_init方法被声明为注释1处的方法，格式为`Java_包名_类名_方法名`，注释1处的方法名多了一个“\_l”，这是因为native_init方法有一个“_”，它会在转换为JNI方法时变成“\_l”。 
其中JNIEnv * 是一个指向全部JNI方法的指针，该指针只在创建它的线程有效，不能跨线程传递。 
jclass是JNI的数据类型，对应Java的java.lang.Class实例。jobject同样也是JNI的数据类型，对应于Java的Object。

当我们在Java中调用native_init方法时，就会从JNI中寻找Java_com_example_MediaRecorder_native_1init方法，如果没有就会报错，如果找到就会为native_init和Java_com_example_MediaRecorder_native_1init建立关联，其实是保存JNI的方法指针，这样再次调用native_init方法时就会直接使用这个方法指针就可以了。 
静态注册就是根据方法名，将Java方法和JNI方法建立关联，但是它有一些缺点：

* JNI层的方法名称过长。
* 声明Native方法的类需要用javah生成头文件。
* 初次调用JIN方法时需要建立关联，影响效率。

静态方法就是根据函数名来建立Java函数和JNI函数之间的关联关系的，这里要求JNI层函数的名字必须遵循特定的格式

### 动态注册

JNI中有一种结构用来记录Java的Native方法和JNI方法的关联关系，它就是JNINativeMethod，它在jni.h中被定义

```c
typedef struct{  
  const char* name;  //Java中native函数的名字，不用携带包的路径，例如“native_init”  
  const char* signature; //Java函数的签名信息，用字符串表示，是参数类型和返回类型的组合，用以应对Java里的函数重载  
  void* fnPtr; //JNI层对应函数的函数指针，注意他是void*类型  
} JNINativeMethod  
```

首先定义一个JNINativeMethod数据，里面填写好了Java函数名和对应的输入输出和对应JNI的函数名，例如

```java
static const JNINativeMethod gMethods[] = {
  {"start",            "()V",      (void *)android_media_MediaRecorder_start},//1
  {"stop",             "()V",      (void *)android_media_MediaRecorder_stop}
};
```

其中注释1处start是Java层的Native方法，它对应的JNI层的方法为android_media_MediaRecorder_start。"()V"是start方法的签名信息

然后进行注册，尝试进行调用跟踪

```c
// frameworks/base/media/jni/android_media_MediaRecorder.cpp
jint JNI_OnLoad(JavaVM* vm, void* /* reserved */)
{
  JNIEnv* env = NULL;
  jint result = -1;
  if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    ALOGE("ERROR: GetEnv failed\n");
    goto *bail;
  }
  assert(env != NULL);
  ...
    if (register_android_media_MediaPlayer(env) < 0) {
      ALOGE("ERROR: MediaPlayer native registration failed\n");
      goto *bail;
    }
  if (register_android_media_MediaRecorder(env) < 0) {
    ALOGE("ERROR: MediaRecorder native registration failed\n");
    goto *bail;
  }
  ...
    result = JNI_VERSION_1_4;
  bail:
  return result;
}

int register_android_media_MediaRecorder(JNIEnv *env)
{
  return AndroidRuntime::registerNativeMethods(
    env,
    "android/media/MediaRecorder",
    gMethods, 
    NELEM(gMethods));
}
```

```c
// frameworks/base/core/jni/AndroidRuntime.cpp
/*static*/ int AndroidRuntime::registerNativeMethods(
  JNIEnv* env,
  const char* className, 
  const JNINativeMethod* gMethods, 
  int numMethods)
{
  return jniRegisterNativeMethods(env, className, gMethods, numMethods);
}
```

```c
// external/conscrypt/src/openjdk/native/JNIHelp.cpp
extern "C" int jniRegisterNativeMethods(
  JNIEnv* env, 
  const char* className,
  const JNINativeMethod* gMethods, 
  int numMethods)
{
  ...
    if (env->RegisterNatives(c.get(), gMethods, numMethods) < 0) {//1
      char* msg;
      (void)asprintf(&msg, 
                     "RegisterNatives failed for '%s'; aborting...", 
                     className);
      env->FatalError(msg);
    }
  return 0;
}
```

## JNIEnv

![img](https://upload-images.jianshu.io/upload_images/1952665-50f914b8ae912780.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/538)

JNIEnv是指向可用JNI函数表的接口指针，原生代码通过JNIEnv接口指针提供的各种函数来使用虚拟机的功能。JNIEnv是一个指向线程-局部数据的指针，而线程-局部数据中包含指向线程表的指针。实现原生方法的函数将JNIEnv接口指针作为它们的第一个参数。

## 数据类型转换

基本类型

| Java类型  | 别名       | C++本地类型        | 字节           |
| ------- | -------- | -------------- | ------------ |
| boolean | jboolean | unsigned char  | 8, unsigned  |
| byte    | jbyte    | signed char    | 8            |
| char    | jchar    | unsigned short | 16, unsigned |
| short   | jshort   | short          | 16           |
| int     | jint     | long           | 32           |
| long    | jlong    | __int64        | 64           |
| float   | jfloat   | float          | 32           |
| double  | jdouble  | double         | 64           |

引用类型

![img](https://upload-images.jianshu.io/upload_images/1952665-29614d7760b5f164.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

[Android面试题：对JNI和NDK的理解](http://blog.csdn.net/yyg_2015/article/details/72229892)

[Android深入理解JNI（一）JNI原理与静态、动态注册](http://blog.csdn.net/itachi85/article/details/73459880)

[JNIEnv结构体解析](https://www.jianshu.com/p/453b0463a84c)

# 安全

## Webview 远程执行JS漏洞

4.4以后增加了防御措施，如果用js调用本地代码，开发者必须在代码申明JavascriptInterface。如果代码无此申明，那么也就无法使得js生效，也就是说这样就可以避免恶意网页利用js对客户端的进行窃取和攻击

[Android中Java和JavaScript交互](https://www.imooc.com/article/1475)

## DNS劫持

DNS劫持俗称抓包。通过对url的二次劫持，修改参数和返回值，进而进行对app访问web数据伪装，实现注入广告和假数据，甚至起到导流用户的作用，严重的可以通过对登录APi的劫持可以获取用户密码，也可以对app升级做劫持，下载病毒apk等目的，解决方法一般用https进行传输数据

[Android OkHttp实现HttpDns的最佳实践（非拦截器）](http://blog.csdn.net/sbsujjbcy/article/details/51612832)

[阿里云HTTPDNS](https://www.aliyun.com/product/httpdns?spm=a2c4e.11153959.blogcont205501.17.5fc29e1bkmAHbZ)

## APP升级过程防劫持

### 问题概述

做app版本升级时一般流程是采用请求升级接口，如果有升级，服务端返回下一个下载地址，下载好Apk后，再点击安装

* 升级API：被劫持后返回错误的下载地址


* 下载API：返回恶意文件或者apk
* 安装过程：安装apk时本地文件path被篡改

### 解决方案

* 升级API：HTTPS，URL验证

  ```java
  UpgradeModel  aResult = xxxx;//解析服务器返回的后数据
  if (aResult != null && aResult.getData() != null ) {
    String url = aResult.getData().getDownUrl();
    if (url == null || !TextUtils.equals(url, "这里是你知道的下载地址： 也可以只验证hostUrl")) {
      // 如果符合，说明不是目标下载地址，就不去下载
    }
  }
  ```

* 下载API：HTTPS，文件Hash校验，签名key验证

  ```java
  File file = DownUtils.getFile(url);
  // 监测是否要重新下载
  if (file.exists() && TextUtils.equals(aResult.getData().getHashCode(), EncryptUtils.Md5File(file))) {
    && TextUtils.equals(aResult.getData().getKey(), DownLoadModel.getData()..getKey())
      // 如果符合，就去安装 不符合重新下载 删除恶意文件
  }
  ```

* 安装过程：安全检查，文件签名，包名校验

  ```java
  if (!SafetyUtils.checkFile(path + name, context)) {
    return;
  }

  if (!SafetyUtils.checkPagakgeName(context, path + name)) {
    Toast.makeText(context, "升级包被恶意软件篡改 请重新升级下载安装", Toast.LENGTH_SHORT ).show();
    DLUtils.deleteFile(path + name);
    ((Activity)context).finish();
    return;
  }

  switch (SafetyUtils.checkPagakgeSign(context, path + name)) {

    case SafetyUtils.SUCCESS:
      DLUtils.openFile(path + name, context);
      break;

    case SafetyUtils.SIGNATURES_INVALIDATE:
      Toast.makeText(context, "升级包安全校验失败 请重新升级", Toast.LENGTH_SHORT ).show();
      ((Activity)context).finish();
      break;

    case SafetyUtils.VERIFY_SIGNATURES_FAIL:
      Toast.makeText(context, "升级包为盗版应用 请重新升级", Toast.LENGTH_SHORT ).show();
      ((Activity)context).finish();
      break;

    default:
      break;
  }
  ```

[App安全（一） Android防止升级过程被劫持和换包](http://blog.csdn.net/sk719887916/article/details/52233112)

# SurfaceView

SurfaceView继承之View，但拥有独立的绘制表面，即它不与其宿主窗口共享同一个绘图表面，可以单独在一个线程进行绘制，并不会占用主线程的资源。这样，绘制就会比较高效，游戏，视频播放，还有最近热门的直播，都可以用SurfaceView

## 和View的区别

|       | SurfaceView  | View     |
| ----- | ------------ | -------- |
| 适用场景  | 被动更新，例如频繁地刷新 | 主动更新     |
| 刷新方式  | 子线程中刷新页面     | 主线程中刷新页面 |
| 双缓冲机制 | 底层实现         | 无        |

## 创建和初始化SurfaceView

创建一个自定义的SurfaceViewL，继承之SurfaceView，并实现两个接口SurfaceHolder.CallBack和Runnable

```java
public class SurfaceViewL extends SurfaceView implements SurfaceHolder.Callback,Runnable{
  // SurfaceHolder,控制SurfaceView的大小，格式，监控或者改变SurfaceView
  private SurfaceHolder mSurfaceHolder;
  // 画布
  private Canvas mCanvas;
  // 子线程标志位，用来控制子线程
  private boolean isDrawing;

  public SurfaceViewL(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {//创建
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {//改变
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {//销毁
  }

  @Override
  public void run() {
  }

  private void init() {
    mSurfaceHolder = getHolder();//得到SurfaceHolder对象
    mSurfaceHolder.addCallback(this);//注册SurfaceHolder
    setFocusable(true);
    setFocusableInTouchMode(true); // 能否获得焦点
    this.setKeepScreenOn(true);//保持屏幕长亮
  }
}
```

> SurfaceHolder.CallBack还有一个子Callback2接口，里面添加了一个surfaceRedrawNeeded (SurfaceHolder holder)方法，当需要重绘SurfaceView中的内容时，可以使用这个接口。

## 使用SurfaceView

利用mSurfaceHolder对象，通过lockCanvas()方法获得当前的Canvas

>  lockCanvas()获取到的Canvas对象还是上次的Canvas对象，并不是一个新的对象。之前的绘图都将被保留，如果需要擦除，可以在绘制之前通过drawColor()方法来进行清屏

绘制要充分利用SurfaceView的三个回调方法，在surfaceCreate()方法中开启子线程进行绘制。在子线程中，使用一个while(isDrawing)循环来不停地绘制。具体的绘制过程，由lockCanvas()方法进行绘制，并通过unlockCanvasAndPost(mCanvas)进行画布内容的提交

## 画图板示例

![img](https://upload-images.jianshu.io/upload_images/2086682-dfdd33500f31689e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/216)

```java
public class SurfaceViewL extends SurfaceView implements SurfaceHolder.Callback, Runnable {
  // SurfaceHolder
  private SurfaceHolder mSurfaceHolder;
  // 画布
  private Canvas mCanvas;
  // 子线程标志位
  private boolean isDrawing;
  // 画笔
  Paint mPaint;
  // 路径
  Path mPath;
  private float mLastX, mLastY;//上次的坐标

  public SurfaceViewL(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  /**
     * 初始化
     */
  private void init() {
    //初始化 SurfaceHolder mSurfaceHolder
    mSurfaceHolder = getHolder();
    mSurfaceHolder.addCallback(this);

    setFocusable(true);
    setFocusableInTouchMode(true);
    this.setKeepScreenOn(true);
    //画笔
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    mPaint.setStrokeWidth(10f);
    mPaint.setColor(Color.parseColor("#FF4081"));
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    //路径
    mPath = new Path();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {//创建
    Log.e("surfaceCreated","--"+isDrawing);
    drawing();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {//改变

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {//销毁
    isDrawing = false;
    Log.e("surfaceDestroyed","--"+isDrawing);
  }

  @Override
  public void run() {
    while (isDrawing) {
      drawing();
    }
  }

  /**
     * 绘制
     */
  private void drawing() {
    try {
      mCanvas = mSurfaceHolder.lockCanvas();
      mCanvas.drawColor(Color.WHITE);
      mCanvas.drawPath(mPath, mPaint);
    } finally {
      if (mCanvas != null) {
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
      }
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        isDrawing = true ;//每次开始将标记设置为ture
        new Thread(this).start();//开启线程
        mLastX = x;
        mLastY = y;
        mPath.moveTo(mLastX, mLastY);
        break;
        
      case MotionEvent.ACTION_MOVE:
        float dx = Math.abs(x - mLastX);
        float dy = Math.abs(y - mLastY);
        if (dx >= 3 || dy >= 3) {
          mPath.quadTo(mLastX, mLastY, (mLastX + x) / 2, (mLastY + y) / 2);
        }
        mLastX = x;
        mLastY = y;
        break;
        
      case MotionEvent.ACTION_UP:
        isDrawing = false;
        break;
    }
    return true;
  }

  /**
     * 测量
     */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
    int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);

    if (wSpecMode == MeasureSpec.AT_MOST && hSpecMode == MeasureSpec.AT_MOST) {
      setMeasuredDimension(300, 300);
    } else if (wSpecMode == MeasureSpec.AT_MOST) {
      setMeasuredDimension(300, hSpecSize);
    } else if (hSpecMode == MeasureSpec.AT_MOST) {
      setMeasuredDimension(wSpecSize, 300);
    }
  }
}
```

[Android SurfaceView入门学习](https://www.jianshu.com/p/15060fc9ef18)

# 图片加载器设计

## 基本功能

* 图片同步和异步加载
* 图片压缩
* 内存和磁盘缓存
* 网络拉取

## 详细设计

参见《Android开发艺术探索》12.2.3节

# 并发和并行的区别

并发是指一个处理器同时处理多个任务，是逻辑上的同时发生
并行是指多个处理器或者是多核的处理器同时处理多个不同的任务，是物理上的同时发生

# 进程和线程的区别

## 宏观认识

进程，是并发执行的程序在执行过程中分配和管理资源的基本单位，是一个动态概念，竟争计算机系统资源的基本单位。每一个进程都有一个自己的地址空间，即进程空间。进程空间的大小 只与处理机的位数有关

线程，是进程的一部分，一个没有线程的进程可以被看作是单线程的。线程有时又被称为轻权进程或轻量级进程，也是 CPU 调度的一个基本单位

## 区别

进程拥有一个完整的虚拟地址空间，不依赖于线程而独立存在；反之，线程是进程的一部分，没有自己的地址空间，与进程内的其他线程一起共享分配给该进程的所有资源

## Android中的进程与线程

**进程：**每个app运行时前首先创建一个进程，该进程是由Zygote fork出来的，用于承载App上运行的各种Activity/Service等组件。进程对于上层应用来说是完全透明的，这也是google有意为之，让App程序都是运行在Android Runtime。大多数情况一个App就运行在一个进程中，除非在AndroidManifest.xml中配置Android:process属性，或通过native代码fork进程

**线程：**线程对应用来说非常常见，比如每次new Thread().start都会创建一个新的线程。该线程与App所在进程之间资源共享，从Linux角度来说进程与线程除了是否共享资源外，并没有本质的区别，都是一个task_struct结构体，**在CPU看来进程或线程无非就是一段可执行的代码，CPU采用CFS调度算法，保证每个task都尽可能公平的享有CPU时间片**

# MVC

## 定义

MVC是一个架构模式，它分离了表现与交互。它被分为三个核心部件：模型、视图、控制器

![img](http://img0.tuicool.com/zAnI3q.jpg!web)

* 逻辑模型（M）：负责建立数据结构和相应的行为操作处理。
* 视图模型（V）：负责在屏幕上渲染出相应的图形信息展示给用户看。
* 控制器（C）：负责截获用户的按键和屏幕触摸等事件，协调Model对象和View对象

用户与视图交互，视图接受并反馈用户的动作；视图把用户的请求传给相应的控制器，由控制器决定调用哪个模型，然后由模型调用相应的业务逻辑对用户请求进行加工处理，如果需要返回数据，模型会把相应的数据返回给控制器，由控制器调用相应的视图，最终由视图格式化和渲染返回的数据，对于返回的数据完全可以增加用户体验效果展现给用户

**Android中最典型MVC是ListView，要显示的数据是Model，界面中的ListView是View，控制数据怎样在ListView中显示是Controller，即Adapter**

## 优势

* **耦合性低**：view和control分离，允许更改view，却不用修改model和control，很容易改变应用层的数据层和业务规则
* **可维护性**：分离view和control使得应用更容易维护和修改(分工明确，逻辑清晰)

## 控制流程

* 所有的终端用户请求被发送到控制器。
* 控制器依赖请求去选择加载哪个模型，并把模型附加到对应的视图。
* 附加了模型数据的最终视图做为响应发送给终端用户。

# MVP

在MVP里，Presenter完全把Model和View进行了分离，主要的程序逻辑在Presenter里实现。而且，Presenter与具体的View是没有直接关联的，而是通过定义好的接口进行交互，从而使得在变更View时候可以保持Presenter的不变，即重用！

作为一种新的模式，MVP与MVC有着一个重大的区别：在MVP中View并不直接使用Model，它们之间的通信是通过Presenter (MVC中的Controller)来进行的，所有的交互都发生在Presenter内部，而在MVC中View会直接从Model中读取数据而不是通过 Controller。

在MVC里，View是可以直接访问Model的！从而，View里会包含Model信息，不可避免的还要包括一些业务逻辑。 在MVC模型里，更关注的Model的不变，而同时有多个对Model的不同显示，即View。所以，在MVC模型里，Model不依赖于View，但是View是依赖于Model的。不仅如此，因为有一些业务逻辑在View里实现了，导致要更改View也是比较困难的，至少那些业务逻辑是无法重用的。

虽然 MVC 中的 View的确“可以”访问Model，但是我们不建议在 View 中依赖Model，而是要求尽可能把所有业务逻辑都放在 Controller 中处理，而 View 只和 Controller 交互

## 优势

1. 模型与视图完全分离，我们可以修改视图而不影响模型 
2. 可以更高效地使用模型，因为所有的交互都发生在一个地方：Presenter内部 
3. 我们可以将一个Presenter用于多个视图，而不需要改变Presenter的逻辑。这个特性非常的有用，因为视图的变化总是比模型的变化频繁。 
4. 如果我们把逻辑放在Presenter中，那么我们就可以脱离用户接口来测试这些逻辑（单元测试）

[MVC面试问题与答案](http://www.cnblogs.com/Hackson/p/7055695.html)

[每日一面试题--MVC思想是什么？](http://blog.csdn.net/qq_34986769/article/details/52594804)

# 断点续传

## 关键点

1. 终端知道当前的文件和上一次加载的文件是不是内容发生了变化，如果有变化，需要重新从offset 0 的位置开始下载
2. 终端记录好上次成功下载到的offset，告诉server端,server端支持从特定的offset 开始吐数据

## 原理

常规下载请求和响应

```
GET /down.zip HTTP/1.1 
Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms- 
excel, application/msword, application/vnd.ms-powerpoint, */* 
Accept-Language: zh-cn 
Accept-Encoding: gzip, deflate 
User-Agent: Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0) 
Connection: Keep-Alive 
```

```
200 
Content-Length=106786028 
Accept-Ranges=bytes 
Date=Mon, 30 Apr 2001 12:56:11 GMT 
ETag=W/"02ca57e173c11:95b" 
Content-Type=application/octet-stream 
Server=Microsoft-IIS/5.0 
Last-Modified=Mon, 30 Apr 2001 12:56:11 GMT 
```

断点续传请求和响应

```
GET /down.zip HTTP/1.0 
User-Agent: NetFox 
RANGE: bytes=2000070- 
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2 
```

```
206 
Content-Length=106786028 
Content-Range=bytes 2000070-106786027/106786028 
Date=Mon, 30 Apr 2001 12:55:20 GMT 
ETag=W/"02ca57e173c11:95b" 
Content-Type=application/octet-stream 
Server=Microsoft-IIS/5.0 
Last-Modified=Mon, 30 Apr 2001 12:55:20 GMT 
```

对比之下可以发现，断点续传的请求增加了`RANGE ` ，同时返回码变成了206，在Android中对应`HttpStatus.SC_PARTIAL_CONTENT` 。

## 实现

1. 通过数据库等方式记录已下载文件的长度

2. 设置下载位置

   ```java
   urlConnection.setRequestProperty("Range","bytes=" + start + "-");
   ```

3. 设置文件写入位置

   ```java
   File file = new File(DOWNLOAD_PATH,FILE_NAME);  
   RandomAccessFile randomFile = new RandomAccessFile(file, "rwd");  
   randomFile.seek(start); 
   ```

4. 判断响应条件

   ```java
   if (urlConnection.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
   ```

5. 写入文件并记录文件长度

   ```java
   inputStream = urlConnection.getInputStream();  
   byte[] buffer = new byte[512];  
   int len = -1;
   while ((len = inputStream.read(buffer))!= -1){   
     randomFile.write(buffer,0,len);
     // 记录文件长度信息
   }
   ```


[断点续传的原理](http://blog.csdn.net/lu1024188315/article/details/51803471)

[Android开发——断点续传原理以及实现](http://blog.csdn.net/SEU_Calvin/article/details/53749776)

# asset和resource的区别

1. 两者目录下的文件在打包后会原封不动的保存在apk包中，不会被编译成二进制

2. res/raw中的文件会被映射到R.Java文件中，访问的时候直接使用资源ID即R.id.filename；assets文件夹下的文件不会被映射到R.Java中，访问的时候需要AssetManager类

   ```java
   // 获得resource文件流
   InputStream is = getResources().openRawResource(R.id.filename);

   // 获得asset文件流
   AssetManager am = null;  
   am = getAssets();  
   InputStream is = am.open("filename");
   ```

3. res/raw不可以有目录结构，而assets则可以有目录结构，也就是assets目录下可以再建立文件夹

# Service

## 生命周期

![img](https://upload-images.jianshu.io/upload_images/944365-cf5c1a9d2dddaaca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/456)

## IntentService

### 使用原因

Android中的Service是用于后台服务的，当应用程序被挂到后台的时候，问了保证应用某些组件仍然可以工作而引入了Service这个概念，那么这里面要强调的是Service不是独立的进程，也不是独立的线程，它是依赖于应用程序的主线程的，也就是说，在更多时候不建议在Service中编写耗时的逻辑和操作，否则会引起ANR

那么我们当我们编写的耗时逻辑，不得不被service来管理的时候，就需要引入IntentService，IntentService是继承Service的，那么它包含了Service的全部特性，当然也包含service的生命周期，那么与service不同的是，IntentService在执行onCreate操作的时候，内部开了一个线程，去你执行你的耗时操作

### 原理

IntentService在执行onCreate的方法的时候，其实开了一个线程HandlerThread,并获得了当前线程队列管理的looper，并且在onStart的时候，把消息置入了消息队列

在消息被handler接受并且回调的时候，执行了onHandlerIntent方法，该方法的实现是子类去做的

[Android中IntentService与Service的区别](http://blog.csdn.net/matrix_xu/article/details/7974393)

# 动画

## 种类

1. View Animation（视图动画）

   视图动画，也叫Tween（补间）动画可以在一个视图容器内执行一系列简单变换（位置、大小、旋转、透明度）。譬如，如果你有一个TextView对象，您可以移动、旋转、缩放、透明度设置其文本，当然，如果它有一个背景图像，背景图像会随着文本变化。

2. Drawable Animation（Drawable动画）

   Drawable动画其实就是Frame动画（帧动画），它允许你实现像播放幻灯片一样的效果，这种动画的实质其实是Drawable，所以这种动画的XML定义方式文件一般放在res/drawable/目录下。

3. Property Animation（属性动画）

   属性动画，这个是在Android 3.0中才引进的，它可以直接更改我们对象的属性。在上面提到的Tween Animation中，只是更改View的绘画效果而View的真实属性是不改变的。假设你用Tween动画将一个Button从左边移到右边，无论你怎么点击移动后的Button，他都没有反应。而当你点击移动前Button的位置时才有反应，因为Button的位置属性木有改变。而Property Animation则可以直接改变View对象的属性值，这样可以让我们少做一些处理工作，提高效率与代码的可读性。