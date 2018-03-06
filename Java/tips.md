# Tips

[TOC]

# Set classpath in shell

```shell
# Windows
SET CLASSPATH="xxx"
# Linux/Unix
export CLASSPATH="xxx"
```

run a Java file with `-classpath`

# Goto in Java

In c or c++, we have `goto` to jump into other code block

```c
mark: printf("goto mark");
goto mark;
```

In java, it can be implemented like this

```java
mark: {
  
}

// in some loops
for (int num: nums) {
  break mark;
}
```

# Different bases expression of a int num

| Base | Expression |
| ---- | ---------- |
| 2    | 1010       |
| 8    | 012        |
| 10   | 10         |
| 16   | 0xa        |

# Strict Computing

In default, Java only reserves the frontier 64 places of a double number to avoid integer overflow in Intel 64.

However if we need to get the accurate result of a double computing, we need to declare a function with `strictfp`, like ```public static stricfp void main(String[] args)```

StrictMath is more strict than Math

# StringBuilder vs StringBuffer

StringBuilder is used in single-thread case with higher efficiency

StringBuffer is used in multi-thread case with lower efficiency

# Access private in a class methods

We all know that a class method can access `private` member in its instance. In fact, it can access all instances of this class.

For example

```java
class Employee {
  private name;
  public boolean equals(Employee other) {
    return this.name.equals(other.name);
  }
}
```

If we call it like ```if (harry.equals(boss))```, `equals` will not only access `name` in `harry`, but also `name` in `boss`

# Initialization Block

Initialization block will be run before the class contructor

```java
class Employee {
  private static int nextId;
  private int id;
  
  {
  	id = nextId;
    nextId++;
  }
}
```

`static` block will only run once. For example, in an Android activity to load a native lib

```java
class Activity {
  static {
    System.loadLibrary("hello");
  }
}
```

# Javadoc usage

```java
/**
 * @param parameter explanation
 * @return result explanation
 * @throws exception explanation
 */
```

# Anonymous List

In general, we create and initialize an ArrayList like this

```java
ArrayList<String> friends = new ArrayList<>();
friends.add("Harry");
friends.add("Tom");
```

If we use anonymous list with "{}", it will seems shorter and clear.

```java
ArrayList<String> friends = new ArrayList<>(){{
  add("Harry");
  add("Tom");
}};
```

# try-catch with resources

If a resource implement `AutoCloseable`, we can use try-with-resources format

```java
public implement AutoCloseable {
  void close throws Exception;
}

try (Resource res = ...) {
  
}
```

`res` will be automatically closed after exit `try` block or `catch` block, which seems like a `finally` block works. It works like:

```java
Resource res = ...;
try {
  
} finally {
  res.close();
}
```

`res` can be File, Scanner, PrintWriter or something similar.

# General debugging tips

1. `System.out.println()` or `Logger.getGlobal().info()` to print something implemented `toString()`

2. Use `main` in every public class to run some unit test cases

3. Use JUnit framework for unit tests

4. logging proxy

5. `printStackTrace()` or `Thread.dumpStack()`

   Show stack trace:

    ```java
   ByteArrayOutputStream out = new ByteArrayOutputStream();
   new Throwable().printStackTrace(out);
   String description = out.toString();
    ```

6. Output errors to file

   Linux: ```java MyProgram 2> errors.txt```

   Windows: ```java MyProgram >& errors.txt```

## Add or Remove in iterator

For example, we have an ArrayList\<Integer\> `list`: [1, 2, 3, 4, 5]

```java
Iterator iter = list.iterator();
Integer first = iter.next(); // 1
Integer second = iter.next(); // 2
iter.remove(); // [1, 3, 4, 5], remove last visited element
Integer third = iter.next(); // 4
iter.add(new Integer(2)); // [1, 3, 2, 4, 5], add the new element exactly before last visited element
Integer fourth = iter.next(); // 5
```

## Others

1. Java is case sensitive
2. Java has no `unsigned`
3. `x == Double.NaN` is never true. Use `Double.isNaN(x)` instead. Also work for `Double.POSITIVE_INFINITY` and `Double.NEGATIVE_INFINITY`
4. Java reserves `const` with no use
5. Java has `>>>` without `<<<`
6. Vector is for multi-thread with low efficiency while ArrayList is for single-thread with high efficiency.