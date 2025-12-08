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
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
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
    void testAddFlightInventory() {
        InventoryAddRequest req = new InventoryAddRequest("AI", "Air India", "DEL", "BOM", ZonedDateTime.now(), ZonedDateTime.now(), 100.0, 100, "737");
        Flight savedFlight = new Flight();
        savedFlight.setFlightId("F1");
        
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);
        when(inventoryRepository.save(any(FlightInventory.class))).thenReturn(new FlightInventory());

        FlightResponse res = flightService.addFlightInventory(req);
        Assertions.assertEquals("F1", res.getFlightId());
    }

    @Test
    void testSearchFlights() {
        FlightSearchRequest req = new FlightSearchRequest("DEL", "BOM", LocalDate.now(), "ONE_WAY");
        
        Flight mockFlight = new Flight();
        mockFlight.setDepartureTime(ZonedDateTime.now()); 

        when(flightRepository.findBySourceAndDestination(any(), any()))
                .thenReturn(List.of(mockFlight));
        
        List<FlightResponse> res = flightService.searchFlights(req);
        Assertions.assertEquals(1, res.size());
    }
    
    @Test
    void testGetFlightById() {
        when(flightRepository.findById("F1")).thenReturn(Optional.of(new Flight()));
        Assertions.assertTrue(flightService.getFlightById("F1").isPresent());
    }

    @Test
    void testUpdateSeats_Success() {
        Flight f = new Flight();
        f.setFlightId("F1");
        f.setTotalSeats(10); 
        
        f.setAvailableSeats(10);
        f.setOccupiedSeats(new HashSet<>());
        
        List<String> seats = List.of("1A", "1B");

        when(flightRepository.findById("F1")).thenReturn(Optional.of(f));

        flightService.updateAvailableSeats("F1", seats);
        Assertions.assertEquals(8, f.getAvailableSeats());
        verify(flightRepository).save(f);
    }

    @Test
    void testUpdateSeats_NotFound() {
        List<String> seats = List.of("1A");
        when(flightRepository.findById("INVALID")).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class, () -> flightService.updateAvailableSeats("INVALID", seats));
    }

    @Test
    void testUpdateSeats_NotEnough() {
        Flight f = new Flight();
        f.setAvailableSeats(1); 
        when(flightRepository.findById("F1")).thenReturn(Optional.of(f));
        List<String> seats = List.of("1A", "1B");
        Assertions.assertThrows(RuntimeException.class, () -> flightService.updateAvailableSeats("F1", seats));
    }

    @Test
    void testUpdateSeats_AlreadyOccupied() {
        Flight f = new Flight();
        f.setAvailableSeats(10);
        f.setOccupiedSeats(new HashSet<>(List.of("1A"))); 

        when(flightRepository.findById("F1")).thenReturn(Optional.of(f));
        List<String> seats = List.of("1A");

        Assertions.assertThrows(RuntimeException.class, () -> flightService.updateAvailableSeats("F1", seats));
    }

    @Test
    void testReleaseSeats_Success() {
        Flight f = new Flight();
        f.setFlightId("F1");
        f.setTotalSeats(10);
     
        f.setOccupiedSeats(new HashSet<>(List.of("1A", "1B", "1C"))); 
        f.setAvailableSeats(7); 

        when(flightRepository.findById("F1")).thenReturn(Optional.of(f));

        flightService.releaseSeats("F1", List.of("1A", "1B"));

        Assertions.assertEquals(9, f.getAvailableSeats()); 
    }
    
    @Test
    void testReleaseSeats_NotFound() {
        List<String> seats = List.of("1A");
        when(flightRepository.findById("INVALID")).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class, () -> flightService.releaseSeats("INVALID", seats));
    }
    
}