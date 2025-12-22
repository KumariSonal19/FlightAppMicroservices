package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.entity.Booking;
import com.flightapp.bookingservice.enums.BookingStatus;
import com.flightapp.bookingservice.enums.MealPreference;
import com.flightapp.bookingservice.feign.FlightDTO;
import com.flightapp.bookingservice.messaging.BookingEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime; 
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PojoTest {

    @Test
    void testAllPojos() {
        assertDoesNotThrow(() -> {
            testBookingRequest();
//            testBookingResponse();
            testBookingEntity();
            testBookingEvent();
            testFlightDTO();
            testEnums();
        });
    }

    void testBookingRequest() {
        BookingRequest r1 = new BookingRequest("FL1", "a@b.com", "U", 1, List.of(), List.of(), MealPreference.VEG, LocalDate.now());
        BookingRequest r2 = new BookingRequest("FL1", "a@b.com", "U", 1, List.of(), List.of(), MealPreference.VEG, LocalDate.now());
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());
        
        BookingRequest empty = new BookingRequest();
        empty.setFlightId("X");
        assertEquals("X", empty.getFlightId());
    }

//    void testBookingResponse() {
//        BookingResponse r1 = new BookingResponse("B1", "PNR", "FL", "E", "U", 1, null, MealPreference.VEG, 10.0, BookingStatus.CONFIRMED, LocalDate.now(), 1L);
//        BookingResponse r2 = new BookingResponse("B1", "PNR", "FL", "E", "U", 1, null, MealPreference.VEG, 10.0, BookingStatus.CONFIRMED, LocalDate.now(), 1L);
//        assertEquals(r1, r2);
//        assertEquals(r1.hashCode(), r2.hashCode());
//        assertNotNull(r1.toString());
//    }

    void testBookingEntity() {
        Booking b1 = new Booking("B1", "PNR", "FL", "E", "U", 1, null, null, MealPreference.VEG, 10.0, BookingStatus.CONFIRMED, LocalDate.now(), 1L, 2L);
        Booking b2 = new Booking("B1", "PNR", "FL", "E", "U", 1, null, null, MealPreference.VEG, 10.0, BookingStatus.CONFIRMED, LocalDate.now(), 1L, 2L);
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotNull(b1.toString());
    }

    void testBookingEvent() {
        BookingEvent e1 = new BookingEvent("PNR", "E", "U", "FL", 1, 10.0, "C", 1L);
        BookingEvent e2 = new BookingEvent("PNR", "E", "U", "FL", 1, 10.0, "C", 1L);
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotNull(e1.toString());
    }

    void testFlightDTO() {
        ZonedDateTime now = ZonedDateTime.now();
        
        FlightDTO f1 = new FlightDTO("F1", "AI", "N", "S", "D", now, now, 100.0, 10, "A");
        FlightDTO f2 = new FlightDTO("F1", "AI", "N", "S", "D", now, now, 100.0, 10, "A");
        
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotNull(f1.toString());
    }

    void testEnums() {
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.valueOf("CONFIRMED"));
        assertEquals(MealPreference.VEG, MealPreference.valueOf("VEG"));
    }
}