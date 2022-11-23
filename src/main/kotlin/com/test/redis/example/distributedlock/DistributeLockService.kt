package com.test.redis.example.distributedlock

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class DistributeLockService(
    private val redissonClient: RedissonClient,
) {
    companion object {
        const val LOCK_NAME = "jerry_lock"
    }

    private val log = LoggerFactory.getLogger(this::class.simpleName)

    fun executeLogicWithLock(waitTime: Long, leaseTime: Long, logic: () -> Unit) {
        val lock: RLock = redissonClient.getLock(LOCK_NAME)
        val isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)
        try {
            if (isLocked.not()) throw IllegalStateException("[$LOCK_NAME] lock 획득 실패")
            log.info("lock 획득")
            logic()
            log.info("logic 수행 완료")
        } finally {
            if (lock.isLocked && lock.isHeldByCurrentThread) lock.unlock()
            log.info("lock 반납")
        }
    }

}