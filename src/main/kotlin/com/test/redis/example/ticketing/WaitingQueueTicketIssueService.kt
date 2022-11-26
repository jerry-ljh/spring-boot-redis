package com.test.redis.example.ticketing

import com.test.redis.repository.RedisRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WaitingQueueTicketIssueService(
    private val ticketIssueService: TicketIssueService,
    private val redisRepository: RedisRepository
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)

    fun registerWaitingQueue(ticket: TICKET, user: User) {
        val score = System.currentTimeMillis().toDouble()
        val result: Boolean? = redisRepository.zAdd(ticket.name, user.id, score)
        if (result != true) throw IllegalStateException("대기열 등록 실패 user: $user, time: $score")
        log.info("ticket: $ticket, user: $user 등록 완료, time: $score")
    }

    fun getUserListByRank(ticket: TICKET, startRank: Long, endRank: Long): List<User> {
        val valueSet = redisRepository.zRange(ticket.name, startRank, endRank) ?: emptySet()
        return valueSet.map { User(it.toString()) }
    }

    fun getEarliestRegisteredUserId(ticket: TICKET): String {
        return redisRepository.zPopMin(ticket.name)?.value?.toString()
            ?: throw TicketNotExistException("발급 대상이 존재하지 않습니다")
    }

    fun issue(ticket: TICKET, startRank: Long, endRank: Long) {
        val userList = getUserListByRank(ticket, startRank, endRank)
        userList.map { user -> ticketIssueService.issueTicketAsync(ticket, user) }.map { it.join() }
    }

    fun issue(ticket: TICKET) {
        var issuableStatus = true
        while (issuableStatus) {
            try {
                val userId = getEarliestRegisteredUserId(ticket)
                ticketIssueService.issueTicket(ticket, User(userId))
            } catch (e: Exception) {
                when (e) {
                    is TicketNotExistException, is TicketSoldOutException -> {
                        log.info(e.stackTraceToString())
                        issuableStatus = false
                    }

                    else -> throw e
                }
            }
        }
    }
}
