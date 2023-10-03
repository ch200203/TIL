MSA 환경을 경험해보면서 여러개로 분산되어진 DB들이 어떻게 트랜잭션을 관리하고 데이터 일관성을 유지 할 수 있을까? 라는 생각이 들어 내용을 찾아보고 정리해보고자 합니다.


## 분산 트랜잭션이 필요한 이유

현재 재직중인 회사에서의 시스템은 수십여개의 서버들이 연결된 MSA 형태로 구성되어 있습니다.
시스템 별로 각기 DB를 분리하여 독립적으로 관리하고 트랜잭션의 가장 중요한 성질 중 하나는 '원자성' 입니다. 단일 DB를 구성할 때와 다르게 DB를 분산하여 운영하게 될 경우 원자성을 만족시키기 어려울 수 있습니다.

A와 B의 데이터베이스가 분산되어있는경우 
다음과 같은 이유로 분산트랜잭션에 대한 관리가 필요합니다.

1. 네트워크 지연 및 실패 이슈
 - 분산 시스템에서는 여러 노드가 네트워크를 통해 통신합니다. 네트워크 지연이나 실패로 인해 특정 노드의 응답을 받지 못하거나 지연될 수 있습니다. 이로 인해 트랜잭션의 일부분만 커밋되고 일부분은 롤백되는 상황이 발생할 수 있습니다.
 
2. 데드락
 - 여러 노드가 서로의 자원 또는 데이터에 동시에 접근하려 할 때, 상호간의 대기 상태에 빠져서 진행을 할 수 없게 되는 현상입니다. 분산 트랜잭션에서는 데드락을 해결하기 위한 중앙화된 관리 메커니즘이 없어 복잡한 해결 전략이 필요합니다.
 
3. 데이터 일관성 유지의 어려움
- 분산 시스템에서 데이터의 복제본이 여러 노드에 분산 저장될 수 있습니다. 따라서 한 노드에서의 데이터 변경이 모든 노드에 즉시 반영되지 않으면 일관성 문제가 발생할 수 있습니다.

말로는 이해가 잘 안될 수 있습니다.
코드를 보면서 이야기를 해보겠습니다.


```java
@Service
@RequiredArgsConstructor
public class OrderService {
	
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    
    public void orderProcess(Order order) {
        inventoryService.decreaseStock(order.getItemId(), order.getQuantity());
        paymentService.charge(order.getUserId(), order.getTotalPrice());
    }
}
```
<img src="https://velog.velcdn.com/images/ch200203/post/9278d525-d8f7-4f17-84e5-46238bb661a9/image.png" width="50%" height="50%">

주문 과정의 일부를 코드로 나타내 보았습니다. 재고 서비스와 결제 서비스. 각 서비스는 각각의 데이터베이스를 가지고 있습니다. 주문이 들어올 때,결제는 성공했지만 재고가 없다면? 이 경우, 두 서비스의 트랜잭션은 롤백되어야 합니다.

위 코드에서 `decreaseStock`과 `charge` 메서드가 각기 다른 데이터베이스에 연결된다면, 한 메서드는 성공하고 다른 메서드가 실패할 위험이 있습니다. 

이러한 상황에서의 일관성을 보장하기 위해 우리는 분산트랜잭션을 관리할 프로세스의 필요성이 있습니다.

## 그럼 스프링에서는?

Spring Boot 대표적으로 **2-Phase-Commit(2PC)** 또는 **SAGA 패턴**을 사용하여 분산 트랜잭션을 관리합니다.

Spring Boot에서 2PC를 구현하는 한 가지 방법은 XA(eXtended Architecture) 프로토콜을 사용하는 것입니다. Spring Boot는 여러 서비스에서 트랜잭션을 관리하는 데 사용할 수 있는 `Atomikos` 및 `Bitronix` 트랜잭션 관리자를 통해 XA 트랜잭션을 지원합니다.

> XA는 분산 컴퓨팅 환경에서 여러 리소스 (예: 데이터베이스, 메시징 시스템) 간의 트랜잭션을 조율하기 위한 표준 인터페이스입니다. 이 인터페이스는 2-Phase-Commit (2PC, Two-Phase Commit) 프로토콜을 사용하여 트랜잭션을 완료합니다. 

### 2-Phase-Commit(2PC)
XA 프로토콜은 2PC 알고리즘을 통해 작동됩니다.
2PC은 두 단계로 구분되어 작동됩니다. 

- Prepare Phase (준비 단계): 트랜잭션 매니저 (TM)는 모든 리소스 매니저 (RM)에게 트랜잭션 커밋 준비를 알립니다. RM들은 이 요청을 받고 필요한 모든 작업을 준비하며 준비가 완료되면 응답합니다.

- Commit/Rollback Phase (커밋/롤백 단계): 모든 RM이 준비되면 TM은 트랜잭션을 커밋합니다. 만약 어떤 RM이 준비되지 않았다면, TM은 트랜잭션을 롤백합니다.

유저가 계좌 A에서 계좌 B로 자금을 이체하는 경우를 예시로 살펴보겠습니다.


```java
public interface BankService {
    void prepareTransfer(int amount);
    void commit();
    void rollback();
}

@Service
public class AccountAService implements BankService {
	
    // 계좌 A에서 금액을 차감하기 전의 준비 작업(계좌호출 차감 금액 확인등..)
    @Override
    public void prepareTransfer(int amount) {    
    }
}

@Service
public class AccountBService implements BankService {

	// 계좌 B로 입금하기 전의 준비 작업(입금 계좌 확인 및 금액 확인 등..)
    @Override
    public void prepareTransfer(int amount) {
    }
}

@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final AccountAService accountA;
    private final AccountBService accountB;

    public void transfer(int amount) {
        boolean isPrepared = accountA.prepareTransfer(amount) && accountB.prepareTransfer(amount);

        if (isPrepared) {
            accountA.commit();
            accountB.commit();
        } else {
            accountA.rollback();
            accountB.rollback();
        }
    }
}
```

<img src="https://velog.velcdn.com/images/ch200203/post/b1695173-cd9d-4bad-bf9b-405aa96edcbb/image.png" width="100%" height="70%">

단계별 설명 : 

1. Prepare Phase (준비 단계)

- 유저가 TransferService를 통해 금액 이체를 요청합니다.
 - Coordinator(`TransferService` )는 계좌 A (`AccountAService`)와 계좌 B (`AccountBService`)에게 prepare 상태인지 확인을 요청합니다 (= `prepareTransfer` 메서드 호출)
- 각 계좌 서비스는 자신의 상태를 확인하여 prepare 상태이면 준비 완료 응답을 반환하고, 그렇지 않으면 준비 실패 응답을 반환합니다.

2. Commit/Rollback Phase (커밋/롤백 단계)

- Coordinator는 모든 계좌 서비스로부터 응답을 받습니다.
- 만약 모든 서비스가 prepared 응답을 반환하면, Coordinator는 각 계좌 서비스에 커밋을 요청합니다. (= `commit` 메서드 호출)
	- `commit` 호출에 따라 계좌 A에서는 출금이 이루어지고, 계좌 B에서는 입금이 이루어집니다. 
- 그러나 하나 이상의 서비스에서 prepared 실패 응답을 받으면, Coordinator는 롤백을 요청합니다.(=`rollback` 메서드 호출) 
	- `rollback` 호출에 따라 이전 상태로 복구됩니다.

> - 원자성 보장: 이 알고리즘의 핵심은 여러 데이터베이스나 서비스에 걸쳐 있는 트랜잭션도 하나의 트랜잭션처럼 다룰 수 있게 해준다는 것입니다. 
즉, 모든 작업이 성공적으로 수행되거나 아무 작업도 수행되지 않은 것처럼 보장됩니다.
- 코디네이터의 중요성: 코디네이터는 분산 트랜잭션을 관리하고 조율하는 중요한 역할을 합니다. 
모든 작업의 상태를 모니터링하고, 최종 커밋 또는 롤백 결정을 내립니다.

**2PC를 사용하였을 경우의 문제점**


### SAGA 패턴
SAGA 패턴은 긴 트랜잭션을 여러 개의 짧은 트랜잭션으로 분리하는 접근 방식입니다. 각 짧은 트랜잭션은 다른 트랜잭션의 완료를 기다리지 않고 독립적으로 실행됩니다. 만약 중간에 문제가 발생하면, 컴펜세이션(보상) 트랜잭션이 실행되어 이전 트랜잭션을 롤백하는 것과 같은 효과를 가져옵니다.
