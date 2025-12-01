//package com.flightapp.bookingservice.messaging;
//
//import com.flightapp.bookingservice.config.RabbitMQConfig;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class BookingPublisher {
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    /**
//     * Publish booking confirmation event
//     */
//    public void publishBookingConfirmation(BookingEvent event) {
//        log.info("Publishing booking confirmation event for PNR: {}", event.getPnr());
//        rabbitTemplate.convertAndSend(
//            RabbitMQConfig.BOOKING_EXCHANGE,
//            RabbitMQConfig.BOOKING_ROUTING_KEY,
//            event
//        );
//        log.info("Booking confirmation event published successfully");
//    }
//}

package com.flightapp.bookingservice.messaging;

import com.flightapp.bookingservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publishes booking events to RabbitMQ if a RabbitTemplate/ConnectionFactory is present.
 * The RabbitTemplate is injected as optional (required = false) so the application can
 * start without a RabbitMQ broker for local/e2e checks.
 */
@Component
public class BookingPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingPublisher.class);

    // Optional injection — will be null when no RabbitMQ ConnectionFactory exists
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public void publishBookingConfirmation(BookingEvent event) {
        if (rabbitTemplate == null) {
            // No RabbitMQ available in this environment — skip publishing
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
            // don't rethrow — we want the app to continue running even if publishing fails
        }
    }
}
