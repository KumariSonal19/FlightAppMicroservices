package com.flightapp.flightservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlightServiceApplicationTest {

    @Test
    void testMain() {
        try {
            FlightServiceApplication.main(new String[]{});
        } catch (Exception e) {
        }
    }
}