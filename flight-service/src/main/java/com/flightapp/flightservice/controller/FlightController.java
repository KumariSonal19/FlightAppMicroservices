package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.service.FlightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/flight")
@Validated
@Slf4j
public class FlightController {

    @Autowired
    private FlightService flightService;

    // Inject validator to validate each parsed InventoryAddRequest
    @Autowired
    private Validator validator;

    // Use Spring's ObjectMapper (pre-configured with Java Time support)
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Batch upload endpoint.
     * Expects multipart/form-data with a "file" part containing a JSON array of InventoryAddRequest objects.
     */
    @PostMapping(
        value = "/airline/inventory/batch",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String,Object>> addFlightInventoryBatch(@RequestPart("file") MultipartFile file) {
        log.info("Request to add flight inventory batch, file: {}", file == null ? "null" : file.getOriginalFilename());

        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> itemResults = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            result.put("message", "File is required");
            result.put("results", itemResults);
            return ResponseEntity.badRequest().body(result);
        }

        List<InventoryAddRequest> requests;
        try {
            // Use autowired objectMapper so ZonedDateTime and Jackson JavaTimeModule are supported
            requests = objectMapper.readValue(file.getInputStream(), new TypeReference<List<InventoryAddRequest>>() {});
        } catch (Exception e) {
            log.error("Failed to parse uploaded file", e);
            result.put("message", "Invalid JSON file format: " + e.getMessage());
            result.put("results", itemResults);
            return ResponseEntity.badRequest().body(result);
        }

        for (int i = 0; i < requests.size(); i++) {
            InventoryAddRequest req = requests.get(i);
            Map<String,Object> item = new HashMap<>();
            item.put("index", i);
            try {
                // Validate the DTO using injected Validator
                Set<ConstraintViolation<InventoryAddRequest>> violations = validator.validate(req);
                if (!violations.isEmpty()) {
                    Map<String,String> errors = new HashMap<>();
                    for (ConstraintViolation<InventoryAddRequest> v : violations) {
                        String prop = v.getPropertyPath().toString();
                        errors.put(prop, v.getMessage());
                    }
                    item.put("errors", errors);
                    itemResults.add(item);
                    continue; // don't try to save invalid item
                }

                FlightResponse saved = flightService.addFlightInventory(req);
                item.put("flightId", saved.getFlightId());
                item.put("status", "CREATED");
            } catch (Exception ex) {
                log.error("Error adding flight at index {}: {}", i, ex.getMessage(), ex);
                item.put("errors", Map.of("exception", ex.getMessage()));
            }
            itemResults.add(item);
        }

        long successCount = itemResults.stream().filter(m -> m.containsKey("flightId")).count();
        long failureCount = itemResults.size() - successCount;

        result.put("total", itemResults.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("results", itemResults);

        // 201 created if all success, else 207 Multi-Status
        return ResponseEntity.status(failureCount == 0 ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS).body(result);
    }

    @PostMapping("/airline/inventory/add")
    public ResponseEntity<Map<String, String>> addFlightInventory(@Valid @RequestBody InventoryAddRequest request) {
        log.info("Request to add flight inventory");
        FlightResponse fullResponse = flightService.addFlightInventory(request);
        Map<String, String> response = new HashMap<>();
        response.put("flightId", fullResponse.getFlightId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        log.info("Request to search flights");
        List<FlightResponse> flights = flightService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<Object> getFlightById(@PathVariable String flightId) {
        log.info("Request to get flight by ID: {}", flightId);
        Optional<FlightResponse> flight = flightService.getFlightById(flightId);
        if (flight.isPresent()) {
            return ResponseEntity.ok(flight.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight not found with ID: " + flightId);
    }

    @PutMapping("/{flightId}/update-seats")
    public ResponseEntity<String> updateSeats(@PathVariable String flightId, @RequestBody List<String> seatNumbers) {
        log.info("Request to update seats for flight: {}", flightId);
        try {
            flightService.updateAvailableSeats(flightId, seatNumbers);
            return ResponseEntity.ok("Seats updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{flightId}/release-seats")
    public ResponseEntity<String> releaseSeats(@PathVariable String flightId, @RequestBody List<String> seatNumbers) {
        log.info("Request to release seats for flight: {}", flightId);
        try {
            flightService.releaseSeats(flightId, seatNumbers);
            return ResponseEntity.ok("Seats released successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
