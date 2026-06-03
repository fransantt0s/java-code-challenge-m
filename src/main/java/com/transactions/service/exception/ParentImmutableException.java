package com.transactions.service.exception;

public class ParentImmutableException extends RuntimeException {
    public ParentImmutableException(long id) {
        super("el parent_id de la transacción " + id + " es inmutable y no puede cambiarse");
    }
}
