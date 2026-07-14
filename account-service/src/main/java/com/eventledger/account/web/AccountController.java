package com.eventledger.account.web;
import com.eventledger.account.api.*;
import com.eventledger.account.service.AccountApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountApplicationService service;
    public AccountController(AccountApplicationService service){this.service=service;}
    @PostMapping("/{accountId}/transactions")
    public ResponseEntity<TransactionResponse> apply(@PathVariable String accountId,@Valid @RequestBody ApplyTransactionRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.apply(accountId,request));}
    @GetMapping("/{accountId}/balance") public BalanceResponse balance(@PathVariable String accountId){return service.balance(accountId);}
    @GetMapping("/{accountId}") public AccountResponse details(@PathVariable String accountId){return service.details(accountId);}
}
