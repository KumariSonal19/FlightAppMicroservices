package com.flightapp.bookingservice.messaging;

import com.flightapp.bookingservice.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BookingPublisher bookingPublisher;

    @Test
    void testPublish_Success() {
        BookingEvent event = new BookingEvent();
        event.setPnr("PNR123");

        bookingPublisher.publishBookingConfirmation(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.BOOKING_EXCHANGE),
                eq(RabbitMQConfig.BOOKING_ROUTING_KEY),
                eq(event)
        );
    }

    // This tests the "if (rabbitTemplate == null)" block
    @Test
    void testPublish_NoRabbitTemplate() {
        // We create an instance manually WITHOUT injecting the RabbitTemplate mock
        BookingPublisher manualPublisher = new BookingPublisher();
        
        BookingEvent event = new BookingEvent();
        event.setPnr("PNR123");

        // This should run without throwing a NullPointerException
        manualPublisher.publishBookingConfirmation(event);
    }
}