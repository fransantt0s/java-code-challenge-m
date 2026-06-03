package com.transactions.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * amount y type son obligatorios; parent_id es opcional (null => transacción raíz).
 * El JSON usa snake_case (parent_id) de forma explícita, de modo que el contrato
 * y el schema OpenAPI coincidan.
 */
public record TransactionRequest(
        @NotNull Double amount,
        @NotBlank String type,
        @JsonProperty("parent_id") Long parentId) {
}
