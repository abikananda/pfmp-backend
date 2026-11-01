package com.techconsulting.pfmp.exception;

import com.techconsulting.pfmp.model.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@ControllerAdvice
public class FileUploadExceptionAdvice {

    // Handle max file size exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseMessage> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        log.error("File size exceeded the allowed limit", exc);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ResponseMessage("File size exceeds the allowed limit!"));
    }

    // Handle generic Multipart file upload errors
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResponseMessage> handleMultipartException(MultipartException exc) {
        log.error("Multipart file upload error", exc);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Invalid file upload request!"));
    }

    // Handle generic exceptions (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGenericException(Exception exc) {
        log.error("Unexpected error occurred", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("An unexpected error occurred."));
    }
}
