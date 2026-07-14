package com.eventledger.gateway.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name="events", indexes=@Index(name="idx_event_account_time",columnList="accountId,eventTimestamp"))
public class EventRecord {
 @Id private String eventId;
 @Column(nullable=false) private String accountId;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private EventType type;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(nullable=false) private String currency;
 @Column(nullable=false) private Instant eventTimestamp;
 @Lob private String metadataJson;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private EventStatus status;
 @Column(nullable=false) private Instant receivedAt;
 private Instant appliedAt;
 protected EventRecord(){}
 public EventRecord(String eventId,String accountId,EventType type,BigDecimal amount,String currency,Instant eventTimestamp,String metadataJson){this.eventId=eventId;this.accountId=accountId;this.type=type;this.amount=amount;this.currency=currency;this.eventTimestamp=eventTimestamp;this.metadataJson=metadataJson;this.status=EventStatus.RECEIVED;this.receivedAt=Instant.now();}
 public String getEventId(){return eventId;} public String getAccountId(){return accountId;} public EventType getType(){return type;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public Instant getEventTimestamp(){return eventTimestamp;} public String getMetadataJson(){return metadataJson;} public EventStatus getStatus(){return status;} public Instant getReceivedAt(){return receivedAt;} public Instant getAppliedAt(){return appliedAt;}
 public void markApplied(){status=EventStatus.APPLIED;appliedAt=Instant.now();} public void markFailed(){status=EventStatus.FAILED;}
}
