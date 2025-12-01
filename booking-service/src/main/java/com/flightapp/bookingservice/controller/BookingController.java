package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/booking")
@Validated
@Slf4j
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping("/flight/{flightid}")
    public ResponseEntity<?> bookFlight(@PathVariable("flightid") String flightId, @RequestBody BookingRequest request) {
        log.info("Request to book flight: {}", flightId);
        try {
            request.setFlightId(flightId);
            BookingResponse response = bookingService.bookFlight(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Booking failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking failed: " + e.getMessage());
        }
    }

    @GetMapping("/ticket/{pnr}")
    public ResponseEntity<?> getBookingByPnr(@PathVariable String pnr) {
        log.info("Request to get booking by PNR: {}", pnr);
        Optional<BookingResponse> booking = bookingService.getBookingByPnr(pnr);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found with PNR: " + pnr);
    }

    @GetMapping("/history/{emailId}")
    public ResponseEntity<?> getBookingHistory(@PathVariable String emailId) {
        log.info("Request to get booking history for email: {}", emailId);
        List<BookingResponse> bookings = bookingService.getBookingHistory(emailId);
        if (!bookings.isEmpty()) {
            return ResponseEntity.ok(bookings);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No bookings found for email: " + emailId);
    }

    @DeleteMapping("/cancel/{pnr}")
    public ResponseEntity<?> cancelBooking(@PathVariable String pnr) {
        log.info("Request to cancel booking: {}", pnr);
        try {
            BookingResponse response = bookingService.cancelBooking(pnr);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Cancellation failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cancellation failed: " + e.getMessage());
        }
    }
}
