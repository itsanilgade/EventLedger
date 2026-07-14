package com.eventledger.gateway.config;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.slf4j.MDC; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.UUID;
@Component
public class TraceFilter extends OncePerRequestFilter {
 public static final String HEADER="X-Trace-Id";
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String id=req.getHeader(HEADER); if(id==null||id.isBlank())id=UUID.randomUUID().toString(); MDC.put("traceId",id);res.setHeader(HEADER,id);
  try{chain.doFilter(req,res);}finally{MDC.remove("traceId");}
 }
}
