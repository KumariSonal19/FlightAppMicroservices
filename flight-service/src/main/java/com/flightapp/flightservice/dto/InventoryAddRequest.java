package com.flightapp.flightservice.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAddRequest {

    @NotBlank
    private String airlineCode;

    @NotBlank
    private String airlineName;

    @NotBlank
    private String source;

    @NotBlank
    private String destination;

    @NotBlank
    private String departureTime;

    @NotBlank
    private String arrivalTime;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @Positive
    private Integer totalSeats;

    @NotBlank
    private String aircraft;
}
