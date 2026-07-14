package com.eventledger.account;
import com.eventledger.account.api.ApplyTransactionRequest;
import com.eventledger.account.domain.TransactionType;
import com.eventledger.account.service.AccountApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class AccountServiceApplicationTests {
 @Autowired AccountApplicationService service;
 @Test void computesBalanceAndIgnoresDuplicate(){
  var credit=new ApplyTransactionRequest("a-1",TransactionType.CREDIT,new BigDecimal("100.00"),"USD",Instant.parse("2026-01-02T00:00:00Z"));
  var debit=new ApplyTransactionRequest("a-2",TransactionType.DEBIT,new BigDecimal("25.00"),"USD",Instant.parse("2026-01-01T00:00:00Z"));
  service.apply("acct-test",credit); service.apply("acct-test",debit); service.apply("acct-test",credit);
  assertThat(service.balance("acct-test").balance()).isEqualByComparingTo("75.00");
 }
}
