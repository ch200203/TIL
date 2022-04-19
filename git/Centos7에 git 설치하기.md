## CentOS7 git 설치과정




### 1. git 확인 및 2.x 버전 설치

- CentOS7에는 git 1.8 버전이 자동으로 설치 되어있음

```shell
	# git --version
	# git version 1.8.3.1
```

- 최신버전의 git 저장소 시스템에 추가 후 yum을 이용하여 업데이트

```shell
	# rpm -Uvh http://opensource.wandisco.com/centos/7/git/x86_64/wandisco-git-release-7-2.noarch.rpm
	# yum update git
```

- git 버전 확인

```
	# git --version
	# git version 2.31.1
```


### 2. git Repository 생성 및 ssh 서버 키 등록

- repository로 사용할 폴더 생성 및 해당 디렉토리 git init

```
	# mkdir -p /home/git/repo/git/project.git
	# git init --bare /home/gitrepo/git/project.git
```


- git 계정 생성 및 디렉토리 소유자를 git으로 변경

```
	# useradd git
	# echo 'git:사용할비밀번호' | chpasswd
	# chown -R git:git /gitrepo/git/
```

- git 클라이언트에서 ssh 키 있는지 확인

``` 
	# ll ~/.ssh/id_rsa*
```

- 없다면 로컬에서 생성 후 id_rsa_pub(공개키)파일을 원격저장소 ssh 폴더에 넣어줌


```
	# 로컬 저장소에서 keygen으로 키 생성
	# ssh-keygen
	
	# git 서버(원격저장소)에 생성된 키 저장
```

- 서버측에서 git 계정을  git-shell로 변경 => git 사용자가 리눅스 shell 접근을 막기위함(보안상)
	- 이것을 적용하면 bash 쉘을 사용 할 수 없으며, 클라이언트 키를 추가 등록하는 것도 수행할 수 없다.

	- 클라이언트 키를 추가로 등록하려면 [chsh git -s /bin/bash]를 임시로 적용했다가 등록 후 다시 이것을 적용하도록 하자.


```
	# which git-shell
	# chsh git -s /usr/bin/git-shell
	# cat /etc/passwd | grep git
	=> git:x:1000:1000::/home/git:/usr/bin/git-shell
```

***

### 3. 로컬에서 설정할 것들

- 버전을 관리할 소스파일들의 상위 경로로 이동하여 초기화(git init)하고, 사본을 추가한다.
```
	$ cd workspace_주소
	$ git init
```

- git config에 이름과 이메일 추가
```
	$ git config --global user.name "<Name>"
	$ git config --global user.email "<Email_addr>"
```

- git에 사본추가 및 원격저장소 추가
	- project라는 이름을 가진 프로젝트를 사본을 떠옴
	- project라는 이름을 가진 프로젝트를 원격저장소 주소지에 저장
	- git pull : 저장소에 추가한 master 프로젝트를 가져와서 로컬프로젝트와 병합
	- git fetch : 원격저장소에 있는 프로젝트의 변경사항을 로컬로 가져오기만 하고 병합은하지 않음
	- git clone : 원격저장소의 내용을 그대로 복사
	
```
	$ git clone ssh://계정주소@ip주소:포트번호/깃 프로젝트 폴더 주소
	$ git clone ssh://git@19.19.20.212:22/home/git/repo/project.git
	
	$ git remote add origin ssh://git@19.19.20.212:22/home/git/repo/project.git
	$ git pull origin master
```

- branch 생성하기

```
	$ git branch 
	현재 존재하는 브랜치명 확인 및 사용중인 브랜치 앞에는 * 표시
	$ git brach 브랜치이름
	해당 이름을 가진 브랜치 생성
	$ git checkout 브랜치이름
	branch 변경
```

***
#### 참고문서

- [참고문서1](https://lhb0517.tistory.com/entry/CentOS-7-Git-%EC%84%9C%EB%B2%84-%EA%B5%AC%EC%B6%95)
- [참고문서2](https://zetawiki.com/wiki/%EB%A6%AC%EB%88%85%EC%8A%A4_Git_%EC%84%9C%EB%B2%84_%EA%B5%AC%EC%B6%95_(SSH_%ED%94%84%EB%A1%9C%ED%86%A0%EC%BD%9C))
