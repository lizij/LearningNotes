# Android开发面试题

[TOC]

# Activity

## 启动模式

```xml
<activity 
          android:name=".xxActivity" 
          android:launchMode="standard/singleTop/singleTask/singleInstance"
          android:taskAffinity="com.example.xxx.yyy"/>
```

### standard

默认的启动模式，每次启动一个Activity都会新建一个实例不管栈中是否已有该Activity的实例

### singleTop

1. 当前栈中已有该Activity的实例并且该实例位于栈顶时，不会新建实例，而是复用栈顶的实例，并且会将Intent对象传入，回调onNewIntent方法
2. 当前栈中已有该Activity的实例但是该实例不在栈顶时，其行为和standard启动模式一样，依然会创建一个新的实例
3. 当前栈中不存在该Activity的实例时，其行为同standard启动模式

> standard和singleTop启动模式都是在原任务栈中新建Activity实例，不会启动新的Task，即使你指定了taskAffinity属性

### singleTask

根据taskAffinity去寻找当前是否存在一个对应名字的任务栈

1. 如果不存在，则会创建一个新的Task，并创建新的Activity实例入栈到新创建的Task中去
2. 如果存在，则得到该任务栈，查找该任务栈中是否存在该Activity实例 
   * 如果存在实例，则将它上面的Activity实例都出栈，然后回调启动的Activity实例的onNewIntent方法 
   * 如果不存在该实例，则新建Activity，并入栈 

此外，我们可以将两个不同App中的Activity设置为相同的taskAffinity，这样虽然在不同的应用中，但是Activity会被分配到同一个Task中去

#### taskAffinity

#### 指定方式

1. 在Manifest中指定`android:taskAffinity` 属性
2. 在Intent中使用`addFlags（Intent.FLAG_ACTIVITY_xxx）`

第一种方式优先级低，无法指定`FLAG_ACTIVITY_CLEAR_TOP`等

第二种方式优先级高，无法指定singleInstance模式

> 可以使用`adb shell dumpsys activity` 查看任务栈

常见FLAG

* FLAG_ACTIVITY_NEW_TASK：同singleTask
* FLAG_ACTIVITY_CLEAR_TOP：将Activity上面的其他Activity实例都出栈，一般配合NEW_TASK使用
* FLAG_ACTIVITY_SINGLE_TOP：同singleTop
* FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS：Activity不会出现在历史Activity中，同`android:excludeFromRecents="true"`

#### 作用

taskAffinity和singleTask配对使用时，它是具有该模式的Activity的目前任务栈的名字，待启动的Activity会运行在名字和taskAffinity相同的任务栈中。

当taskAffinity和allowTaskReparenting结合使用时。当应用A启动了应用B的Activity C后，如果这个Activity的allowTaskReparenting为true，当（单独）启动应用B时，C会从A的任务栈转移到B的任务栈

### singleInstance

除了具备singleTask模式的所有特性外，与它的区别就是，这种模式下的Activity会单独占用一个Task栈，具有全局唯一性，即整个系统中就这么一个实例，由于栈内复用的特性，后续的请求均不会创建新的Activity实例，除非这个特殊的任务栈被销毁了。以singleInstance模式启动的Activity在整个系统中是单例的，如果在启动这样的Activiyt时，已经存在了一个实例，那么会把它所在的任务调度到前台，重用这个实例

[彻底弄懂Activity四大启动模式](http://blog.csdn.net/mynameishuangshuai/article/details/51491074)

## 生命周期

### 一个Activity的生命周期

![](http://hi.csdn.net/attachment/201109/1/0_1314838777He6C.gif)

完整生存期：onCreate() - onDestroy()，内存初始化和释放

可见生存期：onStart() - onStop()，活动可见，不一定可交互，资源加载和释放

前台生存期：onResume() - onPause()，运行状态，可交互

如果一个Activity没有被完全遮挡住，是不会触发onStop的

[基础总结篇之一：Activity生命周期](http://blog.csdn.net/liuhe688/article/details/6733407)

### 一个Activity调用另一个Activity的生命周期

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

### 不同位置调用finish()的结果

#### 表现

1. 在onCreate方法中调用finish

   在onCreate中，调用finish方法，不会显示出此Activity的界面，因为调用finish方法后，立马就会跑onDestroy。即跑的生命周期为：onCreate、onDestroy。

2. 在onStart方法中调用finish

   在onStart方法中，调用finish，会出现闪退，因为调用finish方法后，立马就会跑onStop方法。即跑的生命周期为：onCreate、onStart、onStop、onDestroy。


3. 在onResume方法中调用finish

   在onStart方法中，调用finish，会出现闪退，因为调用finish方法后，立马就会跑onStop方法。即跑的生命周期为：onCreate、onStart、onResume、onPause、onStop、onDestroy。


4. 在onPause、onStop、onDestroy中调用finish

   在onPause、onStop、onDestroy中，调用finish，显示正常。在退出时，正常退出。跑的生命周期为：onCreate、onStart、onResume、onPause、onStop、onDestroy。

#### 原理

在`mInstrumentation.callActivityOnCreate(activity, r.state)`中，执行完 onCreate()后，判断这时 activity 有没有finish ，没有就会接着执行 onStart()，否则会调用 destory()

```java
if (!r.activity.mFinished) {
    activity.performStart();
    r.stopped = false;
}
```

执行完 onStart()后会执行` handleResumeActivity` 函数，其中`performResumeActivity` 函数中会调用 onResume

```java
if (r != null && !r.activity.mFinished) {
    r.activity.performResume();
}
```

 如果此时finish，就不会执行finish()，会调用`ActivityManagerNative.getDefault().finishActivity(token, Activity.RESULT_CANCELED, null)`执行销毁

[android 生命周期不同方法调用finish()，经历生命周期方法不一样，为什么？](https://github.com/android-cn/android-discuss/issues/430)

[Activity的生命周期函数&finish方法](http://blog.csdn.net/hanhan1016/article/details/49991981)

## 异常情况下Activity数据的保存和恢复

### 保存和恢复数据

可以通过onRestoreInstanceState和onCreate方法判读Activity是否被重建了，如果被重建了，那么我们就可以取出之前保存的数据并进行恢复，onRestoreInstanceState的调用时机在onStart之后。

> 在正常情况下Activity的创建和销毁不会调用onSaveInstanceState和onRestoreInstanceState方法

使用

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

使用onRestoreInstanceState和onCreate恢复数据的区别：

1. onRestoreInstanceState的savedInstanceState一定是有值的，不需要额外判断
2. onCreate的savedInstanceState正常启动时为null，需要额外判断

### 防止Activity重建

在AndroidManifest.xml中对Activity的configChange属性进行配置。例如我们不希望屏幕旋转时重建，则需要设置为` android:configChanges="orientation"` ， 如果有多个值，可以用“|”连接

常用的配置选项还有

* orientation：屏幕方向发生了改变，例如横竖屏切换
* locale：设备的本地位置发生了改变，例如切换了系统语言
* keyboard：键盘类型发生了改变，例如插入了外接键盘
* keyboardHidden：键盘的可访问性发生了改变，例如移除了外接键盘

[异常情况下Activity数据的保存和恢复](http://blog.csdn.net/huaheshangxo/article/details/50829752#如何保存和恢复数据)

## 启动过程

启动Activity涉及到Instrumentation,ActivityThread,ActivityManagerService(AMS)

启动Activity的请求会由Instrumentation来处理，它通过Binder向AMS发送请求，AMS内部维护着一个ActivityStack并负责栈内的Activity的状态同步，AMS通过ActivityThread的`scheduleLaunchActivity`方法去同步Activity的状态从而完成生命周期方法的调用

### 具体过程记录（非重点）

1. **startActivity**的各种重载中最终都会调用startActivityForResult，其中调用了**mInstrumentation#execStartActivity**

2. execStartActivity中调用了ActivityManagerNative.getDefault().startActivity，相当于**AMS#startActivity**

   > 继承关系：ActivityManagerService->ActivityManagerNative->实现了IActivityManager的Binder

3. checkStartActivity，检查启动Activity的结果，无法正确启动时抛出异常

4. **AMS#startActivity**中，从ActivityStackSupervisor的startActivityMayWait开始转移，到starActivityLocked，startActivityUncheckedLocked，最终到**ActivityStack#resumeTopActivitiesLocked**

5. **ActivityStack#resumeTopActivitiesLocked**，resumeTopActivityInnerLocked，ActivityStackSuperVisor#startSpecificActivityLocked，**realStartActivityLocked**，其中调用了app.thread.scheduleLaunchActivity，即**ApplicationThread#scheduleLaunchActivity**

   > app.thread类型为IApplicationThread，继承自IInterface，是一个Binder接口，内部包含了大量启动停止Activity的接口，实现类为ApplicationThread

![Activity启动过程](images/activity启动过程1.png)

6. **ApplicationThread#scheduleLaunchActivity**发送启动消息到Handler H，调用ActivityThread#handleLaunchActivity，其中**performLaunchActivity**最终完成了：

   1. 从ActivityClientRecord中获取待启动的Activity组件信息
   2. 通过mInstrumentation的newActivity使用类加载器创建Activity对象
   3. 通过LoadedApk的makeApplication尝试创建Application对象
   4. 创建ContextImpl对象并通过Activity的attach完成重要数据初始化
   5. 调用Activity的onCreate

   > ContextImpl是Context的具体实现，通过Activity#attach与Activity建立联系，除此之外，attach还会建立Window并关联Activity

### Instrumentation

Instrumentation可以把测试包和目标测试应用加载到同一个进程中运行。既然各个控件和测试代码都运行在同一个进程中了，测试代码当然就可以调用这些控件的方法了，同时修改和验证这些控件的一些数据

Android Instrumentation是Android系统里面的一套控制方法或者”钩子“。这些钩子可以在正常的生命周期（正常是由操作系统控制的)之外控制Android控件的运行，其实指的就是Instrumentation类提供的各种流程控制方法

一般在开发Android程序的时候，需要写一个manifest文件，其结构是：

```xml
<application android:icon="@drawable/icon" android:label="@string/app_name">
    <activity 
              android:name=".TestApp" 
              android:label="@string/app_name">
    </activity>
</application>
```

这样，在启动程序的时候就会先启动一个Application，然后在此Application运行过程中根据情况加载相应的Activity，而Activity是需要一个界面的。

但是Instrumentation并不是这样的。可以将Instrumentation理解为一种没有图形界面的，具有启动能力的，用于监控其他类(用Target Package声明)的工具类。

## IntentFilter匹配规则

为了匹配过滤列表，需要同时匹配过滤列表中的action,category,data信息，否则匹配失败。另外，一个Activity中可以有多个intent-filter，一个Intent只要能匹配任何一组intent-filter即可成功启动对应的Activity

* action：要求Intent中的action存在且必须和过滤规则中的其中一个action相同，区分大小写

* category：要求Intent中如果含有category，那么所有的category都必须和过滤规则中的其中一个category相同才匹配

  intent中的category可以为空，因为在intent-filter中系统自动加上了DEFAULT这个category用来匹配空值

* data：要求Intent中必须含有data数据，并且data数据能够完全匹配过滤规则中的某一个data

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

## 本地拒绝服务漏洞

本地拒绝服务一般会导致正在运行的应用崩溃，首先影响用户体验，其次影响到后台的Crash统计数据，另外比较严重的后果是应用如果是系统级的软件，可能导致手机重启

### 原理

Android应用使用Intent机制在组件之间传递数据，如果应用在使用`getIntent()`，`getAction`，`intent.getXXXExtra()`获取到空、异常或畸形数据却没有进行异常捕获，应用就会发生Crash，从而拒绝服务。

漏洞片段存在的activity的export属性必须为true才能够被外部应用调用攻击。正常情况下，该属性默认为false，如果有intent-filter属性，则其对应activity的export属性默认为true

### 应用场景

1. NullPointerException异常导致的拒绝服务，源于程序没有对`getAction()`等获取到的数据进行空指针判断，从而导致空指针异常而导致应用崩溃
2. ClassCastException异常导致的拒绝服务, 源于程序没有对`getSerializableExtra()`等获取到的数据进行类型判断而进行强制类型转换，从而导致类型转换异常而导致应用崩溃
3. IndexOutOfBoundsException异常导致的拒绝服务，源于程序没有对`getIntegerArrayListExtra()`等获取到的数据数组元素大小的判断，从而导致数组访问越界而导致应用崩溃
4. ClassNotFoundException异常导致的拒绝服务，源于程序没有无法找到从getSerializableExtra ()获取到的序列化类对象的类定义，因此发生类未定义的异常而导致应用崩溃

### 漏洞检测

1. 首先我们要检测导出的组件有哪些（包含intent-filter属性的组件默认导出）

2. 然后我们使用空intent去检测这些组件，针对不同组件可发送如下命令：

   ```shell
   adb shell am start -n com.example.hello/.TestActivity  
   adb shell am startservice -n com.example.hello/.TestService  
   adb shell am broadcast -n com.example.hello/.TestReceiver  
   ```


3. 解析key值，空intent导致的拒绝服务只是一部分，还有类型转换异常，数组越界等，这些我们都需要找到其关键函数，检测其是否有异常保护。自动化测试工具在这里的难点是找到关键函数的key值，action值，以及key对应的类型等来组装命令进行攻击。
4. 通用型拒绝服务是由于应用中使用了getSerializableExtra()的API却没有进行异常保护，攻击者可以传入序列化数据，导致应用本地拒绝服务。此时不管传入的key值是否相同，都会抛出类未定义异常，相比前面需要解析key，自动化测试的通用性提高很多

### 修复

1. 将不必要导出的组件export属性设为false
2. 在处理intent数据时捕获异常。

# asset和resource

1. 两者目录下的文件在打包后会原封不动的保存在apk包中，不会被编译成二进制

2. res/raw中的文件会被映射到R.Java文件中，访问的时候直接使用资源ID即R.id.filename；

   assets文件夹下的文件不会被映射到R.Java中，访问的时候需要AssetManager类

   ```java
   // 获得resource文件流
   InputStream is = getResources().openRawResource(R.id.filename);

   // 获得asset文件流
   AssetManager am = null;  
   am = getAssets();  
   InputStream is = am.open("filename");
   ```

3. res/raw不可以有目录结构，而assets则可以有目录结构，也就是assets目录下可以再建立文件夹

# BroadcastReceiver

## 注册方式

静态注册

```xml
<receiver android:name=".Receiver" >  
    <intent-filter>  
        <action android:name="android.intent.action.BOOT_COMPLETED" />  
    </intent-filter>  
</receiver>  
```

动态注册

```java
MyBroadcastReceiver broadcast= new MyBroadcastReceiver();  
IntentFilter filter = new IntentFilter("android.intent.action.BOOT_COMPLETED");  
registerReceiver(broadcast, filter); 
```

> 动态注册的广播 永远要快于 静态注册的广播,不管静态注册的优先级设置的多高,不管动态注册的优先级有多低
>
> 动态注册广播不是 常驻型广播 ，也就是说广播跟随activity的生命周期。注意: 在activity结束前，移除广播接收器
>
> 静态注册是常驻型 ，也就是说当应用程序关闭后，如果有信息广播来，程序也会被系统调用自动运行

## 工作原理（非重点）

### 静态注册

由PackageManagerService（PMS）完成

### 动态注册

1. registerReceiver，ContextImpl.registerReceiver，registerReceiverInternal，其中通过mPackageInfo获取IIntentReceiver（Binder）对象，跨进程向AMS发送广播注册请求

   > BroadcastReceiver是Android组件，不能跨进程传递，需要通过IIntentReceiver中转，由LoadedApk.ReceiverDispatcher.InnerReceiver实现，与Service和ServiceDispatcher.InnerConnection的关系类似

2. AMS#registerReceiver中，将InnerReceiver和IntentFilter存储起来，就完成了注册

### 发送和接收

1. sendBroadcast，ContextImpl#sendBroadcast，AMS#broadcastIntent，broadcastIntentLocked

   > broadcastIntentLocked中默认不发送广播给已经停止的应用（包括手动停止的和安装后未启动过的）
   >
   > ```java
   > // By default broadcasts do not go to stopped apps
   > intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
   > ```
   >
   > 如果确实确实需要发送广播给未启动的应用，只需要添加`FLAG_INCLUDE_STOPPED_PACKAGES`，以覆盖另一个标签

2. `broadcastLocked`内，根据IntentFilter查找匹配的接收者，将接收者添加到BroadcastQueue中，再发送给接收者

3. BroadcastQueue并没有立即发送广播，而是发送了一个BROADCAST_INTENT_MSG的消息，收到后调用`processNextBroadcast`，遍历无序广播mParallelBroadcasts，并通过`deliverToRegisteredReceiverLocked`，`performReceiveLocked`发送给接收者

4. `performReceiveLocked`内，通过`ApplicationThread#scheduleRegisteredReceiver`调起应用程序，其中通过`InnerReceiver#performReceive`，`LoadedApk.ReceiverDispatcher#performReceive`实现广播接收，并调用`onReceive`

# Bitmap加载

## 高效加载

> 由于Bitmap的特殊性以及Android对单个应用所施加的内存限制，比如16MB，这导致加载Bitmap的时候容易出现内存溢出

核心思想就是采用BitmapFactory.Options来加载所需尺寸的图片

通过BitmapFactory.Options来缩放图片，主要用到了它的inSampleSize参数，即采样率。当其为1时不缩放，为2时，即采样后的图片其宽、高均为原图的1/2，而像素为原图的1/4，所以占的内存也为原图的1/4.（采样率小于1没效果，相当于1）

inSampleSize的取值应该总是为2的指数，如果不为2的指数，系统会向下取整并选择一个最近的2的指数来代替。比如3，系统会使用2来代替

**采样过程：** 

1. 将BitmapFactory.Options的inJustDecodeBounds参数设为true并加载图像 
2. 从BitmapFactory.Options中取出图片的原始宽高信息，他们对应于outWidth和outHeight参数 
3. 根据采样率的规则并结合目标View的所需大小计算出采样率inSampleSize. 
4. 将BitmapFactory.Options的inJustDecodeBounds参数设为false，然后重新加载图片。

> inJustDecodeBounds为true时，BitmapFactory只会解析图片的原始宽高信息，并不会真正去加载图片。

## 缓存策略

当程序第一次从网上加载图片后，就将其缓存到存储设备上，这样下次使用这张图片就不会再从网上下载了。

很多时候为了提高应用的用户体验，往往还会把图片在内存中也缓存一份，这样当应用打算从网络上请求一张图片时，程序会首先从内存中取获取，然后再从存储中获取，如果都没有最后才从网络下载。这样既提高了程序的效率又节约了不必要的流量开销。

在使用缓存时，要为其指定一个最大容量。当容量满了以后，采用LRU(Least Recently Used)近期最少使用算法来移除缓存内容

#### LruCache

内部采用LinkedHashMap以强引用的方式存储外界缓存对象，提供get和put方法完成缓存的获取和添加，缓存满时移除较早使用的缓存对象

创建时需要提供缓存的总容量大小并重写`sizeOf`，`sizeOf`用于计算缓存对象的大小，单位需要与总容量单位一致

```java
int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
int cacheSize = maxMemory / 8;
mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
    }
};
```

### DiskLruCache

[源码](https://link.jianshu.com/?t=https://android.googlesource.com/platform/libcore/+/android-4.1.1_r1/luni/src/main/java/libcore/io/DiskLruCache.java)

| 方法                                                         | 备注                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize) | 打开一个缓存目录，如果没有则首先创建它，**directory：**指定数据缓存地址 **appVersion：**APP版本号，当版本号改变时，缓存数据会被清除 **valueCount：**同一个key可以对应多少文件 **maxSize：**最大可以缓存的数据量 |
| Editor edit(String key)                                      | 通过key可以获得一个DiskLruCache.Editor，通过Editor可以得到一个输出流，进而缓存到本地存储上 |
| void flush()                                                 | 强制缓冲文件保存到文件系统                                   |
| Snapshot get(String key)                                     | 通过key值来获得一个Snapshot，如果Snapshot存在，则移动到LRU队列的头部来，通过Snapshot可以得到一个输入流InputStream |
| long size()                                                  | 缓存数据的大小，单位是byte                                   |
| boolean remove(String key)                                   | 根据key值来删除对应的数据，如果该数据正在被编辑，则不能删除  |
| void delete()                                                | 关闭缓存并且删除目录下所有的缓存数据，即使有的数据不是由DiskLruCache 缓存到本目录的 |
| void close()                                                 | 关闭DiskLruCache，缓存数据会保留在外存中                     |
| boolean isClosed()                                           | 判断DiskLruCache是否关闭，返回true表示已关闭                 |
| File getDirectory()                                          | 缓存数据的目录                                               |

## 优化列表卡顿

核心思想：不要在主线程中做太耗时的操作即可。

1. 不要再getView中执行耗时操作。必须使用异步的方式来处理。
2. 控制异步任务的执行频率。以照片墙为例，如果用户频繁的上下滑动，必定带来大量UI更新，所以解决思路就是：在滑动时停止加载图片，等列表停下来以后再加载图片。
3. 通过开启硬件加速解决卡顿。

## ImageLoader设计

### 基本功能

加载方式：同步和异步

缓存：内存和磁盘

图片压缩，网络拉取

### 代码设计

图片压缩

```java
public class ImageResizer {
    private static final String TAG = "ImageResizer";

    public ImageResizer() {
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // 根据Resource加载bitmap
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                                                     reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
        // 根据文件描述符加载bitmap
    }
}
```

缓存创建

```java
private LruCache<String, Bitmap> mMemoryCache;
private DiskLruCache mDiskLruCache;

private ImageLoader(Context context) {
    mContext = context.getApplicationContext();
    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    int cacheSize = maxMemory / 8;
    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    };
    File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
    if (!diskCacheDir.exists()) {
        diskCacheDir.mkdirs();
    }
    if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
        try {
            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
                                              DISK_CACHE_SIZE);
            mIsDiskLruCacheCreated = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
        mMemoryCache.put(key, bitmap);
    }
}

private Bitmap getBitmapFromMemCache(String key) {
    return mMemoryCache.get(key);
}
```

缓存添加和获取

* 通过DiskLruCache.Snapshot获得磁盘缓存对象对应的FileInputStream，但FileInputStream无法便捷地压缩，所以通过FileDescriptor加载压缩后图片

```java
private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        throw new RuntimeException("can not visit network from UI Thread.");
    }
    if (mDiskLruCache == null) {
        return null;
    }

    String key = hashKeyFormUrl(url);
    DiskLruCache.Editor editor = mDiskLruCache.edit(key);
    if (editor != null) {
        OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX); // 默认为0
        if (downloadUrlToStream(url, outputStream)) {
            editor.commit();
        } else {
            editor.abort();
        }
        mDiskLruCache.flush();
    }
    return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
}

private Bitmap loadBitmapFromDiskCache(String url, int reqWidth,
                                       int reqHeight) throws IOException {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Log.w(TAG, "load bitmap from UI Thread, it's not recommended!");
    }
    if (mDiskLruCache == null) {
        return null;
    }

    Bitmap bitmap = null;
    String key = hashKeyFormUrl(url);
    DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
    if (snapShot != null) {
        FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
        FileDescriptor fileDescriptor = fileInputStream.getFD();
        bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,
                                                                     reqWidth, reqHeight);
        if (bitmap != null) {
            addBitmapToMemoryCache(key, bitmap);
        }
    }

    return bitmap;
}
```

同步加载

* 从内存加载
* 从磁盘缓存加载
* 从网络拉取

```java
public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
    Bitmap bitmap = loadBitmapFromMemCache(uri);
    if (bitmap != null) {
        Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
        return bitmap;
    }

    try {
        bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromDisk,url:" + uri);
            return bitmap;
        }
        bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
        Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
    } catch (IOException e) {
        e.printStackTrace();
    }

    if (bitmap == null && !mIsDiskLruCacheCreated) {
        Log.w(TAG, "encounter error, DiskLruCache is not created.");
        bitmap = downloadBitmapFromUrl(uri);
    }

    return bitmap;
}
```

异步加载

* 从内存缓存读取图片
* 从线程池中调用loadBitmap，加载成功则返回LoaderResult对象，通过handler发送到主线程进行更新

```java
public void bindBitmap(final String uri, final ImageView imageView) {
    bindBitmap(uri, imageView, 0, 0);
}

public void bindBitmap(final String uri, final ImageView imageView,
                       final int reqWidth, final int reqHeight) {
    imageView.setTag(TAG_KEY_URI, uri);
    Bitmap bitmap = loadBitmapFromMemCache(uri);
    if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
        return;
    }

    Runnable loadBitmapTask = new Runnable() {

        @Override
        public void run() {
            Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
            if (bitmap != null) {
                LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
            }
        }
    };
    THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
}

private static class LoaderResult {
    public ImageView imageView;
    public String uri;
    public Bitmap bitmap;

    public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
        this.imageView = imageView;
        this.uri = uri;
        this.bitmap = bitmap;
    }
}

private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
        LoaderResult result = (LoaderResult) msg.obj;
        ImageView imageView = result.imageView;
        String uri = (String) imageView.getTag(TAG_KEY_URI);
        if (uri.equals(result.uri)) {
            imageView.setImageBitmap(result.bitmap);
        } else {
            Log.w(TAG, "set image bitmap,but url has changed, ignored!");
        }
    };
};
```

> 如果采用普通线程去加载图片，随着列表滑动可能产生大量线程，不利于整体效率提升
>
> AsyncTask在3.0以上不支持并发，同样不适合
>
> Handler直接采用主线程Looper，使得在非主线程中可以构造ImageLoader
>
> 通过检查url是否改变，如果改变则不设置图片，解决列表错位问题

使用

```java
public class MainActivity extends Activity implements OnScrollListener {
    private class ImageAdapter extends BaseAdapter {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.image_list_item,parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageView imageView = holder.imageView;
            final String tag = (String) imageView.getTag();
            final String uri = getItem(position);
            if (!uri.equals(tag)) {
                // url和tag不对应时加载默认图片
                imageView.setImageDrawable(mDefaultBitmapDrawable);
            }
            if (mIsGridViewIdle && mCanGetBitmapFromNetWork) {
                // 列表不滚动时设置tag，异步加载图片
                imageView.setTag(uri);
                mImageLoader.bindBitmap(uri, imageView, mImageWidth, mImageWidth);
            }
            return convertView;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            mIsGridViewIdle = true;
            mImageAdapter.notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }
}
```



# Binder

## 运行机制

Binder基于Client-Server通信模式，其中Client、Server和Service Manager运行在用户空间，Binder驱动程序运行内核空间

* Client进程：使用服务的进程
* Server进程：提供服务的进程
* ServiceManager进程：ServiceManager的作用是将**字符形式的Binder名字转化成Client中对该Binder的引用**，使得Client能够通过Binder名字获得对Server中Binder实体的引用
* Binder驱动：驱动负责进程之间Binder通信的建立，Binder在进程之间的传递，Binder引用计数管理，数据包在进程之间的传递和交互等一系列底层支持。

![](https://upload-images.jianshu.io/upload_images/1685558-1754d79d2969841f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

Server进程向Service Manager进程注册服务（可访问的方法接口），Client进程通过Binder驱动可以访问到Server进程提供的服务。Binder驱动管理着Binder之间的数据传递，这个数据的具体格式由Binder协议定义（可以类比为网络传输的TCP协议）。并且Binder驱动持有每个Server在内核中的Binder实体，并给Client进程提供Binder的引用

## 线程管理

每个Binder的Server进程会创建很多线程来处理Binder请求，可以简单的理解为创建了一个Binder的线程池（虽然实际上并不完全是这样简单的线程管理方式），而真正管理这些线程并不是由这个Server端来管理的，而是由Binder驱动进行管理的。

一个进程的Binder线程数默认最大是16，超过的请求会被阻塞等待空闲的Binder线程。理解这一点的话，你做进程间通信时处理并发问题就会有一个底，比如使用ContentProvider时（又一个使用Binder机制的组件），你就很清楚它的CRUD（创建、检索、更新和删除）方法只能同时有16个线程在跑

[Android面试一天一题（Day 35：神秘的Binder机制）](https://www.jianshu.com/p/c7bcb4c96b38)

# Crash

crash发生时，系统会回调`UncaughtExceptionHandler#uncaughtException`，其中可以获得异常信息，可以选择将异常信息存到本地或上传，通过对话框告知用户

```java
public class CrashHandler implements UncaughtExceptionHandler {
    private static CrashHandler sInstance = new CrashHandler();
    private UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            //导出异常信息到SD卡中
            dumpExceptionToSDCard(ex);
            uploadExceptionToServer();
            //这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug
        } catch (IOException e) {
            e.printStackTrace();
        }

        ex.printStackTrace();

        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }

    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
    }

    private void uploadExceptionToServer() {
        //TODO Upload Exception Message To Your Web Server
    }

}
```

在Application初始化时使用

```java
public class TestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //在这里为应用设置异常处理程序，然后我们的程序才能捕获未处理的异常
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }
}
```

# Drawable

一种可以在Canvas上进行绘制的抽象概念

* 使用简单，比自定义View成本低
* 非图片Drawable占用空间小，有利于减小apk大小

## 分类

### BitmapDrawable

表示图片

```xml
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
        android:src="@drawable/image1"
        android:tileMode="repeat"
        />
```

NinePatchDrawable：自动根据宽高进行相应缩放并保证不失真

### ShapeDrawable

通过颜色构造图形，纯色或渐变

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
       android:shape="rectangle" >

    <solid android:color="#ff0000" />

    <corners
             android:bottomLeftRadius="0dp"
             android:bottomRightRadius="15dp"
             android:topLeftRadius="10dp"
             android:topRightRadius="15dp" />

</shape>
```

### LayerDrawable

层次化的Drawable集合，通过将不同Drawable放在不同层达到叠加效果

下面的item会覆盖上面的item

```xml
<layer-list xmlns:android="http://schemas.android.com/apk/res/android" >
    <item>
        <shape android:shape="rectangle" >
            <solid android:color="#0ac39e" />
        </shape>
    </item>

    <item android:bottom="6dp">
        <shape android:shape="rectangle" >
            <solid android:color="#ffffff" />
        </shape>
    </item>

    <item
          android:bottom="1dp"
          android:left="1dp"
          android:right="1dp">
        <shape android:shape="rectangle" >
            <solid android:color="#ffffff" />
        </shape>
    </item>

</layer-list>
```

### StateListDrawable

根据View状态，从Drawable集合中顺序查找，选择匹配的显示

默认的item应放在最后，且不附带状态

```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">   
    <!-- 触摸时并且当前窗口处于交互状态 -->    
    <item android:state_pressed="true" android:state_window_focused="true" android:drawable= "@drawable/pic1" />  
    <!--  触摸时并且没有获得焦点状态 -->    
    <item android:state_pressed="true" android:state_focused="false" android:drawable="@drawable/pic2" />    
    <!--选中时的图片背景-->    
    <item android:state_selected="true" android:drawable="@drawable/pic3" />     
    <!--获得焦点时的图片背景-->    
    <item android:state_focused="true" android:drawable="@drawable/pic4" />    
    <!-- 窗口没有处于交互时的背景图片 -->    
    <item android:drawable="@drawable/pic5" />   
</selector>  
```

### LevelListDrawable

表示Drawable集合，每个Drawable都有一个level，根据不同level切换Drawable

```xml
<level-list xmlns:android="http://schemas.android.com/apk/res/android"> 
    <item
          android:drawable="@drawable/drawable_resource"
          android:maxLevel="integer"
          android:minLevel="integer"
          />
</level-list>
```

### TransitionDrawable

实现两个Drawable之间的淡入淡出

```xml
<transition xmlns:android="http://schemas.android.com/apk/res/android" >

    <item android:drawable="@drawable/shape_drawable_gradient_linear"/>
    <item android:drawable="@drawable/shape_drawable_gradient_radius"/>

</transition>
```

### InsetDrawable

将其他Drawable内嵌到自己中，并在四周留出一定间距

```xml
<inset xmlns:android="http://schemas.android.com/apk/res/android"
       android:insetBottom="15dp"
       android:insetLeft="15dp"
       android:insetRight="15dp"
       android:insetTop="15dp" >

    <shape android:shape="rectangle" >
        <solid android:color="#ff0000" />
    </shape>
</inset>
```

### ScaleDrawable

根据自己的等级将指定Drawable缩放一定比例

```xml
<scale xmlns:android="http://schemas.android.com/apk/res/android"
       android:drawable="@drawable/image1"
       android:scaleHeight="70%"
       android:scaleWidth="70%"
       android:scaleGravity="center" />
```

### ClipDrawable

根据自己的等级裁剪另一个Drawable

```xml
<clip xmlns:android="http://schemas.android.com/apk/res/android"
      android:clipOrientation="vertical"
      android:drawable="@drawable/image1"
      android:gravity="bottom" />
```

## 自定义Drawable

通过重写Drawable的draw方法自定义

```java
public class CustomDrawable extends Drawable {
    private Paint mPaint;

    public CustomDrawable(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect r = getBounds();
        float cx = r.exactCenterX();
        float cy = r.exactCenterY();
        canvas.drawCircle(cx, cy, Math.min(cx, cy), mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        // not sure, so be safe
        return PixelFormat.TRANSLUCENT;
    }

}
```

# 动态加载

## 基础性问题

### 资源访问

插件中以R开头的资源都不能访问，因为宿主程序中没有插件的资源

解决方法：

Activity通过ContextImpl管理，而Context中通过`getAssets`和`getResources`两个方法获取资源，只要覆写这两个方法就可以解决资源问题

```java
/** Return an AssetManager instance for your application's package. */
public abstract AssetManager getAssets();
/** Return a Resources instance for your application's package. */
public abstract Resources getResources();
```

```java
protected void loadResources() {  
    try {  
        AssetManager assetManager = AssetManager.class.newInstance();  
        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);  
        addAssetPath.invoke(assetManager, mDexPath);  
        mAssetManager = assetManager;  
    } catch (Exception e) {  
        e.printStackTrace();  
    }  
    Resources superRes = super.getResources();  
    mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),  
                               superRes.getConfiguration());  
    mTheme = mResources.newTheme();  
    mTheme.setTo(super.getTheme());  
}
```

```java
@Override  
public AssetManager getAssets() {  
    return mAssetManager == null ? super.getAssets() : mAssetManager;  
}  

@Override  
public Resources getResources() {  
    return mResources == null ? super.getResources() : mResources;  
}
```

> 加载的方法是通过反射，通过调用AssetManager中的addAssetPath方法，我们可以将一个apk中的资源加载到Resources中，由于addAssetPath是隐藏api我们无法直接调用，所以只能通过反射，下面是它的声明，通过注释我们可以看出，传递的路径可以是zip文件也可以是一个资源目录，而apk就是一个zip，所以直接将apk的路径传给它，资源就加载到AssetManager中了，然后再通过AssetManager来创建一个新的Resources对象，这个对象就是我们可以使用的apk中的资源了，这样我们的问题就解决了
>
> ```java
> /** 
>  * Add an additional set of assets to the asset manager.  This can be 
>  * either a directory or ZIP file.  Not for use by applications.  Returns 
>  * the cookie of the added asset, or 0 on failure. 
>  * {@hide} 
>  */  
> public final int addAssetPath(String path) {  
>     int res = addAssetPathNative(path);  
>     return res;  
> }
> ```

### Activity生命周期管理

#### 反射方式

获取Activity的各种生命周期方法，然后在代理Activity中调用插件Activity的生命周期方法

1. 代码复杂
2. 性能开销大
3. 由于apk中的activity不是真正意义上的activity（没有在宿主程序中注册且没有完全初始化），所以onCreate，onStart等这几个生命周期的方法系统就不会去自动调用了

> Fragment既有类似于Activity的生命周期，又有类似于View的界面，将Fragment加入到Activity中，activity会自动管理Fragment的生命周期，apk中的activity是通过宿主程序中的代理activity启动的，将Fragment加入到代理activity内部，其生命周期将完全由代理activity来管理，但是采用这种方法，就要求apk尽量采用Fragment来实现，还有就是在做页面跳转的时候有点麻烦

#### 接口方式

将activity的大部分生命周期方法提取出来作为一个接口（DLPlugin），然后通过代理activity（DLProxyActivity）去调用插件activity实现的生命周期方法，这样就完成了插件activity的生命周期管理，并且没有采用反射，当我们想增加一个新的生命周期方法的时候，只需要在接口中声明一下同时在代理activity中实现一下即可

```java
public interface DLPlugin {
    public void onStart();
    public void onRestart();
    public void onActivityResult(int requestCode, int resultCode, Intent data);
    public void onResume();
    public void onPause();
    public void onStop();
    public void onDestroy();
    public void onCreate(Bundle savedInstanceState);
    public void setProxy(Activity proxyActivity, String dexPath);
    public void onSaveInstanceState(Bundle outState);
    public void onNewIntent(Intent intent);
    public void onRestoreInstanceState(Bundle savedInstanceState);
    public boolean onTouchEvent(MotionEvent event);
    public boolean onKeyUp(int keyCode, KeyEvent event);
    public void onWindowAttributesChanged(LayoutParams params);
    public void onWindowFocusChanged(boolean hasFocus);
    public void onBackPressed();
    //...
}
```

```java
//...
@Override
protected void onStart() {
    mRemoteActivity.onStart();
    super.onStart();
}

@Override
protected void onRestart() {
    mRemoteActivity.onRestart();
    super.onRestart();
}

@Override
protected void onResume() {
    mRemoteActivity.onResume();
    super.onResume();
}

@Override
protected void onPause() {
    mRemoteActivity.onPause();
    super.onPause();
}

@Override
protected void onStop() {
    mRemoteActivity.onStop();
    super.onStop();
}
//...
```

### ClassLoader管理

将不同插件的ClassLoader存储在一个HashMap中，可以保证不同插件中的类彼此互不干扰，避免了多个ClassLoader加载同一个类时引起的类型转换问题

```java
public class DLClassLoader extends DexClassLoader {

    private static final String TAG = DLClassLoader.class.getSimpleName();

    private static final Map<String, DLClassLoader> mPluginClassLoaders = new ConcurrentHashMap<String, DLClassLoader>();

    protected DLClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    public static DLClassLoader getClassLoader(String dexPath, Context context, ClassLoader parentLoader) {
        Log.d(TAG, "DLClassLoader.getClassLoader(), dexPath=" + dexPath);
        DLClassLoader dLClassLoader = mPluginClassLoaders.get(dexPath);
        if (dLClassLoader != null) return dLClassLoader;

        File dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE);
        if (dexOutputDir == null || !dexOutputDir.exists()) {
            return null;
        }
        final String dexOutputPath = dexOutputDir.getAbsolutePath();
        dLClassLoader = new DLClassLoader(dexPath, dexOutputPath, null, parentLoader);
        mPluginClassLoaders.put(dexPath, dLClassLoader);

        return dLClassLoader;
    }
}
```



[DL : Apk动态加载框架](https://github.com/singwhatiwanna/dynamic-load-apk)

## 示例

实际项目中碰到的场景，需要远程下载class文件并使用自定义classloader加载

定义接口或抽象类，例如TestInterface

```java
public interface TestInterface {
    String test();
}
```

实现TestClass类并实现TestInterface，并编译成TestClass.class文件

```java
package com.example;
class TestClass implements TestInterface {
    @Override
    public String test() {
        return "test"
    }
}
```

```shell
javac com/example/TestClass.java # 生成TestClass.class
```

自定义的classloader

```java
public class WebClassLoader extends ClassLoader {

    private byte[] bclazz;

    public WebClassLoader(ClassLoader parent, byte[] bclazz){
        super(parent);
        this.bclazz = bclazz;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return defineClass(name, bclazz, 0, bclazz.length);
    }
}
```

通过网络下载class文件后，直接读取字节流并保存为`byte[]`

```java
protected byte[] module = null;
public synchronized int getModule(String url){
    try {
        if (module == null) {
            module = httpsRequest.doGet(url);
        }
    } catch (Exception e) {
        e.printStackTrace();
        return -1;
    }
    return 0;
}
```

使用反射或接口调用class中的方法

```java
WebClassLoader loader = new WebClassLoader(MyApplication.getContext().getClassLoader(), module);
Class clazz = loader.loadClass("com.example.TestClass");

// 使用接口调用方法
TestInterface ti = clazz.newInstance();
ti.test();

// 或者使用反射调用方法
Object o = clazz.newInstance();
Method m = clazz.getDeclaredMethod("test");
result = (String) m.invoke(o);
```

CLassLoader类中loadClass的具体实现

1. 查看请求的类装载器是否已经被装载进这个类装载器的命名空间。如果确实如此，返回这个已经装载的Class实例。
2. 否则调用委托机制，如果父类加载成功，则返回这个Class实例。
3. 否则，调用findClass()，findClass会试图寻找或生成一个字节数组。如果成功，findClass()把字节数组传递给defineClass，后者试着导入这个类型，返回一个Class实例。如果findClass返回了一个Class实例，loadClass()把这个实例返回。
4. 否则，findClass抛出某些异常，loadClass返回同样异常

[在运行时刻从文件中调入Class(defineClass 的使用)](https://blog.csdn.net/u013344397/article/details/53002240)

# 动画

## View动画

通过对场景的对象不断做图像变换（平移，缩放，旋转，透明度）从而产生动画效果

**只是在视图层实现了动画效果，并没有真正改变View的属性。view的实际位置还是移动前的位置**

### 种类

| 名称   | 标签           | 子类                 | 效果        |
| ---- | ------------ | ------------------ | --------- |
| 平移   | \<translate> | TranslateAnimation | 移动View    |
| 缩放   | \<scale>     | ScaleAnimation     | 放大或缩小View |
| 透明度  | \<alpha>     | AlphaAnimation     | 改变View透明度 |
| 旋转   | \<rotate>    | RotateAnimation    | 旋转View    |

### 使用

#### 使用xml定义

```xml
<set xmlns:android="http://schemas.android.com/apk/res/android"
     android:duration="300"
     android:interpolator="@android:anim/accelerate_interpolator"
     android:shareInterpolator="true" >

    <alpha
           android:fromAlpha="0.0"
           android:toAlpha="1.0" />

    <translate
               android:fromXDelta="500"
               android:toXDelta="0" />

</set>
```

```java
Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_item);
mButton.startAnimation(animation);
```

#### 使用代码定义

```java
AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
alphaAnimation.setDuration(300);
mButton.startAnimation(alphaAnimation);
```

### 自定义View动画

继承Animation类并重写initialize和applyTransformation方法

initialize中进行初始化工作

```java
@Override
public void initialize(int width, int height, int parentWidth, int parentHeight) {
    super.initialize(width, height, parentWidth, parentHeight);
}
```

applyTransformation中进行相应矩阵变换

```java
protected void applyTransformation(float interpolatedTime, Transformation t) {  
    super.applyTransformation(interpolatedTime, t);  
}  
```

应用

```java
Rotate3dAnimation rotate3dAnimation = new Rotate3dAnimation(0, 360, iv_content.getWidth()/2, 0, 0, true, Rotate3dAnimation.DIRECTION.Y);  
rotate3dAnimation.setDuration(3000);  
iv_content.setAnimation(rotate3dAnimation);  
rotate3dAnimation.start();  
```

### 其他场景

#### LayoutAnimation

为ViewGroup指定一个动画，当它的子元素出现时都会有这种动画效果，常用于ListView

使用xml指定

```xml
<layoutAnimation
                 xmlns:android="http://schemas.android.com/apk/res/android"
                 android:delay="0.5"
                 android:animationOrder="reverse"
                 android:animation="@anim/anim_item"/>
```

```xml
<ListView
          android:id="@+id/list"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:layoutAnimation="@anim/anim_layout" />
```

或使用代码指定

```java
Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_item);
LayoutAnimationController controller = new LayoutAnimationController(animation);
controller.setDelay(0.5f);
controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
listView.setLayoutAnimation(controller);
```

#### 修改Activity切换效果

在startActivity或finish后调用，同样适用于Fragment

```java
Intent intent = new Intent(this, TestActivity.class);
startActivity(intent);
overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
```

## 帧动画

通过顺序播放一系列图像产生动画效果，容易引起OOM，应尽量避免使用过多大尺寸图片

通过XML定义一个AnimationDrawable

```xml
<!--res/drawable/frame_animation.xml-->
<animation-list xmlns:android="http://schemas.android.com/apk/res/android"
                android:oneshot="false" >

    <item
          android:drawable="@drawable/light01"
          android:duration="50"/>
    <item
          android:drawable="@drawable/light02"
          android:duration="50"/>
    <item
          android:drawable="@drawable/light03"
          android:duration="50"/>

</animation-list>
```

将上述Drawable作为View的背景并通过Drawable来播放

```java
mButton.setBackgroundResource(R.drawable.frame_animation)
Animation drawable = (AnimationDrawable) mButton.getBackground();  
//开始动画
drawable.start();  
```

## 属性动画

动态改变对象属性达到动画效果

默认时间300ms，10ms/帧，在一个时间间隔内完成对象从一个属性值到另一个属性值的改变

在API 11之前的系统上可以使用nineoldandroids实现属性动画

### 原理

对object的属性abc做动画，需要满足以下条件

1. object必须提供setAbc方法，如果动画时没有传递初始值，还要提供getAbc方法，因为系统要去取abc属性的初始值，否则crash
2. object的setAbc对属性abc所做的改变必须能够通过某种方法反映出来，比如会带来UI改变，程序不一定crash但会无效果

属性动画要求object提供set和get方法，根据提供的初始值和最终值，以动画的效果多次调用set方法

### 使用

#### 使用代码定义

常用的动画类：ValueAnimator，ObjectAnimator，AnimatorSet

改变myObject的tranlationY属性，让其沿Y轴向上平移一段距离

```java
ObjectAnimator.ofFloat(myObject, "translationY", -myObject.getHeight()).start();
```

改变背景色

```java
ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", 0xFFFF8080, 0xFF8080FF);
colorAnim.setDuration(3000);
colorAnim.setEvaluator(new ArgbEvaluator());
colorAnim.setRepeatCount(ValueAnimator.INFINITE);
colorAnim.setRepeatMode(ValueAnimator.REVERSE);
colorAnim.start();
```

动画集合

```java
AnimatorSet set = new Animator();
set.playTogether(
    ObjectAnimator.ofFloat(myView, "rotationX", 0, 360),
    ObjectAnimator.ofFloat(myView, "translationX", 0, 90)
);
set.setDuration(5 * 1000).start();
```

#### 使用XML定义

定义在res/animator/下

```xml
<set xmlns:android="http://schemas.android.com/apk/res/android"
     android:ordering="sequentially" >
    <!-- 
  表示Set集合内的动画按顺序进行
  ordering的属性值:sequentially & together
  sequentially:表示set中的动画，按照先后顺序逐步进行（a 完成之后进行 b ）
  together:表示set中的动画，在同一时间同时进行,为默认值-->

    <set android:ordering="together" >
        <!--下面的动画同时进行-->
        <objectAnimator
                        android:duration="2000"
                        android:propertyName="translationX"
                        android:valueFrom="0"
                        android:valueTo="300"
                        android:valueType="floatType" >
        </objectAnimator>

        <objectAnimator
                        android:duration="3000"
                        android:propertyName="rotation"
                        android:valueFrom="0"
                        android:valueTo="360"
                        android:valueType="floatType" >
        </objectAnimator>
    </set>

    <set android:ordering="sequentially" >
        <!--下面的动画按序进行-->
        <objectAnimator
                        android:duration="1500"
                        android:propertyName="alpha"
                        android:valueFrom="1"
                        android:valueTo="0"
                        android:valueType="floatType" >
        </objectAnimator>
        <objectAnimator
                        android:duration="1500"
                        android:propertyName="alpha"
                        android:valueFrom="0"
                        android:valueTo="1"
                        android:valueType="floatType" >
        </objectAnimator>
    </set>

</set>
```

### 监听器

AnimatorListener可以监听动画的开始，结束，取消以及重复播放

AnimatorUpdateListener可以监听整个动画过程，每播放一帧就会被调用一次、

### 对任意属性做动画

1. 给对象加上set和get方法

   通常没有权限修改SDK，不可行

2. 用一个类包装对象，提供set和get方法

   ```java
   private static class ViewWrapper {
       private View mTarget;
       public ViewWrapper(View target) {
           mTarget = target;
       }

       public int getWidth() {
           return mTarget.getLayoutParams().width;
       }

       public void setWidth(int width) {
           mTarget.getLayoutParams().width = width;
           mTarget.requestLayout();
       }
   }
   ```

3. 采用ValueAnimator，监听动画过程，实现属性改变

   ```java
   private void performAnimate(final View target, final int start, final int end) {
       ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
       valueAnimator.addUpdateListener(new AnimatorUpdateListener() {

           // 持有一个IntEvaluator对象，方便下面估值的时候使用
           private IntEvaluator mEvaluator = new IntEvaluator();

           @Override
           public void onAnimationUpdate(ValueAnimator animator) {
               // 获得当前动画的进度值，整型，1-100之间
               int currentValue = (Integer) animator.getAnimatedValue();
               Log.d(TAG, "current value: " + currentValue);

               // 获得当前进度占整个动画过程的比例，浮点型，0-1之间
               float fraction = animator.getAnimatedFraction();
               // 直接调用整型估值器通过比例计算出宽度，然后再设给Button
               target.getLayoutParams().width = mEvaluator.evaluate(fraction, start, end);
               target.requestLayout();
           }
       });

       valueAnimator.setDuration(5000).start();
   }

   @Override
   public void onClick(View v) {
       if (v == button) {
           performAnimate(button, button.getWidth(), 500);
       }
   }
   ```

## 常见问题

1. OOM：主要出现在帧动画中，图片数量较多且尺寸较大时一出现
2. 内存泄漏：如果使用无限循环的动画需要在Activity退出时及时停止，否则将导致Activity无法释放出现内存泄露，View动画不存在此类问题
3. 兼容性问题：动画在3.0以下系统上有兼容性问题
4. View动画问题：View动画是对View的影像做动画，可能出现动画完成后View无法隐藏的现象，只要调用view.clearAnimation()消除View动画即可
5. 不要使用px：尽量使用dp，保持在不同设备上的一致性
6. 动画元素交互：3.0以后，属性动画单击事件触发位置在移动后位置，View动画仍在原位置
7. 硬件加速：建议开启，提高流畅性

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
RANGE: bytes=2000070- //此处指定传输的起点和终点
Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2 

```

```
206 // 表示续传
Content-Length=106786028 
Content-Range=bytes 2000070-106786027/106786028 //表示从2000070开始传输 
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
   if (urlConnection.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {// 即206
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

# Fragment

## 加载方式

依赖于Activity,不能单独存在，需要宿主

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
   * 当点击back按钮会自动帮我们实现退栈按钮

5. Fragment之间通信

   * 在fragment中调用activity中的方法getActivity();
   * 在Activity中调用Fragment中的方法，接口回调；
   * 在Fragment中调用Fragment中的方法findFragmentById/Tag

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

# 进程

## 进程和线程的区别

### 宏观认识

进程，是并发执行的程序在执行过程中分配和管理资源的基本单位，是一个动态概念，竟争计算机系统资源的基本单位。每一个进程都有一个自己的地址空间，即进程空间。进程空间的大小 只与处理机的位数有关

线程，是进程的一部分，一个没有线程的进程可以被看作是单线程的。线程有时又被称为轻权进程或轻量级进程，也是 CPU 调度的一个基本单位

### 区别

进程拥有一个完整的虚拟地址空间，不依赖于线程而独立存在；反之，线程是进程的一部分，没有自己的地址空间，与进程内的其他线程一起共享分配给该进程的所有资源

### Android中的进程与线程

**进程：** 每个app运行时前首先创建一个进程，该进程是由Zygote fork出来的，用于承载App上运行的各种Activity/Service等组件。进程对于上层应用来说是完全透明的，这也是google有意为之，让App程序都是运行在Android Runtime。大多数情况一个App就运行在一个进程中，除非在AndroidManifest.xml中配置Android:process属性，或通过native代码fork进程

**线程：** 线程对应用来说非常常见，比如每次new Thread().start都会创建一个新的线程。该线程与App所在进程之间资源共享，从Linux角度来说进程与线程除了是否共享资源外，并没有本质的区别，都是一个task_struct结构体，**在CPU看来进程或线程无非就是一段可执行的代码，CPU采用CFS调度算法，保证每个task都尽可能公平的享有CPU时间片**

## 进程间通信方法（IPC）

### 必要性

多进程带来的问题：

1. 静态成员和单例模式失效
2. 线程同步机制失效
3. SharePreference可靠性下降
4. Application多次创建

### 序列化

#### 原因

当两个进程在进行远程通信时，彼此可以发送各种类型的数据。无论是何种类型的数据，都会以二进制序列的形式在网络上传送。

发送方，序列化：对象->字节序列

接收方，反序列化：字节序列->对象

序列化的目的就是为了跨进程传递格式化数据

#### Serializable

```java
public class User implements Serializable {
    private static final long serialVersionUID = 519067123721295773L;
}
```

serialVersionUID用于辅助序列化和反序列化，序列化后数据只有serialVersionUID和当前类serialVersionUID一致才可以序列化

相比不指定（自动生成）serialVersionUID，手动指定serialVersionUID可以避免由于类的改变，导致系统重新计算hash值并赋给serialVersionUID并反序列化失败

#### Parcelable

```java
public class User implements Parcelable {
    public int userId;
    public String userName;
    public boolean isMale;

    public Book book;

    public User() {
    }

    public User(int userId, String userName, boolean isMale) {
        this.userId = userId;
        this.userName = userName;
        this.isMale = isMale;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(userId);
        out.writeString(userName);
        out.writeInt(isMale ? 1 : 0);
        out.writeParcelable(book, 0);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private User(Parcel in) {
        userId = in.readInt();
        userName = in.readString();
        isMale = in.readInt() == 1;
        book = in.readParcelable(Thread.currentThread().getContextClassLoader());
    }
}
```

#### Serializable和Parcelable的区别

1. Serializable是JAVA中的序列化接口，虽然使用起来简单但是开销很大，序列化和反序列化过程都要大量的I/O操作。
2. Parcelable是Android中的序列化方式，更适合使用在Android平台上。它的缺点就是使用起来稍微麻烦一点，但是效率高。
3. Parcelable主要用在内存序列化上，Serializable主要用于将对象序列化到存储设备中或者通过网络传输

### 文件共享

两个进程通过读/写同一个文件来交换数据，比如A进程把数据写入文件，B进程通过读取这个文件来获取数据。

文件共享方式适合在对数据同步要求不高的进程之间进行通信，并且要妥善处理并发读/写的问题

> SharePreference在高并发读写时很不可靠，会丢失数据

### Bundle

四大组件中的三大组件（Activity,Service,Receiver）都是支持在Intent中传递Bundle数据的，由于Bundle实现了Parcelable接口，所以他可以方便地在不同的进程间传输。基于这一点，我们在一个进程中启动了另一个进程的时候，就可以在Bundle中附加我们需要传输的信息，并通过Intent传送出去。但是，传输的数据必须能够被序列化，比如基本类型、实现了Serializable/Parcelable的对象以及一些Android支持的特殊对象

```java
Intent intent = new Intent();    
intent.setClass(TestBundle.this, Target.class);    
Bundle mBundle = new Bundle();    
mBundle.putString("Data", "data from TestBundle"); 
intent.putExtras(mBundle);    
startActivity(intent);  
```

### AIDL

AIDL通过定义服务端暴露的接口，以提供给客户端来调用，AIDL使服务器可以并行处理，而Messenger封装了AIDL之后只能串行运行，所以Messenger一般用作消息传递

通过编写aidl文件来设计想要暴露的接口，编译后会自动生成响应的java文件，服务器将接口的具体实现写在Stub中，用IBinder对象传递给客户端，客户端bindService的时候，用asInterface的形式将IBinder还原成接口，再调用其中的方法

支持以下几种数据：

* 基本数据类型
* String和CharSequence
* List：只支持ArrayList，且里面的每个元素都必须被AIDL支持
* Map：只支持HashMap，且里面的每个元素都必须被AIDL支持
* 实现了Parcelable的对象（需要新建一个同名的AIDL文件，并声明为Parcelable类型）
* 其他AIDL接口

> Parcelable对象类型的参数上必须标上方向：in，out或inout
>
> 自定义的Parcelable对象或AIDL对象一定要显式地import进来，不管和当前AIDL文件在不在同一个包内

定义AIDL，新建一个`.aidl`文件

```java
package com.example.aidl;
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
         android:process=":remote" />
```

> “:”表示在当前进程名前附加包名，是一种简写

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

> 可以onBind或onTransact方法中进行权限验证，例如检查包名等

[Android的进阶学习(四)--AIDL的使用与理解](https://www.jianshu.com/p/4e38cdc016c9)

### Messenger

Messager实现IPC通信，底层是使用了AIDL方式。和AIDL方式不同的是，Messager方式是利用Handler形式处理，因此，它是线程安全的，这也表示它不支持并发处理。相反，AIDL方式是非线程安全的，支持并发处理。服务端（被动方）提供一个Service来处理客户端（主动方）连接，维护一个Handler来创建Messenger，在onBind时返回Messenger的binder。双方用Messenger来发送数据，用Handler来处理数据。Messenger处理数据依靠Handler，所以是串行的，也就是说，Handler接到多个message时，就要排队依次处理

> Message对象本身是无法被传递到进程B的，send(message)方法会使用一个Parcel对象对Message对象编集，再将Parcel对象传递到进程B中，然后解编集，得到一个和进程A中Message对象内容一样的对象），再把Message对象加入到进程B的消息队列里，Handler会去处理它
>
> Message对象的object字段不支持自定义的Parcelable对象

服务端

```java
public class RemoteService extends Service {
    private static final String TAG = "RemoteService"
        private final Messenger mMessenger = new Messenger(new Handler() {  
            @Override  
            public void handleMessage(Message msg) {  
                switch (msg.what) {
                    case Constants.MSG_FROM_CLIENT:
                        Log.i(TAG, "receive msg from client:" + msg.getData().getString("msg"));

                        // 回复消息
                        Messenger client = msg.replyTo;
                        Message replyMessage = Message.obtain(null, Constants.MSG_FROM_SERVICE);
                        Bundle data = new Bundle();
                        data.putString("reply", "reply message");
                        replyMessage.setData(data);
                        try {
                            client.send(replyMessage);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
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

客户端

```java
private Messenger mGetReplyMessenger = new Messenger(new Handler() {
    @Override  
    public void handleMessage(Message msg) {  
        switch (msg.what) {
            case Constants.MSG_FROM_SERVICE:
                // 接收服务器消息
                Log.i(TAG, "receive msg from service:" + msg.getData().getString("msg"));
                break;
            default:
                super.handleMessage(msg);
        }
    }  
});  

private Messenger mService;  

private ServiceConnection mConnection = new ServiceConnection() {  
    @Override  
    public void onServiceConnected(ComponentName name, IBinder service) {  
        mService = new Messenger(service);

        // 发送消息
        Message msg = Message.obtain(null, Constants.MSG_FROM_CLIENT);
        Bundle data = new Bundle();
        data.putString("msg", "message");
        msg.setData(data);
        msg.replyTo = mGetReplyMessenger; // 设置回复的Messenger
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }  

    @Override public void onServiceDisconnected(ComponentName name) {  
        mService = null;  
    } 
};
```

客户端绑定服务端的服务

```java
bindService(new Intent(this, RemoteService.class), mConnection, Context.BIND_AUTO_CREATE); 
```

[Android的进阶学习（五）--Messenger的使用和理解](https://www.jianshu.com/p/af8991c83fcb)

[Android IPC进程通信——Messager方式](http://blog.csdn.net/chenfeng0104/article/details/7010244)

### ContentProvider

系统四大组件之一，底层也是Binder实现，主要用来应用程序之间的数据共享，也就是说一个应用程序用ContentProvider将自己的数据暴露出来，其他应用程序通过ContentResolver来对其暴露出来的数据进行增删改查

Android内置的许多数据都是使用ContentProvider形式，供开发者调用的(如视频，音频，图片，通讯录等)

自定义的ContentProvider注册时要提供authorities属性，应用需要访问的时候将属性包装成Uri.parse("content://authorities")。还可以设置permission，readPermission，writePermission来设置权限。 ContentProvider有query，delete，insert等方法，看起来貌似是一个数据库管理类，但其实可以用文件，内存数据等等一切来充当数据源，query返回的是一个Cursor，可以自定义继承AbstractCursor的类来实现

#### 工作原理（非重点）

> ContentProvider所在的进程启动后，ContentProvider会被同时启动并发布到AMS中，onCreate优先于Application的onCreate执行

![ontentprovider启动过程](D:\Lizij\Document\LearningNotes\Android\images\contentprovider启动过程1.png)

1. 应用启动时，从ActivityThread#main进入，创建ActivityThread实例并创建主线程消息队列
2. 在attach中远程调用AMS#attachApplication并将ApplicationThread（Binder，IApplicationThread）提供给AMS
3. 在attachApplication中调用ApplicationThread#bindApplication，通过ActivityThread的mH Handler切换到ActivityThread中执行，调用handleBindApplication
4. ActivityThread创建Application对象，加载ContentProvider，然后执行Application#onCreate

ContentProvider启动后就可以访问增删改查的四个接口，通过Binder调用。外界程序通过AMS根据Uri获取对应的Binder接口IContentProvider，再通过IContentProvider访问

一般来说，`android:multiprocess`都指定为false，表示ContentProvider为单实例

[Android之ContentProvider详解](http://blog.csdn.net/x605940745/article/details/16118939)

### Socket

Android不允许在主线程中请求网络，而且请求网络必须要注意声明相应的permission。然后，在服务器中定义ServerSocket来监听端口，客户端使用Socket来请求端口，连通后就可以进行通信

[Android：这是一份很详细的Socket使用攻略](http://blog.csdn.net/carson_ho/article/details/53366856)

### Binder连接池

#### 适用场景

大量业务都需要AIDL，AIDL需要在少数几个Service中集中管理

#### 原理

每个业务模块创建自己的AIDL接口并实现接口，业务之间不能有耦合，所有实现细节单独分开，向Service提供自己的唯一标识和其对应的Binder对象

服务端至少一个Service，提供queryBinder接口，根据业务特征返回相应的Binder对象，避免重复创建Service

#### 实现

创建2个AIDL接口

```java
// ISecurityCenter.aidl
package com.ryg.chapter_2.binderpool;

interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}

// ICompute.aidl
package com.ryg.chapter_2.binderpool;

interface ICompute {
    int add(int a, int b);
}
```

实现接口

```java
public class SecurityCenterImpl extends ISecurityCenter.Stub {}
public class ComputeImpl extends ICompute.Stub {}
```

创建BinderPool接口

```java
// IBinderPool.aidl
package com.ryg.chapter_2.binderpool;

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}
```

实现BinderPoolImpl接口，queryBinder根据不同模块的标识即binderCode返回不同的Binder对象

```java
@Override
public IBinder queryBinder(int binderCode) throws RemoteException {
    IBinder binder = null;
    switch (binderCode) {
        case BINDER_SECURITY_CENTER: {
            binder = new SecurityCenterImpl();
            break;
        }
        case BINDER_COMPUTE: {
            binder = new ComputeImpl();
            break;
        }
        default:
            break;
    }

    return binder;
}
```

在远程BinderPoolService的onBind中返回BinderPool的实例

```java
private Binder mBinderPool = new BinderPool.BinderPoolImpl();

@Override
public IBinder onBind(Intent intent) {
    return mBinderPool;
}
```

实现BinderPool

```java
public class BinderPool {
    private static final String TAG = "BinderPool";
    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY_CENTER = 1;

    private Context mContext;
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInstance;
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    // 使用单例模式实现
    public static BinderPool getInsance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context);
                }
            }
        }
        return sInstance;
    }

    private synchronized void connectBinderPoolService() {
        // CountDownLatch类位于java.util.concurrent包下
        // 利用它可以实现类似计数器的功能。比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，
        // 此时就可以利用CountDownLatch来实现这种功能了
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(service, mBinderPoolConnection,
                             Context.BIND_AUTO_CREATE);
        try {
            // 调用await()方法的线程会被挂起，它会等待直到count值为0才继续执行
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * query binder by binderCode from binder pool
     * 
     * @param binderCode
     *            the unique token of binder
     * @return binder who's token is binderCode<br>
     *         return null when not found or BinderPoolService died.
     */
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // ignored.
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // 将count值减1
            // 相当于通知connectBinderPoolService从await处继续执行
            // 通过CountDownLatch将bindService这一异步操作转换为同步操作
            // 应避免在主线程中执行
            mConnectBinderPoolCountDownLatch.countDown();
        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            // 当binder意外死亡时重新连接
            Log.w(TAG, "binder died.");
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };

    public static class BinderPoolImpl extends IBinderPool.Stub {
        //...
    }

}
```

# ListView和RecyclerView

## ListView

### ListView的内部点击事件

在使用ListView的时候，我们通常会使用到其item的点击事件。而有些时候我们可能会用到item内部控件的点击操作，比如在item内部有个Button，当点击该Button时，删除所在的item。

![itemdeleteclick](http://img.blog.csdn.net/20170129141401807?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvSlpob3dl/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

ListView布局文件

```xml
<ListView
          android:id="@+id/listView"
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          />
```

item布局文件list_item.xml中包括了一个TextView和一个Button，其中Button添加了一个属性`android:focusable="false"` ，目的是为了不让Button强制获取item的焦点，否则item的点击事件就没用了

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              android:orientation="horizontal">

    <TextView
              android:id="@+id/item_tv"
              android:layout_width="0dp"
              android:layout_height="wrap_content"
              android:layout_weight="1"
              android:gravity="center"
              android:text="this is text"
              android:textSize="24dp"/>

    <Button
            android:focusable="false"
            android:id="@+id/item_btn"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="删除"/>
</LinearLayout>
```

实现Adapter时，采用接口回调，将点击item的position作为参数传出去

```java
private List<String> mList = new ArrayList<>();

public MyAdapter(Context context, List<String> list) {
    mContext = context;
    mList = list;
}

@Override
public View getView(final int i, View view, ViewGroup viewGroup) {
    ViewHolder viewHolder = null;
    if (view == null) {
        viewHolder = new ViewHolder();
        view = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
        viewHolder.mTextView = (TextView) view.findViewById(R.id.item_tv);
        viewHolder.mButton = (Button) view.findViewById(R.id.item_btn);
        view.setTag(viewHolder);
    } else {
        viewHolder = (ViewHolder) view.getTag();
    }
    viewHolder.mTextView.setText(mList.get(i));
    viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mOnItemDeleteListener.onDeleteClick(i);
        }
    });
    return view;
}
/**
     * 删除按钮的监听接口
     */
public interface onItemDeleteListener {
    void onDeleteClick(int i);
}

private onItemDeleteListener mOnItemDeleteListener;

public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
    this.mOnItemDeleteListener = mOnItemDeleteListener;
}
```

Activity中用匿名类实现该接口

```java
List<String> mList = new ArrayList<>();
initList();
final MyAdapter adapter = new MyAdapter(MainActivity.this, mList);
//ListView item 中的删除按钮的点击事件
adapter.setOnItemDeleteClickListener(new MyAdapter.onItemDeleteListener() {
    @Override
    public void onDeleteClick(int i) {
        mList.remove(i);
        adapter.notifyDataSetChanged();
    }
});
```

[Android ListView：实现item内部控件的点击事件](http://blog.csdn.net/JZhowe/article/details/54767477)

## RecyclerView

### 使用

在gradle中添加依赖

```
dependencies {
    compile 'com.android.support:recyclerview-v7:24.2.1'
}
```

在布局中添加RecyclerView

```xml
<android.support.v7.widget.RecyclerView
                                        android:id="@+id/recycler_view"
                                        android:layout_width="match_parent"
                                        android:layout_height="match_parent" />
```

新建Adapter

```java
public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder>{

    private List<Fruit> mFruitList;

    // 静态内部类ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        View fruitView;
        ImageView fruitImage;
        TextView fruitName;

        public ViewHolder(View view) {
            super(view);
            fruitView = view;
            fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
            fruitName = (TextView) view.findViewById(R.id.fruit_name);
        }
    }

    // 初始化数据源
    public FruitAdapter(List<Fruit> fruitList) {
        mFruitList = fruitList;
    }

    // 创建ViewHolder实例，可以针对每个view实现点击事件
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Fruit fruit = mFruitList.get(position);
                Toast.makeText(v.getContext(), "you clicked view " + fruit.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.fruitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Fruit fruit = mFruitList.get(position);
                Toast.makeText(v.getContext(), "you clicked image " + fruit.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    // 对子项数据进行赋值
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Fruit fruit = mFruitList.get(position);
        holder.fruitImage.setImageResource(fruit.getImageId());
        holder.fruitName.setText(fruit.getName());
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }
}
```

Activity中使用

```java
RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
LinearLayoutManager layoutManager = new LinearLayoutManager(this); // 竖直线性布局
// layoutManager。setOrientation(LinearLayoutManager.HORIZONTAL); // 水平线性布局
// StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL); // 3格网格布局，可以实现瀑布流布局
recyclerView.setLayoutManager(layoutManager);
FruitAdapter adapter = new FruitAdapter(fruitList);
recyclerView.setAdapter(adapter);
```

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

## fromJSON原理

```java
Gson gson = new Gson();
Object obj = gson.fromJson(String, Object.class);
```

Gson反序列化的过程本质上是一个递归过程。当对其中一个字段进行解析时，其值如果是花括号保存的对象，则递归解析该对象；其值如果是数组，则处理数组后递归解析数组中的各个值。递归的终止条件是反序列的字段类型是java的基本类型信息

1. 解析对象类型，缓存对象字段信息 
2. 解析json，获得json中的键值对信息 
3. 根据json中的键名寻找对象中对应的字段 
4. 如果字段是非基本类型，则回到流程1处理该字段信息和json中的值，否则继续5 
5. 字段是基本类型，则把值信息转化为基本类型返回 
6. 反射赋值：所以需要Class对象

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

下载并配置NDK

在Java代码中中声明一个native方法

```java
static {
    System.loadLibrary("hello"); // 加载hello库，文件名需要和Android.mk文件中的LOCAL_MODULE属性指定的值相同
}
public native String sayHello();
```

使用javah命令生成带有native方法的头文件

```shell
javac com/xxx/TestHelloActivity.java
javah com.xxx.TestHelloActivity
```

> JDK1.7 需要在工程的src目录下执行上面的命令，JDK1.6 需要在工程的bin/classes目录下执行以上命令

创建`jni`目录，并在`jni`目录中创建一个Hello.c文件，根据头文件实现C代码。写C代码时，结构体JNIEnv*对象对个别object对象很重要，在实现的C代码的方法中必须传入这两个参数

```c
jstring Java_com_xxx_TestHelloActivity_sayHello(JNIEnv* env,jobject obj){
    char* text = "hello from c!";
    return (**env).NewsStringUTF(env,text);
}
```

在JNI的目录下创建Android.mk和Application.mk,并根据需要编写里面的内容

```makefile
# Android.mk
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

# Application.mk
# 常见架构有armeabi，x86和mips，默认编译all，即所有平台
APP_ABI := armeabi
```

在工程的根目录下执行`ndk_build`命令，编译.so文件

这是会创建一个`jni`目录平级的目录libs，libs下放的就是so库的目录

在`app/src/main`下创建`jniLibs`目录，将so库拷贝到`jniLibs`中，通过AndroidStudio编译运行即可。如果想用其他目录，则在gradle中配置

```
android {
    ...
    sourceSets.main {
        jniLibs.srcDir 'src/main/jni_libs'
    }
}
```

还可以在defaultConfig中添加NDK选项，让AndroidStudio可以自动编译JNI代码。将JNI代码放在`app/src/main/jni`中，或者手动指定`jni.srcDirs`

```
android {
    ...
    defaultConfig {
        ...
        ndk {
            moduleName 'jni-test'
        }
        
        sourceSets.main {
            jni.srcDirs 'src/main/jni_src'
        }
    }
}
```

AndroidStudio默认会将所有CPU平台的so库打包到apk中，可以在gradle中指定只需要打包armeabi的so库，然后在Build Variants面板中选择armDebug选项进行编辑

```
android {
    ...
    productFlavors {
        arm {
            ndk {
                abiFilter "armeabi"
            }
        }
        x86 {
            ndk {
                abiFilter "x86"
            }
        }
    }
}
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
        (JNIEnv *, jclass);
    // 方法名多了一个“_l”，这是因为native_init方法有一个“_”，它会在转换为JNI方法时变成“_1”，类似于转义。 

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

`native_init`方法被声明为注释1处的方法，格式为`Java_包名_类名_方法名`

`extern "C"`表示内部的函数采用C语言命名风格编译

`JNIEnv *` 是一个指向全部JNI方法的指针，该指针只在创建它的线程有效，不能跨线程传递。 

`jclass`是JNI的数据类型，对应Java的java.lang.Class实例

`jobject`同样也是JNI的数据类型，对应于Java对象中的this

`JNIEXPORT`和`JNICALL`是JNI中定义的宏

当我们在Java中调用`native_init`方法时，就会从JNI中寻找`Java_com_example_MediaRecorder_native_1init`方法，如果没有就会报错，如果找到就会为`native_init`和`Java_com_example_MediaRecorder_native_1init`建立关联，其实是保存JNI的方法指针，这样再次调用native_init方法时就会直接使用这个方法指针就可以了。 
静态注册就是根据方法名，将Java方法和JNI方法建立关联，但是它有一些缺点：

* JNI层的方法名称过长。
* 声明Native方法的类需要用javah生成头文件。
* 初次调用JIN方法时需要建立关联，影响效率。

静态方法就是根据函数名来建立Java函数和JNI函数之间的关联关系的，这里要求JNI层函数的名字必须遵循特定的格式

### 动态注册

JNI中有一种结构用来记录Java的Native方法和JNI方法的关联关系，它就是JNINativeMethod，它在jni.h中被定义

```c
typedef struct{  
    const char* name;  // Java中native函数的名字，不用携带包的路径，例如“native_init”  
    const char* signature; // Java函数的签名信息，用字符串表示，是参数类型和返回类型的组合，用以应对Java里的函数重载  
    void* fnPtr; // JNI层对应函数的函数指针，注意他是void*类型  
} JNINativeMethod  
```

定义JNINativeMethod数组，依次为方法名，函数签名和函数指针

```c
JNINativeMethod nativeMethod[] = {
    {"dynamicRegFromJni", "()Ljava/lang/String;", (void*)nativeDynamicRegFromJni}
};
```

在`JNI_Onload`方法中注册

```c
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnv *env;
    if ((*jvm) -> GetEnv(jvm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        return -1;
    }

    jclass clz = (*env) -> FindClass(env, "github/jp1017/hellojni/MainActivity");

    (*env) -> RegisterNatives(env, clz, nativeMethod, sizeof(nativeMethod) / sizeof(nativeMethod[0]));

    return JNI_VERSION_1_4;
}
```

> `JNI_OnLoad()`作用：
>
> 1. 指定 jni 版本：告诉 JVM 该组件使用哪一个 jni 版本(若未提供`JNI_OnLoad()`函数，JVM 会默认该使用最老的 JNI 1.1版本)，如果要使用新版本的JNI，例如JNI 1.4版，则必须由 `JNI_OnLoad()` 函数返回常量 JNI_VERSION_1_4 (该常量定义在 jni.h 中) 来告知 JVM 
> 2. 一系列初始化操作，当 JVM 执行到 System.loadLibrary() 函数时，会立即调用` JNI_OnLoad() `方法，因此在该方法中进行各种资源的初始化操作最为恰当

> ` jint RegisterNatives(JNIEnv *env, jclass clazz, const JNINativeMethod *methods, jint nMethods)`
>
> 向 clazz 参数指定的类注册本地方法。methods 参数将指定 JNINativeMethod 结构的数组，其中包含本地方法的名称、签名和函数指针。nMethods 参数将指定数组中的本地方法数

[安卓jni开发之native方法的动态注册](https://www.jianshu.com/p/67019062774b)

## JNI数据类型和类型签名

![img](https://upload-images.jianshu.io/upload_images/1952665-50f914b8ae912780.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/538)

JNIEnv是指向可用JNI函数表的接口指针，原生代码通过JNIEnv接口指针提供的各种函数来使用虚拟机的功能。JNIEnv是一个指向线程-局部数据的指针，而线程-局部数据中包含指向线程表的指针。实现原生方法的函数将JNIEnv接口指针作为它们的第一个参数。

### 数据类型转换

基本类型

| Java类型 | 别名     | C++本地类型    | 字节         | 签名 |
| -------- | -------- | -------------- | ------------ | ---- |
| boolean  | jboolean | unsigned char  | 8, unsigned  | Z    |
| byte     | jbyte    | signed char    | 8            | B    |
| char     | jchar    | unsigned short | 16, unsigned | C    |
| short    | jshort   | short          | 16           | S    |
| int      | jint     | long           | 32           | I    |
| long     | jlong    | __int64        | 64           | J    |
| float    | jfloat   | float          | 32           | F    |
| double   | jdouble  | double         | 64           | D    |
| void     | void     | void           |              | V    |

引用类型

![img](https://upload-images.jianshu.io/upload_images/1952665-29614d7760b5f164.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

类的签名：`L+包名+类名+；`，例如`Ljava/lang/String`

数组签名：`[+类型签名`，可以多级嵌套，例如`[java/lang/String`

方法签名：`(参数类型签名)+返回值类型签名`，例如`boolean fun1(int a , String b, int[]c)`的签名为`(ILjava/lang/String;[I)Z`

[Android面试题：对JNI和NDK的理解](http://blog.csdn.net/yyg_2015/article/details/72229892)

[Android深入理解JNI（一）JNI原理与静态、动态注册](http://blog.csdn.net/itachi85/article/details/73459880)

[JNIEnv结构体解析](https://www.jianshu.com/p/453b0463a84c)

## jni调用java

### 一般步骤

在本地方法中调用Java对象的方法的步骤：

1. 获取你需要访问的Java对象的类

   * FindClass通过传java中完整的类名来查找java的class
   * GetObjectClass通过传入jni中的一个java的引用来获取该引用的类型。

   > 前者要求你必须知道完整的类名，后者要求在Jni有一个类的引用。

2. 获取MethodID,调用方法

   * GetMethodID 得到一个实例的方法的ID 
   * GetStaticMethodID 得到一个静态方法的ID 

3. 获取对象的属性

   * GetFieldID 得到一个实例的域的ID 
   * GetStaticFieldID 得到一个静态的域的ID

   > JNI通过ID识别域和方法，一个域或方法的ID是任何处理域和方法的函数的必须参数。

### 常用函数

* `jclass FindClass (JNIEnv *env, const char *name)`

  该函数用于加载本地定义的类。它将搜索由CLASSPATH 环境变量为具有指定名称的类所指定的目录和 zip文件

* `jobject NewObject (JNIEnv *env ,  jclass clazz,  jmethodID methodID, ...)`

  构造新 Java 对象。方法 ID指示应调用的构造函数方法。该 ID 必须通过调用 GetMethodID() 获得，且调用时的方法名必须为 \<init>，而返回类型必须为 void (V)

* `jfieldID   GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig)`

  返回类的实例（非静态）域的域 ID。该域由其名称及签名指定。访问器函数的Get\<type>Field 及 Set\<type>Field 系列使用域 ID 检索对象域

* `jmethodID GetMethodID(JNIEnv *env, jclass clazz,    const char *name, const char *sig)`

  返回类或接口实例（非静态）方法的方法 ID。方法可在某个 clazz 的超类中定义，也可从 clazz 继承。该方法由其名称和签名决定。 GetMethodID() 可使未初始化的类初始化。要获得构造函数的方法 ID，应将 \<init> 作为方法名，同时将void (V) 作为返回类型

* `NativeType Call<type>Method (JNIEnv*en v, jobject obj, jmethodID methodID, ...)`

  根据所指定的方法 ID 调用 Java 对象的实例非静态方法，参数附加在函数后面

* `NativeType Call<type>StaticMethod (JNIEnv*env, jclass classzz, ...)`

  根据所指定的方法 ID 调用 Java 对象的实例静态方法，参数附加在函数后面

### 示例

```java
public static void methodCalledByJni(String msgFromJni) {
    Log.d(TAG, "methodCalledByJni, msg: " + msgFromJni);
}
```

```c
void callJavaMethod(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass("com/ryg/JniTestApp/MainActivity");
    if (clazz == NULL) {
        printf("find class MainActivity error!");
        return;
    }
    jmethodID id = env->GetStaticMethodID(clazz, "methodCalledByJni", "(Ljava/lang/String;)V");
    if (id == NULL) {
        printf("find method methodCalledByJni error!");
    }
    jstring msg = env->NewStringUTF("msg send by callJavaMethod in test.cpp.");
    env->CallStaticVoidMethod(clazz, id, msg);
}
```

### 常见问题

#### JNIEnv和jobject多线程共享问题

JNIEnv是一个线程相关的变量， 对于每个 thread 而言是唯一的 ，所以`*env`指针不可以为多个线程共用

不能直接保存一个线程中的jobject指针到全局变量中,然后在另外一个线程中使用它

**解决办法：**

java虚拟机的JavaVM指针是整个jvm公用的，我们可以通过JavaVM来得到当前线程的JNIEnv指针

用env->NewGlobalRef创建一个全局变量，将传入的obj(局部变量)保存到全局变量中,其他线程可以使用这个全局变量来操纵这个java对象

> 若不是一个 jobject，则不需要这么做。如：
>
> jclass 是由 jobject public 继承而来的子类，所以它当然是一个 jobject，需要创建一个 global reference 以便日后使用。
>
> 而 jmethodID/jfieldID 与 jobject 没有继承关系，它不是一个 jobject，只是个整数，所以不存在被释放与否的问题，可保存后直接使用

```c
#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<pthread.h>
#include<jni.h>
#include<android/log.h>
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "native-activity", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "native-activity", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "native-activity", __VA_ARGS__))
static JavaVM *gs_jvm = NULL; // 保存起来  
static jobject gs_object = NULL; // 保存起来  

JNIEXPORT jint JNICALL Java_com_example_testjni_Hunter_test(JNIEnv *env, jclass obj) {  
    // 注意了，在第一次进来的时候，我就保存他们了，要快！！！！  
    env->GetJavaVM(&gs_jvm); //保存到全局变量中JVM     
    gs_object = env->NewGlobalRef(obj); //直接赋值obj到全局变量是不行的,应该调用以下函数: 
    return 0;  
}  

/* 服务器发送过来的消息到达了 */  
int frecvMsg_callback() {  
    // 注意！！我要调用了  
    JNIEnv *env;  
    // 获取当前线程的 env   
    //Attach主线程
    if((*gs_jvm)->AttachCurrentThread(gs_jvm, &env, NULL) != JNI_OK)
    {
        LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
        return NULL;
    }
    // 这个class默认是初始化gs_object时所调用的Java 类  
    jclass cls;
    //找到对应的类
    cls = (*env)->GetObjectClass(env,gs_obj);
    if(cls == NULL)
    {
        LOGE("FindClass() Error.....");
        goto error;
    }
    //再获得类中的方法
    mid = (*env)->GetMethodID(env, cls, "fromJNI", "(I)V");
    if (mid == NULL)
    {
        LOGE("GetMethodID() Error.....");
        goto error; 
    }
    //最后调用java中的方法
    (*env)->CallVoidMethod(env, cls, mid ,(int)arg);

    // 用完之后一定要  DetachCurrentThread 取消关联，要不然程序退出会有异常  
    error:   
    //Detach主线程
    if((*gs_jvm)->DetachCurrentThread(gs_jvm) != JNI_OK)
    {
        LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
    }

    return 0;  
}

//由java调用以创建子线程
JNIEXPORT void Java_com_test_JniThreadTestActivity_mainThread( JNIEnv* env, jobject obj, jint threadNum)
{
    int i;
    pthread_t* pt;
    pt = (pthread_t*) malloc(threadNum * sizeof(pthread_t));
    for (i = 0; i < threadNum; i++){
        //创建子线程
        pthread_create(&pt[i], NULL, &thread_fun, (void *)i);
    }

    for (i = 0; i < threadNum; i++){
        pthread_join(pt[i], NULL);
    }
    LOGE("main thread exit.....");
}

//当动态库被加载时这个函数被系统调用
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;   

    //获取JNI版本
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_4) != JNI_OK)
    {
        LOGE("GetEnv failed!");
        return result;
    }

    return JNI_VERSION_1_4;
}
```

[深入了解android平台的jni---本地多线程调用java代码](http://www.cnblogs.com/aiguozhe/p/5355226.html)

[JNI方法整理](https://blog.csdn.net/autumn20080101/article/details/8646431)

# MarsDaemon

MarsDaemon是一个轻量级的开源库，配置简单，在6.0及其以下的系统中拥有出色的保活能力

[源码地址]([https://github.com/Marswin/MarsDaemon](https://link.jianshu.com/?t=https://github.com/Marswin/MarsDaemon))

## 配置

1. 从github导入项目，并修改gradle

   ```
   dependencies {
       compile fileTree(include: ['*.jar'], dir: 'libs')
       testCompile 'junit:junit:4.12'
       compile project(':LibMarsdaemon')
   }
   ```

   ​

   ![img](https://upload-images.jianshu.io/upload_images/3610640-4141b27edb6813fb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/276)

2. ​


1. 明确自己需要常驻的进程service，创建一个和他同进程的receiver，然后在另外一个进程中创建一个service和一个receiver，并写在Manifest中。进程名可以自定义

   ```xml
   <service android:name=".Service1" android:process=":process1"/>
   <receiver android:name=".Receiver1" android:process=":process1"/>
   <service android:name=".Service2" android:process=":process2"/>
   <receiver android:name=".Receiver2" android:process=":process2"/>
   ```

   service1是应用中有业务逻辑的需要常驻进程的service，其他三个组件都是额外创建的，里面不要做任何事情，都是空实现就好了

2. 用你的Application继承`DaemonApplication`，然后在回调方法`getDaemonConfigurations`中返回一个配置，将刚才注册的进程名，service类名，receiver类名传进来

   ```java
   public class MyApplication1 extends DaemonApplication {

       /**
        * you can override this method instead of {@link android.app.Application attachBaseContext}
        * @param base
        */
       @Override
       public void attachBaseContextByDaemon(Context base) {
           super.attachBaseContextByDaemon(base);
       }
   ```


       /**
        * give the configuration to lib in this callback
        * @return
        */
       @Override
       protected DaemonConfigurations getDaemonConfigurations() {
           DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
               "com.marswin89.marsdaemon.demo:process1",
               Service1.class.getCanonicalName(),
               Receiver1.class.getCanonicalName());
    
           DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
               "com.marswin89.marsdaemon.demo:process2",
               Service2.class.getCanonicalName(),
               Receiver2.class.getCanonicalName());
    
           DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
           //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
           return new DaemonConfigurations(configuration1, configuration2, listener);
       }
    
    
       class MyDaemonListener implements DaemonConfigurations.DaemonListener{
           @Override
           public void onPersistentStart(Context context) {
           }
    
           @Override
           public void onDaemonAssistantStart(Context context) {
           }
    
           @Override
           public void onWatchDaemonDaed() {
           }
       }
   }
   ```

   此时如果你想在自己的application里面复写`attachBaseContext`方法的话，发现他已经被写为final，因为我们需要抢时间，所以必须保证进程进入先加载Marsdaemon，如果你想在`attchBaseContext`中做一些事情的话，可以复写`attachBaseContextByDaemon`方法

3. 如果你的Application已经继承了其他的Application类，那么可以参考Appliation2，在Application的`attachBaseContex`t的时候初始化一个`DaemonClient`，然后调用他的`onAttachBaseContext`同样可以实现，当然了，他同样需要一个配置来告诉他我们刚才在manifest中配的信息

   ```java
   public class MyApplication2 extends Application {

       private DaemonClient mDaemonClient;

       @Override
       protected void attachBaseContext(Context base) {
           super.attachBaseContext(base);
           mDaemonClient = new DaemonClient(createDaemonConfigurations());
           mDaemonClient.onAttachBaseContext(base);
       }



       private DaemonConfigurations createDaemonConfigurations(){
           DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
               "com.marswin89.marsdaemon.demo:process1",
               Service1.class.getCanonicalName(),
               Receiver1.class.getCanonicalName());
           DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
               "com.marswin89.marsdaemon.demo:process2",
               Service2.class.getCanonicalName(),
               Receiver2.class.getCanonicalName());
           DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
           //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
           return new DaemonConfigurations(configuration1, configuration2, listener);
       }


       class MyDaemonListener implements DaemonConfigurations.DaemonListener{
           @Override
           public void onPersistentStart(Context context) {
           }

           @Override
           public void onDaemonAssistantStart(Context context) {
           }

           @Override
           public void onWatchDaemonDaed() {
           }
       }
   }
   ```

[Android 使用MarsDaemon进程常驻](https://www.jianshu.com/p/70d45a79456a)

# MVC和MVP

## MVC

### 定义

MVC是一个架构模式，它分离了表现与交互。它被分为三个核心部件：模型、视图、控制器

![img](http://img0.tuicool.com/zAnI3q.jpg!web)

* 逻辑模型（M）：负责定义封装信息的数据结构。
* 视图模型（V）：负责将Model中的信息展示给用户。
* 控制器（C）：用于控制Model中信息在View中的展示方式

**Android中最典型MVC是ListView**

* Model：封装的信息，例如array
* View：ListView，
* Controller：Adapter控制数据怎样在ListView中显示

### 优势

* **耦合性低**：view和control分离，允许更改view，却不用修改model和control，很容易改变应用层的数据层和业务规则
* **可维护性**：分离view和control使得应用更容易维护和修改(分工明确，逻辑清晰)

### 控制流程

* 所有的终端用户请求被发送到控制器。
* 控制器依赖请求去选择加载哪个模型，并把模型附加到对应的视图。
* 附加了模型数据的最终视图做为响应发送给终端用户。

## MVP

在MVP里，Presenter完全把Model和View进行了分离，主要的程序逻辑在Presenter里实现。而且，Presenter与具体的View是没有直接关联的，而是通过定义好的接口进行交互，从而使得在变更View时候可以保持Presenter的不变

* Model：数据源
* View：Activity或Fragment等
* Presenter：业务处理层，能调用View也能调用Model，纯Java类不涉及Android API

调用顺序：View->Presenter->Model，不可反向调用

![MVP架构调用关系](http://www.jcodecraeer.com/uploads/userup/13953/1G020140036-F40-0.png)

作为一种新的模式，MVP与MVC有着一个重大的区别：在MVP中View并不直接使用Model，它们之间的通信是通过Presenter (MVC中的Controller)来进行的，所有的交互都发生在Presenter内部，而在MVC中View会直接从Model中读取数据而不是通过 Controller。

MVP 模式将Activity 中的业务逻辑全部分离出来，让Activity 只做 UI 逻辑的处理，所有跟Android API无关的业务逻辑由 Presenter 层来完成。

将业务处理分离出来后最明显的好处就是管理方便，但是缺点就是增加了代码量

> 在MVC里，View是可以直接访问Model的！从而，View里会包含Model信息，不可避免的还要包括一些业务逻辑。 在MVC模型里，更关注的Model的不变，而同时有多个对Model的不同显示，即View。所以，在MVC模型里，Model不依赖于View，但是View是依赖于Model的。不仅如此，因为有一些业务逻辑在View里实现了，导致要更改View也是比较困难的，至少那些业务逻辑是无法重用的。
>
> 虽然 MVC 中的 View的确“可以”访问Model，但是我们不建议在 View 中依赖Model，而是要求尽可能把所有业务逻辑都放在 Controller 中处理，而 View 只和 Controller 交互
>
> 日常开发中的Activity，Fragment和XML界面就相当于是一个 MVC 的架构模式，Activity中不仅要处理各种 UI 操作还要请求数据以及解析。
>
> 这种开发方式的缺点就是业务量大的时候一个Activity 文件分分钟飙到上千行代码，想要改一处业务逻辑光是去找就要费半天劲，而且有点地方逻辑处理是一样的无奈是不同的 Activity 就没办法很好的写成通用方法

[Android MVP架构搭建](http://www.jcodecraeer.com/a/anzhuokaifa/2017/1020/8625.html?1508484926)

### 优势

1. 模型与视图完全分离，我们可以修改视图而不影响模型 
2. 可以更高效地使用模型，因为所有的交互都发生在一个地方：Presenter内部 
3. 我们可以将一个Presenter用于多个视图，而不需要改变Presenter的逻辑。这个特性非常的有用，因为视图的变化总是比模型的变化频繁。 
4. 如果我们把逻辑放在Presenter中，那么我们就可以脱离用户接口来测试这些逻辑（单元测试）

[MVC面试问题与答案](http://www.cnblogs.com/Hackson/p/7055695.html)

[每日一面试题--MVC思想是什么？](http://blog.csdn.net/qq_34986769/article/details/52594804)

# multidex

## 使用场景

Android中单个dex所能包含的最大方法数为65536：

* 如果方法数超过65536，编译器就会无法完成编译工作，抛出`DexIndexOverflowException`异常
* 有时方法数较多但未超过65536，在应用安装时，方法数超过了低版本Android的dexopt的缓冲区，会导致安装失败

## 使用

### 修改gradle

```gradle
android {
    ...
    defaultConfig {
        ...
        multiDexEnabled true
    }
}

dependencies {
    compile 'com.android.support:multidex:1.0.0'
}
```

### 修改代码

有3种方案可选

* 修改manifest

  ```xml
  <application
               android:name="android.support.multidex.MultiDexApplication"
               ...
  />
  ```

* 修改Application继承MultiDexApplication

  ```java
  public class TestApplication extends MultiDexApplication
  ```

* 修改Application的`attachBaseContext`，此方法先于`onCreate`执行

  ```java
  public class TestApplication extends Application {

      @Override
      protected void attachBaseContext(Context base) {
          super.attachBaseContext(base);
          MultiDex.install(this);
      }

  }
  ```

  ​

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

# PendingIntent

处于pending状态的意图，即待定，即将发生的意思

Intent是立即发生

## 用途

### 主要方法

| 用途         | 主要方法                                     |
| ---------- | ---------------------------------------- |
| 启动Activity | getActivity(Context, int requestCode, Intent intent, int flags)<br>获得一个PendingIntent，当发生时相当于Context.startActivity(Intent) |
| 启动Service  | getService(Context, int requestCode, Intent intent, int flags)<br>获得一个PendingIntent，当发生时相当于Context.startService(Intent) |
| 发送广播       | getBroadcast(Context, int requestCode, Intent intent, int flags)<br>获得一个PendingIntent，当发生时相当于Context.sendBroadcast(Intent) |

### 匹配规则

1. 内部Intent相同，即
   1. ComponentName相同
   2. intent-filter相同
2. requestCode相同

### 参数解析

Context：上下文

requestCode：Pending发送方请求码，默认为0

Intent：要调用的Intent

flags：

1. FLAG_ONE_SHOT：当前PendingIntent只能使用一次，自动cancel
2. FLAG_NO_CREATE：当前PendingIntent无法主动创建，使用很少
3. FLAG_CANCEL_CURRENT：当前PendingIntent如果已存在，则cancel所有之前的，并创建一个新的PendingIntent
4. FLAG_UPDATE_CURRENT：当前PendingIntent如果已存在，则更新其中Intent的Extras

# RemoteViews

在其他进程中显示并更新View界面

由于没有提供findViewById方法，无法访问内部的View元素，必须通过一系列set方法来完成

主要用于通知栏和小部件

## 原理

1. NotificationManagerService和AppWidgetService运行在SystemServer进程中
2. RemoteViews会通过Binder传输到SystemServer进程中
3. 系统根据RemoteViews的信息得到应用资源，用LayoutInflater加载布局文件
4. 通过一系列set方法执行界面更新
5. 将View操作封装成Action对象（Parcelable），传输到远程进程并依次执行

![技术分享图片](http://image.bubuko.com/info/201801/20180111124332052387.png)

# Service

## startService和bindService

### startService

 `onCreate()`--->`onStartCommand()` ---> `onDestory()`

> 如果服务已经开启，不会重复的执行`onCreate()`， 而是会调用`onStart()`和`onStartCommand()`
>
> 服务停止的时候调用 `onDestory()`。服务只会被停止一次

一旦服务开启跟调用者(开启者)就没有任何关系了。开启者退出了，开启者挂了，服务还在后台长期的运行。

开启者**不能调用**服务里面的方法

### bindService

`onCreate()` --->`onBind()`--->`onunbind()`--->`onDestory()`

> 绑定服务不会调用`onstart()`或者`onstartcommand()`方法

bind的方式开启服务，绑定服务，调用者挂了，服务也会跟着挂掉。
绑定者**可以调用**服务里面的方法

Service

```java
public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //返回MyBind对象
        return new MyBinder();
    }

    private void methodInMyService() {
        Toast.makeText(getApplicationContext(), "服务里的方法执行了。。。",
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * 该类用于在onBind方法执行后返回的对象，
     * 该对象对外提供了该服务里的方法
     */
    private class MyBinder extends Binder implements IMyBinder {

        @Override
        public void invokeMethodInMyService() {
            methodInMyService();
        }
    }
}

public interface IMyBinder { // 自定义的MyBinder接口用于保护服务中不想让外界访问的方法
    void invokeMethodInMyService();
}
```

Activity

```java
public class MainActivity extends Activity {

    private MyConn conn;
    private Intent intent;
    private IMyBinder myBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //开启服务按钮的点击事件
    public void start(View view) {
        intent = new Intent(this, MyService.class);
        conn = new MyConn();
        //绑定服务，
        // 第一个参数是intent对象，表面开启的服务。
        // 第二个参数是绑定服务的监听器
        // 第三个参数一般为BIND_AUTO_CREATE常量，表示自动创建bind
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    //调用服务方法按钮的点击事件
    public void invoke(View view) {
        myBinder.invokeMethodInMyService();
    }

    private class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder){
            //iBinder为服务里面onBind()方法返回的对象，所以可以强转为IMyBinder类型
            myBinder = (IMyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }
}
```

[Android 服务两种启动方式的区别](https://www.jianshu.com/p/2fb6eb14fdec)

## 生命周期

![img](https://upload-images.jianshu.io/upload_images/944365-cf5c1a9d2dddaaca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/456)

## HandlerThread

HandlerThread 是一个包含 Looper 的 Thread，我们可以直接使用这个 Looper 创建 Handler

HandlerThread相当于Thread + Looper，要停止的话需要先停止Looper，Looper停止后线程会自动结束

使用场景：**在子线程中执行耗时的、可能有多个任务的操作**。比如说多个网络请求操作，或者多文件 I/O 等等

### 原理

```java
public void run() {
    mTid = Process.myTid();
    Looper.prepare();
    synchronized (this) {
        mLooper = Looper.myLooper();
        notifyAll();
    }
    Process.setThreadPriority(mPriority);
    onLooperPrepared(); // 可以设置的回调接口，用于在loop前进行自定义设置
    Looper.loop();
    mTid = -1;
}
```



### 使用

```java
private HandlerThread mHandlerThread;
private Handler mThreadHandler;
private Handler mMainHandler = new Handler();
private TextView tvMain;

@Override
protected void onCreate(Bundle savedInstanceState)
{
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mHandlerThread = new HandlerThread("check-message-coming");
    mHandlerThread.start();

    // 获取子线程的Looper创建Handler，可以执行耗时操作
    mThreadHandler = new Handler(mHandlerThread.getLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            update();//模拟数据更新
            if (isUpdateInfo)
                mThreadHandler.sendEmptyMessage(MSG_UPDATE_INFO);
        }
    };
}

@Override
protected void onResume()
{
    super.onResume();
    //开始查询
    isUpdateInfo = true;
    mThreadHandler.sendEmptyMessage(MSG_UPDATE_INFO);
}

@Override
protected void onPause()
{
    super.onPause();
    //停止查询
    //以防退出界面后Handler还在执行
    isUpdateInfo = false;
    mThreadHandler.removeMessages(MSG_UPDATE_INFO);
}

@Override
protected void onDestroy()
{
    super.onDestroy();
    //释放资源
    mHandlerThread.quit();
}

private void update()
{
    try
    {
        //模拟耗时
        Thread.sleep(2000);
        mMainHandler.post(new Runnable() {
            @Override
            public void run()
            {
                String result = "每隔2秒更新一下数据：";
                result += Math.random();
                tvMain.setText(result);
            }
        });

    } catch (InterruptedException e)
    {
        e.printStackTrace();
    }

}
```

## IntentService

### 使用场景

Android中的Service是用于后台服务的，当应用程序被挂到后台的时候，问了保证应用某些组件仍然可以工作而引入了Service这个概念，那么这里面要强调的是Service不是独立的进程，也不是独立的线程，它是依赖于应用程序的主线程的，也就是说，在更多时候不建议在Service中编写耗时的逻辑和操作，否则会引起ANR

那么我们当我们编写的耗时逻辑，不得不被service来管理的时候，就需要引入IntentService，IntentService是继承Service的，那么它包含了Service的全部特性，当然也包含service的生命周期，那么与service不同的是，IntentService在执行onCreate操作的时候，内部开了一个线程，在onHandleIntent中执行耗时操作

由于IntentService是服务，优先级高，不容易被系统杀死，适合执行一些高优先级的任务

### 使用

```java
public class MyIntentService extends IntentService {
    public MyIntentService() {
        // 获取当前类名传入super
        // 类似的可以用getMethodName获取当前方法名
        super(Thread.currentThread().getStackTrace()[1].getClassName);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        // ...
    }
}

Intent service = new Intent(this, MyIntentService.class);
service.putExtra("args", "args1");
startService(service); // 可以多次运行，每次都会调用onHandleIntent
```

### 原理

IntentService在执行onCreate的方法的时候，其实开了一个线程HandlerThread,并获得了当前线程队列管理的looper，并且在onStart的时候，把消息置入了消息队列

在消息被handler接受并且回调的时候，执行了onHandlerIntent方法，该方法的实现是子类去做的

由于每启动一个后台任务就必须启动一次IntentService，而IntentService内部通过消息方式向HandlerThread请求执行任务，Handler的Looper是顺序处理消息的，所以IntentService也是顺序执行后台任务的，按照外界发起的顺序

`stopSelf()`会立刻停止服务

`stopSelf(int statId)`在尝试停止服务之前会判断最近启动服务的次数是否和statId相等，如果相等则停止服务，否则不停止

[Android中IntentService与Service的区别](http://blog.csdn.net/matrix_xu/article/details/7974393)

## 启动和绑定过程

### 启动

1. **startService**，ContextImpl#startService，startServiceCommon，**AMS#startService**
2. AMS通过ActiveServices#startServiceLocked，startServiceInnerLocked，bringUpServiceLocked，realStartServiceLocked，**ApplicationThread#scheduleCreateServcice**，创建Service对象并调用其onCreate，通过sendServiceArgsLocked调用其他生命周期方法
3. 与启动Activity类似，最终由H#handleCreateService
   1. 加载Service类并创建实例
   2. 创建Application对象并调用onCreate
   3. 创建ContextImpl并通过Service的attach建立联系
   4. 调用Service的onCreate
   5. sendServiceArgsLocked调用其他生命周期方法

### 绑定

1. **bindService**，ContextImpl#bindService，bindServiceCommon，**AMS#bindService**

   在此过程中，客户端的ServiceConnection会转化为ServiceDispatcher.InnerConnection的Binder对象

2. 与启动过程类似，最终会调用ApplicationThread#scheduleBindService，ActiveServices#requestServiceBindingLocked，**scheduleBindService**，**H#handleBindService**

3. **H#handleBindService**通过AMS#publishService通知客户端已经成功连接Service，其中RunConnection#run会将ServiceConnection对象和Service对象绑定，调用Service#onBind

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

> lockCanvas()获取到的Canvas对象还是上次的Canvas对象，并不是一个新的对象。之前的绘图都将被保留，如果需要擦除，可以在绘制之前通过drawColor()方法来进行清屏

绘制要充分利用SurfaceView的三个回调方法，在`surfaceCreate()`方法中开启子线程进行绘制。在子线程中，使用一个`while(isDrawing)`循环来不停地绘制。具体的绘制过程，由`lockCanvas()`方法进行绘制，并通过`unlockCanvasAndPost(mCanvas)`进行画布内容的提交

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

# View工作原理

## DecorView和ViewRoot

### DecorView

![image](http://upload-images.jianshu.io/upload_images/2397836-f1f6a200704884a2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240&_=6219915)

DecorView是一个应用窗口的根容器，它本质上是一个FrameLayout。DecorView有唯一一个子View，它是一个垂直LinearLayout，包含两个子元素，一个是TitleView（ActionBar的容器），另一个是ContentView（窗口内容的容器）。关于ContentView，它是一个FrameLayout（android.R.id.content)，我们平常用的setContentView就是设置它的子View。上图还表达了每个Activity都与一个Window（具体来说是PhoneWindow）相关联，用户界面则由Window所承载

### ViewRoot

View的绘制是由ViewRoot来负责的。每个应用程序窗口的decorView都有一个与之关联的ViewRoot对象，这种关联关系是由WindowManager来维护的。Activity启动时，`ActivityThread.handleResumeActivity()`方法中建立了ViewRoot和decorView的关联关系。

当建立好了decorView与ViewRoot的关联后，ViewRoot类的`requestLayout()`方法会被调用，以完成应用程序用户界面的初次布局。实际被调用的是ViewRootImpl类的`requestLayout()`方法

```java
@Override
public void requestLayout() {
    if (!mHandlingLayoutInLayoutRequest) {
        // 检查发起布局请求的线程是否为主线程 
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
}
```

上面的方法中调用了`scheduleTraversals()`方法来调度一次完成的绘制流程，该方法会向主线程发送一个“遍历”消息，最终会导致ViewRootImpl的`performTraversals()`方法被调用，开始View绘制的以下三个阶段

## MeasureSpec和LayoutParams

### 概念

MeasureSpec代表一个32位的int值，用于决定View的宽高

* 高2位代表SpecMode：测量模式
* 低30位代表SpecSize：某种测量模式下的规格大小

SpecMode有3类：

* UNSPECIFIED：父容器不对View有任何限制，一般用于系统内部，表示一种测量状态
* EXACTLY：父容器已经检测出View所需的精确大小，即SpecSize指定的值，对应于LayoutParams中的match_parent和具体数值两种模式
* AT_MOST：父容器指定了一个可用大小即SpecSize，View大小不能大于这个值，对应于LayoutParams的wrap_content

### MeasureSpec和LayoutParams的关系

* 对于顶级View，MeasureSpec由窗口尺寸和自身的LayoutParams共同决定
* 对于普通View，MeasureSpec由父容器的MeasureSpec和自身的LayoutParams共同决定

### 普通View的MeasureSpec创建规则

| childLayoutParams\parentSpecMode | EXACTLY               | AT_MOST               | UNSPECIFILED         |
| -------------------------------- | --------------------- | --------------------- | -------------------- |
| dp/px                            | EXACTLY<br>childSize  | EXACTLY<br>childSize  | EXACTLY<br>childSize |
| match_parent                     | EXACTLY<br>parentSize | AT_MOST<br>parentSize | UNSPECIFILED<br>0    |
| wrap_content                     | AT_MOST<br>parentSize | AT_MOST<br>parentSize | UNSPECIFILED<br>0    |

## View的绘制过程

View的工作流程主要是指measure、layout、draw这三大流程，即测量、布局和绘制，其中measure确定View的测量宽/高，layout确定View的最终宽/高和四个顶点的位置，而draw则将View绘制到屏幕上

![image](http://upload-images.jianshu.io/upload_images/2397836-19c08de6439514a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240&_=6219915)

### measure

计算出控件树中的各个控件要显示其内容的话，需要多大尺寸

#### View的measure过程

直接继承View的自定义控件需要重写`onMeasure方法`并设置wrap_content时的自身大小，否则使用wrap_content相当于match_parent

> 如果View在不居中使用wrap_content，那么它的SpecMode是AT_MOST模式，在这种模式下，它的宽高等于specSize，即此时就相当于parentSize，也就是父容器当前剩余的大小

解决方法是给View指定一个默认的内部宽高（mWidth和mHeight），并在wrap_content时设置此宽高。对于非wrap_content的情形，沿用系统的测量值。

```java
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
    if (widthSpecMode == MeasureSpec.AT_MOST
        && heightSpecMode == MeasureSpec.AT_MOST) {
        setMeasuredDimension(mWidth, mHeight);
    } else if (widthSpecMode == MeasureSpec.AT_MOST) {
        setMeasuredDimension(mWidth, heightSpecSize);
    } else if (heightSpecMode == MeasureSpec.AT_MOST) {
        setMeasuredDimension(widthSpecSize, mHeight);
    }
}
```

#### ViewGroup的measure过程

除了完成自己的测量过程外，还会遍历去调用所有子元素的measure方法，各个子元素再递归去执行这个流程

#### Activity实时获取View的宽高

在onCreate，onStart，onResume中均无法正确获取某个View的宽高，因为View的measure过程和Activity的生命周期方法不是同步执行的，因此无法保证Activity执行了onCreate，onStart，onResume时某个View已经测量完毕，如果还没有测量完毕，获取的宽高就是0

解决方法

* `Activity/View#onWindowFocusChanged`：此时View已经初始化完毕，宽高已经准备好，一定能获取到
* `View.post(runnable)`：通过post可以将一个runnable投递到消息队列的尾部，然后等待Looper调用此runnable时，View也已经初始化好了
* `ViewTreeObserver`：比如使用ViewTreeObserver的onGlobalLayoutListener回调方法，当View树状态改变或View树内部View可见性改变时，onGlobalLayout将被回调，此时可以获取View的宽高
* `view.measure()`：手动对View进行measure，根据View的LayoutParams分情况处理
  * match_parent：直接放弃，此种方法需要知道父容器parentSize，而此时无法知道
  * 具体数值或wrap_content：可行

### layout

layout的作用是ViewGroup用来确定子元素的位置，当ViewGroup的位置被确定为后，它在onLayout中会遍历所有的子元素并调用其layout方法

layout方法的大致流程如下：首先通过setFrame方法来设定View的四个顶点的位置，即初始化mLeft、mTop、mRight和mBottom四个参数，这四个参数描述了View相对其父View的位置。在setFrame()方法中会判断View的位置是否发生了改变，若发生了改变，则需要对子View进行重新布局，对子View的局部是通过onLayout()方法实现了

### draw

将View绘制到屏幕上面

绘制过程的传递是通过`dispatchDraw`实现的，dispatchDraw会遍历调用所有子元素的`draw`方法

```java
public void draw(Canvas canvas) {
    // ...
    // 绘制背景，只有dirtyOpaque为false时才进行绘制，下同
    if (!dirtyOpaque) {
        drawBackground(canvas);
    }

    // ...

    // 绘制自身内容
    if (!dirtyOpaque) onDraw(canvas);

    // 绘制子View
    dispatchDraw(canvas);

    // ...
    // 绘制滚动条等装饰
    onDrawForeground(canvas);

}
```

默认情况下ViewGroup不启用setWillNotDraw，不绘制任何内容，系统可以进行相应的优化。如果需要绘制，需要显式关闭WILL_NOT_DRAW标记位

## 自定义View

### 通常情况

1. 继承View重写onDraw：如果想控制View在屏幕上的渲染效果，就在重写`onDraw()`方法，在里面进行相应的处理，如处理wrap_content和padding；
2. 继承ViewGroup实现自定义布局：重点处理onMeasure和onLayout过程
3. 继承特定View：扩展已有View的功能，例如TextView，一般不需要处理wrap_content和padding
4. 继承特定ViewGroup：扩展已有ViewGroup功能

**关键点：**

1. 让View支持wrap_content和padding
2. 尽量不要在View中使用Handler
3. View中如果有线程或动画，需要在`onDetachFromWindow`中及时停止，否则可能导致内存泄漏
4. 处理好滑动冲突

**其他方面：**

1. 如果想要控制用户同View之间的交互操作，则在onTouchEvent()方法中对手势进行控制处理。
2. 如果想要控制View中内容在屏幕上显示的尺寸大小，就重写onMeasure()方法中进行处理。
3. 在 XML文件中设置自定义View的XML属性。
4. 如果想避免失去View的相关状态参数的话，就在`onSaveInstanceState() `和` onRestoreInstanceState()`方法中保存有关View的状态信息。

### 自定义属性

1. 在values目录下创建自定义属性的XML，如attrs.xml

   例如指定格式为`color`的属性`circle_color`

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <declare-styleable name="CircleView">
           <attr name="circle_color" format="color"/>
       </declare-styleable>
   </resources>
   ```

2. 在View的构造方法中解析自定义属性的值并做相应处理

   首先接在自定义属性集合CircleView，接着解析其中的`circle_color` 属性，id为`R.styleable.CircleView_circle_color` ，选取红色作为默认值

   ```java
   public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
       super(context, attrs, defStyleAttr);
       TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
       mColor = a.getColor(R.styleable.CircleView_circle_color, Color.RED);
       a.recycle();
       init();
   }
   ```

3. 在布局文件中使用自定义属性

   ```xml
   app:circle_color="@color/light_green"
   ```

> 为了使用自定义属性，必须在布局文件中添加schemas声明：`xmlns:app=http://schemas.android.com/apk/res-auto` 。在这个声明中，app是自定义属性的前缀，可以换成其他名字，但必须和CircleView中的自定义属性前缀保持一致

## 实例：实现自动换行的ViewGroup

效果图：

![](http://www.jcodecraeer.com/uploads/allimg/130305/22540UU5-0.png)

自定义一个viewgroup，在onlayout里面自动检测view的右边缘的横坐标值，判断是否换行显示view就可以了

```java
public class AutoLinefeedLayout extends ViewGroup {  

    public AutoLinefeedLayout(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  

    public AutoLinefeedLayout(Context context, AttributeSet attrs) {  
        this(context, attrs, 0);  
    }  

    public AutoLinefeedLayout(Context context) {  
        this(context, null);  
    }  

    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b) {  
        layoutHorizontal();  
    }  

    private void layoutHorizontal() {  
        final int count = getChildCount();  
        final int lineWidth = getMeasuredWidth() - getPaddingLeft()  
            - getPaddingRight();  
        int paddingTop = getPaddingTop();  
        int childTop = 0;  
        int childLeft = getPaddingLeft();  

        int availableLineWidth = lineWidth;  
        int maxLineHight = 0;  

        for (int i = 0; i < count; i++) {  
            final View child = getChildAt(i);  
            if (child == null) {  
                continue;  
            } else if (child.getVisibility() != GONE) {  
                final int childWidth = child.getMeasuredWidth();  
                final int childHeight = child.getMeasuredHeight();  

                if (availableLineWidth < childWidth) {  
                    availableLineWidth = lineWidth;  
                    paddingTop = paddingTop + maxLineHight;  
                    childLeft = getPaddingLeft();  
                    maxLineHight = 0;  
                }  
                childTop = paddingTop;  
                setChildFrame(child, childLeft, childTop, childWidth,  
                              childHeight);  
                childLeft += childWidth;  
                availableLineWidth = availableLineWidth - childWidth;  
                maxLineHight = Math.max(maxLineHight, childHeight);  
            }  
        }  
    }  

    private void setChildFrame(View child, int left, int top, int width,  
                               int height) {  
        child.layout(left, top, left + width, top + height);  
    }  

    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);  
        int count = getChildCount();  
        for (int i = 0; i < count; i++) {  
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);  
        }  
        if (heightMode == MeasureSpec.AT_MOST||heightMode == MeasureSpec.UNSPECIFIED) {  
            final int width = MeasureSpec.getSize(widthMeasureSpec);  
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(  
                getDesiredHeight(width), MeasureSpec.EXACTLY));  
        } else {  
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
        }  
    }  

    private int getDesiredHeight(int width) {  
        final int lineWidth = width - getPaddingLeft() - getPaddingRight();  
        int availableLineWidth = lineWidth;  
        int totalHeight = getPaddingTop() + getPaddingBottom();  
        int lineHeight = 0;  
        for (int i = 0; i < getChildCount(); i++) {  
            View child = getChildAt(i);  
            final int childWidth = child.getMeasuredWidth();  
            final int childHeight = child.getMeasuredHeight();  
            if (availableLineWidth < childWidth) {  
                availableLineWidth = lineWidth;  
                totalHeight = totalHeight + lineHeight;  
                lineHeight = 0;  
            }  
            availableLineWidth = availableLineWidth - childWidth;  
            lineHeight = Math.max(childHeight, lineHeight);  
        }  
        totalHeight = totalHeight + lineHeight;  
        return totalHeight;  
    }  

}  
```

[教你搞定Android自定义View](https://www.jianshu.com/p/84cee705b0d3)

[Android自定义View的三种实现方式](https://www.cnblogs.com/jiayongji/p/5560806.html)

[自定义View，有这一篇就够了](https://www.jianshu.com/p/c84693096e41)

[Android 自动换行的LinearLayout](http://blog.csdn.net/sun_leilei/article/details/49740575)

[android之自定义ViewGroup实现自动换行布局](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2013/0305/969.html)

[Android最简洁的自动换行布局组件](http://blog.csdn.net/u011192530/article/details/53019212)

# View事件体系

## 基础概念

### 坐标

相对父容器的坐标

* left，top：View左上角的坐标
* right，bottom：View右下角坐标

其他坐标

* translationX，translationY是View左上角相对于父容器的偏移量，默认值为0，提供了get/set方法

* x，y是View左上角的坐标

  x = left + translationX，y = top + translationY

### 事件类型

* ACTION_DOWN：刚接触屏幕
* ACTION_UP：从屏幕上松开的一瞬间
* ACTION_MOVE：在屏幕上滑动

典型事件序列：DOWN->MOVE->...->MOVE->UP

### TouchSlop

系统可以识别的最小滑动距离：`ViewConfiguration.get(getContext()).getScaledTouchSlop()`

## View的滑动

* scrollTo/scrollBy：操作简单，适合滑动View内容
* 动画：操作简单，适合没有交互的View和实现复杂的动画效果
* 改变布局参数：操作复杂，适合有交互的View

### 使用scrollTo/scrollBy

```java
public void scrollTo(int x, int y);
public void scrollBy(int x, int y);
```

scrollBy实际上也是调用了scrollTo，以下只讨论scrollTo

scrollTo只能改变View内容的位置而不能改变View在布局中的位置

左->右：x为负值

上->下：y为负值

### 使用动画

通过动画让一个View平移，主要是操作View的tranlationX和translationY属性

例如，让一个View在100ms内从原始位置向右平移100像素

```java
ObjectAnimator.ofFloat(targetView, "translationX", 0, 100).setDuration(100).start();
```

View动画是对View的影像做操作，不能真正改变VIew的位置参数，设置fillAfter属性为true可以保存动画后状态。使用属性动画不存在这个问题

### 改变布局参数

即改变LayoutParams，例如将一个Button向右平移100像素

```java
MarginLayoutParams params = (MarginLayoutParams) mButton.getLayoutParams();
params.width += 100;
params.leftMargin += 100;
mButton.requestLayout();
// 或mButton.setLayoutParams(params);
```

### 弹性滑动

将一次大的滑动分成若干次小的滑动并在一段时间内完成

* 使用Scroller：配合View的computeScroll，不断让View重绘，而每一次重绘距离滑动起始时间会有一个时间间隔，Scroller通过这个时间间隔获得View当前的滑动位置，知道了位置就可以通过scrollTo方法完成View的滑动
* 使用动画：在动画的每一帧到来时获取动画完成的比例，根据比例计算出当前View要滑动的距离
* 使用延时策略：通过发送一系列延时消息从而达到渐进式效果，可以使用Handler或View的postDelayed，也可以使用线程的sleep

## 事件分发机制

### 核心方法

* `dispatchTouchEvent(MotionEvent ev)`：用来进行事件分发
  * 如果事件能传递给当前View则一定会被调用
  * 返回结果受当前View的`onTouchEvent`和下级View的`dispatchTouchEvent`影响，表示是否消耗当前事件
* `onInterceptTouchEvent(MotionEvent ev)`：判断是否拦截某个事件
  * 在`dispatchTouchEvent`中调用
  * 如果当前View拦截了某个事件，在同一事件序列中不会被再次调用
  * 返回结果表示是否拦截事件
* `onTouchEvent(MotionEvent ev)`：处理点击事件
  * 在`dispatchTouchEvent`中调用
  * 返回结果表示是否消耗当前事件，如果不消耗，当前View无法再次接收到事件

三者的关系（伪代码）

```java
public boolean dispatchTouchEvent(MotionEvent ev) {
    boolean consume = false;
    if (onInterceptTouchEvent(ev)) {
        consume = onTouchEvent(ev);
    } else {
        consume = child.dispatchTouchEvent(ev);
    }
    return consume;
}
```



### 原理

事件分发流程图分为3层，从上往下依次是Activity、ViewGroup、View

![img](https://upload-images.jianshu.io/upload_images/966283-b9cb65aceea9219b.png)

* 事件从左上角开始，由Activity的`dispatchTouchEvent`做分发
* 箭头的上面字代表方法返回值，（return true、return false、return super.xxxxx(),super 的意思是调用父类实现）
* `dispatchTouchEvent`和 `onTouchEvent`的框里有个【**true---->消费**】的字，表示的意思是如果方法返回true，那么代表事件就此消费，不会继续往别的地方传了，事件终止
* 目前图中的事件是仅仅针对ACTION_DOWN的
* 只有`return super.dispatchTouchEvent(ev) `才是往下走，返回true 或者 false 事件就被消费了（终止传递）

### 关键点

* 默认实现流程
  * 整个事件流向应该是从Activity---->ViewGroup--->View 从上往下调用`dispatchTouchEvent`方法，一直到叶子节点（View）的时候，再由View--->ViewGroup--->Activity从下往上调用`onTouchEvent`方法
  * ViewGroup 和View的这些方法的默认实现就是会让整个事件安装U型完整走完，所以` return super.xxxxxx() `就会让事件依照U型的方向的完整走完整个事件流动路径）
* `dispatchTouchEvent`，`onTouchEvent`的返回值
  * true：终结事件传递
  * false：回溯到父View的`onTouchEvent`方法
* 拦截器`onInterceptTouchEvent`
   * ViewGroup 把事件分发给自己的`onTouchEvent`，需要拦截器`onInterceptTouchEvent`方法return true 把事件拦截下来。
   * ViewGroup 的拦截器`onInterceptTouchEvent `默认不拦截，即return false；
   * View 没有拦截器，为了让View可以把事件分发给自己的`onTouchEvent`，View的`dispatchTouchEvent`默认实现（super）就是把事件分发给自己的`onTouchEvent`。
* 正常情况下，一个事件序列只能被一个View拦截且消耗
  * ACTION_DOWN事件
    * 在`dispatchTouchEvent`消费：事件到此为止停止传递
    * 在`onTouchEvent`消费：把ACTION_MOVE或ACTION_UP事件传给该View的`onTouchEvent`处理并结束传递
    * 没有被消耗：同一事件序列的其他事件都不会再交给该View
  * View的`onTouchEvent`默认都会消耗事件，除非clickable和longClickable同时为false，enable属性不影响
  * View可以通过`requestDisallowInterceptTouchEvent`干预父元素的事件分发，但是ACTION_DOWN除外
* `onTouchListene`r的优先级比`onTouchEvent`要高，其中会调用`onTouch`方法，并屏蔽`onTouchEvent`

[图解 Android 事件分发机制](https://www.jianshu.com/p/e99b5e8bd67b)

## 滑动冲突处理

### 常见场景

1. 外部滑动方向和内部滑动方向不一致

   例如：可以通过左右滑动切换页面，而每个页面内部又是ListView

   根据滑动特征解决，即根据水平滑动还是竖直滑动判断该由谁拦截事件

2. 外部滑动方向和内部滑动方向一致

   例如：内外两层都是上下滑动

   根据业务特点判断

3. 上述两种情况的嵌套，同样根据业务特点判断

### 外部拦截

特点：子View代码无需修改，符合View事件分发机制

操作：需要在父ViewGroup，重写`onInterceptTouchEvent`方法，根据业务需要，判断哪些事件是父Viewgroup需要的，需要的话就对该事件进行拦截，然后交由`onTouchEvent`方法处理，若不需要，则不拦截，然后传递给子View或子ViewGroup

```java
public boolean onInterceptTouchEvent(MotionEvent ev) {
    boolean isIntercept = false;
    int x = (int) ev.getX();
    int y = (int) ev.getY();

    switch (ev.getAction()){
        case MotionEvent.ACTION_DOWN:
            // 必须返回false，否则事件无法传递给子容器
            isIntercept = false;

            // 如果用户正在水平滑动，但在水平滑动停止之前进行了竖直滑动，
            // 则会导致界面在水平方向无法滑动到终点，
            // 因此需要父容器拦截，从而优化滑动体验
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
                isIntercept = true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (父容器需要当前事件) {
                isIntercept = true;
            } else {
                isIntercept = false;
            }
            break;
        case MotionEvent.ACTION_UP:
            isIntercept = false;
            break;
    }
    mLastXIntercept = x;
    mLastYIntercept = y;
    return isIntercept;         //返回true表示拦截，返回false表示不拦截
}
```

### 内部拦截

特点：父Viewgroup需要重写`onInterceptTouchEvent`，不符合View事件分发机制

操作：在子`View`中拦截事件，父ViewGroup默认是不拦截任何事件的，所以，当事件传递到子View时， 子View根据自己的实际情况来，如果该事件是需要子View来处理的，那么子view就自己消耗处理，如果该事件不需要由子View来处理，那么就调用`getParent().requestDisallowInterceptTouchEvent()`方法来通知父Viewgroup来拦截这个事件，也就是说，叫父容器来处理这个事件，这刚好和View的分发机制相反

子View

```java
public boolean dispatchTouchEvent(MotionEvent ev) {
    int x = (int) ev.getX();
    int y = (int) ev.getY();
    switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            getParent().requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_MOVE:
            int deltaX = x - mLastX;
            int deltaY = y - mLastY;

            if (父容器需要此类事件) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            break;
        case MotionEvent.ACTION_UP:
            break;
    }
    mLastX = x;
    mLastY = y;
    return super.dispatchTouchEvent(ev);
}
```

或

```java
public boolean onTouch(View v, MotionEvent event) {
    switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            pager.requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            pager.requestDisallowInterceptTouchEvent(false);
            break;
    }
}
```

> 例如：ViewPager来实现左右滑动切换tab，如果tab的某一项中嵌入了水平可滑动的View就会让你有些不爽，比如想滑动tab项中的可水平滑动的控件，却导致tab切换。
>
> 因为Android事件机制是从父View传向子View的，可以去检测你当前子View是不是在有可滑动控件等，决定事件是否拦截，但是这个比较麻烦，而且并不能解决所有的问题（必须检测触摸点是否在这个控件上面），其实有比较简单的方法，在你嵌套的控件中注入ViewPager实例（调用控件的getParent()方法），然后在onTouchEvent，onInterceptTouchEvent，dispatchTouchEvent里面告诉父View，也就是ViewPager不要拦截该控件上的触摸事件

父ViewGroup

```java
public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN){
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isIntercept = true;
        }
        return false;
    } else {
        return true;
    }
}
```

[View滑动冲突处理方法（外部拦截法、内部拦截法）](http://blog.csdn.net/z_l_p/article/details/53488085)

[Android事件冲突场景分析及一般解决思路](https://www.jianshu.com/p/c62fb2f25057)

[用requestDisallowInterceptTouchEvent()方法防止viewpager和子view冲突](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2013/0803/1500.html)

# Window

即窗口，一般通过WindowManager，用于在桌面显示悬浮窗

## 用法

将一个Button添加到屏幕坐标为(100, 300)的位置上

```java
mFloatingButton = new Button(this);
mFloatingButton.setText("click me");
mLayoutParams = new WindowManager.LayoutParams(
    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0,
    PixelFormat.TRANSPARENT);
mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_SHOW_WHEN_LOCKED;
mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
mLayoutParams.x = 100;
mLayoutParams.y = 300;
mFloatingButton.setOnTouchListener(this);
mWindowManager.addView(mFloatingButton, mLayoutParams);
```

Flags参数，表示Window的属性，控制显示特性

1. `FLAG_NOT_FOCUSABLE`：不需要获取焦点，也不需要接收各种输入事件，默认启动`FLAG_NOT_TOUCH_MODAL`，事件会传递给下层具有焦点的Window
2. `FLAG_NOT_TOUCH_MODAL`：将当前Window区域的单击事件传给底层的Window，区域以内的自己处理
3. `FLAG_SHOW_WHEN_LOCKED`：让Window显示在锁屏界面上

type参数，表示Window的类型，控制显示层级

* 默认层级：应用Window 1-99，子Window 1000-1999，系统Window 2000-2999，数字越大，层级越高，显示越靠上
* 显示为系统层级，需要指定为`TYPE_SYSTEM_OVERLAY`或`TYPE_SYSTEM_ERROR`，同时声明权限`<use-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>`

实现可以拖动的Window

```java
mFloatingButton.setOnTouchListener(this);

// onTouch()
@Override
public boolean onTouch(View v, MotionEvent event) {
    int rawX = (int) event.getRawX();
    int rawY = (int) event.getRawY();
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            // 获得点击事件在屏幕上的绝对坐标，然后更新LayoutParams
            int x = (int) event.getX();
            int y = (int) event.getY();
            mLayoutParams.x = rawX;
            mLayoutParams.y = rawY;
            mWindowManager.updateViewLayout(mFloatingButton, mLayoutParams);
            break;
        }
        case MotionEvent.ACTION_UP: {
            break;
        }
        default:
            break;
    }

    return false;
}
```

## 原理

每一个Window都对应一个View和一个ViewRootImpl，Window和View通过ViewRootImpl建立联系

Window以View的形式存在，通过WindowManager接口提供`addView`，`updateViewLayout`和`removeView`方法

WindowManager接口的实现类是WindowManagerImpl，其中又交给了WindowManagerGlobal来处理，这是典型的桥接模式。

WindowManagerGlobal以工厂形式向外提供实例，这是典型的工厂模式。

### Window的添加过程

1. 检查参数是否合法，如果是子Window还需要调整一些布局参数

2. 创建ViewRootImpl并将View添加到列表中

3. 通过ViewRootImpl更新界面并完成Window的添加过程

   > 该步骤通过ViewRootImpl的setView方法来完成，即View的绘制过程，其中会调用requestLayout完成异步刷新请求，scheduleTraversals正是View绘制的入口
   >
   > 完成绘制后会通过WindowSession完成Window的添加过程，WindowSession的实现类是Session，返回一个Binder对象，所以这是一个IPC过程，最终通过WindowManagerService实现Window的添加

### Window的删除过程

与添加过程类似，通过WIndowManagerImpl后再通过WindowManagerGlobal实现

1. 通过findViewLocked查找待删除View的索引，调用removeViewLocked删除

2. 主要使用异步删除：调用removeView，其中调用了ViewRootImpl的die方法，发送了请求删除的消息，将其加入mDyingViews中，再调用doDie->dispatchDetachedFromWindow

3. dispatchDetachedFromWindow：垃圾回收

   ->Session.remove（IPC）->View#onDetachedFromWindow（资源回收）

   ->WindowManagerGlobal#doRemoveView（刷新数据）

### Window的更新过程

WindowManagerGlobal#updateViewLayout

1. 更新View和ViewRootImpl的LayoutParams
2. 调用scheduleTraversals重新布局
3. 通过WindowSession更新Window视图

## Window的创建过程

### Activity的Window创建过程

* Activity创建，`Activity#performLaunchActivity()`
* `activity#attach()`
* `PolicyManager#makeNewWindow()`
* `Activity#setContentView()`

> setContentView的步骤：
>
> 1. 如果没有DecorView则创建
> 2. 将View添加到DecorView的mContentParent中
> 3. 回调Activity的onContentChanged通知Activity视图已经发生改变

### Dialog的Window创建过程

1. 创建Window
2. 初始化DecorView并将Dialog的视图添加到DecorView中
3. 将DecorView添加到Window中并显示

> 普通Dialog需要Activity的context，而不能使用Application的context，因为Dialog需要应用token，而只有Activity才有
>
> 系统Window比较特殊，它不需要token

### Toast的Window创建过程

Toast的显示和消失都需要通过NotificationManagerService（NMS）来实现，由于NMS在系统进程中，需要采用IPC方式调用

NMS会跨进程调用Toast中TN中的方法，由于TN运行在Binder线程池中（TN本身就是一个Binder对象），需要通过Handler将其切换到当前进程中，所以Toast无法在没有Looper的线程中弹出。

# 线程和消息机制

## 消息机制

Handler运行机制及Handler附带的MessageQueue和Looper工作过程

Handler的主要作用是将一个任务切换到某个指定的线程中执行

> Android规定访问UI只能在主线程中进行，如果子线程访问则抛出异常。因为UI控件不是线程安全的，多线程并发访问可能会导致UI处于不可预期状态，而使用锁机制则会导致代码逻辑复杂，效率降低，所以采用单线程模型控制
>
> 通常在子线程中执行耗时操作，切换到主线程更新UI

![](http://img.blog.csdn.net/20140805002935859?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbG1qNjIzNTY1Nzkx/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### MessageQueue

单链表结构存储

`enqueueMessage`：向消息队列中插入一条消息

`next`：无限循环，直到从消息队列中取出一条消息并将其从消息队列中移除

### Looper

### 主要函数

`Looper`：构造方法，创建一个MessageQueue，并将当前线程对象保存

```java
private Looper(boolean quitAllowed) {
    mQueue = new MessageQueue(quitAllowed);
    mThread = Thread.currentThread();
}
```

`prepare`：为当前线程创建一个Looper

`loop`：开启消息循环

```java
public static void loop() {
    final Looper me = myLooper();
    final MessageQueue queue = me.mQueue();
    // ...
    for (;;) {
        Message msg = queue.next();
        if (msg == null) {
            // 如果next返回null则跳出循环
            return;
        }
        // ...
        msg.target.dispatchMessage(msg); // target为发送这条消息的Handler
        // ...
    }
}
```

`prepareMainLooper`：为主线程即ActivityThread创建Looper，本质也是通过prepare实现的

`getMainLooper`：获取主线程的Looper

`quit`：直接退出Looper

`quitSafely`：设定退出标记，等消息队列中已有消息处理完毕后退出

> 在子线程中，如果手动创建了Looper，在所有事情完成后应调用quit，否则线程会一直处于等待状态
>
> 退出Looper后，线程会立刻终止

#### Android系统是如何保证一个线程只有一个Looper的

`Looper.prepare()`使用了ThreadLocal来保证一个线程只有一个Looper

ThreadLocal为每个线程存储当前线程的Looper，线程默认没有Looper，需要创建

> ThreadLocal实现了线程本地存储。所有线程共享同一个ThreadLocal对象，但不同线程仅能访问与其线程相关联的值，一个线程修改ThreadLocal对象对其他线程没有影响，而其他线程也无法获取该线程的数据
>
> 当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本
>
> 不同线程访问同一个ThreadLocal的get方法，ThreadLocal内部会从各自的线程中取出一个数据，然后根据当前ThreadLocal的索引去查找对应的value，而不同线程的数据是不同的
>
> ![这里写图片描述](http://img.blog.csdn.net/20160401173413434)
>
> ```java
> public void set(T value) {
>     Thread currentThread = Thread.currentThread();
>     Values values = values(currentThread);
>     if (values == null) {
>         values = initializeValues(currentThread);
>     }
>     values.put(this, value);
> }
>
> public void get() {
>     Thread currentThread = Thread.currentThread();
>     Values values = values(currentThread);
>     if (values != null) {
>         Object[] table = values.table;
>         int index = hash & values.mask;
>         if (this.reference == table[index]) {
>             return (T) table[index + 1];
>         }
>     } else {
>         values = initializeValues(currentThread);
>     }
>     return (T) values.getAfterMiss(this);
> }
> ```

[Android如何保证一个线程最多只能有一个Looper？](http://blog.csdn.net/sun927/article/details/51031268)

#### 主线程消息循环——为什么主线程不会因为Looper.loop()方法造成阻塞

ActivityThread通过ApplicationThread和AMS进行进程间通信，AMS以进程间通信的方式完成ActivityThread的请求后回调ApplicationThread的Binder方法，然后ApplicationThread会向H发送消息，H收到消息后将ApplicationThread中的逻辑切换到ActivityThread中执行，即主线程

Android应用程序的主线程在进入消息循环过程前，会在内部创建一个Linux管道（Pipe），这个管道的作用是使得Android应用程序主线程在消息队列为空时可以进入空闲等待状态，并且使得当应用程序的消息队列有消息需要处理时唤醒应用程序的主线程

在代码`ActivityThread.main()`中

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

`thread.attach(false) `会创建一个Binder线程（具体是指ApplicationThread，Binder的服务端，用于接收系统服务AMS发送来的事件），该Binder线程通过Handler将Message发送给主线程

ActivityThread实际上并非线程，不像HandlerThread类，ActivityThread并没有真正继承Thread类，只是往往运行在主线程，该人以线程的感觉，其实承载ActivityThread的主线程就是由Zygote fork而创建的进程。ActivityThread的内部类H继承于Handler，代码如下：

```java
public void handleMessage(Message msg) {
    if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
    switch (msg.what) {
        case LAUNCH_ACTIVITY:
            //...
        case RELAUNCH_ACTIVITY:
            //...
        case PAUSE_ACTIVITY:
            //...
        case PAUSE_ACTIVITY_FINISHING:
            //...
        case STOP_ACTIVITY_SHOW:
            //...
        case STOP_ACTIVITY_HIDE:
            //...
        case SHOW_WINDOW:
            //...
        case HIDE_WINDOW:
            //...
        case RESUME_ACTIVITY:
            //...
        case SEND_RESULT:
            //...

            //...
    }
```

Activity的生命周期都是依靠主线程的`Looper.loop`，当收到不同Message时则采用相应措施：
在`H.handleMessage(msg)`方法中，根据App进程中的其他线程通过Handler发送给主线程的msg，执行相应的生命周期

[Android中为什么主线程不会因为Looper.loop()方法造成阻塞](http://blog.csdn.net/u013435893/article/details/50903082)

[Android中为什么主线程不会因为Looper.loop()里的死循环卡死](https://www.zhihu.com/question/34652589)

#### 手动创建Looper

实现一个类似于HandlerThread的类，即具有Looper的Thread，同时提供管理Looper的功能

```java
class LooperThread extends Thread {
    private Looper mLooper;
    public LooperThread() {
        
    }
    
    @Override
    public void run() {
        Looper.prepare(); // 创建消息队列
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Looper.loop(); // 开启消息循环
    }
    
    public Looper getLooper() {
        return mLooper;
    }
    
    public void stopLooper() {
        mLooper.quitSafely();
    }
} 
```

### Handler

#### 常用函数

`Handler(Callback callback)`：使用特定Callback接口创建Handler，需要实现`handleMessage(Message msg)`

`Handler(Looper looper)`：使用特定Looper创建Handler

`sendMessage`：向消息队列中插入一条消息

`post`：向Handler的Looper中投入一个Runnable对象等待处理

`postDelayed`：向Handler的Looper中投入一个Runnable对象并在指定时延后处理

`dispatchMessage`：处理消息

![andler消息处理流程图](D:\Lizij\Document\LearningNotes\Android\images\Handler消息处理流程图1.png)

#### postDelayed的原理

##### 精确计时的

在`MessageQueue.next()`中，如果头部的这个Message是有延迟而且延迟时间没到的（now < msg.when），会计算一下时间（保存为变量`nextPollTimeoutMillis`），然后在循环开始的时候判断如果这个Message有延迟，就调用`nativePollOnce(ptr, nextPollTimeoutMillis)`进行阻塞。`nativePollOnce()`的作用类似与`object.wait()`，只不过是使用了Native的方法对这个线程精确时间的唤醒

##### 多个带有时延Runnable的执行顺序

如果Message会阻塞MessageQueue的话，那么先postDelay10秒一个Runnable A，消息队列会一直阻塞，然后我再post一个Runnable B，B并不会等A执行完了再执行

调用过程如下：

1. `postDelay()`一个10秒钟的Runnable A、消息进队，MessageQueue调用`nativePollOnce()`阻塞，Looper阻塞；
2. 紧接着`post()`一个Runnable B、消息进队，判断现在A时间还没到、正在阻塞，把B插入消息队列的头部（A的前面），然后调用`nativeWake()`方法唤醒线程；
3. `MessageQueue.next()`方法被唤醒后，重新开始读取消息链表，第一个消息B无延时，直接返回给Looper；
4. Looper处理完这个消息再次调用`next()`方法，MessageQueue继续读取消息链表，第二个消息A还没到时间，计算一下剩余时间（假如还剩9秒）继续调用`nativePollOnce()`阻塞；
5. 直到阻塞时间到或者下一次有Message进队；

## 线程间通信

### 共享内存

公共变量

### AsyncTask

Android提供的轻量级异步类，可以直接继承，封装了线程池和Handler，方便开发者在子线程中更新UI

AsyncTask定义了三种泛型类型*Params，Progress和Result*

* **Params**：在执行AsyncTask时需要传入的参数，可用于在后台任务中使用（`doInBackground`方法的参数类型）如HTTP请求的URL
* **Progress**：后台任务执行时，如果需要在界面上显示当前的进度，则指定进度类型
* **Result**：后台任务的返回结果类型

#### 使用

```java
class myAsync extends AsyncTask<Params, Progress, Result> {

    //下面这个方法在主线程中执行，在doInBackground函数执行前执行
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //下面这个方法在子线程中执行，用来处理耗时行为
    @Override
    protected String doInBackground(Params... arg0) {
        Result res;
        return res;
    }

    //下面这个方法在主线程中执行，用于显示子线程任务执行的进度
    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
    }

    //下面这个方法在主线程中执行，在doinBackground方法执行完后执行
    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
    }
}
```

[AsyncTask 实现Android的线程通信](http://blog.csdn.net/qq_15267341/article/details/79056947)

#### 限制

1. AsyncTask的类必须在主线程中加载
2. AsyncTask的对象必须在主线程中创建
3. `execute`方法必须在UI线程中调用
4. 不要直接调用`onPreExecute`，`onPostExecute`，`doInBackground`和`onProgressUpdate`
5. 一个AsyncTask对象只能执行一次`execute`，否则会报异常
6. AsyncTask默认采用串行执行任务，可以通过`executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "")`并行执行任务

#### 原理（非重点）

1. `execute`，`executeOnExecutor`中，一个线程中所有的AsyncTask都在sDefaultExecutor串行线程池中执行

2. 排队执行过程

   1. AsyncTask的Params封装为FutureTask，是一个并发类，充当Runnable
   2. SerialExecutor的`execute`将FutureTask插入任务队列mTasks，如果当前没有正在活动的AsyncTask，则调用SerialExecutor的`scheduleNext`执行下一个AsyncTask任务，直到所有任务都被执行

3. SerialExecutor：串行线程池，用于任务排队

   THREAD_POOL_EXECUTOR：并行线程池，用于执行任务

   InternalHandler：用于将执行环境从线程池切换到主线程

### Handler

1. 主线程中创建一个Handler对象，并重写`handleMessage()`方法 
2. 当子线程需要进行UI操作时，就创建一个Message对象，并通过`handler.sendMessage()`将这条消息发送出去 ，或者通过post方法将一个Runnable投递到Handler内部的Looper中
3. 这条消息被添加到MessageQueue的队列中等待被处理 
4. Looper一直尝试从MessageQueue中提出待处理消息，分发到Handler的`handleMessage()`方法中，这样Handler中的业务逻辑就被切换到创建Handler的线程中了

[Android 异步消息处理机制 让你深入理解 Looper、Handler、Message三者关系](http://blog.csdn.net/lmj623565791/article/details/38377229)

## 线程池

1. 重用线程池中的线程，避免线程创建和销毁带来的性能开销
2. 有效控制最大并发数，避免大量线程抢占资源导致阻塞
3. 管理线程，提供定时执行、指定间隔执行等功能

### ThreadPoolExecutor

```java
ThreadPoolExecutor(int corePoolSize, // 核心线程数
                   int maximumPoolSize, //最大线程数
                   long keepAliveTime,  //非核心线程闲置的超时时长
                   TimeUnit unit, // keepAliveTime的时间单位，毫秒，秒，分钟等
                   BlockingQueue<Runnable> workQueue, // 任务队列，存储execute提交的Runnable
                   ThreadFactory threadFactory) // 线程工厂，创建新线程
```

### 执行规则

1. 未达到核心线程数，启动核心线程执行任务
2. 达到核心线程数，插入等待队列
3. 如果2中无法插入等待队列（一般是因为队列已满），且线程数量未达到最大线程数，则启动非核心线程执行任务
4. 如果3中线程数量已经达到最大值，则拒绝执行，并调用`RejectedExecutionHandler#rejectedExecution`通知调用者

> 默认情况下，核心线程会在线程池中一直存货，即使处于闲置状态，也可以设定超时终止
>
> 非核心线程运行结束或超时后就会被回收
>
> AsyncTask的THREAD_POOL_EXECUTOR线程池配置：
>
> * 核心线程数：CPU核心数+1
> * 最大线程数：CPU核心数x2+1
> * 核心线程无超时，非核心线程闲置超时为1秒
> * 任务队列容量128

### 分类

| 名称                 | 功能                                                         |
| -------------------- | ------------------------------------------------------------ |
| FixedThreadPool      | 线程数量固定，只有核心线程，空闲不回收<br>可以更快响应外界请求 |
| CachedThreadPool     | 线程数量不定，只有非核心线程，超时60秒回收<br>执行大量耗时较少的任务 |
| ScheduledThreadPool  | 核心线程数量固定，非核心线程数量不定，闲置立刻回收<br>执行定时任务和具有固定周期任务 |
| SingleThreadExecutor | 只有一个核心线程<br>统一所有外界任务到一个线程中，避免同步问题 |

# 性能优化

Android程序不可能无限制地使用内存和CPU资源，过多地使用内存会导致程序内存溢出，即OOM。而过多地使用CPU资源，一般是指做大量的耗时任务，会导致手机变得卡顿甚至出现程序无法响应的情况，即ANR.

一些有效的性能优化方法：布局优化、绘制优化、内存泄漏优化、响应速度优化、ListView优化、Bitmap优化、线程优化

## 布局优化

### 减少布局文件层级

* 删除布局中无用的控件和层级，其次有选择地使用性能较低的ViewGroup，比如RelativeLayout。尽量使用性能较高的ViewGroup如LinearLayout

* 采用\<include\>和\<merge\>标签和ViewStub。\<include\>标签主要用于布局重用，\<include\>标签和\<merge\>标签配合使用，降低减少布局的层级。而ViewStub则提供了按需加载功能，提高了程序的初始化效率

> include标签只支持android:layout开头的属性，除了android:id
>
> 如果include指定了android:layout_*，那么必须同时指定android:layout_width和android_layout_height

### ViewStub

* ViewStub是一个轻量级的View，用于延迟加载布局和视图，避免资源的浪费，减少渲染时间
* 不可见时不占布局位置，所占资源非常少。当可见时或调用ViewStub.inflate时它所指向的布局才会初始化
* ViewStub只能被inflate一次
* ViewStub只能用来inflate一个布局，不能inflate一个具体的View

## 绘制优化

绘制优化是指View的onDraw方法要避免执行大量的操作。主要体现在两个方面： 

1. onDraw中不要创建新的局部对象。 
2. onDraw中不要做耗时的任务，不要执行超过千次的循环

## 内存泄露优化

### 原理

内存泄漏也称作“存储渗漏”，用动态存储分配函数动态开辟的空间，在使用完毕后未释放，结果导致一直占据该内存单元。直到程序结束。即所谓内存泄漏

简单地说就是申请了一块内存空间，使用完毕后没有释放掉。它的一般表现方式是程序运行时间越长，占用内存越多，最终用尽全部内存，整个系统崩溃。由程序申请的一块内存，且没有任何一个指针指向它，那么这块内存就泄露了

内存泄漏的堆积，这会最终消耗尽系统所有的内存。从这个角度来说，一次性内存泄漏并没有什么危害，因为它不会堆积，而隐式内存泄漏危害性则非常大，因为较之于常发性和偶发性内存泄漏它更难被检测到

### 常见内存泄露实例

1. 静态变量导致内存泄露

   sContext或sView持有了当前Activity，导致Activity无法被释放

   ```java
   public class MainActivity extends Activity {
       private static Context sContext;
       private static View sView;

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);
           sContext = this; 
           // 或sView = new View(this);
       }
   }
   ```

2. 单例模式导致内存泄露

   如下是一个单例模式的TestManager，可以接收外部注册并将外部监听器存储起来

   ```java
   public class TestManager {

       private List<OnDataArrivedListener> mOnDataArrivedListeners = new ArrayList<OnDataArrivedListener>();

       private static class SingletonHolder {
           public static final TestManager INSTANCE = new TestManager();
       }

       private TestManager() {
       }

       public static TestManager getInstance() {
           return SingletonHolder.INSTANCE;
       }

       public synchronized void registerListener(OnDataArrivedListener listener) {
           if (!mOnDataArrivedListeners.contains(listener)) {
               mOnDataArrivedListeners.add(listener);
           }
       }

       public synchronized void unregisterListener(OnDataArrivedListener listener) {
           mOnDataArrivedListeners.remove(listener);
       }

       public interface OnDataArrivedListener {
           public void onDataArrived(Object data);
       }
   }
   ```

   如果Activity实现`OnDataArrivedListener`并向TestManager注册监听，并缺少解注册过程，则会引起内存泄露。因为Activity被单例TestManager持有，而单例模式生命周期与Application一致，因此Activity无法被释放

   ```java
   public class MainActivity extends Activity implements OnDataArrivedListener {
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);
           TestManager.getInstance().registerListener(this);
       }
   }
   ```

3. 属性动画导致内存泄露

   属性动画中有一类无限循环的动画，如果在Activity中播放此类动画且没有在onDestroy中停止，则View会被动画持有，而View持有Activity，则Activity无法被释放

   ```java
   ObjectAnimator animator = ObjectAnimator.ofFloat(mButton, "rotation", 0, 360).setDuration(2000);
   animator.setRepeatCount(ValueAnimator.INFINITE);
   animator.start();
   ```



4. 非静态内部类的静态实例容易造成内存泄漏

   ```java
   public class MainActivity extends Activity {  
       static Demo sInstance = null;  

       @Override  
       public void onCreate(BundlesavedInstanceState) {  
           super.onCreate(savedInstanceState);  
           setContentView(R.layout.activity_main);  
           if (sInstance == null) {  
               sInstance = new Demo();  
           }  
       }  
       class Demo {  
           void doSomething() {  
               System.out.print("dosth.");  
           }  
       }  
   } 
   ```

   上面的代码中的sInstance实例类型为静态实例，在第一个MainActivity act1实例创建时，sInstance会获得并一直持有act1的引用。当MainAcitivity销毁后重建，因为sInstance持有act1的引用，所以act1是无法被GC回收的，进程中会存在2个MainActivity实例（act1和重建后的MainActivity实例），这个act1对象就是一个无用的但一直占用内存的对象，即无法回收的垃圾对象。所以，对于launchMode不是singleInstance的Activity， 应该避免在activity里面实例化其非静态内部类的静态实例

5. Activity使用静态成员

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

   `label .setBackgroundDrawable`函数调用会将label赋值给sBackground的成员变量mCallback。上面代码意味着：sBackground（GC Root）会持有TextView对象，而TextView持有Activity对象。所以导致Activity对象无法被系统回收

6. 使用handler时的内存问题

   Handler通过发送Message与主线程交互，Message发出之后是存储在MessageQueue中的，有些Message也不是马上就被处理的。在Message中存在一个 target，是Handler的一个引用，如果Message在Queue中存在的时间过长，就会导致Handler无法被回收。如果Handler是非静态的，则会导致Activity或者Service不会被回收。 所以正确处理Handler等之类的内部类，应该将自己的Handler定义为静态内部类。

7. 注册某个对象后未反注册

   虽然有些系统程序，它本身好像是可以自动取消注册的(当然不及时)，但是我们还是应该在我们的程序中明确的取消注册，程序结束时应该把所有的注册都取消掉

   假设我们希望在锁屏界面(LockScreen)中，监听系统中的电话服务以获取一些信息(如信号强度等)，则可以在LockScreen中定义一个PhoneStateListener的对象，同时将它注册到TelephonyManager服务中。对于LockScreen对象，当需要显示锁屏界面的时候就会创建一个LockScreen对象，而当锁屏界面消失的时候LockScreen对象就会被释放掉。

   但是如果在释放LockScreen对象的时候忘记取消我们之前注册的PhoneStateListener对象，则会导致LockScreen无法被GC回收。如果不断的使锁屏界面显示和消失，则最终会由于大量的LockScreen对象没有办法被回收而引起OutOfMemory,使得system_process进程挂掉

8. 集合中对象没清理造成的内存泄露

   我们通常把一些对象的引用加入到了集合中，当我们不需要该对象时，如果没有把它的引用从集合中清理掉，这样这个集合就会越来越大。如果这个集合是static的话，那情况就更严重了

9. 资源对象没关闭造成的内存泄露

   资源性对象比如(Cursor，File文件等)往往都用了一些缓冲，我们在不使用的时候，应该及时关闭它们，以便它们的缓冲及时回收内存。它们的缓冲不仅存在于Java虚拟机内，还存在于Java虚拟机外。如果我们仅仅是把它的引用设置为null,而不关闭它们，往往会造成内存泄露

10. 一些不良代码成内存压力

  * 构造Adapter时，没有使用 convertView 重用 
  * Bitmap对象不再使用时，调用recycle()释放内存 
  * 对象被生命周期长的对象引用，如activity被静态集合引用导致activity不能释放

### 内存泄露的调试

1. 内存监测工具 DDMS --> Heap
2. 内存分析工具 MAT(Memory Analyzer Tool) 

[Android 内存泄露原理和检测](http://blog.csdn.net/gao878280390/article/details/55252732)

## 响应速度优化——避免ANR

### 概念

应用无响应，生成/data/anr/traces.txt

* Activity 5秒内无法响应屏幕触摸事件或键盘输入事件
* BroadcastReceiver 10秒内未执行完操作

### 调试

1. DDMS输出的LOG可以判断ANR发生在哪个类，但无法确定在类中哪个位置
2. 在/data/anr/traces.txt文件中保存了ANR发生时的代码调用栈，可以跟踪到发生ANR的所有代码段

### 避免

任何在主线程中运行的，需要消耗大量时间的操作都会引发ANR

任何运行在主线程中的方法，都要尽可能的只做少量的工作。特别是活动生命周期中的重要方法如onCreate()和 onResume()等更应如此

耗时操作：

* 访问网络和数据库
* 开销很大的计算，比如改变位图的大小，需要在一个单独的子线程中完成

## ListView和Bitmap优化

### ListView/GridView优化

1. convertView的复用

   在Adapter类的getView方法中通过判断convertView是否为null，是的话就需要在创建一个视图出来，然后给视图设置数据，最后将这个视图返回给底层，呈现给用户；如果不为null的话，其他新的view可以通过复用的方式使用已经消失的条目view，重新设置上数据然后展现出来

2. 使用内部类ViewHolder

   可以创建一个内部类ViewHolder，里面的成员变量和view中所包含的组件个数、类型相同，在convertview为null的时候，把findviewbyId找到的控件赋给ViewHolder中对应的变量，就相当于先把它们装进一个容器，下次要用的时候，直接从容器中获取

3. 分段分页加载

   分批加载大量数据，缓解一次性加载大量数据而导致OOM崩溃的情况

4. 减少变量的使用，减少逻辑判断和加载图片等耗时操作，减少GC的执行，减少耗时操作造成的卡顿

5. 根据列表滑动状态控制任务执行频率，例如快速滑动时不适合开启大量异步任务

6. 开启硬件加速

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

### Bitmap优化：

* 使用BitmapFactory.Options根据需要对图片进行采样，控制inSampleSize
* 使用内存缓存和磁盘缓存

## 线程优化

采用线程池，避免程序中存在大量的Thread。

线程池可以重用内部线程，从而避免了线程的创建和销毁所带来的性能开销，同时线程池还能有效地控制线程池的最大并发数，避免大量的线程因为相互抢占系统资源从而导致阻塞现象的发生

## 防止内存溢出（OOM）

### 原因

1. 资源未得到及时释放：比如Context，Cursor等资源使用后未及时释放
2. 对象内存过大：Bitmap，xml等耗用内存较大
3. static修饰的静态成员过多，生命周期过长

### 解决

1. 及时释放资源，避免加载过大资源，比如图片进行采样加载，及时使用`recycle()`释放资源
2. 使用WeakReference代替强引用
3. 减少static关键字使用
4. 对于不可控的线程，尽量使用静态内部类，防止非静态内部类拥有外部类的强引用造成内存泄露
5. 使用TraceView，heap工具，allocation tracker等工具进行筛查

[Android性能优化(一)--关于内存溢出](https://blog.csdn.net/checkiming/article/details/60480773)

## 防止内存抖动

内存抖动是指在短时间内有大量的对象被创建或者被回收的现象

### 表现

* 在Memory Monitor里面查看到短时间发生了多次内存的涨跌
* 通过Allocation Tracker来查看在短时间内，同一个栈中不断进出的相同对象

### 原理

核心：大量的对象被创建又在短时间内马上被释放

瞬间产生大量的对象会严重占用Young Generation的内存区域，当达到阀值，剩余空间不够的时候，也会触发GC。即使每次分配的对象占用了很少的内存，但是他们叠加在一起会增加Heap的压力，从而触发更多其他类型的GC。这个操作有可能会影响到帧率，并使得用户感知到性能问题

> 1. 最近刚分配的对象会放在Young Generation区域，这个区域的对象通常都是会快速被创建并且很快被销毁回收的，同时这个区域的GC操作速度也是比Old Generation区域的GC操作速度更快的
>
> 2. 执行GC操作的时候，任何线程的任何操作都会需要暂停，等待GC操作完成之后，其他操作才能够继续运行（所以垃圾回收运行的次数越少，对性能的影响就越少）
>
>    通常来说，单个的GC并不会占用太多时间，但是大量不停的GC操作则会显著占用帧间隔时间(16ms)。如果在帧间隔时间里面做了过多的GC操作，那么自然其他类似计算，渲染等操作的可用时间就变得少了

### 避免

1. 避免在for循环里面分配对象占用内存，需要尝试把对象的创建移到循环体之外
2. 每次屏幕发生绘制以及动画执行过程中，onDraw方法都会被调用到，避免在onDraw方法里面执行复杂的操作，避免创建对象。
3. 对于无法避免需要创建对象的情况，通过对象池来解决频繁创建与销毁的问题，但是这里需要注意结束使用之后，需要手动释放对象池中的对象

> 通过GenericObjectPool\<T>构建对象池模型
>
> 对象池技术基本原理的核心有两点：缓存和共享，即对于那些被频繁使用的对象，在使用完后，不立即将它们释放，而是将它们缓存起来，以供后续的应用程序重复使用，从而减少创建对象和释放对象的次数，进而改善应用程序的性能。事实上，由于对象池技术将对象限制在一定的数量，也有效地减少了应用程序内存上的开销。

[Android App解决卡顿慢之内存抖动及内存泄漏（发现和定位）](https://blog.csdn.net/huang_rong12/article/details/51628264)

[Java对象池](https://blog.csdn.net/shimiso/article/details/9814917)

## 其他

* 避免创建过多地对象
* 不要过多使用枚举，枚举占用的内存空间要比整形大
* 常量请使用static final来修饰
* 使用一些Android特有的数据结构
* 适当使用软引用和弱引用
* 采用内存缓存和磁盘缓存
* 尽量采用静态内部类，这样可以避免潜在的由于内部类而导致的内存泄漏

# 像素单位

px：像素点，如1920x1080

dpi：像素密度，即像素/英寸

dp：设备独立像素，dpi / 160

px = dp * (dpi / 160)，在每英寸160像素点的屏幕上，1dp = 1px