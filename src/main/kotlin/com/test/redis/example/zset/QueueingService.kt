package com.test.redis.example.zset

import com.test.redis.repository.RedisRepository
import org.springframework.stereotype.Service

@Service
class QueueingService(
    private val redisRepository: RedisRepository
) {

    fun registerWaitingQueue(key: String, userId: String) {
        val result: Boolean? = redisRepository.zAdd(key, userId, System.currentTimeMillis().toDouble())
        if (result != true) {
            throw IllegalStateException("대기열 등록 실패, key : $key, userId: $userId")
        }
    }
}
