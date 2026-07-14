package com.eventledger.gateway;

import com.eventledger.gateway.api.SubmitEventRequest;
import com.eventledger.gateway.client.AccountServiceClient;
import com.eventledger.gateway.domain.EventType;
import com.eventledger.gateway.service.EventApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class EventApplicationServiceTests {
    @Autowired EventApplicationService service;
    @MockitoBean AccountServiceClient accountClient;

    @Test
    void duplicateIsIdempotentAndEventsAreChronological() {
        var later = request("evt-later", "2026-05-15T14:02:11Z", EventType.CREDIT, "150.00");
        var earlier = request("evt-earlier", "2026-05-14T10:00:00Z", EventType.DEBIT, "25.00");

        assertThat(service.submit(later).duplicate()).isFalse();
        assertThat(service.submit(earlier).duplicate()).isFalse();
        assertThat(service.submit(later).duplicate()).isTrue();

        assertThat(service.list("acct-123")).extracting(e -> e.eventId())
                .containsExactly("evt-earlier", "evt-later");
        verify(accountClient, times(2)).apply(eq("acct-123"), any());
    }

    private SubmitEventRequest request(String id, String timestamp, EventType type, String amount) {
        return new SubmitEventRequest(id, "acct-123", type, new BigDecimal(amount), "USD",
                Instant.parse(timestamp), Map.of("source", "test"));
    }
}
