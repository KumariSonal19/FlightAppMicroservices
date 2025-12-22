package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.entity.PassengerInfo; 
import com.flightapp.bookingservice.enums.BookingStatus;
import com.flightapp.bookingservice.enums.MealPreference;
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
    private MealPreference mealPreference; 
    private Double totalPrice;
    private BookingStatus bookingStatus; 
    private LocalDate journeyDate;
    private Long createdAt;
    private List<PassengerInfo> passengers;
}