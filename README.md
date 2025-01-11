# Context

MSA 환경에서는 도메인에 대한 생성, 수정, 삭제 이벤트가 일어났을 때, 이를 메세지 브로커를 통해서 다른 분산 서버에 전송해야하는 일이 발생한다. 
하지만, 데이터베이스와 메세지 브로커를 아우르는 전통적인 분산 트랜잭션(2PC, 2-Phase Commit)을 사용하는 것은 현실적으로 불가능하다. 데이터베이스나 메세지 브로커가 2PC를 지원하지 않을 수 있을 뿐더러, 두 인프라가 강결합 되는것은 바람직하지 않을 수 있다.
구체적인 문제 상황은 아래와 같다.
1. 데이터베이스 작업과 메세지 전송이 하나의 트랜잭션으로 묶일 경우: 메세지 전송 시에 예외가 발생할 경우, 데이터베이스 Rollback이 발생한다. 이 경우, 메세지 전송 자체는 성공할 수 있다. (메세지 브로커 내부 동작을 파악하지 못하고 있으므로) 
   -> 즉, 각 Consumer에 이벤트는 전송되었으나, 실제 이벤트 발행 서비스는 동작을 Rollback하여 도메인의 원자성이 보장되지 않는다.
2. 메세지 전송이 트랜잭션 외부에 있을 경우: 별도의 설명이 필요하지 않을 정도로 명확한 문제가 발생한다. 도메인 이벤트에 대한 전송을 완벽하게 보장하지 못한다.

# Transaction Outbox Pattern


데이터베이스의 수정과 메세지 브로커로의 전송을 원자적으로 수행하려면 어떻게 해야할까?

메세지를 전송하는 서비스는 도메인을 업데이트하는 트랜잭션의 일부로 전송할 메세지를 데이터베이스에 먼저 저장한다. 이후 별도의 프로제스로 메세지 브로커에 메세지를 전송한다. 
즉, 메세지 저장까지 하나의 트랜잭션으로 묶어 메세지 발행 자체를 보장하도록 하는것이다.

## 아키텍쳐
<img width="926" alt="Screenshot 2025-01-11 at 3 17 12 PM" src="https://github.com/user-attachments/assets/7c35ef8a-5f82-4a81-91b6-4f21e562b46d" />

- Sender: 도메인 메세지를 보내는 서비스
- Database: 도메인 엔티티와 메세지 Outbox를 저장
- Message Outbox
	- 관계형 데이터베이스: 전송할 메세지를 별도의 테이블에 저장
	- NoSQL 데이터베이스: 각 레코드에 속성으로 저장
- Message Relay: 저장된 메세지를 메세지 브로커로 전송

### Message Relay

- Scheduler 혹은 이벤트 기반으로 실행되며, 아웃박스에서 메세지를 읽어와 메세지 브로커로 전송한다.
- 메세지 브로커로 전송된 후, 아웃박스에서 삭제하거나 상태를 업데이트한다.
- 전송이 실패한 경우, 재시도가 가능하다 -> 메세지 전송이 보장된다.

## 구현

- 해당 패턴을 구현하는 여러가지 방법이 있겠지만, 이번 예제에서는 Scheduler를 활용한 Relay 대신 Transaction 상태에 따라 이벤트를 처리하는 EventListener를 사용하도록 구현하였다. (참고 - 29cm의 적용 사례)

![transactional drawio](https://github.com/user-attachments/assets/33278baa-7e18-4ec8-8971-774f08e07cf0)

1. User 저장: User 도메인 저장 요청
2. User Database 저장: 1에서 만든 엔티티를 Database에 저장
3. User 생성 이벤트 발행: ApplicationEventPublisher 를 사용하여 이벤트를 발행한다.
4. EventRecord 저장: UserCreateService의 create 트랜잭션이 커밋되기 직전 Database에 Record 정보를 저장한다. -> Transcation Outbox
5. Kafka 프로듀싱: UserCreateService의 create 트랜잭션이 커밋되는 직후 kafka에 이벤트를 프로듀싱 한다. 
6. EventRecord 결과 저장: Kafka 프로듀싱 결과와 함께 기존 Record를 업데이트 혹은 삭제 한다.


### UserCreateService

```
@Service
class UserCreateService(
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional // --- 1
    fun create(name: String) {
        val user = User(name)

        userRepository.save(user) // --- 2
        eventPublisher.publishEvent(UserEventRecord(user.id, user.name)) // --- 3

        logger.info("Complete User Creation")
    }
}
```
1. @Transactional : 도메인 로직과 Outbox 저장을 위한 이벤트 발행을 하나의 트랜잭션 범위로 잡는다. 
2. user 도메인 저장: 도메인 로직을 실행한다.
3. event 발행: Spring에서 제공하는 ApplicationEventPublisher 를 활용하여 이벤트를 발행한다.


### UserEventListener

```
@Component
class UserEventListener(
    private val userEventRecorder: UserEventRecorder,
    private val userEventRepository: UserEventRepository,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT) // --- 1
    fun recordMessageHandler(eventRecord: UserEventRecord) { // ---- 2
        userEventRecorder.save(eventRecord)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // --- 1
    fun sendMessageHandler(eventRecord: UserEventRecord) { // --- 3
        val event = UserCreateEvent(eventRecord.id, eventRecord.name)

        val status: EventRecordStatus = runCatching {
            userEventRepository.publishCreateEvent(event)
        }.fold(
            onSuccess = {
                logger.info("Success to Publish Event")
                EventRecordStatus.SUCCESS
            }, onFailure = {
                logger.info("Fail to Publish Event")
                EventRecordStatus.FAIL
            }
        )

        userEventRecorder.save(eventRecord.copy(status = status))
    }
}
```
1. @TransactionalEventListener : 트랜잭션 상태에 따라 이벤트 처리 메서드를 지정할 수 있다. 
	1. TransactionPhase.BEFORE_COMMIT : Transaction이 커밋되기 직전에 실행 -> 해당 로직이 성공해야 Commit 
	2. TransactionPhase.AFTER_COMMIT : Transaction이 커밋되고 난 후 실행
	3. Commit을 기준으로 Before과 After로 나누어져 있어, 순차실행 보장이 되므로 이를 이용하여 Transaction Outbox Pattern을 구현
2. recordMessageHandler() : Processing 중인 EventRecord를 최초 저장한다.
3. sendMessageHandler() : userEventRepository(Kafka)를 발행하고, 해당 결과에 따라 Record 상테를 업데이트하여 저장한다.
---

## 이슈

해당 패턴은 도메인 객체의 원자성을 보장한다. 
하지만 Scheduler를 활용하거나, 실패 케이스에 대해 재시도 요구사항을 가진 경우, 메세지 브로커로 동일한 메세지가 여러번 게시될 수 있다.
결과적으로 Consumer는 멱등성을 보장할 수 있는 Idempotent Consumer로 구현되어야 한다. 
위 방법은 추후 게시글로 소개 할 예정이다.
