package com.eventledger.gateway.web;
import com.eventledger.gateway.client.AccountServiceUnavailableException; import com.eventledger.gateway.service.EventNotFoundException; import com.eventledger.gateway.service.EventConflictException; import org.slf4j.MDC; import org.springframework.http.*; import org.springframework.http.converter.HttpMessageNotReadableException; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(EventNotFoundException.class) ResponseEntity<?> notFound(RuntimeException e){return error(HttpStatus.NOT_FOUND,e.getMessage());}
 @ExceptionHandler(EventConflictException.class) ResponseEntity<?> conflict(RuntimeException e){return error(HttpStatus.CONFLICT,e.getMessage());}
 @ExceptionHandler(AccountServiceUnavailableException.class) ResponseEntity<?> unavailable(RuntimeException e){return error(HttpStatus.SERVICE_UNAVAILABLE,e.getMessage());}
 @ExceptionHandler({MethodArgumentNotValidException.class}) ResponseEntity<?> validation(MethodArgumentNotValidException e){return error(HttpStatus.BAD_REQUEST,e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).reduce((a,b)->a+", "+b).orElse("Validation failed"));}
 @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<?> malformed(Exception e){return error(HttpStatus.BAD_REQUEST,"Malformed JSON or unsupported event type/timestamp");}
 @ExceptionHandler(Exception.class) ResponseEntity<?> other(Exception e){return error(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error");}
 private ResponseEntity<?> error(HttpStatus status,String message){return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now(),"status",status.value(),"error",status.getReasonPhrase(),"message",message,"traceId",Objects.toString(MDC.get("traceId"),"")));}
}
