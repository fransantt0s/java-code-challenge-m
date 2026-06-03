package com.transactions.service.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(long id) {
        super("la transacción " + id + " no existe");
    }
}
