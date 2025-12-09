package com.flightapp.bookingservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not Found");
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) handler.handleNotFound(ex, request);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody());
    }
    
    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Invalid input");
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) handler.handleBadRequest(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody()); 
        assertTrue(response.getBody().toString().contains("Invalid input"));
    }

    @Test
    void testHandleServiceException() {
        ServiceException ex = new ServiceException("Logic failed");
        
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) handler.handleService(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleAllExceptions() {
        Exception ex = new Exception("Crash");
        
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) handler.handleAll(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void testHandleServiceUnavailable() {
        ServiceUnavailableExceptionTest ex = new ServiceUnavailableExceptionTest("System Down");
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) handler.handleServiceUnavailable(ex, request);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    
}