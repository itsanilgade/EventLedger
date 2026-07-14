package com.eventledger.gateway;

import com.eventledger.gateway.api.SubmitEventRequest;
import com.eventledger.gateway.domain.EventType;
import com.eventledger.gateway.service.EventApplicationService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "account-service.base-url=http://localhost:19091")
class GatewayToAccountIntegrationTest {
    private static HttpServer server;
    private static final AtomicReference<String> receivedTrace = new AtomicReference<>();

    @Autowired EventApplicationService service;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(19091), 0);
        server.createContext("/accounts/acct-integration/transactions", exchange -> {
            receivedTrace.set(exchange.getRequestHeaders().getFirst("X-Trace-Id"));
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterAll
    static void stopServer() { if (server != null) server.stop(0); }

    @Test
    void submitsThroughRealHttpClientAndPropagatesTrace() {
        org.slf4j.MDC.put("traceId", "integration-trace-001");
        try {
            var result = service.submit(new SubmitEventRequest(
                    "evt-integration", "acct-integration", EventType.CREDIT,
                    new BigDecimal("42.00"), "USD", Instant.parse("2026-05-15T14:02:11Z"),
                    Map.of("source", "integration-test")));
            assertThat(result.event().status().name()).isEqualTo("APPLIED");
            assertThat(receivedTrace.get()).isEqualTo("integration-trace-001");
        } finally {
            org.slf4j.MDC.clear();
        }
    }
}
