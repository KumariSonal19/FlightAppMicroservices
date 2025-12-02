package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.dto.PassengerRequest;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.feign.FlightDTO;
import com.flightapp.bookingservice.feign.FlightServiceClient;
import com.flightapp.bookingservice.messaging.BookingEvent;
import com.flightapp.bookingservice.messaging.BookingPublisher;
import com.flightapp.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightServiceClient flightServiceClient;

    @Mock
    private BookingPublisher bookingPublisher;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void testBookFlight_Success() {
        BookingRequest request = new BookingRequest();
        request.setFlightId("FL123");
        request.setNumberOfSeats(2);
        request.setJourneyDate(LocalDate.now().plusDays(5));
        request.setUserEmail("test@test.com");
        request.setUserName("John Doe");
        request.setPassengers(List.of(new PassengerRequest("John", "M", 30)));
        request.setSelectedSeats(List.of("1A", "1B"));

        FlightDTO mockFlight = new FlightDTO();
        mockFlight.setFlightId("FL123");
        mockFlight.setPrice(100.0);
        mockFlight.setAvailableSeats(10); 

        when(flightServiceClient.getFlightById("FL123")).thenReturn(mockFlight);
        
        Booking savedBooking = new Booking();
        savedBooking.setPnr("PNR123");
        savedBooking.setBookingStatus("CONFIRMED");
        savedBooking.setTotalPrice(200.0);
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponse response = bookingService.bookFlight(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("CONFIRMED", response.getBookingStatus());
        Assertions.assertEquals("PNR123", response.getPnr());

        verify(flightServiceClient, times(1)).updateFlightSeats("FL123", 2);
        verify(bookingPublisher, times(1)).publishBookingConfirmation(any(BookingEvent.class));
    }

    @Test
    void testBookFlight_InsufficientSeats() {
        BookingRequest request = new BookingRequest();
        request.setFlightId("FL123");
        request.setNumberOfSeats(5);

        FlightDTO mockFlight = new FlightDTO();
        mockFlight.setAvailableSeats(2); 

        when(flightServiceClient.getFlightById("FL123")).thenReturn(mockFlight);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.bookFlight(request);
        });

        Assertions.assertEquals("Booking failed: Insufficient seats available", exception.getMessage());
        
        verify(bookingRepository, never()).save(any());
        verify(bookingPublisher, never()).publishBookingConfirmation(any());
    }

    @Test
    void testCancelBooking_Success() {
        String pnr = "PNR123";
        Booking existingBooking = new Booking();
        existingBooking.setPnr(pnr);
        existingBooking.setFlightId("FL123");
        existingBooking.setNumberOfSeats(2);
        existingBooking.setJourneyDate(LocalDate.now().plusDays(10));
        existingBooking.setBookingStatus("CONFIRMED");

        when(bookingRepository.findByPnr(pnr)).thenReturn(Optional.of(existingBooking));
        
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        BookingResponse response = bookingService.cancelBooking(pnr);

        Assertions.assertEquals("CANCELLED", response.getBookingStatus());
        verify(flightServiceClient, times(1)).releaseFlightSeats("FL123", 2);
    }
    @Test
    void testBookingFallback() {
        BookingRequest request = new BookingRequest();
        RuntimeException ex = new RuntimeException("Simulated Failure");

        RuntimeException result = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.bookingFallback(request, ex);
        });

        Assertions.assertEquals("Flight service is currently unavailable. Please try again later.", result.getMessage());
    }
    
    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findByPnr("INVALID_PNR")).thenReturn(Optional.empty());

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("INVALID_PNR");
        });
        Assertions.assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void testCancelBooking_DatePassed() {
        Booking oldBooking = new Booking();
        oldBooking.setPnr("PNR_OLD");
        oldBooking.setJourneyDate(LocalDate.now().minusDays(1)); 

        when(bookingRepository.findByPnr("PNR_OLD")).thenReturn(Optional.of(oldBooking));

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("PNR_OLD");
        });
        Assertions.assertEquals("Cancellation not allowed. Journey date has passed.", ex.getMessage());
    }

    @Test
    void testCancelBooking_TooLate() {
        Booking lateBooking = new Booking();
        lateBooking.setPnr("PNR_LATE");
        lateBooking.setJourneyDate(LocalDate.now()); 

        when(bookingRepository.findByPnr("PNR_LATE")).thenReturn(Optional.of(lateBooking));

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("PNR_LATE");
        });
        Assertions.assertEquals("Cancellation not allowed. Journey date has passed.", ex.getMessage()); 
    }

    @Test
    void testGetBookingHistory() {
        Booking b1 = new Booking();
        b1.setPnr("PNR1");
        Booking b2 = new Booking();
        b2.setPnr("PNR2");

        when(bookingRepository.findByUserEmail("test@email.com")).thenReturn(List.of(b1, b2));

        List<BookingResponse> result = bookingService.getBookingHistory("test@email.com");
        
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("PNR1", result.get(0).getPnr());
    }
    @Test
    void testFallbacks() {
        BookingRequest req = new BookingRequest();
        RuntimeException ex = new RuntimeException("Test");
   
        RuntimeException res1 = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> 
            bookingService.bookingFallback(req, ex));
        org.junit.jupiter.api.Assertions.assertTrue(res1.getMessage().contains("unavailable"));

        RuntimeException res2 = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> 
            bookingService.cancelBookingFallback("PNR", ex));
        org.junit.jupiter.api.Assertions.assertTrue(res2.getMessage().contains("unavailable"));
    }
}