package com.transactions.web;

import com.transactions.domain.Transaction;
import com.transactions.service.TransactionService;
import com.transactions.web.dto.StatusResponse;
import com.transactions.web.dto.SumResponse;
import com.transactions.web.dto.TransactionRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    public StatusResponse put(@PathVariable long id, @Valid @RequestBody TransactionRequest request) {
        service.upsert(new Transaction(id, request.amount(), request.type(), request.parentId()));
        return StatusResponse.ok();
    }

    @GetMapping("/types/{type}")
    public List<Long> byType(@PathVariable String type) {
        return service.findIdsByType(type);
    }

    @GetMapping("/sum/{id}")
    public SumResponse sum(@PathVariable long id) {
        return new SumResponse(service.sum(id));
    }
}
