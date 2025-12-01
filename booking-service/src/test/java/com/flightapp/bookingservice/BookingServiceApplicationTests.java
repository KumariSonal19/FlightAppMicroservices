package com.flightapp.bookingservice;

import com.flightapp.bookingservice.service.BookingService;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.feign.FlightServiceClient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookingServiceApplicationTests {

    // mock external dependencies so they don't try to connect
    @MockBean
    private FlightServiceClient flightServiceClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Test
    void contextLoads() {
        assertThat(bookingService).isNotNull();
    }
}
