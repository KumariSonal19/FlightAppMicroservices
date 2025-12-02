package com.flightapp.bookingservice.messaging;

import com.flightapp.bookingservice.config.RabbitMQConfig;
import com.flightapp.bookingservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingListener {

    @Autowired
    private EmailService emailService;
    
    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void handleBookingEvent(BookingEvent event) {
        log.info("Received Booking Event for PNR: {}", event.getPnr());
        emailService.sendBookingEmail(event);
    }
}