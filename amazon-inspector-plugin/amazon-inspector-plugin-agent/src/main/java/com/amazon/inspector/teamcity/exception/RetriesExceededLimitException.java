package com.amazon.inspector.teamcity.exception;

public class RetriesExceededLimitException extends Exception {
    public RetriesExceededLimitException(String errorMessage) {
        super(errorMessage);
    }
}
