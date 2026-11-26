package com.example.agroflowbackend.dto;

import java.util.List;

public class ApiErrorResponse {
    private int status;
    private String message;
    private List<FieldErrorResponse> errors;

    public ApiErrorResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public ApiErrorResponse(int status, String message, List<FieldErrorResponse> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldErrorResponse> getErrors() {
        return errors;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrors(List<FieldErrorResponse> errors) {
        this.errors = errors;
    }
}
