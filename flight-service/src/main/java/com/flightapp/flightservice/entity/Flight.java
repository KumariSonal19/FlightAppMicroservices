package com.flightapp.flightservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flights")
public class Flight {

    @Id
    private String flightId;

    private String airlineCode;
    private String airlineName;
    private String source;
    private String destination;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private Double price;
    private Integer totalSeats;
    private Integer availableSeats;

    private String aircraft;

    private Long createdAt;
    private Long updatedAt;
}
