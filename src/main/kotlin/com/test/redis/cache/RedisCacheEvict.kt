package com.test.redis.cache

/**
 * 메서드 실행 후 캐시를 제거한다.
 * 메서드 실행중 예외 발생시 캐시가 제거되지 않는다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisCacheEvict(
    /**
     * redis에 사용할 캐시 key
     */
    val key: String,

    /**
     *  clearAll = true : 메서드 parameter에 관계 없이 해당 key로 전부 제거
     *  clearAll = false : 메서드 parameter와 일치하는 캐시만 제거
     */
    val clearAll: Boolean = false
)
