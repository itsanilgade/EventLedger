package com.eventledger.account.service;

import com.eventledger.account.api.*;
import com.eventledger.account.domain.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AccountApplicationService {
    private static final Logger log= LoggerFactory.getLogger(AccountApplicationService.class);
    private final AccountRepository accounts;
    private final AccountTransactionRepository transactions;
    private final Counter appliedCounter;
    private final Counter duplicateCounter;

    public AccountApplicationService(AccountRepository accounts, AccountTransactionRepository transactions, MeterRegistry registry){
        this.accounts=accounts; this.transactions=transactions;
        this.appliedCounter=registry.counter("account_transactions_applied_total");
        this.duplicateCounter=registry.counter("account_transactions_duplicate_total");
    }

    @Transactional
    public TransactionResponse apply(String accountId, ApplyTransactionRequest request){
        var existing=transactions.findById(request.eventId());
        if(existing.isPresent()){
            duplicateCounter.increment();
            return map(existing.get());
        }
        Account account=accounts.findById(accountId).orElseGet(()->new Account(accountId, request.currency()));
        if(!account.getCurrency().equals(request.currency())) throw new CurrencyMismatchException("Account currency is "+account.getCurrency()+", event currency is "+request.currency());
        BigDecimal signed=request.type()==TransactionType.CREDIT ? request.amount() : request.amount().negate();
        account.setBalance(account.getBalance().add(signed));
        var tx=new AccountTransaction(request.eventId(), accountId, request.type(), request.amount(), request.currency(), request.eventTimestamp());
        accounts.save(account); transactions.save(tx); appliedCounter.increment();
        log.info("Transaction applied eventId={} accountId={} balance={}", request.eventId(), accountId, account.getBalance());
        return map(tx);
    }
    @Transactional(readOnly=true)
    public BalanceResponse balance(String accountId){
        var a=accounts.findById(accountId).orElseThrow(()->new AccountNotFoundException(accountId));
        return new BalanceResponse(a.getAccountId(),a.getBalance(),a.getCurrency());
    }
    @Transactional(readOnly=true)
    public AccountResponse details(String accountId){
        var a=accounts.findById(accountId).orElseThrow(()->new AccountNotFoundException(accountId));
        return new AccountResponse(a.getAccountId(),a.getBalance(),a.getCurrency(),transactions.findTop20ByAccountIdOrderByEventTimestampDesc(accountId).stream().map(this::map).toList());
    }
    private TransactionResponse map(AccountTransaction t){return new TransactionResponse(t.getEventId(),t.getAccountId(),t.getType(),t.getAmount(),t.getCurrency(),t.getEventTimestamp(),t.getAppliedAt());}
}
