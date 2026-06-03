package com.transactions.web;

import com.transactions.service.exception.ParentImmutableException;
import com.transactions.service.exception.ParentNotFoundException;
import com.transactions.service.exception.TransactionNotFoundException;
import com.transactions.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ParentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleParentNotFound(ParentNotFoundException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(new ErrorResponse("parent_not_found", ex.getMessage()));
    }

    @ExceptionHandler(ParentImmutableException.class)
    public ResponseEntity<ErrorResponse> handleParentImmutable(ParentImmutableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("parent_immutable", ex.getMessage()));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("transaction_not_found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .findFirst()
                .orElse("request inválido");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("validation_error", detail));
    }
}
