# Dagger

[TOC]

# 背景知识

## 控制反转和依赖注入

**控制反转**（Inversion of Control，缩写为**IoC**），是[面向对象编程](https://zh.wikipedia.org/wiki/%E9%9D%A2%E5%90%91%E5%AF%B9%E8%B1%A1%E7%BC%96%E7%A8%8B)中的一种设计原则，可以用来减低计算机代码之间的[耦合度](https://zh.wikipedia.org/wiki/%E8%80%A6%E5%90%88%E5%BA%A6_(%E8%A8%88%E7%AE%97%E6%A9%9F%E7%A7%91%E5%AD%B8))

一般情况下，A依赖于B，需要在内部new一个B的对象

```java
class A {
    B b;
    
    public A() {
        // 在A内部new一个B的对象
        this.b = new B();
    }
}
```

控制反转就是将B对象的new过程交给外部控制程序，最后传入A对象

实现控制反转的主要方式

* 依赖注入
  * 基于接口/set方法/构造函数/注解等传入

```java
class A {
    B b;
    // 通过注解查找传入
    @Inject B b1;
    
    // 通过构造函数传入
    public A(B b) {
        this.b = b;
    }
    
    // 通过方法或直接调用a.b=xxx设置
    public setB(B b) {
        this.b = b;
    }
}
```

* 依赖查找
  * 通过调用框架提供的方法获取对象

## Java 依赖注入标准（JSR-330）

[Java 依赖注入标准（JSR-330）简介](https://docs.google.com/document/d/1-4YYpKvSpna6r-HfFladtqURqt6W3NqkjO9CYUt-0yY/preview)

## 引入

```groovy
implementation 'com.google.dagger:dagger:2.15'
apt 'com.google.dagger:dagger-compiler:2.15'
```

# API

如果需要详细的代码demo和源码解析，参考[参考](#参考)第一篇，建议自己手动操作参照Dagger自动生成的代码附注理解，事实上Dagger自动生成的代码非常接近于人工手写，很好理解

## @Inject

用于标记应该被依赖注入框架提供的依赖

```java
class A {
    // 属性注入：
    // 表示b应该由依赖注入框架提供实例，不能为private
    @Inject B b;
    private C c;
    private D d;
    
    // 构造器注入： 
    // 表示依赖注入框架可以使用该构造方法提供A的实例，且使用时需要提供参数C的实例
    public A(C c) {
        this.c = c;
    }
    
    // 方法注入：
    // 表示依赖注入框架会在构造器执行后立即调用该方法，且使用时需要提供参数D的实例
    // 该方法必须为public，原理上与属性注入类似，当需要使用this对象时优于属性注入
    public void setD(D d) {
        this.d = d;
    }
}
```

## @Component

Dagger使用@Component完成依赖注入，定义@Component如下

```java
@Component
// 定义为目标类名+Component，Rebuild后会自动生成Dagger+目标类名+Component
public interface AComponent { 
    // Dagger会从目标类（参数A）开始查找@Inject注解并自动生成依赖注入代码，调用inject即可完成
    void inject(A a);
    
    // 这种方式一般为其他Component提供依赖
    B getB();
}
```

### dependencies和@SubComponent

一个Comonent依赖于另一个Component，可以使用2种方式

### 使用dependencies

A依赖于B，B中提供了A所需的实例，但是A和B都是独立的Component

```java
@Component(dependencies = {BComponent.class})
public interface AComponent {
    void inject(A a);
}
```

同时B定义时需要提供get方法

```java
@Component
public interface BComponent {
    C getC();
}
```

使用时需要指定Component

```java
DaggerAComponent.builder().bComponent(DaggerBComponent.create()).inject(a);
```

### 使用@SubComponent

形式上更类似于一个插件，也可以表示依赖关系

例如同样是A依赖于B，但是B是一个独立的Component，提供单向扩展为A的方法

```java
@Component
public interface BComponent {
    // 表示使用AModule后扩展为AComponent
    AComponent plus(AModule aModule);
}

@SubComponent
public inteface AComponent {
    void inject();
}
```

使用时写法也不同，相对会更加简洁

```java
DaggerBComponent.plus(new AModule()).inject(a);
```

## @Module和@Provides

用于提供自定义的实例，而不仅仅使用@Inject注解调用对应构造器

```java
@Module
// 命名为提供者类名+Module
public class AModule {
    
    // 表示该方法用于提供RepoViewModel的实例
    @Provides
    public A provideA() {
        return new A()
    }
}
```

定义了Module后，需要在Component上指定对应的module

```java
@Component(modules = {AModule.class})
```

## @Scope, @Singleton

用于指定作用域，最常见的是使用@Singleton创建单例

作用域必须配合使用，即Module和Component都必须使用相同作用域，例如

```java
@Module
class AModule {

    // 表示提供单例实例
    @Provides
    @Singleton
    public A provideA() {
        return new A();
    }
}

// 对应Component必须声明为相同作用域
@Singleton
@Component(modules = {AModule.class})
public interface BComponent {
    public void inject(B b);
    
    public A getA();
}
```

同时@Singleton标注的Component不能依赖于另一个@Singleton标注的Component，单例依赖单例是违反设计原则的

例如MainActivityComponent需要使用BComponent，则需要定义一个新的@Scope，单例会在该@Scope标注的范围内保持局部单例，实质上@Singleton也是一个@Scope

> ```java
> @Scope
> @Documented
> @Retention(RUNTIME)
> public @interface Singleton {}
> ```

仿照@Singleton写法，定一个@Scope，例如ActivityScope

```java
@Scope
@Documented
@Retention(RUNTIME)
public @interface ActivityScope {}

@ActivityScope
@Component(dependencies = {BComponent.class})
public interface MainActivityScope {
    void inject(MainActivity mainActivity);
}
```

### 在Android中实现全局单例

在Android中全局单例=Application生命周期内单例，最好的办法是保证对应的Component直接由Application提供

例如有以下Component

```java
@Singleton
@Component(modules = {UserModule.class})
public interface UserComponent {
    // ...
}

@Component(dependencies = {UserComponent.class})
public interface LoginActivityComponent {
    void inject(LoginActivity activity);
}
```

Application中初始化对应Component

```java
public class MyApplication extends Application {
    private UserComponent userComponent;
    
    @Override
    public void onCreate() {
        super.onCreate();
        userComponent = DaggerUserComponent.create();
    }
    
    public UserComponent getUserComponent() {
        return userComponent;
    }
}
```

LoginActivity中使用

```java
public class LoginActivity extends AppCompatActivity {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        DaggerLoginActivityComponent.builder()
            .userComponent(((MyApplication) getApplication()).getUserComponent())
            .build()
            .inject(this);
    }
}
```

## @MapKey

用于定义依赖集合Map或Set

定义

```java
@MapKey(unwrapValue = true)
@interface TestKey {
    String value();
}
```

提供

```java
@Provides(type = Type.MAP)
@TestKey("foo")
String provideFooKey() {
    return "foo value";
}

@Provides(type = Type.MAP)
@TestKey("bar")
String provideBarKey() {
    return "bar value";
}
```

使用

```java
@Inject
Map<String, String> map;

map.toString() // => „{foo=foo value, bar=bar value}”
```

## Lazy

Lazy模式表示@Inject时不初始化，而是等到调用get时才初始化

```java
public class A {
    @Inject Lazy<B> b;
    
    public A() {
        DaggerAComponent.create().inject(this);
       	Log.d("A", b.get());
    }
}
```

# AndroidInjection

AndroidInjection是dagger提供给Android向四大组件和Fragment注入依赖的工具，相比传统dagger写法有些不同，因为传统dagger在注入时需要自己调用DaggerXXXComponent进行注入，而且还需要指定依赖，重复代码较多，不利于开发

### 引入

```groovy
implementation 'com.google.dagger:dagger-android:2.15'
implementation 'com.google.dagger:dagger-android-support:2.15'
apt 'com.google.dagger:dagger-android-processor:2.15'
```

### API

### 配置AndroidInjectionModule和AndroidSupportInjectionModule

```java
@Module
public class AppModule {
    private Application application;
    
    public AppModule(Application application) {
        this.application = application;
    }
    
    @Provides
    @Singleton
    Application provideApplication() {
        return application
    }
}

@Component(modules = {
    AppModule.class, 
    AndroidInjectionModule.class, 
    AndroidSupportInjectionModule.class
        })
public interface AppComponent() {
    void inject(MyApplication application);
}
```

### 设置Application

实现HasActivityInject，注入DispatchingAndroidInjector

```java
public class MyApplication extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerAppComponent.builder().appModule(new AppModule(this)).inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
```

### 配置YourActivity注入内容

定义YourActivitySubcomponent和YourActivityModule

```java
@Subcomponent(modules = {/*...*/})
public interface YourActivitySubcomponent extends AndroidInjector<YourActivity> {
    @Subcomponent.Builder
        public abstract class Builder extends AndroidInjector.Builder<YourActivity> {}
}

@Module(subcomponents = {YourActivitySubcomponent.class})
abstract class YourActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(YourActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
        bindYourActivityInjectorFactory(YourActivitySubcomponent.Builder builder);
}

@Component(modules = {/*...*/, YourActivityModule.class})
interface YourApplicationComponent {}
```

或者直接在AppModule中定义

```java
@ActivityScope
@ContributesAndroidInjector(modules = { /* modules to install into the subcomponent */ })
abstract YourActivity contributeYourActivityInjector();
```

### 使用

```java
public class YourActivity extends Activity {
    @Inject YourPresenter yourPresenter;
    
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }
}
```

### 更好的形式

定义BaseActivity，其他Activity都继承自该Acitivity

```java
public class BaseActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }
}
```

定义ActivitiesModule，所有需要注入的Activity都在此注册，并提供给AppComponent

```java
@Module
public class ActivitiesModule {
    @ContributesAndroidInjector(modules = MainActivityModule.class)
    abstract MainActivity contributeMainActivitytInjector();

    @ContributesAndroidInjector(modules = SecondActivityModule.class)
    abstract SecondActivity contributeSecondActivityInjector();
}

@Component(modules = {
    AppModule.class, 
    ActivitiesModule.class
    AndroidInjectionModule.class, 
    AndroidSupportInjectionModule.class
        })
public interface AppComponent() {
    void inject(MyApplication application);
}
```

# 项目Demo

本人项目：[JetPackDemo](https://github.com/lizij/JetPackDemo)

其他参考项目：[GithubClient](https://github.com/lizij/GithubClient.git)

# 参考

[控制反转](https://zh.wikipedia.org/wiki/%E6%8E%A7%E5%88%B6%E5%8F%8D%E8%BD%AC)

[Dagger2 最清晰的使用教程](https://www.jianshu.com/p/24af4c102f62)

[Dependency injection with Dagger 2 - the API](http://frogermcs.github.io/dependency-injection-with-dagger-2-the-api/)

[Dagger2在Android开发中的新用法.](https://segmentfault.com/a/1190000010016618)

[Dagger User's Guide](https://google.github.io/dagger/users-guide)

[告别Dagger2模板代码：Dagger Android使用详解](https://blog.csdn.net/mq2553299/article/details/77485800)