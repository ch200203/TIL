# RabbitMQ와 Kafka 비교하기

## Message Queue (메시지 큐)
최근 이직을 준비하면서 채용공고들을 살펴보았는데 많은 서비스 기업에서 Rabbit MQ, Kafka 와 같은 기술 스택을 요구하고 있습니다.   
이제는 알고 넘어가야할 떄가 된거 같아 해당 내용을 정리해보려고 합니다.

먼저, RabbitMQ, Kafka는 스트림 처리에 사용할 수 있는 메시지 큐(Message Queue)의 종류 입니다.

메시지 큐는 프로세스 or 프로그램 인스턴스간 데이터를 교환할떄 사용하는 통신 방법입니다.
더 확장된 개념으로는 메시지 지향 미들웨어(Message Oriented MiddleWare:MOM)을 구현한 시스템을 의미합니다.

메시지 지향 미들웨어는 비동기 메시지를 사용하는 응용 프로그램들 사이에서 데이터를 송수신하는 것을 의미합니다. 여기서 메시지(Message)란 요청, 응답, 오류 메시지 혹은 단순한 정보 등의 작은 정보들을 말합니다.

# Reference

 - https://tecoble.techcourse.co.kr/post/2021-09-19-message-queue/