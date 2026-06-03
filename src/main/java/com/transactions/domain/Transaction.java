package com.transactions.domain;

/**
 * Transacción inmutable. {@code parentId} es null para transacciones raíz.
 * El vínculo al padre es estructural e inmutable una vez creado.
 */
public record Transaction(long id, double amount, String type, Long parentId) {
}
