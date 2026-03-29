package com.example.ari.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidConfiguration(InvalidConfigurationException e) {
        log.error("Configuration error: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "CONFIGURATION_ERROR",
                e.getMessage()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        log.warn("Business error: code={}, message={}", e.getCode(), e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                e.getStatus().value(),
                e.getCode(),
                e.getMessage()
        );
        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleRestClientResponse(RestClientResponseException e) {
        log.error("ARI REST 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "ARI_CLIENT_ERROR",
                "ARI 서버와 통신 중 오류가 발생했습니다"
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                e.getMessage()
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
