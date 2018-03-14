# Java面试题
[TOC]

# ArrayList，LinkedList和Vector

|      | ArrayList | LinkedList | Vector |
| ---- | --------- | ---------- | ------ |
| 实现   | 动态数组      | 链表         | 动态数组   |
| 随机访问 | 支持        | 不支持        | 支持     |
| 随机增删 | 需要移动数据    | 不需要移动数据    |        |
| 线程安全 | 不支持       | 不支持        | 支持     |
| 扩容   | 1.5       |            | 2      |
| 实现接口 |           | Queue      |        |

[比较ArrayList、LinkedList、Vector](http://blog.csdn.net/renfufei/article/details/17077425)

# Classloader

## 基本概念

与C/C++编写的程序不同，JAVA程序并不是一个可执行文件，而是由许多独立的类文件组成，每一个文件对应一个JAVA类。此外，这些类文件并非全部装入内存，而是根据程序需要逐渐载入。ClassLoader是JVM实现的一部分，ClassLoader包括bootstrap classloader（启动类加载器），ExtClassLoader（扩展类加载器）和AppClassLoader(系统类加载器)

* **bootstrap classloader** ：在JVM运行的时候加载JAVA核心的API，以满足JAVA程序最基本的需求，其中就包括后两种ClassLoader。
* **ExtClassLoader**：加载JAVA的扩展API，也就是/lib/ext中的类
* **AppClassLoader**：用来加载在用户机器上CLASSPATH设置目录中的Class的，通常在没有指定ClassLoader的情况下，程序员自定义的类就由该ClassLoader进行加载。

除了Java默认提供的三个ClassLoader之外，用户还可以根据需要定义自已的ClassLoader，而这些自定义的ClassLoader都必须继承自java.lang.ClassLoader类，也包括Java提供的另外2个ClassLoader（Extension ClassLoader和App ClassLoader）在内，但是Bootstrap ClassLoader不继承自ClassLoader，因为它不是一个普通的Java类，底层由C++编写，已嵌入到了JVM内核当中，当JVM启动后，Bootstrap ClassLoader也随着启动，负责加载完核心类库后，并构造Extension ClassLoader和App ClassLoader类加载器

## 加载过程

当运行一个程序的时候，JVM启动，运行bootstrap classloader，加载JAVA核心API，同时加载另两个ClassLoader。然后调用ExtClassLoader加载扩展API，最后AppClassLoader加载CLASSPATH目录下定义的Class.这是最基本的加载流程

## 加载原理

### 双亲委托模式

ClassLoader使用的是双亲委托模型来搜索类的，每个ClassLoader实例都有一个父类加载器的引用（不是继承的关系，是一个包含的关系），虚拟机内置的类加载器（Bootstrap ClassLoader）本身没有父类加载器，但可以用作其它ClassLoader实例的的父类加载器。当一个ClassLoader实例需要加载某个类时，它会试图亲自搜索某个类之前，先把这个任务委托给它的父类加载器，这个过程是由上至下依次检查的，首先由最顶层的类加载器Bootstrap ClassLoader试图加载，如果没加载到，则把任务转交给Extension ClassLoader试图加载，如果也没加载到，则转交给App ClassLoader 进行加载，如果它也没有加载得到的话，则返回给委托的发起者，由它到指定的文件系统或网络等URL中加载该类。如果它们都没有加载到这个类时，则抛出ClassNotFoundException异常。否则将这个找到的类生成一个类的定义，并将它加载到内存当中，最后返回这个类在内存中的Class实例对象

### 使用双亲委托的原因

因为这样可以避免重复加载，当父亲已经加载了该类的时候，就没有必要子ClassLoader再加载一次。考虑到安全因素，我们试想一下，如果不使用这种委托模式，那我们就可以随时使用自定义的String来动态替代java核心api中定义的类型，这样会存在非常大的安全隐患，而双亲委托的方式，就可以避免这种情况，因为String已经在启动时就被引导类加载器（Bootstrcp ClassLoader）加载，所以用户自定义的ClassLoader永远也无法加载一个自己写的String，除非你改变JDK中ClassLoader搜索类的默认算法

## class相同的条件

1. 类名是否相同
2. 是否同一个类加载器实例加载的

只有两者同时满足的情况下，JVM才认为这两个class是相同的。就算两个class是同一份class字节码，如果被两个不同的ClassLoader实例所加载，JVM也会认为它们是两个不同class。

比如网络上的一个Java类org.classloader.simple.NetClassLoaderSimple，javac编译之后生成字节码文件NetClassLoaderSimple.class，ClassLoaderA和ClassLoaderB这两个类加载器并读取了NetClassLoaderSimple.class文件，并分别定义出了java.lang.Class实例来表示这个类，对于JVM来说，它们是两个不同的实例对象，但它们确实是同一份字节码文件，如果试图将这个Class实例生成具体的对象进行转换时，就会抛运行时异常java.lang.ClassCaseException，提示这是两个不同的类型

# 对象

## 创建过程

### 检测类是否被加载

虚拟机遇到一条new指令时，首先将去检查这个指令的参数是否能在常量池中定位到一个类的符号引用，并且检查这个符号引用代表的类是否已被加载、解析和初始化过。如果没有，那必须先执行相应的类加载过程。

### 为新生对象分配内存

为对象分配空间的任务等同于把一块确定大小的内存从Java堆中划分出来。分配的方式有两种： 

1. 指针碰撞：假设Java堆中内存是绝对规整的，用过的和空闲的内存各在一边，中间放着一个指针作为分界点的指示器，分配内存就是把那个指针向空闲空间的那边挪动一段与对象大小相等的距离。 
2. 空闲列表：如果Java堆中的内存不是规整的，虚拟机就需要维护一个列表，记录哪个内存块是可用的，在分配的时候从列表中找到一块足够大的空间划分给对象实例，并更新列表上的记录。

采用哪种分配方式是由Java堆是否规整决定的，而Java堆是否规整是由所采用的垃圾收集器是否带有压缩整理功能决定的。 

另外一个需要考虑的问题就是对象创建时的线程安全问题，有两种解决方案：

1. 对分配内存空间的动作进行同步处理；
2. 把内存分配的动作按照线程划分在不同的空间之中进行，即每个线程在Java堆中预先分配一小块内存(TLAB)，哪个线程要分配内存就在哪个线程的TLAB上分配，只有TLAB用完并分配新的TLAB时才需要同步锁定。

### 初始化为零值

内存分配完成后，虚拟机需要将分配到的内存空间都初始化为零值（不包括对象头），这一步操作保证了对象的实例字段在Java代码中可以不赋初始值就直接使用，程序能访问到这些字段的数据类型所对应的零值。

### 必要设置

接下来，虚拟机要对对象进行必要的设置，例如这个对象是哪个类的实例、如何才能找到类的元数据信息、对象的哈希码、对象的GC分代年龄等信息。这些信息存放在对象的对象头之中。

### 执行构造方法

把对象按照程序员的意愿进行初始化

## 内存布局

在HotSpot虚拟机中，对象在内存中存储的布局可分为三个部分： 对象头、实例数据和对齐填充。

对象头包括两个部分：第一部分用于存储对象自身的运行时数据，如哈希码、GC分代年龄、线程所持有的锁等。官方称之为“Mark Word”。第二个部分为是类型指针，即对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。

实例数据是对象真正存储的有效信息，也是程序代码中所定义的各种类型的字段内容。

对齐填充并不是必然存在的，仅仅起着占位符的作用。、Hotpot VM要求对象起始地址必须是8字节的整数倍，对象头部分正好是8字节的倍数，所以当实例数据部分没有对齐时，需要通过对齐填充来对齐。

## 访问定位

Java程序通过栈上的reference数据来操作堆上的具体对象。主要的访问方式有使用句柄和直接指针两种：

1. 句柄：Java堆将会划出一块内存来作为句柄池，引用中存储的就是对象的句柄地址，而句柄中包含了对象实例数据与类型数据各自的具体地址信息 。如图所示：

   ![Java内存区域详解](http://ww1.sinaimg.cn/mw690/b254dc71gw1eumzdy6lupg20i308waae.gif)

2. 直接指针：Java堆对象的布局要考虑如何放置访问类型数据的相关信息，引用中存储的就是对象地址 。如图所示：

   ![Java内存区域详解](http://ww3.sinaimg.cn/mw690/b254dc71gw1eumzdyjnawg20if08hglw.gif)

两个方式各有优点，使用句柄最大的好处是引用中存储的是稳定的句柄地址，对象被移动时只会改变句柄中实例的地址，引用不需要修改、使用直接指针访问的好处是速度更快，它节省了一次指针定位的时间开销。

# 泛型

泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数

## 原理

1. 类型检查：在生成字节码之前提供类型检查
2. 类型擦除：所有类型参数都用他们的限定类型替换，包括类、变量和方法（类型擦除）
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

有两种限定通配符，一种是<? extends T>它通过确保类型必须是T的子类来设定类型的上界，另一种是<? super T>它通过确保类型必须是T的父类来设定类型的下界。泛型类型必须用限定内的类型来进行初始化，否则会导致编译错误。

<?>表 示了非限定通配符，因为<?>可以用任意类型来替代

T 可以是类也可以是接口，在泛型中没有implement关键字

指定多个绑定类型：例如<? extends T & U>

## 其他

### 运行时类型查询只适用于原始类型

```java
Pair<String> ps = new Pair<String>();
Pair<Integer> pi = new Pair<Integer>();
if (ps.getClass() == pi.getClass()) // always true
```

这点对于 instanceof 或强制类型转换同样适用

[java泛型总结2-2 面试题总结](http://zhouchaofei2010.iteye.com/blog/2259899)

# HashMap

## 工作原理

* 基本原理

  HashMap是基于hashing的原理，我们使用put(key, value)存储对象到HashMap中，使用get(key)从HashMap中获取对象。当我们给put()方法传递键和值时，我们先对键调用hashCode()方法，返回的hashCode用于找到bucket位置来储存Entry对象


* 两个对象hashcode相同会发生什么

  因为hashcode相同，所以它们的bucket位置相同，碰撞会发生。因为HashMap使用链表存储对象，这个Entry(包含有键值对的Map.Entry对象)会存储在链表中

* 如果两个键的hashcode相同如何获取值对象

  找到bucket位置之后，会调用keys.equals()方法去找到链表中正确的节点，最终找到要找的值对象

* 如果HashMap的大小超过了负载因子(load factor)定义的容量怎么办

  默认的负载因子大小为0.75，也就是说，当一个map填满了75%的bucket时候，和其它集合类(如ArrayList等)一样，将会创建原来HashMap大小的两倍的bucket数组，来重新调整map的大小，并将原来的对象放入新的bucket数组中。这个过程叫作rehashing，因为它调用hash方法找到新的bucket位置

* 重新调整HashMap大小存在什么问题

  重新调整HashMap大小的时候存在条件竞争，因为如果两个线程都发现HashMap需要重新调整大小了，它们会同时试着调整大小。在调整大小的过程中，存储在链表中的元素的次序会反过来，因为移动到新的bucket位置的时候，HashMap并不会将元素放在链表的尾部，而是放在头部，这是为了避免尾部遍历(tail traversing)。如果条件竞争发生了，那么就死循环了

* 为什么String, Interger这样的wrapper类适合作为键

  因为String是不可变的，也是final的，而且已经重写了equals()和hashCode()方法了。其他的wrapper类也有这个特点。不可变性是必要的，因为为了要计算hashCode()，就要防止键值改变，如果键值在放入时和获取时返回不同的hashcode的话，那么就不能从HashMap中找到你想要的对象。不可变性还有其他的优点如线程安全

* 可以使用自定义的对象作为键吗

  可以使用任何对象作为键，只要它遵守了equals()和hashCode()方法的定义规则，并且当对象插入到Map中之后将不会再改变了。如果这个自定义对象时不可变的，那么它已经满足了作为键的条件，因为当它创建之后就已经不能改变了

* 可以使用CocurrentHashMap来代替Hashtable吗

  Hashtable是synchronized的，但是ConcurrentHashMap同步性能更好，因为它仅仅根据同步级别对map的一部分进行上锁。ConcurrentHashMap当然可以代替HashTable，但是HashTable提供更强的线程安全性

**总结**

HashMap基于hashing原理，我们通过put()和get()方法储存和获取对象。当我们将键值对传递给put()方法时，它调用键对象的hashCode()方法来计算hashcode，找到bucket位置来储存值对象。当获取对象时，通过键对象的equals()方法找到正确的键值对，然后返回值对象。HashMap使用链表来解决碰撞问题，当发生碰撞了，对象将会储存在链表的下一个节点中。 HashMap在每个链表节点中储存键值对对象。

当两个不同的键对象的hashcode相同时会发生什么？ 它们会储存在同一个bucket位置的链表中。键对象的equals()方法用来找到键值对

## HashMap和HashTable的区别

1. 继承的父类不同

   Hashtable继承自Dictionary类

   HashMap继承自AbstractMap类，但二者都实现了Map接口

2. 线程安全性不同

   Hashtable是线程安全的，Hashtable 中的方法是Synchronize的。在多线程并发的环境下，可以直接使用Hashtable，不需要为它的方法实现同步

   HashMap是非线程安全的。HashMap中的方法在缺省情况下是非Synchronize的。在多线程并发的环境下，使用HashMap时就必须要增加同步处理。这一般通过对自然封装该映射的对象进行同步操作来完成。如果不存在这样的对象，则应该使用**Collections.synchronizedMap**方法来“包装”该映射。最好在创建时完成这一操作，以防止对映射进行意外的非同步访问

3. 是否提供contains方法

   Hashtable保留了contains，containsValue和containsKey三个方法，其中contains和containsValue功能相同

   HashMap把Hashtable的contains方法去掉了，改成containsValue和containsKey，因为contains方法容易让人引起误解

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

为了减少程序再并发执行时所付出的时空开销，使OS具有更好的并发性。**线程是“进程”中某个单一顺序的控制流，也被称为轻量进程**

### 与进程的对比

1. **调度**。进程是资源拥有的基本单位，线程是调度和分派的基本单位。
2. **并发性**。进程之间可以并发执行，在一个进程中的多个线程之间也可以并发执行。
3. **拥有资源**。 进程可以拥有资源，是系统中拥有资源的一个基本单位。而线程自己不拥有系统资源，但它可以访问其隶属进程的资源。
4. **系统开销** 系统创建进程需要为该进程重新分配系统资源，但创建线程的代价很小。因此多线程的实现多任务并发比多进程实现并发的效率高

## 多线程

### 概念

1. 并发与并行：

   并发是通过CPU时间分片，快速在进程切换而模拟出多进程

   并行是多个进程并行执行


1. 阻塞：系统会一直等待这个函数执行完毕直到它产生一个返回值

### 使用

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

1. 继承Thread类

```java
class MyThread extends Thread {
    public void run() {
        //这里是线程要执行的任务
    }
}
```

> 由于Java中不允许多继承，我们自定义的类继承了Thread后便不能再继承其他类，这在有些场景下会很不方便；实现Runnable接口的那个方法虽然稍微繁琐些，但是它的优点在于自定义的类可以继承其他的类

**结束线程的方式** ：采用设置一个条件变量的方式，run方法中的while循环会不断的检测flag的值，在想要结束线程的地方将flag的值设置为false就可以

> 不使用stop的原因：因为它在终止一个线程时会强制中断线程的执行，不管run方法是否执行完了，并且还会释放这个线程所持有的所有的锁对象。这一现象会被其它因为请求锁而阻塞的线程看到，使他们继续向下执行。这就会造成数据的不一致

### 线程池

基本思想还是一种对象池的思想，开辟一块内存空间，里面存放了众多(未死亡)的线程，池中线程执行调度由池管理器来处理。当有线程任务时，从池中取一个，执行完成后线程对象归池，这样可以避免反复创建线程对象所带来的性能开销，节省了系统的资源。

1. 避免线程的创建和销毁带来的性能开销。
2. 避免大量的线程间因互相抢占系统资源导致的阻塞现象。
3. 能够对线程进行简单的管理并提供定时执行、间隔执行等功能。

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

静态优先执行，父类优先于子类执行。静态代码块是在JVM加载类的时候执行的，而且静态代码块执行且仅执行一次

1. 父类静态代码块
2. 子类静态代码块
3. 父类非静态代码块
4. 父类构造函数
5. 子类非静态代码块
6. 子类构造函数

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

finalize()方法可以被子类对象所覆盖，然后作为一个终结者，当GC被调用的时候完成最后的清理工作（例如释放系统资源之类）。这就是终止。默认的finalize()方法什么也不做，当被调用时直接返回

应该尽量避免使用finalize()。相对于其他JVM实现，终结器被调用的情况较少——可能是因为终结器线程的优先级别较低的原因。如果你依靠终结器来关闭文件或者其他系统资源，可能会将资源耗尽，当程序试图打开一个新的文件或者新的系统资源的时候可能会崩溃，就因为这个缓慢的终结器

## Class< > getClass()

通过getClass()方法可以得到一个和这个类有关的java.lang.Class对象。返回的Class对象是一个被static synchronized方法封装的代表这个类的对象；例如，static sychronized void foo(){}。这也是指向反射API。因为调用gerClass()的对象的类是在内存中的，保证了类型安全

## int hashCode()

hashCode()方法返回给调用者此对象的哈希码（其值由一个hash函数计算得来）。这个方法通常用在基于hash的集合类中，像java.util.HashMap,java.until.HashSet和java.util.Hashtable

在覆盖equals()的时候同时覆盖hashCode()可以保证对象的功能兼容于hash集合。这是一个好习惯，即使这些对象不会被存储在hash集合中

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

   **wait()是Object的方法**，也就是说可以对任意一个对象调用wait方法，调用wait()方法后会将调用者的线程挂起，直到其他线程调用同一个对象的notify()方法才会重新激活调用者

2. 同步锁释放不同

   **sleep()不释放同步锁,wait()释放同步锁.**

   Thread.sleep不会导致锁行为的改变，如果当前线程是拥有锁的，那么Thread.sleep不会让线程释放锁。

   而当调用wait()方法的时候，线程会放弃对象锁，进入等待此对象的等待锁定池，只有针对此对象调用notify()方法后本线程才进入对象锁定池准备

3. 使用方式不同

   sleep()方法可以在任何地方使用；wait()方法则只能在同步方法或同步块中使用

[Java：Object类详解](http://blog.csdn.net/jack_owen/article/details/39936483)

# StrongReference，SoftReference 和WeakReference

StrongReference 是 Java 的默认引用实现,  它会尽可能长时间的存活于 JVM 内， 当没有任何对象指向它时 GC 执行后将会被回收；

WeakReference， 顾名思义,  是一个弱引用,  当所引用的对象在 JVM 内不再有强引用时, GC 后 WeakReference 将会被自动回收；

SoftReference 于 WeakReference 的特性基本一致， 最大的区别在于 SoftReference 会尽可能长的保留引用直到 JVM 内存不足时才会被回收(虚拟机保证), 这一特性使得 SoftReference 非常适合缓存应用

# String

## String和StringBuffer，StringBuilder

1. String用于存放字符的数组被声明为final的，因此只能赋值一次，不可再更改。 
2. 要是需要多次更改，需要用到StringBulider或者StringBuffer，两者不同点在于StringBuffer是线程安全的。 
3. StringBulider转为String： `String m = sb.toString()` 
   String转StringBuilder： `StringBuilder sb = new StringBuilder(m)` 

## 不变性

### 定义

new String(“xxx”)会在堆中创建对象，所有在编译期间确定的字符串都会在常量池中，具有不变性

不变性是指引用的对象实例的是不可以改变的，但是可以改变引用地址，所以通过改变引用地址就可以改变值了

string.intern()会根据字符串内容去常量池中寻找并返回相同内容的字符串，如果没有则先创建

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

# 设计模式

## 单例模式

### 使用场景

1. 应用中某个实例对象需要频繁的被访问
2. 应用中每次启动只会存在一个实例。如账号系统，数据库系统

### 懒汉式

实名一个静态对象，并且在用户第一次调用getInstance 时进行初始化

优点：单例只有在使用时才会被实例化，一定程度上节约了资源

缺点：第一次加载时需要及时进行实例化，反应稍慢，最大的问题是每次调用getInstance 都进行同步，造成不必要的同步开销

```java
public class Singleton {  
    private static Singleton instance = null;  
    private Singleton() {}  
    public static Singleton getInstance() {  
        if (instance == null) {  
            instance = new Singleton();  
        }  
        return instance;  
    }  
}
```

### 饿汉式

在声明静态对象时就已经初始化

```java
public class Singleton {  
    private static Singleton instance = new Singleton();  
    private Singleton() {}  
    public static synchronized Singleton getInstance() {  
        return instance;  
    }  
}
```

### Double Check Lock

第一个判空是为了避免不必要的同步，第二层判断是为了在null 情况下创建实例

优点：资源利用率高，第一次执行getInstance 时才会被实例化，效率高

缺点：第一次加载反应慢，也由于java 内存 模型的原因偶尔会失败，在高并发环境下，有一定缺陷，虽然发生概率很小

> Volatile修饰的成员变量在每次被线程访问时，都强迫从共享内存中重读该成员变量的值。而且，当成员变量发生变化时，强迫线程将变化值回写到共享内存。这样在任何时刻，两个不同的线程总是看到某个成员变量的同一个值

```java
public class Singleton {  
    private volatile static Singleton instance = null;  
    private Singleton() {}  
    public static synchronized Singleton getInstance() {  
        if (instance == null) {
            synchronized(Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }  
        return instance;
    }  
}
```

### 静态内部类单例模式

加载singleton 类时不会初始化instance 。只有在调用getInstance 方法时，才会导致instance 被初始化。由于SingletonHolder只会被类加载器加载一次，所以静态常量instance只会被初始化一次且不能再被更改。这个方法不仅能够确保线程安全，也能够保证单例对象的唯一性,同时也延迟了单例的实例化，是推荐使用的单例模式实现方式。

```java
public class Singleton {
    private Singleton(){}
    public static final Singleton getInstance() {
        return SingletonHolder.instance;
    }
    private static class SingletonHolder {
        private static final Singleton instance = new Singleton();
    }
}
```

### 枚举单例

```java
public enum Singleton {
    INSTANCE;
    public void dosomething() {
    }
}
```

它在任何情况下都是单例的，也是最简单的。在上述的几种单例模式下，都会有一种情况，它们会出现重新创建对象的情况，那就是反序列化。要杜绝单例对象在反序列化时重新生成对象，那么必须加入如下方法

```java
private Object readResolve() throws ObjectStreamException {
    return instance;
}
```

但是枚举就不必要加这个方法，因为反序列话它也不会生成新的实例

### 使用容器模式实现单例

将众多单例模式类型注入到一个统一的管理类中，在使用时根据key 对应类型的对象。这种方式使得我们可以管理多种类型的单例，并且在使用时可以通过统一的接口进行获取操作，降低了用户的使用成本，也对用户隐藏了具体实现，降低了耦合度

```java
public class SingletonManager {
    private static Map<String, Object> obMap = new HashMap<String, Object>();
    private SingletonManager() {}
    public static void registerService (String key, Object instance) {
        if (obMap.containsKey(key)) {
            obMap.put(key, instance);
        }
    }

    public static Object getService (String key) {
        return obMap.get(key);
    }
}
```

[Android 设计模式之 单例模式](https://www.jianshu.com/p/93dfb78d292f)

## 工厂模式

### 使用场景

1. 在编码时不能预见需要创建哪种类的实例。
2. 系统不应依赖于产品类实例如何被创建、组合和表达的细节

### 简单工厂模式

它由三种角色组成（关系见下面的类图）：

1. 工厂类角色：这是本模式的核心，含有一定的商业逻辑和判断逻辑。在java中它往往由一个具体类实现。
2. 抽象产品角色：它一般是具体产品继承的父类或者实现的接口。在java中由接口或者抽象类来实现。
3. 具体产品角色：工厂类所创建的对象就是此角色的实例。在java中由一个具体类实现。

![clip_image002](https://images.cnblogs.com/cnblogs_com/poissonnotes/WindowsLiveWriter/3b3b76bcffad_13405/clip_image002_thumb.jpg)

```java
//抽象产品角色
public interface Car{ 
  public void drive(); 
}

//具体产品角色
public class Benz implements Car{ 
  public void drive() { 
    System.out.println("Driving Benz "); 
  } 
}

//工厂类角色
public class Driver{

  //工厂方法
  //注意 返回类型为抽象产品角色
  public static Car driverCar(String s) throws Exception {

    //判断逻辑，返回具体的产品角色给Client 
    if(s.equalsIgnoreCase("Benz")) return new Benz(); 
    else if(s.equalsIgnoreCase("Bmw")) return new Bmw();
  }
}

//欢迎暴发户出场...... 
public class Magnate{ 
  public static void main(String[] args){ 
    //告诉司机我今天坐奔驰
    Car car = Driver.driverCar("benz"); 
    //下命令：开车
    car.drive(); 
  }
}
```

### 工厂方法模式

组成：
1、抽象工厂角色：这是工厂方法模式的核心，它与应用程序无关。是具体工厂角色必须实现的接口或者必须继承的父类。在java中它由抽象类或者接口来实现。
2、具体工厂角色：它含有和具体业务逻辑有关的代码。由应用程序调用以创建对应的具体产品的对象。在java中它由具体的类来实现。
3、抽象产品角色：它是具体产品继承的父类或者是实现的接口。在java中一般有抽象类或者接口来实现。
4、具体产品角色：具体工厂角色所创建的对象就是此角色的实例。在java中由具体的类来实现。

![clip_image006](https://images.cnblogs.com/cnblogs_com/poissonnotes/WindowsLiveWriter/3b3b76bcffad_13405/clip_image006_thumb.jpg)

```java
//抽象工厂角色
public interface Driver{ 
  public Car driverCar(); 
} 

public class BenzDriver implements Driver{ 
  public Car driverCar(){ 
    return new Benz(); 
  } 
} 

//应该和具体产品形成对应关系，这里略... 
//有请暴发户先生
public class Magnate 
{ 
  public static void main(String[] args) 
  { 
    Driver driver = new BenzDriver();

    Car car = driver.driverCar(); 
    car.drive(); 
  } 
} 
```

### 抽象工厂模式

它和工厂方法模式的区别就在于需要创建对象的复杂程度上。而且抽象工厂模式是三个里面最为抽象、最具一般性的。抽象工厂模式的用意为：给客户端提供一个接口，可以创建多个产品族中的产品对象。而且使用抽象工厂模式还要满足一下条件：

1. 系统中有多个产品族，而系统一次只可能消费其中一族产品
2. 同属于同一个产品族的产品一起使用时。

来看看抽象工厂模式的各个角色（和工厂方法的如出一辙）：
抽象工厂角色：这是工厂方法模式的核心，它与应用程序无关。是具体工厂角色必须实现的接口或者必须继承的父类。在java中它由抽象类或者接口来实现。
具体工厂角色：它含有和具体业务逻辑有关的代码。由应用程序调用以创建对应的具体产品的对象。在java中它由具体的类来实现。
抽象产品角色：它是具体产品继承的父类或者是实现的接口。在java中一般有抽象类或者接口来实现。
具体产品角色：具体工厂角色所创建的对象就是此角色的实例。在java中由具体的类来实现。

![clip_image008](https://images.cnblogs.com/cnblogs_com/poissonnotes/WindowsLiveWriter/3b3b76bcffad_13405/clip_image008_thumb.jpg)

[工厂模式](http://www.cnblogs.com/poissonnotes/archive/2010/12/01/1893871.html)

## 建造者模式

### 定义

将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示

建造模式是将复杂的内部创建封装在内部，对于外部调用的人来说，只需要传入建造者和建造工具，对于内部是如何建造成成品的，调用者无需关心，良好的封装性是建造者模式的优点之一。

建造者类逻辑独立，易拓展。

### 角色

1. Builder：给出一个抽象接口或抽象类，以规范产品的建造。这个接口规定要实现复杂对象的哪些部分的创建，并不涉及具体的对象部件的创建，一般由子类具体实现。
2. ConcreteBuilder：Builder接口的实现类，并返回组建好对象实例。
3. Director：调用具体建造者来创建复杂对象的各个部分，在指导者中不涉及具体产品的信息，只负责保证对象各部分完整创建或按某种顺序创建。
4. Product：要创建的复杂对象，产品类。

### 使用场景

1. 当产品有复杂的内部构造时（参数很多）。
2. 需要生产的产品的属性相互依赖，这些属性的赋值顺序比较重要时（因为在调用ConcreteBuilder的赋值方法时是有先后顺序的）。

### 实例

要组装一台电脑（Computer类），我们假设它有三个部件：CPU 、主板以及内存

```java
public class Computer {  
  private String mCpu;  
  private String mMainboard;  
  private String mRam;  

  public void setmCpu(String mCpu) {  
    this.mCpu = mCpu;  
  }  

  public void setmMainboard(String mMainboard) {  
    this.mMainboard = mMainboard;  
  }  

  public void setmRam(String mRam) {  
    this.mRam = mRam;  
  }  
}  
```

Builder

```java
public abstract class Builder {  
  public abstract void buildCpu(String cpu);  
  public abstract void buildMainboard(String mainboard);  
  public abstract void buildRam(String ram);  
  public abstract Computer create();  
}  

public class MyComputerBuilder extends Builder {  
  private Computer mComputer = new Computer();  
  @Override  
  public void buildCpu(String cpu) {  
    mComputer.setmCpu(cpu);  
  }  

  @Override  
  public void buildMainboard(String mainboard) {  
    mComputer.setmMainboard(mainboard);  
  }  

  @Override  
  public void buildRam(String ram) {  
    mComputer.setmRam(ram);  
  }  

  @Override  
  public Computer create() {  
    return mComputer;  
  }  
}  
```

Director

```java
public class Direcror {  
  Builder mBuild=null;  
  public Direcror(Builder build){  
    this.mBuild=build;  
  }  
  public Computer CreateComputer(String cpu,String mainboard,String ram){  
    //规范建造流程，这个顺序是由它定的  
    this.mBuild.buildMainboard(mainboard);  
    this.mBuild.buildCpu(cpu);  
    this.mBuild.buildRam(ram);  
    return mBuild.create();  
  }  
}  
```

使用

```java
Builder mBuilder = new MyComputerBuilder();  
Direcror mDirecror = new Direcror(mBuilder);  
mDirecror.CreateComputer("i7","Intel主板","mRam");//返回Computer实例对象  
```

[设计模式——建造者模式解析](http://blog.csdn.net/SEU_Calvin/article/details/52249885)

## 门面模式

### 使用场景

门面模式要求一个子系统的外部与其内部的通信必须通过一个统一的门面(Facade)对象进行。门面模式提供一个高层次的接口，使得子系统更易于使用。

就如同医院的接待员一样，门面模式的门面类将客户端与子系统的内部复杂性分隔开，使得客户端只需要与门面对象打交道，而不需要与子系统内部的很多对象打交道。

### 角色

![img](https://images.cnblogs.com/cnblogs_com/zhenyulu/Pic90.gif)

门面(Facade)角色：客户端可以调用这个角色的方法。此角色知晓相关的(一个或者多个)子系统的功能和责任。在正常情况下，本角色会将所有从客户端发来的请求委派到相应的子系统去。

子系统(subsystem)角色：可以同时有一个或者多个子系统。每一个子系统都不是一个单独的类，而是一个类的集合。每一个子系统都可以被客户端直接调用，或者被门面角色调用。子系统并不知道门面的存在，对于子系统而言，门面仅仅是另外一个客户端而已。

### 实例

一个保安系统由两个录像机、三个电灯、一个遥感器和一个警报器组成。保安系统的操作人员需要经常将这些仪器启动和关闭。

首先，在不使用门面模式的情况下，操作这个保安系统的操作员必须直接操作所有的这些部件。下图所示就是在不使用门面模式的情况下系统的设计图。

![img](https://images.cnblogs.com/cnblogs_com/zhenyulu/Pic88.gif)

一个合情合理的改进方法就是准备一个系统的控制台，作为保安系统的用户界面。如下图所示：

![img](https://images.cnblogs.com/cnblogs_com/zhenyulu/Pic89.gif)

[门面（Facade）模式](http://www.cnblogs.com/skywang/articles/1375447.html)

## 策略模式

策略模式是对算法的包装，是把使用算法的责任和算法本身分割开来，委派给不同的对象管理。策略模式通常把一个系列的算法包装到一系列的策略类里面，作为一个抽象策略类的子类。用一句话来说，就是：“准备一组算法，并将每一个算法封装起来，使得它们可以互换”。下面就以一个示意性的实现讲解策略模式实例的结构

[《JAVA与模式》之策略模式——我看过最好的一篇策略模式博文](http://blog.csdn.net/zhangliangzi/article/details/52161211)

## 观察者模式

观察者模式是软件设计模式的一种。有时被称作发布/订阅模式，观察者模式定义了一种一对多的依赖关系，让多个观察者对象同时监听某一个主题对象。这个主题对象在状态发生变化时，会通知所有观察者对象，使它们能够自动更新自己。此种模式通常被用来实现事件处理系统。

# 内存管理

## 内存区域

![Java内存区域详解](http://ww1.sinaimg.cn/mw690/b254dc71gw1eumzdxs38lg20eb0a1aab.gif)

### 程序计数器

程序计数器，可以看做是当前线程所执行的字节码的行号指示器。在虚拟机的概念模型里，字节码解释器工作就是通过改变程序计数器的值来选择下一条需要执行的字节码指令，分支、循环、跳转、异常处理、线程恢复等基础功能都要依赖这个计数器来完成。

多线程中，为了让线程切换后能恢复到正确的执行位置，每条线程都需要有一个独立的程序计数器，各条线程之间互不影响、独立存储，因此这块内存是 线程私有 的。

当线程正在执行的是一个Java方法，这个计数器记录的是在正在执行的虚拟机字节码指令的地址；当执行的是Native方法，这个计数器值为空。

此内存区域是唯一一个没有规定任何OutOfMemoryError情况的区域 

### Java虚拟机栈

Java虚拟机栈也是线程私有的 ，它的生命周期与线程相同。虚拟机栈描述的是Java方法执行的内存模型：每个方法在执行的同时都会创建一个栈帧用于存储局部变量表、操作数栈、动态链表、方法出口信息等。每一个方法从调用直至执行完成的过程，就对应着一个栈帧在虚拟机栈中入栈到出栈的过程。

局部变量表中存放了编译器可知的各种基本数据类型(boolean、byte、char、short、int、float、long、double)、对象引用和returnAddress类型(指向了一条字节码指令的地址)。

如果扩展时无法申请到足够的内存，就会抛出OutOfMemoryError异常。

### 本地方法栈

本地方法栈与虚拟机的作用相似，不同之处在于虚拟机栈为虚拟机执行的Java方法服务，而本地方法栈则为虚拟机使用到的Native方法服务。有的虚拟机直接把本地方法栈和虚拟机栈合二为一。

会抛出stackOverflowError和OutOfMemoryError异常。

### Java堆

Java堆是所有线程共享的一块内存区域，在虚拟机启动时创建，此内存区域的唯一目的就是存放对象实例 。

Java堆是垃圾收集器管理的主要区域。由于现在收集器基本采用分代回收算法，所以Java堆还可细分为：新生代和老年代。从内存分配的角度来看，线程共享的Java堆中可能划分出多个线程私有的分配缓冲区(TLAB)。

Java堆可以处于物理上不连续的内存空间，只要逻辑上连续的即可。在实现上，既可以实现固定大小的，也可以是扩展的。

如果堆中没有内存完成实例分配，并且堆也无法完成扩展时，将会抛出OutOfMemoryError异常。

### 方法区

方法区是各个线程共享的内存区域，它用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据 。

相对而言，垃圾收集行为在这个区域比较少出现，但并非数据进了方法区就永久的存在了，这个区域的内存回收目标主要是针对常量池的回收和对类型的卸载，

当方法区无法满足内存分配需要时，将抛出OutOfMemoryError异常。

### 运行时常量池

方法区的一部分，它用于存放编译期生成的各种字面量和符号引用。

[Java内存管理原理及内存区域详解](http://www.importnew.com/16433.html)

## GC工作原理

JVM的GC采用根搜索算法，设立若干种根对象，当任何一个根对象到某一个对象均不可达时，则认为这个对象是可以被回收的

根一般有4种：

1. 栈（栈帧中的本地变量表）中引用的对象。
2. 方法区中的静态成员。
3. 方法区中的常量引用的对象（全局变量）
4. 本地方法栈中JNI（一般说的Native方法）引用的对象。

### 内存回收准则

一个变量不可达，即没有任何可达变量指向它。当一个对象不可达时会调用finalize()方法，但是仅调用一次

### 现代GC回收算法

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

# 内部类

## 成员内部类

成员内部类是最普通的内部类，它的定义为位于另一个类的内部

```java
class Outter {   
  class Inner {
  }
}
```

成员内部类，就是作为外部类的成员，可以直接使用外部类的所有成员和方法，即使是private的。

在外部类中如果要访问成员内部类的成员，必须先创建一个成员内部类的对象，再通过指向这个对象的引用来访问

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

将内部类移到了外部类的方法中，然后在外部类的方法中再生成一个内部类对象去调用内部类方法。如果此时我们需要往外部类的方法中传入参数，那么外部类的方法形参必须使用**final**定义

在方法中定义的内部类只能访问方法中final类型的局部变量，这是因为在方法中定义的局部变量相当于一个常量，它的生命周期超出方法运行的生命周期，由于局部变量被设置为final，所以不能再内部类中改变局部变量的值

## 静态嵌套类

又叫静态局部类、嵌套内部类，就是修饰为static的内部类。声明为static的内部类，不需要内部类对象和外部类对象之间的联系，就是说我们可以直接引用outer.inner，即不需要创建外部类，也不需要创建内部类

```java
class Outter {   
  static class Inner {
  }
}
```

可以看到，如果用static 将内部内静态化，那么内部类就只能访问外部类的静态成员变量，具有局限性。

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

匿名内部类不能有访问修饰符和static修饰符的

**匿名内部类是唯一一种没有构造器的类**。正因为其没有构造器，所以匿名内部类的使用范围非常有限，大部分匿名内部类用于接口回调。匿名内部类在编译的时候由系统自动起名为Outter$1.class。一般来说，匿名内部类用于继承其他类或是实现接口，并不需要增加额外的方法，只是对继承方法的实现或是重写

[Java内部类的一些总结](http://www.cnblogs.com/hasse/p/5020519.html)

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
* static和“this、super”势不两立，static跟具体对象无关，而this、super正好跟具体对象有关。
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

# 线程安全

## 定义

非线程安全是指多线程操作同一个对象可能会出现问题

线程安全是多线程操作同一个对象不会有问题

如果你的代码所在的进程中有多个线程在同时运行，而这些线程可能会同时运行这段代码。如果每次运行结果和单线程运行的结果是一样的，而且其他的变量的值也和预期的是一样的，就是线程安全的

线程安全的类：Vector，HashTable，StringBuffer

非线程安全的类：ArrayList，HashMap，StringBuilder

## 实现

线程安全是通过线程同步控制来实现的，也就是synchronized关键字

如果使用很多**synchronized关键字**来同步控制，必然会导致**性能降低**

## synchronized

### 说明

synchronized关键字可以作为函数的修饰符，也可作为函数内的语句，也就是平时说的同步方法和同步语句块。如果再细的分类，synchronized可作用于instance变量、object reference（对象引用）、static函数和class literals(类名称字面常量)身上

### 关键点

1. 无论synchronized关键字加在方法上还是对象上，它取得的锁都是对象，而不是把一段代码或函数当作锁――而且同步方法很可能还会被其他线程的对象访问。
2. 每个对象只有一个锁（lock）与之相关联。
3. 实现同步是要很大的系统开销作为代价的，甚至可能造成死锁，所以尽量避免无谓的同步控制

### 用法说明

1. 函数修饰符:锁定调用这个同步方法对象

   ```java
   public synchronized void methodAAA() {
     //….. 
   }
   ```

   相当于

   ```java
   public void methodAAA()  
   {  
     synchronized (this) // this指的就是调用这个方法的对象
     {  
       //…..  
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

3. 用于static：锁定class literal

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

## volatile

保证了新值能立即存储到主内存，每次使用前立即从主内存中刷新。 

禁止指令重排序优化

> 编译器和处理器”为了提高性能，而在程序执行时会对程序进行的重排序。它的出现是为了提高程序的并发度，从而提高性能！但是对于多线程程序，重排序可能会导致程序执行的结果不是我们需要的结果！重排序分为“编译器”和“处理器”两个方面，而“处理器”重排序又包括“指令级重排序”和“内存的重排序”

[JAVA线程安全之synchronized关键字的正确用法](http://blog.csdn.net/yaerfeng/article/details/7254734)

[Java线程安全和非线程安全](http://blog.csdn.net/xiao__gui/article/details/8934832)

# 尾递归

最简单的递归形式是把递归调用语句放在函数结尾即恰在return语句之前。这种形式被称作尾递归或者结尾递归，因为递归调用出现在函数尾部。由于尾递归的作用相当于一条循环语句，所以它是最简单的递归形式

对于“尾递归”的情况，也就是说函数体中用到的变量不需要栈保存，是可以进行优化的，会将其展开成循环，但如果递归函数中有分支就不行，比如路径遍历的实现。有分支就要保存条件变量，就需要压栈

# 错误和异常

![img](http://img.my.csdn.net/uploads/201211/27/1354020417_5176.jpg)

## Error

程序无法处理的错误，表示运行应用程序中较严重问题。大多数错误与代码编写者执行的操作无关，而表示代码运行时 JVM（Java 虚拟机）出现的问题。例如，Java虚拟机运行错误（Virtual MachineError），当 JVM 不再有继续执行操作所需的内存资源时，将出现 OutOfMemoryError。这些异常发生时，Java虚拟机（JVM）一般会选择线程终止。

这些错误表示故障发生于虚拟机自身、或者发生在虚拟机试图执行应用时，如Java虚拟机运行错误（Virtual MachineError）、类定义错误（NoClassDefFoundError）等。这些错误是不可查的，因为它们在应用程序的控制和处理能力之 外，而且绝大多数是程序运行时不允许出现的状况。对于设计合理的应用程序来说，即使确实发生了错误，本质上也不应该试图去处理它所引起的异常状况。在 Java中，错误通过Error的子类描述。

## Exception

程序本身可以处理的异常

**运行时异常：**都是RuntimeException类及其子类异常，如NullPointerException(空指针异常)、IndexOutOfBoundsException(下标越界异常)等，这些异常是不检查异常，程序中可以选择捕获处理，也可以不处理。这些异常一般是由程序逻辑错误引起的，程序应该从逻辑角度尽可能避免这类异常的发生。

运行时异常的特点是Java编译器不会检查它，也就是说，当程序中可能出现这类异常，即使没有用try-catch语句捕获它，也没有用throws子句声明抛出它，也会编译通过。

**非运行时异常 （编译异常）：**是RuntimeException以外的异常，类型上都属于Exception类及其子类。从程序语法角度讲是必须进行处理的异常，如果不处理，程序就不能编译通过。如IOException、SQLException等以及用户自定义的Exception异常，一般情况下不自定义检查异常。

[深入理解java异常处理机制](http://blog.csdn.net/hguisu/article/details/6155636)



