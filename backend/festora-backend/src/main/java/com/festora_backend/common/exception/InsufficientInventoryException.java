package com.festora_backend.common.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException() {
        super("Insufficient ticket inventory");
    }
}