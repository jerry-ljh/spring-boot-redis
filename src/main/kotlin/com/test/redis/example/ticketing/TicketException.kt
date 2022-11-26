package com.test.redis.example.ticketing

class TicketSoldOutException(msg: String) : RuntimeException(msg)

class TicketNotExistException(msg: String) : RuntimeException(msg)