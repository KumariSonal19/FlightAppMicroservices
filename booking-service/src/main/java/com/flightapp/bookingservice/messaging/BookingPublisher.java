package com.flightapp.bookingservice.messaging;

import com.flightapp.bookingservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingPublisher.class);

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public void publishBookingConfirmation(BookingEvent event) {
        if (rabbitTemplate == null) {
            log.info("RabbitTemplate not available, skipping publish for PNR={}", event.getPnr());
            return;
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_ROUTING_KEY,
                    event);
            log.info("Published booking confirmation for PNR={}", event.getPnr());
        } catch (Exception ex) {
            log.error("Failed to publish booking confirmation for PNR={}: {}", event.getPnr(), ex.getMessage(), ex);
            
        }
    }
}
