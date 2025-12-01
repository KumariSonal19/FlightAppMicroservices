package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightInventory;
import com.flightapp.flightservice.repository.FlightInventoryRepository;
import com.flightapp.flightservice.repository.FlightRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightInventoryRepository inventoryRepository;

    @InjectMocks
    private FlightService flightService;

    @Test
    void testAddFlightInventory_Success() {
        // 1. Prepare Request
        InventoryAddRequest request = new InventoryAddRequest();
        request.setAirlineCode("AI");
        request.setAirlineName("Air India");
        request.setSource("DEL");
        request.setDestination("BOM");
        request.setDepartureTime("2023-12-01T10:00:00");
        request.setArrivalTime("2023-12-01T12:00:00");
        request.setPrice(5000.0);
        request.setTotalSeats(100);
        request.setAircraft("Boeing 737");

        // 2. Mock Repository
        Flight mockFlight = new Flight();
        mockFlight.setFlightId("F1");
        mockFlight.setAirlineCode("AI");
        // ... set other fields if needed for assertion

        when(flightRepository.save(any(Flight.class))).thenReturn(mockFlight);
        when(inventoryRepository.save(any(FlightInventory.class))).thenReturn(new FlightInventory());

        // 3. Execute
        FlightResponse response = flightService.addFlightInventory(request);

        // 4. Verify
        Assertions.assertNotNull(response);
        Assertions.assertEquals("F1", response.getFlightId());
        verify(flightRepository, times(1)).save(any(Flight.class));
        verify(inventoryRepository, times(1)).save(any(FlightInventory.class));
    }

    @Test
    void testSearchFlights_Found() {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setSource("DEL");
        request.setDestination("BOM");
        request.setDepartureDate(LocalDate.of(2023, 12, 1));

        Flight flight = new Flight();
        flight.setFlightId("F1");

        when(flightRepository.findBySourceAndDestinationAndDepartureTimeBetween(any(), any(), any(), any()))
                .thenReturn(List.of(flight));

        List<FlightResponse> responses = flightService.searchFlights(request);

        Assertions.assertEquals(1, responses.size());
        Assertions.assertEquals("F1", responses.get(0).getFlightId());
    }

    @Test
    void testGetFlightById_Found() {
        Flight flight = new Flight();
        flight.setFlightId("F1");
        
        when(flightRepository.findById("F1")).thenReturn(Optional.of(flight));
        
        Optional<FlightResponse> response = flightService.getFlightById("F1");
        
        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals("F1", response.get().getFlightId());
    }

    // Logic Test: Updating Seats
    @Test
    void testUpdateAvailableSeats_Success() {
        Flight flight = new Flight();
        flight.setFlightId("F1");
        flight.setAvailableSeats(10);

        FlightInventory inventory = new FlightInventory();
        inventory.setBookedSeats(0);
        inventory.setAvailableSeats(10);

        when(flightRepository.findById("F1")).thenReturn(Optional.of(flight));
        when(inventoryRepository.findByFlightId("F1")).thenReturn(Optional.of(inventory));

        // Attempt to book 2 seats
        flightService.updateAvailableSeats("F1", 2);

        // Verify Save was called
        verify(flightRepository, times(1)).save(flight);
        verify(inventoryRepository, times(1)).save(inventory);
        
        // Verify values changed
        Assertions.assertEquals(8, flight.getAvailableSeats());
    }

    @Test
    void testUpdateAvailableSeats_Insufficient() {
        Flight flight = new Flight();
        flight.setFlightId("F1");
        flight.setAvailableSeats(1); // Only 1 seat

        when(flightRepository.findById("F1")).thenReturn(Optional.of(flight));

        // Attempt to book 2 seats -> Should throw Exception
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            flightService.updateAvailableSeats("F1", 2);
        });

        Assertions.assertEquals("Not enough seats available", ex.getMessage());
        // Verify we NEVER saved the flight
        verify(flightRepository, never()).save(any());
    }
 // ... inside FlightServiceTest class ...

    // Test: Release Seats Success
    @Test
    void testReleaseSeats_Success() {
        Flight flight = new Flight();
        flight.setFlightId("F1");
        flight.setAvailableSeats(5);

        FlightInventory inventory = new FlightInventory();
        inventory.setFlightId("F1");
        inventory.setBookedSeats(5);
        inventory.setAvailableSeats(5);

        when(flightRepository.findById("F1")).thenReturn(Optional.of(flight));
        when(inventoryRepository.findByFlightId("F1")).thenReturn(Optional.of(inventory));

        flightService.releaseSeats("F1", 2);

        // Verify updates
        Assertions.assertEquals(7, flight.getAvailableSeats()); // 5 + 2
        Assertions.assertEquals(3, inventory.getBookedSeats()); // 5 - 2
        verify(flightRepository).save(flight);
        verify(inventoryRepository).save(inventory);
    }

    // Test: Release Seats - Flight Not Found
    @Test
    void testReleaseSeats_FlightNotFound() {
        when(flightRepository.findById("INVALID")).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> {
            flightService.releaseSeats("INVALID", 1);
        });
    }

    // Test: Update Seats - Inventory Missing (Edge Case / Branch coverage)
    @Test
    void testUpdateSeats_InventoryMissing() {
        Flight flight = new Flight();
        flight.setFlightId("F1");
        flight.setAvailableSeats(10);

        when(flightRepository.findById("F1")).thenReturn(Optional.of(flight));
        when(inventoryRepository.findByFlightId("F1")).thenReturn(Optional.empty()); // Missing inventory

        // Should not throw exception, just log warning
        flightService.updateAvailableSeats("F1", 1);
        
        verify(flightRepository).save(flight);
        verify(inventoryRepository, never()).save(any());
    }

    // Test: Release Seats - Inventory Missing (Edge Case / Branch coverage)
    @Test
    void testReleaseSeats_InventoryMissing() {
        Flight flight = new Flight();
        flight.setFlightId("F1");
        flight.setAvailableSeats(5);

        when(flightRepository.findById("F1")).thenReturn(Optional.of(flight));
        when(inventoryRepository.findByFlightId("F1")).thenReturn(Optional.empty()); // Missing inventory

        flightService.releaseSeats("F1", 1);
        
        verify(flightRepository).save(flight);
        verify(inventoryRepository, never()).save(any());
    }
}