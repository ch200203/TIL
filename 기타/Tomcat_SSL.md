## Tomcat 8.5 SSL 인증서 연결하기




#### 1.SSL 인증서 발급

1) *.crt 파일인 경우  

	*.crt은 바로 적용이 안될수도 있음.  
	따라서, crt 파일을 => *.jks 파일로 변환 후 작업해야함  
	과정은 crt파일 => prx파일 => jks 파일로 다시 변환 하는 과정이 필요
	

2) *.crt -> *.prx

	```bash
		openssl pkcs12 -inkey 파일명.key -in 파일명.crt -certfile 어쩌고_CA.crt -export -out 도메인이름.pfx
	```
	
	*.key 파일, 도메인명.crt 파일, ROOTCA.crt 파일이 존재하는데 도메인명.crt 파일만 변환하면 됨.
	
3) *.prx -> *.jks

	```bash
		keytool -importkeystore -srckeystore test.test.co.kr.pfx -srcstoretype pkcs12 -destkeystore test.test.co.kr.jks -deststoretype jks
	```
	
#### 2. Tomcat에 설정
		
1) server.xml 파일 수정
	
	- redirectPort를 변경 (기본값 443)
	```xml
		<Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="443" />
	```
	
	- SSL 설정을 주석해제  
	- Connector port와 redirectPort를 맞춰줌
	- certificateKeystoreFile과 certificateKeystorePassword 설정
	
	```xml
		<Connector port="443" protocol="org.apache.coyote.http11.Http11NioProtocol"
				   maxThreads="200" SSLEnabled="true">
		   <SSLHostConfig>
				<Certificate certificateKeystoreFile="/~~~경로/ssl/도메인명.jks"
							certificateKeystorePassword="jks 생성할때 설정한 password"
							 type="RSA" />
			</SSLHostConfig>
		</Connector>
	```
	
	- 서버 재시작 후 https로 진입.
	
	
#### 3. 라우팅 작업

1) 80 port 와 443 port에서 8080, 8443으로 접속되게 필요한 경우.
	
	```bash
		sudo iptables -A PREROUTING-t nat -i etho0 -p tcp -dprot 80 -j REDIRECT -to-port 8080
		sudo iptables -A PREROUTING-t nat -i etho0 -p tcp -dprot 443 -j REDIRECT -to-port 8443
	```

#### 4.http -> https 변환

1) web.xml에 해당 내용 추가

	```xml
		<security-constraint>    
			<web-resource-collection>        
				<web-resource-name>SSL Forward</web-resource-name>
				<url-pattern>/*</url-pattern>
			</web-resource-collection>
			<user-data-constraint>        
				<transport-guarantee>CONFIDENTIAL</transport-guarantee>  
			</user-data-constraint>
		</security-constraint>
	```

2) 특정 resource는 https http 모두 처리 가능
	
- `/image/*, /css/*` 리소스는 http https 모두에서 처리

	```xml
		<security-constraint>    
			<web-resource-collection>        
				<web-resource-name>HTTPS or HTTP</web-resource-name>        
				<url-pattern>/images/*</url-pattern>       
				<url-pattern>/css/*</url-pattern>
			</web-resource-collection>
			<user-data-constraint>        
				<transport-guarantee>NONE</transport-guarantee>  
			</user-data-constraint>
		</security-constraint>
	```
	


#### 5. 참고
[참고링크1](https://offbyone.tistory.com/262 )  
[참고링크2](https://offbyone.tistory.com/274 )   
[참고링크3](https://ddil-ddil.tistory.com/48 )

