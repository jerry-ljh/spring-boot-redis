package com.test.redis.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    fun zAdd(key: String, value: Any, score: Double): Boolean? {
        return redisTemplate.opsForZSet().add(key, value, score)
    }

    fun zCard(key: String): Long? {
        return redisTemplate.opsForZSet().zCard(key)
    }

    fun zRange(key: String, startRank: Long, endRank: Long): Set<Any>? {
        return redisTemplate.opsForZSet().range(key, startRank, endRank)
    }

    fun zRank(key: String, value: Any): Long? {
        return redisTemplate.opsForZSet().rank(key, value)
    }

    fun zRangeByScore(key: String, startScore: Double, endScore: Double): Set<Any>? {
        return redisTemplate.opsForZSet().rangeByScore(key, startScore, endScore)
    }
    
    fun zSize(key: String): Long? {
        return redisTemplate.opsForZSet().size(key)
    }

}