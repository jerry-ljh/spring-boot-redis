package com.test.redis.cache

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.concurrent.TimeUnit

@Aspect
@Component
class RedisCacheAspect(
    private val redisTemplate: RedisTemplate<String, Any?>,
) {

    @Around("@annotation(RedisCacheable)")
    fun cacheableProcess(joinPoint: ProceedingJoinPoint): Any? {
        val redisCacheable = (joinPoint.signature as MethodSignature).method.getAnnotation(RedisCacheable::class.java)
        val cacheKey = generateKey(redisCacheable.key, joinPoint)
        if (redisTemplate.hasKey(cacheKey)) return redisTemplate.opsForValue().get(cacheKey)
        val methodReturnValue = joinPoint.proceed()
        if (redisCacheable.ttl < 0) {
            redisTemplate.opsForValue().set(cacheKey, methodReturnValue)
        } else {
            redisTemplate.opsForValue()
                .set(cacheKey, methodReturnValue, redisCacheable.ttl, TimeUnit.MILLISECONDS)
        }
        return methodReturnValue
    }

    @Around("@annotation(RedisCacheEvict)")
    fun cacheEvictProcess(joinPoint: ProceedingJoinPoint): Any? {
        val methodReturnValue = joinPoint.proceed()
        val redisCacheEvict = (joinPoint.signature as MethodSignature).method.getAnnotation(RedisCacheEvict::class.java)
        if (redisCacheEvict.clearAll) {
            val keySet = redisTemplate.keys("${redisCacheEvict.key}*")
            redisTemplate.delete(keySet)
        } else {
            val cacheKey = generateKey(redisCacheEvict.key, joinPoint)
            redisTemplate.delete(cacheKey)
        }
        return methodReturnValue
    }

    @Around("@annotation(RedisCachePut)")
    fun cachePutProcess(joinPoint: ProceedingJoinPoint): Any? {
        val redisCachePut = (joinPoint.signature as MethodSignature).method.getAnnotation(RedisCachePut::class.java)
        val cacheKey = generateKey(redisCachePut.key, joinPoint)
        val methodReturnValue = joinPoint.proceed()
        if (redisCachePut.ttl < 0) {
            redisTemplate.opsForValue().set(cacheKey, methodReturnValue)
        } else {
            redisTemplate.opsForValue().set(cacheKey, methodReturnValue, redisCachePut.ttl, TimeUnit.MILLISECONDS)
        }
        return methodReturnValue
    }

    private fun generateKey(
        cacheKey: String,
        joinPoint: ProceedingJoinPoint,
    ): String {
        val generatedKey = StringUtils.arrayToCommaDelimitedString(joinPoint.args)
        return "$cacheKey::($generatedKey)"
    }
}
