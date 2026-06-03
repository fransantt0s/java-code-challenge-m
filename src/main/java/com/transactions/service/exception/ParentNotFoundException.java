package com.transactions.service.exception;

public class ParentNotFoundException extends RuntimeException {
    public ParentNotFoundException(long parentId) {
        super("parent_id " + parentId + " no existe");
    }
}
