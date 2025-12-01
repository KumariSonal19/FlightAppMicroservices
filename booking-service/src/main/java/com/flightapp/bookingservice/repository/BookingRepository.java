package com.flightapp.bookingservice.repository;

import com.flightapp.bookingservice.entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    Optional<Booking> findByPnr(String pnr);
    List<Booking> findByUserEmail(String userEmail);
    List<Booking> findByFlightId(String flightId);
}
