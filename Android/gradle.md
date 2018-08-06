# Groovy

Groovy是一种动态语言，它和Java类似(算是Java的升级版，但是又具备脚本语言的特点)，都在Java虚拟机中运行。当运行Groovy脚本时它会先被编译成Java类字节码，然后通过JVM虚拟机执行这个Java字节码类。

## 安装

```shell
curl -s get.sdkman.io | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install groovy
groovy -version
```

## 语法基础简记 (仅列出与Java有区别的部分)

### 注释

保留声明脚本运行类型的单行注释

### 标识符

* 普通标识符: 和C语言类似，只能以字母、美元符、下划线开始，不能以数字开头
* 引用标识符: 所有字符串都可以当作引用标示符定义

### 字符和字符串

* 字符: 没有明确的Characters，`'A'`或`'B' as char`或`(char)'C'`表示

* 单引号字符串: java.lang.String类型的，不支持站位符插值操作

* 三重单引号字符串: java.lang.String类型的，不支持站位符插值操作，可以标示多行字符串

* 双引号字符串: 支持站位插值操作，如果双引号字符串中不包含站位符则是java.lang.String类型的，如果双引号字符串中包含站位符则是groovy.lang.GString类型的。

  对于插值占位符我们可以用`${}`或者`$`来标示，`${}`用于一般替代字串或者表达式，`$`主要用于A.B的形式中，如

  ```groovy
  def name = 'Guillaume' // a plain string
  def greeting = "Hello ${name}"
  assert greeting.toString() == 'Hello Guillaume'
  ```

* 多重双引号字符串: 支持多行，支持站位插值操作

* 斜线字符串: 通常用在正则表达式中，如`def fooPattern = /.*foo.*/`

### Lists

可以通过超出列表范围的数来索引列表，如

```groovy
def letters = ['a', 'b', 'c', 'd']

//负数下标则从右向左index
assert letters[-1] == 'd'    
assert letters[-2] == 'c'

//获取一段List子集
assert letters[1, 3] == ['b', 'd']         
assert letters[2..4] == ['C', 'd', 'e'] 
```
### Maps 

```groovy
//把一个定义的变量作为Map的key，访问Map的该key是失败的
def key = 'name'
def person = [key: 'Guillaume']      
assert !person.containsKey('name')   
assert person.containsKey('key') 

//把一个定义的变量作为Map的key的正确写法---添加括弧，访问Map的该key是成功的
person = [(key): 'Guillaume']        
assert person.containsKey('name')    
assert !person.containsKey('key') 
```
### 运算符

* `**`: 次方运算符
* `?.`: 安全占位符，这个运算符主要用于避免空指针异常
* `.@`: 直接域访问操作符
* `.&`: 方法指针操作符
* 支持将`?:`三目运算符简化为二目
* `*.`: 展开运算符，一个集合使用展开运算符可以得到一个元素为原集合各个元素执行后面指定方法所得值的集合

### times循环

(n).times表示从0到n-1循环n次，默认参数为it

```groovy
3.times {
    println "Hello " + it
}
```

输出

```
Test 0
Test 1
Test 2
```



## 程序结构

* 包名: 与Java相同，`package com.yoursite`

* Imports引入: 默认导入了一些常用的包

* 脚本与类: groovy文件编译后的class其实是Java类，该类从Script类派生而来(查阅API)；可以发现，每个脚本都会生成一个static main方法，我们执行groovy脚本的实质其实是执行的这个Java类的main方法，脚本源码里所有代码都被放到了run方法中，脚本中定义的方法(该例暂无)都会被定义在Main类中

  ```groovy
  class Main {                                    
      static void main(String... args) {          
          println 'Groovy world!'                 
      }
  }
  ```

## 闭包

### 语法

* 定义: `{ [closureParameters -> ] statements }`
* 调用: 闭包对象.call(参数) 或 闭包对象(参数)

```groovy
def code = { 123 }
assert code() == 123
assert code.call() == 123

def isOdd = { int i-> i%2 == 1 }                            
assert isOdd(3) == true                                     
assert isOdd.call(2) == false
```
### 参数: 

* 普通参数: 包含参数类型(可选)、参数名字、默认值(可选)，逗号分隔
* 隐含参数: it
* 可变长参数: ...表示

### 省略调用

当闭包作为闭包或方法的最后一个参数时我们可以将闭包从参数圆括号中提取出来接在最后，如果闭包是唯一的一个参数，则闭包或方法参数所在的圆括号也可以省略；对于有多个闭包参数的，只要是在参数声明最后的，均可以省略

```groovy
def debugClosure(int num, String str, Closure closure){  
      //dosomething  
}  

debugClosure(1, "groovy", {  
   println"hello groovy!"  
})
```
## GDK(Groovy Development Kit)

对JDK的一些类的二次封装

* I/O操作

  * 读: 

    ```groovy
    //读文件打印及打印行号脚本
    new File(baseDir, 'haiku.txt').eachLine { line, nb ->
        println "Line $nb: $line"
    }
    ```

  * 写: 

    ```groovy
    //向一个文件以utf-8编码写三行文字
    new File(baseDir,'haiku.txt').withWriter('utf-8') { writer ->
        writer.writeLine 'Into the ancient pond'
        writer.writeLine 'A frog jumps'
        writer.writeLine 'Water’s sound!'
    }
    ```

  * 文件树: 

    ```groovy
    //遍历所有指定路径下文件名打印
    dir.eachFile { file ->                      
        println file.name
    }
    ```

  * 执行外部命令: 

    ```groovy
    def process = "ls -l".execute()             
    println "Found text ${process.text}"
    ```

* 工具类: 

  * ConfigSlurper: 配置管理文件读取工具类

    ```groovy
    def config = new ConfigSlurper().parse('''
        app.date = new Date()  
        app.age  = 42
        app {                  
            name = "Test${42}"
        }
    ''')
    
    assert config.app.date instanceof Date
    assert config.app.age == 42
    assert config.app.name == 'Test42'
    ```

  * Expando扩展

    ```groovy
    def expando = new Expando()
    expando.toString = { -> 'John' }
    expando.say = { String s -> "John says: ${s}" }
    
    assert expando as String == 'John'
    assert expando.say('Hi') == 'John says: Hi'
    ```

## DSL(Domain Specific Languages)

DSL是一种特定领域的语言(功能领域、业务领域)，Groovy是通用的编程语言，所以不是DSL，但是Groovy却对编写全新的DSL提供了很好的支持，这些支持来自于Groovy自身语法的特性，如下: 

- Groovy不需用定义CLASS类就可以直接执行脚本
- Groovy语法省略括弧和语句结尾分号等操作

[《官方权威指南》](http://www.groovy-lang.org/index.html)

[Groovy脚本基础全攻略](https://blog.csdn.net/yanbober/article/details/49047515)

# Gradle

## DSL基础

* Gradle的实质是配置脚本，执行一种类型的配置脚本时就会创建一个关联的对象

| 脚本类型        | 关联对象类型 | 注释                                                         |
| --------------- | ------------ | ------------------------------------------------------------ |
| Build script    | Project      | 每个工程的build.gradle对应一个Project对象，每个Project在构建的时候都包含一系列Task，这些Task中很多又是Gradle的插件默认支持的 |
| Init script     | Gradle       | 构建初始化时创建，主要的用途是为接下来的Build script做一些准备工作，整个构建执行过程中只有这么一个对象，一般很少去修改这个默认配置脚本 |
| Settings script | Settings     | 每个settings.gradle会转换成一个Settings对象，用来进行一些项目设置的配置。这个文件一般放置在工程的根目录 |

### 项目构建流程

* 为当前项目创建一个Settings类型的实例。
* 如果当前项目存在settings.gradle文件，则通过该文件配置刚才创建的Settings实例。
* 通过Settings实例的配置创建项目层级结构的Project对象实例。
* 最后通过上面创建的项目层级结构Project对象实例去执行每个Project对应的build.gradle脚本。

### Build生命周期

* Initialization: 初始化阶段决定哪些项目需要加入构建，并为这些需要加入构建的项目分别创建Project实例，即执行settings.gradle
* Configuration: 确定Project和Task的关系，解析每个被加入构建项目的build.gradle
* Execution: 执行gradle命令，执行指定task

## Gradle构建基础

每一个Gradle构建都是由一个或多个project构成，每一个project都是由一个或多个tasks构成，每个task的实质其实是一些更加细化的构建（譬如编译class、创建jar文件等）

### 构建方式

- 单项目构建: settings.gradle可选，只会执行settings.gradle和gradle命令指定的task中的代码

- 多项目创建: 必须保证在根目录下有settings.gradle文件

  - 分层布局: 子目录

    ```groovy
    //分层布局的多项目构建settings.gradle文件
    include 'project1', 'project2:child', 'project3:child1'
    ```

  - 平面布局: 兄弟目录

    ```groovy
    //平面布局的多项目构建settings.gradle文件
    includeFlat 'project3', 'project4'
    ```

### Task

**基础**

```groovy
//创建一个名为build.gradle的文件
task hello {
    doLast {
        println 'Hello world!'
    }
}

//这是快捷写法，用<<替换doLast，后面解释
task hl << {
    println 'Hello world!'
}

//创建upper的task，使用Groovy语言编写
task upper << {
    String someString = 'mY_nAmE'
    println "Original: " + someString
    println "Upper case: " + someString.toUpperCase()
}
```

> 有无Action的区别，即<<
>
> 如果task没有加<<则这个任务在脚本初始化initialization阶段（即无论执行啥task都被执行）被执行，如果加了<<则在gradle actionTask后才执行。因为没有加<<则闭包在task函数返回前会执行，而加了<<则变成调用actionTask.doLast()，所以会等到gradle actionTask时执行

**依赖**

```Groovy
task taskX(dependsOn: 'taskY') << {
    println 'taskX'
}
task taskY << {
    println 'taskY'
}
```

## Gradle依赖管理基础

依赖关系可能需要从远程的Maven等仓库中下载，也可能是在本地文件系统中，或者是通过多项目构建另一个构建，我们称这个过程为依赖解析

**Gradle依赖配置**

在Gradle中依赖可以组合成configurations（配置），一个配置简单地说就是一系列的依赖，通俗说也就是依赖配置；我们可以使用它们声明项目的外部依赖，也可以被用来声明项目的发布。下面我们给出几种Java插件中常见的配置，如下：

- compile  用来编译项目源代码的依赖；
- runtime  在运行时被生成的类需要的依赖，默认项，包含编译时的依赖；
- testCompile 编译测试代码依赖，默认项，包含生成的类运行所需的依赖和编译源代码的依赖；
- testRuntime 运行测试所需要的依赖，默认项，包含上面三个依赖；

* providedCompile:  jar包/依赖代码，仅在编译时需要依赖
* annotationProcessor: APT工具中的一种，他是google开发的内置框架，不需要引入

> APT(Annotation Processing Tool)是一种处理注释的工具,它对源代码文件进行检测找出其中的Annotation，使用Annotation进行额外的处理。 Annotation处理器在处理Annotation时可以根据源文件中的Annotation生成额外的源文件和其它的文件(文件具体内容由Annotation处理器的编写者决定),APT还会编译生成的源文件和原来的源文件，将它们一起生成class文件。

**Gradle外部依赖**

我们可以用Gradle声明许多种依赖，其中有一种是外部依赖（external dependency），它是在当前构建之外的一种依赖，一般存放在远程（譬如Maven）或本地的仓库里。如下是一个外部依赖的例子：

```Groovy
dependencies {
    compile group: 'org.hibernate', name: 'hibernate-core', version: '3.6.7.Final'
}
```

可以看见，引用一个外部依赖需要用到group、name、version属性。简写为`group:name:version`

```Groovy
dependencies {
    compile 'org.hibernate:hibernate-core:3.6.7.Final'
}
```

**Gradle仓库**

Gradle会在一个仓库（repository）里找这些依赖文件，仓库其实就是很多依赖文件的集合服务器, 他们通过group、name、version进行归类存储，

Gradle可以解析好几种不同的仓库形式（譬如Maven等），但是Gradle默认不提前定义任何仓库，我们必须手动在使用外部依赖之前定义自己的仓库。

同一个库只要找到第一个就不会再找了

```groovy
repositories {
    // 使用本地文件系统仓库
    ivy {
        // URL can refer to a local directory
        url "../local-repo"
    }
    // 使用远程Maven仓库
    maven {
        url "http://repo.mycompany.com/maven2"
    }
    // 使用MavenCentral仓库
    mavenCentral()
}
```

**Gradle发布artifacts**

依赖配置也可以用来发布文件，我们可以通过在uploadArchives任务里加入仓库来完成。下面是一个发布到Maven库的例子，Gradle将生成和上传pom.xml，如下：

```groovy
apply plugin: 'maven'

uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: "file://localhost/tmp/myRepo/")
        }
    }
}
```

## Gradle命令

在Android Studio中，执行`./gradlew xxx`

## Gradle脚本

### Project API

常用默认自带属性

| Name        | Type       | Default Value      |
| ----------- | ---------- | ------------------ |
| project     | Project    | Project实例对象    |
| name        | String     | 项目目录的名称     |
| path        | String     | 项目的绝对路径     |
| description | String     | 项目描述           |
| projectDir  | File       | 包含构建脚本的目录 |
| build       | File       | projectDir/build   |
| group       | Object     | 未具体说明         |
| version     | Object     | 未具体说明         |
| ant         | AntBuilder | Ant实例对象        |

### Script API

当Gradle执行一个脚本时它会将这个脚本编译为实现了Script的类，也就是说所有的属性和方法都是在Script的接口中声明。

### Gradle API

参考[Gradle](https://docs.gradle.org/current/dsl/org.gradle.api.invocation.Gradle.html)

## Gradle变量声明

* 局部变量: 使用关键字def声明，只在声明的地方可见
* 扩展变量: 所有被增强的对象可以拥有自定义属性（譬如projects、tasks、source sets等），使用ext扩展块可以一次添加多个属性

## Gradle文件操作

### 定位文件

```groovy
File configFile = file('src/config.xml')
```

### 文件集合

```groovy
FileCollection collection = files('src/file1.txt',
                                  new File('src/file2.txt'),
                                  ['src/file3.txt', 'src/file4.txt'])
```

### 文件树

```groovy
// 以一个基准目录创建一个文件树
FileTree tree = fileTree(dir: 'src/main')

// 添加包含和排除规则
tree.include '**/*.java'
tree.exclude '**/Abstract*'

// 使用路径创建一个树
tree = fileTree('src').include('**/*.java')

// 使用闭合创建一个数
tree = fileTree('src') {
    include '**/*.java'
}

// 使用map创建一个树
tree = fileTree(dir: 'src', include: '**/*.java')
tree = fileTree(dir: 'src', includes: ['**/*.java', '**/*.xml'])
tree = fileTree(dir: 'src', include: '**/*.java', exclude: '**/*test*/**')

// 遍历文件树
tree.each {File file ->
    println file
}
```

### 指定输入文件

```groovy
//使用一个File对象设置源目录
compile {
    source = file('src/main/java')
}

//使用一个字符路径设置源目录
compile {
    source = 'src/main/java'
}

//使用一个集合设置多个源目录
compile {
    source = ['src/main/java', '../shared/java']
}
```

### 复制文件

```groovy
task copyTask(type: Copy) {
    from 'src/main/webapp'
    into 'build/explodedWar'
}
```

### 同步文件

执行时会复制源文件到目标目录，然后从目标目录删除所有非复制文件

```groovy
task libs(type: Sync) {
    from configurations.runtime
    into "$buildDir/libs"
}
```

### 创建归档文件

```groovy
apply plugin: 'java'

task zip(type: Zip) {
    from 'src/dist'
    into('libs') {
        from configurations.runtime
    }
}
```

## Gradle插件

### 概述

Gradle其实是依托于各种插件壮大的，譬如Java插件用来构建Java工程，Android插件用来构建打包Android工程，我们只需要选择合适的插件即可，插件会为我们提供丰富的任务用来快捷处理构建

[Gradle支持的插件](https://plugins.gradle.org/)

- 脚本插件 `apply from: 'other.gradle'`

  是额外的构建脚本，它会进一步配置构建，通常会在构建内部使用。脚本插件可以从本地文件系统或远程获取，如果从文件系统获取则是相对于项目目录，如果是远程获取则是由HTTP URL指定。

- 二进制插件 `apply plugin: 'java'`
  是实现了Plugin接口的类，并且采用编程的方式来操纵构建。

- 使用构建脚本块应用插件

  可以向构建脚本中加入插件的类路径，然后再应用，注意buildscript中包含的repositories和dependencies是为gradle服务的，而不是为项目服务的，所以有必要在gradle中再写一遍相似的代码以引进仓库和类库

  ```groovy
  buildscript {
      repositories {
          jcenter()
      }
      dependencies {
          classpath "com.jfrog.bintray.gradle:gradle-bintray-plugin:0.4.1"
      }
  }
  
  apply plugin: "com.jfrog.bintray"
  ```

> ```groovy
> // 用于引入Gradle的插件，保证Gradle脚本自身的运行
> buildscript {
>     repositories {
>         
>     }
>     
>     dependencies {
>         
>     }
> }
> 
> // 用于多项目构建，为所有项目提供依赖包
> allprojects {
>     repositories {
>         
>     }
>     
>     dependencies {
>         
>     }
> }
> 
> // 在子项目的build.gradle中定义该项目需要的依赖
> repositories {
>      
> }
> dependencies {
>      
> }
> ```
>
> 

### 实例

**单个基础Java项目构建**

* 在src/main/java路径下找到源代码
* 在src/test/java路径下找到测试代码
* 任何src/main/resources路径的文件都会被包含在JAR文件里
* 任何src/test/resources路径的文件都会被加入到classpath中以运行测试代码
* 所有的输出文件将会被创建在构建目录里
* JAR文件存放在 build/libs文件夹里。

```groovy
//把Java插件加入到项目中，也就是许多预定制的任务被自动加入到了项目里
apply plugin: 'java'
```

**单个具有外部依赖的Java项目构建**

```groovy
//加入Maven仓库
repositories {
    mavenCentral()
}
```

**定制构建项目**

定制属性

```groovy
//定制 MANIFEST.MF 文件
sourceCompatibility = 1.5
version = '1.0'
jar {
    manifest {
        attributes 'Implementation-Title': 'Gradle Quickstart', 'Implementation-Version': version
    }
}
```

定制任务

```groovy
//测试阶段加入一个系统属性
test {
    systemProperties 'property': 'value'
}
```

**发布JAR文件**

```groovy
//uploadArchives task
uploadArchives {
    repositories {
       flatDir {
           dirs 'repos'
       }
    }
}
```

**多Java项目构建**

在Gradle中为了定义一个多项目构建我们需要创建一个设置文件（settings.gradle），设置文件放在源代码的根目录，它用来指定要包含哪个项目且名字必须叫做settings.gradle。如下例子：

```groovy
//多项目工程结构树：
multiproject/
  api/
  services/webservice/
  shared/
```

```groovy
//多项目构建settings.gradle文件
include "shared", "api", "services:webservice", "services:shared"
```

对于大多数多项目构建有一些配置对所有项目都是通用的，所以我们将在根项目里定义一个这样的通用配置（配置注入技术 configuration injection）。 根项目就像一个容器，subprojects方法遍历这个容器的所有元素并且注入指定的配置。如下：

```groovy
//多项目构建通用配置
subprojects {
    apply plugin: 'java'
    apply plugin: 'eclipse-wtp'

    repositories {
       mavenCentral()
    }

    dependencies {
        testCompile 'junit:junit:4.11'
    }

    version = '1.0'

    jar {
        manifest.attributes provider: 'gradle'
    }
}
```

可以看见，上面通用配置把Java插件应用到了每一个子项目中。

我们还可以在同一个构建里加入项目之间的依赖，这样可以保证他们的先后关系。如下：

```groovy
//api/build.gradle
dependencies {
    compile project(':shared')
}
```

[Gradle脚本基础全攻略](https://blog.csdn.net/yanbober/article/details/49314255)

# Android Gradle

## 构建过程

![img](https://developer.android.com/images/tools/studio/build-process_2x.png)

## 配置文件

### settings.gradle

gradle设置文件

```groovy
include ‘:app’
```

### 顶级build.gradle

定义适用于项目中所有模块的构建配置

```groovy
/**
 * The buildscript {} block is where you configure the repositories and
 * dependencies for Gradle itself--meaning, you should not include dependencies
 * for your modules here. For example, this block includes the Android plugin for
 * Gradle as a dependency because it provides the additional instructions Gradle
 * needs to build Android app modules.
 */

buildscript {

    /**
     * The repositories {} block configures the repositories Gradle uses to
     * search or download the dependencies. Gradle pre-configures support for remote
     * repositories such as JCenter, Maven Central, and Ivy. You can also use local
     * repositories or define your own remote repositories. The code below defines
     * JCenter as the repository Gradle should use to look for its dependencies.
     */

    repositories {
        jcenter()
    }

    /**
     * The dependencies {} block configures the dependencies Gradle needs to use
     * to build your project. The following line adds Android Plugin for Gradle
     * version 3.1.0 as a classpath dependency.
     */

    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.0'
    }
}

/**
 * The allprojects {} block is where you configure the repositories and
 * dependencies used by all modules in your project, such as third-party plugins
 * or libraries. Dependencies that are not required by all the modules in the
 * project should be configured in module-level build.gradle files. For new
 * projects, Android Studio configures JCenter as the default repository, but it
 * does not configure any dependencies.
 */

allprojects {
    repositories {
        jcenter()
    }
}
```

### 模块级 build.gradle

```groovy
/**
 * The first line in the build configuration applies the Android plugin for
 * Gradle to this build and makes the android {} block available to specify
 * Android-specific build options.
 */

apply plugin: 'com.android.application'

/**
 * The android {} block is where you configure all your Android-specific
 * build options.
 */

android {

    /**
       * compileSdkVersion specifies the Android API level Gradle should use to
       * compile your app. This means your app can use the API features included in
       * this API level and lower.
       *
       * buildToolsVersion specifies the version of the SDK build tools, command-line
       * utilities, and compiler that Gradle should use to build your app. You need to
       * download the build tools using the SDK Manager.
       */

    compileSdkVersion 26
    buildToolsVersion "27.0.3"

    /**
       * The defaultConfig {} block encapsulates default settings and entries for all
       * build variants, and can override some attributes in main/AndroidManifest.xml
       * dynamically from the build system. You can configure product flavors to override
       * these values for different versions of your app.
       */

    defaultConfig {

        /**
     * applicationId uniquely identifies the package for publishing.
     * However, your source code should still reference the package name
     * defined by the package attribute in the main/AndroidManifest.xml file.
     */

        applicationId 'com.example.myapp'

        // Defines the minimum API level required to run the app.
        minSdkVersion 15

        // Specifies the API level used to test the app.
        targetSdkVersion 26

        // Defines the version number of your app.
        versionCode 1

        // Defines a user-friendly version name for your app.
        versionName "1.0"
    }

    /**
   * The buildTypes {} block is where you can configure multiple build types.
   * By default, the build system defines two build types: debug and release. The
   * debug build type is not explicitly shown in the default build configuration,
   * but it includes debugging tools and is signed with the debug key. The release
   * build type applies Proguard settings and is not signed by default.
   */

    buildTypes {

        /**
     * By default, Android Studio configures the release build type to enable code
     * shrinking, using minifyEnabled, and specifies the Proguard settings file.
     */

        release {
            minifyEnabled true // Enables code shrinking for the release build type.
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

    /**
   * The productFlavors {} block is where you can configure multiple product
   * flavors. This allows you to create different versions of your app that can
   * override defaultConfig {} with their own settings. Product flavors are
   * optional, and the build system does not create them by default. This example
   * creates a free and paid product flavor. Each product flavor then specifies
   * its own application ID, so that they can exist on the Google Play Store, or
   * an Android device, simultaneously.
   */

    productFlavors {
        free {
            applicationId 'com.example.myapp.free'
        }

        paid {
            applicationId 'com.example.myapp.paid'
        }
    }

    /**
   * The splits {} block is where you can configure different APK builds that
   * each contain only code and resources for a supported screen density or
   * ABI. You'll also need to configure your build so that each APK has a
   * different versionCode.
   */

    splits {
        // Screen density split settings
        density {

            // Enable or disable the density split mechanism
            enable false

            // Exclude these densities from splits
            exclude "ldpi", "tvdpi", "xxxhdpi", "400dpi", "560dpi"
        }
    }
}

/**
 * The dependencies {} block in the module-level build configuration file
 * only specifies dependencies required to build the module itself.
 */

dependencies {
    compile project(":lib")
    compile 'com.android.support:appcompat-v7:27.1.1'
    compile fileTree(dir: 'libs', include: ['*.jar'])
}
```

### 源集

Android Studio 按逻辑关系将每个模块的源代码和资源分组为*源集*。模块的 `main/` 源集包括其所有构建变体共用的代码和资源。其他源集目录为可选项，在您配置新的构建变体时，Android Studio 不会自动为您创建这些目录。不过，创建类似于 `main/` 的源集有助于让 Gradle 只应在构建特定应用版本时使用的文件和资源井然有序：

- `src/main/`

  此源集包括所有构建变体共用的代码和资源。

- `src/<buildType>/`

  创建此源集可加入特定构建类型专用的代码和资源。

- `src/<productFlavor>/`

  创建此源集可加入特定产品风味专用的代码和资源。

- `src/<productFlavorBuildType>/`

  创建此源集可加入特定构建变体专用的代码和资源。

例如，要生成应用的“完整调试”版本，构建系统需要合并来自以下源集的代码、设置和资源：

- `src/fullDebug/`（构建变体源集）
- `src/debug/`（构建类型源集）
- `src/full/`（产品风味源集）
- `src/main/`（主源集）

**注**：当您在 Android Studio 中使用 **File > New** 菜单选项新建文件或目录时，可以针对特定源集进行创建。可供您选择的源集取决于您的构建配置，如果所需目录尚不存在，Android Studio 会自动创建。

如果不同源集包含同一文件的不同版本，Gradle 将按以下优先顺序决定使用哪一个文件（左侧源集替换右侧源集的文件和设置）：

> 构建变体 > 构建类型 > 产品风味 > 主源集 > 库依赖项

## 应用ID

* applicationId在defaultConfig或者productFlavors中指定
* 可以指定applicationIdSuffix，增加后缀

## 依赖

### 依赖类型

* 子项目 
  * `compile project(':mylibrary')`
* 本地库
  * `compile fileTree(dir: 'libs', include: ['*.jar'])`
  * `compile files('libs/foo.jar', 'libs/bar.jar')`
* 远程库
  *  `compile 'com.example.android:app-magic:12.3'`
  * `compile group: 'com.example.android', name: 'app-magic', version: '12.3'`

### 依赖配置

* compile

  - 指定编译时依赖项。Gradle 将此配置的依赖项添加到类路径和应用的 APK。这是默认配置

* apk

  - 指定 Gradle 需要将其与应用的 APK 一起打包的仅运行时依赖项。
  - 可以将此配置与 JAR 二进制依赖项一起使用
  - 不能与其他库模块依赖项或 AAR 二进制依赖项一起使用

* provided

  - 指定 Gradle 不与应用的 APK 一起打包的编译时依赖项。如果运行时无需此依赖项，这将有助于缩减 APK 的大小。
  - 可以将此配置与 JAR 二进制依赖项一起使用
  - 不能与其他库模块依赖项或 AAR 二进制依赖项一起使用

* variant感知，即productFlavor，buildType，[compile, apk, provided]的组合

  例如，`free`为一个flavor，可以配置`freeCompile`，`debugCompile`，`freeDebugCompile`

### 依赖查看

在Studio的Gradle窗口中，展开\<AppName\>>Tasks>android运行androidDependencies，Run窗口会显示输出

## 优化构建速度

### 配置构建变体

* 仅包含需要的配置
* 组合使用flavor

### 避免编译不必要资源

```groovy
productFlavors {
    dev {

        // The following configuration limits the "dev" flavor to using
        // English stringresources and xxhdpi screen-density resources.
        resConfigs "en", "xxhdpi"
    }

}
```

### 停用Crashlytics

```groovy
buildTypes {
    debug {
        ext.enableCrashlytics = false // 停用Crashlytics
        ext.alwaysUpdateBuildId = false // 阻止 Crashlytics 不断更新其构建 ID
    }
```

### 动态代码版本

```groovy
int MILLIS_IN_MINUTE = 1000 * 60
int minutesSinceEpoch = System.currentTimeMillis() / MILLIS_IN_MINUTE

android {
    defaultConfig {
        // Making either of these two values dynamic in the defaultConfig will
        // require a full APK build and reinstallation because the AndroidManifest.xml
        // must be updated (which is not supported by Instant Run).
        versionCode 1
        versionName "1.0"
    }

    // The defaultConfig values above are fixed, so your incremental builds don't
    // need to rebuild the manifest (and therefore the whole APK, slowing build times).
    // But for release builds, it's okay. So the following script iterates through
    // all the known variants, finds those that are "release" build types, and
    // changes those properties to something dynamic.
    applicationVariants.all { variant ->
        if (variant.buildType.name == "release") {
            variant.mergedFlavor.versionCode = minutesSinceEpoch;
            variant.mergedFlavor.versionName = minutesSinceEpoch + "-" + variant.flavorName;
        }
    }
}
```

### 配置 dexOptions 和启用库预 dexing

```groovy
android {
    dexOptions {
        // Sets the maximum number of DEX processes
        // that can be started concurrently.
        maxProcessCount 8
        // Sets the maximum memory allocation pool size
        // for the dex operation.
        javaMaxHeapSize "2g"
        // Enables Gradle to pre-dex library dependencies.
        preDexLibraries true
        // ignore indexes limits
        jumbomode true
    }
}
```

### 增加 Gradle 的堆大小并启用 dex-in-process

gradle.properties

```properties
org.gradle.jvmargs = -Xmx2048m
```

### 停用PNG转换为WebP

在对 WebP 图像进行解压缩时，设备的 CPU 使用率有小幅上升

```groovy
android {
    aaptOptions {
        cruncherEnabled false
    }
}
```

### 其他

* 避免使用版本号和加号，例如1.+

* 启动offline work
* 启用Configure on demand
* 图像转换为WebP
* 停用PNG处理

### 分析方式

命令行中执行`gradlew --profile --recompile-scripts --offline --rerun-tasks projectName:taskName`，会生成html报告

## 配置构建变体

变体名: `[flavor1][flavor2][buildType]`

apk名:  `app-[flavor1]-[flavor2]-[buildType].apk`

### buildTypes

```groovy
buildTypes {
    release {
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
    }

    debug {
        applicationIdSuffix ".debug"
    }

    /**
         * The 'initWith' property allows you to copy configurations from other build types,
         * so you don't have to configure one from the beginning. You can then configure
         * just the settings you want to change. The following line initializes
         * 'jnidebug' using the debug build type, and changes only the
         * applicationIdSuffix and versionNameSuffix settings.
         */

    jnidebug {

        // This copies the debuggable attribute and debug signing configurations.
        initWith debug

        applicationIdSuffix ".jnidebug"
        jniDebuggable true
    }
}
```

### Flavor

```groovy
productFlavors {
    demo {
        applicationIdSuffix ".demo"
        versionNameSuffix "-demo"
    }
    full {
        applicationIdSuffix ".full"
        versionNameSuffix "-full"
    }
}
```

### 组合

```groovy
flavorDimensions "api", "mode"

productFlavors {
    demo {
        // Assigns this product flavor to the "mode" flavor dimension.
        dimension "mode"

    }

    // Configurations in the "api" product flavors override those in "mode"
    // flavors and the defaultConfig {} block. Gradle determines the priority
    // between flavor dimensions based on the order in which they appear next
    // to the flavorDimensions property above--the first dimension has a higher
    // priority than the second, and so on.
    minApi24 {
        dimension "api"
        minSdkVersion '24'
        // To ensure the target device receives the version of the app with
        // the highest compatible API level, assign version codes in increasing
        // value with API level. To learn more about assigning version codes to
        // support app updates and uploading to Google Play, read Multiple APK Support
        versionCode 30000 + android.defaultConfig.versionCode
        versionNameSuffix "-minApi24"

    }
}
```

### 过滤

```groovy
android {

    buildTypes {...}

    flavorDimensions "api", "mode"
    productFlavors {
        demo {...}
        full {...}
        minApi24 {...}
        minApi23 {...}
        minApi21 {...}
    }

    variantFilter { variant ->
        def names = variant.flavors*.name
        // To check for a certain build type, use variant.buildType.name == "<buildType>"
        if (names.contains("minApi21") && names.contains("demo")) {
            // Gradle ignores any variants that satisfy the conditions above.
            setIgnore(true)
        }
    }
}
```

### 查看源集

Tasks > android 并双击 sourceSets

### 更改默认源集

```groovy
android {

    sourceSets {
        // Encapsulates configurations for the main source set.
        main {
            // Changes the directory for Java sources. The default directory is
            // 'src/main/java'.
            java.srcDirs = ['other/java']

            // If you list multiple directories, Gradle uses all of them to collect
            // sources. Because Gradle gives these directories equal priority, if
            // you define the same resource in more than one directory, you get an
            // error when merging resources. The default directory is 'src/main/res'.
            res.srcDirs = ['other/res1', 'other/res2']

            // Note: You should avoid specifying a directory which is a parent to one
            // or more other directories you specify. For example, avoid the following:
            // res.srcDirs = ['other/res1', 'other/res1/layouts', 'other/res1/strings']
            // You should specify either only the root 'other/res1' directory, or only the
            // nested 'other/res1/layouts' and 'other/res1/strings' directories.

            // For each source set, you can specify only one Android manifest.
            // By default, Android Studio creates a manifest for your main source
            // set in the src/main/ directory.
            manifest.srcFile 'other/AndroidManifest.xml'

        }

        // Create additional blocks to configure other source sets.
        androidTest {

            // If all the files for a source set are located under a single root
            // directory, you can specify that directory using the setRoot property.
            // When gathering sources for the source set, Gradle looks only in locations
            // relative to the root directory you specify. For example, after applying the
            // configuration below for the androidTest source set, Gradle looks for Java
            // sources only in the src/tests/java/ directory.
            setRoot 'src/tests'

        }
    }
}
```

### 配置签署设置

```groovy
android {
    defaultConfig { ... }

    // Encapsulates signing configurations.
    signingConfigs {
        // Creates a signing configuration called "release".
        release {
            // Specifies the path to your keystore file.
            storeFile file("my-release-key.jks")
            // Specifies the password for your keystore.
            storePassword "password"
            // Specifies the identifying name for your key.
            keyAlias "my-alias"
            // Specifies the password for your key.
            keyPassword "password"
        }
    }
    buildTypes {
        release {
            // Adds the "release" signing configuration to the release build type.
            signingConfig signingConfigs.release
        }
    }
}
```

## 构建多个APK

为了减小APK大小，根据指定的屏幕尺寸和ABI编译生成多个APK文件

命名格式：modulename-screendensityABI-buildvariant.apk

例如，myApp-mdpiX86-release.apk

### 指定屏幕尺寸

```groovy
android {
    splits {
        // Configures multiple APKs based on screen density.
        density {
            // Configures multiple APKs based on screen density.
            enable true
            // Specifies a list of screen densities Gradle should not create multiple APKs for.
            exclude "ldpi", "xxhdpi", "xxxhdpi"
            // Specifies a list of compatible screen size settings for the manifest.
            compatibleScreens 'small', 'normal', 'large', 'xlarge'
        }
    }
}
```

### 指定ABI

```groovy
android {
    splits {
        // Configures multiple APKs based on ABI.
        abi {
            // Enables building multiple APKs per ABI.
            enable true
            // By default all ABIs are included, so use reset() and include to specify that we only
            // want APKs for x86 and x86_64.
            // Resets the list of ABIs that Gradle should create APKs for to none.
            reset()
            // Specifies a list of ABIs that Gradle should create APKs for.
            include "x86", "x86_64"
            // Specifies that we do not want to also generate a universal APK that includes all ABIs.
            universalApk false
        }
    }
}
```

## 合并Manifest

清单合并工具通过遵循某些合并启发式算法，并遵守您通过特殊 XML 属性定义的合并首选项，来合并各个文件中的所有 XML 元素

### 合并优先级

有 3 种基本的清单文件可以互相合并，它们的合并优先级如下（按优先级由高到低的顺序）：

1. 清单文件构建变体

   * 如果您的变体有多个源集，则其清单优先级如下：
     1. 构建变体清单（如 src/demoDebug/）
     2. 构建类型清单（如 src/debug/）
     3. 产品定制清单（如 src/demo/）

   * 如果您使用的是定制维度，清单优先级将与每个维度在 flavorDimensions 属性中的列示顺序（按优先级由高到低的顺序排列）对应。

2. 应用模块的主清单文件

3. 所包括库中的清单文件

   如果您有多个库，则其清单优先级与依赖顺序（库出现在 Gradle dependencies 块中的顺序）匹配。

### 合并冲突启发式算法

如果优先级较低的清单中的元素与优先级较高的清单中的任何元素均不匹配，则该元素将被添加至合并清单。 但是，如果有匹配元素，则合并工具会尝试将其中的所有属性合并到相同元素中。如果工具发现两个清单包含相同属性，但值不相同，则会出现合并冲突

## 压缩代码和资源

### 压缩代码

代码压缩通过 ProGuard 提供

1. 检测和移除封装应用中未使用的类、字段、方法和属性，包括自带代码库中的未使用项

   解决 [64k 引用限制](https://developer.android.com/studio/build/multidex.html)

2. 优化字节码，移除未使用的代码指令，以及用短名称混淆其余的类、字段和方法。

   难以被逆向工程

```groovy
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'),
                'proguard-rules.pro'
        }
    }
    productFlavors {
        flavor1 {
        }
        flavor2 {
            proguardFile 'flavor2-rules.pro'
        }
    }
}
```

每次构建时 ProGuard 都会输出下列文件：

* dump.txt 说明 APK 中所有类文件的内部结构。
* mapping.txt 提供原始与混淆过的类、方法和字段名称之间的转换。
* seeds.txt 列出未进行混淆的类和成员。
* usage.txt 列出从 APK 移除的代码。

这些文件保存在 \<module-name>/build/outputs/mapping/release/ 中

Proguard可能错误移除代码的情况包括：

- 当应用引用的类只来自 `AndroidManifest.xml` 文件时
- 当应用调用的方法来自 Java 原生接口 (JNI) 时
- 当应用在运行时（例如使用反射或自检）操作代码时

要修正错误并强制 ProGuard 保留特定代码，请在 ProGuard 配置文件中添加一行 `-keep` 代码。例如：

```groovy
-keep public class MyClass
```

或者，您可以向您想保留的代码添加 `@Keep` 注解。在类上添加 `@Keep` 可原样保留整个类。在方法或字段上添加它可完整保留方法/字段（及其名称）以及类名称。请注意，只有在使用[注解支持库](https://developer.android.com/studio/write/annotations.html)时，才能使用此注解。

### 压缩资源

资源压缩通过适用于 Gradle 的 Android 插件提供

1. 移除封装应用中未使用的资源，包括代码库中未使用的资源。
2. 移除未使用的代码后，移除任何不再被引用的资源。

资源压缩只与代码压缩协同工作。代码压缩器移除所有未使用的代码后，资源压缩器便可确定应用仍然使用的资源。这在您添加包含资源的代码库时体现得尤为明显 - 您必须移除未使用的库代码，使库资源变为未引用资源，才能通过资源压缩器将它们移除

```groovy
android {
    buildTypes {
        release {
            shrinkResources true
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'),
                    'proguard-rules.pro'
        }
    }
}
```

### 自定义保留的资源

res/raw/keep.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
           tools:keep="@layout/l_used*_c,@layout/l_used_a,@layout/l_used_b*"
           tools:discard="@layout/unused2" />
```

### 严格引用检查

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
           tools:shrinkMode="strict" />
```

### 移除未使用的备用资源

资源压缩器只会移除未被您的应用代码引用的资源，这意味着它不会移除用于不同设备配置的[备用资源](https://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources)，例如语言资源，可以使用resConfigs限定使用哪些语言资源

## 多dex

Android 应用 (APK) 文件包含 [Dalvik](https://source.android.com/devices/tech/dalvik/) Executable (DEX) 文件形式的可执行字节码文件，其中包含用来运行您的应用的已编译代码。Dalvik Executable 规范将可在单个 DEX 文件内可引用的方法总数限制在 65,536，其中包括 Android 框架方法、库方法以及您自己代码中的方法。

### dex分包支持

### 5.0之前

使用 Dalvik 运行时来执行应用代码。默认情况下，Dalvik 限制应用的每个 APK 只能使用单个 `classes.dex` 字节码文件。要想绕过这一限制，您可以使用 [Dalvik 可执行文件分包支持库](https://developer.android.com/tools/support-library/features.html#multidex)，它会成为您的应用主要 DEX 文件的一部分，然后管理对其他 DEX 文件及其所包含代码的访问

```groovy
android {
    defaultConfig {
        minSdkVersion 15 
        targetSdkVersion 26
        multiDexEnabled true
    }
}

dependencies {
    compile 'com.android.support:multidex:1.0.1'
}
```

### 5.0之后

使用名为 ART 的运行时，后者原生支持从 APK 文件加载多个 DEX 文件。ART 在应用安装时执行预编译，扫描 `classesN.dex` 文件，并将它们编译成单个 `.oat` 文件，供 Android 设备执行。因此，如果您的 `minSdkVersion` 为 21 或更高值，则不需要 Dalvik 可执行文件分包支持库

```groovy
android {
    defaultConfig {
        minSdkVersion 21 
        targetSdkVersion 26
        multiDexEnabled true
    }
}
```

```java
public class MyApplication extends SomeOtherApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(context);
        Multidex.install(this);
    }
}
```

### 声明主 DEX 文件中需要的类

为 Dalvik 可执行文件分包构建每个 DEX 文件时，构建工具会执行复杂的决策制定来确定主要 DEX 文件中需要的类，以便应用能够成功启动。如果启动期间需要的任何类未在主 DEX 文件中提供，那么应用将崩溃并出现错误 `java.lang.NoClassDefFoundError`。

该情况不应出现在直接从应用代码访问的代码上，因为构建工具能识别这些代码路径，但可能在代码路径可见性较低（如使用的库具有复杂的依赖项）时出现。例如，如果代码使用自检机制或从原生代码调用 Java 方法，那么这些类可能不会被识别为主 DEX 文件中的必需项。

因此，如果收到 `java.lang.NoClassDefFoundError`，则必须使用构建类型中的 [`multiDexKeepFile`](http://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.BuildType.html#com.android.build.gradle.internal.dsl.BuildType:multiDexKeepFile) 或 [`multiDexKeepProguard`](http://google.github.io/android-gradle-dsl/2.2/com.android.build.gradle.internal.dsl.BuildType.html#com.android.build.gradle.internal.dsl.BuildType:multiDexKeepProguard) 属性声明它们，以手动将这些其他类指定为主 DEX 文件中的必需项。如果类在 `multiDexKeepFile` 或 `multiDexKeepProguard` 文件中匹配，则该类会添加至主 DEX 文件。

#### multiDexKeepFile 属性

创建一个名为 `multidex-config.txt` 的文件，与 `build.gradle` 文件在同一目录中

```groovy
com/example/MyClass.class
com/example/MyOtherClass.class
```

```groovy
android {
    buildTypes {
        release {
            multiDexKeepFile file 'multidex-config.txt'
            
        }
    }
}
```

#### multiDexKeepProguard 属性

创建一个名为 `multidex-config.pro` 的文件，，与 `build.gradle` 文件在同一目录中

```groovy
-keep class com.example.MyClass
-keep class com.example.MyClassToo
```

指定包中的所有类

```groovy
-keep class com.example.** { *; } // All classes in the com.example package
```

然后，您可以按以下方式针对构建类型声明该文件：

```groovy
android {
    buildTypes {
        release {
            multiDexKeepProguard 'multidex-config.pro'
        }
    }
}
```

## 管理项目和代码

### 配置项目范围的属性
可以将额外属性添加到顶级 build.gradle 文件的 ext 代码块中，在所有模块间共享这些属性

```groovy
buildscript {...}
allprojects {...}

// This block encapsulates custom properties and makes them available to all
// modules in the project.
ext {
    // The following are only a few examples of the types of properties you can define.
    compileSdkVersion = 26
    buildToolsVersion = "27.0.3"

    // You can also use this to specify versions for dependencies. Having consistent
    // versions between modules can avoid behavior conflicts.
    supportLibVersion = "27.1.1"
}
```

在模块级 build.gradle 文件中

```groovy
android {
    // Use the following syntax to access properties you defined at the project level:
    // rootProject.ext.property_name
    compileSdkVersion rootProject.ext.compileSdkVersion
    buildToolsVersion rootProject.ext.buildToolsVersion
}
dependencies {
    compile "com.android.support:appcompat-v7:${rootProject.ext.supportLibVersion}"
}
```

## 测试应用

### 配置 lint 选项

```groovy
android {
    lintOptions {
        // Turns off checks for the issue IDs you specify.
        disable 'TypographyFractions','TypographyQuotes'
        // Turns on checks for the issue IDs you specify. These checks are in
        // addition to the default lint checks.
        enable 'RtlHardcoded', 'RtlCompat', 'RtlEnabled'
        // To enable checks for only a subset of issue IDs and ignore all others,
        // list the issue IDs with the 'check' property instead. This property overrides
        // any issue IDs you enable or disable using the properties above.
        check 'NewApi', 'InlinedApi'
        // If set to true, turns off analysis progress reporting by lint.
        quiet true
        // if set to true (default), stops the build if errors are found.
        abortOnError false
        // if true, only report errors.
        ignoreWarnings true
    }
}
```

### 配置manifest 设置

```groovy
android {
    // Each product flavor you configure can override properties in the
    // defaultConfig block. To learn more, go to Configure Product Flavors.
    defaultConfig {
        // Specifies the application ID for the test APK.
        testApplicationId "com.test.foo"
        // Specifies the fully-qualified class name of the test instrumentation runner.
        testInstrumentationRunner "android.test.InstrumentationTestRunner"
        // If set to 'true', enables the instrumentation class to start and stop profiling.
        // If set to false (default), profiling occurs the entire time the instrumentation
        // class is running.
        testHandleProfiling true
        // If set to 'true', indicates that the Android system should run the instrumentation
        // class as a functional test. The default value is 'false'
        testFunctionalTest true
    }
}
```

## 简化应用开发

### 与应用的代码共享自定义字段和资源值

在构建时，Gradle 将生成 `BuildConfig` 类，以便应用代码可以检查与当前构建有关的信息。

可以使用 `buildConfigField()` 函数，将自定义字段添加到 Gradle 构建配置文件的 `BuildConfig` 类中，然后在应用的运行时代码中访问这些值。

可以使用 `resValue()` 添加应用资源值。

```groovy
android {
    buildTypes {
        release {
            // These values are defined only for the release build, which
            // is typically used for full builds and continuous builds.
            buildConfigField("String", "BUILD_TIME", "\"${minutesSinceEpoch}\"")
            resValue("string", "build_time", "${minutesSinceEpoch}")
        }
        debug {
            // Use static values for incremental builds to ensure that
            // resource files and BuildConfig aren't rebuilt with each run.
            // If they were dynamic, they would prevent certain benefits of
            // Instant Run as well as Gradle UP-TO-DATE checks.
            buildConfigField("String", "BUILD_TIME", "\"0\"")
            resValue("string", "build_time", "0")
        }
    }
}
```

在应用代码中，可以访问以下属性：

```java
Log.i(TAG, BuildConfig.BUILD_TIME);
Log.i(TAG, getString(R.string.build_time));
```

### 与 manifest 共享属性

某些情况下，可能需要同时在 manifest 和代码中声明相同属性（例如，在为 `FileProvider` 声明机构时）。如以下示例中所示，请在模块的 `build.gradle` 文件中定义一个属性并使其对 manifest 和代码均可用，而不必在多个位置更新相同的属性以反映更改。要了解详情，请阅读[将构建变量注入 Manifest](https://developer.android.com/studio/build/manifest-build-variables.html)。

```groovy
android {
    // For settings specific to a product flavor, configure these properties
    // for each flavor in the productFlavors block.
    defaultConfig {
        // Creates a property for the FileProvider authority.
        def filesAuthorityValue = applicationId + ".files"
        // Creates a placeholder property to use in the manifest.
        manifestPlaceholders = [filesAuthority: filesAuthorityValue]
        // Adds a new field for the authority to the BuildConfig class.
        buildConfigField("String",
                         "FILES_AUTHORITY",
                         "\"${filesAuthorityValue}\"")
    }
}
```

在 manifest 中，访问以下占位符：

```xml
<manifest>
    <application>
        <provider
                  android:name="android.support.v4.content.FileProvider"
                  android:authorities="${filesAuthority}"
                  android:exported="false"
                  android:grantUriPermissions="true">
        </provider>
    </application>
</manifest>
```

访问应用代码中 `FILES_AUTHORITY` 字段的方式类似于如下：

```java
Uri contentUri = FileProvider.getUriForFile(getContext(),
                                            BuildConfig.FILES_AUTHORITY,
                                            myFile);
```

## 其他

### 禁用gradle检查png的合法性

有时会碰到网络图片资源文件后缀由jpg改为png的情况，造成gradle打包检查时编译不通过，可以在buildTypes中添加以下属性，禁用检查png的合法性

```groovy
buildTypes {
        debug {
            aaptOptions.cruncherEnabled = false
            aaptOptions.useNewCruncher = false
        }
    }
```

### 启用zipalign

zipalign可以优化APK包的结构，使得Android系统读取APK中文件时可以按照4K对齐的方式加快读取速度，而不必显式地缓慢读取，例如

```groovy
buildTypes {
        release {
            zipAlignEnabled true
        }
}
```

### 使用packagingOptions修改apk包内容

可以使用packagingOptions排除不想添加到APK中的内容，指定要添加的文件

```groovy
packagingOptions {
    exclude 'META-INF/LICENSE.txt'
    pickFirst 'lib/armeabi-v7a/libxxx.so'
}
```

## 显式指定注解处理器

使用注解编译库，需要显示的声明，例如butterknife是含有注解编译功能的，但是并没有声明，就会报错

```groovy
javaCompileOptions {
    annotationProcessorOptions {
        includeCompileClasspath true
    }
}
```

或

```groovy
provided 'com.jakewharton:butterknife:7.0.1'
annotationProcessor 'com.jakewharton:butterknife:7.0.1'
```

## 指定lint

```groovy
lintOptions {
    // true--关闭lint报告的分析进度
    quiet true
    // true--错误发生后停止gradle构建
    abortOnError false
    // true--只报告error
    ignoreWarnings true
    // true--忽略有错误的文件的全/绝对路径(默认是true)
    //absolutePaths true
    // true--检查所有问题点，包含其他默认关闭项
    checkAllWarnings true
    // true--所有warning当做error
    warningsAsErrors true
    // 关闭指定问题检查
    disable 'TypographyFractions','TypographyQuotes'
    // 打开指定问题检查
    enable 'RtlHardcoded','RtlCompat', 'RtlEnabled'
    // 仅检查指定问题
    check 'NewApi', 'InlinedApi'
    // true--error输出文件不包含源码行号
    noLines true
    // true--显示错误的所有发生位置，不截取
    showAll true
    // 回退lint设置(默认规则)
    lintConfig file("default-lint.xml")
    // true--生成txt格式报告(默认false)
    textReport true
    // 重定向输出；可以是文件或'stdout'
    textOutput 'stdout'
    // true--生成XML格式报告
    xmlReport false
    // 指定xml报告文档(默认lint-results.xml)
    xmlOutput file("lint-report.xml")
    // true--生成HTML报告(带问题解释，源码位置，等)
    htmlReport true
    // html报告可选路径(构建器默认是lint-results.html )
    htmlOutput file("lint-report.html")
    //  true--所有正式版构建执行规则生成崩溃的lint检查，如果有崩溃问题将停止构建
    checkReleaseBuilds true
    // 在发布版本编译时检查(即使不包含lint目标)，指定问题的规则生成崩溃
    fatal 'NewApi', 'InlineApi'
    // 指定问题的规则生成错误
    error 'Wakelock', 'TextViewEdits'
    // 指定问题的规则生成警告
    warning 'ResourceAsColor'
    // 忽略指定问题的规则(同关闭检查)
    ignore 'TypographyQuotes'
}
```

## 钩子

### beforeEvaluate

在解析setting.gradle之后，开始解析build.gradle之前，这里如果要干些事情（更改build.gradle校本内容），可以写在`beforeEvaluate`

### afterEvaluate

在所有build.gradle解析完成后，开始执行task之前，此时所有的脚本已经解析完成，task，plugins等所有信息可以获取，task的依赖关系也已经生成，如果此时需要做一些事情，可以写在`afterEvaluate`

### doFirst和doLast

每个task都可以定义doFirst，doLast，用于定义在此task执行之前或之后执行的代码

[Google——配置构建](https://developer.android.com/studio/build/)