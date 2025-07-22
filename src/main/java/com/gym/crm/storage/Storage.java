package com.gym.crm.storage;

import java.util.Map;
import java.util.Optional;

public interface Storage<T> {
    Long save(T entity);
    Optional<T> findById(Long id);
    Map<Long, T> findAll();
    void update(Long id, T entity);
    void delete(Long id);
    Long getNextId();
} 