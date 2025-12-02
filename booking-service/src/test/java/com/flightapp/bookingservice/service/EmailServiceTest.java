package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.messaging.BookingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendEmail_Success() {
        BookingEvent event = new BookingEvent();
        event.setUserEmail("valid@test.com");
        event.setPnr("PNR123");
        event.setUserName("John");
        event.setNumberOfSeats(1);
        event.setTotalPrice(100.0);
        event.setFlightId("F1");
        assertDoesNotThrow(() -> emailService.sendBookingEmail(event));
        
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_Invalid() {
        BookingEvent event = new BookingEvent();
        event.setUserEmail("invalid-email-string"); 
        assertDoesNotThrow(() -> emailService.sendBookingEmail(event));
        
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_Exception() {
        BookingEvent event = new BookingEvent();
        event.setUserEmail("error@test.com");
        
        doThrow(new RuntimeException("Mail Error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendBookingEmail(event));
    }
}