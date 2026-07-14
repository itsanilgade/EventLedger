package com.eventledger.gateway.web;
import com.eventledger.gateway.api.BalanceResponse; import com.eventledger.gateway.client.AccountServiceClient; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/accounts")
public class AccountProxyController { private final AccountServiceClient client; public AccountProxyController(AccountServiceClient client){this.client=client;} @GetMapping("/{id}/balance") public BalanceResponse balance(@PathVariable String id){return client.balance(id);} @GetMapping("/{id}") public Map details(@PathVariable String id){return client.details(id);} }
