package com.flightapp.flightservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponse {

    private String flightId;
    private String airlineCode;
    private String airlineName;
    private String source;
    private String destination;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private Double price;
    private Integer availableSeats;

    private String aircraft;
}
