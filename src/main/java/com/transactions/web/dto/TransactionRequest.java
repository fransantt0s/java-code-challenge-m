package com.transactions.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** amount y type son obligatorios; parent_id es opcional (null => transacción raíz). */
public record TransactionRequest(
        @NotNull Double amount,
        @NotBlank String type,
        Long parentId) {
}
