package com.flightapp.flightservice;

import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightInventory;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class PojoTest {

    @Test
    void testAllPojos() {
        testFlight();
        testFlightInventory();
        testFlightSearchRequest();
        testFlightResponse();
        testInventoryAddRequest();
    }

    private void testFlight() {
        Flight f1 = new Flight();
        f1.setFlightId("1");
        f1.setOccupiedSeats(new HashSet<>()); 
        f1.setAirlineCode("AI");
        f1.setAirlineName("Air India");
        f1.setSource("A");
        f1.setDestination("B");
        f1.setDepartureTime(ZonedDateTime.now()); 
        f1.setArrivalTime(ZonedDateTime.now());   
        f1.setPrice(100.0);
        f1.setTotalSeats(100);
        f1.setAvailableSeats(50);
        f1.setAircraft("747");
        f1.setCreatedAt(1L);
        f1.setUpdatedAt(2L);
        f1.setVersion(0L); 

        assertNotNull(f1.getFlightId());
        assertNotNull(f1.getOccupiedSeats());
        assertNotNull(f1.getDepartureTime());
        assertNotNull(f1.getVersion());
        assertNotNull(f1.toString());

        assertEquals(f1, f1);
        assertNotEquals(f1, null);
        assertNotEquals(f1, new Object());
  
        Flight f2 = new Flight();
        f2.setFlightId("1");
        f2.setOccupiedSeats(f1.getOccupiedSeats());
        f2.setAirlineCode("AI");
        f2.setAirlineName("Air India");
        f2.setSource("A");
        f2.setDestination("B");
        f2.setDepartureTime(f1.getDepartureTime());
        f2.setArrivalTime(f1.getArrivalTime());
        f2.setPrice(100.0);
        f2.setTotalSeats(100);
        f2.setAvailableSeats(50);
        f2.setAircraft("747");
        f2.setCreatedAt(1L);
        f2.setUpdatedAt(2L);
        f2.setVersion(0L);

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    private void testFlightInventory() {
        FlightInventory i1 = new FlightInventory("1", "F1", 100, 10, 90, 1L, 2L);
        FlightInventory i2 = new FlightInventory("1", "F1", 100, 10, 90, 1L, 2L);

        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
        assertNotNull(i1.toString());
    }

    private void testFlightSearchRequest() {
        LocalDate date = LocalDate.now();
        FlightSearchRequest r1 = new FlightSearchRequest("A", "B", date, "ONE_WAY");
        FlightSearchRequest r2 = new FlightSearchRequest("A", "B", date, "ONE_WAY");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
    }

    private void testFlightResponse() {
        ZonedDateTime now = ZonedDateTime.now();
        FlightResponse r1 = new FlightResponse("F1", "AI", "Air", "A", "B", now, now, 100.0, 50, "747");
        FlightResponse r2 = new FlightResponse("F1", "AI", "Air", "A", "B", now, now, 100.0, 50, "747");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
    }

    private void testInventoryAddRequest() {
        ZonedDateTime now = ZonedDateTime.now();
        InventoryAddRequest r1 = new InventoryAddRequest("AI", "Test", "A", "B", now, now, 100.0, 100, "747");
        InventoryAddRequest r2 = new InventoryAddRequest("AI", "Test", "A", "B", now, now, 100.0, 100, "747");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
    }
}