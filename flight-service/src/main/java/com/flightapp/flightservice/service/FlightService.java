package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightInventory;
import com.flightapp.flightservice.exception.BadRequestException;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.repository.FlightInventoryRepository;
import com.flightapp.flightservice.repository.FlightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime; 
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightInventoryRepository inventoryRepository;

    public FlightService(FlightRepository flightRepository,
                         FlightInventoryRepository inventoryRepository) {
        this.flightRepository = flightRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<FlightResponse> searchFlights(FlightSearchRequest request) {
        log.info("Searching flights from {} to {} on {}",
                request.getSource(), request.getDestination(), request.getDepartureDate());

        ZonedDateTime startDateTime = request.getDepartureDate().atStartOfDay(java.time.ZoneId.systemDefault());
        ZonedDateTime endDateTime = request.getDepartureDate().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault());

        List<Flight> flights = flightRepository.findBySourceAndDestination(
                request.getSource(), request.getDestination());
        
   
        List<Flight> filteredFlights = flights.stream()
                .filter(f -> f.getDepartureTime().toLocalDate().equals(request.getDepartureDate()))
                .collect(Collectors.toList());

        log.info("Found {} flights", filteredFlights.size());

        return filteredFlights.stream()
                .map(this::convertToFlightResponse)
                .collect(Collectors.toList());
    }

    public FlightResponse addFlightInventory(InventoryAddRequest request) {
        log.info("Adding flight inventory for {} -> {}", request.getSource(), request.getDestination());

        Flight flight = new Flight();
        flight.setFlightId(UUID.randomUUID().toString()); 
        flight.setAirlineCode(request.getAirlineCode());
        flight.setAirlineName(request.getAirlineName());
        flight.setSource(request.getSource());
        flight.setDestination(request.getDestination());
        
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        
        flight.setPrice(request.getPrice());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getTotalSeats());
        flight.setAircraft(request.getAircraft());
        flight.setCreatedAt(System.currentTimeMillis());
        flight.setUpdatedAt(System.currentTimeMillis());

        Flight savedFlight = flightRepository.save(flight);

        FlightInventory inventory = new FlightInventory();
        inventory.setInventoryId(UUID.randomUUID().toString());
        inventory.setFlightId(savedFlight.getFlightId());
        inventory.setTotalSeats(request.getTotalSeats());
        inventory.setBookedSeats(0);
        inventory.setAvailableSeats(request.getTotalSeats());
        inventory.setCreatedAt(System.currentTimeMillis());
        inventory.setUpdatedAt(System.currentTimeMillis());
        inventoryRepository.save(inventory);

        log.info("Flight inventory added successfully: {}", savedFlight.getFlightId());
        return convertToFlightResponse(savedFlight);
    }

    public Optional<FlightResponse> getFlightById(String flightId) {
        log.info("Fetching flight with ID: {}", flightId);
        return flightRepository.findById(flightId).map(this::convertToFlightResponse);
    }

    public void updateAvailableSeats(String flightId, List<String> seatNumbers) {
        log.info("Updating seats for flight: {} - Seats: {}", flightId, seatNumbers);

        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isEmpty()) {
            throw new ResourceNotFoundException("Flight not found with ID: " + flightId);
        }

        Flight flight = flightOptional.get();

        if (flight.getAvailableSeats() < seatNumbers.size()) {
            throw new BadRequestException("Not enough seats available");
        }

        for (String seat : seatNumbers) {
            if (flight.getOccupiedSeats().contains(seat)) {
                throw new BadRequestException("Seat " + seat + " is already booked! Please select another seat.");
            }
        }

        flight.getOccupiedSeats().addAll(seatNumbers);
        flight.setAvailableSeats(flight.getTotalSeats() - flight.getOccupiedSeats().size());
        flight.setUpdatedAt(System.currentTimeMillis());
        flightRepository.save(flight);

        Optional<FlightInventory> invOpt = inventoryRepository.findByFlightId(flightId);
        if (invOpt.isPresent()) {
            FlightInventory inv = invOpt.get();
            inv.setBookedSeats(inv.getBookedSeats() + seatNumbers.size());
            inv.setAvailableSeats(inv.getTotalSeats() - inv.getBookedSeats());
            inv.setUpdatedAt(System.currentTimeMillis());
            inventoryRepository.save(inv);
        }
    }

    public void releaseSeats(String flightId, List<String> seatNumbers) {
        log.info("Releasing seats for flight: {} - Seats: {}", flightId, seatNumbers);

        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isEmpty()) {
            throw new ResourceNotFoundException("Flight not found with ID: " + flightId);
        }

        Flight flight = flightOptional.get();

        flight.getOccupiedSeats().removeAll(seatNumbers);
  
        flight.setAvailableSeats(flight.getTotalSeats() - flight.getOccupiedSeats().size());
        flight.setUpdatedAt(System.currentTimeMillis());
        flightRepository.save(flight);

        Optional<FlightInventory> invOpt = inventoryRepository.findByFlightId(flightId);
        if (invOpt.isPresent()) {
            FlightInventory inv = invOpt.get();
            inv.setBookedSeats(Math.max(0, inv.getBookedSeats() - seatNumbers.size()));
            inv.setAvailableSeats(inv.getTotalSeats() - inv.getBookedSeats());
            inv.setUpdatedAt(System.currentTimeMillis());
            inventoryRepository.save(inv);
        }
    }

    private FlightResponse convertToFlightResponse(Flight flight) {
        return new FlightResponse(
                flight.getFlightId(),
                flight.getAirlineCode(),
                flight.getAirlineName(),
                flight.getSource(),
                flight.getDestination(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice(),
                flight.getAvailableSeats(),
                flight.getAircraft()
        );
    }
}