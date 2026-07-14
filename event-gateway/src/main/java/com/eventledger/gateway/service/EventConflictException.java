package com.eventledger.gateway.service;

public class EventConflictException extends RuntimeException {
    public EventConflictException(String eventId) {
        super("eventId '" + eventId + "' was already used with a different payload");
    }
}
