package com.eventledger.gateway;

import com.eventledger.gateway.client.AccountServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayWebTests {
    @Autowired MockMvc mvc;
    @MockitoBean AccountServiceClient accountClient;

    @Test
    void validatesInputAndReturnsTraceId() throws Exception {
        mvc.perform(post("/events").header("X-Trace-Id", "trace-test-123")
                        .contentType("application/json")
                        .content("""
                          {"eventId":"bad","accountId":"acct","type":"CREDIT","amount":0,
                           "currency":"USD","eventTimestamp":"2026-01-01T00:00:00Z"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Trace-Id", "trace-test-123"))
                .andExpect(jsonPath("$.traceId").value("trace-test-123"));
    }

    @Test
    void localReadsStillWorkAfterAccountCallFailure() throws Exception {
        doThrow(new com.eventledger.gateway.client.AccountServiceUnavailableException("Account Service is unavailable"))
                .when(accountClient).apply(anyString(), any());
        mvc.perform(post("/events").contentType("application/json").content("""
            {"eventId":"failed-1","accountId":"acct-down","type":"CREDIT","amount":10,
             "currency":"USD","eventTimestamp":"2026-01-01T00:00:00Z"}
            """))
            .andExpect(status().isServiceUnavailable());

        mvc.perform(get("/events/failed-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
