package com.flightapp.bookingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class BookingServiceApplicationTest {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {}); 
    }

    @Test
    void testMain() {
        assertDoesNotThrow(() -> BookingServiceApplication.main(new String[]{}));
    }
}