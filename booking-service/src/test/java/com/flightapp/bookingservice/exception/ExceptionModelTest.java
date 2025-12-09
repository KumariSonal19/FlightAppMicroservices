package com.flightapp.bookingservice.exception;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionModelTest {

    @Test
    void testServiceException() {
        ServiceException ex1 = new ServiceException("Error 1");
        assertEquals("Error 1", ex1.getMessage());
        RuntimeException cause = new RuntimeException("Root Cause");
        ServiceException ex2 = new ServiceException("Error 2", cause);
        assertEquals("Error 2", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not Found");
        assertEquals("Not Found", ex.getMessage());
    }

    @Test
    void testBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad Req");
        assertEquals("Bad Req", ex.getMessage());
    }

    @Test
    void testApiError() {
        Instant now = Instant.now();
        List<String> details = List.of("e1");
        ApiError error1 = new ApiError(now, 400, "Bad Request", "Message", "/path", details);
        ApiError error2 = new ApiError(now, 400, "Bad Request", "Message", "/path", details);
        assertEquals(400, error1.getStatus());
        assertEquals("Message", error1.getMessage());
        assertEquals("/path", error1.getPath());
        assertEquals(details, error1.getErrors());
        assertEquals(error1, error2); 
        assertEquals(error1.hashCode(), error2.hashCode());
        assertNotNull(error1.toString());
        ApiError error3 = new ApiError();
        error3.setStatus(500);
        assertEquals(500, error3.getStatus());
    }
}