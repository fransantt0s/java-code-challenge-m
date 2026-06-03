package com.transactions.web;

import com.transactions.service.exception.ParentImmutableException;
import com.transactions.service.exception.ParentNotFoundException;
import com.transactions.service.exception.TransactionNotFoundException;
import com.transactions.web.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ParentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleParentNotFound(ParentNotFoundException ex) {
        log.warn("422 parent_not_found: {}", ex.getMessage());
        return ResponseEntity.unprocessableEntity()
                .body(new ErrorResponse("parent_not_found", ex.getMessage()));
    }

    @ExceptionHandler(ParentImmutableException.class)
    public ResponseEntity<ErrorResponse> handleParentImmutable(ParentImmutableException ex) {
        log.warn("409 parent_immutable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("parent_immutable", ex.getMessage()));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TransactionNotFoundException ex) {
        log.warn("404 transaction_not_found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("transaction_not_found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .findFirst()
                .orElse("request inválido");
        log.warn("400 validation_error: {}", detail);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("validation_error", detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("400 malformed_request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("malformed_request",
                        "JSON inválido o con campos desconocidos (se esperan: amount, type, parent_id)"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("400 validation_error: parámetro '{}' con tipo inválido", ex.getName());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("validation_error",
                        "parámetro '" + ex.getName() + "' con tipo inválido"));
    }

    /**
     * Catch-all. Las excepciones estándar de Spring MVC (405, 415, etc.) implementan
     * {@link org.springframework.web.ErrorResponse}: preservamos su status en vez de
     * degradarlas a 500. Lo verdaderamente inesperado se loguea como ERROR y devuelve 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse framework) {
            log.warn("{} request_error: {}", framework.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(framework.getStatusCode())
                    .body(new ErrorResponse("request_error", ex.getMessage()));
        }
        log.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("internal_error", "Error interno"));
    }
}
