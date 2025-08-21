package com.gym.crm.util;

import java.util.UUID;

public class TransactionContext {
    
    private static final ThreadLocal<String> transactionIdHolder = new ThreadLocal<>();
    
    public static void setTransactionId(String transactionId) {
        transactionIdHolder.set(transactionId);
    }
    
    public static String getTransactionId() {
        return transactionIdHolder.get();
    }
    
    public static String generateTransactionId() {
        return "txn-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static void clear() {
        transactionIdHolder.remove();
    }
}
