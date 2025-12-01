package com.flightapp.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service")
public interface FlightServiceClient {
    @GetMapping("/api/flight/{flightId}")
    FlightDTO getFlightById(@PathVariable("flightId") String flightId);

    @PutMapping("/api/flight/{flightId}/update-seats")
    String updateFlightSeats(@PathVariable("flightId") String flightId, @RequestParam("seats") Integer seats);

    @PutMapping("/api/flight/{flightId}/release-seats")
    String releaseFlightSeats(@PathVariable("flightId") String flightId, @RequestParam("seats") Integer seats);
}
