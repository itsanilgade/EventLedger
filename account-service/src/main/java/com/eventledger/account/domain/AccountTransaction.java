package com.eventledger.account.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="account_transactions", indexes = @Index(name="idx_tx_account_time", columnList="accountId,eventTimestamp"))
public class AccountTransaction {
    @Id
    private String eventId;
    @Column(nullable=false)
    private String accountId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TransactionType type;
    @Column(nullable=false, precision=19, scale=2)
    private BigDecimal amount;
    @Column(nullable=false)
    private String currency;
    @Column(nullable=false)
    private Instant eventTimestamp;
    @Column(nullable=false)
    private Instant appliedAt;

    protected AccountTransaction() {}
    public AccountTransaction(String eventId, String accountId, TransactionType type, BigDecimal amount, String currency, Instant eventTimestamp) {
        this.eventId=eventId; this.accountId=accountId; this.type=type; this.amount=amount; this.currency=currency;
        this.eventTimestamp=eventTimestamp; this.appliedAt=Instant.now();
    }
    public String getEventId(){return eventId;} public String getAccountId(){return accountId;}
    public TransactionType getType(){return type;} public BigDecimal getAmount(){return amount;}
    public String getCurrency(){return currency;} public Instant getEventTimestamp(){return eventTimestamp;}
    public Instant getAppliedAt(){return appliedAt;}
}
