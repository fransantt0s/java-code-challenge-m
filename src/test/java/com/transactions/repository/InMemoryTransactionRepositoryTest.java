package com.transactions.repository;

import com.transactions.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTransactionRepositoryTest {

    private InMemoryTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void savesAndFindsById() {
        repository.save(new Transaction(10L, 5000d, "cars", null));

        assertThat(repository.existsById(10L)).isTrue();
        assertThat(repository.findById(10L))
                .contains(new Transaction(10L, 5000d, "cars", null));
    }

    @Test
    void existsByIdIsFalseForUnknownId() {
        assertThat(repository.existsById(999L)).isFalse();
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findIdsByTypeReturnsSortedIds() {
        repository.save(new Transaction(12L, 1d, "shopping", null));
        repository.save(new Transaction(11L, 1d, "shopping", null));
        repository.save(new Transaction(10L, 1d, "cars", null));

        assertThat(repository.findIdsByType("shopping")).containsExactly(11L, 12L);
        assertThat(repository.findIdsByType("cars")).containsExactly(10L);
    }

    @Test
    void findIdsByTypeReturnsEmptyListForUnknownType() {
        assertThat(repository.findIdsByType("unknown")).isEmpty();
    }

    @Test
    void reindexesTypeWhenTransactionTypeChangesOnReplace() {
        repository.save(new Transaction(10L, 5000d, "cars", null));
        repository.save(new Transaction(10L, 5000d, "shopping", null));

        assertThat(repository.findIdsByType("cars")).isEmpty();
        assertThat(repository.findIdsByType("shopping")).containsExactly(10L);
    }

    @Test
    void indexesChildrenByParent() {
        repository.save(new Transaction(10L, 5000d, "cars", null));
        repository.save(new Transaction(11L, 1d, "shopping", 10L));
        repository.save(new Transaction(12L, 1d, "shopping", 10L));

        assertThat(repository.findChildrenIds(10L)).containsExactlyInAnyOrder(11L, 12L);
        assertThat(repository.findChildrenIds(11L)).isEmpty();
    }
}
