# Flutter

[TOC]

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

# 基本知识

## dart

主要代码都在`lib/main.dart`中，程序入口

运行`flutter package get`获取依赖库

### 常见类

[Material](https://material.io/guidelines/)

* Scaffold：主屏幕的widget tree，包含title，bar和body等部分

- StatelessWidget：不可修改，所有的属性都是final

- StatefulWidget：同样不可修改，不过在生命周期内可以包含一个可变的State的类
- State\<T\>：提供给`StatefulWidget#createState`，只能在其生命周期内存在
- ListView：列表，主要调用`ListView.builder`返回`ListView`
- ListTile：用于构建LiveView中一行的Widget
- Divider：1px高的分隔线
- Navigator：用于管理子Widget的返回栈

### 常见方法

* build：用于描述如何构造页面
* setState：类似于notifyDataSetChanged()，提醒State的信息有改变
* ListTile#divideTiles：用于构造List\<ListTile\>，在每两个元素中插入Divider

### 常见语法

* =>：单行函数

## 导入依赖库

修改`pubspec.yaml`，添加english_words库

```yaml
dependencies:
  flutter:
    sdk: flutter

  cupertino_icons: ^0.1.0
  english_words: ^3.1.0
```

### Demo Code

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