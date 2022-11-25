package com.test.redis.example.ticketing

import com.test.redis.example.distributedlock.DistributeLockService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class TicketIssueService(
    private val distributeLockService: DistributeLockService
) {

    fun issueTicket(ticket: TICKET, user: User) {
        loadData()
        distributeLockService.executeWithLock(lockName = ticket.name, waitTime = 3, leaseTime = 3) {
            TicketOffice.issueTicket(ticket, user)
        }
    }

    @Async
    fun issueTicketAsync(ticket: TICKET, user: User): CompletableFuture<Unit> {
        return CompletableFuture.completedFuture(issueTicket(ticket, user))
    }

    private fun loadData() {
        Thread.sleep((200..300).random().toLong())
    }
}
