package com.flightapp.bookingservice.dto;

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
    private String userEmail;
    private String userName;
    private Integer numberOfSeats;
    private List<PassengerRequest> passengers;
    private List<String> selectedSeats;
    private String mealPreference; // VEG or NON_VEG
    private LocalDate journeyDate;
}
