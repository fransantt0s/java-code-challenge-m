package com.transactions.repository;

import com.transactions.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacenamiento en memoria con tres índices mantenidos en cada escritura:
 * lookup por id, ids por tipo, e ids hijos por padre. Las estructuras son
 * thread-safe; la atomicidad del check-then-act la garantiza el Service.
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> byId = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> idsByType = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> childrenByParent = new ConcurrentHashMap<>();

    @Override
    public boolean existsById(long id) {
        return byId.containsKey(id);
    }

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void save(Transaction transaction) {
        Transaction previous = byId.put(transaction.id(), transaction);

        if (previous != null && !previous.type().equals(transaction.type())) {
            Set<Long> oldTypeBucket = idsByType.get(previous.type());
            if (oldTypeBucket != null) {
                oldTypeBucket.remove(transaction.id());
            }
        }
        idsByType.computeIfAbsent(transaction.type(), t -> ConcurrentHashMap.newKeySet())
                .add(transaction.id());

        // parentId es inmutable, así que (re)agregar al índice de hijos es idempotente.
        if (transaction.parentId() != null) {
            childrenByParent.computeIfAbsent(transaction.parentId(), p -> ConcurrentHashMap.newKeySet())
                    .add(transaction.id());
        }
    }

    @Override
    public List<Long> findIdsByType(String type) {
        Set<Long> ids = idsByType.get(type);
        if (ids == null) {
            return List.of();
        }
        return ids.stream().sorted().toList();
    }

    @Override
    public Set<Long> findChildrenIds(long id) {
        Set<Long> ids = childrenByParent.get(id);
        return ids == null ? Set.of() : Set.copyOf(ids);
    }
}
