package com.flightapp.bookingservice.messaging;

import com.flightapp.bookingservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingListener bookingListener;

    @Test
    void testHandleBookingEvent() {
        BookingEvent event = new BookingEvent();
        event.setPnr("PNR123");
        assertDoesNotThrow(() -> bookingListener.handleBookingEvent(event));

        verify(emailService).sendBookingEmail(event);
    }
}