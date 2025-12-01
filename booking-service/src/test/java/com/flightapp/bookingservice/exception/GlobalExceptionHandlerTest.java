package com.flightapp.bookingservice.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not Found");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test-url");

        ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("Not Found", response.getBody().getMessage());
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad Request");
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ApiError> response = handler.handleBadRequest(ex, request);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Bad Request", response.getBody().getMessage());
    }

    @Test
    void testHandleServiceException() {
        ServiceException ex = new ServiceException("Service Error");
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ApiError> response = handler.handleService(ex, request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Generic Error");
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ApiError> response = handler.handleAll(ex, request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}