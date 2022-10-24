## Properties 파일 분리하여 개발환경 세팅하기

고객사 별로 커스터마이징 된 설정에 따라서 서버를 배포하는 환경에서 문제가 있었습니다.  
서비스 초기에는 많은 수정이있어 배포를 계속 새로해주어야 했는데 그때 마다 **application.properties**  
파일을 수정하고 반복적으로 build를 새로해야 하여 해당 문제를 개선하는 작업을 진행하였습니다.
 

 
### Property를 Spring이 읽는 과정

**Property** 파일을 Spring Boot가 찾는 방법의 순서

* 총 11가지가 있으나 기본적인 일부만 소개하자면...
(우선순위와는 별개)

|방법|내용|
|---|---|
|1. Termial에서의 명령어| --spring.properties.active== real	|
|2. Java System 속성 | System.getProperties()			   	|
|3. 배포 파일과 같은 경로에 존재하는 properties 파일| properties, yml 파일|
|4. 배포 파일과 함께 패키징 된 properties 파일| src/main/resource 경로에 위한 application.properties or yml|


- 해당내용의 자세한 방법은 공식문서를 참고.  
[Spring 공식문서](https://docs.spring.io/spring-boot/docs/1.2.3.RELEASE/reference/html/boot-features-external-config.html)


이 중 3번을 채택하여 사용하기로 하였습니다.  
공식문서의 **23.4**의 내용입니다.

 
파일 외에도 ***application.properties*** 이름 지정 규측을 이용하여 프로필별 속성을 정의할 수 있습니다.  
예) ***application-{profile}.properties***.  
프로필 특정 속성은 standard와 동일한 위치에서 로드되며 ***application.properties*** 프로필 특정 파일이  
외부에 있는지 여부에 관계없이 항상 기본파일을 재정의 하게됩니다.

### 스캔범위 지정하기

Spring Boot는 응용프로그램이 시작될 때 다음의 위치에서 ***application.properties or yml*** 파일을  
자동을 찾아 로드하게 됩니다.

1. classpath:/
2. classpath/config/
3. file:./
4. file:./config

해당 경로가 기본 경로이며, 필요한 경우 경로를 직접 정의도 가능합니다.

***spring.config.location***에 별도로 설정 파일 위치가 명시되면 기본값 위치를 대체합니다.
```
	-Dspring.config.location=file:/home/config
```


### Properties 파일 관리하기

먼저 프로필별 속성을 정의하여 properties 파일을 구성합니다.

예)
```
- application.properties(yml)
- application-dev.properties(yml)
- application-local.properties(yml)
- application-{serverName}.properties(yml)
```

공통된 환경은 **application.properties**에 하나의 파일로 관리합니다.  
profile별 종속정인 정보들은 ```dev, local, serverName``` 별로 따로 분리하여 관리합니다.

해당 파일들은 ```classpath/config/``` 에 위치시켜 관리합니다.

### 사용방법

- ***application.properties***
```properties
spring.profiles.active=dev
```

- ***application-dev.properties***
```
spring.profiles=dev
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/hnt?zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.datasource.username=dev
spring.datasource.password=dev1234
test.username=devUser
```

- ***application-release.properties***
```
spring.profiles=release
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://releaseURL:3306/hnt?zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.datasource.username=release
spring.datasource.password=release1234
test.username=releaseUser
```

리소스를 다음과 같이 분리하여 처리합니다

```java
@SpringBootApplication
@EnableAutoConfiguration
public class DemoApplication {
    @Autowired
    private static Environment environment;

    @Autowired
    public DemoApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoApplication.class, args);

        DemoApplication da = new DemoApplication(environment);
        da.contextLoads();
    }

    public void contextLoads() throws Exception {
        System.out.println("DemoApplication 실행");
        System.out.println("profile 값 :: " + environment.getProperty("spring.profiles.active"));

        String username = environment.getProperty("spring.test.username");
        System.out.println("USERNAME :: " + username);
    }

}
```

콘솔 확인값

- active=dev
```
DemoApplication 실행
profile 값 :: dev
USERNAME :: devUser
```

- active=release
```
DemoApplication 실행
profile 값 :: release
USERNAME :: releaseUser
```


### 실행

1. jar 파일인경우
마지막으로 프로그램 실행 시 ```-Dspring.profiles.active={active profile}``` 옵션으로 Profile 정보를 전달해 주면 ***application.properties*** 와 ***application-{active profile}***이 구성됩니다.

```shell
java -jar SampleApp -Dspring.profiles.active=dev
```

2. .war파일을 Tomcat으로 올리는 경우
Tomcat의 실행경로 ```{Tomcat_dir}/bin```안에 ```setevn.sh``` 파일을 생성한다.

- setevn.sh
```shell
export JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active={active profile}"
```

해당 내용 저장 후 chmod를 사용하여 실행가능 권한을 줘야한다
```$ chmod +x setevn.sh```

이렇게 설정하게 되면
	startup.sh => catalina.sh => setenv.sh
해당 파일을 참조하며 실행하게 된다.



### 참고
[참고1](https://devsquare.tistory.com/34)
[참고2](https://m.blog.naver.com/qhdqhdekd261/221837351506)
[참고3](https://deveely-log.netlify.app/2020-03-21-spring-boot-profiles/)
[참고4](https://kim-jong-hyun.tistory.com/40)










