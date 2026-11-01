package com.techconsulting.pfmp.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Returns 400 Bad Request if thrown in a Spring controller
@NoArgsConstructor
public class InvestmentException extends RuntimeException {

    public InvestmentException(String message) {
        super(message); // Call the parent Exception class
    }

    public InvestmentException(String message, Throwable cause) {
        super(message, cause); // Allow wrapping another exception
    }
}