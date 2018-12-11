Flutter

[TOC]

# 简介

## 框架介绍

* 同一套代码同时运行在Android和iOS系统上

* 渲染引擎依靠跨平台的Skia图形库来实现，依赖系统的只有图形绘制相关的接口

* 逻辑处理使用支持AOT的Dart语言

  * 同时支持AOT和JIT运行方式，JIT支持Hot Reload

    > 在Android Studio中编辑Dart代码后，只需要点击保存或者“Hot Reload”按钮，就可以立即更新到正在运行的设备上，不需要重新编译App，甚至不需要重启App，立即就可以看到更新后的样式

* 执行效率也比JavaScript高得多

## 同类框架对比

* 基于WebView的Cordova、AppCan等
  * 几乎可以完全继承现代Web开发的所有成果
  * 不需要太多的学习和迁移成本
  * WebView的渲染效率和JavaScript执行性能太差，兼容性差
* 使用HTML+JavaScript渲染成原生控件的React Native、Weex等
  * 充分利用原生控件，相对于WebView绘制效率较高
  * 框架本身和App开发者绑在了系统的控件系统上，跨平台能力差

# 配置

## 安装（Mac OS）

```shell
# 拉取flutter的git仓库
git clone https://github.com/flutter/flutter ~/flutter
# 查询最新beta版本的tag，并切换到最新版本 https://flutter.io/sdk-archive/#macos
git co v0.6.0
# 添加flutter/bin路径到PATH中，最好配置到.zshrc中
export PATH=~/flutter/bin:$PATH
# 配置flutter依赖
flutter doctor
```

[Get Started: Install on macOS](https://flutter.io/setup-macos/)

## 配置IDE（Android Studio）

安装flutter和dart插件

![image-20180831110016038](assets/image-20180831110016038.png)

![image-20180831110038872](assets/image-20180831110038872.png)

重启Studio，选择`Start a new Flutter Project `

[Get Started: Configure Editor](https://flutter.io/get-started/editor/)

[Get Started: Test Drive](https://flutter.io/get-started/test-drive/)

## 依赖库

### 配置

依赖包由[Pub](https://pub.dartlang.org/)仓库管理，项目依赖配置在pubspec.yaml文件中声明即可（类似于NPM的版本声明 [Pub Versioning Philosophy](https://www.dartlang.org/tools/pub/versioning)），对于未发布在Pub仓库的插件可以使用git仓库地址或文件路径

`^`开头的版本表示[和当前版本接口保持兼容](https://www.dartlang.org/tools/pub/dependencies#caret-syntax)的最新版，`^1.2.3` 等效于 `>=1.2.3 <2.0.0` 而 `^0.1.2` 等效于 `>=0.1.2 <0.2.0`

例如添加english_words库

```yaml
dependencies:
  flutter:
    sdk: flutter

  cupertino_icons: ^0.1.0
  english_words: ^3.1.0
```

运行`flutter package get`获取依赖库

### 插件

**Dart语言无法直接调用Android系统提供的Java接口**，这时就需要使用插件来实现中转

| 插件名                                                       | 介绍                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [android_alarm_manager](https://github.com/flutter/plugins/blob/master/packages/android_alarm_manager) | 访问Android系统的`AlertManager`                              |
| [android_intent](https://github.com/flutter/plugins/blob/master/packages/android_intent) | 构造Android的Intent对象                                      |
| [battery](https://github.com/flutter/plugins/blob/master/packages/battery) | ，获取和监听系统电量变化。                                   |
| [connectivity](https://github.com/flutter/plugins/blob/master/packages/connectivity) | ，获取和监听系统网络连接状态。                               |
| [device info](https://github.com/flutter/plugins/blob/master/packages/device_info) | ，获取设备型号等信息。                                       |
| [image_picker](https://github.com/flutter/plugins/blob/master/packages/image_picker) | ，从设备中选取或者拍摄照片。                                 |
| [package_info](https://github.com/flutter/plugins/blob/master/packages/package_info) | ，获取App安装包的版本等信息。                                |
| [path_provider](https://github.com/flutter/plugins/blob/master/packages/path_provider) | ，获取常用文件路径。                                         |
| [quick_actions](https://github.com/flutter/plugins/blob/master/packages/quick_actions) | ，App图标添加快捷方式，iOS的[eponymous concept](https://developer.apple.com/ios/human-interface-guidelines/extensions/home-screen-actions)和Android的[App Shortcuts](https://developer.android.com/guide/topics/ui/shortcuts.html)。 |
| [sensors](https://github.com/flutter/plugins/blob/master/packages/sensors) | ，访问设备的加速度和陀螺仪传感器。                           |
| [shared_preferences](https://github.com/flutter/plugins/blob/master/packages/shared_preferences) | ，App KV存储功能。                                           |
| [url_launcher](https://github.com/flutter/plugins/blob/master/packages/url_launcher) | ，启动URL，包括打电话、发短信和浏览网页等功能。              |
| [video_player](https://github.com/flutter/plugins/blob/master/packages/video_player) | ，播放视频文件或者网络流的控件。                             |

# Widgets

[Material](https://material.io/guidelines/)

## 基本Widgets

- StatelessWidget：不可修改，所有的属性都是final
  - 用来展示静态的文本或者图片
- StatefulWidget：同样不可修改，不过在生命周期内可以包含一个可变的State的类
  * 用来展示动态内容，用来展示静态的文本或者图片
  * State\<T\>：提供给`StatefulWidget#createState`，只能在其生命周期内存在

## 其他Widgets

- Scaffold：主屏幕的widget tree，包含title，bar和body等部分

- ListView：列表，主要调用`ListView.builder`返回`ListView`
  - ListTile：用于构建LiveView中一行的Widget
  - Divider：1px高的分隔线
- Navigator：用于管理子Widget的返回栈

## 常见方法

* build：用于描述如何构造页面
* setState：类似于notifyDataSetChanged()，提醒State的信息有改变
* ListTile#divideTiles：用于构造List\<ListTile\>，在每两个元素中插入Divider

# 常见语法

* =>：单行函数

## Hot Reload

### 运行过程

* 注入新代码到正在运行的DartVM中
* 类结构更新
* 重建整个控件树
* 更新界面

### 无法生效的情况

* 编译错误
  * 无法通过编译，控制台报错

* 控件类型从`StatelessWidget`到`StatefulWidget`的转换
  * 在执行热刷新时会保留程序原来的state，重新创建会报错
  * stageless→stateful：`myWidget is not a subtype of StatelessWidget`
  * stateful→stateless：`type 'myWidget' is not a subtype of type 'StatefulWidget' of 'newWidget'`

* 全局变量和静态成员变量不会在热刷新时更新

* 修改了main函数中创建的根控件节点
  * 热刷新后只会根据原来的根节点重新创建控件树，不会修改根节点

* 某个类从普通类型转换成枚举类型，或者类型的泛型参数列表变化

热刷新无法实现更新时，执行一次热重启（Hot Restart）就可以全量更新所有代码，同样不需要重启App，区别是restart会将所有Dart代码打包同步到设备上，并且所有状态都会重置。

# Demo Code

```dart
import 'package:flutter/material.dart';
import 'package:english_words/english_words.dart';

// => 用于单行函数
void main() => runApp(MyApp());

// Widget包含padding，margin等，基本组件
// StatelessWidget是不可更改的，所有的属性都是final
class MyApp extends StatelessWidget {
    // build方法提供一个
    @override
    Widget build(BuildContext context) {
        // 默认Material主题
        return MaterialApp(
            title: 'Welcome to Flutter',
            theme: ThemeData(          // Add the 3 lines from here...
                primaryColor: Colors.red,
            ),
            home: RandomWords(),
        );
    }
}

// StatefulWidget同样不可变，但是在生命周期内可以包含一个可变的state
class RandomWords extends StatefulWidget {
    @override
    RandomWordsState createState() => new RandomWordsState();
}

// 提供给StatefulWidget的State
class RandomWordsState extends State<RandomWords> {
    // 前缀加_表示是private类型
    final List<WordPair> _suggestions = <WordPair>[];

    final Set<WordPair> _saved = new Set<WordPair>();

    // const表示常量
    final TextStyle _biggerFont = const TextStyle(fontSize: 18.0);

    @override
    Widget build(BuildContext context) {
        // Material库中的Widget，包含app bar，title和body用于显示主屏幕内容
        return Scaffold(
            appBar: AppBar(
                title: Text("Flutter Demo"),
                actions: <Widget>[
                    // 点击触发_pushSaved
                    IconButton(icon: Icon(Icons.list), onPressed: _pushSaved)
                ],
            ),
            body: _buildSuggestions(),
        );
    }

    Widget _buildSuggestions() {
        return ListView.builder(
            // padding = 16px
            padding: const EdgeInsets.all(16.0),

            // itemBuilder对于每行只会调用一次，类似于Android中的RecyclerView中的bindView
            itemBuilder: (context, i) {
                // 单数行时，return 1px高的分行符
                if (i.isOdd) return Divider();

                // ~/整除
                final index = i ~/ 2;
                // 达到列表底部时，随机生成10个单词添加到列表中
                if (index >= _suggestions.length) {
                    _suggestions.addAll(generateWordPairs().take(10));
                }
                return _buildRow(_suggestions[index]);
            });
    }

    Widget _buildRow(WordPair pair) {
        final bool alreadySaved = _saved.contains(pair);

        // ListTile
        return ListTile(
            title: Text(
                pair.asPascalCase,
                style: _biggerFont,
            ),
            trailing: Icon(
                alreadySaved ? Icons.favorite : Icons.favorite_border,
                color: alreadySaved ? Colors.red : null,
            ),
            // (){...} => setOnClickListener(new GestureTapCallback(){...})
            onTap: () {
                // setState => notifyDataSetChanged() in Android
                setState(() {
                    if (alreadySaved) {
                        _saved.remove(pair);
                    } else {
                        _saved.add(pair);
                    }
                });
            },
        );
    }

    void _pushSaved() {
        // 将一个route压入Navigator的返回栈
        Navigator.of(context).push(
            new MaterialPageRoute(builder: (BuildContext context) {
                // 构造ListTile
                final List<Widget> divided = ListTile.divideTiles(
                    context: context,
                    tiles: _saved.map((WordPair pair) {
                        return ListTile(
                            title: Text(
                                pair.asPascalCase,
                                style: _biggerFont,
                            ),
                        );
                    })
                ).toList();

                return Scaffold(
                    appBar: AppBar(
                        title: const Text("Saved Suggestions"),
                    ),
                    body: ListView(
                        children: divided,
                    ),
                );
            }),
        );
    }
}
```

[Write Your First Flutter App](https://flutter.io/get-started/codelab/)

[Flutter for Android Developers](https://flutter.io/flutter-for-android/)

# Dart语法简记

## const与final的区别

* final：只能被设一次值，在声明处赋值，值和普通变量的设值一样，可以是对象、字符串、数字等，用于修饰值的表达式不变的变量
* const：只能被设一次值，在声明处赋值，且值必须为编译时常量，用于修饰常量

```dart
int Func() {
  // 代码
}

final int m1 = 60;
final int m2 = Func(); // 正确
const int n1 = 42;
const int n2 = Func(); // 错误
```

# TODO

[Flutter原理与实践](https://tech.meituan.com/waimai-flutter-practice.html)