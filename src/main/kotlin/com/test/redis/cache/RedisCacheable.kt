package com.test.redis.cache

/**
 * 메서드 반환 값을 캐시 저장한다.
 * 이미 저장된 캐시가 있다면 캐시 값을 리턴한다.
 * 메서드 실행중 예외 발생시 캐시가 저장되지 않는다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisCacheable(
    /**
     * redis에 사용할 캐시 key
     */
    val key: String,
    /**
     * 캐시 만료 기간 millisecond (time-to-live)  default : 만료시간 없음
     */
    val ttl: Long = NO_TIME_TO_LIMIT,
)
