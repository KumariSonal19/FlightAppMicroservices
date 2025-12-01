package com.flightapp.flightservice.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not Found");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);
        
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("Not Found", response.getBody().get("message"));
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad Request");
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);
        
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Bad Request", response.getBody().get("message"));
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new Exception("Internal Error");
        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(ex);
        
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertEquals("Internal Error", response.getBody().get("message"));
    }
}