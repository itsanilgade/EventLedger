package com.eventledger.account.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction,String> {
    List<AccountTransaction> findTop20ByAccountIdOrderByEventTimestampDesc(String accountId);
}
