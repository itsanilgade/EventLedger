package com.eventledger.gateway.api;
import com.eventledger.gateway.domain.EventType;
import java.math.BigDecimal;
import java.time.Instant;
public record AccountTransactionRequest(String eventId,EventType type,BigDecimal amount,String currency,Instant eventTimestamp) {}
