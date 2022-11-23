package com.test.redis.cache

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.util.concurrent.TimeUnit

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@SpringBootTest
class RedisCacheAspectTest(
    private val cacheTest: CacheTest,
    private val redisTemplate: RedisTemplate<String, Any>
) {

    @AfterEach
    fun deleteAllKey() {
        val keys = redisTemplate.keys("testKey*")
        redisTemplate.delete(keys)
    }

    @Test
    fun `RedisCacheable 캐시를 저장한다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 1
    }

    @Test
    fun `RedisCacheable 동일한 key, args로 호출하면 캐시를 반환한다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        val callCachedResult = cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        // when
        val key = redisTemplate.keys("${CacheTest.KEY}*").toList().first()
        val cacheResult = redisTemplate.opsForValue()[key]
        // then
        callCachedResult shouldBe cacheResult
    }

    @Test
    fun `RedisCacheable 동일한 key, 다른 args면 캐시를 새롭게 저장한다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3)
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 2
    }

    @Test
    fun `RedisCacheable ttl을 설정하지 않으면 만료되지 않는다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        // when
        val key = redisTemplate.keys("${CacheTest.KEY}*").toList().first()
        // then
        val ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)
        ttl shouldBe NO_TIME_TO_LIMIT
    }

    @Test
    fun `RedisCacheable ttl에 값을 설정하면 만료시간과 함께 저장한다`() {
        // given
        cacheTest.setCacheWithTTLAndReturnInputs(1, 2, 3, 4)
        // when
        val key = redisTemplate.keys("${CacheTest.KEY}*").toList().first()
        // then
        val ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)
        ttl shouldNotBe NO_TIME_TO_LIMIT
    }

    @Test
    fun `RedisCacheable 메서드 실행중 예외 발생가 발생하면 캐시가 저장되지 않는다`() {
        // given
        assertThrows<Exception> { cacheTest.setCacheAndThrowException(1, 2, 3, 4) }
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 0
    }

    @Test
    fun `RedisCachePut 캐시를 업데이트 한다`() {
        // given
        val result1 = cacheTest.putCacheAndReturnInputs(1, 2, 3, 4)
        val result2 = cacheTest.putCacheAndReturnReversedInputs(1, 2, 3, 4)
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*").toList()
        val cacheResult = redisTemplate.opsForValue()[keys.first()]
        // then
        keys shouldHaveSize 1
        result1 shouldNotBe cacheResult
        result2 shouldBe cacheResult
    }

    @Test
    fun `RedisCachePut ttl을 설정하지 않으면 만료되지 않는다`() {
        // given
        cacheTest.putCacheAndReturnInputs(1, 2, 3, 4)
        // when
        val key = redisTemplate.keys("${CacheTest.KEY}*").toList().first()
        // then
        val ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)
        ttl shouldBe NO_TIME_TO_LIMIT
    }

    @Test
    fun `RedisCachePut ttl을 설정하면 만료시간과 함께 저장한다`() {
        // given
        cacheTest.putCacheWithTTLAndReturnInputs(1, 2, 3, 4)
        // when
        val key = redisTemplate.keys("${CacheTest.KEY}*").toList().first()
        // then
        val ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)
        ttl shouldNotBe NO_TIME_TO_LIMIT
    }

    @Test
    fun `RedisCachePut 메서드 실행중 예외 발생가 발생하면 캐시가 저장되지 않는다`() {
        // given
        assertThrows<Exception> { cacheTest.putCacheAndThrowException(1, 2, 3, 4) }
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 0
    }

    @Test
    fun `RedisCacheEvict 메서드가 호출되면 캐시는 제거된다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        cacheTest.evictCacheAndReturnInputs(1, 2, 3, 4)
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 0
    }

    @Test
    fun `RedisCacheEvict clearAll=true이면 args에 상관없이 캐시키 단위로 전부 제거된다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 5)
        cacheTest.evictCacheWithClearAllAndReturnInputs(1, 2, 3)
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 0
    }

    @Test
    fun `RedisCacheEvict 메서드 실행중 예외 발생가 발생하면 캐시가 제거되지 않는다`() {
        // given
        cacheTest.setCacheAndReturnInputs(1, 2, 3, 4)
        assertThrows<Exception> { cacheTest.eviceCacheAndThrowException(1, 2, 3, 4) }
        // when
        val keys = redisTemplate.keys("${CacheTest.KEY}*")
        // then
        keys shouldHaveSize 1
    }
}

@Component
class CacheTest {
    companion object {
        const val KEY = "testKey"
    }

    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    @RedisCacheable(key = KEY)
    fun setCacheAndReturnInputs(vararg inputs: Int): List<Int> {
        logger.info("call setCacheAndReturnInputs args : ${inputs.toList()}")
        return inputs.toList()
    }

    @RedisCacheable(key = KEY, ttl = 10000)
    fun setCacheWithTTLAndReturnInputs(vararg inputs: Int): List<Int> {
        logger.info("call setCacheWithTTLAndReturnInputs args : ${inputs.toList()}")
        return inputs.toList()
    }

    @RedisCacheable(key = KEY)
    fun setCacheAndThrowException(vararg inputs: Int): List<Int> {
        logger.info("call setCacheAndThrowException args : ${inputs.toList()}")
        throw Exception()
    }

    @RedisCachePut(key = KEY)
    fun putCacheAndReturnInputs(vararg inputs: Int): List<Int> {
        logger.info("call putCacheAndReturnInputs args : ${inputs.toList()}")
        return inputs.toList()
    }

    @RedisCachePut(key = KEY)
    fun putCacheAndReturnReversedInputs(vararg inputs: Int): List<Int> {
        logger.info("call putCacheAndReturnReversedInputs args : ${inputs.toList()}")
        return inputs.toList().reversed()
    }

    @RedisCachePut(key = KEY, ttl = 10000)
    fun putCacheWithTTLAndReturnInputs(vararg inputs: Int): List<Int> {
        logger.info("call putCacheWithTTLAndReturnInputs args : ${inputs.toList()}")
        return inputs.toList()
    }

    @RedisCachePut(key = KEY)
    fun putCacheAndThrowException(vararg inputs: Int): List<Int> {
        logger.info("call putCacheAndThrowException args : ${inputs.toList()}")
        throw Exception()
    }

    @RedisCacheEvict(key = KEY)
    fun evictCacheAndReturnInputs(vararg inputs: Int): List<Int> {
        logger.info("call evictCacheAndReturnInputs args : ${inputs.toList()}")
        return inputs.toList()
    }

    @RedisCacheEvict(key = KEY, clearAll = true)
    fun evictCacheWithClearAllAndReturnInputs(vararg inputs: Int): List<Int> {
        logger.info("call evictCacheWithClearAllAndReturnInputs args : ${inputs.toList()}")
        return inputs.toList()
    }

    @RedisCacheEvict(key = KEY)
    fun eviceCacheAndThrowException(vararg inputs: Int): List<Int> {
        logger.info("call eviceCacheAndThrowException args : ${inputs.toList()}")
        throw Exception()
    }
}
