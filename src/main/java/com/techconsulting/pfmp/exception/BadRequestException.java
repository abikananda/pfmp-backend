package com.techconsulting.pfmp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@Getter
@RequiredArgsConstructor
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6958128182491079251L;

    private String field; // Renamed from 'value' for clarity

    public BadRequestException(String message) {
        super(message); // Use RuntimeException's built-in message handling
    }

    public BadRequestException(String field, String message) {
        super(message);
        this.field = field;
    }
}
