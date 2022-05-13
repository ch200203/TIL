### Java Stream 정리

[참고문서] (https://futurecreator.github.io/2018/08/26/java-8-streams/)

***

#### 스트림 Streams

Java8에서 추가한 스트림(Streams)는 람다를 활용한 기술 중 하나.  
Java8 이전에는 배열 혹은 컬렉션 인스턴스를 제어하기 위하여 **for / foreach** 문을 사용.  
간단한 경우는 상관 없으니 로직이 복잡해질 수록 => 루프제어가 힘듬.

스트림은 => 말그대로 '데이터의 흐름', 배열 또는 컬렉션에 함수 여러개를 조합하여 원하는 결과를 필터링하고 
가공된 결과를 얻을 수 있음.  
또한 람다를  이용하여 코드의 양을 줄이고 간결하게 표현 가능  
즉, Java8부터 스트림을 이용하여 배열과 컬렉션을 함수형으로 처리할 수 있음.

장점으로는 간단하게 병렬처리 가능 => 하나의 작업을 둘 이상의 작업으로 잘게 나눠서 처리할 수 있음.
따라서, 쓰레드를 이용하여 많은 요소들을 빠르게 처리할 수 있음.

Stream에 대한 내용은 크게 세가지로 분류가능

1. 생성하기 : 스트림 인스턴스 생성  
2. 가공하기 : 필터링(filtering) 및 맵핑(mapping) 등 원하는 결과를 만들어 가는 중간 작업  
3. 결과 만들기 : 최종적으로 원하는 결과를 만들어 내는 작업  
	

		전체 -> 맵핑 -> 필터링_1 -> 필터링_2 -> 결과만들기 -> 결과물


***

#### 생성하기
보통은 배열, 컬렉션으로 스트림을 생성함

#### 배열 스트림

스트림을 이용하기 위해서는 생성이 필요, 스트림은 배열 또는 컬렉션 인스턴스를 통해서 생성
`Arrays.stream` 메소드 사용


```java
String[] arr = {"a", "b", "c"};
Stream<String> stream = Arrays.stream(arr);
Stream<String> streamOfArrayPart = 
	Arrays.stream(arr, 1, 3); // 1-2요소 [b, c]
	
// 출력하기
stream.forEach(a -> System.out.print(a + ", ");
```

#### 컬렉션 스트림

컬렉션 타입(Collection, List, Set)의 경우 인터페이스에 추가된 디폴트 메소드 `stream`을 이용해서 스트림을 생성 가능

```java
	public interface Collection<E> extends Iterable<E> {
		default Stream<E> stream(){
			return StreamSupport.stream(spliterator(), false);
		}
	}
```

다음과 같이 생성하여 활용가능

```java
	List<String> list = Arrays.asList("a", "b", "c");
	Stream<String> stream = list.stream();
	Stream<String> paralleStream = list.parallelStream(); // 병렬처리 스트림.
```


#### 비어있는 스트림

빈 스트림은 요소가 없을 때, `null` 대신 사용 가능

```java
	public Stream<String> streamOf(List<String> list) {
	  return list == null || list.isEmpty()  
		? Stream.empty() 
		: list.stream();
	}
```

#### Stream.builder()

Builder 메소드에 `add()`를 이용하여 직접적으로 원하는 값을 넣을 수 있음.  
마지막에 `build()` 메소드로 스트림을 리턴.

```java
	Stream<String> builderStream = Stream<String>builder
		.add("One").add("Two).add("Three")		
		.build(); // [One, Two, Three]
```

#### Stream.generate()

`generate()` 메소드를 이용하면 `Supplier<T>`에 해당하는 람다로 값을 넣을 수 있음.  
`Supplier<T>`는 함수형 인터페이스 => 람다에서의 리턴값이 들어감

```java
	public static<T> Stream<T> generate(Supplier<T> s) {...}
```

이 때 생성되는 스트림은 크기가 무한하기 때문에 특정사이즈로 최대 크기를 제한해야함.

```java
	Stream<String> generateStream = 
		Stream.generate(() -> "gen").limit(5); // [gen, gen, gen, gen, gen]
```


#### Stream.iterate()

`iterate` 메소드 이용시 초기값과 해당 값을 다루는 람다를 이용해 스트림에 들어갈 요소를 생성  
즉, 요소가 다음 값의 Input으로 들어간다. 이 때 생성되는 스트림도 크기가 무한하여 크기를 제한하여야 한다.

```java
	Stream<String> iteratedStream = Stream.iterate(30, n -> n+2).limit(5);
	iteratedStream.forEach(a -> System.out.print(a + ", ");
		// ['30', '32', '34', '36', '38']
```


#### 기본 타입형 스트림

제네릭을 사용하여 리스트나 배열을 이용하여 기본타입(int, long, double) 스트림을 생성 가능.  
뿐만 아니라, 제네릭을 사용하지 않고 직접적으로 해당 타입의 스트림 생성이 가능.  
`range` 와 `rangeClosed` 는 범위의 차이 => 두 번째 인자인 종료지점이 포함되느냐 안되느냐의 차이(1 ~ 4 / 1 ~ 5)

```java
	IntStream intStream = IntStream.range(1, 5); // [1, 2, 3, 4]
	LongStream longStream = LongStream.rangeClosed(1, 5); // [1, 2, 3, 4, 5]
```

제네릭을 사용하지 않아 autoBoxing이 일어나지 않음 => 필요한 경우 `boxed` 메소드 사용.

```java
	Stream<Integer> boxedIntStream = IntStream.range(1, 5).boxed());
```

`Random` 클래스는 난수를 가지고 IntStream, LongStream, DoubleStream 을 생성가능  
```java
	DoubleStream doubleStream = new Random().doubles(3); // 난수 3개 생성
```

#### 파일스트림

자바 NIO 의 `Files` 클래스의 `lines` 메소드는 해당 파일의 각 라인을 스트링 타입의 스트림으로 생성.
```java
	Stream<String> lineStream = Files.lines(Path.get("파일명.txt"), Charset.forName("UTF-8"));
```

#### 병렬스트림

스트림 생성 시 사용하는 `stream` 대신 `parallelStream` 메소드를 사용하여 병렬 스트림을 생성

```java
	// 병렬스트림 생성
	Stream<Product> parallelStream = productList.parallelStream();
	// 병렬 여부 확인
	boolean isParallel = parallelStream.isParallel();
	
	// 쓰레드를 이용해 병렬처리
	boolean isMany = parallelStream
	  .map(product -> product.getAmount() * 10)
	  .anyMatch(amount -> amount > 200);
	 
	// 배열을 이용하여 병렬스트림을 생성
	Arrays.stream(arr).parallel();
	
	// 컬렉션과 배열이 아닌 경우는 다음과 같이 parallel 메소드를 이용해서 처리
	IntStream intStream = IntStream.range(1, 150).parallel();
	boolean isParallel = intStream.isParallel();
	
	// 다시 시퀀셜(sequential) 모드로 돌리고 싶다면 다음처럼 sequential 메소드를 사용
	// 반드시 병렬 스트림이 좋은 것은 아닙니다.
	IntStream intStream = intStream.sequential();
	boolean isParallel = intStream.isParallel();
```




