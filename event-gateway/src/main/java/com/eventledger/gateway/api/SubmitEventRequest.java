package com.eventledger.gateway.api;
import com.eventledger.gateway.domain.EventType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
public record SubmitEventRequest(
 @NotBlank String eventId,
 @NotBlank String accountId,
 @NotNull EventType type,
 @NotNull @DecimalMin(value="0.0",inclusive=false) BigDecimal amount,
 @NotBlank @Pattern(regexp="[A-Z]{3}",message="must be a 3-letter uppercase code") String currency,
 @NotNull Instant eventTimestamp,
 Map<String,Object> metadata) {}
