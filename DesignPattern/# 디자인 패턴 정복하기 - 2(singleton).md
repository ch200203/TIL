# 디자인 패턴 정복하기 - 2 

## 싱글톤 패턴 Singleton Pattern

- 싱글톤 패턴은 디자인 패턴 중 하나로 **생성(Creational)** 패턴 중 하나 입니다.
- 싱글톤 패턴은 어플리케이션에서 인스턴스가 하나만 존재하도록 보장하고, 이 인스턴스에 대해서 어디서든 접근할 수 있게 제공합니다.

## 싱글톤 패턴이 필요한 이유

### 문제(Problem)

어떤 클래스에 대해 오직 하나의 인스턴스만 생성하고, 이 인스턴스에 대한 전역적인 접근이 필요할 때, 또는 인스턴스가 여러 개 생성되면 예기치 않은 문제가 발생하는 경우 사용합니다.

예를 들어, 다음과 같은 상황에서 싱글톤 패턴을 사용할 수 있습니다.

- 데이터베이스 연결 객체
- 로그 출력 객체

### 해결책(Solution)

- 생성자를 private으로 선언하여 외부에서 직접적인 인스턴스 생성을 방지합니다.
- 대신 해당 클래스 내부에서 인스턴스를 생성하고, 이를 반환하는 static 메서드를 구현합니다.
- 이렇게 하면 클래스 내에서 유일한 인스턴스를 생성하고, 이를 외부에서 호출할 수 있습니다.
- 생성된 인스턴스는 전역에서 유일하게 사용되어야 합니다.

### 결과(Consequence)

- **장점**
    - 싱글톤 패턴을 사용하면 클래스의 인스턴스를 하나만 유지하므로 **메모리 사용을 줄일 수 있습니다.** 
        - 싱글톤 패턴을 사용하면 객체를 매번 생성할 필요가 없기 때문에, 객체 생성과 소멸에 대한 비용을 절감할 수 있습니다. 
        - GC(Garbage Collection)에 의한 오버헤드를 줄일 수 있습니다.
    - 리소스의 공유가 쉽습니다.
        - 싱글톤 패턴을 사용하면 전역적으로 하나의 인스턴스를 공유하기 때문에, 리소스를 공유하여 사용할 수 있습니다.
        - 예) 데이터베이스 연결, 파일 입출력...
    - 상태 유지가 용이합니다.
        - 하나의 인스턴스에서 유지되는 상태를 다른 객체에서 공유하여 사용할 수 있습니다. 
- **단점**
    - 단위 테스트를 구현하기 어렵습니다.
        - 단위 테스트는 서로 독립적이여야 하며, 어떤 순서로든 테스트가 가능 해야합니다.
        - 싱글톤 패턴은 미리 생성된 하나의 인스턴스를 기반으로 구현하기 때문에, 각 테스트 별로 '**독립적인**' 인스턴스를 구현하기 어렵습니다.
    - 싱글톤 패턴은 전역적인 접근점을 제공하기 때문에, 객체간의 결합도가 높아지는 문제가 발생합니다.
    - 싱글톤 패턴을 구현하기 위해서는 코드 자체가 많이 필요합니다. 따라서 코드가 복잡해질 수 있습니다.
    - 싱글톤 패턴은 다중 스레드 환경에서는 안정성 문제가 발생할 수 있습니다. 따라서, **Thread Safe**한 구현방법이 필요합니다.

        > 싱글톤 패턴에서는 오직 하나의 인스턴스만 생성되도록 보장하는데, 다중 스레드 환경에서 동시에 접근하면서 인스턴스 생성을 시도할 경우, 둘 이상의 스레드에서 동시에 인스턴스를 생성하는 문제가 발생할 수 있습니다. 이러한 경우, 둘 이상의 인스턴스가 생성될 가능성이 있으며, 이는 싱글톤 패턴의 목적과는 상반되는 결과를 가져올 수 있습니다.
        
        > 예를 들어, 스레드 A와 스레드 B가 동시에 인스턴스를 생성하려는 경우, 둘 다 아직 인스턴스가 생성되지 않았다고 판단하고 새로운 인스턴스를 생성할 수 있습니다. 따라서, 다중 스레드 환경에서 싱글톤 패턴을 사용할 경우, 인스턴스 생성 시점에 대한 **동기화 처리**가 필요합니다.

## 싱글톤 패턴의 구현 방법

1. 생성자를 private로 선언하여 외부에서 인스턴스를 생성할 수 없도록 합니다.
2. 클래스 내부에 private static 변수를 선언 합니다.
3. `getInstance` 메서드를 구현하여 인스턴스를 반환합니다.
이 때, 인스턴스가 생성되어 있는 경우에는 해당 인스턴스를 반환하고, 생성되어 있지 않은 경우에는 인스턴스를 생성하여 반환합니다.

```java
public class Singleton {
    private static Singleton instance = null;
    public String field = "Some value";
    
    private Singleton() {
    }
    
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
    
    public static void main(String[] args) {
        Singleton singleton1 = Singleton.getInstance();
        Singleton singleton2 = Singleton.getInstance();

        System.out.println("singleton1 = " + singleton1);
        System.out.println("singleton2 = " + singleton2);
        
        System.out.println("Are the two instances the same object? " 
                + (singleton1 == singleton2));
    }
}
```
- result

    ![이미지](../%EA%B8%B0%ED%83%80/img/resultSingleton.png)

--- 

## 참고문헌
- https://tecoble.techcourse.co.kr/post/2020-11-07-singleton/
- https://gmlwjd9405.github.io/2018/07/06/singleton-pattern.html
- https://bgasparotto.com/design-patterns/singleton