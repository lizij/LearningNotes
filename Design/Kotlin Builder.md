# Kotlin Builder

[TOC]

## Java Builder

* 优点：逻辑清晰
* 缺点：代码冗长

```java
public class Bean {
    private int a;

    public int getA() {
        return a;
    }

    public static class Builder {
        private int a = 0;

        public Builder a(int a) {
            this.a = a;
            return this;
        }
        
        public Bean build() {
            Bean inst = new Bean();
            inst.a = a;
            return inst;
        }
    }
}
```

## Kotlin Builder

### 标准写法

* 优点：相对java代码更简短
* 缺点：Bean每多一个字段，Builder中都需要一个对应的变量和方法

```kotlin
class Bean(
    val a: Int
) {
    class Builder {
        var a: Int = 0
        
        fun a(a: Int) = apply { this.a = a }
        fun build(): Bean {
            val inst = Bean()
            inst.a = a
            return inst
        }
    }
}
```

####

### 不标准的写法

* 优点：相对标准写法更简短，可以不需要在Builder中不需要写对应的变量
* 缺点：Builder中仍然需要一个对应字段的方法

```kotlin
class Bean (
    var a: Int = 0
) {
    class Builder {
        var inst = Bean()
        
        fun a(a: Int) = apply { this.a = a }
        fun build() = inst
    }
}
```

### apply

- 优点：省去了Builder类
- 缺点：字段不够直观，只能在kotlin中使用

```kotlin
val bean = Bean().apply {
    a = 0
}
```

### [DSL写法](<https://kotlinlang.org/docs/reference/type-safe-builders.html)

* 优点：实现了类似apply的功能，java和kotlin都可以调用
* 缺点：字段不够直观

```kotlin
class Bean(
    var a: Int = 0
) {
    companion object {
    	@JvmStatic
        fun build(init: Bean.() -> Unit): Bean {
            val inst = Bean()
            inst.init()
            return inst
        } 
    }
}
```

Kotlin调用

```kotlin
val bean = Bean.build {
    a = 0
}
```

Java调用

```java
// 非lambda
Bean bean = Bean.build(new Function1<Bean, Unit>() {
    @Override
    public Unit invoke(Bean bean) {
        bean.setA(0);
        return null;
    }
});

// lambda
Bean bean = Bean.build(bean1 -> {
    bean1.setA(0);
    return null;
});
```



