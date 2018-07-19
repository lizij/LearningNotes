# 安全

## Webview 远程执行JS漏洞

4.4以后增加了防御措施，如果用js调用本地代码，开发者必须在代码申明JavascriptInterface。如果代码无此申明，那么也就无法使得js生效，也就是说这样就可以避免恶意网页利用js对客户端的进行窃取和攻击

[Android中Java和JavaScript交互](https://www.imooc.com/article/1475)

## DNS劫持

DNS劫持俗称抓包。通过对url的二次劫持，修改参数和返回值，进而进行对app访问web数据伪装，实现注入广告和假数据，甚至起到导流用户的作用，严重的可以通过对登录APi的劫持可以获取用户密码，也可以对app升级做劫持，下载病毒apk等目的，解决方法一般用https进行传输数据

[Android OkHttp实现HttpDns的最佳实践（非拦截器）](http://blog.csdn.net/sbsujjbcy/article/details/51612832)

[阿里云HTTPDNS](https://www.aliyun.com/product/httpdns?spm=a2c4e.11153959.blogcont205501.17.5fc29e1bkmAHbZ)

## APP升级过程防劫持

做app版本升级时一般流程是采用请求升级接口，如果有升级，服务端返回下一个下载地址，下载好Apk后，再点击安装

- 升级API：被劫持后返回错误的下载地址（HTTPS，URL验证）

- 下载API：返回恶意文件或者apk（HTTPS，文件Hash校验，签名key验证）
- 安装过程：安装apk时本地文件path被篡改（安全检查，文件签名，包名校验）

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

1. 解析key值，空intent导致的拒绝服务只是一部分，还有类型转换异常，数组越界等，这些我们都需要找到其关键函数，检测其是否有异常保护。自动化测试工具在这里的难点是找到关键函数的key值，action值，以及key对应的类型等来组装命令进行攻击。
2. 通用型拒绝服务是由于应用中使用了getSerializableExtra()的API却没有进行异常保护，攻击者可以传入序列化数据，导致应用本地拒绝服务。此时不管传入的key值是否相同，都会抛出类未定义异常，相比前面需要解析key，自动化测试的通用性提高很多

### 修复

1. 将不必要导出的组件export属性设为false
2. 在处理intent数据时捕获异常。