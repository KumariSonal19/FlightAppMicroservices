package com.flightapp.flightservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime; 

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAddRequest {

    @NotBlank(message = "Airline Code is required")
    private String airlineCode;

    @NotBlank(message = "Airline Name is required")
    private String airlineName;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure Time is required") 
    private ZonedDateTime departureTime; 

    @NotNull(message = "Arrival Time is required")   
    private ZonedDateTime arrivalTime;   
    
    @NotNull(message = "Price is required") 
    @Positive(message = "Price must be greater than zero") 
    private Double price;

    @NotNull(message = "Total Seats is required")
    @Positive(message = "Total Seats must be positive")
    private Integer totalSeats;

    @NotBlank(message = "Aircraft type is required")
    private String aircraft;
}