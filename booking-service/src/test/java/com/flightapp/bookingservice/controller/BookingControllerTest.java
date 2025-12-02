package com.flightapp.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @MockBean
    private BookingService bookingService; 

    @Autowired
    private ObjectMapper objectMapper; 

    @Test
    void testBookFlight_Endpoint_Created() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setFlightId("FL123");
        
        BookingResponse response = new BookingResponse();
        response.setPnr("PNR_TEST");
        response.setBookingStatus("CONFIRMED");

        when(bookingService.bookFlight(any(BookingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/booking/flight/FL123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.pnr").value("PNR_TEST")); 
    }

    @Test
    void testGetBookingByPnr_Found() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setPnr("PNR123");

        when(bookingService.getBookingByPnr("PNR123")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/booking/ticket/PNR123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pnr").value("PNR123"));
    }

    @Test
    void testGetBookingByPnr_NotFound() throws Exception {
        when(bookingService.getBookingByPnr("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/booking/ticket/INVALID"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetBookingHistory_Found() throws Exception {
        BookingResponse b1 = new BookingResponse();
        b1.setPnr("PNR1");
        
        when(bookingService.getBookingHistory("test@email.com")).thenReturn(List.of(b1));

        mockMvc.perform(get("/api/booking/history/test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pnr").value("PNR1"));
    }

    @Test
    void testGetBookingHistory_NotFound() throws Exception {
        when(bookingService.getBookingHistory("empty@email.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/booking/history/empty@email.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelBooking_Success() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setBookingStatus("CANCELLED");

        when(bookingService.cancelBooking("PNR123")).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/booking/cancel/PNR123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"));
    }

    @Test
    void testCancelBooking_Failure() throws Exception {
        when(bookingService.cancelBooking("INVALID")).thenThrow(new RuntimeException("Cannot cancel"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/booking/cancel/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("Cancellation failed: Cannot cancel"));
    }
}