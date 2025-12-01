package com.flightapp.flightservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flight_inventory")
public class FlightInventory {

    @Id
    private String inventoryId;

    private String flightId;
    private Integer totalSeats;
    private Integer bookedSeats;
    private Integer availableSeats;

    private Long createdAt;
    private Long updatedAt;
}
