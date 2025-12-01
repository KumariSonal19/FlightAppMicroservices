package com.flightapp.bookingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String bookingId;
    private String pnr;
    private String flightId;
    private String userEmail;
    private String userName;
    private Integer numberOfSeats;
    private List<PassengerInfo> passengers;
    private List<String> selectedSeats;
    private String mealPreference; 
    private Double totalPrice;
    private String bookingStatus; 
    private LocalDate journeyDate;
    private Long createdAt;
    private Long updatedAt;
}
