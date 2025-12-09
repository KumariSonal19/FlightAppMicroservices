package com.flightapp.bookingservice.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime; 

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private String flightId;
    private String airlineCode;
    private String airlineName;
    private String source;
    private String destination;
    private ZonedDateTime departureTime; 
    private ZonedDateTime arrivalTime;
    
    private Double price;
    private Integer availableSeats;
    private String aircraft;
}