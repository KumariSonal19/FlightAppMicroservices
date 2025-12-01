package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.entity.PassengerInfo;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.dto.PassengerRequest;
import com.flightapp.bookingservice.feign.FlightServiceClient;
import com.flightapp.bookingservice.messaging.BookingEvent;
import com.flightapp.bookingservice.messaging.BookingPublisher;
import com.flightapp.bookingservice.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightServiceClient flightServiceClient;

    @Autowired
    private BookingPublisher bookingPublisher;

    @CircuitBreaker(name = "flight-service", fallbackMethod = "bookingFallback")
    public BookingResponse bookFlight(BookingRequest request) {
        log.info("Processing flight booking for flight: {}", request.getFlightId());
        try {
            var flightDetails = flightServiceClient.getFlightById(request.getFlightId());
            if (flightDetails == null) {
                throw new RuntimeException("Flight not found");
            }
            if (flightDetails.getAvailableSeats() < request.getNumberOfSeats()) {
                throw new RuntimeException("Insufficient seats available");
            }
            
            Booking booking = new Booking();
            booking.setBookingId(UUID.randomUUID().toString());
            booking.setPnr(generatePNR());
            booking.setFlightId(request.getFlightId());
            booking.setUserEmail(request.getUserEmail());
            booking.setUserName(request.getUserName());
            booking.setNumberOfSeats(request.getNumberOfSeats());
            booking.setSelectedSeats(request.getSelectedSeats());
            booking.setMealPreference(request.getMealPreference());
            booking.setJourneyDate(request.getJourneyDate());
            booking.setTotalPrice(flightDetails.getPrice() * request.getNumberOfSeats());
            booking.setBookingStatus("CONFIRMED");
            booking.setCreatedAt(System.currentTimeMillis());
            booking.setUpdatedAt(System.currentTimeMillis());
           
            List<PassengerInfo> passengers = request.getPassengers().stream()
                    .map(p -> new PassengerInfo(p.getName(), p.getGender(), p.getAge()))
                    .collect(Collectors.toList());
            booking.setPassengers(passengers);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            flightServiceClient.updateFlightSeats(request.getFlightId(), request.getNumberOfSeats());
            
            BookingEvent event = new BookingEvent(
                savedBooking.getPnr(),
                savedBooking.getUserEmail(),
                savedBooking.getUserName(),
                savedBooking.getFlightId(),
                savedBooking.getNumberOfSeats(),
                savedBooking.getTotalPrice(),
                "CONFIRMED",
                System.currentTimeMillis()
            );
            bookingPublisher.publishBookingConfirmation(event);
            log.info("Flight booking confirmed with PNR: {}", savedBooking.getPnr());
            return convertToBookingResponse(savedBooking);
        } catch (Exception e) {
            log.error("Error during flight booking", e);
            throw new RuntimeException("Booking failed: " + e.getMessage());
        }
    }

    public BookingResponse bookingFallback(BookingRequest request, Exception e) {
        log.error("Circuit breaker triggered for flight booking", e);
        throw new RuntimeException("Flight service is currently unavailable. Please try again later.");
    }

    public Optional<BookingResponse> getBookingByPnr(String pnr) {
        log.info("Fetching booking details for PNR: {}", pnr);
        return bookingRepository.findByPnr(pnr).map(this::convertToBookingResponse);
    }

    public List<BookingResponse> getBookingHistory(String emailId) {
        log.info("Fetching booking history for email: {}", emailId);
        return bookingRepository.findByUserEmail(emailId).stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = "flight-service", fallbackMethod = "cancelBookingFallback")
    public BookingResponse cancelBooking(String pnr) {
        log.info("Processing cancellation for booking: {}", pnr);
        Optional<Booking> bookingOptional = bookingRepository.findByPnr(pnr);
        if (!bookingOptional.isPresent()) {
            throw new RuntimeException("Booking not found");
        }
        Booking booking = bookingOptional.get();
        LocalDate journeyDate = booking.getJourneyDate();
        LocalDate today = LocalDate.now();
        long daysUntilJourney = ChronoUnit.DAYS.between(today, journeyDate);
        if (daysUntilJourney <= 0) {
            throw new RuntimeException("Cancellation not allowed. Journey date has passed.");
        }
        if (daysUntilJourney < 1) {
            throw new RuntimeException("Cancellation not allowed. Less than 24 hours before journey.");
        }
        booking.setBookingStatus("CANCELLED");
        booking.setUpdatedAt(System.currentTimeMillis());
        Booking cancelledBooking = bookingRepository.save(booking);
        flightServiceClient.releaseFlightSeats(booking.getFlightId(), booking.getNumberOfSeats());
        log.info("Booking cancelled successfully: {}", pnr);
        return convertToBookingResponse(cancelledBooking);
    }

    public BookingResponse cancelBookingFallback(String pnr, Exception e) {
        log.error("Circuit breaker triggered for booking cancellation", e);
        throw new RuntimeException("Flight service is currently unavailable. Cancellation failed.");
    }

    private String generatePNR() {
        return "PNR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        return new BookingResponse(
            booking.getBookingId(),
            booking.getPnr(),
            booking.getFlightId(),
            booking.getUserEmail(),
            booking.getUserName(),
            booking.getNumberOfSeats(),
            booking.getSelectedSeats(),
            booking.getMealPreference(),
            booking.getTotalPrice(),
            booking.getBookingStatus(),
            booking.getJourneyDate(),
            booking.getCreatedAt()
        );
    }
}
