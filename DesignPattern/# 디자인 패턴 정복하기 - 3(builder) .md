# 디자인 패턴 정복하기 - 3

## 빌더 패턴 Builder Pattern

- 빌더 패턴은 디자인 패턴 중 하나로 **생성(Creational)** 패턴 중 하나 입니다.
- 빌더 패턴이란 복잡합한 객체를 생성하는 방법 중 하나로, 객체의 생성 코드와 객체의 사용코드를 분리하여 코드의 가독성과 유지 보수성을 향상시키는 패턴입니다.

## 빌더 패턴이 필요한 이유

### 문제(Problem)

빌더 패턴은 많은 Optional한 멤버 변수나 파라미터 혹은 지속성 없는 상태 값들에 대해 처리해야 하는 문제를 해결합니다.

객체를 생성할 때, 필요한 매개변수를 모두 포함하는 생성자를 사용하게 되는데, 이 생성자의 매개변수가 많아질 경우 문제가 발생합니다. 
이를 `telescoping constructor pattern` 이라고도 합니다. 
이렇게 되면 매개변수의 순서를 혼동하기 쉽고, 일부 매개변수에 대해 null 값 또는 기본값을 할당해야 할 때, 그 의미를 파악하기 어렵게 만들 수 있습니다.


빌더 패턴은 객체의 불변성을 유지하는 데 도움이 됩니다.
객체가 생성되고 나면 그 상태를 변경할 수 없게 만들면, 다중 스레드 환경에서 해당 객체를 안전하게 사용할 수 있습니다.


### 해결책(Solution)

Builder 패턴은 이러한 문제들을 해결하기 위해 별도의 Builder 클래스를 만들어 필수 값에 대해서는 생성자를 사용하고,
선택적인 값들에 대해서는 메소드를 통해 필요한 값들을 입력받고 `build()` 메서드를 사용해 인스턴스를 리턴 받는 방식 입니다.

### 결과(Consequence)

- **장점**
    - 생성할 객체의 속성을 자유롭게 지정할 수 있습니다.
        - 빌더 패턴은 유연성을 제공하며, 필수적인 속성만 생성자에서 처리하고 선택적인 속성은 메서드 체이닝을 통해 처리할 수 있습니다.
    - 생성할 객체의 속성이 많거나 복잡한 경우에도 코드의 가독성을 유지할 수 있습니다.
    - 빌더는 재사용이 가능합니다.

- **단점**
    - 코드량이 많아질 수 있습니다. 각 속성에 대한 setter 메서드를 작성해야 하므로, 클래스의 코드량이 증가합니다.
    - 디자인 패턴이므로 사용할 때는 주의해야 합니다. 무조건적으로 모든 곳에 빌더 패턴을 적용하는 것은 적절하지 않을 수 있습니다.

## 빌더 패턴의 구현 방법
- 빌더 클래스를 선언하고, 생성할 객체의 속성에 대한 setter 메서드를 구현합니다.
- 이 메서드들은 빌더 객체 자신을 반환하므로 메서드 체이닝이 가능합니다. 또한 build 메서드를 통해 최종적인 객체를 생성합니다.

```java
public class User {
    private String name;
    private int age;
    
    public static class Builder {
        private String name;
        private int age;
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withAge(int age) {
            this.age = age;
            return this;
        }
        
        public User build() {
            User user = new User();
            user.name = this.name;
            user.age = this.age;
            return user;
        }
    }
    
    public static void main(String[] args) {
        User user = new User.Builder().withName("Henry").withAge(30).build();
    }
}
```
 - Lombok 라이브러리의 @Builder 어노테이션을 사용하여 빌더 패턴을 간단하게 구현할 수 있습니다.

```java
@Builder
@Getter
@Setter
public class User {
    private String name;
    private int age;
}

public static void main(String[] args) {
    User user = User.builder().name("Henry").age(30).build();
}
```

> 이렇게 복잡한 객체의 생성 과정을 단계별로 나누어 클라이언트가 이해하기 쉽도록 인터페이스를 제공하는 것이 빌더 패턴의 핵심입니다.
> 특히 복잡한 객체를 생성해야하는 상황에서 유용하며, DTO와 같은 복잡한 객체 생성에 사용하면 많은 이점을 제공합니다.