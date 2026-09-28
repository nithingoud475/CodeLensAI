package com.codelens.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> response = new HashMap<>();
        String message = ex.getMessage();
        
        if (message != null && message.contains("Failed to communicate with local LLM")) {
            response.put("error", "Unable to perform AI review. The local AI service is currently unavailable or returned an invalid response. Please make sure Ollama is running and the model is pulled.");
            return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }
        
        response.put("error", "An unexpected error occurred: " + message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseExceptions(DataAccessException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "A database error occurred. Please ensure MySQL is running and accessible.");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
