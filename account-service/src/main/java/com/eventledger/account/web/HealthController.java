package com.eventledger.account.web;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
@RestController
public class HealthController {
 private final JdbcTemplate jdbc; public HealthController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 @GetMapping("/health") public Map<String,Object> health(){Integer one=jdbc.queryForObject("select 1",Integer.class);return Map.of("status","UP","service","account-service","database",one!=null&&one==1?"UP":"DOWN","timestamp", Instant.now());}
}
