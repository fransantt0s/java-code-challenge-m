package com.transactions.service;

import com.transactions.domain.Transaction;
import com.transactions.repository.TransactionRepository;
import com.transactions.service.exception.ParentImmutableException;
import com.transactions.service.exception.ParentNotFoundException;
import com.transactions.service.exception.TransactionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DefaultTransactionService implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTransactionService.class);
    private static final int STRIPES = 64;

    private final TransactionRepository repository;
    private final ReentrantLock[] locks;

    public DefaultTransactionService(TransactionRepository repository) {
        this.repository = repository;
        this.locks = new ReentrantLock[STRIPES];
        for (int i = 0; i < STRIPES; i++) {
            this.locks[i] = new ReentrantLock();
        }
    }

    private ReentrantLock lockFor(long id) {
        return locks[Math.floorMod(Long.hashCode(id), STRIPES)];
    }

    @Override
    public void upsert(Transaction transaction) {
        if (transaction.parentId() != null && !repository.existsById(transaction.parentId())) {
            throw new ParentNotFoundException(transaction.parentId());
        }
        ReentrantLock lock = lockFor(transaction.id());
        lock.lock();
        boolean replaced;
        try {
            Optional<Transaction> existing = repository.findById(transaction.id());
            if (existing.isPresent()
                    && !Objects.equals(existing.get().parentId(), transaction.parentId())) {
                throw new ParentImmutableException(transaction.id());
            }
            replaced = existing.isPresent();
            repository.save(transaction);
        } finally {
            lock.unlock();
        }
        log.info("Transacción {} {} (type={})",
                transaction.id(), replaced ? "reemplazada" : "creada", transaction.type());
        log.debug("Detalle transacción {}: amount={}, parentId={}",
                transaction.id(), transaction.amount(), transaction.parentId());
    }

    @Override
    public List<Long> findIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    @Override
    public double sum(long id) {
        if (!repository.existsById(id)) {
            throw new TransactionNotFoundException(id);
        }
        log.debug("Calculando suma transitiva para {}", id);
        double total = 0d;
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(id);
        while (!stack.isEmpty()) {
            long current = stack.pop();
            total += repository.findById(current).map(Transaction::amount).orElse(0d);
            stack.addAll(repository.findChildrenIds(current));
        }
        log.debug("Suma transitiva de {} = {}", id, total);
        return total;
    }
}
