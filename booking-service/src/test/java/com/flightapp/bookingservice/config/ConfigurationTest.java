package com.flightapp.bookingservice.config;

import feign.Logger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationTest {

    @Test
    void testFeignConfig() {
        FeignClientConfig feignConf = new FeignClientConfig();
        assertEquals(Logger.Level.FULL, feignConf.feignLoggerLevel());
    }

    @Test
    void testRabbitConfig() {
        RabbitMQConfig rabbitConf = new RabbitMQConfig();
        
        assertNotNull(rabbitConf.bookingQueue());
        assertNotNull(rabbitConf.bookingExchange());
        assertNotNull(rabbitConf.bookingBinding(
            rabbitConf.bookingQueue(), 
            rabbitConf.bookingExchange()
        ));
        
        assertNotNull(rabbitConf.jackson2MessageConverter());
    }
}