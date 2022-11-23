package com.test.redis.example.zset

import com.test.redis.repository.RedisRepository
import io.kotest.matchers.collections.shouldHaveSize
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
class QueueingServiceTest(
    private val queueingService: QueueingService,
    private val redisRepository: RedisRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    @BeforeEach
    fun deleteAllKey() {
        val keys = redisTemplate.keys("*")
        redisTemplate.delete(keys)
    }

    @Test
    fun `registerWaitingQueue 대기열에 요청 시간순으로 적재한다`() {
        // given
        val key = "선착순 대기열 key"
        val userCount = 1000L
        // when
        registerWaitingQueueAsync(key, userCount)
        // then
        val userIdList = redisRepository.zRange(key, 0, 9999)!!.toList()
        userIdList shouldHaveSize 1000
        userIdList.forEachIndexed { idx, userId -> redisRepository.zRank(key, userId) shouldBe idx }
    }

    private fun registerWaitingQueueAsync(key: String, userCount: Long) {
        (1..userCount).map { idx ->
            CompletableFuture.runAsync {
                Thread.sleep((0..100).random().toLong())
                queueingService.registerWaitingQueue(key = key, "user_$idx")
            }
        }.map { it.join() }
    }
}