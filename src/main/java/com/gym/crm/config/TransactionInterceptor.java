package com.gym.crm.config;

import com.gym.crm.util.TransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component("gymTransactionInterceptor")
@Slf4j
public class TransactionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String transactionId = TransactionContext.generateTransactionId();
        TransactionContext.setTransactionId(transactionId);
        
        log.info("Transaction started [{}]: {} {}", transactionId, request.getMethod(), request.getRequestURI());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String transactionId = TransactionContext.getTransactionId();
        
        if (ex != null) {
            log.error("Transaction completed with error [{}]: {} {} - Status: {}, Error: {}", 
                    transactionId, request.getMethod(), request.getRequestURI(), response.getStatus(), ex.getMessage());
        } else {
            log.info("Transaction completed [{}]: {} {} - Status: {}", 
                    transactionId, request.getMethod(), request.getRequestURI(), response.getStatus());
        }
        
        TransactionContext.clear();
    }
}
