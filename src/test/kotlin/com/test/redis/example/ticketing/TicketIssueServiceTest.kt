package com.test.redis.example.ticketing

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.util.concurrent.CompletionException

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@SpringBootTest
class TicketIssueServiceTest(
    private val ticketIssueService: TicketIssueService,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    @BeforeEach
    fun deleteAllKey() {
        val keys = redisTemplate.keys("*")
        redisTemplate.delete(keys)
    }

    @Test
    fun `issueTicketAsync 잔여 티켓 수보다 많은 유저가 동시에 티켓 발급을 요청해도 잔여 티켓이상으로 발급되지 않는다`() {
        // given
        val totalTicketCount = TicketOffice.getRemainTicketCount(TICKET.JERRY_MUSICAL)
        val userCount = totalTicketCount * 2
        // when
        val result = assertThrows<CompletionException> {
            (1..userCount)
                .map { ticketIssueService.issueTicketAsync(TICKET.JERRY_MUSICAL, User(id = "user_$it")) }
                .map { it.join() }
        }
        // then
        val remainTicketCount = TicketOffice.getRemainTicketCount(TICKET.JERRY_MUSICAL)
        remainTicketCount shouldBe 0
        result.message shouldContain "남아있는 티켓이 존재하지 않습니다"
    }
}