package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.entity.PassengerInfo;
import com.flightapp.bookingservice.feign.FlightDTO;
import com.flightapp.bookingservice.messaging.BookingEvent;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PojoTest {

    @Test
    void testBookingRequest() {
        BookingRequest r1 = new BookingRequest("FL1", "a@b.com", "User", 1, List.of(new PassengerRequest("P1", "M", 20)), List.of("1A"), "VEG", LocalDate.now());
        BookingRequest r2 = new BookingRequest("FL1", "a@b.com", "User", 1, List.of(new PassengerRequest("P1", "M", 20)), List.of("1A"), "VEG", LocalDate.now());
        
        assertEquals(r1, r2);
        assertEquals(r1, r1); 
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, null);
        assertNotEquals(r1, new Object());
 
        BookingRequest empty = new BookingRequest();
        empty.setFlightId("X");
        assertEquals("X", empty.getFlightId());
        assertNotNull(r1.toString());
    }

    @Test
    void testBookingResponse() {
        BookingResponse r1 = new BookingResponse("B1", "PNR1", "FL1", "u", "n", 1, null, "VEG", 100.0, "CONFIRMED", LocalDate.now(), 1L);
        BookingResponse r2 = new BookingResponse("B1", "PNR1", "FL1", "u", "n", 1, null, "VEG", 100.0, "CONFIRMED", LocalDate.now(), 1L);

        assertEquals(r1, r2);
        assertEquals(r1, r1);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, null);
        assertNotEquals(r1, new Object());
        
        BookingResponse empty = new BookingResponse();
        empty.setBookingId("X");
        assertNotNull(empty.getBookingId());
        assertNotNull(r1.toString());
    }

    @Test
    void testPassengerRequest() {
        PassengerRequest p1 = new PassengerRequest("John", "M", 30);
        PassengerRequest p2 = new PassengerRequest("John", "M", 30);
        
        assertEquals(p1, p2);
        assertEquals(p1, p1);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, null);
        assertNotEquals(p1, new Object());
        
        PassengerRequest empty = new PassengerRequest();
        empty.setName("X");
        assertNotNull(empty.getName());
        assertNotNull(p1.toString());
    }

    @Test
    void testBookingEntity() {
        List<PassengerInfo> passengers = List.of(new PassengerInfo("P1", "F", 25));
        Booking b1 = new Booking("B1", "PNR1", "F1", "e", "u", 1, passengers, null, "V", 100.0, "C", LocalDate.now(), 1L, 2L);
        Booking b2 = new Booking("B1", "PNR1", "F1", "e", "u", 1, passengers, null, "V", 100.0, "C", LocalDate.now(), 1L, 2L);

        assertEquals(b1, b2);
        assertEquals(b1, b1);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, null);
        assertNotEquals(b1, new Object());
        
        Booking empty = new Booking();
        empty.setBookingId("X");
        assertNotNull(empty.getBookingId());
        assertNotNull(b1.toString());
    }

    @Test
    void testPassengerInfo() {
        PassengerInfo p1 = new PassengerInfo("Jane", "F", 22);
        PassengerInfo p2 = new PassengerInfo("Jane", "F", 22);
        
        assertEquals(p1, p2);
        assertEquals(p1, p1);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, null);
        assertNotEquals(p1, new Object());
        
        assertNotNull(p1.toString());
    }

    @Test
    void testBookingEvent() {
        BookingEvent e1 = new BookingEvent("PNR", "e", "u", "f", 1, 100.0, "C", 1L);
        BookingEvent e2 = new BookingEvent("PNR", "e", "u", "f", 1, 100.0, "C", 1L);
        
        assertEquals(e1, e2);
        assertEquals(e1, e1);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, null);
        assertNotEquals(e1, new Object());
        
        assertNotNull(e1.toString());
    }

    @Test
    void testFlightDTO() {
        LocalDateTime now = LocalDateTime.now();
        FlightDTO f1 = new FlightDTO("F1", "AI", "N", "S", "D", now, now, 100.0, 10, "A");
        FlightDTO f2 = new FlightDTO("F1", "AI", "N", "S", "D", now, now, 100.0, 10, "A");
        
        assertEquals(f1, f2);
        assertEquals(f1, f1);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, null);
        assertNotEquals(f1, new Object());
        
        assertNotNull(f1.toString());
    }
}