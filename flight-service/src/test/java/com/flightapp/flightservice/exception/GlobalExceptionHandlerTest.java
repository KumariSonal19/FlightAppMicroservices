package com.flightapp.flightservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, Object>> res = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("Not found", res.getBody().get("message"));
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<Map<String, Object>> res = handler.handleBadRequest(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("Bad request", res.getBody().get("message"));
    }

    @Test
    void testHandleGlobal() {
        Exception ex = new Exception("Error");
        ResponseEntity<Map<String, Object>> res = handler.handleGlobalException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
    }
}