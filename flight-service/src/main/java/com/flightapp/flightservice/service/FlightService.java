package com.flightapp.flightservice.service;

import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightInventory;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.repository.FlightInventoryRepository;
import com.flightapp.flightservice.repository.FlightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.exception.BadRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightInventoryRepository inventoryRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public FlightService(FlightRepository flightRepository,
                         FlightInventoryRepository inventoryRepository) {
        this.flightRepository = flightRepository;
        this.inventoryRepository = inventoryRepository;
    }

 
    public List<FlightResponse> searchFlights(FlightSearchRequest request) {
        log.info("Searching flights from {} to {} on {}",
                request.getSource(), request.getDestination(), request.getDepartureDate());

       
        LocalDateTime startDateTime = request.getDepartureDate().atStartOfDay();
        LocalDateTime endDateTime = request.getDepartureDate().atTime(23, 59, 59);

        
        List<Flight> flights = flightRepository.findBySourceAndDestinationAndDepartureTimeBetween(
                request.getSource(), request.getDestination(), startDateTime, endDateTime);
        log.info("Found {} flights", flights.size());

        
        return flights.stream()
                .map(this::convertToFlightResponse)
                .collect(Collectors.toList());
    }

    
    public FlightResponse addFlightInventory(InventoryAddRequest request) {
        log.info("Adding flight inventory for {} -> {}", request.getSource(), request.getDestination());

        
        LocalDateTime departure = LocalDateTime.parse(request.getDepartureTime(), FORMATTER);
        LocalDateTime arrival = LocalDateTime.parse(request.getArrivalTime(), FORMATTER);

        
        Flight flight = new Flight();
        flight.setFlightId(UUID.randomUUID().toString());
        flight.setAirlineCode(request.getAirlineCode());
        flight.setAirlineName(request.getAirlineName());
        flight.setSource(request.getSource());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
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

    
    public void updateAvailableSeats(String flightId, Integer seatsToBook) {
        log.info("Updating available seats for flight: {} by {}", flightId, seatsToBook);

        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isEmpty()) {
        	throw new ResourceNotFoundException("Flight not found with ID: " + flightId);
        }

        Flight flight = flightOptional.get();
        if (flight.getAvailableSeats() < seatsToBook) {
        	throw new BadRequestException("Not enough seats available");
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - seatsToBook);
        flight.setUpdatedAt(System.currentTimeMillis());
        flightRepository.save(flight);

        
        Optional<FlightInventory> inventoryOptional = inventoryRepository.findByFlightId(flightId);
        if (inventoryOptional.isPresent()) {
            FlightInventory inventory = inventoryOptional.get();
            inventory.setBookedSeats(inventory.getBookedSeats() + seatsToBook);
            inventory.setAvailableSeats(inventory.getAvailableSeats() - seatsToBook);
            inventory.setUpdatedAt(System.currentTimeMillis());
            inventoryRepository.save(inventory);
        } else {
            log.warn("Inventory not found for flightId: {}", flightId);
        }
    }

   
    public void releaseSeats(String flightId, Integer seatsToRelease) {
        log.info("Releasing seats for flight: {} - {}", flightId, seatsToRelease);

        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isEmpty()) {
            throw new RuntimeException("Flight not found with ID: " + flightId);
        }

        Flight flight = flightOptional.get();
        flight.setAvailableSeats(flight.getAvailableSeats() + seatsToRelease);
        flight.setUpdatedAt(System.currentTimeMillis());
        flightRepository.save(flight);

        
        Optional<FlightInventory> inventoryOptional = inventoryRepository.findByFlightId(flightId);
        if (inventoryOptional.isPresent()) {
            FlightInventory inventory = inventoryOptional.get();
            inventory.setBookedSeats(Math.max(0, inventory.getBookedSeats() - seatsToRelease));
            inventory.setAvailableSeats(inventory.getAvailableSeats() + seatsToRelease);
            inventory.setUpdatedAt(System.currentTimeMillis());
            inventoryRepository.save(inventory);
        } else {
            log.warn("Inventory not found for flightId: {}", flightId);
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
