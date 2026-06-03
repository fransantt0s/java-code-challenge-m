package com.transactions.service;

import com.transactions.domain.Transaction;

import java.util.List;

public interface TransactionService {

    /** Crea (id nuevo) o reemplaza amount/type (id existente). parent_id inmutable. */
    void upsert(Transaction transaction);

    List<Long> findIdsByType(String type);

    /** Suma del monto de la transacción más todos sus descendientes transitivos. */
    double sum(long id);
}
