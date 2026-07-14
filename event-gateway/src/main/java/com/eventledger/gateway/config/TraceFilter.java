package com.eventledger.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Trace-Id";
    public static final String REQUEST_ATTRIBUTE = TraceFilter.class.getName() + ".traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String traceId = request.getHeader(HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        request.setAttribute(REQUEST_ATTRIBUTE, traceId);
        response.setHeader(HEADER, traceId);
        MDC.put("traceId", traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }
}