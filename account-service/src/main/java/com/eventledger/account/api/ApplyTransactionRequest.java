package com.eventledger.account.api;
import com.eventledger.account.domain.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
public record ApplyTransactionRequest(
    @NotBlank String eventId,
    @NotNull TransactionType type,
    @NotNull @DecimalMin(value="0.0", inclusive=false) BigDecimal amount,
    @NotBlank @Pattern(regexp="[A-Z]{3}") String currency,
    @NotNull Instant eventTimestamp) {}
