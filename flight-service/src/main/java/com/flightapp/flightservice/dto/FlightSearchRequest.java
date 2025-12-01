package com.flightapp.flightservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {

    private String source;
    private String destination;
    private LocalDate departureDate;
    private String journeyType;
}
