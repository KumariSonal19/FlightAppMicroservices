package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.dto.PassengerRequest;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.enums.BookingStatus;
import com.flightapp.bookingservice.enums.MealPreference;
import com.flightapp.bookingservice.exception.BadRequestException;
import com.flightapp.bookingservice.exception.ResourceNotFoundException;
import com.flightapp.bookingservice.exception.ServiceUnavailableException;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
        request.setPassengers(List.of(
                new PassengerRequest("P1", "M", 30),
                new PassengerRequest("P2", "F", 25)
        ));
        request.setSelectedSeats(List.of("1A", "1B"));
        request.setMealPreference(MealPreference.VEG);

        FlightDTO mockFlight = new FlightDTO();
        mockFlight.setFlightId("FL123");
        mockFlight.setPrice(100.0);
        mockFlight.setAvailableSeats(10);

        when(flightServiceClient.getFlightById("FL123")).thenReturn(mockFlight);

        Booking savedBooking = new Booking();
        savedBooking.setPnr("PNR123");
        savedBooking.setBookingStatus(BookingStatus.CONFIRMED);
        savedBooking.setTotalPrice(200.0);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponse response = bookingService.bookFlight(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(BookingStatus.CONFIRMED, response.getBookingStatus());
        Assertions.assertEquals("PNR123", response.getPnr());
        verify(flightServiceClient, times(1)).updateFlightSeats(eq("FL123"), anyList());
        verify(bookingPublisher, times(1)).publishBookingConfirmation(any(BookingEvent.class));
    }

    @Test
    void testBookFlight_InsufficientSeats() {
        BookingRequest request = new BookingRequest();
        request.setFlightId("FL123");
        request.setNumberOfSeats(5);
        request.setPassengers(List.of(
            new PassengerRequest("P1", "M", 20),
            new PassengerRequest("P2", "M", 20),
            new PassengerRequest("P3", "M", 20),
            new PassengerRequest("P4", "M", 20),
            new PassengerRequest("P5", "M", 20)
        ));
        request.setSelectedSeats(List.of("1A", "1B", "1C", "1D", "1E"));

        FlightDTO mockFlight = new FlightDTO();
        mockFlight.setAvailableSeats(2); 

        when(flightServiceClient.getFlightById("FL123")).thenReturn(mockFlight);

        com.flightapp.bookingservice.exception.BadRequestException exception = 
            org.junit.jupiter.api.Assertions.assertThrows(
                com.flightapp.bookingservice.exception.BadRequestException.class, 
                () -> bookingService.bookFlight(request)
            );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("Insufficient seats available"));
        
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testBookFlight_PassengerMismatch() {
        BookingRequest request = new BookingRequest();
        request.setNumberOfSeats(2);
        request.setPassengers(List.of(new PassengerRequest("John", "M", 30)));

        BadRequestException ex = Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.bookFlight(request)
        );

        Assertions.assertTrue(ex.getMessage().contains("must match the number of passengers"));
        verify(flightServiceClient, never()).getFlightById(any());
    }

    @Test
    void testCancelBooking_Success() {
        String pnr = "PNR123";
        Booking existingBooking = new Booking();
        existingBooking.setPnr(pnr);
        existingBooking.setFlightId("FL123");
        existingBooking.setNumberOfSeats(2);
        existingBooking.setJourneyDate(LocalDate.now().plusDays(10));
        existingBooking.setBookingStatus(BookingStatus.CONFIRMED);
        existingBooking.setSelectedSeats(List.of("1A", "1B"));

        when(bookingRepository.findByPnr(pnr)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        BookingResponse response = bookingService.cancelBooking(pnr);

        Assertions.assertEquals(BookingStatus.CANCELLED, response.getBookingStatus());
        verify(flightServiceClient, times(1)).releaseFlightSeats(eq("FL123"), anyList());
    }

    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findByPnr("INVALID_PNR")).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.cancelBooking("INVALID_PNR");
        });
    }

    @Test
    void testCancelBooking_DatePassed() {
        Booking oldBooking = new Booking();
        oldBooking.setPnr("PNR_OLD");
        oldBooking.setJourneyDate(LocalDate.now().minusDays(1));
        oldBooking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByPnr("PNR_OLD")).thenReturn(Optional.of(oldBooking));

        Assertions.assertThrows(BadRequestException.class, () -> {
            bookingService.cancelBooking("PNR_OLD");
        });
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        String pnr = "PNR_ALREADY_CANCELLED";
        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setBookingStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findByPnr(pnr)).thenReturn(Optional.of(booking));

        ResourceNotFoundException ex = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(pnr)
        );

        Assertions.assertEquals("Ticket with pnr " + pnr + " already cancelled", ex.getMessage());
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
    void testBookingFallback() {
        BookingRequest request = new BookingRequest();
        RuntimeException ex = new RuntimeException("Simulated Failure");

        ServiceUnavailableException result = Assertions.assertThrows(
                ServiceUnavailableException.class,
                () -> bookingService.bookingFallback(request, ex)
        );

        Assertions.assertEquals(
                "Flight service is currently unavailable. Please try again later.",
                result.getMessage()
        );
    }
}
