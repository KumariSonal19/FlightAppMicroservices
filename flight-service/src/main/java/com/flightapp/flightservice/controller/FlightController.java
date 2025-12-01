package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.service.FlightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flight")
@Validated
@Slf4j
public class FlightController {

    @Autowired
    private FlightService flightService;

    @PostMapping("/airline/inventory/add")
    public ResponseEntity<FlightResponse> addFlightInventory(@RequestBody InventoryAddRequest request) {
        log.info("Request to add flight inventory");
        FlightResponse response = flightService.addFlightInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(@RequestBody FlightSearchRequest request) {
        log.info("Request to search flights");
        List<FlightResponse> flights = flightService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<?> getFlightById(@PathVariable String flightId) {
        log.info("Request to get flight by ID: {}", flightId);
        Optional<FlightResponse> flight = flightService.getFlightById(flightId);
        if (flight.isPresent()) {
            return ResponseEntity.ok(flight.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight not found with ID: " + flightId);
    }

    @PutMapping("/{flightId}/update-seats")
    public ResponseEntity<String> updateSeats(@PathVariable String flightId, @RequestParam Integer seats) {
        log.info("Request to update seats for flight: {} by {}", flightId, seats);
        try {
            flightService.updateAvailableSeats(flightId, seats);
            return ResponseEntity.ok("Seats updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{flightId}/release-seats")
    public ResponseEntity<String> releaseSeats(@PathVariable String flightId, @RequestParam Integer seats) {
        log.info("Request to release seats for flight: {} by {}", flightId, seats);
        try {
            flightService.releaseSeats(flightId, seats);
            return ResponseEntity.ok("Seats released successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}