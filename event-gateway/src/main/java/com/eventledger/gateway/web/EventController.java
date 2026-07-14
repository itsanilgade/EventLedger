package com.eventledger.gateway.web;
import com.eventledger.gateway.api.*; import com.eventledger.gateway.service.*; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController
public class EventController {
 private final EventApplicationService service; public EventController(EventApplicationService service){this.service=service;}
 @PostMapping("/events") public ResponseEntity<EventResponse> submit(@Valid @RequestBody SubmitEventRequest request){var result=service.submit(request);return ResponseEntity.status(result.duplicate()?HttpStatus.OK:HttpStatus.CREATED).header("Idempotent-Replay",Boolean.toString(result.duplicate())).body(result.event());}
 @GetMapping("/events/{id}") public EventResponse get(@PathVariable String id){return service.get(id);}
 @GetMapping("/events") public List<EventResponse> list(@RequestParam("account") String account){return service.list(account);}
}
