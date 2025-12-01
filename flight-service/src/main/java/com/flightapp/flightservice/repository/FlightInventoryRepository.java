package com.flightapp.flightservice.repository;

import com.flightapp.flightservice.entity.FlightInventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FlightInventoryRepository extends MongoRepository<FlightInventory, String> {
    Optional<FlightInventory> findByFlightId(String flightId);
}
