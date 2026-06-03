package com.transactions.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void putTransaction(long id, String body) throws Exception {
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void replicatesPdfAcceptanceExample() throws Exception {
        putTransaction(10L, "{\"amount\":5000,\"type\":\"cars\"}");
        putTransaction(11L, "{\"amount\":10000,\"type\":\"shopping\",\"parent_id\":10}");
        putTransaction(12L, "{\"amount\":5000,\"type\":\"shopping\",\"parent_id\":11}");

        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(content().json("[10]"));

        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000d));

        mockMvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(15000d));
    }

    @Test
    void unknownTypeReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/transactions/types/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void parentThatDoesNotExistReturns422() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 50L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1,\"type\":\"x\",\"parent_id\":999}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("parent_not_found"));
    }

    @Test
    void changingParentIdReturns409() throws Exception {
        putTransaction(60L, "{\"amount\":1,\"type\":\"x\"}");
        putTransaction(61L, "{\"amount\":1,\"type\":\"x\"}");
        putTransaction(62L, "{\"amount\":1,\"type\":\"x\",\"parent_id\":60}");

        mockMvc.perform(put("/transactions/{id}", 62L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1,\"type\":\"x\",\"parent_id\":61}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("parent_immutable"));
    }

    @Test
    void sumOfUnknownTransactionReturns404() throws Exception {
        mockMvc.perform(get("/transactions/sum/{id}", 12345L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("transaction_not_found"));
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 70L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"x\"}")) // falta amount
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }
}
