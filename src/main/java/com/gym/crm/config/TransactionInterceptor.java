package com.gym.crm.config;

import com.gym.crm.util.TransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.MDC;

@Component("gymTransactionInterceptor")
@Slf4j
public class TransactionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String transactionId = TransactionContext.generateTransactionId();
        TransactionContext.setTransactionId(transactionId);
        
        // Add transaction ID to MDC for logging
        MDC.put("transactionId", transactionId);
        
        log.info("Transaction started [{}]: {} {}", transactionId, request.getMethod(), request.getRequestURI());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String transactionId = TransactionContext.getTransactionId();
        
        try {
            if (ex != null || response.getStatus() >= 400) {
                log.error("Transaction completed with error [{}]: {} {} - Status: {}, Error: {}", 
                        transactionId, request.getMethod(), request.getRequestURI(), response.getStatus(), 
                        ex != null ? ex.getMessage() : "HTTP Error");
            } else {
                log.info("Transaction completed [{}]: {} {} - Status: {}", 
                        transactionId, request.getMethod(), request.getRequestURI(), response.getStatus());
            }
        } finally {
            // Clean up
            TransactionContext.clear();
            MDC.clear();
        }
    }
}
