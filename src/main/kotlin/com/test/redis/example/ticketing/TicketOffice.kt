package com.test.redis.example.ticketing

import org.slf4j.LoggerFactory

object TicketOffice {

    private val log = LoggerFactory.getLogger(this::class.simpleName)
    const val JERRY_MOVIE_TOTAL_TICKET_COUNT = 150L
    const val JERRY_MUSICAL_TOTAL_TICKET_COUNT = 200L
    const val JERRY_PHOTO_EXHIBITION_TOTAL_TICKET_COUNT = 300L
    private val remainTicketCountMap: MutableMap<TICKET, Long> = mutableMapOf(
        TICKET.JERRY_MOVIE to JERRY_MOVIE_TOTAL_TICKET_COUNT,
        TICKET.JERRY_MUSICAL to JERRY_MUSICAL_TOTAL_TICKET_COUNT,
        TICKET.JERRY_PHOTO_EXHIBITION to JERRY_PHOTO_EXHIBITION_TOTAL_TICKET_COUNT
    )

    fun getRemainTicketCount(ticket: TICKET): Long {
        return remainTicketCountMap[ticket] ?: throw TicketNotExistException("존재하지 않는 타입의 티켓 $ticket")
    }

    fun issueTicket(ticket: TICKET, user: User) {
        val remainTicket = remainTicketCountMap[ticket] ?: throw TicketNotExistException("존재하지 않는 타입의 티켓 $ticket")
        if (remainTicket <= 0) throw TicketSoldOutException("남아있는 티켓이 존재하지 않습니다 $ticket")
        Thread.sleep(10)
        remainTicketCountMap[ticket] = remainTicket - 1
        log.info("ticket 발급 완료. ticket: ${ticket.name} user: $user, 남은 티켓 수 ${remainTicketCountMap[ticket]}")
    }
}

enum class TICKET {
    JERRY_MUSICAL, JERRY_MOVIE, JERRY_PHOTO_EXHIBITION
}