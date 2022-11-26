package com.test.redis.example.ticketing

import com.test.redis.example.ticketing.TicketOffice.JERRY_PHOTO_EXHIBITION_TOTAL_TICKET_COUNT
import com.test.redis.repository.RedisRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.util.concurrent.CompletableFuture

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@SpringBootTest
class WaitingQueueTicketIssueServiceTest(
    private val waitingQueueTicketIssueService: WaitingQueueTicketIssueService,
    private val redisRepository: RedisRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
) {

    @BeforeEach
    fun deleteAllKey() {
        val keys = redisTemplate.keys("*")
        redisTemplate.delete(keys)
    }


    @Test
    fun `registerTicketingWaitingQueue 대기열은 선착순으로 등록된다`() {
        // when
        (1..1000).map {
            CompletableFuture.runAsync {
                Thread.sleep((0..10).random().toLong())
                waitingQueueTicketIssueService.registerWaitingQueue(TICKET.JERRY_MUSICAL, User("user_$it"))
            }
        }.map { it.join() }
        // then
        var prevUserTimestamp = redisRepository.zPopMin(TICKET.JERRY_MUSICAL.name)!!.score!!
        while (redisRepository.zSize(TICKET.JERRY_MUSICAL.name)!! > 0L) {
            val nextUserTimestamp = redisRepository.zPopMin(TICKET.JERRY_MUSICAL.name)!!.score!!
            prevUserTimestamp shouldBeLessThanOrEqualTo nextUserTimestamp
            prevUserTimestamp = nextUserTimestamp
        }
    }

    @Test
    fun `getUserListByRank 상위 1000명의 유저 목록을 가져온다`() {
        // given
        setWaitingQueue(2000, TICKET.JERRY_MUSICAL)
        // when
        val userList = waitingQueueTicketIssueService.getUserListByRank(TICKET.JERRY_MUSICAL, 0, 999)
        // then
        userList shouldHaveSize 1000
    }

    @Test
    fun `issue 대기열에서 선착순 50명에 대한 티켓을 발급한다`() {
        // given
        val issueCount = 50L
        setWaitingQueue(1000, TICKET.JERRY_PHOTO_EXHIBITION)
        // when
        waitingQueueTicketIssueService.issue(TICKET.JERRY_PHOTO_EXHIBITION, 0, issueCount - 1)
        // then
        TicketOffice.getRemainTicketCount(TICKET.JERRY_PHOTO_EXHIBITION) shouldBe (JERRY_PHOTO_EXHIBITION_TOTAL_TICKET_COUNT - issueCount)
    }

    @Test
    fun `issue 대기열에서 선착순으로 티켓을 모두 발급한다`() {
        // given
        val totalTicketCount = TicketOffice.getRemainTicketCount(TICKET.JERRY_MOVIE)
        setWaitingQueue(userCount = totalTicketCount * 10, TICKET.JERRY_MOVIE)
        // when
        waitingQueueTicketIssueService.issue(TICKET.JERRY_MOVIE)
        // then
        TicketOffice.getRemainTicketCount(TICKET.JERRY_MOVIE) shouldBe 0
    }

    fun setWaitingQueue(userCount: Long, ticket: TICKET) {
        (1..userCount).map {
            CompletableFuture.supplyAsync {
                Thread.sleep((0..10).random().toLong())
                waitingQueueTicketIssueService.registerWaitingQueue(ticket, User("user_$it"))
            }
        }.map { it.join() }
    }
}