package com.flightapp.flightservice.dto;

import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightInventory;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PojoTest {

    @Test
    void testFlightSearchRequest_FullCoverage() {
        // 1. Test NoArgsConstructor
        FlightSearchRequest dto = new FlightSearchRequest();
        
        // 2. Test Setters
        LocalDate date = LocalDate.now();
        dto.setSource("DEL");
        dto.setDestination("BOM");
        dto.setDepartureDate(date);
        dto.setJourneyType("ONE_WAY");

        // 3. Test Getters
        assertEquals("DEL", dto.getSource());
        assertEquals("BOM", dto.getDestination());
        assertEquals(date, dto.getDepartureDate());
        assertEquals("ONE_WAY", dto.getJourneyType());

        // 4. Test AllArgsConstructor
        FlightSearchRequest dto2 = new FlightSearchRequest("DEL", "BOM", date, "ONE_WAY");
        
        // 5. Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        
        // 6. Test toString
        assertNotNull(dto.toString());
        
        // 7. Test Not Equals
        FlightSearchRequest dto3 = new FlightSearchRequest("DXB", "LHR", date, "ROUND_TRIP");
        assertNotEquals(dto, dto3);
    }

    @Test
    void testFlightResponse_FullCoverage() {
        FlightResponse dto = new FlightResponse();
        LocalDateTime now = LocalDateTime.now();

        dto.setFlightId("F1");
        dto.setAirlineCode("AI");
        dto.setAirlineName("Air India");
        dto.setSource("DEL");
        dto.setDestination("BOM");
        dto.setDepartureTime(now);
        dto.setArrivalTime(now.plusHours(2));
        dto.setPrice(500.0);
        dto.setAvailableSeats(100);
        dto.setAircraft("Boeing");

        assertEquals("F1", dto.getFlightId());
        assertEquals("AI", dto.getAirlineCode());
        assertEquals("Air India", dto.getAirlineName());
        assertEquals("DEL", dto.getSource());
        assertEquals("BOM", dto.getDestination());
        assertEquals(now, dto.getDepartureTime());
        assertEquals(now.plusHours(2), dto.getArrivalTime());
        assertEquals(500.0, dto.getPrice());
        assertEquals(100, dto.getAvailableSeats());
        assertEquals("Boeing", dto.getAircraft());

        FlightResponse dto2 = new FlightResponse("F1", "AI", "Air India", "DEL", "BOM", now, now.plusHours(2), 500.0, 100, "Boeing");
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());
    }

    @Test
    void testInventoryAddRequest_FullCoverage() {
        InventoryAddRequest dto = new InventoryAddRequest();
        dto.setAirlineCode("AI");
        dto.setAirlineName("Test");
        dto.setSource("A");
        dto.setDestination("B");
        dto.setDepartureTime("10:00");
        dto.setArrivalTime("12:00");
        dto.setPrice(100.0);
        dto.setTotalSeats(50);
        dto.setAircraft("747");

        assertEquals("AI", dto.getAirlineCode());
        assertEquals("Test", dto.getAirlineName());
        assertEquals("A", dto.getSource());
        assertEquals("B", dto.getDestination());
        assertEquals("10:00", dto.getDepartureTime());
        assertEquals("12:00", dto.getArrivalTime());
        assertEquals(100.0, dto.getPrice());
        assertEquals(50, dto.getTotalSeats());
        assertEquals("747", dto.getAircraft());

        InventoryAddRequest dto2 = new InventoryAddRequest("AI", "Test", "A", "B", "10:00", "12:00", 100.0, 50, "747");
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());
    }

    @Test
    void testFlightEntity_FullCoverage() {
        Flight entity = new Flight();
        LocalDateTime now = LocalDateTime.now();

        entity.setFlightId("1");
        entity.setAirlineCode("AI");
        entity.setAirlineName("Air India");
        entity.setSource("DEL");
        entity.setDestination("BOM");
        entity.setDepartureTime(now);
        entity.setArrivalTime(now);
        entity.setPrice(100.0);
        entity.setTotalSeats(200);
        entity.setAvailableSeats(200);
        entity.setAircraft("A320");
        entity.setCreatedAt(1000L);
        entity.setUpdatedAt(2000L);

        assertEquals("1", entity.getFlightId());
        assertEquals("AI", entity.getAirlineCode());
        assertEquals("Air India", entity.getAirlineName());
        assertEquals("DEL", entity.getSource());
        assertEquals("BOM", entity.getDestination());
        assertEquals(now, entity.getDepartureTime());
        assertEquals(now, entity.getArrivalTime());
        assertEquals(100.0, entity.getPrice());
        assertEquals(200, entity.getTotalSeats());
        assertEquals(200, entity.getAvailableSeats());
        assertEquals("A320", entity.getAircraft());
        assertEquals(1000L, entity.getCreatedAt());
        assertEquals(2000L, entity.getUpdatedAt());

        Flight entity2 = new Flight("1", "AI", "Air India", "DEL", "BOM", now, now, 100.0, 200, 200, "A320", 1000L, 2000L);
        assertEquals(entity, entity2);
        assertEquals(entity.hashCode(), entity2.hashCode());
        assertNotNull(entity.toString());
    }

    @Test
    void testFlightInventoryEntity_FullCoverage() {
        FlightInventory inv = new FlightInventory();
        inv.setInventoryId("I1");
        inv.setFlightId("F1");
        inv.setTotalSeats(100);
        inv.setBookedSeats(10);
        inv.setAvailableSeats(90);
        inv.setCreatedAt(1L);
        inv.setUpdatedAt(2L);

        assertEquals("I1", inv.getInventoryId());
        assertEquals("F1", inv.getFlightId());
        assertEquals(100, inv.getTotalSeats());
        assertEquals(10, inv.getBookedSeats());
        assertEquals(90, inv.getAvailableSeats());
        assertEquals(1L, inv.getCreatedAt());
        assertEquals(2L, inv.getUpdatedAt());

        FlightInventory inv2 = new FlightInventory("I1", "F1", 100, 10, 90, 1L, 2L);
        assertEquals(inv, inv2);
        assertEquals(inv.hashCode(), inv2.hashCode());
        assertNotNull(inv.toString());
    }
}