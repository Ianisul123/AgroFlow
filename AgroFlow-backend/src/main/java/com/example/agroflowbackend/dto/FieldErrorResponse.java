package com.example.agroflowbackend.dto;

public class FieldErrorResponse {
    private String field;
    private String message;

    public FieldErrorResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public FieldErrorResponse(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
