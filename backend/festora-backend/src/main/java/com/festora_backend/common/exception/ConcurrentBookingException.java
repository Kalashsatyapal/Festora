package com.festora_backend.common.exception;

public class ConcurrentBookingException extends RuntimeException {

    public ConcurrentBookingException() {
        super("Ticket inventory changed. Please retry checkout.");
    }
}