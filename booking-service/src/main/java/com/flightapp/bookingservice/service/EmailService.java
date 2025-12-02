package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.messaging.BookingEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public void sendBookingEmail(BookingEvent event) {
        String toEmail = event.getUserEmail();

        if (toEmail == null || !toEmail.contains("@")) {
            log.warn("Invalid email address provided: {}. Skipping email.", toEmail);
            return;
        }

        try {
            log.info("Attempting to send email to: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("YOUR_REAL_GMAIL@gmail.com"); 
            message.setTo(toEmail);
            message.setSubject("Flight Booking Confirmed - PNR: " + event.getPnr());
            
            String body = String.format("""
                Dear %s,
                
                Your flight booking is confirmed!
                PNR: %s
                Flight ID: %s
                Seats: %d
                Total Price: $%.2f
                
                Have a safe journey!
                """, 
                event.getUserName(), event.getPnr(), event.getFlightId(), event.getNumberOfSeats(), event.getTotalPrice());

            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to {}. Reason: {}", toEmail, e.getMessage());
        }
    }
}