package com.aloneinabyss.lovelace.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    /**
     * Machine-readable error code for frontend error handling.
     * Examples: "TOKEN_EXPIRED", "INVALID_TOKEN", "TOKEN_MALFORMED"
     */
    private String errorCode;
    
}
