package com.transactions.service;

import com.transactions.domain.Transaction;
import com.transactions.repository.InMemoryTransactionRepository;
import com.transactions.service.exception.ParentImmutableException;
import com.transactions.service.exception.ParentNotFoundException;
import com.transactions.service.exception.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultTransactionServiceTest {

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new DefaultTransactionService(new InMemoryTransactionRepository());
    }

    @Test
    void createsRootTransaction() {
        service.upsert(new Transaction(10L, 5000d, "cars", null));

        assertThat(service.findIdsByType("cars")).containsExactly(10L);
        assertThat(service.sum(10L)).isEqualTo(5000d);
    }

    @Test
    void rejectsTransactionWhoseParentDoesNotExist() {
        assertThatThrownBy(() -> service.upsert(new Transaction(11L, 1d, "shopping", 10L)))
                .isInstanceOf(ParentNotFoundException.class);
    }

    @Test
    void replaceIsIdempotentAndUpdatesAmountAndType() {
        service.upsert(new Transaction(10L, 5000d, "cars", null));
        service.upsert(new Transaction(10L, 7000d, "shopping", null));

        assertThat(service.sum(10L)).isEqualTo(7000d);
        assertThat(service.findIdsByType("cars")).isEmpty();
        assertThat(service.findIdsByType("shopping")).containsExactly(10L);
    }

    @Test
    void rejectsChangingParentIdOnReplace() {
        service.upsert(new Transaction(10L, 5000d, "cars", null));
        service.upsert(new Transaction(20L, 1d, "cars", null));
        service.upsert(new Transaction(11L, 1d, "shopping", 10L));

        assertThatThrownBy(() -> service.upsert(new Transaction(11L, 1d, "shopping", 20L)))
                .isInstanceOf(ParentImmutableException.class);
    }

    @Test
    void allowsReplaceWhenParentIdIsUnchanged() {
        service.upsert(new Transaction(10L, 5000d, "cars", null));
        service.upsert(new Transaction(11L, 1d, "shopping", 10L));

        service.upsert(new Transaction(11L, 2d, "shopping", 10L)); // mismo parent

        assertThat(service.sum(11L)).isEqualTo(2d);
    }

    @Test
    void sumIncludesAllTransitiveDescendants() {
        // Ejemplo del PDF: 10 -> 11 -> 12
        service.upsert(new Transaction(10L, 5000d, "cars", null));
        service.upsert(new Transaction(11L, 10000d, "shopping", 10L));
        service.upsert(new Transaction(12L, 5000d, "shopping", 11L));

        assertThat(service.sum(10L)).isEqualTo(20000d);
        assertThat(service.sum(11L)).isEqualTo(15000d);
        assertThat(service.sum(12L)).isEqualTo(5000d);
    }

    @Test
    void sumHandlesDeepChainsWithoutStackOverflow() {
        service.upsert(new Transaction(0L, 1d, "chain", null));
        for (long i = 1; i <= 100_000; i++) {
            service.upsert(new Transaction(i, 1d, "chain", i - 1));
        }

        assertThat(service.sum(0L)).isEqualTo(100_001d);
    }

    @Test
    void sumThrowsWhenTransactionDoesNotExist() {
        assertThatThrownBy(() -> service.sum(999L))
                .isInstanceOf(TransactionNotFoundException.class);
    }
}
