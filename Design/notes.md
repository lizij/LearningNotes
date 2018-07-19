# 设计

[TOC]

# 设计原则

## 单一职责原则

### 概念

在[面向对象编程](https://zh.wikipedia.org/wiki/%E9%9D%A2%E5%90%91%E5%AF%B9%E8%B1%A1%E7%BC%96%E7%A8%8B)领域中，**单一功能原则**（Single responsibility principle）规定每个类都应该有一个单一的功能，并且该功能应该由这个类完全封装起来。所有它（这个类）的服务都应该严密的和该功能平行

> 功能平行，意味着没有依赖

### 职责扩散

因为某种原因，职责P（类T负责）需要被分化为粒度更细的职责P1和P2

* 遵循原则：分解T为T1和T2，分别负责P1和P2，但是代码修改量大
* 不遵循原则：直接修改T以适配P1和P2，但是扩展性较差

### 示例

用一个类描述动物呼吸这个场景

```java
class Animal{
    public void breathe(String animal){
        System.out.println(animal+"呼吸空气");
    }
}
public class Client{
    public static void main(String[] args){
        Animal animal = new Animal();
        animal.breathe("牛");
        animal.breathe("羊");
        animal.breathe("猪");
    }
}
```

但是并不是所有动物都呼吸空气，比如鱼是呼吸水，那么可以有以下修改

* 拆分Animal为陆生动物Terrestrial和水生动物Aquatic（类和方法级别上都符合单一职责）

  ```java
  class Terrestrial{  
      public void breathe(String animal){  
          System.out.println(animal+"呼吸空气");  
      }  
  }  
  class Aquatic{  
      public void breathe(String animal){  
          System.out.println(animal+"呼吸水");  
      }  
  }  
    
  public class Client{  
      public static void main(String[] args){  
          Terrestrial terrestrial = new Terrestrial();  
          terrestrial.breathe("牛");  
          terrestrial.breathe("羊");  
          terrestrial.breathe("猪");  
            
          Aquatic aquatic = new Aquatic();  
          aquatic.breathe("鱼");  
      }  
  }  
  ```

* 拆分方法breathe（只有方法级别上符合单一职责，方法比较少才可以使用）

  ```java
  class Animal{  
      public void breathe(String animal){  
          System.out.println(animal+"呼吸空气");  
      }  
    
      public void breathe2(String animal){  
          System.out.println(animal+"呼吸水");  
      }  
  }  
    
  public class Client{  
      public static void main(String[] args){  
          Animal animal = new Animal();  
          animal.breathe("牛");  
          animal.breathe("羊");  
          animal.breathe("猪");  
          animal.breathe2("鱼");  
      }  
  }  
  ```

* 直接修改breathe（类和方法级别上都不遵循单一职责）

  ```java
  class Animal{  
      public void breathe(String animal){  
          if("鱼".equals(animal)){  
              System.out.println(animal+"呼吸水");  
          }else{  
              System.out.println(animal+"呼吸空气");  
          }  
      }  
  }  
    
  public class Client{  
      public static void main(String[] args){  
          Animal animal = new Animal();  
          animal.breathe("牛");  
          animal.breathe("羊");  
          animal.breathe("猪");  
          animal.breathe("鱼");  
      }  
  }
  ```

[单一功能原则-维基百科](https://zh.wikipedia.org/wiki/%E5%8D%95%E4%B8%80%E5%8A%9F%E8%83%BD%E5%8E%9F%E5%88%99)

[设计模式六大原则（1）：单一职责原则](https://blog.csdn.net/zhengzhb/article/details/7278174)

# 设计模式

## 单例模式

### 使用场景

1. 应用中某个实例对象需要频繁的被访问
2. 应用中每次启动只会存在一个实例。如账号系统，数据库系统

### 懒汉式——延迟加载

#### 标准形式

优点：单例只有在使用时才会被实例化，一定程度上节约了资源，可以延迟加载

缺点：第一次加载时需要及时进行实例化，反应稍慢，最大的问题是每次调用getInstance 都进行同步，造成不必要的同步开销

```java
public class Singleton {  
    private static Singleton instance = null;  
    private Singleton() {}  
    public static synchronized Singleton getInstance() {  
        if (instance == null) {  
            instance = new Singleton();  
        }  
        return instance;  
    }  
}
```

#### Double Check形式

第一个判空是为了避免不必要的同步，第二层判断是为了在null 情况下创建实例

能够实现的关键在于volatile关键字，禁止指令重排序优化，这样可以保证对象按照[既定过程](#创建过程)创建

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

### 饿汉式——即时加载

#### 标准形式

优点：类加载时就创建了一次实例，不会存在多线程创建多个实例的问题

缺点：即使没有被使用也会创建，占用内存

```java
public class Singleton {  
    private static Singleton instance = new Singleton();  
    private Singleton() {}  
    public static Singleton getInstance() {  
        return instance;  
    }  
}
```

#### 静态内部类形式

利用了类加载机制的单线程操作特点保证单例，同时加载Singleton类时不会加载Holder内部类，因此调用getInstance时才会唯一一次加载Holder类并初始化instance，从而同时保证了单例和延迟加载

```java
public class Singleton {
    private Singleton(){}
    public static Singleton getInstance() {
        return Holder.instance;
    }
    private static class Holder {
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

优点：它在任何情况下都是单例的，也是最简单的。在上述的几种单例模式下，都会有一种情况，它们会出现重新创建对象的情况，那就是反序列化。要杜绝单例对象在反序列化时重新生成对象，那么必须加入如下方法

```java
private Object readResolve() throws ObjectStreamException {
    return instance;
}
```

但是枚举就不必要加这个方法，因为反序列化它也不会生成新的实例，同时也可以防止构造方法被反射调用创建新实例

缺点：内存占用大

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

1. 抽象工厂角色：这是工厂方法模式的核心，它与应用程序无关。是具体工厂角色必须实现的接口或者必须继承的父类。在java中它由抽象类或者接口来实现。
2. 具体工厂角色：它含有和具体业务逻辑有关的代码。由应用程序调用以创建对应的具体产品的对象。在java中它由具体的类来实现。
3. 抽象产品角色：它是具体产品继承的父类或者是实现的接口。在java中一般有抽象类或者接口来实现。
4. 具体产品角色：具体工厂角色所创建的对象就是此角色的实例。在java中由具体的类来实现。

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

1. 抽象工厂角色：这是工厂方法模式的核心，它与应用程序无关。是具体工厂角色必须实现的接口或者必须继承的父类。在java中它由抽象类或者接口来实现。
2. 具体工厂角色：它含有和具体业务逻辑有关的代码。由应用程序调用以创建对应的具体产品的对象。在java中它由具体的类来实现。
3. 抽象产品角色：它是具体产品继承的父类或者是实现的接口。在java中一般有抽象类或者接口来实现。
4. 具体产品角色：具体工厂角色所创建的对象就是此角色的实例。在java中由具体的类来实现。

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

[建造者模式](http://www.runoob.com/design-pattern/builder-pattern.html)

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

策略模式是对算法的包装，是把使用算法的责任和算法本身分割开来，委派给不同的对象管理。策略模式通常把一个系列的算法包装到一系列的策略类里面，作为一个抽象策略类的子类。用一句话来说，就是：“准备一组算法，并将每一个算法封装起来，使得它们可以互换”

[《JAVA与模式》之策略模式——我看过最好的一篇策略模式博文](http://blog.csdn.net/zhangliangzi/article/details/52161211)

## 观察者模式

观察者模式是软件设计模式的一种。有时被称作发布/订阅模式，观察者模式定义了一种一对多的依赖关系，让多个观察者对象同时监听某一个主题对象。这个主题对象在状态发生变化时，会通知所有观察者对象，使它们能够自动更新自己。此种模式通常被用来实现事件处理系统。

## 适配器模式

适配器模式（Adapter Pattern）是作为两个不兼容的接口之间的桥梁。这种类型的设计模式属于结构型模式，它结合了两个独立接口的功能。

将一个类的接口转换成客户希望的另外一个接口。适配器模式使得**原本由于接口不兼容而不能一起工作的那些类可以一起工作**

例如，读卡器是作为内存卡和笔记本之间的适配器。将内存卡插入读卡器，再将读卡器插入笔记本，这样就可以通过笔记本来读取内存卡

例如，由于InputStream是字节流不能享受到字符流读取字符那么便捷的功能，因此借助InputStreamReader将其转为Reader子类，因此可以拥有便捷操作文本文件方法。OutputStream同理

```java
//1、获得子节输入流
FileInputStream fileInputStream = new FileInputStream(file);
//2、构造转换流(是继承Reader的) InputStreamReader类具有将子节输入流转换为字符输入流的功能
InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
```

## 装饰器模式

装饰器模式（Decorator Pattern）允许**向一个现有的对象添加新的功能，同时又不改变其结构**。这种类型的设计模式属于结构型模式，它是作为现有的类的一个包装。

动态地给一个对象添加一些额外的职责。就增加功能来说，装饰器模式相比生成子类更为灵活

例如，将InputStream字节流包装为BufferedReader过程就是装饰的过程。一开始InputStream只有read一个字节的方法，包装为Reader之后拥有read一个字符的功能，在包装成BufferedReader之后就拥有read一行字符串功能。OutputStream同理

```java
//2、构造转换流(是继承Reader的)
InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//3、 构造缓冲字符流
BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
```

## 桥接模式

### 概念

桥接（Bridge）是用于把抽象化与实现化解耦，使得二者可以独立变化。这种类型的设计模式属于结构型模式，它通过提供抽象化和实现化之间的桥接结构，来实现二者的解耦

**何时使用：**实现系统可能有多个角度分类，每一种角度都可能变化。

**如何解决：**把这种多角度分类分离出来，让它们独立变化，减少它们之间耦合

### 使用

![桥接模式的 UML 图](http://www.runoob.com/wp-content/uploads/2014/08/bridge_pattern_uml_diagram.jpg)

创建桥接实现接口DrawAPI.java

```java
public interface DrawAPI {
    public void drawCircle(int radius, int x, int y);
}
```

创建实现了 DrawAPI接口的实体桥接实现类

```java
public class RedCircle implements DrawAPI {
    @Override
    public void drawCircle(int radius, int x, int y) {
        System.out.println("Drawing Circle[ color: red, radius: "
                           + radius +", x: " +x+", "+ y +"]");
    }
}

public class GreenCircle implements DrawAPI {
    @Override
    public void drawCircle(int radius, int x, int y) {
        System.out.println("Drawing Circle[ color: green, radius: "
                           + radius +", x: " +x+", "+ y +"]");
    }
}
```

使用 DrawAPI接口创建抽象类 Shape.java

```java
public abstract class Shape {
    protected DrawAPI drawAPI;
    protected Shape(DrawAPI drawAPI){
        this.drawAPI = drawAPI;
    }
    public abstract void draw();    
}
```

创建实现了 Shape接口的实体类Circle.java

```java
public class Circle extends Shape {
    private int x, y, radius;

    public Circle(int x, int y, int radius, DrawAPI drawAPI) {
        super(drawAPI);
        this.x = x;  
        this.y = y;  
        this.radius = radius;
    }

    public void draw() {
        drawAPI.drawCircle(radius,x,y);
    }
}
```

使用 Shape 和 DrawAPI类画出不同颜色的圆BridgePatternDemo.java

```java
public class BridgePatternDemo {
    public static void main(String[] args) {
        Shape redCircle = new Circle(100,100, 10, new RedCircle());
        Shape greenCircle = new Circle(100,100, 10, new GreenCircle());

        redCircle.draw();
        greenCircle.draw();
    }
}
```

验证输出。

```shell
Drawing Circle[ color: red, radius: 10, x: 100, 100]
Drawing Circle[  color: green, radius: 10, x: 100, 100]
```

## 生产消费者模式

## 概念

生产者消费者问题是多线程的一个经典问题，它描述是有**一块缓冲区作为仓库，生产者可以将产品放入仓库，消费者则可以从仓库中取走产品**。

解决生产者/消费者问题的方法可分为两类：

- 采用某种机制保护生产者和消费者之间的同步；
- 在生产者和消费者之间建立一个管道。

第一种方式有较高的效率，并且易于实现，代码的可控制性较好，属于常用的模式。第二种管道缓冲区不易控制，被传输数据对象不易于封装等，实用性不强

### 实现

#### wait+notify

```java
public class InterThreadCommunicationExample {

    public static void main(String args[]) {

        final Queue sharedQ = new LinkedList();

        Thread producer = new Producer(sharedQ);
        Thread consumer = new Consumer(sharedQ);

        producer.start();
        consumer.start();

    }
}

public class Producer extends Thread {
    private final Queue sharedQ;

    public Producer(Queue sharedQ) {
        super("Producer");
        this.sharedQ = sharedQ;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (sharedQ) {
                while (sharedQ.size() >= 1) {
                    // 等待Product被消费
                    try {
                        sharedQ.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                sharedQ.add("Product");
                sharedQ.notify();
            }
        }
    }
}

public class Consumer extends Thread {
    private final Queue sharedQ;

    public Consumer(Queue sharedQ) {
        super("Consumer");
        this.sharedQ = sharedQ;
    }

    @Override
    public void run() {
        while(true) {
            synchronized (sharedQ) {
                //waiting condition - wait until Queue is not empty
                while (sharedQ.size() == 0) {
                    try {
                        sharedQ.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                String number = sharedQ.poll();
                sharedQ.notify();
            }
        }
    }
}
```

#### await+signal

await()和signal()的功能基本上和wait() / nofity()相同，完全可以取代它们，但是它们和新引入的锁定机制Lock直接挂钩，具有更大的灵活性。通过在Lock对象上调用newCondition()方法，将条件变量和一个锁对象进行绑定，进而控制并发程序访问竞争资源的安全。

```java
public class Storage {
    // 仓库最大存储量
    private final int MAX_SIZE = 100;

    // 仓库存储的载体
    private LinkedList<Object> list = new LinkedList<Object>();
    // 锁
    private final Lock lock = new ReentrantLock();

    // 仓库满的条件变量
    private final Condition full = lock.newCondition();

    // 仓库空的条件变量
    private final Condition empty = lock.newCondition();

    // 生产产品
    public void produce(String producer) {
        lock.lock();
        // 如果仓库已满
        while (list.size() == MAX_SIZE) {
            System.out.println("仓库已满，【" + producer + "】： 暂时不能执行生产任务!");
            try {
                // 由于条件不满足，生产阻塞
                full.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 生产产品
        list.add(new Object());

        System.out.println("【" + producer + "】：生产了一个产品\t【现仓储量为】:" + list.size());

        empty.signalAll();

        // 释放锁
        lock.unlock();

    }

    // 消费产品
    public void consume(String consumer) {
        // 获得锁
        lock.lock();

        // 如果仓库存储量不足
        while (list.size() == 0) {
            System.out.println("仓库已空，【" + consumer + "】： 暂时不能执行消费任务!");
            try {
                // 由于条件不满足，消费阻塞
                empty.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        list.remove();
        System.out.println("【" + consumer + "】：消费了一个产品\t【现仓储量为】:" + list.size());
        full.signalAll();

        // 释放锁
        lock.unlock();

    }

    public LinkedList<Object> getList() {
        return list;
    }

    public void setList(LinkedList<Object> list) {
        this.list = list;
    }

    public int getMAX_SIZE() {
        return MAX_SIZE;
    }
}
```



#### BlockingQueue

它是一个已经在内部实现了同步的队列，实现方式采用的是我们第2种await() / signal()方法。它可以在生成对象时指定容量大小。它用于阻塞操作的是put()和take()方法：

`put()`：类似于我们上面的生产者线程，容量达到最大时，自动阻塞。

`take()`：类似于我们上面的消费者线程，容量为0时，自动阻塞。

```java
import java.util.concurrent.LinkedBlockingQueue;

public class Storage {
    // 仓库最大存储量
    private final int MAX_SIZE = 100;

    // 仓库存储的载体
    private LinkedBlockingQueue<Object> list = new LinkedBlockingQueue<Object>(100);  

    // 生产产品
    public void produce(String producer) {
        // 如果仓库已满
        if (list.size() == MAX_SIZE) {
            System.out.println("仓库已满，【" + producer + "】： 暂时不能执行生产任务!");            
        }

        // 生产产品
        try {
            list.put(new Object());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("【" + producer + "】：生产了一个产品\t【现仓储量为】:" + list.size());
    }

    // 消费产品
    public void consume(String consumer) {
        // 如果仓库存储量不足
        if (list.size() == 0) {
            System.out.println("仓库已空，【" + consumer + "】： 暂时不能执行消费任务!");            
        }

        try {
            list.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("【" + consumer + "】：消费了一个产品\t【现仓储量为】:" + list.size());        

    }

    public LinkedBlockingQueue<Object> getList() {
        return list;
    }

    public void setList(LinkedBlockingQueue<Object> list) {
        this.list = list;
    }
    public int getMAX_SIZE() {
        return MAX_SIZE;
    }
}
```

[生产者消费者问题Java三种实现](http://www.cnblogs.com/Ming8006/p/7243858.html)

[JAVA多线程（三）生产者消费者模式及实现方法](https://blog.csdn.net/antony9118/article/details/51481884)