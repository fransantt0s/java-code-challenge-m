package com.transactions.repository;

import com.transactions.domain.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionRepository {

    boolean existsById(long id);

    Optional<Transaction> findById(long id);

    /** Crea o reemplaza la transacción, manteniendo los índices internos. */
    void save(Transaction transaction);

    /** Ids del tipo dado, en orden ascendente determinista; lista vacía si no hay. */
    List<Long> findIdsByType(String type);

    /** Snapshot de los ids hijos directos de {@code id}; conjunto vacío si no hay. */
    Set<Long> findChildrenIds(long id);
}
