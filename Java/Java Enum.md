# Java Enum 정리하기



### Enum 이란?
- Enum type : 변수가 일련된 static final로 정의 될 수 있도록 하는 데이터 타입
- Enum은 Eumeration 열거형이라고 불리며, 서로 관련이 있는 상수들의 집합
- static final String, int 와 같이 기본 자료형의 값을 고정 할 수 있음.



### Enum의 특징

1) 클래스를 상수처럼 사용 가능함.

```java
enum Student {
    CHEOLSU("SEOUL", "COMPUTER_SCIENCE"),
    YURI("KOREA", "BUSINESS"),
    HOON("YEONSEI". "KOREAN");

    private final String UNIVERSITY;
    private final String MAJOR;
    private final STRING STUDENT_NUMBER;

    // public 사용시 컴파일 에러 발생
    Student(String university, String major) {
        this.UNIVERSITY = university;
        this.major = major;
    }
}
```
- 생성자는 ```Public``` 으로 사용 시 컴파일 에러 발생 => 반드시 private로 생성해야 함.
- Enum은 고정된 상수의 집합으로, 런타임이 아닌 컴파일 타임에 모든 값에 대해 알아야 합니다. 즉, 다른 패키지나 클래스에서 Enum 타입에 접근해서 동적으로 어떤 값을 지정 할 수 없습니다.


2) Enum 클래스를 구현하는 경우 상수 값과 같이 유일하게 하나의 인스턴스가 생성되어 사용된다.
```java
enum Student {
    CHEOLSU("SEOUL", "COMPUTER_SCIENCE"),
    YURI("KOREA", "BUSINESS"),
    HOON("YEONSEI". "KOREAN");

    private final String UNIVERSITY;
    private final String MAJOR;
    private final STRING STUDENT_NUMBER;

    // public 사용시 컴파일 에러 발생
    Student(String university, String major) {
        this.UNIVERSITY = university;
        this.major = major;
    }
    
    public void plusnumber() {
        STUDENT_NUMBER++;
    }
}
```
- STUDENT_NUMBER 변수는 CHEOLSU 라는 인스턴스에서 plusnumber 메소드를 통해 변경 할 수 있음.
- 그러나, 여러 쓰레드엣 Student.CHEOLSU 상수를 사용할 경우 number 변수는 공유 되기 때문에 주의 하여야 한다.

3) 인스턴스 생성과 상속을 방지하여 상수값의 Type 안정성의 보장
```java
public class EnumExample {
    public static void main(String[] args) {
        // java.lang.enum의 toString 메소드 상속
        System.out.println(Student.CHEOLSU.toString());
    }
}
```
- Enum의 클래스 생성자를 private로 생성하였기 때문에 다른 클래스 or 패키지에서 동적으로 값을 할당 할 수 없어 안정성이 보장이 된다.
- 즉, 계승(상속)이 불가능하다.

이 밖에도
4) Object를 계승받아 Objecㅅ에서 제공하는 메소드를 활용 할 수 있으며, 디폴트 메소드를 사용 가능하다.  
5) Serializable, Compareable이 가능하다.  
6) 비교연산은 Int 상수와 성능이 비슷하다.  
7) 그룹핑이 가능하다(Enum + Enum 조합가능)  

등이 있다.


--- 
### 참고
[기본 활용, Spring, MyBatis 적용](https://ehdvudee.tistory.com/33)  
[JavaEnum 활용경험](https://techblog.woowahan.com/2527/)  
[Enum 기본 메소드](https://math-coding.tistory.com/179)
