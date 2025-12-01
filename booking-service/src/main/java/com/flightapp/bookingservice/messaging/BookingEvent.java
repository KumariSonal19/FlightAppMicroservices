package com.flightapp.bookingservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private String pnr;
    private String userEmail;
    private String userName;
    private String flightId;
    private Integer numberOfSeats;
    private Double totalPrice;
    private String bookingStatus;
    private Long timestamp;
}
