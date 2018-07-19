# Java面试题
[TOC]

# ArrayList，LinkedList和Vector

|          | ArrayList                                               | LinkedList                                       | Vector                                                  |
| -------- | ------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------- |
| 实现     | 动态数组                                                | 链表                                             | 动态数组                                                |
| 随机访问 | 支持                                                    | 不支持                                           | 支持                                                    |
| 随机增删 | 需要移动数据                                            | 不需要移动数据                                   | 需要移动数据                                            |
| 线程安全 | 不支持                                                  | 不支持                                           | 支持                                                    |
| 扩容     | 1.5                                                     | 无                                               | 2                                                       |
| 实现接口 | List，<br>RandomAccess，<br>Cloneable，<br>Serializable | List，<br>Deque，<br>Cloneable，<br>Serializable | List，<br>RandomAccess，<br>Cloneable，<br>Serializable |

[比较ArrayList、LinkedList、Vector](http://blog.csdn.net/renfufei/article/details/17077425)

# Classloader

## 基本概念

与C/C++编写的程序不同，JAVA程序并不是一个可执行文件，而是由许多独立的类文件组成，每一个文件对应一个JAVA类。此外，这些类文件并非全部装入内存，而是根据程序需要逐渐载入。

ClassLoader是JVM实现的一部分，ClassLoader包括bootstrap classloader（启动类加载器），ExtClassLoader（扩展类加载器）和AppClassLoader(系统类加载器)

* **bootstrap classloader** ：在JVM运行的时候加载**JAVA核心的API**，以满足JAVA程序最基本的需求，其中就包括后两种ClassLoader

  负责加载存放在JDK\jre\lib下，或被-Xbootclasspath参数指定的路径中的，并且能被虚拟机识别的类库（如rt.jar，所有的**java.*开头的类均被Bootstrap ClassLoader加载**），包括java.lang.String和java.lang.Object

  启动类加载器是无法被Java程序直接引用的。

* **ExtClassLoader**：该加载器由sun.misc.Launcher$ExtClassLoader实现，它负责加载**JDK\jre\lib\ext**目录中，或者由**java.ext.dirs**系统变量指定的路径中的所有类库（如**javax.*开头的类**），开发者可以直接使用扩展类加载器

* **AppClassLoader**：该类加载器由sun.misc.Launcher$AppClassLoader来实现，它负责加载用户类路径（ClassPath）所指定的类，开发者可以直接使用该类加载器，如果应用程序中没有自定义过自己的类加载器，一般情况下这个就是程序中默认的类加载器

除了Java默认提供的三个ClassLoader之外，用户还可以根据需要定义自已的ClassLoader，而这些自定义的ClassLoader都必须继承自java.lang.ClassLoader类，也包括Java提供的另外2个ClassLoader（Extension ClassLoader和App ClassLoader）在内，但是Bootstrap ClassLoader不继承自ClassLoader，因为它不是一个普通的Java类，底层由C++编写，已嵌入到了JVM内核当中，当JVM启动后，Bootstrap ClassLoader也随着启动，负责加载完核心类库后，并构造Extension ClassLoader和App ClassLoader类加载器

## 加载过程

当运行一个程序的时候，JVM启动，运行bootstrap classloader，加载JAVA核心API，同时加载另两个ClassLoader。然后调用ExtClassLoader加载扩展API，最后AppClassLoader加载CLASSPATH目录下定义的Class.这是最基本的加载流程

## 加载原理

### 双亲委托模式

ClassLoader使用的是双亲委托模型来搜索类的，每个ClassLoader实例都有一个父类加载器的引用（不是继承的关系，是一个包含的关系），虚拟机内置的类加载器（Bootstrap ClassLoader）本身没有父类加载器，但可以用作其它ClassLoader实例的父类加载器。

当一个ClassLoader实例需要加载某个类时，它会试图亲自搜索某个类之前，先把这个任务委托给它的父类加载器，这个过程是由上至下依次检查的，首先由最顶层的类加载器Bootstrap ClassLoader试图加载，如果没加载到，则把任务转交给Extension ClassLoader试图加载，如果也没加载到，则转交给App ClassLoader 进行加载，如果它也没有加载得到的话，则返回给委托的发起者，由它到指定的文件系统或网络等URL中加载该类。如果它们都没有加载到这个类时，则抛出ClassNotFoundException异常。否则将这个找到的类生成一个类的定义，并将它加载到内存当中，最后返回这个类在内存中的Class实例对象

```java
protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    // First, check if the class has already been loaded
    Class c = findLoadedClass(name);
    if (c == null) {
        long t0 = System.nanoTime();
        try {
            if (parent != null) {
                c = parent.loadClass(name, false);
            } else {
                c = findBootstrapClassOrNull(name);
            }
        } catch (ClassNotFoundException e) {
            // ClassNotFoundException thrown if class not found
            // from the non-null parent class loader
        }

        if (c == null) {
            // If still not found, then invoke findClass in order
            // to find the class.
            long t1 = System.nanoTime();
            c = findClass(name);

            // this is the defining class loader; record the stats
        }
    }
    return c;
}
```



### 使用双亲委托的原因

因为这样可以避免重复加载，当父亲已经加载了该类的时候，就没有必要子ClassLoader再加载一次。考虑到安全因素，我们试想一下，如果不使用这种委托模式，那我们就可以随时使用自定义的String来动态替代java核心api中定义的类型，这样会存在非常大的安全隐患，而双亲委托的方式，就可以避免这种情况，因为String已经在启动时就被引导类加载器（Bootstrcp ClassLoader）加载，所以用户自定义的ClassLoader永远也无法加载一个自己写的String，除非你改变JDK中ClassLoader搜索类的默认算法

## class相同的条件

1. 类名是否相同
2. 是否同一个类加载器实例加载的

只有两者同时满足的情况下，JVM才认为这两个class是相同的。就算两个class是同一份class字节码，如果被两个不同的ClassLoader实例所加载，JVM也会认为它们是两个不同class。

比如网络上的一个Java类org.classloader.simple.NetClassLoaderSimple，javac编译之后生成字节码文件NetClassLoaderSimple.class，ClassLoaderA和ClassLoaderB这两个类加载器并读取了NetClassLoaderSimple.class文件，并分别定义出了java.lang.Class实例来表示这个类，对于JVM来说，它们是两个不同的实例对象，但它们确实是同一份字节码文件，如果试图将这个Class实例生成具体的对象进行转换时，就会抛运行时异常java.lang.ClassCaseException，提示这是两个不同的类型

## 常用函数

| 方法                                                   | 说明                                                         |
| ------------------------------------------------------ | ------------------------------------------------------------ |
| `getParent()`                                          | 返回该类加载器的父类加载器。                                 |
| `loadClass(String name)`                               | 加载名称为 `name`的类，返回的结果是 `java.lang.Class`类的实例。 |
| `findClass(String name)`                               | 查找名称为 `name`的类，返回的结果是 `java.lang.Class`类的实例。 |
| `findLoadedClass(String name)`                         | 查找名称为 `name`的已经被加载过的类，返回的结果是 `java.lang.Class`类的实例。 |
| `defineClass(String name, byte[] b, int off, int len)` | 把字节数组 `b`中的内容转换成 Java 类，返回的结果是 `java.lang.Class`类的实例。这个方法被声明为 `final`的。 |
| `resolveClass(Class<?> c)`                             | 链接指定的 Java 类。                                         |

## 自定义Classloader

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

# 对象

## 创建过程

### 检测类是否被加载

虚拟机遇到一条new指令时，首先将去检查这个指令的参数是否能在常量池中**定位到一个类的符号引用**，并且检查这个符号引用代表的类**是否已被加载、解析和初始化过**。如果没有，那必须先执行相应的类加载过程。

### 为新生对象分配内存

为对象分配空间的任务等同于**把一块确定大小的内存从Java堆中划分出来**。分配的方式有两种： 

1. 指针碰撞：假设Java堆中内存是绝对规整的，用过的和空闲的内存各在一边，中间放着一个指针作为分界点的指示器，分配内存就是把那个指针向空闲空间的那边挪动一段与对象大小相等的距离。 
2. 空闲列表：如果Java堆中的内存不是规整的，虚拟机就需要维护一个列表，记录哪个内存块是可用的，在分配的时候从列表中找到一块足够大的空间划分给对象实例，并更新列表上的记录。

采用哪种分配方式是由Java堆是否规整决定的，而Java堆是否规整是由所采用的垃圾收集器是否带有压缩整理功能决定的。 

另外一个需要考虑的问题就是对象创建时的线程安全问题，有两种解决方案：

1. 对分配内存空间的动作进行同步处理；
2. 把内存分配的动作按照线程划分在不同的空间之中进行，即每个线程在Java堆中预先分配一小块内存(TLAB)，哪个线程要分配内存就在哪个线程的TLAB上分配，只有TLAB用完并分配新的TLAB时才需要同步锁定。

### 初始化为零值

内存分配完成后，虚拟机需要将分配到的内存空间都**初始化为零值（不包括对象头）**，这一步操作保证了对象的实例字段在Java代码中可以不赋初始值就直接使用，程序能访问到这些字段的数据类型所对应的零值。

### 必要设置

接下来，虚拟机要对对象进行必要的设置，例如这个对象是哪个类的实例、如何才能找到类的元数据信息、对象的哈希码、对象的GC分代年龄等信息。这些信息存放在对象的对象头之中。

### 执行构造方法

把对象按照程序员的意愿进行初始化

## 内存布局

在HotSpot虚拟机中，对象在内存中存储的布局可分为三个部分： 对象头、实例数据和对齐填充。

对象头包括两个部分：

1. 存储对象自身的运行时数据，如哈希码、GC分代年龄、线程所持有的锁等。官方称之为“Mark Word”。
2. 类型指针，即对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。

实例数据是对象真正存储的有效信息，也是程序代码中所定义的各种类型的字段内容。

对齐填充并不是必然存在的，仅仅起着占位符的作用。、Hotpot VM要求对象起始地址必须是8字节的整数倍，对象头部分正好是8字节的倍数，所以当实例数据部分没有对齐时，需要通过对齐填充来对齐。

## 访问定位

Java程序通过栈上的reference数据来操作堆上的具体对象。主要的访问方式有使用句柄和直接指针两种：

1. 句柄：Java堆将会划出一块内存来作为句柄池，引用中存储的就是对象的句柄地址，而句柄中包含了对象实例数据与类型数据各自的具体地址信息 。如图所示：

   ![Java内存区域详解](http://ww1.sinaimg.cn/mw690/b254dc71gw1eumzdy6lupg20i308waae.gif)

2. 直接指针：Java堆对象的布局要考虑如何放置访问类型数据的相关信息，引用中存储的就是对象地址 。如图所示：

   ![Java内存区域详解](http://ww3.sinaimg.cn/mw690/b254dc71gw1eumzdyjnawg20if08hglw.gif)

两个方式各有优点，使用句柄最大的好处是引用中存储的是稳定的句柄地址，对象被移动时只会改变句柄中实例的地址，引用不需要修改、使用直接指针访问的好处是速度更快，它节省了一次指针定位的时间开销。

# 多态

## 概念

允许基类的指针或引用指向派生类的对象，而在具体访问时实现方法的动态绑定

## 原理

### Java实现方式

Java 对于方法调用动态绑定的实现主要依赖于方法表，但通过类引用调用和接口引用调用的实现则有所不同。总体而言，当某个方法被调用时，JVM 首先要查找相应的常量池，得到方法的符号引用，并查找调用类的方法表以确定该方法的直接引用，最后才真正调用该方法

### JVM结构



![img](https://www.ibm.com/developerworks/cn/java/j-lo-polymorph/image003.jpg)



当程序运行需要某个类的定义时，载入子系统 (class loader subsystem) 装入所需的 class 文件，并在内部建立该类的类型信息，这个类型信息就存贮在方法区。类型信息一般包括该类的方法代码、类变量、成员变量的定义等等。可以说，类型信息就是类的 Java 文件在运行时的内部结构，包含了改类的所有在 Java 文件中定义的信息。

> 该类型信息和 class 对象是不同的。class 对象是 JVM 在载入某个类后于堆 (heap) 中创建的代表该类的对象，可以通过该 class 对象访问到该类型信息。比如最典型的应用，在 Java 反射中应用 class 对象访问到该类支持的所有方法，定义的成员变量等等。可以想象，JVM 在类型信息和 class 对象中维护着它们彼此的引用以便互相访问。两者的关系可以类比于进程对象与真正的进程之间的关系

### Java 的方法调用方式

Java 的方法调用有两类，动态方法调用与静态方法调用。静态方法调用是指对于类的静态方法的调用方式，是静态绑定的；而动态方法调用需要有方法调用所作用的对象，是动态绑定的。类调用 (invokestatic) 是在编译时刻就已经确定好具体调用方法的情况，而实例调用 (invokevirtual) 则是在调用的时候才确定具体的调用方法，这就是动态绑定，也是多态要解决的核心问题。

JVM 的方法调用指令有四个，分别是 invokestatic，invokespecial，invokesvirtual 和 invokeinterface。前两个是静态绑定，后两个是动态绑定的。本文也可以说是对于 JVM 后两种调用实现的考察。

### 常量池（constant pool）

常量池中保存的是一个 Java 类引用的一些常量信息，包含一些字符串常量及对于类的符号引用信息等。Java 代码编译生成的类文件中的常量池是静态常量池，当类被载入到虚拟机内部的时候，在内存中产生类的常量池叫运行时常量池。

常量池在逻辑上可以分成多个表，每个表包含一类的常量信息：

* CONSTANT_Utf8_info：字符串常量表
* CONSTANT_Class_info：类信息表


* CONSTANT_NameAndType_info：名字类型表


* CONSTANT_InterfaceMethodref_info：接口方法引用表


* CONSTANT_Methodref_info：类方法引用表
* ![img](https://www.ibm.com/developerworks/cn/java/j-lo-polymorph/image005.jpg)

可以看到，给定任意一个方法的索引，在常量池中找到对应的条目后，可以得到该方法的类索引（class_index）和名字类型索引 (name_and_type_index), 进而得到该方法所属的类型信息和名称及描述符信息（参数，返回值等）。注意到所有的常量字符串都是存储在 CONSTANT_Utf8_info 中供其他表索引的。

[java多态实现原理](https://blog.csdn.net/huangrunqing/article/details/51996424)

# 反射

## Class

Class保存着运行时信息的类，通过Class可以获取类中的值

### 常用函数

`类名.getClass()`：一般通过这个方法获取某个类的Class对象实例

`Class.forName(String className)`：通过类全名获取某个类的Class对象实例

`类名.class`：获取某个类的Class实例，基本类型也可以通过这种方式调用，例如`int.class`

`newInstance()`：调用无参构造函数创建一个实例

```java
// 调用无参的私有构造函数
Constructor c1 = Class.forName("java.lang.String").getDeclaredConstructor();
c1.setAccessible(true);
String str1 = (String) c1.newInstance();

// 调用有参的私有构造函数
Constructor c2 = Class.forName("java.lang.String").getDeclaredConstructor(new Class[] { String.class });
c2.setAccessible(true);
String str2 = (String) c2.newInstance("hello");
```

`getName`：获取类名+包名，例如`java.lang.String`

`getSimpleName`：获取类名，例如String

`getComponentType`：获取数组的Class对象

`getConstructors`：获取构造器数组，包括超类的共有成员

`getMethods`：返回方法数组，包括超类的公有成员

`getFields`：返回域数组，包括超类的公有成员

`getDeclaredConstructors`：返回全部构造器数组,无论是public/private还是protected,不包括超类的成员

`getDeclaredFields`：返回全部域数组,无论是public/private还是protected,不包括超类的成员

`getDeclaredMethods`：返回全部方法数组,无论是public/private还是protected,不包括超类的成员

`getModifiers`：获取类前的修饰符

## this

`类名.this`会返回类的实例

一般在一个类的内部类中，想要调用外部类的方法或者成员域时，就需要使用`外部名.this.成员域`

# 泛型

泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数

## 原理

1. 类型检查：在生成字节码之前提供类型检查

2. 类型擦除：所有类型参数都用他们的限定类型替换，包括类、变量和方法（类型擦除）

   泛型类型会被自动对应一个原始类型

   原始类型用第一个限定的类型变量来替换，如果没有给定限定就用Object替换

3. 如果类型擦除和多态性发生了冲突时，则在子类中生成桥方法解决

4. 如果调用泛型方法的返回类型被擦除，则在调用该方法时插入强制类型转换

## 优势

1. 类型安全，提供编译期间的类型检测
2. 前后兼容
3. 泛化代码,代码可以更多的重复利用
4. 性能较高，用GJ(泛型JAVA)编写的代码可以为java编译器和虚拟机带来更多的类型信息，这些信息对java程序做进一步优化提供条件

## 泛型方法和泛型类

用泛型类型来替代原始类型，比如使用T, E or K,V等被广泛认可的类型占位符

一般来说，T常用于表示任意类型，K，V常用于表示key和value

```java
// 泛型类
class MyClass<K> {

    // 泛型方法
    public <K, V> V put(K key, V value) {
        return cache.put(key,value);
    }

    public static <T> T getMiddle(T... a) {
        return a[a.length / 2];
    }
}
```

调用时最好指定类型，例如```MyClass<String>.<String>getMiddle("1", "2", "3")```

如果不指定，编译器会自己通过输入参数推断类型。

如果在`getMiddle` 中输入参数类型不同（例如getMiddle(1, 2.0, "3")），视为无效输入，将抛出异常。

## 限定通配符和非限定通配符

有两种限定通配符

* <? extends T>，通过确保类型必须是T的子类来设定类型的上界
* <? super T>，通过确保类型必须是T的父类来设定类型的下界。
* 泛型类型必须用限定内的类型来进行初始化，否则会导致编译错误。

<?>表示非限定通配符，因为<?>可以用任意类型来替代

T 可以是类也可以是接口，在泛型中没有implement关键字

**指定多个绑定类型**：例如<? extends T & U>

**PECS原则（REWS，Read Extends Write Super）**

* 如果要从集合中读取类型T的数据，并且不能写入，可以使用<? extends>通配符；(Producer Extends)

  例如，对于Pair<? extends Employee>

  ```java
  ? extends Employee getFirst(); // 可以将返回值赋给一个Employee引用，相当于有一个上限，合法
  void setFirst(? extends Employee); // 编译器只知道需要某个Employee子类，但不知道具体什么类型，不能赋值给Employee引用进行操作，不合法
  ```

  ​

* 如果要从集合中写入类型T的数据，并且不需要读取，可以使用<? super>通配符；(Consumer Super)

  例如，对于Pair<? super Manager>

  ```java
  ? super Manager getFirst(); // 返回对象不能得到保证，只能返回Object，不合法
  void setFirst(? super Manager); // 虽然不知道具体类型，但可以强制转换为Manager操作，合法
  ```

  ​

* 如果既要存又要取，那么就不要使用任何通配符。

## 类型擦除带来的问题

### 运行时类型查询只适用于原始类型

```java
Pair<String> ps = new Pair<String>();
Pair<Integer> pi = new Pair<Integer>();
if (ps.getClass() == pi.getClass()) // always true, will always return Pair.class
```

这点对于 instanceof 或强制类型转换同样适用

### 不能创建参数化类型的数组

例如

```java
Pair<String>[] table = new Pair<String>[10]; // ERROR
```

table类型是`Pair[]`，擦除后是`Object[]`，如果试图存储其他类型元素，则会抛出ArrayStoreException异常

应该使用`ArrayList<Pair<String>>`

### Varargs警告

例如，以下代码将发出警告，因为ts实际上是一个数组

```java
public <T> void add(Collection<T> c, T... ts) {
    for (T t: ts) c.add(t);
}
```

可以添加`SuppressWarnings("unchecked")`或`@SafeVarargs`消除警告

### 不能实例化类型变量

以下构造方法是非法的，类型擦除将导致`new T()`变为`new Object()`，对于数组同样有效

```java
public Pair() {
    first = new T();
}
```

只能通过反射调用创建实例

```java
public static <T> Pair<T> makePair(Class<T> cl) {
    try {
        return new Pair<>(cl.newInstance());
    } catch (Exception e) {
        return null;
    }
}
```

### 不能在静态域中使用泛型

类型擦除后都将变为Object

### 不能抛出或捕获泛型类的实例

不能抛出也不能捕获泛型类对象

### 不能使用equals(T t)

类型擦除后相当于`equals(Object t)`，与Object中的`equals`冲突，应该重新命名

[java泛型总结2-2 面试题总结](http://zhouchaofei2010.iteye.com/blog/2259899)

# HashMap

## 工作原理

* 基本原理

  HashMap是基于hashing的原理，我们使用`put(key, value)`存储对象到HashMap中，使用`get(key)`从HashMap中获取对象。当我们给`put()`方法传递键和值时，我们先对键调用`hashCode()`方法，返回的hashCode用于找到bucket位置来储存Entry对象


* 两个对象hashcode相同会发生什么

  因为hashcode相同，所以它们的bucket位置相同，碰撞会发生。因为HashMap使用链表存储对象，这个Entry(包含有键值对的Map.Entry对象)会存储在链表中

* 如果两个键的hashcode相同如何获取值对象

  找到bucket位置之后，会调用`keys.equals()`方法去找到链表中正确的节点，最终找到要找的值对象

* 如果HashMap的大小超过了负载因子(load factor)定义的容量怎么办

  默认的负载因子大小为0.75，也就是说，当一个map填满了75%的bucket时候，和其它集合类(如ArrayList等)一样，将会创建原来HashMap大小的两倍的bucket数组，来重新调整map的大小，并将原来的对象放入新的bucket数组中。这个过程叫作rehashing，因为它调用hash方法找到新的bucket位置

* 重新调整HashMap大小存在什么问题

  重新调整HashMap大小的时候存在条件竞争，因为如果两个线程都发现HashMap需要重新调整大小了，它们会同时试着调整大小。在调整大小的过程中，存储在链表中的元素的次序会反过来，因为移动到新的bucket位置的时候，HashMap并不会将元素放在链表的尾部，而是放在头部，这是为了避免尾部遍历(tail traversing)。如果条件竞争发生了，那么就死循环了

* 为什么String, Interger这样的wrapper类适合作为键

  因为String是不可变的，也是final的，而且已经重写了`equals()`和`hashCode()`方法了。其他的wrapper类也有这个特点。不可变性是必要的，因为为了要计算`hashCode()`，就要防止键值改变，如果键值在放入时和获取时返回不同的hashcode的话，那么就不能从HashMap中找到你想要的对象。不可变性还有其他的优点如线程安全

* 可以使用自定义的对象作为键吗

  可以使用任何对象作为键，只要它遵守了`equals()`和`hashCode()`方法的定义规则，并且当对象插入到Map中之后将不会再改变了。如果这个自定义对象时不可变的，那么它已经满足了作为键的条件，因为当它创建之后就已经不能改变了

* 可以使用CocurrentHashMap来代替Hashtable吗

  Hashtable是synchronized的，但是ConcurrentHashMap同步性能更好，因为它仅仅根据同步级别对map的一部分进行上锁。ConcurrentHashMap当然可以代替HashTable，但是HashTable提供更强的线程安全性

**总结**

HashMap基于hashing原理，我们通过`put()`和`get()`方法储存和获取对象。当我们将键值对传递给`put()`方法时，它调用键对象的`hashCode()`方法来计算hashcode，找到bucket位置来储存值对象。当获取对象时，通过键对象的`equals()`方法找到正确的键值对，然后返回值对象。HashMap使用链表来解决碰撞问题，当发生碰撞了，对象将会储存在链表的下一个节点中。 HashMap在每个链表节点中储存键值对对象。

当两个不同的键对象的hashcode相同时会发生什么？ 它们会储存在同一个bucket位置的链表中。键对象的equals()方法用来找到键值对

## HashMap和HashTable的区别

1. 继承的父类不同

   Hashtable继承自Dictionary类

   HashMap继承自AbstractMap类，但二者都实现了Map接口

2. 线程安全性不同

   Hashtable是线程安全的，Hashtable 中的方法是Synchronize的。在多线程并发的环境下，可以直接使用Hashtable，不需要为它的方法实现同步

   HashMap是非线程安全的。HashMap中的方法在缺省情况下是非Synchronize的。在多线程并发的环境下，使用HashMap时就必须要增加同步处理。这一般通过对自然封装该映射的对象进行同步操作来完成。如果不存在这样的对象，则应该使用`Collections.synchronizedMap`方法来“包装”该映射。最好在创建时完成这一操作，以防止对映射进行意外的非同步访问

3. 是否提供contains方法

   Hashtable保留了`contains`，`containsValue`和`containsKey`三个方法，其中`contains`和`containsValue`功能相同

   HashMap把Hashtable的`contains`方法去掉了，改成`containsValue`和`containsKey`，因为`contains`方法容易让人引起误解

4. key和value是否允许null值

   Hashtable中，key和value都不允许出现null值。但是如果在Hashtable中有类似put(null,null)的操作，编译同样可以通过，因为key和value都是Object类型，但运行时会抛出NullPointerException异常，这是JDK的规范规定的

   HashMap中，null可以作为键，这样的键只有一个；可以有一个或多个键所对应的值为null。当get()方法返回null值时，可能是 HashMap中没有该键，也可能使该键所对应的值为null。因此，在HashMap中不能由get()方法来判断HashMap中是否存在某个键， 而应该用containsKey()方法来判断

5. 两个遍历方式的内部实现上不同

   Hashtable、HashMap都使用了 Iterator。而由于历史原因，Hashtable还使用了Enumeration的方式

6. hash值不同

   HashTable直接使用对象的hashCode（jdk根据对象的地址或者字符串或者数字算出来的int类型的数值）

   HashMap重新计算hash值

7. 内部实现使用的数组初始化和扩容方式不同

   HashTable在不指定容量的情况下的默认容量为11，不要求底层数组的容量一定要为2的整数次幂

   HashMap默认容量为16，要求底层数组的容量一定为2的整数次幂。

   Hashtable扩容时，将容量变为原来的2倍加1

   HashMap扩容时，将容量变为原来的2倍

[HashTable和HashMap的区别详解](http://blog.csdn.net/fujiakai/article/details/51585767)

# Hash冲突

## 概念

当关键字值域远大于哈希表的长度，而且事先并不知道关键字的具体取值时，就会发生冲突

当关键字的实际取值大于哈希表的长度时，而且表中已装满了记录，如果插入一个新记录，不仅发生冲突，而且还会发生溢出

## 解决冲突的办法

### 开放定址法

#### 原理

当冲突发生时，使用某种探查(亦称探测)技术在散列表中形成一个探查(测)序列。沿此序列逐个单元地查找，直到找到给定的关键字，或者碰到一个开放的地址(即该地址单元为空)为止（若要插入，在探查到开放的地址，则可将待插入的新结点存人该地址单元）。查找时探查到开放的地址则表明表中无待查的关键字，即查找失败

> 用开放定址法建立散列表时，建表前须将表中所有单元(更严格地说，是指单元中存储的关键字)置空。空单元的表示与具体的应用相关

#### 应用

1. 线性探查法：步长为1，从初始探查地址向后探查
2. 线性补偿探测法：步长为某一常量，从初始探查地址向后探查
3. 随机探测：步长为随机数，从初始探查地址向后探查

### 拉链法

#### 原理

将所有关键字为同义词的结点链接在同一个单链表中

[解决哈希（HASH）冲突的主要方法](http://blog.csdn.net/lightty/article/details/11191971)

# 缓存机制设计

## FIFO

先入先出

```java
public class FIFOCache<K,V> extends LinkedHashMap<K, V>{
    private final int SIZE;

    public FIFOCache(int size) {
        super();//调用父类无参构造，不启用LRU规则
        SIZE = size;
    }

    //重写淘汰机制
    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > SIZE;  //如果缓存存储达到最大值删除最后一个
    }
}
```



## LRU

最近最少使用，意思就是最近读取的数据放在最前面，最早读取的数据放在最后面，如果这个时候有新的数据进来，那么最后面存储的数据淘汰

```java
public class LRUCache<K,V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 5853563362972200456L;

    private final int SIZE;

    public LRUCache(int size) {
        super(size, 0.75f, true);  //int initialCapacity, float loadFactor, boolean accessOrder这3个分别表示容量，加载因子和是否启用LRU规则
        SIZE = size;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > SIZE;
    }
}
```



## LFU

最不常使用，意思就是对存储的数据都会有一个计数引用，然后队列按数据引用次数排序，引用数多的排在最前面，引用数少的排在后面。如果这个时候有新的数据进来，把最后面的数据删除，把新进数据排在最后面，且引用次数为1

```java
public class LFUCache{

    static class Value implements Comparable<Value>{    //定义一个静态内部类，主要是用于统计命中数
        Object key;
        Object val;
        int hitCount;

        public Value(Object v, Object key) {
            this.key = key;
            this.val = v;
            this.hitCount = 1;  //第一次进入设置命中为1
        }

        public void setVal(Object obj){
            this.val = obj;
        }

        public void countInc(){
            hitCount++;
        }

        @Override
        public int compareTo(Value o) {
            if(o instanceof Value){  //如果比较的类属于Value或者是Value的子类
                Value v = (Value) o;
                if(this.hitCount > v.hitCount)
                    return 1;
                else
                    return -1;
            }
            return 0;
        }

    }
    final int SIZE;

    private Map<Object, Value> map = new HashMap<Object, Value>();

    public LFUCache(int size) {
        SIZE = size;
    }

    //获取缓存中的数据
    public Object get(Object k){
        if(k == null)
            return null;

        //命中+1
        map.get(k).countInc();
        return map.get(k).val;
    }

    //存储数据
    public void put(Object k, Object v){
        //如果本来就存在
        if(map.get(k) != null){
            map.get(k).countInc();//命中计数
            map.get(k).setVal(v);//覆盖结果值
        }else{
            //如果存储已超过限定值
            if(map.size() >= SIZE){
                remove();//移除最后一个数据
            }
            Value value  = new Value(v, k);
            map.put(k, value);
        }


        //
    }

    //数据移除最后一个数据
    public void remove(){
        //先拿到最后一个数据
        Value v = Collections.min(map.values());
        //移除最后一个数据
        map.remove(v.key);
    }

}
```

[用Java实现多种缓存机制](https://blog.csdn.net/u012403290/article/details/68926201)

# 进程和线程

## 进程

### 定义

在多任务系中，每一个独立运行的程序就是一个进程，也可以理解为当前正在运行的每一个程序都是一个进程。

### 组成

1. **至少一个可执行程序**，包括代码和初始数据，一般在进程创建时说明。注意可执行程序可以被多进程共享。 

2. **一个独立的进程空间** ，在进程创建时由操作系统分配。 

3. **系统资源**，指在进程创建时及执行过程中，由操作系统分配给进程的系统资源，包括I/O设备、文件等。 

4. **一个执行栈区** ，包含运行现场信息，如子程序调用时所压的栈帧、系统调用时所压的栈帧等

   > 用户栈：进程创建时在用户进程空间定义，用来在用户态运行时保存用户程序现场
   >
   > 核心栈：在操作系统核心空间分配，用来保存中断/异常点现场及在进程运行核态程序后的转子现场

### PCB

操作系统为了管理和控制一个进程必须建立一个表格，描述该进程的存在及状态。这个表格被称为进程控制块（Process Control Block，PCB），存放进程标识、空间、运行状态、资源使用等信息。

### 状态转换

![image](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490762862437&di=37cd64a8730b3b4ad91abc9313f0f9a2&imgtype=0&src=http%3A%2F%2Fimg0.ph.126.net%2F0EAGc-zpq2O6075rs15QSg%3D%3D%2F6598002152516532030.jpg)

## 线程

### 状态

* 新创建（new）：尚未运行，准备工作
* 可运行（runnable）：可能在运行，取决于操作系统给线程的运行时间，抢占式调度采用时间片机制
* 被阻塞（blocked）：尝试获取被其他对象持有的对象锁，直到其他线程释放
* 等待（waiting）：等待其他线程通知调度器一个条件，与Object.wait和Thread.join有关
* 计时等待（timed wating）：保持等待直到超时或接收到通知
* 被终止（terminated）：run方法正常退出或出现异常意外退出

为了减少程序再并发执行时所付出的时空开销，使OS具有更好的并发性。**线程是“进程”中某个单一顺序的控制流，也被称为轻量进程**

### 与进程的对比

1. **调度**。进程是资源拥有的基本单位，线程是调度和分派的基本单位。
2. **并发性**。进程之间可以并发执行，在一个进程中的多个线程之间也可以并发执行。
3. **拥有资源**。 进程可以拥有资源，是系统中拥有资源的一个基本单位。而线程自己不拥有系统资源，但它可以访问其隶属进程的资源。
4. **系统开销** 系统创建进程需要为该进程重新分配系统资源，但创建线程的代价很小。因此多线程的实现多任务并发比多进程实现并发的效率高

### 使用

#### 创建线程

1. 实现Runnable接口

```java
class MyRunnable implements Runnable {
    public void run() {
        //这里是新线程需要执行的任务
    }
}

Runnable r = new MyRunnable();
Thread t = new Thread(r);
```

2. 继承Thread类

```java
class MyThread extends Thread {
    public void run() {
        //这里是线程要执行的任务
    }
}
```

> 由于Java中不允许多继承，自定义的类继承了Thread后便不能再继承其他类，这在有些场景下会很不方便；实现Runnable接口的那个方法虽然稍微繁琐些，但是它的优点在于自定义的类可以继承其他的类

#### 结束线程

1. 退出标志：采用设置一个条件变量的方式，run方法中的while循环会不断的检测flag的值，在想要结束线程的地方将flag的值设置为false就可以

2. stop

   不使用stop的原因：因为它在终止一个线程时会强制中断线程的执行，不管run方法是否执行完了，并且还会释放这个线程所持有的所有的锁对象。这一现象会被其它因为请求锁而阻塞的线程看到，使他们继续向下执行。这就会造成数据的不一致

3. `interrupt`方法可以用来请求终止线程，该方法会将线程的中断状态置位，使用`Thread.currentThread().isInterrupted()`可以判断该线程是否被中断，相比`interrupted()`该方法不会清除中断位

   > 注意sleep会清除中断状态并抛出InterruptedException

```java
public void run() {
    try {
        while(!Thread.currentThread.isInterrupted() && 其他条件) {
            // currentThread返回当前执行线程的Thread对象，isInterrupted用于测试线程是否被终止
        }
    } catch (InterruptedException e) {
        // sleep或wait期间线程被终止
    } finally {
        // 清理工作
    }
    // 退出run方法，终止线程
}
```

### 线程池

基本思想还是一种对象池的思想，开辟一块内存空间，里面存放了众多(未死亡)的线程，池中线程执行调度由池管理器来处理。当有线程任务时，从池中取一个，执行完成后线程对象归池，这样可以避免反复创建线程对象所带来的性能开销，节省了系统的资源。

1. 避免线程的创建和销毁带来的性能开销。
2. 避免大量的线程间因互相抢占系统资源导致的阻塞现象。
3. 能够对线程进行简单的管理并提供定时执行、间隔执行等功能

### 优先级

一个线程可以继承父线程的优先级

setPriority可以设置优先级

优先级的实现高度依赖于系统，不要将程序构建的正确性依赖于优先级

### 未捕获异常处理器

可以实现`Thread.UncaughtExceptionHandler`，通过`setUncaughtExceptionHandler`为线程安装一个处理器，也可以调用`Thread.setDefaultUncaughtExceptionHandler`为所有线程安装一个处理器

### 其他

1. `Thread.start()`与`Thread.run()`的区别

   `Thread.start()`方法(native)启动线程，使之进入就绪状态，当cpu分配时间该线程时，由JVM调度执行`run()`方法

   调用`start()`方法时将创建新的线程，并且执行在`run()`方法里的代码

   但是如果直接调用`run()`方法，它不会创建新的线程，只会执行线程中的代码

2. 在静态方法上使用同步时会发生什么

   同步静态方法时会获取该类的Class对象，所以当一个线程进入同步的静态方法中时，线程监视器获取类本身的对象锁，其它线程不能进入这个类的任何静态同步方法。它不像实例方法，因为多个线程可以同时访问不同实例同步实例方法

3. 当一个同步方法已经执行，线程能够调用对象上的非同步实例方法吗

   可以，一个非同步方法总是可以被调用而不会有任何问题。实际上，Java没有为非同步方法做任何检查，锁对象仅仅在同步方法或者同步代码块中检查。如果一个方法没有声明为同步，即使你在使用共享数据Java照样会调用，而不会做检查是否安全，所以在这种情况下要特 别小心。一个方法是否声明为同步取决于临界区访问(critial section access),如果方法不访问临界区(共享资源或者数据结构)就没必要声明为同步的

4. 在一个对象上两个线程可以调用两个不同的同步实例方法吗

   不能，因为一个对象已经同步了实例方法，线程获取了对象的对象锁。所以只有执行完该方法释放对象锁后才能执行其 它同步方法

5. 什么是死锁

   死锁就是两个或两个以上的线程被无限的阻塞，线程之间相互等待所需资源。这种情况可能发生在当两个线程尝试获取其它资源的锁，而每个线程又陷入无限等待其它资源锁的释放，除非一个用户进程被终止

   **四个必要条件：互斥、请求与保持、不可剥夺、环路等待**

   **四个解决方法：预防、避免、检测、解除**

   > 预防是静态地破坏条件，性能损失大
   >
   > 避免是动态地计算是否可能造成死锁，性能损失小

6. 现在有T1、T2、T3三个线程，你怎样保证T2在T1执行完后执行，T3在T2执行完后执行

   使用`join()`方法

7. `Thread.join()`：让主线程等待子线程结束之后才能继续运行

   在 `Parent.run()` 中，通过` new Child()` 新建 child 子线程（此时 child 处于 NEW 状态），然后调用 `child.start()`（child 转换为 RUNNABLE 状态），再调用 `child.join()`。

   在 Parent 调用 `child.join() `后，child 子线程正常运行，Parant 主线程会等待 child 子线程结束后再继续运行

   ```java
   // 主线程
   public class Parent extends Thread {
       public void run() {
           Child child = new Child();
           child.start();
           child.join();
           // ...
       }
   }

   // 子线程
   public class Child extends Thread {
       public void run() {
           // ...
       }
   }
   ```

## 线程安全

### 定义

非线程安全是指多线程操作同一个对象可能会出现问题

线程安全是多线程操作同一个对象不会有问题

如果你的代码所在的进程中有多个线程在同时运行，而这些线程可能会同时运行这段代码。如果每次运行结果和单线程运行的结果是一样的，而且其他的变量的值也和预期的是一样的，就是线程安全的

线程安全的类：Vector，HashTable，StringBuffer

非线程安全的类：ArrayList，HashMap，StringBuilder

### Lock

Lock可以确保任何时候只有一个线程可以进入临界区

以下代码可以锁定A的一个实例

```java
class A {
    private Lock l;
    private Condition c;
    public A() {
        l = new ReentrantLock(); // 非公平锁
        l = new ReentrantLock(true); // 公平锁
    }
    
    public void f() {
        l.lock();
        try {
            
        } finally {
            l.unlock();// 保证出现异常也可以正常释放锁
        }
    }
}
```

### ReentrantLock中的公平锁和非公平锁

#### 概念

`ReentrantLock`的实现是基于其内部类`FairSync`(公平锁)和`NonFairSync`(非公平锁)实现的。 

公平锁：公平和非公平锁的队列都基于锁内部维护的一个双向链表，表结点Node的值就是每一个请求当前锁的线程。公平锁则在于每次都是依次从队首取值

非公平锁：在等待锁的过程中， 如果有任意新的线程妄图获取锁，都是有很大的几率直接获取到锁的

#### 可重入性

基于`Thread.currentThread()`实现的，如果当前线程已经获得了执行序列中的锁， 那执行序列之后的所有方法都可以获得这个锁

可重入性的实现基于下面代码片段的 `else if` 语句

```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        //...
        // 尝试获取锁成功
    }
    else if (current == getExclusiveOwnerThread()) {
        // 是当前线程，直接获取到锁。实现可重入性。
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

#### 实现分析

`ReentrantLock` 的公平锁和非公平锁都委托了 `AbstractQueuedSynchronizer#acquire` 去请求获取

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

* `tryAcquire` 是一个抽象方法，是**公平与非公平**的实现原理所在。
* `addWaiter` 是将当前线程结点加入等待队列之中。**公平锁在锁释放后会严格按照等到队列去取后续值**，而非公平锁在对于新晋线程有很大优势。
* `acquireQueued` 在多次循环中尝试获取到锁或者将当前线程阻塞。
* `selfInterrupt` 如果线程在阻塞期间发生了中断，调用 `Thread.currentThread().interrupt()`中断当前线程。

公平锁和非公平锁在锁的获取上都使用到了 `volatile `关键字修饰的`state`字段， 这是保证多线程环境下锁的获取与否的核心。 
但是当并发情况下多个线程都读取到 `state == 0`时，则必须用到CAS技术，一门CPU的原子锁技术，可通过CPU对共享变量加锁的形式，实现数据变更的原子操作。 
`volatile `和 CAS的结合是并发抢占的关键

> `volatile`可以保证：
>
> * 任何进程在读取的时候，都会清空本进程里面持有的共享变量的值，强制从主存里面获取
> * 任何进程在写入完毕的时候，都会强制将共享变量的值写会主存。 
> * volatile 会干预指令重排
> * volatile 实现了JMM规范的 happen-before 原则
>
> CAS是CPU提供的一门技术：
>
> * 使用CAS技术可以锁定住元素的值。[Intel开发文档, 第八章](https://www.intel.com/content/www/us/en/architecture-and-technology/64-ia-32-architectures-software-developer-vol-3a-part-1-manual.html) 
> * 编译器在将线程持有的值与被锁定的值进行比较，相同则更新为更新的值。 
> * CAS同样遵循JMM规范的 happen-before 原则。
>
> JMM 允许编译器在指令重排上自由发挥，除非程序员通过 **volatile**等 显式干预这种重排机制，建立起同步机制，保证多线程代码正确运行。见文章：[Java并发：volatile内存可见性和指令重排](http://blog.csdn.net/jiyiqinlovexx/article/details/50989328)。

##### 公平锁

公平锁的实现机理在于每次有线程来抢占锁的时候，都会检查一遍有没有等待队列，如果有， 当前线程会执行如下步骤：

```java
if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
    setExclusiveOwnerThread(current);
    return true;
}
```

其中`hasQueuedPredecessors`是用于检查是否有等待队列的。

```java
public final boolean hasQueuedPredecessors() {
    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    return h != t && ((s = h.next) == null || s.thread != Thread.currentThread());
}
```

##### 非公平锁

非公平锁在实现的时候多次强调随机抢占：

```java
if (c == 0) {
    if (compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(current);
        return true;
    }
}
```

与公平锁的区别在于新晋获取锁的进程会有多次机会去抢占锁。如果被加入了等待队列后则跟公平锁没有区别。

#### 释放锁

ReentrantLock锁的释放是**逐级释放**的，也就是说在**可重入性**场景中，必须要等到场景内所有的加锁的方法都释放了锁，当前线程持有的锁才会被释放！ 
释放的方式很简单，state字段减一即可：

```java
protected final boolean tryRelease(int releases) {
    //  releases = 1
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

#### 等待队列中元素的唤醒

当当前拥有锁的线程释放锁之后， 且非公平锁无线程抢占，就开始线程唤醒的流程。 
通过`tryRelease`释放锁成功，调用`LockSupport.unpark(s.thread);` 终止线程阻塞。

```java
private void unparkSuccessor(Node node) {
    // 强行回写将被唤醒线程的状态
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);
    Node s = node.next;
    // s为h的下一个Node, 一般情况下都是非Null的
    if (s == null || s.waitStatus > 0) {
        s = null;
        // 否则按照FIFO原则寻找最先入队列的并且没有被Cancel的Node
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    // 再唤醒它
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

[Java中的公平锁和非公平锁实现详解](http://blog.csdn.net/qyp199312/article/details/70598480)

### 条件对象

使用条件对象让线程进入等待状态

```java
class A {
    private Lock l;
    private Condition c;
    public A() {
        l = new ReentrantLock();
        c = l.newCondition();
    }
    
    public void f() {
        l.lock();
        try {
            while(需要等待的条件成立) {
                c.await(); // 将该线程放入条件的等待集中，进入阻塞状态，直到另一线程调用同一条件上的signalAll为止
            }
            //...
            c.signalAll();
        } finally {
            l.unlock();// 保证出现异常也可以正常释放锁
        }
    }
}
```

### synchronized

#### 说明

synchronized关键字可以作为函数的修饰符，也可作为函数内的语句，也就是平时说的同步方法和同步语句块。

synchronized可作用于

* instance变量
* object reference（对象引用）
* static函数
* class literals(类名称字面常量)

#### 关键点

1. 无论synchronized关键字加在方法上还是对象上，它**取得的锁都是对象，而不是把一段代码或函数当作锁**，而且同步方法很可能还会被其他线程的对象访问。
2. 每个对象只有一个锁（lock）与之相关联。
3. 实现同步是要很大的系统开销作为代价的，甚至可能造成死锁，所以尽量避免无谓的同步控制

#### 用法说明

1. 函数修饰符:锁定调用这个同步方法对象

   ```java
   public synchronized void methodAAA() {
       while(需要等待的条件成立) {
           wait();
       }
       //….. 
       notifyAll();
   }
   ```

   相当于

   ```java
   public void methodAAA()  
   {  
       synchronized (this) {// this指的就是调用这个方法的对象
           while(需要等待的条件成立) {
               wait();
           }
           //….. 
           notifyAll();
       }  
   }
   ```

2. 同步块：锁定对象o

   ```java
   public void method3(SomeObject o)  
   {  
       synchronized(o)  
       {  
           //…..  
       }  
   } 
   ```

3. 用于static：锁定类字面量

   ```java
   class Foo  
   {  
       public synchronized static void methodAAA() // 同步的static 函数  
       {  
           //….  
       }  
       public void methodBBB()  
       {  
           synchronized(Foo.class) // class literal(类名称字面常量)  
       }  
   }  
   ```

#### synchronized，ReentrantLock和Atomic的区别

* synchronized：
  在资源竞争不是很激烈的情况下，偶尔会有同步的情形下，synchronized是很合适的。原因在于，编译程序通常会尽可能的进行优化synchronize，另外可读性非常好，不管用没用过5.0多线程包的程序员都能理解。
* ReentrantLock:
  ReentrantLock提供了多样化的同步，比如有时间限制的同步，可以被Interrupt的同步（synchronized的同步是不能Interrupt的）等。在资源竞争不激烈的情形下，性能稍微比synchronized差点点。但是当同步非常激烈的时候，synchronized的性能一下子能下降好几十倍。而ReentrantLock确还能维持常态。
* Atomic:
  和上面的类似，不激烈情况下，性能比synchronized略逊，而激烈的时候，也能维持常态。激烈的时候，Atomic的性能会优于ReentrantLock一倍左右。但是其有一个缺点，就是只能同步一个值，一段代码中只能出现一个Atomic的变量，多于一个同步无效。因为他不能在多个Atomic之间同步。

所以，我们写同步的时候，优先考虑synchronized，如果有特殊需要，再进一步优化。ReentrantLock和Atomic如果用的不好，不仅不能提高性能，还可能带来灾难。

### volatile

保证了新值能立即存储到主内存，每次使用前立即从主内存中刷新。 

禁止指令重排序优化

> 编译器和处理器为了提高性能，而在程序执行时会对程序进行的重排序。它的出现是为了提高程序的并发度，从而提高性能！但是对于多线程程序，重排序可能会导致程序执行的结果不是我们需要的结果！重排序分为编译器和处理器两个方面，而处理器重排序又包括指令级重排序和内存的重排序

[JAVA线程安全之synchronized关键字的正确用法](http://blog.csdn.net/yaerfeng/article/details/7254734)

[Java线程安全和非线程安全](http://blog.csdn.net/xiao__gui/article/details/8934832)

# 接口和抽象类

## 接口

1. 接口中的成员变量默认都是static和final类型的。成员变量在定义的时候必须直接初始化他。 
2. 接口中的方法默认都是abstract类型的。 
3. 接口中的成员变量和成员方法的访问权限都是public类型 
4. 接口可继承接口，并可多继承接口，但类只能单继承。 
5. 一个类可以实现多个接口，多个接口名之间用逗号间隔。

## 抽象类

### 概念

抽象方法是一种特殊的方法：它只有声明，而没有具体的实现。

```java
abstract void fun();
```

抽象方法必须用abstract关键字进行修饰。如果一个类含有抽象方法，则称这个类为抽象类，抽象类 必须在类前用abstract关键字修饰。因为抽象类中含有无具体实现的方法，所以**不能用抽象类创建对象**

```java
[public] abstract class ClassName {
    abstract void fun();
}
```

### 和普通类的区别

1. 抽象方法必须为public或者protected。 
2. 抽象类不能创建对象 
3. 如果一个类继承于一个抽象类，则子类必须实现父类的抽象方法。如果子类没有实现父类的抽象方法，则必须将子类也定义为abstract类

## 对比

|   参数    |                   抽象类                    |                   接口                   |
| :-----: | :--------------------------------------: | :------------------------------------: |
| 默认的方法实现 |                可以有默认的方法实现                |             完全抽象的，不存在方法的实现             |
|   实现    | 子类使用extends关键字来继承抽象类。如果子类不是抽象类的话，它需要提供抽象类中所有声明的方法的实现 | 子类使用implements来实现接口。它需要提供接口中所有声明的方法的实现 |
|   构造器   |                抽象类可以有构造器                 |                接口不能有构造器                |
| 与普通类的区别 |            除了不能实例化抽象类以外，没有区别             |               接口是完全不同的类型               |
|  访问修饰符  |   抽象方法可以有public、protacted和default这些修饰符   |       接口方法默认修饰符是public，不可使用其他修饰符       |
| main方法  |           抽象方法可以有main方法并且可以运行            |            接口没有main方法，不能运行             |
|   多继承   |            抽象类可以继承一个类和实现多个接口             |            接口只可以继承一个或者多个接口             |
|  添加新方法  | 如果你往抽象类中添加新的方法，你可以给它提供默认的实现。因此你不需要改变你现在的代码。 |      如果你往接口中添加方法，那么你必须改变实现该接口的类。       |

# 类构造顺序

## 执行顺序

静态优先执行，父类优先于子类执行。静态代码块是在JVM加载类的时候执行的，而且静态代码块执行且仅执行一次



![img](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAlAAAAHVCAIAAACxFVLkAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAGotSURBVHhe7Z1Z0F1Vmf5z65U3Vlle9AWWXFjlhaVVWhWhyyoKlIAQoIMgNIEQmYcEIrPQiUQSGkkgyCiDgOXfDrREGhnTFCIEWuy0hKAYhqRbGTIAAcKQAf6/8z3wstjnfEO+76x99vCsOnVqn7XXetdaz15r/c679jTpAwcrYAWsgBWwAi1QYFIL2ugmWgErYAWsgBX4YELAW7Zs2YsvvigVr7jiip5yPpOEW265pTsNRh588EHin3jiiXRvz8Rpgvnz58dPjFBOwTiRb775JpFs+FBbAStgBaxAyxWYEPDQbrfddhNRQAsh0DJnzpwgEDyDixCxwDClgVugjg1+pgeDGEGUvUpASNF1wAEHRPqjjz5abEtDJChYbvkhb3Dzu/vAqI1N/2bFv7cRchX6cOFfWnfGNIH+2BVCFDqW0kdtjhNYASswggLDAo/hBzC63SbZYi+jl8EM8EgDb/hmLojhLT4xAQlXJOObLDIogEEvAjQio+DHBt/sBXWxi7LCmQt0RSQbBE1DKQ4pK+Ym5aIyo3qN7iv1UoAjq36o/0b0k0KPjW5G3+Do06ULiCImMNn9x4jE6qX0cIrQn7MUq+kfu5AuZVhqM/2LFomJVHqKqJf4rq0VqJ0CvYHHNME4FKWGaxLjn70awxrVhfmCYSzaxa74GTMCMwjjnO+eq45hMDZUHDOOsjCFiYuqJPZj1iBl/KFWgjSmdsepzRXW8gBHvNsH0pHlQKsz6I9RT4e+Zx/TAkNoq216V4Ga9KtIFnUQCClU//YERf3PC7BRseiEsZ22Iq1AVHu4f5lt7gNuuxXoiwIjLWmODDz5VQE8xjMzhYa3/nQzgMNXIxkzghiZzi+aSvjGGvNFOlUpu9YqYwYprFIqL5GxLirgUYG0LEzJWl8ks5GSFZAbxwHddddd06J1Cpld8ttIFt0g/gDJRQsKiiXRzegtJNDKBEZkR9CSBa0KqC9FEWkdSDDc3zJVTERMkUklg4LaJYPxrzFlcMlSuzgr0GwFxgO8cN20/Kjv2CjwrKeHl643psM7rnwRHYNS4bcVJpfAmwqlYpqqyB7eJD9HJnezD3CTWgfwUvdIawNiRvofK1YLBDOtmcMeuYnhKWJKKJVEPVcalEa46skh/SGLJQSZir9l6p98qwIki5VVddcC8EjmVfcm9Vi3pWoKjAd4aoP+8GphR+s5hWVJ/d3WabnCSb74H82GaKS/1RrwOi2nWUynWApLmjqBR7wmlPhjHotaqh5BKX12pGrdbhz14VDqCqk0qLfEnxsSFJY95ZypB+pfVGSXYxd/yLS62H2xsZxL/ZFSlwtGynvjW2f1goixIWvqfnHmT5d3EZNWQN3YtBtHx3AWKzB2BSYKPIGK0L1mKODFhKIl0FjAURXl6vU8gRd5Nakpffx3TpdAI1L/xIPHUVx6kq/7PNDYxXLKASoAzApXOarz6E+V/h6RptCdCksC+lMVrVAfi0g5f+m6OsQSh7QqrsUM9TdlFCPV+cOyOqFqRRphj7rFX0Pixbzo5GTpeUnLAAV30VageQr0Bp6uCmEFiZE83A12KV309zZgpl1y1zSwhRm5ZamI4XuxUUCR5gLSRwVUK2XXxIR9TUkBuRR4UZ8UeD3h2rzj2rAWyZEqNIpDLwSqq2jFUt1MKXWdiDCjs8j0hNTdFybTU3Ry2lLmkSZWNdP/XipCZWkhobsTqnOqH0YvLYwUVY8EUaj/kzWs97o51VFgWA9Pk4j+ovasLru0TJT+b1XKgIqwRDJdEcCf3JhuSFM4KUJK/QuOiSM91cfcEf/xCzcYpEuaQUed1JEp/T1XTbxqVJ3ON8aaiEDxByjNRefUWTF1m0KPSv+rBU7oGNEJsamM6Zpk+p9MnZyY+HunjhR1COBFvwqnjTRa+aB/MhCGA17Pv2UROUaJnMwKWIGxKDDRG8/HUobTWIGJKBAXm0CXdFUzThvrj5FWJuNMcPxR08JjumAI5AoriqpegZfhe8k1DHzqhHRkiUUIFZSeR0yJW1jSjH91wTZdNUOgrEJNJqKe81oBKxAKGHjuDFbAClgBK9AKBQy8VhxmN9IKWAErYAUMPPcBK2AFrIAVaIUCBl4rDrMbaQWsgBWwAgae+4AVsAJWwAq0QgEDrxWH2Y20AlbAClgBA899wApYAStgBVqhgIHXisPsRloBK2AFrICB5z5gBayAFbACrVDAwGvFYXYjrYAVsAJWwMBzH7ACVsAKWIFWKGDgteIwu5FWwApYAStg4LkPWAErYAWsQCsUMPBacZjdSCtgBayAFTDw3AesgBWwAlagFQoYeK04zG5kSxSYMWPGNx1qrsDMmTNb0l3Lb6aBV77mLtEK5FKAqX6zQ80V4CDm6h+tt2vgtb4LWIAGKWDg1Rx2nepzEF988cUNGzY0qGNWpSkGXlWOhOthBSaugIHXGODBvIn3B1soKGDguUtYgeYoAPBed6i5AvLwDLwcw9LAy6GqbVqBwShg4NUcdp3qG3j5Bo+Bl09bW7YCZStg4Bl4Zfe5WpVn4NXqcLmyVmBEBQDeaw41V8AeXr5RbuDl09aWrUDZChh4NYddp/oGXr5hY+Dl09aWrUDZChh4Bl7Zfa5W5Rl4tTpcrqwVGG1J81WHmitgDy/fKDfw8mlry1agbAWYK2s+27v6rxp4+YaNgZdPW1u2AmUrYOA1AJgGXr5hY+Dl09aWrUDZCjBXbnKouQIGXr5hY+Dl09aWrUDZChh4NYddp/oGXr5hY+Dl09aWrUDZChh4Bl7Zfa5W5Rl4tTpcrqwVGO0qzY0ONVfAHl6+UW7g5dPWlq1A2QowV9Z8tq9i9detW1dmtQy8fMPGwMunrS1bgbIVGCPwli1bduuttzKJr169uns2X7hwIbuWL19++OGHs3HiiSc+/vjj6YxPjH6ee+65JMNINw+uGgojc2KfffYhL0YKybBP9SJEcSSjtorHuDZUyb4H2hX1p4YFBaRP1JyGdFcAGSVyhMLP4eps4OUbNgZePm1t2QqUrQBzJS8OHTmsXbuWCZpvJbvllluYuNMsTz31FIx54IEHiL/jjjuuvPLK2Ms28dr72GOPscH3YYcdJmt881OBXSTWdsFIWKMaqgAhrQB505+F6rGL0glsqKppYorTrjEG0vdMGUWwt7sCqkPEF+pQqFhoQpaQfYTqGXj5ho2Bl09bW7YCZSswKvCYcAvTNzGQCWgxBbMh/LABjYhcsGCBYsQkbRBPAsFPMEuhqKmc7AWMpfGy8/Wvfz2KSxNjPwplgzoU8EATRDV2wbzYC3jIy141h+90b2pErCWQpWcaAS/aGxtkkYYUjX2xnw10SP9GqHqqxs4GAy/fsDHw8mlry1agbAVGBh4ztbjC7ByzMNSRR5LOy8zmpJGXxuQuN0jZCYoXbOTcFGb28H66faMgDblib8pL7FN0eEVssLdQPRE3sBo2P//5z0crqC1NI5mcKmzKYWWb6pFSTArQUkQkEMJF9LSeaXPURhmXCN2OKcUFufXXYSwINPDyDRsDL5+2tmwFylZgVA9PPGDmFULk0qWoC1YxWcu1EjnEvJi+xTx+ypPjO6ClNU/hMLyflK8yAi1EFJKlGAjvLTgXMWFEHpV+ptnl4alpJIBqxAifMqJ6Ujd2CVc0TXWQuxl077mkGYSTFOHFirgp8MTUVNie66Jpgtg28PINGwMvn7a2bAXKVmCMwIvZudszC1+KSTz1q7QtbOi8nSbo1LmRNyavCJZEmlhjJD6NlINITHoiLfCWpkxPy5FLcBWPsZCeGGMX6FKtBLMAiUgs8ASNlAZTBSBpPVNuGWnYq58EnQQlRlkoseDh0Rzh0MArewCMVp6BN5pC3m8F6qPA2IHHpMw8XlgqTJfg2JuewBNd0kU8rXDKywnnRuzRSThF6kqWmPqFsfAjdaawAAZRKoBR8PCiLFGq4KHKlJoWwKMCQpS+u4EX9ErpKBdW5O4uJZqgxU/SqCGklwgGXgXHjYFXwYPiKlmBcSoA8NaPLZxwwgmapletWtWd49e//jV72QVU0r3nnHPO/fffv2LFCjIqHjtpAsVPmTKFb+UlMbluvvlmJWMb47KjlMpCcUpAer6VhlzEK0aB+DDFLrLLjoK2X3jhBVDHBt+KwRSBDWqrDTw8ZVEazKoaNJnsbJNS5dIKlVhoKblkKjKSgOxRPYkQQYnHErykOc7eP4ZsBt4YRHISK1ATBcYCPM3U4pwmdyZ9YtjWdMzsH5M7iUkQ1IlZOxKkGMCmfsZcj03FCAOCqCgSu8StFA8kjoJIzy79JIvsgCIsUE9xiBpqmw2ZUjK+2VahIit2hGFyESNyC2yKKVBK3ptkwVr8P8CgzgWqIUrARlQ1FYGUaYtGZZ6Bl2+0GXj5tLVlK1C2AiMDj5mdmbenSxd+yajT8UQSCEsK6Xa4kgJMdw1VPeJpAhkLCVK/aiLVq0heAy/fsDHw8mlry1agbAWYK19xqLkCBl6+YWPg5dPWlq1A2QoYeDWHXaf6Bl6+YWPg5dPWlq1A2QoYeAZe2X2uVuUZeLU6XK6sFRhRAYD3skPNFbCHl2+UG3j5tLVlK1C2AgZezWHXqb6Bl2/YGHj5tLVlK1C2AgaegVd2n6tVeQZerQ6XK2sFRlvSfMmh5grYw8s3yg28fNrashUoWwHmyprP9q7+SwZevmFj4OXT1patQNkKGHgNAKaBl2/YGHj5tLVlK1C2AjFXvuhQWwUMvHzDxsDLp60tW4GyFTDwaou5jytu4OUbNgZePm1t2QqUrYCBZ+CV3edqVZ6BV6vD5cpagdGu0vy7Q80VsIeXb5QbePm0tWUrULYCzJU1n+1d/b8bePmGzaSZM2eir0OtFeAg5usitlwjBQy8BgDTwMs34iYh7vsONVeAg5ivi9hyjRSgJ/zNoeYKGHj5RpyBV3PWDVVfI2TDhg35Ooot10IBA6/msOtU38DLN9Y6wNvhUHMFPELyjZB6WTbwDLx69diSa2vg1Zx1Q9U38EoeNpUtbt9996312WhXHgWmTJmimysq283qW7EO8LY71FwBA6++IzBHzXljQANuR+vZhPbcaMhBzNE3Wm7TwKs564aqb+C1fBi3p/m+Pqs9xzpHSw08Ay9Hv7JNK5BFAQMvi6ytMdoB3jaHmitgD681A7btDTXw2t4DJtZ+A6/mrBuqvoE3sVHg3LVRwMCrzaGqZEU7wNvqUHMFDLxKDi5Xqv8KGHj917RNFg28mrNuqPoGXpvGbKvbauC1+vBPuPEGnoE34U5kA1agLAUMvLKUbmY5HeC9lz/cf//9UciKFSuGKzBN1jPNpk2biNd3Ggox69aty9+mHiWk5Y7alj7W0B5eM0enW9WlgIHnTjERBbID76mhcNRRR8E5GHDjjTdedtllbBDZPePvv//+I2MAI7NnzyY732nKefPmsUtlqbh07+1DgUhtkJdq9JE3YSott1DDHMWFTQNvImPAeWukgIFXo4NVwap2gPduzrB27VrMM/tvHApz587lJ9+KJyxevPi2j8LkyZNjW1kKVXv00UdlAWIRYq8iI5C3u01HHnmkImNDP++77z6yU40xykCthkuclqsqUeExmp1IMgOvgkPLVcqhgIGXQ9X22CwDeEz6MGbVqlUgClQowIZgXoCHBACvG3IkJjsBOMV2yhIy8lNpCD2BhPuIZXCVYgmDUJa8WCAjG0rWHVIodlcSs2SnmWoCdlTPnuidCNt65jXw2jNiW95SA6/lHWCCzc8OPAFAbpw4FE5eYe6W4wWBUtdNTlLqwCmZHEFZEF3CNVRBBeaJPSBWe4NeQm/qGn7hC19QcewCfkI1PGObQCRGIgt7iYyadHt4Bdez76iTQQNvgsPA2euigIFXlyNVzXpmBx40Eu2AARvADEjwk+9gkhAibglmUISUWtKUg6gspBTwSBALnkFEOWrdjBS94FaQKXXj8PBkmWRkl8OnbX0rMXWT08Ze0qgOSjMy8EpY1TTwqjm6XKu+K2Dg9V3SVhksD3hx5iz18ICWFgBFL7YhirwW+VKil5ghHOqSk9SXCuCRRSkLTmHk1bpl6h2qLOJx7FQTwYxA6fL/FEO147ScYsiilBFIjIcq7tJebUfrCon7+JNZYIxh5syZrerfbmzDFDDwGnZAS25OB3jv5AxM9zfccMOTTz45a9Ysylm6dCkk4Bt4RLH33nsvP0lJGnYp2QsvvKAEys7Gfvvtxzfb8CatsvBGDJZ56zcbxCxatCi1L7NKo5oUAgmwL+Cxi9Ll6lG6YgQ8GVcMwCsYwQLp1RwssJ1T2o9tj93D83xR8gBzcf1VwB24v3q2zVoZwIMQAoZm6BR1/AQPwEnA4yfEEpyCavCJSBLAKuL5CVfIAlGURUG8BIekLJAmSqQs0mAnaEpZRGrVVGQV57RBuaRXTShR9gN4qoZwKwsyy3fQvRzmGXhtG7etau/jjz++evVqNTmAxz05d99998MPP9wqKdzYCSpQEvCCAeHqBaiEBJgBe7QAKHIEzOSrwR4i2Za3R3qRSXYwknI03DJ2CZ/y1WCb0lMEuOInNuW6BZlE37BJmthFPHupSbiPyqsqKYjf8ZOCwu+MyL5vGHgTHAbOXmUFeKzEXnvtdcQRR/A/kq7ON4EYAruqXHPXrWoKdID3dtPDI4880t3E559/vjHtNvCqNq5cn/4qcOmll3afpb722mv7W4qtNV6BVgCvMWAbriEGXuMHassbuGbNmgLwDjzwQFY1Wy6Lm7+zCnSAt8Wh5goYeDvb752+dgqcd955KfM4VVG7JrjCA1fAwKs564aqb+ANfCC5ArkV4PqUAJ7vrsmtdlPtG3gGXlP7ttvVNAXgnJjnizObdmjLak8HeG851FwBe3hljReXM0gFli1bRlc/44wzBlkJl11nBQy8mrNuqPoGXp3HoOs+VgW4SoVrVbiAZawZnM4KfFKBDvDedKi5Agaex/XOKsCt3DwAj8v9dVvbHnvs0X3dv2MmosAhhxwibdEZ39S3DO5sF82R3sCrOeuGqm/g5RgbjbQJ54Dc3nvvffDBB5922mnnnnvuvw6Fn//857c69FWBK664Qtqi8/HHH4/mnIPk4lIGbCO7Vi0aZeAZeLXoqK7kRBV46aWX5s2bB+eA3HXXXdfXud3GxqTAZZdddtJJJ02dOhXs+SbCiXboceXvAO8Nh5orYA9vXJ2/RZl4KAnzLI/BG9PE7EQ5FeDfBtjjSWnxgNAWdcRBN9XAqznrhqpv4A16HFW3fFYwOI00Z86cm2666RaHyihw1VVXHXrooXfccUd1u04Ta9YB3maHmitg4DVxbPahTVzQeNBBB1188cWVmeddkY8V4C/Icccdd8kll/ThSNvE2BQw8GrOuqHqG3hj6+3tSsVlgdAOT8KQqbICON+8X6VdXXNwrTXwDLzB9T6XnE0BrolgJRPv4WaHyiuAn/erX/0qW1+w4Y8VmLTvvvtO5F6TauadMWNGNSuWqVZTpkx5cSiM2rWpwKhpnKABCnDvwTnnnFP5qd4V7CjAjXqcz/vDH/7QgI5X8SZMUv1efvllzZjNCEzrvElH71l9Nwn87SWw95lnnnn22Web0dhoBQdx1N5m4I0qUQMSrFy5kgnUMKmRAkuWLJk+fTrLTQ3oflVuwofAq3IVx1G3UYE3Rn9oHEVXPIuBV/ED1JfqcYPz4sWLazTdu6oocOKJJ15//fXbtm3rSx+wkZ4KGHjt6hgGXuOPN28SwFfgySkO9VLg6quvnjZt2lhOTDS+D+droIGXT9sqWjbwqnhU+lon3Duu+qvXXO/aSgGuXsHV40RMX3uEjX2sgIHXrt5g4DX7ePP8MC5DMz9qqsDChQtPOOGEDRs2vP/++83uqINqnYE3KOUHU66BNxjdyyqVhzQyY3JHs0NNFdhrr724no5r7srqMu0qx8Br1/E28Jp9vLn3bsGCBTWd611tFGBF+pe//OXrr7/e7I46qNYZeINSfjDlGniD0b2sUnkHG2+lMTnqqwB/WbhFgVuMvKqZY9AYeDlUra5NA6+6x6YfNeP4chezQ30V4OVNF1xwAddq+v1B/RgQRRsGXg5Vq2vTwKvusZlwzXh4Jm8Zre9c75qjwIUXXnjqqacCvC1btky4R9iAgTf0pBXfeO6h0DwFAB6XPIwDG2eeeWaai7sa+HnRRRcdfvjhbOBwFGwec8wximFX917tInvBbHfFsMPNZyouDWQkuwL2TznllBEa1Z19HAp0Z6Fi0TQqQE0KadIG8pDubgtUrDvXqHXjiQG6G4/XfjWviw68RfbwBn4ISq2APbxS5S69MI7vDaOFM84448dJ+P73v/+Vr3yFiMgH54g8//zzDzzwQCJ33313Xrmgvexi9idGCcion0zuhWLJS4KR60J22S+k/Pa3v51mVDUikJjSqQl1ZnvXXXcdrcXj2Y/lqFWhAjIX+rBdqLASSBakQyKF7pb2rJn+kfsxYzlGj4GXQ9Xq2jTwqnts+lGzsQAvnWSBHxNxxIiDTOViHvM4cz1pCPwkmfYSTy4ihQTtEgPY1uQOzMgbc32UwkbQFlaJKyouqgEYIk3KlUgg3PIzKqZd5KJcFa2fw7Eudqms7mQBPPbS3qhn2lLiVW0BL7VDM1NkDleNEYC3fv36fvQI2/iEAgZeuzqEgdfs471TwOv2zMJRAyRM4iRg4g73jtlZLgs44RtKEZRGvloa5M10O3lpyp6OkVyi1FS3EWGYNJQi7Cm9CBox8bObK5/97GcV2e2bEqmmEVTD8PBiQ+APotMo6lAQQaQsFF1o2gjA8zPGcgxVAy+HqtW1aeBV99j0o2YjAw+WpIuZ4oHmbpFDAa8lXdJkWo+pPBwXrYuSWHM63yk55AIKFUEjGQ9TpGevjKTLkkRq4TScxe6VQK2pqqrpqmZKOHZBNWLYoCx5fvxkm+yTJk3SQm4s51Jn9vJTDem5pJmubYanS2KpV8CbWhpOM3sldU/IpZFtvsigH4NgJBsGXm6Fq2XfwKvW8eh3bXbKw+t2y5iRgY1mfHFCE3H4eTozJ04INloSjIVBsujsmlZEycvPWGMU8MQSYUxwTdcz4+Rf4LD7dGCseaqe4TYJfuG5dnt4mBK0wsNTGqicIl/A05ptuqSpNV72ygMm8B8i8qbAo9WFk4s9TwT2hJ+B1+9h8bE9Ay+ftlW0bOBV8aj0r04TB156gi0ub9H8Lo8trr/QZB0eniAhj020SDEW7FR6uXfiX4G7gbeILwAPs+FcwpvuZUNgI56lwBN3ie8JvABhEEhUVrV1bU4KJ1Up4Kd13aiJnNrudo3q2ymBgde/AVG0ZODl07aKlg28Kh6V/tVpgsCLGVkgYeJOT+B1Ay98u1i3FOQ01+tb5/zCsqgg305pQBEbccVHOHZxdWjqawZilWW4SzSJ114tIaZn43oCjwSF5Up+amE29fBSYgXw4q9A4YSigde/ft03SwZe36SshSEDrxaHadyV5PjyEtExBmbk4VJ+61vfuvTSS9nLBjCLZGBA28QXNvRTNvV95ZVX8n3yySdjQRmBB8+K1MIj9tk+7LDDSEyaMBhmSUwaMkZl2EV6JWCv6gZmVFWVq/SqALuUhoKI4ZtyVRNtUENBEQuK0WIpKcNmVExrsCGFgKfEiqQVVC9qWJA3pAsLw23Ywxt3/x81o4E3qkSNSmDgNepwdjVmjMATJ3oCL+bxmI6ZwYMTmrXFD2ihjXTiFicEHrGHglJT2lbeQCnJKCKwQcbgjYqTEepGRr5FTaUnJXsJ7GJbS5EplsRdXTCiopVLP4kPO1qSTfMGvRQZOsA2tkW4tPk0JAAZykilQsoRsGfg5RukBl4+bato2cCr4lHpX53GCLwRZlvvGrgCBl7/BoTP4fnRYvl6ky0PVAEeN7zHHnv8zKHOCuCATpkypbXPPsw9gOzh5Va4WvZ5bUq1KuTa9E8Bve68zrO96/6z+fPn8wpfA69/w+ITlgy8HsK++eabDz74oHYsGwrDqT9nzhwSd+99ZiiMcMxuueWWyEgX75my8KgF0nfb5OVnI5SSNqRnspGz0/DhHveAPk888URI1LOxY6z/yE+UwDJaDdfGkeufacxU2SyrYYZGrRU4++yz9XogXolX5Z5W07oZeD0OHFM5JNMOaDQC8A444ICewCPjbrvtpr9psoYdEsfkrr0qgrPfKTCidNKQkUDGo48+Ggua+gUbBSVgg0pGnaNJaUPSdkbRZAlmdDeEZNSNb6pXMM7PkIXqpeAZtf7CsOpPi2imtmmjKonxEKRnE6Kq2KH0mo69HNXmco/LL7/8OofaKsALYLmghkHnl57nGCAGXm/gyevSbMukzEb4Yel8DQ80WTPjxxwtvBFEKSZl5WVm51vwYJoWq9hLMhFLPFOhJCC9fCyRIOyLfMG8IB8bakzwLK12+KyqiUrHMkHbtCXSYEGVIZ5tWhc0IpJtLATCg1ipaCPUXw0s1F9Cqf60VBAVEZU49fNCvVRJVSnHIKmRzSOOOOInP/lJbWd7V/y64447jqtm/HqgTIPOwCsKy6QZThVTcABPuBKQYooP4AlyYUtuB98QS9TBVPBMphQfaUSdAvCY9DW5K6UssC0ACKsEDKYuo3gWDVEaVVXZu8EAYwrLgynhMJjyElNqKVXCVJAp2hXA7ln/ECTqT1nYTD1p/lXIzwvgRaHDeXVphTONluqbvfbaa3ERzI36KrDffvsxQADeW2+9Vf3+VrsaGnjFQ6ZFyECC6KLJPQWetoOCQRHmaBGR6Zss4pzYU8hOMsFM8WEh9fAARlAhahIbqhUTfUoj1Up1ThsS9Ycl4dUpewA+1SIIjSDRTCUI9hBPdi0wqtXdwO6uf1orFd19lo7S1ahIHIUSqT8H8lCloTaGW16u3bAcd4VXr179T//0T2DPoY4KXHLJJd/73vd0KuSdd94ZdzdwxuEUMPB6KJOuuaUenigYM3t4V5qUtRfgyatTCOAxp4eHx9wtBGrBUJzYKeAFRbAga4RA2sjAU4NTZ3G4ziGESIE0TZSomsu53CngabFXIa1/LNtStAQE+VSADZLFnwMKlTuYstODXAroNF4dp3vXOU7gccXK+++/7y7ddwUMvN7AYyLWeTJm85h8w20SUbSGprlYM7hssZG6TTEpC0gpGJRFF7ME8HTOT+fAenp4OucX9ZZZahtrkj2XNFMvLaWsmomR7istdS6t4N5RVrqkqfaGVvwctf5yeQv1p0rimTxj1Uciy4UV/8KHo1ZKIBySZoSLOfs+bKpsUKuahkftFOCho3jnK1asYCxs2rSpyn2svnUz8IrHTl6FmCE4hXOmpCJcOmXLC0mBJyCJYQG8OPOXnsOL4kkpYrGh01QCXso8MSk8OeXVz3Q1b1QPD/vhVsb1Lz3XA7VoWdCoALyQRTUZtf7SpAC8tIi4zIdIuY+p2kpJHahwAI+Nke8Dqe8Q3dmac/v5IYccctlll13jUCsFTjvttIULF2o9c8uWLTt73J1+LAoYeEWVUl+BbS2d6WxczOzdTk/4Z5rxoYiuxtTkrssag3MCUsHREV/JrrNiZA9usSEEak4vEKjAP0EinNFARZpMzZEbmrKnQB2VqDsHChetyN9KbQalRq1/T2CnRYeYUrKgfyisI9XNwrH0+2anWbp06bHHHlur2b7tleUBK9OmTXv66aeh3SuvvOL1zEwj1MDrLaxuBkivG9TapryKFHjiWXpNf3gwctrCw0t5KeDJpWMDy7rjjZ+xMqmlvFjApDLy+eKuA+yrVoU2yC1Lr+nQqT4lC4c1ViwFY4LgodXFFCQUTXY5u/KuUuAJxnGZ5cj11zlO1USrl+nVKIrHoPzadJUyfE15dUoZ/yRU/25nNNOwqbhZnDye1rFgwYK2Y6Q+7ecPiu5G8PWZWQeXgddDXqjW84SQHKyejy9J0xfWBns+IaVQKpYDY7Gruxq6NiTN2/Om+JGfe9KdRfSSZfIWrvmM4qKN3VUlTben2LP+hbrxs2dtez7kRTXvrr9O5g3nqmYdP5U1zmPG+DOxZMmS+sz57a3pOeecwwNWRLv169fbvcs3rAy8fNrashUYpAL//d//ffDBB1/tUG0FWMnAHV+3bp2Ah3c+yE7T9LINvKYfYbevxQrcddddMI+7FKo957e3dueddx60e/bZZ0U730iae7AaeLkVrpZ9vy2hWscjf23+8Ic/TJ8+/aKLLmovVara8lNOOYWz0UG7V199NX93aHsJBl67eoBfANuu4z3U2ueffx7mHXPMMYsWLeJmL4eBKzB37typU6fqIdEK3HjnU3cljE0DrwSRK1SEgVehg1FiVTgzhHPPY6tOPPFEY2+AwAN1hx56KMuYusFcYfPmzSX2hVYXZeC16/AbeO063klrt2/fvnbtWq7bxLcgzJ49m8l3gFN/e4rmH8b5558/c+ZMZAd1d999d6COW+7efvvt1vbJ8htu4JWv+SBLNPAGqX4FyuYZ/EyyuBc81IPJl/5A2GeffbjrWdsO/VIAVffcc0+swblTTz315ptv1n3lEXjj3Y4dOyrQKVpUBQOvRQebphp47TrevVrLuaI33ngD7MXM+6ehkM7Fld2mA1e2boWKcWNo3GyQ7uLB0K+99trWrVvdFctXwMArX/NBlmjgDVL9ipW9bds2yMflEtzsXBeK1Ah4qaQ8BwCdcel46Y8vThngODDwBij+AIo28AYguovsnwLuwP3Tso2WDLx2HXXPF+063o1rrTtw4w5pqQ0y8EqVe+CFeb4Y+CFwBSaigDvwRNRzXgOvXX3A80W7jnfjWusO3LhDWmqDDLxS5R54YZ4vBn4IXIGJKOAOPBH1nLeuwDvppJNGuF2GO2C4nZMLogjvJoHnTRD23XffEfLOmjWrwd3C80WDD24bmtb4Dszzo3mRYRmH8s03yyilYmXUFXhr1qyBeVzsy3XVdBECd9QStgwFaDcC8LgDhsA12QQeP0Hg9k8CpqAdlit2jPpZncbPF/0Uy7aqp0CzOzD37c2YMWOcbeTtzUMvcO4E3ty5226jHD0S8+7o4bA3FgsqgLcxf/RC5s5PXmrd9UrqD2sSKXn15ghF5+x1dQUemvC0iJNPPhnm9QV4PM4ufcAdN83klH1gtsc5lgZWXxdsBT6hQIM78OrVq3nv+XPPPUcb+f/NTXtjPfa8GFn04nP00Z1tvtlmA+y9+GJvOykgxchCkCkCfEo/RKaJKWLozcwfhsLP1Oauu36YEmuj8nisjd+5dDUGHv4ZzzKAeTy5YIIeXjftmvochAbPFzvX8Z26ngo0tQMvX76cqYzH37BMFTfXj/UQ4aWBEPjEBy8K/LBBuOWWjr8VQX5VfMAPn/SnPDOsKRImkUAG5ZwJe0SmwOMnWR58sJOXbxkpQFG7AnIp8MIrHWtrJ5SuxsCj3axG8rovMW/cS5r8kyr4dk2lHYo1db6Y0CBw5voo0MgOvHTp0gsuuIDnjemMzE4DDx8uPDy5d8BJ7p3iuwOIAlTsBYrDBWFJa56pO0hGBZUlO/LeKCscSvKmxuV9KgTwqCfJup3LbB2y3sBDFnx/+Xn6c7Sz5/BaRTsDL9s4suGSFGge8Hgr3uLFi3XZgQLX3I1wVV1h19677/67z3zmrs99jm9t8Dlo8uTrdtmFzzFf/eqTn/50tzV2/b9/+AfiyTLry1/uWRwZ2YW1c770JVlTsv/71Kci/fwvflF2iGSDb3Jhk22+IyV22MaUMqpW5O1Zt7G3vTslr6QYuSPWHngF5u0U8PhL1R7fTv2gefNFSROti6mGAk3qwFwxft5559122226nnzc4b177902a9a2uXP5bN9vv9jobM+a9f4XvsB3anzrokU7Jk9mLxmJ72wfeeS7Tz6ZpiGGjCRgg3gskEsJiI+UHSOPPvruCy9ghO+tN9zAd+x9b+lSbasmVEw/yYIRsoy7ycNllHO8YcOG4XprE4BH23ge6x//+Ef8PB6DO8arNFtIOwOvGpO2azF+BZoEvFNOOeXOO+9M75sa3zb8AB58b73vvg6l1q5le/vs2Xy23XhjB0UfBRLs2H9/wEOEqPPeqlVsk1IE2nrbbfwklzLyUWLZ7+RatYqUYVDGSdxBJsmGjOvDtoyzl7yKVEYVN77Gjpxr1NXghgCvwLxRb0t49dVX2+bb2cMb/yzrnJVRoEnA4w4omLdx48YJTv1Cl4gyAvCAGcALnoEoUaqDsbVrP/4GnEPYg1jvbtwo1AWfBNcPc61a1dnL9/77y3Kn9CEupozESKdQAW/jxk65s2enGBYX+xJaBDwNyZUrV+Ln4dKOcB9ea2lnD68y87YrMk4FmgQ8JBDzuAlKz8QYX9i6YgWk2T5vHp8OcjZt2nb//eHhEZmaBU6dZN/4BslITF42tl922Y6jjiqU3nHRhkLH2/tob6cs8n4Utq5bx64Obm+/nbgPDQ7tJVnH+EeBQknJN1kKRqKg8TU/zdU64NGH/ud//oc+BPN6PmmFvtVO384e3jinWGerkgINAx7S/vnPf+Z96PwL1wMxCNOnT9+pCzfSK1PiOpH0MpOwpkta2KWrRbgahW9djaILXtJyidFPEnNpjLYX77qrrlJJbWKBcqmGviNXbKuUuOylcClNHy9dmTJlil5D2PBzeIXmiXmsFRQeLdZy2tnDq9LU7bqMR4HmAS+Yx1UFevxTuCljfCvvhnvuee9rX1v/+9+zsW2XXV5euZKNt44//o2zzvrES2jXrHln771J9vr8+WyQRh/yEkP8xttuS9OThp9bDj2UNNogpRJHMiJfW7KEn2SnXEonhqL5sK2MClSGSG2TmL3E6MP2qzfdNMbGjjFZu4BHa5988snC+jj8a7NvZw9vPPOr81RMgUYCL5jHXVLcWxyXGo5xcocrYAbeQCNBBQhBkQLAUmsBG21AoO6ysFbYhVkKShnWzciX1qwJwsW2gJcCmIxRhxHqOUYFCsm4Lbt1wKPBq1atCuaZdgZexaZuV2esCugUV3cH5rGCTXryrdY2Yd6oJ6LGKpzTdSnQnKs0ex7cp556CubxSFb7dgaeh39NFQB4Bx54IDdo33jjjcCAbwLPed9rr70a9sxbMc/Ay9dRJ3Fr+k6dIHXiCiow6vMFogM1dUUo3wix5SooAO26x921115bhbr1tw4wb9q0aVqj669lW0OBSXQj7tp2qLUCY8fY2FN6eFiB6ijA0mUBePh8sc5ZnXr2pSZcqGng9UXJbiMd4OltcA71VSDOco/aSwy8USVygmoqwFO4UubxwOVq1rMvtYJ5PBy4L6ZsJFXAwKsv5j6u+dgX/Q08j/+aKvDwww8H8Ma+hl/TxrramRToAE9v/XaorwIGXqbhYbOVUiAuOAB+laqYK1MXBQy8+mLu45obeHUZb67nRBRYtmwZXf2MM86YiBHnbbMCHeDp9n6H+ipg4LV5DLen7VylwrUqTbr3rj3HriItNfDqi7mPa27gVWQ4DbwaM2bMqOBtM67STingM5T5xlEHePHQUm/UVAEDL98IqZdlesJmh5or4CvL8g26QQJv9erV8Yzw+++/f1TY8MCUnmmw0zM+fQA5CQrZRy3xscceC7M9E4fB4So2aov6lcDAyzdC6mXZwKs57DrVH/tdRvXqnFWobQd4E38L0bgt7L///tCCcNlll2GEDZni0UErht6lBGkIbN9+++1HHXUUjwrrLmvevHmkwQJp0r1Kr4CF2bNnp3tJrELTEBVgI01PPbvLVeWJp6BxK9CXjAZeFcZSFepg4DUGeH7SSo4BNRjgQSB4Q/jGN77BN2jhG8iBrngXYvCGGNAimAWQ0lcmkgueEYOFlHkFwpFMdKF0pSRG5ZISC8SzoSIEWqWP7ShdRsJgFNSTx31B2shGDLwcY6OONukJPH3YodYKjH0417GLDrbOHeD15d3qO2vktttue/TRRyEZGefy6vd33wUbvN+gYEeRfJOYwIYSkAVQsYuwePHi2CZNWCCNEhCIp8Ru42FNG6rPfffdxwY2jzzySDKqeiqUXdrWLm2rVnxHyp1VY4Lpxz5CfHpgsOMtd+kGXq1Rp8qPfTjn7k7Nsz8w4IkZAby1a9cGkAAJsNGuAvBS9gRvlF6JsRNwAoTYjFAAKnuhFwZ5c5DKCuCpbvpWTciLnagVpRSApye4T5Bb484+9hFi4DVvDKctMvAMvGb38Am2bgDAAzCwQRyCN3wHmeSopW6TCJdyrsAzaIRB+VjBLWUREeXnpTFsy1mkXKUJxzE22BXYw74KZUPb1FMeZ2BygLSjDgbeBIdBY7LTE3hxtkOtFRj7cG5Mvy2tIQMAXvgxOEnhfgGYwnqg2KM0kbKbfHLFQJ3YGcYDeEHBiME/w7cDVxSqvTISG+KZFkuJnDx5suopr07MC+BhNgXtuL20iWRkhIwx7LvvvqX1LRdUvgIGXq1Rp8obePkGTgd47wwo7LffflGyTpWlFeEFj/xUZOyKDe0lyAjxqTXFYDMSKObJJ59MSyTBCy+8cO9QSK2xvWjRIgx2W1AMgQ2e105gOyqDtYFo6RGSb4TUy7KBZ+DVq8eWXNtBAg/PCQIp3HDDDSnw4I2IJaIAJFiCg6WfBLbJBWlIqZ/wiWSRYMOGDeIWMcRjLXYFsUSvtNzYlnFsUm6gNCAq4AX8AnhpZJnkM/BKHjaVLY6ewO2nDrVWwMM53/jqAO/tAQWA96ePwvXXX/8v//Ivqsg999wDnNh45JFHYAkwY5cC7CEHCQhK8Pzzz0dGUmJTRhQvOwrsJcRP8hIgIjHr168nMXuphhIoXgGbcuZIQBZFKntYVvWobVQmspew4RGSb4TUy7KBV2vUqfIezvkG3SCBVwIJWlKER0i+EVIvywaegVevHltybTvA2+JQcwUMvJKHTWWLoyfwBAaHWivg4ZxvfBl4NWfdUPU9QvKNkHpZNvBqjTpV3sM536DrAO8th5or4BGSb4TUy7KBZ+DVq8eWXFsDr+asG6q+gVfysKlscfQEPR7Bob4KeDjnG18d4L3pUHMFPELyjZB6WR448HjA+qikGUuaUY3sVIJRSxw1wU4VN8HEHs75Bp2BV3PWDVXfIyTfCKmX5XzAAwnnnnsuUzkbvIGyMKcvX7781ltvJXKfffZhL+HEE08spCHm8ccfJ5KNFDBEKh4LEQ4//PCFCxd2k4O8y5YtY5eyRCAmzU410r0k7q5PmoD0USUVUSga+1dddRWRiJCbjh7O+QZdB3hvONRcAY+QfCOkXpbpCTxyIUcACQ888IAs825kqMAj+tKCDjvsMGJIRiR7+VYCvm8ZCqCCcOVQ4Cfpw6ByKZB3wYIFvGmrZyuwQC72spGm4WeaXhVQID0VxiaFUjQb2uabn0pDZdIKdBcdNYyCCs3vWdvxRXo45xt0Bl7NWTdUfY+QfCOkXpYzAQ82wIyYvpnrocjXv/518Uz8EMM+//nPB9Jgg6gAmUgvVinccccdpE8NCnXiECnZSBkTRQiHKo6NsKAsEVLgqeaqlfgn0JI9WCizQqA4yjaVlH02oiwBD5sppMcHtuFyeTjnG3Qd4DXgHcEtb4JHSL4RUi/LOYAnMoU3xjZzPVQoOGFQAVqwiw15cumETjxQCeClfhIp5S+KUgGkgJZIKWuCJRspL8EPRviO0A1UrGmvOIeFMKvKyO+knsFOEZ0AyNPKUNXUI+wv7bDm4Zxv0Bl4TWClR0i+EVIvyzmApwkdvIkxbKT+k/aKFmJDxMgH0i5RTV6UHCmtLiqxkENeoUv0ClPslVdHjBYzSZw6WHIfiSRX4Dld5KToYHDEx4YKUqNSR1ZchJ1hX1XNSjsDL+uIm8T7YhgkDQgzZsxoQCvG14QpU6a8OBSy9hUbr74C+YCnuT78MIGKoOVNbevcXuxKXUAtiiqk/CgATwuVgbd0bzAJ1KWwJD4MEi//Ly2CSPlkYmTK0ULp2hvrpeKxDIqOWsjNd/ZO9fH/13wDbZJMa7qsdaCXxJMz400FPV8y914Som/Vuu1R+XwdxZZroUBW4DEXCzaBNAEvzpxBhfDGdJVKOEyFM16Fn8LJcEuaAdG4IqaQPfBGSlG2J1OpTOqepucIVXM1UK0rWIh1ztRxzEQ+Ay/fWPsQeC+//HLdZ/xxA++ZZ5559tln69586s9BzNdRbLkWCjAKePVHpiC/Ch/o17/+dc8i2EU8aVasWFFIgGN0cxL4GQl40/IJJ5xw//33n3POOUSSF/vEsG6hNJglktKVgF1k571dUQ22McI3WYgkZXcFlJddpAyzUQeKIC+5otCoj9Ioe7pR2O6j5gZevrH2IfDyFVCa5XEDz4uBpR0jF5RbgUzAAxLM+AEYtkGOKBUTffqT7cLeAInSx09RihhoSBZgiU0FlUjRUEqsIjGR2ksFUpuwiuxpZQolKmOYorhIQKQaQnEBZmoSkCsAj7yqQ6GIfjHPwMs3TAy8bxp4+bqXLZesQN+BB2M0v3fP5oJHfwPcEuoUBKEoouBZwiT5eUGstDI5qtffxg5nzcDLN2oMPAMvX++y5bIVYK58xaHmChh4+YaNgWfg5etdtly2AgZezWHXqb6Bl2/YGHgGXr7eZctlK2DgGXhl97lalWfgGXi16rCu7IgKADwu1nWotQL28PKNcgPPwMvXu2y5bAUMvFqjTpU38PINGwPPwMvXu2y5bAUMPAOv7D5Xq/IMPAOvVh3WlR1tSfMlh5orYA8v3yg38Ay8fL3LlstWgLmy5rO9q/+SgZdv2Bh4Bl6+3mXLZStg4DUAmAZevmFj4Bl4+XqXLZetQMMeht6AJ9yOowkGXr5hU0XgnXTSSeN4Sw6POR/H2xLG8XakWbNm5TsetmwFJqKAgTcOwFQti4E3kSEwct4qAo9FiSuuuOKQQw7hDSP0xTc+Cm8m4a2PwpYkjAN4Wz8K25Kw/aOw46Pw/vvv//a3v6VKPL6P6uU7HrZsBSaigIFXNXqNoz4G3kSGQP2Apxo//fTTCxcunDZt2jXXXANjoN6ggAfqqMYFF1zwpz/9Kbrvpk2b8h0VW7YC41OAufLvDjVXwMAbX+cfS64qeniqN17W5s2bn3/++SVLlsAbfL7//d//DeaV4+H1RB3PLnr99dep3lj0dRorUKYCBl7NYdepvoGXb8hUF3hqM2uKwt7VV18N9hYtWiTs5QbeXXfd1e3VGXX5OqIt90UBA8/A60tHaqqRqgMvsMeS5t/+9rfrr79+6tSpP/rRj5577jkxr+/n8Iy6pvb1NrQL4DFMHGqtgD28fEO1HsAL7OHb4Wb98pe/xP268MIL//KXv/QReEZdvn5my+UoYODVGnWqvIGXb7DUCXhSgQsmcex4Yh7Y+973vnf22WevWrVK2Bv3VZr/8R//4QXMfJ3MlktTwMAz8ErrbHUsqH7AC5UhHN7eb37zm+nTp59++umPPfbYOIBn1NWx17rOwykwjvtKx3HPq7NkVWDKlCm6Gtz9vO8K1Bh40gLsrV+//u677z7hhBNOOeUUYe+dj8K7vcJ7Q+HOO++0V9f3/mSDVVCA9Y9x3P5ViyztudGQg1iFvtSwOtQeeDoeAG7Dhg0PPPAA2JsxY8Z//ud/Cnk9gWfUNawTuzntUQDgtaexbmnfFWgI8KQLeON+8BUrVsyZMwfssdpZAJ5R1/cOZINWoEwFDLwy1W5eWY0Cng4Py5XCHs9G+e53vwvkwJ5R17y+6xa1UAEDr4UHvY9N3jngrVmzhrNlNw4FHiyZ9cztBI1/5zvf4SnPP/zhD7mSE1N8s/3jjwLbPKJ67733nmAp/c2uZ3VK3pUrV3IPRh+PtE1ZgQYoYOA14CAOsAljAh6T76WXXnrggQfOnDkzZuS//vWv3CFQ8YC399prr6UPX+BUMDE8KbqCNeeqAR5mJuBBa664O+OMM5YtW2byDXCEuOhKKWDgVepw1K4yowBv9erVXAbC5HvHHXds3LixgpAYS5XAm7BXWdQN1wouOuWvBp4fCATeteterrAV6K8CBl5/9WybtWGBx2mw8847D9o99dRTY4GK0+RTgMeqaQ15+fLlbeugbq8VSBUw8NwfJqJAb+Bxrg7U/e53v4sXwnlj4Apw38XcuXN5a8REjrfzWoFaK2Dg1frwDbzyPYCHGwHtWAAc+BTvCnQr8G//9m88Tc3LmwMfOa7AQBQw8AYie2MKLQIP2p177rncsm3YVFYBPO+zzjrLzGvMIHRDxq6AgTd2rZyyW4FPAI9LVE499VTTrrKoi4rxTBnOsPLTfdoKtEoBA69Vh7vvjf0YeFylcvzxx/Oscd7l7VB9Ba655prrrruOK2X63ids0ApUVgEDr7KHphYV+xh4eAwPPfRQ9Sd61zAUOProo7lFshb9zJW0An1RwMDri4ytNfIh8FjMxL0zS+qlAI9PYwmatwO2tvu64W1TwMBr2xHvb3s/BB6PUOHt4dyg7VAvBbjCiGdkw+n+dgtbswLVVMDAq+ZxqUutOsB7/PHHf/CDH9RrondtpcCTTz75/e9//9VXX61Lh3M9rcBEFDDwJqKe83aAx+Mx77rrLiOkpgrwRog//elPW7dudW+2Ao1XwMBr/CHO2sAO8HhIMS4CM6ZDHRW45JJLbr75Zjt5WceJjVdEAQOvIgeiptWYxGV+XPhQx4nedZYCf/jDH3gyDm+B8C0KNR2ErvbICnDHVCQoAM+PX3Dn2SkFJi1duvTyyy83POqrAL7dlClTeLXQ22+/vVPH3omtQC0UAHi/+MUvhL0Ant5Z5jdn1eIIVqeSk6699loWxPij5FBfBfbcc89169bx8qPqdCzXxAr0UQHuEu5+2bIfpN5HhVtiahJXrNx55531netdcxTQdSu8TqElvdbNbJsCDz/8cAF4e+21V7rU2TZB3N7xKTCJt6w9/fTTVcDG7bffrmrQj++///6+VwmzuEEyyy3bfbc/QIMAjxZxGm98ncC5rED1FTjwwANT5tm9q/4hq2ANJ9GH3q1GmDx5sirCy04XL16cVoqXrUcM74R79NFH77vvvjTBqlWrbvtkIH1qJHKxgbX999+/0OjZs2evXbsWy0ceeSTbJOCbKhGplMSrBOK1gX2qWgXxOIicwyNUsIe5SlagLwow1gJ4du/6ImkLjVQIeDBGdGED8MAwvuFT8IYNEghFBAg0HG+gUWRUduCENYGKn2RXfPCMDXaRhqAExKRGYjvyRuKBM0/Aw8Pz+xNaOIZb0mRWaAJ4du9actD73sxKAA+XC3jgTolzYpJCwRWT09ZNGiwQL7cPEPITI4okyCamgJbcRwIxpAwvUOST76hdJA62CYHYIbBLG5gSHQce7OH1fWDYYAUV0KUrdu8qeGjqUqVKAE9M0vohsEldK61AClegiJ9aSxSxtFBJdjawoEjIJKqxK4gVK5AimdAYNFVKLWmyl7z85JvEAbPqe3he0qzLqHM9x6eALl2xezc+9ZwLBaoCPHAC8AAM1IE0sdIYmAk4xS4I100jnZ+TjxgrouBNDpk8MyxoLbR7xVK+o3BYuyVNA89DuvEKcJGdL85s/FHO18CqAA/IASotQopGglY38GJpkY1YUQxHMDw8OX/KLl8QoMYqpTzF9MoXpRTw4K58vihdK646dxgbSjbw9Uwq4CXNfCOkXpZnzJjRfb+aY+qlAO+uqVevq1FtKwE8wBNXTmohUfxLL1oJDy8gFOuZ4mKcUdMyplYmIzFFyMNTDD8LZwdlTWlEMmLS7MG2yl60Yg+vRgMvU1WZ2Tc71FwBPy800+j4cEnznWqE/fbbL60It1GLOkQuWrRo1qxZ9957L9s6CUcgPS/HURYlI4viFRnZteuGG25gl1Jqm8goMcyqCL4pTonDvrapSdiniCqIZw8v3wipl2UDr+aw61Rfw9nPkcgx9DoeXhWmbOqQAg+SwSTAA3VeeOEFgUeLjaJdBFEQCPFQUAUyagNMBrEUL4yxoVyUSJqgl0ConwFI/Yz4wCEZ5QhWQT0DL8fYqKNNA68xwPOCTY4BWCHg9Z0cQCtcwL4br5RBAy/H2KijTXrC6w41V8DDOd/Qm8RNLXQPHrTvUF8F9PBo/yXMN07qYtnAqznsOtU38PINt0lnnHEGd7fUd653zVHguOOOe+ihhwy8fOOkLpYNPAOvLn11IPXsvC1h2bJlxkatFdDbEl566SW/A3Ygo6g6hQI83hLlUGsF7OHlG1CTuBCfV+JtcaizAhohfpZmvnFSF8sGXq1Rp8obePmG2yTcu4svvrjOs33b6/63v/1t6tSpAA8PL19HseVaKGDgGXi16KiDquQkLnY4/PDD2w6NOrf/t7/97dlnnw3wXnnllUF1I5dbEQUA3qsONVfAHl6+0TQJ00ccccRf/vKXtxzqqcD555/Pu3MBnp8xmG+c1MWygVdz2HWqb+DlG24d4HEOjzN59Zzt215rhgfrmc8++yzAe+ONN/J1FFuuhQIGnoFXi446qEp2gLdmzRqeOdt2dNSz/ffff/+cOXP0uvP33ntvUN3I5VZEAYCHo+9QawXs4eUbTR3gEebNm8epoHrO+e2tNX9muQNvxYoVPoGXb4TUy7KBV2vUqfIGXr5B9yHwUPnggw/m+02H+ihw6623Lly4UO6d1zPzDZIaWWau5MUjDrVWwMDLN+I+BF6cyavPbN/2mnLX3fe+972nn35a7p1vOc83SGpk2cCrNepUeQMv34j7GHiUceqpp/L4/7aTpA7txxc//fTTH3jgAbl3rOrm6yK2XCMFKgW85cuXD4efxx9/fPXq1ezdZ599tHHVVVdFYjKee+65+skGKxnD2eGuKkxxM/FOcY4SKQKzysh9WapDRYKBl2/EfQJ4TPVcvcIlf6yPOVRZAVYyb775ZtGOl0LYvcs3QuplmbmS/lCFcOKJJ/LYQtXkjjvuuPLKKwu1AnVr167lm3ioxr+3SMA2MfqpBN15FXPYYYeRmPDYY4+l2fkZ4ZZbbsFIaj9yqYb8rIJiUQcDL9+g+wTwKOavf/3rsccey0JZlaf7ltft0ksvXbJkSdBu+/bt+fqHLddLgYoAD7wVCMdPoQvIsQ2EgA3h85//PD/BGzEwkr3QkXggxPdTTz1FJLuUnu0gKBvKSCRBFrq5pbxpvIogryogI9BRFagC+Qy8fIOuCDxK4oWrxxxzzH333ddyrlSw+Tw87OSTT77++utFO8K7776br3PYcu0UqALwQAj8gFUBD+jFz27wQJqvf/3romOaHiDJw0upiYtGfAo8GZSHx0Y4eXBLpCSSb/ZSAdVK2eVZUoQKktmeruRA+Gfg5Rt3PYBHYVzvzmuDfvSjH9EzGvAG4WY04d///d+5SiXO20E7HoiWr2fYch0VqALwBAmtKGq7myWBtNglR03p4RPxWm8kUnZSsIFJfgK8dOmSGOYrZeFbDqI8RX7GqilpiJE1NsIFrM7CpoGXb+j1Bh7lsVC2dOnSadOmafWsGcyoaSvuvffeo4466oILLtA1mQTOrvPK9XzdwpZrqkB1gBfggTSp9wZvYIxi+I5zdamLBnu0VilQKXEALyWoVjK1Gpl6eAInXp18uxR4cgeVXpa1pBlnHJV3gMHAyzf6hgUeRXIpxPr166+++uopU6acdtppv/rVr+zwlYnMRx55hEHIk8N4loruLg/abdu2LV+fsOX6KlAp4MnBSpEGRQS89LRc/NS1JPwM/0/bBPgEt+LCk/ALw3h62Qt2lD44pwVMZccm24rBU5QvWKDyAGlH0QZevgE4EvBUKp4E2Lv77rsvvPBCJl8OBle1nHTSSTh/bDv0UQHu/UdYgnQ+4YQTuBQzvDrdb8eZxR07duTrELZcawXoNozWigSeewd7CFwW0LNK9HDiIVO6l1yEc845h0i+f/3rXysN28QrJX/BY4N4AjGxF7OUSAg7jKM0Oz910YqM828S8lVENKph4OUbg6MDT64ed3pxp7M8jIceeoj/SnyHz+GNvijAW8u1bpNCTpYRH+fSqMs3EpphuTrAE6KYvletWgWNwJ7oEkHXquhnYW+ASnvFSzAGmWSNGHAFtwJysaGUykiaiE+BFwQVjElPStW2CsHAyzcYxwS8KJ7HEzPt4mdoCpbP0Ze53kZ6KoDIr7/++ttvv+077fKNgSZZrgLwABskg0wpPGAVdOGbSCAU3lWkkZcmtilB7BKTwqsLf5GClCuC8EbpFFQI4iV7yQUpZTzdls9n4DVpOHS3ZeeAl+bH24B/W7dubbZA5beOy4UQ1mfpyle+ASUCPP6DOtRaAXt4+Ubi+IGXr062bAWswPgUMPBqjTpV3sAbX+cfSy4DbywqOY0VqIcCBp6BV4+eOqBaGngDEt7FWoEMCgA8zvs61FoBe3gZRsaHJg28fNrashUoWwEDr9aoU+UNvHzDxsDLp60tW4GyFTDwDLyy+1ytyjPwanW4XFkrMKICAI8njDvUWgF7ePlGuYGXT1tbtgJlK2Dg1Rp1qryBl2/YGHj5tLVlK1C2AgaegVd2n6tVeQZerQ6XK2sFRlvS9EOL6q6APbx8o9zAy6etLVuBshWIubLuk36b62/g5Rs2Bl4+bW3ZCpStgIHXAFIaePmGjYGXT1tbtgJlK8Bc+XeHmitg4OUbNgZePm1t2QqUrYCBV3PYdapv4OUbNgZePm1t2QqUrYCBZ+CV3edqVZ6BV6vD5cpagdGu0vybQ80VsIeXb5QbePm0tWUrULYCzJU1n+1d/b8ZePmGjYGXT1tbtgJlK2DgNQCYBl6+YWPg5dPWlq1A2Qrsu+++TJcOtVZgypQpurmi7N7TgvIMvBYcZDexZQo04F604ZrQqhsNW9Zty2iugVeGyi7DCpSpAK/IaSrz2gM8DmKZfaYlZRl4LTnQbqYVaIICAK8JzXAbBqSAgTcg4V2sFbACO6+AgbfzmjnHxwoYeO4NVsAK1EYBA682h6qSFTXwKnlYXCkrYAV6KWDguV9MRAEDbyLqOa8VsAKlKmDglSp34woz8Bp3SN0gK9BcBRoPvDfffHPBggXNPYADbpmBN+AD4OKtgBUYuwLNBt66detmzJhBG3fs2PH666+PXZYPU3Kv+jPPfLzdM/+DD35wyy0f7tl11w+eeKKzPWdOMS12dtttTBUgb5r9iis+OOCA3hkjJYWS5s03x2S/r4kMvL7KaWNWwArkVKDBwFu9evWxxx773HPPjf/RYiCKj0ACeI4++oOej2shXjADeIRlyzo/g5Rx+Eg2f37nF3xKP0SmicmLhQiFn2lnoDilxNoYadrvvmTg9VtR27MCViCbAk0F3vLly08++eRXXnnlrbfeGifw4BOfNODMwZjw5wChuEU8vGGDvWzjbPENiuSokYwYPqQhAZHaxUfZiUyBx0+yYIEga4QCFLUrIJcCT0wtKxh4ZSntcqyAFZiwAo0E3tKlSy+44ILXXntty1AYD/CgEeQoLBICKj5y4xRIow/piZ80qcMn5dUntSAsKUZ7FcIgMSCWn2zIe8Na+JTkDdaSi13B4wAeRfd0LifcT4YzYOBlk9aGrYAV6LcCzQPepZdeunjx4reTsM8+++zUw6//3z/8w+Ff+xpZ7vrc5yLj4l13ffLTn+andqXhoMmT2TXry1/+v099ir3k4uf8L36xkExp2HvOl7503S678FECckVKclG6Itngm1y/+8xn2OY7UqqsqN4xX/2qSlQN+xhmzpw5co8z8Po9Im3PCliBbAo0CXjvvffeeeedd9ttt73Tp7B10aKtN9wgYzsmT37v3nu7DZOgs+vRR9n1/he+oASkZJvskX77kUcSQzwbRG6bNSv2Ri7it++3H6befeEFbPKNcb7DyHtLl2qbZFjYNnfuh8U9+ihGyNKndn9sRs7xhg0b7OFlG4I2bAWsQFkKNAl4p5xyyp133vluX0MHPO++u+3GG3fsv3/B8Nb77oNe7FI8+OFnpHlv7VoSfLjrxhs7FiZP5kMyIsVINt5btQpWRa4oTqYolPT6sE1iVYa8ilTG7bNnp0b6KMCoq8H28MoaqS7HCliBCSvQJOCtWbMG5m3cuLGPM/7W226DNB0+rV3bDbygkYCU8kkQIhdwwojo9e7GjUJd8IltQU7w6+zle//9BcuOhSEupozEiPjaAd7GjaCRssKI7PRLAQNvwiPMBqyAFaiMAk0CHqKKeZs2bWJ5s18B6nTAM5pN0my7/XbYM1y5HRdtKHS8vaOO0vbWFSve/8Y3IsvWdevYJVNEdtZFV6zQXpLFNj+3z5tHSr7JUjASBU1cAQOvMiPVFbECVmDCCjQMeOjx5z//+dRTT3311Ve3fhSmT58+vus4uAKFS0W4GIQPF4nEZSbd1tilS0hISRYuY+lOQ7wiubRk79131zbXwugqlQjYwRrFcSmKviNXbBOTXvaii1bCQh8vXRn1ZfFe0pzwELQBK2AFylKgecAL5nFbwrahEG7KTr3F963jj39n771fXrlSudjg57Zddnnva19jV5h6ac2aLYceyq6IeW3JEpIR8/r8+eyNeKUh8YZ77tEGpviQLNIQSXZ+rv/97ykRO8RQHB+2lVHhjbPOimqQmL3E6MP2qzfdtFONHTWxL1opa0S6HCtgBbIp0EjgBfN4nNj27dvjUsNRp3USgCgBBop0pwdCgTGgQjKAtPG22wopSUY87EmBB9uISc3CNpKlDCuYgpFhoWBNbIu8ZAzgdddnLA0fIc0IL4u3h5dtaNqwFbAC/VCAU1w8UlmWUuDx5EnOgfWjhErY0Nrm5s2bRz0RVYnq1rMSBl49j5trbQVaowDA23fffWfNmnXjjTcCA765WZtbjPfaay92NUkGMc/Ay3dMDbx82tqyFbAC/VEAwnVfVXHttdf2x3qVrMC8adOmab2uSvVqSF0MvIYcSDfDCjRYAZYuC8A78MADuYq9kU3mak0DL9ORNfAyCWuzVsAK9FMBnsKVMo8HLvfTesVswTzenFCxSjWhOgZeE46i22AFGq/Aww8/HMAb9RnBjVfDDRyfAgbe+HRzLitgBcpWAM6JecCv7LJdXiMUMPAacRjdCCvQAgWWLVsG7c4444wWtNVNzKKAgZdFVhu1Alag7wpwlQrXqjTp3ru+S2SDIytg4LmHWIGWKrBy5crly5dzWxth6tSp3df9O2YiCrAAK23RmXvkW9rJKtZsA69iB8TVsQI5FeCRJUBu3rx5e+yxx6GHHnr00UefNhSuuOKKWx36qsD5558vbdGZ/xNHHHEEIts9zdm7R7dt4I2ukVNYgWYowDkwZl4gN3fu3J///Od9nd5tbBQFoN1ZZ5110EEHcQ7S2BvUgDLwBqW8y7UC5Snw+OOPg7rjjz/+uuuuM5oGq8CCBQvAHk52PCC0vH7Q+pIMvNZ3AQvQdAU4jXT44YcvWbLkFofKKICTDfbs6pU8+Ay8kgV3cVagPAXwIXhAyezZsyszz7siHytw1VVXHXzwwQ888EB5HaL1JRl4re8CFqChCnARP28YuPDCCw2Zyipw00038X7ze++9t6F9sHLNMvAqd0hcISvQFwV4w8C5555b2bneFZMCMI/LOP/4xz/25aDbyMgKGHjuIVaggQpwQeZxxx13s0MdFLjmmmu4of7vf/97AztixZpk4FXsgLg6VmDCCrz00kv7778/16rUYbZ3HTsK/PjHP+bVr0194dGEe3TfDBh4fZPShqxARRTgQpUf/ehHJkm9FOBk3m9+85sdO3ZUpBc1shoGXiMPqxvVXgW40p31Me4rd6iXAosWLYJ5r776anv7bv6WG3j5NXYJVqBEBXiQB+tj9ZrrXVspICePt7+W2F/aVZSB167j7dY2WwFuvNt7771vuOEGI6SOClxwwQVnn332hg0bmt1LB9g6A2+A4rtoK9BnBe6++24e0s+V7g51VOD666+fMmUKr1bw1St9HhgfmTPwMglrs1ZgAArwhEa8hDrO9a6zFDjssMP417J58+YB9J4WFGngteAgu4mtUeCQQw7hqfyGR30V4OE4PPX0lVdeaU2fLbWhBl6pcrswK5BVAd5y97Of/UzvHXWoowK8Qoinwb344ote1cwxUgy8HKraphUYgAJcsbLXXnvVcZZ3nUMB3qIwZ84cgLdly5YB9KGmF2ngNf0Iu32tUYAHrAwceGeeeWZPel199dUXXXSRdnGWcdyECyMFC9hPY7inLf05XK5Ik1Z75MSFgsbdkOEyXnzxxdOmTQN4b7zxRmt6bnkNNfDK09olWYHcCnzzm9/knoS+h1O6wq677so79qIg7vz79re/zc+vfOUrfHMvoH6mgdvhMUMMG3zDJO09//zzv//972uDQF6+d999dyXuNkICii7sxSa5qIYC2dOMvIinEFMwq2orkBILhQQ0h3Kj8n1XODXIQQR4vm4lx2Ax8HKoaptWYDAKZAJeYX6HT91IADlwBXLwLaSJEPwULmEGoFLgJ2nCiGhESr5JViCi4hUENmKoQ8o8ZYkQuCWliuObNFSJjKq/NpQlxbNAXmhgEDEKohqZsCfgrV+/fjB9qNGlGniNPrxuXMsUyA08oasw0RMTftVnP/tZKCJnK5CAM0ekWKVQwJUMyrUKIIVPRkYhigCNYjtFThQayQqVVOlEqgLBTtKziyASB8+EZBlR5YO4bLB3ZJdxIiwU8Agt67xlNNfAK0Nll2EFylEgN/DkJGk2L5APWgASeXhsFxYG+YkXFcALfkRirToOB7xgW6x/Yi1dFOVnuuwqegV1yKUEVD7IFxty7+QIslQbptgO/y8FnqxNBGkj5zXw8g0WAy+ftrZsBcpWIAfw4sSYTtSlDpYWGwEAtBB+0hU/+UBy5rSo2H0uMHWbRJHwwAIqcr/IK5qmJwuVnSwqN3AYMWFfGEtrGFWVQX0HRGObXLFIq+xZaUcRBl6+YWPg5dPWlq1A2QrkAF7qjoRXlEbGOTYwky70pS6gLkKJkF4kEhBSmjjJly5pqjjFkJdqpMYDb/IyUwSyrZRQiozwLMwG4eQLhvHAs4iok3naBt6Fmqc69GvbwMs3bAy8fNrashUoW4GBAC9cQJ1s63YBRamUBz2BN5yHFx6VclFKIXsAD2ilTmcB1fxMfbUwQoXJKKRFZLePCAjhZXp1THoSsV+0s4eXdcwYeFnltXErUKoCAI8HEOcL8vCGsw822MXTqwmFNICEjEKjTvVFgpNPPhmzfPMYSSKvvPLKSy+9lJhvfetbSqNze/xUAn6SnbNxZElLwSxpdBFKdwWIJLG+FcK+bBIwGBUT8CJxbPeM7K/g9vDyjRkDL5+2tmwFylYgH/AAALzhOg5o1D2/CzYpw7TwCL0UORzwSKCMgpw8LQWyQCDARiABRShNUJDKRIkQLkURP9O9Qh3WRGsaQqHCs35qg8qQi2Sib0+2KW/UsL+okzUDL9+wMfDyaWvLVqBsBfIBL8fMbps9FTDw8g0bAy+ftrZsBUpVgMcN6+HRDvVV4Jprrtlzzz19H16mkWPgZRLWZq1A2QrwLM199923vnO9a44C8+fPP+GEEwy8TIPHwMskrM1agbIVsIfXAGSed955Z599NsB7+eWXy+5ALSjPwGvBQXYTW6MAHt5Pf/rT6xxqq8Dpp5++cOFCgPf666+3ptuW11ADrzytXZIVyK0AbzxfvHhxbWd7V/y6E088kTcQ+fVAmUaKgZdJWJu1AgNQYMGCBSyImRv1VeC73/3uAw88APDeeuutAXSgphdp4DX9CLt9bVLg8ccfP/TQQ691qKcCl19++dSpU3XFyttvv92mnltSWw28koR2MVagBAW4boV7oq+44op6Tvhtr/UPfvCDCy+8UFes7Nixo4QO07YiDLy2HXG3t+EK8DgS5s22o6Oe7T/ggANWrFgB8DZt2tTwbjqg5hl4AxLexVqBPAowV+6zzz48Jbmec357a81jz+bMmaP1TJ/AyzM4PjDwMglrs1ZgYAqwpHnaaafxzA6HGikQ7t0rr7zi9cxMg8fAyySszVqBgSmAk7fffvstWbKkRtN9y6t65plnXnDBBXLvNm/ePLCu0/SCDbymH2G3r5UKrFy5kss1efx/y0FSi+brcWLr1q2DduvXr7d7l2/IGnj5tLVlKzBIBe644w7eksNdzA5VVuAnP/kJb6B9+umnffauhNFi4JUgsouwAoNR4JJLLjn22GOrPN23vG4XX3wxtNOVmX6cWAmDxMArQWQXYQUGpsAvfvGLgw8+mDuaW46WCjZ/3rx50O6JJ54Q7Tjz+v777w+so7SjYAOvHcfZrWyxAo888gjP7+Ax/Nyr4FAFBfj/ccwxx5x66qnPPvusaLdhw4bt27e3uJOW1HQDryShXYwVGKACzz33HPd4HXTQQXPnzq3CjN/aOnDHyCmnnDJt2jTeda6rVOTb+UKVckaHgVeOzi7FCgxYAZ46hqvH1YDcsYB7cdFFF7WWOuU3HM7xUO8jjjhiypQpPAonHDvfhFDyqDDwShbcxVmBgSmAG8E9Xpw0wr2YPn36N7/5TU7vEY488kj9dOijAiwj87Ym5OUfxp577gnwbr/99vDqtIzJv5CB9YZWFmzgtfKwu9EtVoBzRa+99hqPJ2bO5U00hF/+8pdxoaAW2SobAFJl61aoGKrefffdyBuXpUQCnqWyZcuWFvfBgTXdwBuY9C7YCgxQAbw95lzI99JLL6UTccVxUiPgpUrq7wU3lb/xxhtbt24d4HFvedEGXss7gJtvBToKMAuzvFb9CwUBXr0OGKoSfE1KRY6agVeRA+FqWAErMLoCtQPe6E1yihIVMPBKFNtFWQErMDEFDLyJ6df23AZe23uA228FaqSAgVejg1XBqhp4FTworpIVsAK9FTDw3DMmooCBNxH1nNcKWIFSFTDwSpW7cYUZeI07pG6QFWiuAgZec49tGS0z8MpQ2WVYASvQFwUMvL7I2FojBl5rD70bbgXqp4CBV79jVqUaG3hVOhquixWwAiMqYOC5g0xEAQNvIuo5rxWwAqUqYOCVKnfjCjPwGndI3SAr0FwFDLzmHtsyWmbglaGyy7ACVqAvChh4fZGxtUYMvNYeejfcCtRPAQOvfsesSjU28Kp0NFwXK2AFRlTAwHMHmYgCBt5E1HNeK2AFSlXAwCtV7sYVZuA17pC6QVaguQoYeM09tmW0zMArQ2WXYQWsQF8UMPD6ImNrjRh4rT30brgVqIcCvDE8KloA3ptvvlmPNriW1VDAwKvGcXAtrIAVGEaBTZs2zZo168Ybb1y5ciXA43v58uVXXHHFIYccwi7LZgXGroCBN3atnNIKWIHBKHDppZeCukK49tprB1Mbl1pbBQy82h46V9wKtEaBNWvWFGh34IEHpkudrVHCDZ2QAgbehORzZitgBcpR4LzzzkuZt3Tp0nLKdSlNUsDAa9LRdFusQGMVePjhhwN4M2fObGw73bCcChh4OdW1bStgBfqnAJwT84Bf/6zaUosUMPBadLDdVCtQawWWLVsG7c4444xat8KVH6ACBt4AxXfRVsAK7IQCXKXCtSpcwLITeZzUCiQKGHjuDlagvQpwHxu3tREOOuig7uv+HTMRBViAlbbt7V7Va7mBV71j4hpZgZwKADnWBlkYZDafMmXKoUNhwYIFv3DonwI333zzMcccI22lM7cS+tRjzn49JtsG3phkciIr0AAF1q1bB+f23nvv448/HsLd6lCWAtddd9255547ffr0vfbaC6r6DsJBjSYDb1DKu1wrUJ4CeHV4GFOnTjXnymJc73JuuOGGU0899Tvf+Q5OdnmH3yV9pICB575gBRquAKeRQB0exmDnepceCuDw4WRzRPzw65LHnoFXsuAuzgqUqgBPJOE00vXXX3+LQ8UUuPDCC4899ljWmUvtEO0uzMBr9/F36xutAMuYJ510UsXmeVfnYwWWLFly8MEHP/PMM43uhhVqnIFXoYPhqliBPirAWaLjjjvOeKm4AldddRXMe+WVV/p46G1qOAUMPPcNK9BABVavXs1KJu+Q4/p4h4orcMkll5xwwglvvfVWAztixZpk4FXsgLg6VmDCCnDV+7777nvNNddUfKJ39UKBOXPmXHbZZTt27JjwwbeBkRQw8Nw/rEDTFOBt4GeeeaZxUi8FeGraE0888f777zetO1apPQZelY6G62IFJqwAt9zh3nG/188daqXAvHnz8PPeeOONCXcBGxhWAQPPncMKNEoBrsw8++yzazXVu7IfKoCTt2LFCi9s5huQBl4+bW3ZCpStAGfveHIY9zWbIXVUgH8q3Jy3efPmsvtNa8oz8FpzqN3QFijw+OOPH3bYYTc51FMBblHgmTgvvvjitm3bWtBbB9BEA28AortIK5BJAa1n1nO2d607CkybNu2hhx7ymbxMA8TAyySszVqBAShwxBFHcHW70VFfBU4++eSrr756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)



## 应用实例

由于子类的静态代码块在父类的构造函数之前执行，所以可以用来进行一些针对子类的初始化操作

```java
class A {
    public A() {
        System.out.println("I am A");
    }
}

class B extends A {
    static {
        System.out.println("I am B");
    }
    
    public B() {
    }
}

B mB = new B();
```

```shell
I am B
I am A
```



# 枚举类

使用enum关键字定义，继承Enum类

每个元素都是该类的一个实例

枚举中可以定义构造方法（必须是私有的）、成员变量、普通方法和抽象方法

枚举元素必须位于枚举体中最开始部分，枚举元素列表后要有分号与其他成员分隔

```java
public enum Weekday {
    MON(1,"mon"),TUS(2,"tus"),WED(3,"wed"),THU(4,"thu"),FRI(5,"fri"),SAT(6,"sat"),SUN(0,"sun");

    private int value;
    private String label;

    private Weekday(int value,String label){
        this.value = value;
        this.label = label;
    }
}
```

# 内存管理

## 内存区域

![Java内存区域详解](http://ww1.sinaimg.cn/mw690/b254dc71gw1eumzdxs38lg20eb0a1aab.gif)

### 程序计数器

程序计数器，可以看做是当前线程所执行的字节码的行号指示器。在虚拟机的概念模型里，字节码解释器工作就是通过改变程序计数器的值来选择下一条需要执行的字节码指令，分支、循环、跳转、异常处理、线程恢复等基础功能都要依赖这个计数器来完成。

多线程中，为了**让线程切换后能恢复到正确的执行位置**，每条线程都需要有一个独立的程序计数器，各条线程之间互不影响、独立存储，因此这块内存是**线程私有**的。

当线程正在执行的是一个Java方法，这个计数器记录的是在正在执行的虚拟机字节码指令的地址；当执行的是Native方法，这个计数器值为空。

此内存区域是**唯一一个没有规定任何OutOfMemoryError情况的区域** 

### Java虚拟机栈

Java虚拟机栈也是线程私有的 ，它的生命周期与线程相同。虚拟机栈描述的是Java方法执行的内存模型：每个方法在执行的同时都会创建一个栈帧用于存储**局部变量表、操作数栈、动态链表、方法出口信息等**。每一个方法从调用直至执行完成的过程，就对应着一个栈帧在虚拟机栈中入栈到出栈的过程。

局部变量表中存放了编译器可知的各种基本数据类型(boolean、byte、char、short、int、float、long、double)、对象引用和returnAddress类型(指向了一条字节码指令的地址)。

如果扩展时无法申请到足够的内存，就会抛出OutOfMemoryError异常。

### 本地方法栈

本地方法栈与虚拟机的作用相似，不同之处在于虚拟机栈为虚拟机执行的Java方法服务，而本地方法栈则**为虚拟机使用到的Native方法服务**。有的虚拟机直接把本地方法栈和虚拟机栈合二为一。

会抛出StackOverflowError和OutOfMemoryError异常。

### Java堆

Java堆是所有线程共享的一块内存区域，在虚拟机启动时创建，此内存区域的**唯一目的就是存放对象实例** 。

Java堆是垃圾收集器管理的主要区域。由于现在收集器基本采用分代回收算法，所以Java堆还可细分为：新生代和老年代。从内存分配的角度来看，线程共享的Java堆中可能划分出多个线程私有的分配缓冲区(TLAB)。

Java堆可以处于物理上不连续的内存空间，只要**逻辑上连续即可**。在实现上，既可以实现固定大小的，也可以是扩展的。

如果堆中没有内存完成实例分配，并且堆也无法完成扩展时，将会抛出OutOfMemoryError异常。

### 方法区

方法区是各个线程共享的内存区域，它用于存储已被虚拟机加载的**类信息、常量、静态变量、即时编译器编译后的代码等数据** 。

相对而言，垃圾收集行为在这个区域比较少出现，但并非数据进了方法区就永久的存在了，这个区域的内存回收目标主要是针对常量池的回收和对类型的卸载，

当方法区无法满足内存分配需要时，将抛出OutOfMemoryError异常。

### 运行时常量池

方法区的一部分，它用于存放**编译期生成的各种字面量和符号引用**。

[Java内存管理原理及内存区域详解](http://www.importnew.com/16433.html)

## GC工作原理

JVM的GC采用根搜索算法，设立若干种根对象，当任何一个根对象到某一个对象均不可达时，则认为这个对象是可以被回收的

根一般有4种：

1. 虚拟机栈（栈帧中的本地变量表）中引用的对象
2. 本地方法栈中JNI（一般说的Native方法）引用的对象
2. 方法区中的静态成员
4. 方法区中的常量引用的对象（全局变量）

### 内存回收准则

一个变量不可达，即没有任何可达变量指向它。当一个对象不可达时会调用finalize()方法，但是仅调用一次

### 垃圾收集算法

#### 标记-清除算法

标记-清除算法将垃圾回收分为两个阶段：标记阶段和清除阶段。一种可行的实现是，在标记阶段，首先通过根节点，标记所有从根节点开始的可达对象。因此，未被标记的对象就是未被引用的垃圾对象；然后，在清除阶段，清除所有未被标记的对象

当程序运行期间，若可以使用的内存被耗尽的时候，GC线程就会被触发并将程序暂停，随后将依旧存活的对象标记一遍，最终再将堆中所有没被标记的对象全部清除掉，接下来便让程序恢复运行

效率比较低，容易出现内存碎片，一般很少用到

#### 复制算法（新生代GC）

将原有的内存空间分为两块，每次只使用其中一块，在垃圾回收时，将正在使用的内存中的存活对象复制到未使用的内存块中，之后，清除正在使用的内存块中的所有对象，交换两个内存的角色，完成垃圾回收

浪费内存多，不适用于大对象和存活时间长的对象，一般用于新生代对象的GC

#### 标记-整理算法（老年代GC）

在标记-清除算法的基础上做了一些优化。和标记-清除算法一样，标记-压缩算法也首先需要从根节点开始，对所有可达对象做一次标记；但之后，它并不简单的清理未标记的对象，而是**将所有的存活对象压缩到内存的一端；**之后，清理边界外所有的空间

克服了内存碎片，但缺点仍是效率不高，一般用于老年代对象的GC

#### 分代收集算法（新生代的GC+老年代的GC）

根据对象的存活周期的不同将内存划分为几块儿。一般是把Java堆分为新生代和老年代：短命对象归为新生代，长命对象归为老年代。

* 少量对象存活，适合复制算法：在新生代中，每次GC时都发现有大批对象死去，只有少量存活，那就选用复制算法，只需要付出少量存活对象的复制成本就可以完成GC。
* 大量对象存活，适合用标记-清理/标记-整理：在老年代中，因为对象存活率高、没有额外空间对他进行分配担保，就必须使用“标记-清理”/“标记-整理”算法进行GC。

> 老年代的对象中，有一小部分是因为在新生代回收时，老年代做担保，进来的对象；绝大部分对象是因为很多次GC都没有被回收掉而进入老年代。

### System.gc()

`System.gc()`的作用只是提醒虚拟机：程序员希望进行一次垃圾回收。但是它不能保证垃圾回收一定会进行，而且具体什么时候进行是取决于具体的虚拟机的，不同的虚拟机有不同的对策

#### 如何确保gc()执行后内存被回收

```java
/**
     * Indicates to the VM that it would be a good time to run the
     * garbage collector. Note that this is a hint only. There is no guarantee
     * that the garbage collector will actually be run.
     */
public static void gc() {
    boolean shouldRunGC;
    synchronized(lock) {
        shouldRunGC = justRanFinalization;
        if (shouldRunGC) {
            justRanFinalization = false;
        } else {
            runGC = true;
        }
    }
    if (shouldRunGC) {
        Runtime.getRuntime().gc();
    }
}
```

也就是`justRanFinalization=true`的时候才会执行 

```java
/**
* Provides a hint to the VM that it would be useful to attempt
* to perform any outstanding object finalization.
*/
public static void runFinalization() {
    boolean shouldRunGC;
    synchronized(lock) {
        shouldRunGC = runGC;
        runGC = false;
    }
    if (shouldRunGC) {
        Runtime.getRuntime().gc();
    }
    Runtime.getRuntime().runFinalization();
    synchronized(lock) {
        justRanFinalization = true;
    }
}
```

当调用runFinalization()的时候`justRanFinalization`变为`true` 

解决方案在`ZygoteInit.java`中

```java
static void gcAndFinalize() {
    final VMRuntime runtime = VMRuntime.getRuntime();

    /* runFinalizationSync() lets finalizers be called in Zygote,
    * which doesn't have a HeapWorker thread.
    */
    System.gc();
    runtime.runFinalizationSync();
    System.gc();
}
```

### 垃圾收集器

#### Serial收集器

串行收集器是最古老，最稳定以及效率高的收集器，可能会产生较长的停顿，只使用一个线程去回收。新生代、老年代使用串行回收；新生代复制算法、老年代标记-压缩；垃圾收集的过程中会Stop The World（服务暂停）

#### ParNew收集器

ParNew收集器其实就是Serial收集器的多线程版本。新生代并行，老年代串行；新生代复制算法、老年代标记-压缩

#### CMS收集器

CMS（Concurrent Mark Sweep）收集器是一种以获取最短回收停顿时间为目标的收集器。目前很大一部分的Java应用都集中在互联网站或B/S系统的服务端上，这类应用尤其重视服务的响应速度，希望系统停顿时间最短，以给用户带来较好的体验

整个过程分为4个步骤，包括： 

初始标记（CMS initial mark）-> 并发标记（CMS concurrent mark）

->重新标记（CMS remark）-> 并发清除（CMS concurrent sweep）

其中**初始标记、重新标记这两个步骤仍然需要“Stop The World”**。初始标记仅仅只是标记一下GC Roots能直接关联到的对象，速度很快，并发标记阶段就是进行GC Roots Tracing的过程，而重新标记阶段则是为了修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录，这个阶段的停顿时间一般会比初始标记阶段稍长一些，但远比并发标记的时间短。 
​      由于整个过程中**耗时最长的并发标记和并发清除过程**中，收集器线程都可以与用户线程一起工作，所以总体上来说，CMS收集器的内存回收过程是与用户线程一起并发地执行。老年代收集器（新生代使用ParNew）

优点：并发收集、低停顿 

缺点：产生大量空间碎片、并发阶段会降低吞吐量

# 内部类

## 成员内部类

成员内部类是最普通的内部类，它的定义为位于另一个类的内部

```java
class Outter {   
    class Inner {
    }
}
```

成员内部类，就是作为外部类的成员，可以**直接使用外部类的所有成员和方法，即使是private的**。

在外部类中如果要访问成员内部类的成员，必须先**创建一个成员内部类的对象，再通过指向这个对象的引用来访问**

> 在编译时，为了使内部类可以使用外部类的成员，生成了一个附加的实例域this$0指向外部类，同时在外部类和内部类生成了一些access\$xxx方法为private/protected成员提供合适的可访问性，这样就绕开了原本的成员的可访问性不足的问题

内部类可以拥有private访问权限、protected访问权限、public访问权限及包访问权限。

比如上面的例子，如果成员内部类Inner用private修饰，则只能在外部类的内部访问，如果用public修饰，则任何地方都能访问；如果用protected修饰，则只能在同一个包下或者继承外部类的情况下访问；如果是默认访问权限，则只能在同一个包下访问。

这一点和外部类有一点不一样，外部类只能被public和包访问两种权限修饰

由于成员内部类看起来像是外部类的一个成员，所以可以像类的成员一样拥有多种权限修饰。要注意的是，成员内部类不能含有static的变量和方法。**因为成员内部类需要先创建了外部类，才能创建它自己的**

## 局部内部类

局部内部类是定义在一个方法或者一个作用域里面的类，它和成员内部类的区别在于局部内部类的访问仅限于方法内或者该作用域内

```java
class Outter {
    public void Print(final int x) {    //这里局部变量x必须设置为final类型！
        class Inner {
            public void inPrint() {
                System.out.println(x);
            }
        }
        new Inner().inPrint();
    }
}
```

将内部类移到了外部类的方法中，然后在外部类的方法中再生成一个内部类对象去调用内部类方法。如果此时我们需要往外部类的方法中传入参数，那么**外部类的方法形参必须使用final定义**

在方法中定义的内部类只能访问方法中final类型的局部变量，这是因为在方法中定义的局部变量相当于一个常量，它的生命周期超出方法运行的生命周期，由于局部变量被设置为final，所以不能再内部类中改变局部变量的值

## 静态嵌套类

又叫静态局部类、嵌套内部类，就是修饰为static的内部类。声明为static的内部类，不需要内部类对象和外部类对象之间的联系，就是说我们可以直接引用outer.inner，即不需要创建外部类，也不需要创建内部类

```java
class Outter {   
    static class Inner {
    }
}
```

可以看到，如果用static 将内部类静态化，那么内部类就**只能访问外部类的静态成员变量**，具有局限性。

其次，因为内部类被静态化，因此Outter.Inner可以当做一个整体看，可以直接new 出内部类的对象（**通过类名访问static，生不生成外部类对象都没关系**）

## 匿名内部类

匿名内部类应该是平时我们编写代码时用得最多的，在编写事件监听的代码时使用匿名内部类不但方便，而且使代码更加容易维护

```java
button.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }
});
```

匿名内部类**不能有访问修饰符和static修饰符**的

**匿名内部类是唯一一种没有构造器的类**。正因为其没有构造器，所以匿名内部类的使用范围非常有限，大部分匿名内部类用于接口回调。匿名内部类在编译的时候由系统自动起名为Outter$1.class。一般来说，匿名内部类用于继承其他类或是实现接口，并不需要增加额外的方法，只是对继承方法的实现或是重写

[Java内部类的一些总结](http://www.cnblogs.com/hasse/p/5020519.html)

# Object类

## protected Object clone()

Object将clone()作为一个本地方法来实现，这意味着它的代码存放在本地的库中。当代码执行的时候，将会检查调用对象的类(或者父类)是否实现了java.lang.Cloneable接口(Object类不实现Cloneable)。如果没有实现这个接口，clone()将会抛出一个检查异常()——java.lang.CloneNotSupportedException,如果实现了这个接口，clone()会创建一个新的对象，并将原来对象的内容复制到新对象，最后返回这个新对象的引用

**浅克隆**(也叫做浅拷贝)仅仅复制了这个对象本身的成员变量，该对象如果引用了其他对象的话，也不对其复制。新的对象中的数据包含在了这个对象本身中，不涉及对别的对象的引用。

如果一个对象中的所有成员变量都是原始类型，并且其引用了的对象都是不可改变的(大多情况下都是)时，使用浅克隆效果很好！但是，如果其引用了可变的对象，那么这些变化将会影响到该对象和它克隆出的所有对象

**深克隆**(也叫做深复制)会复制这个对象和它所引用的对象的成员变量，如果该对象引用了其他对象，深克隆也会对其复制

## boolean equals(Object obj)

equals()函数可以用来检查一个对象与调用这个equals()的这个对象是否相等

调用它的对象和传入的对象的引用是否相等。也就是说，默认的equals()进行的是引用比较。如果两个引用是相同的，equals()函数返回true；否则，返回false

覆盖equals()函数的时候需要遵守的规则在Oracle官方的文档中都有申明：

* 自反性：对于任意非空的引用值x，x.equals(x)返回值为真。
* 对称性：对于任意非空的引用值x和y，x.equals(y)必须和y.equals(x)返回相同的结果。
* 传递性：对于任意的非空引用值x,y和z,如果x.equals(y)返回真，y.equals(z)返回真，那么x.equals(z)也必须返回真。
* 一致性：对于任意非空的引用值x和y，无论调用x.equals(y)多少次，都要返回相同的结果。在比较的过程中，对象中的数据不能被修改。
* 对于任意的非空引用值x，x.equals(null)必须返回假。

正确覆盖equals的方式

```java
class A {
    private String value;
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        // Don't use if (other isinstanceof A)
        // if other is an instance of one child class of A, this will return true

        A o = (A) other;
        return this.value.equals(o.value);
    }
}
```

## protected void finalize()

Runtime 类里有一个 runFinalizersOnExit 方法，可以让程序在退出时执行所有对象的未被自动调用 finalize 方法，即使该对象仍被引用。但是从官方文档可以看出，该方法已经废弃，不建议使用

1. 对象的 `finalize` 方法不一定会被调用，即使是进程退出前。
2. 发生 GC 时一个对象的内存是否释放取决于是否存在该对象的引用，如果该对象包含对象成员，那对象成员也遵循本条。
3. 对象里包含的对象成员按声明顺序进行释放。

## Class< > getClass()

通过getClass()方法可以得到一个和这个类有关的java.lang.Class对象。返回的Class对象是一个被static synchronized方法封装的代表这个类的对象；例如，static sychronized void foo(){}。这也是指向反射API。因为调用gerClass()的对象的类是在内存中的，保证了类型安全

## int hashCode()

hashCode()方法返回给调用者此对象的哈希码（其值由一个hash函数计算得来）。这个方法通常用在基于hash的集合类中，像java.util.HashMap,java.until.HashSet和java.util.Hashtable

在覆盖equals()的时候同时覆盖hashCode()可以保证对象的功能兼容于hash集合。这是一个好习惯，即使这些对象不会被存储在hash集合中

```java
// java.lang.String#hashcode
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;

        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];
        }
        hash = h;
    }
    return h;
}
```

## String toString()

当 toString() 没有被覆盖的时候，返回的字符串格式是 类名@哈希值，哈希值是十六进制的。举例说，假设有一个 Employee 类，toString() 方法返回的结果可能是 Empoyee@1c7b0f4d

## void wait()，void notify()，void notifyAll()

wait()，notify() 和 notifyAll() 可以让线程协调完成一项任务。例如，一个线程生产，另一个线程消费。生产线程不能在前一产品被消费之前运行，而应该等待前一个被生产出来的产品被消费之后才被唤醒，进行生产。同理，消费线程也不能在生产线程之前运行，即不能消费不存在的产品。所以，应该等待生产线程执行一个之后才执行。利用这些方法，就可以实现这些线程之间的协调。从本质上说，一个线程等待某种状态（例如一个产品被生产），另一个线程正在执行，知道产生了某种状态（例如生产了一个产品）

```java
public synchronized void fun() {
    while (condition) { // 不满足运行条件，需要等待
        wait();
    }
    notifyAll(); // 通知其他所有调用wait()的线程，解除阻塞状态
}
```

### wait和sleep的区别

1. 继承不同

   **sleep是Thread类的静态方法**，sleep的作用是让线程休眠制定的时间，在时间到达时恢复，也就是说sleep将在接到时间到达事件事恢复线程执行

   **wait是Object的方法**，也就是说可以对任意一个对象调用wait方法，调用wait()方法后会将调用者的线程挂起，直到其他线程调用同一个对象的notify()方法才会重新激活调用者

2. 同步锁释放不同

   **sleep不释放同步锁,wait释放同步锁.**

   Thread.sleep不会导致锁行为的改变，如果当前线程是拥有锁的，那么Thread.sleep不会让线程释放锁。

   而当调用wait()方法的时候，线程会放弃对象锁，进入等待此对象的等待锁定池，只有针对此对象调用notify()方法后本线程才进入对象锁定池准备

3. 使用方式不同

   sleep()方法可以在任何地方使用；wait()方法则只能在同步方法或同步块中使用

[Java：Object类详解](http://blog.csdn.net/jack_owen/article/details/39936483)

# 强引用，软引用和弱引用

强引用：Java 的默认引用实现，它会尽可能长时间的存活于 JVM 内，当没有任何对象指向它时 GC 执行后将会被回收；

弱引用：当所引用的对象在 JVM 内不再有强引用时，GC 后弱引用将会被自动回收；

软引用：与弱引用的特性基本一致，最大的区别在于软引用会尽可能长的保留直到 JVM 内存不足时才会被回收(虚拟机保证)，这一特性使得 SoftReference 非常适合缓存应用

# String

## StringBuffer和StringBuilder

1. String用于存放字符的数组被声明为final的，因此只能赋值一次，不可再更改。 
2. 要是需要多次更改，需要用到StringBulider或者StringBuffer，两者不同点在于StringBuffer是线程安全的。 
3. StringBulider转为String： `String m = sb.toString()` 
   String转StringBuilder： `StringBuilder sb = new StringBuilder(m)` 

## 不变性

### 定义

`new String("xxx")`会在堆中创建对象，所有**在编译期间确定的字符串都会在常量池**中，具有不变性

不变性是指引用的对象实例的是不可以改变的，但是可以改变引用地址，所以通过改变引用地址就可以改变值了

`intern()`会根据字符串内容去常量池中寻找并返回相同内容的字符串，如果没有则先创建

### 作用

1. 字符串不变时，字符串池才有可能实现，运行时能节约很多堆空间
2. 字符串不变，就不用考虑多线程同步问题，是线程安全的
3. 类加载器要用到字符串，字符串不变性提供了安全性，保证正确的类被加载
4. 字符串不变hashcode就能被缓存，作为HashMap的键要比其他对象速度快

# Socket通信

## 通信模型

![img](http://img.blog.csdn.net/20130621185516921?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbWFkMTk4OQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

## 代码示例

TCP

```java
Socket socket;  
try {// 创建一个Socket对象，并指定服务端的IP及端口号  
    socket = new Socket("192.168.1.32", 1989);  
    // 创建一个InputStream用户读取要发送的文件。  
    InputStream inputStream = new FileInputStream("e://a.txt");  
    // 获取Socket的OutputStream对象用于发送数据。  
    OutputStream outputStream = socket.getOutputStream();  
    // 创建一个byte类型的buffer字节数组，用于存放读取的本地文件  
    byte buffer[] = new byte[4 * 1024];  
    int temp = 0;  
    // 循环读取文件  
    while ((temp = inputStream.read(buffer)) != -1) {  
        // 把数据写入到OuputStream对象中  
        outputStream.write(buffer, 0, temp);  
    }  
    // 发送读取的数据到服务端  
    outputStream.flush();  

    /** 或创建一个报文，使用BufferedWriter写入,看你的需求 **/  
    //          String socketData = "[2143213;21343fjks;213]";  
    //          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(  
    //                  socket.getOutputStream()));  
    //          writer.write(socketData.replace("\n", " ") + "\n");  
    //          writer.flush();  
    /************************************************/  
} catch (UnknownHostException e) {  
    e.printStackTrace();  
} catch (IOException e) {  
    e.printStackTrace();  
}
```

UDP

```java
DatagramSocket socket;  
try {  
    //创建DatagramSocket对象并指定一个端口号，注意，如果客户端需要接收服务器的返回数据,  
    //还需要使用这个端口号来receive，所以一定要记住  
    socket = new DatagramSocket(1985);  
    //使用InetAddress(Inet4Address).getByName把IP地址转换为网络地址    
    InetAddress serverAddress = InetAddress.getByName("192.168.1.32");  
    String str = "[2143213;21343fjks;213]";//设置要发送的报文    
    byte data[] = str.getBytes();//把字符串str字符串转换为字节数组    
    //创建一个DatagramPacket对象，用于发送数据。    
    //参数一：要发送的数据  参数二：数据的长度  参数三：服务端的网络地址  参数四：服务器端端口号   
    DatagramPacket packet = new DatagramPacket(data, data.length ,serverAddress ,10025);    
    socket.send(packet);//把数据发送到服务端。    
} catch (SocketException e) {  
    e.printStackTrace();  
} catch (UnknownHostException e) {  
    e.printStackTrace();  
} catch (IOException e) {  
    e.printStackTrace();  
}    
```

# 修饰符

## final

final可以修饰：属性，方法，类，局部变量（方法中的变量）

* final修饰的属性的初始化可以在编译期，也可以在运行期，初始化后不能被改变。
* final修饰的属性跟具体对象有关，在运行期初始化的final属性，不同对象可以有不同的值。
* final修饰的属性表明是一个常数（创建后不能被修改）。
* final修饰的方法表示该方法在子类中不能被重写，final修饰的类表示该类不能被继承。

对于基本类型数据，final会将值变为一个常数（创建后不能被修改）；但是对于对象句柄（亦可称作引用或者指针），final会将句柄变为一个常数（进行声明时，必须将句柄初始化到一个具体的对象。而且不能再将句柄指向另一个对象。但是，对象的本身是可以修改的。这一限制也适用于数组，数组也属于对象，数组本身也是可以修改的。方法参数中的final句柄，意味着在该方法内部，我们不能改变参数句柄指向的实际东西，也就是说在方法内部不能给形参句柄再另外赋值）。

## static

static可以修饰：属性，方法，代码段，内部类（静态内部类或嵌套内部类）

* static修饰的属性的初始化在编译期（类加载的时候），初始化后能改变。
* static修饰的属性所有对象都只有一个值。
* static修饰的属性强调它们只有一个。
* static修饰的属性、方法、代码段跟该类的具体对象无关，不创建对象也能调用static修饰的属性、方法等
* static和this，super势不两立，static跟具体对象无关，而this、super正好跟具体对象有关。
* static不可以修饰局部变量。

[java中static、final、static final的区别](http://blog.csdn.net/qq1623267754/article/details/36190715)

## 权限修饰符

### 定义

* public：可以被所有其他类所访问

  具有最大的访问权限，可以访问任何一个在classpath下的类、接口、异常等。它往往用于对外的情况，也就是对象或类对外的一种接口的形式

* protected：自身、子类及同一个包中类可以访问

  主要的作用就是用来保护子类的。它的含义在于子类可以用它修饰的成员，其他的不可以，它相当于传递给子类的一种继承的东西

* default：同一包中的类可以访问，声明时没有加修饰符，认为是friendly

  有时候也称为friendly，它是针对本包访问而设计的，任何处于本包下的类、接口、异常等，都可以相互访问，即使是父类没有用protected修饰的成员也可以

* private：只能被自己访问和修改

  访问权限仅限于类的内部，是一种封装的体现，例如，大多数成员变量都是修饰符为private的，它们不希望被其他任何外部的类访问

|           | 类内部  | 本包    | 子类    | 外部包   |
| --------- | ---- | ----- | ----- | ----- |
| public    | True | True  | True  | True  |
| protected | True | True  | True  | False |
| default   | True | True  | False | False |
| private   | True | False | False | False |

> 注意：java的访问控制是停留在编译层的，也就是它不会在.class文件中留下任何的痕迹，只在编译的时候进行访问控制的检查。其实，通过反射的手段，是可以访问任何包下任何类中的成员，访问类的私有成员也是可能的。

### 通过反射调用访问private变量和方法

例如

```java
public class Exam{  
    private String field1="私有属性";  
    public String field2="公有属性";  
    public void fun1(){  
        System.out.println("fun1:这是一个public访问权限方法");  
    }  

    private void fun2(){  
        System.out.println("fun2:这是一个private访问权限方法");  
    }  

    private void fun3(String arg){  
        System.out.println("fun3:这是一个private访问权限且带参数的方法，参数为："+arg);  
    }  
}  
```

在将Exam.java编译为class文件后，通过反射调用访问private变量和方法

```java
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test02 {
    public static void main(String args[]){
        Exam e = new Exam();
        try {
            Field field1 = e.getClass().getDeclaredField("field1");
            Field field2 = e.getClass().getDeclaredField("field2");
            field1.setAccessible(true);
            System.out.println("field1: "+field1.get(e));
            field1.set(e,"重新设置一个field1值");
            System.out.println("field1: "+field1.get(e));
            System.out.println("field2: "+field2.get(e));
            field2.set(e,"重新设置一个field2值");
            System.out.println("field2: "+field2.get(e));
        } catch (NoSuchFieldException e1) {
            e1.printStackTrace();
        }catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }

        try {

            Method method1 = e.getClass().getDeclaredMethod("fun1");
            method1.invoke(e);

            Method method2 = e.getClass().getDeclaredMethod("fun2");
            method2.setAccessible(true);
            method2.invoke(e);

            Method method3 = e.getClass().getDeclaredMethod("fun3",String.class);
            method3.setAccessible(true);
            method3.invoke(e,"fun3的参数");
        } catch (NoSuchMethodException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
```

运行结果为



![](http://img.blog.csdn.net/20141001175741868?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY29kZWZ1bmphdmE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)



[java利用反射访问类的私有(private)属性及方法](http://blog.csdn.net/codefunjava/article/details/39718843)

### 如何防止被反射调用

通过调用堆栈判断

```java
class Dummy {
    private void safeMethod() {
        StackTraceElement[] st = new Exception().getStackTrace();
        // If a method was invoked by reflection, the stack trace would be similar
        // to something like this:
        /*
        java.lang.Exception
            at package1.b.Dummy.safeMethod(SomeClass.java:38)
            at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
            at sun.reflect.16qcsLhcYm4NdG4fVkW2mwfDB8kwpPsYCq.invoke(16qcsLhcYm4NdG4fVkW2mwfDB8kwpPsYCq.java:43)
        ->    at java.lang.reflect.Method.invoke(Method.java:601)
            at package1.b.Test.main(SomeClass.java:65)
        */
        //5th line marked by "->" is interesting one so I will try to use that info

        if (st.length > 5 &&
            st[4].getClassName().equals("java.lang.reflect.Method"))
            throw new RuntimeException("safeMethod() is accessible only by Dummy object");

        // Now normal code of method
        System.out.println("code of safe method");
    }

    // I will check if it is possible to normally use that method inside this class
    public void trySafeMethod(){
        safeMethod();
    }

    Dummy() {
        safeMethod();
    }
}

class Dummy1 extends Dummy {}

class Test {
    public static void main(String[] args) throws Exception {
        Dummy1 d1 = new Dummy1(); // safeMethod can be invoked inside a superclass constructor
        d1.trySafeMethod(); // safeMethod can be invoked inside other Dummy class methods
        System.out.println("-------------------");

        // Let's check if it is possible to invoke it via reflection
        Method m2 = Dummy.class.getDeclaredMethod("safeMethod");
        // m.invoke(d);//exception java.lang.IllegalAccessException
        m2.setAccessible(true);
        m2.invoke(d1);
    }
}
```

```shell
code of safe method
code of safe method
-------------------
Exception in thread "main" java.lang.reflect.InvocationTargetException
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    at sun.reflect.16qcsLhcYm4NdG4fVkW2mwfDB8kwpPsYCq.invoke(16qcsLhcYm4NdG4fVkW2mwfDB8kwpPsYCq.java:43)
    at java.lang.reflect.Method.invoke(Method.java:601)
    at package1.b.Test.main(MyClass2.java:87)
    Caused by: java.lang.RuntimeException: method safeMethod() is accessible only by Dummy object
    at package1.b.Dummy.safeMethod(MyClass2.java:54)
    ... 5 more
```

[How do I access private methods and private data members via reflection?](https://stackoverflow.com/questions/11483647/how-do-i-access-private-methods-and-private-data-members-via-reflection)

# 尾递归

最简单的递归形式是把递归调用语句放在函数结尾即恰在return语句之前。这种形式被称作尾递归或者结尾递归，因为递归调用出现在函数尾部。由于尾递归的作用相当于一条循环语句，所以它是最简单的递归形式

对于“尾递归”的情况，也就是说函数体中用到的变量不需要栈保存，是可以进行优化的，会将其展开成循环，但如果递归函数中有分支就不行，比如路径遍历的实现。有分支就要保存条件变量，就需要压栈

# 异常和错误

![img](http://img.my.csdn.net/uploads/201211/27/1354020417_5176.jpg)

## Error

**程序无法处理的错误** ：表示运行应用程序中较严重问题。大多数错误与代码编写者执行的操作无关，而表示代码运行时 JVM（Java 虚拟机）出现的问题。例如，Java虚拟机运行错误（Virtual MachineError），当 JVM 不再有继续执行操作所需的内存资源时，将出现 OutOfMemoryError。这些异常发生时，Java虚拟机（JVM）一般会选择线程终止。

这些错误表示故障发生于虚拟机自身、或者发生在虚拟机试图执行应用时，如Java虚拟机运行错误（Virtual MachineError）、类定义错误（NoClassDefFoundError）等。这些错误是不可查的，因为它们在应用程序的控制和处理能力之 外，而且绝大多数是程序运行时不允许出现的状况。对于设计合理的应用程序来说，即使确实发生了错误，本质上也不应该试图去处理它所引起的异常状况。在 Java中，错误通过Error的子类描述。

## Exception

程序本身可以处理的异常

**运行时异常：** RuntimeException类及其子类异常，如NullPointerException(空指针异常)、IndexOutOfBoundsException(下标越界异常)等，这些异常是不检查异常，程序中可以选择捕获处理，也可以不处理。这些异常一般是由程序逻辑错误引起的，程序应该从逻辑角度尽可能避免这类异常的发生。

运行时异常的特点是Java**编译器不会检查**它，也就是说，当程序中可能出现这类异常，即使没有用try-catch语句捕获它，也没有用throws子句声明抛出它，也会编译通过。

**非运行时异常 （编译异常）：**是RuntimeException以外的异常，类型上都属于Exception类及其子类。从程序语法角度讲是**必须进行处理的异常，如果不处理，程序就不能编译通过**。如IOException、SQLException等以及用户自定义的Exception异常，一般情况下不自定义检查异常。

[深入理解java异常处理机制](http://blog.csdn.net/hguisu/article/details/6155636)

# 注解

Java注解是附加在代码中的一些元信息，用于一些工具在编译、运行时进行解析和使用，起到说明、配置的功能
注解不会也不能影响代码的实际逻辑，仅仅起到辅助性的作用。包含在 java.lang.annotation 包中。

## 分类

### 按运行机制划分

* 源码注解：只在源码中存在，编译成.class文件就不存在了
* 编译时注解：在源码和.class文件中都存在。像前面的`@Override`、`@Deprecated`、`@SuppressWarnings`，他们都属于编译时注解
* 运行时注解：在运行阶段还起作用，甚至会影响运行逻辑的注解。像`@Autowired`自动注入的这样一种注解就属于运行时注解，它会在程序运行的时候把你的成员变量自动的注入进来

### 按来源划分

* JDK注解
  * `@Override`：告诉用户和编译器该方法覆盖了父类中的同一个方法
  * `@Deprecated`：表明这个方法已经过时了，会发出警告
  * `@Suppvisewarnings("xxx")`：表示忽略了xxx的警告

* 第三方注解

  例如Spring的`@Autowired`，`@Service`，Mybatis的`@InsertProvider`，`@Options`等

* 自定义注解

### 元注解

元注解是给注解进行注解，可以理解为注解的注解就是元注解

## 自定义注解

使用`@interface`关键字定义一个注解

```java
// 元注解部分
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented

// 注解部分
public @interface Description {
    String desc();
    String author();
    int age() default 18;
}
```

其中定义的“方法”在注解里只是一个成员变量，可以用`default`指定默认值

* 成员类型是受限制的，合法的类型包括基本的数据类型以及String，Class，Annotation,Enumeration等。
* 如果注解只有一个成员，则成员名必须取名为`value()`，在使用时可以忽略成员名和赋值号（=）。
* 注解类可以没有成员，没有成员的注解称为标识注解。

元注解部分说明：

* `@Target`：作用域列表，格式为`ElementType.xxx`，中间用逗号分隔

  * `METHOD`：方法声明

    * `CONSTRUCTOR`：构造方法声明
    * `FIELD`：字段声明
    * `LOCAL VARIABLE`：局部变量声明
    * `METHOD`：方法声明
    * `PACKAGE`：包声明
    * `PARAMETER`：参数声明
    * `TYPE`：类接口

* `@Retention`：生命周期

  * `RUNTIME`在运行时存在，可以通过反射读取

    * `SOURCE`：只在源码显示，编译时丢弃
    * `CLASS`：编译时记录到class中，运行时忽略

* `@Inherited`是一个标识性的元注解，它允许子注解继承它。

* `@Documented`，生成javadoc时会包含注解

使用自定义注解：`@<注解名>(<成员名1>=<成员值1>,<成员名1>=<成员值1>,…)`

```java
@Description(desc="i am Color",author="boy",age=18)
public String Color() {
    return "red";
}
```

## 解析注解

通过反射获取类 、函数或成员上的**运行时注解信息**，从而实现动态控制程序运行的逻辑。

```java
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Description {
    String value();
}

@Description("Class Annotation")
public class Child {
    @Override
    @Description("Method Annotation")
    public String name() {
        return null;
    }
}

public static void main(String[] args) {
    try {
        // 使用类加载器加载类
        Class c = Class.forName("com.test.Child");
        // 找到类上面的注解
        boolean isExist = c.isAnnotationPresent(Description.class);
        // 上面的这个方法是用这个类来判断这个类是否存在Description这样的一个注解
        if (isExist) {
            // 拿到注解实例，解析类上面的注解
            Description d = (Description) c.getAnnotation(Description.class);
            System.out.println(d.value());
        }

        //获取所有的方法
        Method[] ms = c.getMethods();
        for (Method m : ms) {
            //拿到方法上的所有的注解
            Annotation[] as = m.getAnnotations();
            for (Annotation a : as) {
                //用二元操作符判断a是否是Description的实例
                if (a instanceof Description) {
                    Description d = (Description) a;
                    System.out.println(d.value());
                }

            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

输出

```shell
Class Annotation
Method Annotation
```

[框架开发之Java注解的妙用](https://www.jianshu.com/p/b560b30726d4)