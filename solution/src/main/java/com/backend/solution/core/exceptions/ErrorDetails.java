package com.backend.solution.core.exceptions;

import lombok.Data;

@Data
public class ErrorDetails {
    private int code;
    private String message;
}
