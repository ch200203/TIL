## CentOS7 서버 JDK, Tomcat 설정


### 1)	JDK 설치
#### 1. Oracle JDK 설치의 경우

- Oracle 홈페이에서 jdk 설치파일 다운로드  
- /usr/local/java/jdk1.8.0 => 압축 해제 (tar.gz => $ tar -xvf 파일명)
- 환경변수 확인 `echo $JAVA_HOME`  
	=> 아무것도 출력되지 않았다면  
- /etc/profile 파일 맨 하단에 다음내용 추가  
	```
		export JAVA_HOME=/usr/local/java/jdk1.8.0
		export PATH=$PATH:$JAVA_HOME/bin
		export CLASSPATH=.:$JAVA_HOME/lib/tools.jar
	```	
***	
		
#### 2. Open JDK 설치의 경우 (yum을 이용한 설치)
	
- `javac -version` 자바 설치가 안된걸 확인하기
- 설치가능 open jdk 확인 `yum list java*jdk_devel`
- 설치 `yum install java-1.8.0-openjdk-devel.x86_64`
- `readlink -f /usr/bin/javac` JDK 설치경로 확인
- /etc/profile 파일 맨 하단에 다음내용 추가  
	```
		export JAVA_HOME=/usr/lib/jvm/java-1.8.0-open-... (확인한 경로)
		export PATH=$PATH:$JAVA_HOME/bin
		export CLASSPATH=.:$JAVA_HOME/lib/tools.jar
	```
		
		
### 2) Tomcat 설치의 경우
- Tomcat 홈페이지에서 필요한버전 다운로드
- usr/local/tomcat => 압축해제
- tomcat 실행하여 => jdk 위치 잘 잡았는지 확인하기
- 방화벽이 설정되어 있는 경우
	방화벽에 8080 포트 추가  
	`$ sudo firewall-cmd --zone=public --permanent --add-port=8080/tcp`  
	
	방화벽 재시작
	`$ firewall-cm --reload`
	
- **접속하여 확인하기**

 
