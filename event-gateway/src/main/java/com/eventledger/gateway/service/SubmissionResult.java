package com.eventledger.gateway.service;
import com.eventledger.gateway.api.EventResponse;
public record SubmissionResult(EventResponse event, boolean duplicate) {}
