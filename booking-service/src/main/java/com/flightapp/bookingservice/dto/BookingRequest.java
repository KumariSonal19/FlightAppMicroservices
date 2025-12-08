package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.enums.MealPreference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private String flightId;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotBlank(message = "User name is required")
    private String userName;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat must be booked")
    private Integer numberOfSeats;

    @NotEmpty(message = "Passenger list cannot be empty")
    @Valid 
    private List<PassengerRequest> passengers;

    @NotEmpty(message = "Must select at least one seat")
    private List<String> selectedSeats;

    @NotNull(message = "Meal preference is required")
    private MealPreference mealPreference;

    @NotNull(message = "Journey date is required")
    @FutureOrPresent(message = "Journey date cannot be in the past")
    private LocalDate journeyDate;
}