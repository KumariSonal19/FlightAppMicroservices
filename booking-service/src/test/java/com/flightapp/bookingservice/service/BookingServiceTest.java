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

// @ExtendWith(MockitoExtension.class) sets up the Mockito environment
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    // @Mock creates a "fake" object. We don't want the real DB or Feign client.
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightServiceClient flightServiceClient;

    @Mock
    private BookingPublisher bookingPublisher;

    // @InjectMocks puts the "fake" objects above into the real BookingService
    @InjectMocks
    private BookingService bookingService;

    // Test Case 1: Successful Booking
    @Test
    void testBookFlight_Success() {
        // 1. Prepare Data
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
        mockFlight.setAvailableSeats(10); // We have enough seats

        // 2. Define Mock Behavior (The "Script")
        // When the service asks for flight details, return our mockFlight
        when(flightServiceClient.getFlightById("FL123")).thenReturn(mockFlight);
        
        // When the repository saves ANY booking, return a booking with a specific PNR
        Booking savedBooking = new Booking();
        savedBooking.setPnr("PNR123");
        savedBooking.setBookingStatus("CONFIRMED");
        savedBooking.setTotalPrice(200.0); // 2 seats * 100.0
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // 3. Execute the method
        BookingResponse response = bookingService.bookFlight(request);

        // 4. Assertions (Check the results)
        Assertions.assertNotNull(response);
        Assertions.assertEquals("CONFIRMED", response.getBookingStatus());
        Assertions.assertEquals("PNR123", response.getPnr());

        // 5. Verify interactions (Did we call the dependencies?)
        verify(flightServiceClient, times(1)).updateFlightSeats("FL123", 2);
        verify(bookingPublisher, times(1)).publishBookingConfirmation(any(BookingEvent.class));
    }

    // Test Case 2: Booking Fails (Insufficient Seats)
    @Test
    void testBookFlight_InsufficientSeats() {
        // 1. Prepare Data
        BookingRequest request = new BookingRequest();
        request.setFlightId("FL123");
        request.setNumberOfSeats(5);

        FlightDTO mockFlight = new FlightDTO();
        mockFlight.setAvailableSeats(2); // Only 2 seats available, but we want 5

        // 2. Define Mock Behavior
        when(flightServiceClient.getFlightById("FL123")).thenReturn(mockFlight);

        // 3. Execute & Assert Exception
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.bookFlight(request);
        });

        // FIXED: Updated the expected message to match what the Service actually throws
        Assertions.assertEquals("Booking failed: Insufficient seats available", exception.getMessage());
        
        // Ensure we NEVER saved to DB or published an event
        verify(bookingRepository, never()).save(any());
        verify(bookingPublisher, never()).publishBookingConfirmation(any());
    }

    // Test Case 3: Cancel Booking Success
    @Test
    void testCancelBooking_Success() {
        String pnr = "PNR123";
        Booking existingBooking = new Booking();
        existingBooking.setPnr(pnr);
        existingBooking.setFlightId("FL123");
        existingBooking.setNumberOfSeats(2);
        // Journey is 10 days from now (so cancellation is allowed)
        existingBooking.setJourneyDate(LocalDate.now().plusDays(10));
        existingBooking.setBookingStatus("CONFIRMED");

        when(bookingRepository.findByPnr(pnr)).thenReturn(Optional.of(existingBooking));
        
        // We expect the repository to save the updated "CANCELLED" status
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        BookingResponse response = bookingService.cancelBooking(pnr);

        Assertions.assertEquals("CANCELLED", response.getBookingStatus());
        verify(flightServiceClient, times(1)).releaseFlightSeats("FL123", 2);
    }
    @Test
    void testBookingFallback() {
        BookingRequest request = new BookingRequest();
        RuntimeException ex = new RuntimeException("Simulated Failure");

        // We call the fallback method directly to ensure it returns the specific error message
        RuntimeException result = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.bookingFallback(request, ex);
        });

        Assertions.assertEquals("Flight service is currently unavailable. Please try again later.", result.getMessage());
    }

    // Test 5: Cancel Booking - Flight Not Found
    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findByPnr("INVALID_PNR")).thenReturn(Optional.empty());

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("INVALID_PNR");
        });
        Assertions.assertEquals("Booking not found", ex.getMessage());
    }

    // Test 6: Cancel Booking - Date Passed (Sad Path)
    @Test
    void testCancelBooking_DatePassed() {
        Booking oldBooking = new Booking();
        oldBooking.setPnr("PNR_OLD");
        // Set journey to yesterday
        oldBooking.setJourneyDate(LocalDate.now().minusDays(1)); 

        when(bookingRepository.findByPnr("PNR_OLD")).thenReturn(Optional.of(oldBooking));

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("PNR_OLD");
        });
        Assertions.assertEquals("Cancellation not allowed. Journey date has passed.", ex.getMessage());
    }

    // Test 7: Cancel Booking - Less than 24h (Sad Path)
    @Test
    void testCancelBooking_TooLate() {
        Booking lateBooking = new Booking();
        lateBooking.setPnr("PNR_LATE");
        // Set journey to today (0 days difference)
        lateBooking.setJourneyDate(LocalDate.now()); 

        when(bookingRepository.findByPnr("PNR_LATE")).thenReturn(Optional.of(lateBooking));

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.cancelBooking("PNR_LATE");
        });
        Assertions.assertEquals("Cancellation not allowed. Journey date has passed.", ex.getMessage()); 
        // Note: Based on your logic "daysUntilJourney <= 0", today counts as passed.
    }

    // Test 8: Get Booking History
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
}