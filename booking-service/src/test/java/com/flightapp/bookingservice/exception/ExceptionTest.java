package com.flightapp.bookingservice.exception;

import com.flightapp.bookingservice.config.FeignClientConfig;
import com.flightapp.bookingservice.config.RabbitMQConfig;
import feign.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not Found");
        MockHttpServletRequest req = new MockHttpServletRequest();
        ResponseEntity<ApiError> response = handler.handleNotFound(ex, req);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad Request");
        MockHttpServletRequest req = new MockHttpServletRequest();
        ResponseEntity<ApiError> response = handler.handleBadRequest(ex, req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleServiceException() {
        ServiceException ex = new ServiceException("Service Error");
        MockHttpServletRequest req = new MockHttpServletRequest();
        ResponseEntity<ApiError> response = handler.handleService(ex, req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
    
    @Test
    void testHandleAll() {
        Exception ex = new Exception("General Error");
        MockHttpServletRequest req = new MockHttpServletRequest();
        ResponseEntity<ApiError> response = handler.handleAll(ex, req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
    
    @Test
    void testConfigs() {
        FeignClientConfig feignConf = new FeignClientConfig();
        assertEquals(Logger.Level.FULL, feignConf.feignLoggerLevel());
        
        RabbitMQConfig rabbitConf = new RabbitMQConfig();
        assertNotNull(rabbitConf.bookingQueue());
        assertNotNull(rabbitConf.bookingExchange());
        assertNotNull(rabbitConf.bookingBinding(rabbitConf.bookingQueue(), rabbitConf.bookingExchange()));
        assertNotNull(rabbitConf.jackson2MessageConverter());
    }
}