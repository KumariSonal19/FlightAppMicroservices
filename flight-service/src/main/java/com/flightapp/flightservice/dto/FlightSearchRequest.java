package com.flightapp.flightservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    @NotBlank(message="Provide the journey source")
    private String source;
    @NotBlank(message="Provide the journey destination")
    private String destination;
    @NotNull(message="Provide the journey departure date")
    private LocalDate departureDate;
    @NotBlank(message="Provide the journey type")
    private String journeyType;
}