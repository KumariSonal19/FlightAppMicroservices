package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.entity.PassengerInfo;
import com.flightapp.bookingservice.enums.BookingStatus;
import com.flightapp.bookingservice.exception.BadRequestException;
import com.flightapp.bookingservice.exception.ResourceNotFoundException;
import com.flightapp.bookingservice.exception.ServiceException;
import com.flightapp.bookingservice.exception.ServiceUnavailableException;
import com.flightapp.bookingservice.feign.FlightServiceClient;
import com.flightapp.bookingservice.messaging.BookingEvent;
import com.flightapp.bookingservice.messaging.BookingPublisher;
import com.flightapp.bookingservice.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightServiceClient flightServiceClient;
    private final BookingPublisher bookingPublisher;

    @CircuitBreaker(name = "flight-service", fallbackMethod = "bookingFallback")
    public BookingResponse bookFlight(BookingRequest request) {
        log.info("Processing flight booking for flight: {}", request.getFlightId());

        if (request.getNumberOfSeats() == 0) {
            throw new BadRequestException("Number of seats must be greater than 0");
        }
        if (request.getPassengers() == null || request.getNumberOfSeats() != request.getPassengers().size()) {
            throw new BadRequestException(
                    "Number of seats (" + request.getNumberOfSeats() +
                    ") must match the number of passengers provided (" +
                    (request.getPassengers() == null ? 0 : request.getPassengers().size()) + ")"
            );
        }
        java.util.Set<String> uniqueSeats = new java.util.HashSet<>(request.getSelectedSeats());
        if (uniqueSeats.size() != request.getSelectedSeats().size()) {
            throw new BadRequestException("Duplicate seats selected! Each passenger must have a unique seat.");
        }

        try {
            var flightDetails = flightServiceClient.getFlightById(request.getFlightId());
            if (flightDetails == null) {
                throw new ResourceNotFoundException("Flight not found");
            }
            if (flightDetails.getAvailableSeats() < request.getNumberOfSeats()) {
                throw new BadRequestException("Insufficient seats available");
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
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setCreatedAt(System.currentTimeMillis());
            booking.setUpdatedAt(System.currentTimeMillis());

            List<PassengerInfo> passengers = request.getPassengers().stream()
                    .map(p -> new PassengerInfo(p.getName(), p.getGender(), p.getAge()))
                    .toList();
            booking.setPassengers(passengers);

            Booking savedBooking = bookingRepository.save(booking);

            flightServiceClient.updateFlightSeats(request.getFlightId(), request.getSelectedSeats());

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

        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during flight booking", e);
            throw new ServiceException("Booking failed: " + e.getMessage());
        }
    }

    public Optional<BookingResponse> getBookingByPnr(String pnr) {
        log.info("Fetching booking details for PNR: {}", pnr);
        return bookingRepository.findByPnr(pnr).map(this::convertToBookingResponse);
    }

    public List<BookingResponse> getBookingHistory(String emailId) {
        log.info("Fetching booking history for email: {}", emailId);
        return bookingRepository.findByUserEmail(emailId).stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @CircuitBreaker(name = "flight-service", fallbackMethod = "cancelBookingFallback")
    public BookingResponse cancelBooking(String pnr) {
        log.info("Processing cancellation for booking: {}", pnr);
        Optional<Booking> bookingOptional = bookingRepository.findByPnr(pnr);

        if (bookingOptional.isEmpty()) {
            throw new ResourceNotFoundException("Booking not found");
        }

        Booking booking = bookingOptional.get();

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ResourceNotFoundException("Ticket with pnr " + pnr + " already cancelled");
        }

        LocalDate journeyDate = booking.getJourneyDate();
        LocalDate today = LocalDate.now();
        long daysUntilJourney = ChronoUnit.DAYS.between(today, journeyDate);

        if (daysUntilJourney <= 0) {
            throw new BadRequestException("Cancellation not allowed. Journey date has passed.");
        }
        if (daysUntilJourney < 1) {
            throw new BadRequestException("Cancellation not allowed. Less than 24 hours before journey.");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(System.currentTimeMillis());
        Booking cancelledBooking = bookingRepository.save(booking);

        flightServiceClient.releaseFlightSeats(booking.getFlightId(), booking.getSelectedSeats());
        log.info("Booking cancelled successfully: {}", pnr);
        return convertToBookingResponse(cancelledBooking);
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
                booking.getCreatedAt(),
                booking.getPassengers() 
        );
    }

    @SuppressWarnings("unused")
    public BookingResponse bookingFallback(BookingRequest request, Exception e) {
        log.error("Circuit breaker triggered for flight booking", e);
        throw new ServiceUnavailableException("Flight service is currently unavailable. Please try again later.");
    }

    @SuppressWarnings("unused")
    public BookingResponse cancelBookingFallback(String pnr, Exception e) {
        log.error("Circuit breaker triggered for booking cancellation", e);
        throw new ServiceUnavailableException("Flight service is currently unavailable. Cancellation failed.");
    }
}
