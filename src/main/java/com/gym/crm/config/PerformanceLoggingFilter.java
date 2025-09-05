package com.gym.crm.config;

import com.gym.crm.util.TransactionContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to log performance metrics for API requests
 */
@Slf4j
public class PerformanceLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String transactionId = TransactionContext.getTransactionId();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();
            
            // Log performance metrics
            if (duration > 1000) { // Slow requests (>1s)
                log.warn("SLOW_REQUEST [{}]: {} {} {} - Status: {}, Duration: {}ms", 
                        transactionId, method, uri, 
                        queryString != null ? "?" + queryString : "", 
                        status, duration);
            } else if (duration > 500) { // Moderate requests (>500ms)
                log.info("MODERATE_REQUEST [{}]: {} {} {} - Status: {}, Duration: {}ms", 
                        transactionId, method, uri, 
                        queryString != null ? "?" + queryString : "", 
                        status, duration);
            } else {
                log.debug("FAST_REQUEST [{}]: {} {} {} - Status: {}, Duration: {}ms", 
                        transactionId, method, uri, 
                        queryString != null ? "?" + queryString : "", 
                        status, duration);
            }
            
            // Log error responses
            if (status >= 400) {
                if (status >= 500) {
                    log.error("SERVER_ERROR [{}]: {} {} {} - Status: {}, Duration: {}ms", 
                            transactionId, method, uri, 
                            queryString != null ? "?" + queryString : "", 
                            status, duration);
                } else {
                    log.warn("CLIENT_ERROR [{}]: {} {} {} - Status: {}, Duration: {}ms", 
                            transactionId, method, uri, 
                            queryString != null ? "?" + queryString : "", 
                            status, duration);
                }
            }
        }
    }
}
