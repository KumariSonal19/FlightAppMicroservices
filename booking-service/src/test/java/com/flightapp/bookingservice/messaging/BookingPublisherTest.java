package com.flightapp.bookingservice.messaging;

import com.flightapp.bookingservice.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingPublisher bookingPublisher;

    @Test
    void testPublish() {
        BookingEvent event = new BookingEvent();
        event.setPnr("PNR123");
        assertDoesNotThrow(() -> bookingPublisher.publishBookingConfirmation(event));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.BOOKING_EXCHANGE),
                eq(RabbitMQConfig.BOOKING_ROUTING_KEY),
                eq(event)
        );
    }
    
    @Test
    void testPublish_NoRabbit() {
        BookingPublisher pub = new BookingPublisher();
        assertDoesNotThrow(() -> pub.publishBookingConfirmation(new BookingEvent()));
    }
}