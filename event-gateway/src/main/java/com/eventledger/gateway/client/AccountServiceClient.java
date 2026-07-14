package com.eventledger.gateway.client;
import com.eventledger.gateway.api.*; import com.eventledger.gateway.config.TraceFilter; import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; import org.slf4j.MDC; import org.springframework.stereotype.Component; import org.springframework.web.client.*;
import java.util.Map;
@Component
public class AccountServiceClient {
 private final RestClient client; public AccountServiceClient(RestClient accountRestClient){this.client=accountRestClient;}
 @CircuitBreaker(name="accountService",fallbackMethod="applyFallback")
 public void apply(String accountId,AccountTransactionRequest request){client.post().uri("/accounts/{id}/transactions",accountId).header(TraceFilter.HEADER,trace()).body(request).retrieve().toBodilessEntity();}
 @CircuitBreaker(name="accountService",fallbackMethod="balanceFallback")
 public BalanceResponse balance(String accountId){return client.get().uri("/accounts/{id}/balance",accountId).header(TraceFilter.HEADER,trace()).retrieve().body(BalanceResponse.class);}
 @CircuitBreaker(name="accountService",fallbackMethod="detailsFallback")
 public Map details(String accountId){return client.get().uri("/accounts/{id}",accountId).header(TraceFilter.HEADER,trace()).retrieve().body(Map.class);}
 private String trace(){return MDC.get("traceId");}
 private void applyFallback(String accountId,AccountTransactionRequest request,Throwable t){throw unavailable(t);}
 private BalanceResponse balanceFallback(String accountId,Throwable t){throw unavailable(t);}
 private Map detailsFallback(String accountId,Throwable t){throw unavailable(t);}
 private AccountServiceUnavailableException unavailable(Throwable t){return new AccountServiceUnavailableException("Account Service is unavailable. Please retry later.",t);}
}
