package com.test.redis.example.distributedlock

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@SpringBootTest
class DistributeLockServiceTest(
    private val distributeLockService: DistributeLockService
) {

    @Test
    fun `executeLogicWithLock 분산락을 걸고 순차 실행한다`() {
        // given
        var num = 0
        // when
        executeLogicWithLockAsync(threadCount = 1000, waitTime = 3, leaseTime = 3) { num += 1 }
        // then
        num shouldBe 1000

    }

    @Test
    fun `executeLogicWithLock lock wait time이 지나면 예외를 반환한다`() {
        val result = assertThrows<CompletionException> {
            executeLogicWithLockAsync(threadCount = 2, waitTime = 3, leaseTime = 3) {
                Thread.sleep(5000)
            }
        }
        result.message shouldContain "lock 획득 실패"
    }

    @Test
    fun `executeLogicWithLock lock lease time이 지나면 작업 완료 여부에 상관없이 lock이 해제된다`() {
        assertDoesNotThrow {
            executeLogicWithLockAsync(threadCount = 3, waitTime = 3, leaseTime = 1) {
                Thread.sleep(5000)
            }
        }
    }


    private fun executeLogicWithLockAsync(waitTime: Long, leaseTime: Long, threadCount: Long, logic: () -> Unit) {
        (1..threadCount).map {
            CompletableFuture.runAsync {
                distributeLockService.executeLogicWithLock(waitTime, leaseTime) {
                    logic()
                }
            }
        }.map { it.join() }
    }
}