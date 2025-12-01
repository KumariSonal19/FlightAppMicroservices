package com.flightapp.flightservice.repository;

import com.flightapp.flightservice.entity.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends MongoRepository<Flight, String> {

    List<Flight> findBySourceAndDestinationAndDepartureTimeBetween(
            String source,
            String destination,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<Flight> findBySourceAndDestination(String source, String destination);
}
