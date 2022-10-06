## CentOS7 Tomcat Service 등록



#### 1. tomcat.service 파일 생성

`$ /etc/systemd/system` 위치에 tomcat.service 파일을 생성

```bash
[Unit]
Description=tomcat 9
After=network.target syslog.target

[Service]
Type=forking
#Environment="/usr/local/tomcat"
Environment=JAVA_HOME=/usr/local/jdk
User=ippbx
Group=ippbx
ExecStart=/usr/local/tomcat/bin/startup.sh
ExecStop=/usr/local/tomcat/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
```

#### 2. tomcat 자동시작으로 등록

`$ systemctl enable tomcat.service`


#### 3. tomcat service 명령어

```bash
$ systemctl start tomcat.service	//실행
$ systemctl stop tomcat.service		//종료
$ systemctl enable tomcat.service	//활성화
$ systemctl disable tomcat.service	//비활성화
$ systemctl status tomcat.service	// 시스템 상태 확인
```

- tomcat.service 파일 변경 후 systemctl 데몬 reload
`$ sudo systemctl daemon-reload` 