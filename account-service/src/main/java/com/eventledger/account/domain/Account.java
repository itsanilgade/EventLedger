package com.eventledger.account.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private String accountId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false)
    private String currency;
    @Version
    private long version;

    protected Account() {}
    public Account(String accountId, String currency) { this.accountId = accountId; this.currency = currency; }
    public String getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
