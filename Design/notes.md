# 设计模式原则

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

