package com.flightapp.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String bookingId;
    private String pnr;
    private String flightId;
    private String userEmail;
    private String userName;
    private Integer numberOfSeats;
    private List<String> selectedSeats;
    private String mealPreference;
    private Double totalPrice;
    private String bookingStatus;
    private LocalDate journeyDate;
    private Long createdAt;
}
