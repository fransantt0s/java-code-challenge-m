package com.transactions.service;

import com.transactions.domain.Transaction;
import com.transactions.repository.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultTransactionServiceConcurrencyTest {

    @Test
    void concurrentChildrenUnderSameParentAreAllCounted() throws InterruptedException {
        TransactionService service = new DefaultTransactionService(new InMemoryTransactionRepository());
        service.upsert(new Transaction(0L, 0d, "root", null));

        int children = 1_000;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        for (int i = 1; i <= children; i++) {
            final long id = i;
            pool.submit(() -> service.upsert(new Transaction(id, 1d, "leaf", 0L)));
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        // root(0) + 1000 hijos de monto 1 => 1000
        assertThat(service.sum(0L)).isEqualTo(1_000d);
        assertThat(service.findIdsByType("leaf")).hasSize(children);
    }
}
