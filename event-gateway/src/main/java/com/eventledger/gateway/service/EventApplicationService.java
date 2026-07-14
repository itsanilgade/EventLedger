package com.eventledger.gateway.service;

import com.eventledger.gateway.api.AccountTransactionRequest;
import com.eventledger.gateway.api.EventResponse;
import com.eventledger.gateway.api.SubmitEventRequest;
import com.eventledger.gateway.client.AccountServiceClient;
import com.eventledger.gateway.domain.EventRecord;
import com.eventledger.gateway.domain.EventRepository;
import com.eventledger.gateway.domain.EventStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EventApplicationService {
    private static final Logger log = LoggerFactory.getLogger(EventApplicationService.class);

    private final EventRepository repository;
    private final AccountServiceClient accountClient;
    private final ObjectMapper objectMapper;
    private final Counter accepted;
    private final Counter duplicates;
    private final Counter failures;
    private final Counter reconciled;

    public EventApplicationService(EventRepository repository,
                                   AccountServiceClient accountClient,
                                   ObjectMapper objectMapper,
                                   MeterRegistry registry) {
        this.repository = repository;
        this.accountClient = accountClient;
        this.objectMapper = objectMapper;
        this.accepted = registry.counter("gateway_events_accepted_total");
        this.duplicates = registry.counter("gateway_events_duplicate_total");
        this.failures = registry.counter("gateway_account_call_failures_total");
        this.reconciled = registry.counter("gateway_events_reconciled_total");
    }

    public SubmissionResult submit(SubmitEventRequest request) {
        var existing = repository.findById(request.eventId());
        if (existing.isPresent()) {
            return handleReplay(existing.get(), request);
        }

        var event = new EventRecord(
                request.eventId(), request.accountId(), request.type(), request.amount(),
                request.currency(), request.eventTimestamp(), json(request.metadata()));
        try {
            repository.saveAndFlush(event);
        } catch (DataIntegrityViolationException race) {
            var winner = repository.findById(request.eventId()).orElseThrow(() -> race);
            return handleReplay(winner, request);
        }

        applyAndRecord(event, false);
        return new SubmissionResult(map(event), false);
    }

    private SubmissionResult handleReplay(EventRecord event, SubmitEventRequest request) {
        validateSamePayload(event, request);
        duplicates.increment();

        if (event.getStatus() == EventStatus.APPLIED) {
            return new SubmissionResult(map(event), true);
        }

        // Safe recovery: Account Service is itself idempotent by eventId. This handles
        // a lost HTTP response after the downstream transaction was already committed.
        applyAndRecord(event, true);
        return new SubmissionResult(map(event), true);
    }

    private void applyAndRecord(EventRecord event, boolean replay) {
        try {
            accountClient.apply(event.getAccountId(), new AccountTransactionRequest(
                    event.getEventId(), event.getType(), event.getAmount(),
                    event.getCurrency(), event.getEventTimestamp()));
            event.markApplied();
            repository.save(event);
            accepted.increment();
            if (replay) {
                reconciled.increment();
            }
            log.info("Event applied eventId={} accountId={} replay={}",
                    event.getEventId(), event.getAccountId(), replay);
        } catch (RuntimeException exception) {
            event.markFailed();
            repository.save(event);
            failures.increment();
            log.warn("Account Service call failed eventId={} accountId={}",
                    event.getEventId(), event.getAccountId(), exception);
            throw exception;
        }
    }

    private void validateSamePayload(EventRecord event, SubmitEventRequest request) {
        boolean same = Objects.equals(event.getAccountId(), request.accountId())
                && event.getType() == request.type()
                && event.getAmount().compareTo(request.amount()) == 0
                && Objects.equals(event.getCurrency(), request.currency())
                && Objects.equals(event.getEventTimestamp(), request.eventTimestamp())
                && Objects.equals(event.getMetadataJson(), json(request.metadata()));
        if (!same) {
            throw new EventConflictException(request.eventId());
        }
    }

    @Transactional(readOnly = true)
    public EventResponse get(String id) {
        return repository.findById(id).map(this::map)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(String accountId) {
        return repository.findByAccountIdOrderByEventTimestampAscEventIdAsc(accountId)
                .stream().map(this::map).toList();
    }

    private String json(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("metadata must be valid JSON", exception);
        }
    }

    private EventResponse map(EventRecord event) {
        try {
            return new EventResponse(event.getEventId(), event.getAccountId(), event.getType(),
                    event.getAmount(), event.getCurrency(), event.getEventTimestamp(),
                    objectMapper.readValue(event.getMetadataJson(), new TypeReference<>() {}),
                    event.getStatus(), event.getReceivedAt(), event.getAppliedAt());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored metadata is invalid", exception);
        }
    }
}
