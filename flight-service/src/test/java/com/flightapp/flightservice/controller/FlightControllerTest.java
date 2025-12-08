package com.flightapp.flightservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.flightservice.dto.FlightResponse;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.InventoryAddRequest;
import com.flightapp.flightservice.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZonedDateTime; 
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
public class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddInventory_Created() throws Exception {
        InventoryAddRequest request = new InventoryAddRequest();
        request.setAirlineCode("AI");
        request.setAirlineName("Test Air");
        request.setSource("A");
        request.setDestination("B");
        request.setPrice(100.0);
        request.setTotalSeats(100);
        request.setAircraft("747");
        
        // FIXED: Added required Date fields to pass validation
        request.setDepartureTime(ZonedDateTime.now());
        request.setArrivalTime(ZonedDateTime.now().plusHours(2));

        FlightResponse response = new FlightResponse();
        response.setFlightId("F123");

        when(flightService.addFlightInventory(any(InventoryAddRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/flight/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightId").value("F123"));
    }

    @Test
    void testSearchFlights_Success() throws Exception {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setSource("DEL");
        request.setDestination("BOM");
        request.setDepartureDate(LocalDate.now());
        request.setJourneyType("ONE_WAY"); 

        FlightResponse response = new FlightResponse();
        response.setFlightId("F123");

        when(flightService.searchFlights(any(FlightSearchRequest.class))).thenReturn(List.of(response));

        mockMvc.perform(post("/api/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void testGetFlightById_Found() throws Exception {
        FlightResponse response = new FlightResponse();
        response.setFlightId("F123");

        when(flightService.getFlightById("F123")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/flight/F123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightId").value("F123"));
    }

    @Test
    void testGetFlightById_NotFound() throws Exception {
        when(flightService.getFlightById("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/flight/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchFlights_ValidationFailure() throws Exception {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setDestination("BOM");
        request.setJourneyType("ONE_WAY");
        
        mockMvc.perform(post("/api/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.source").value("Provide the journey source"));
    }

    @Test
    void testUpdateSeats_Success() throws Exception {
        List<String> seats = List.of("1A", "1B");
        doNothing().when(flightService).updateAvailableSeats("F1", seats);

        mockMvc.perform(put("/api/flight/F1/update-seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seats)))
                .andExpect(status().isOk())
                .andExpect(content().string("Seats updated successfully"));
    }

    @Test
    void testUpdateSeats_Failure() throws Exception {
        List<String> seats = List.of("1A");
        doThrow(new RuntimeException("Error")).when(flightService).updateAvailableSeats("F1", seats);

        mockMvc.perform(put("/api/flight/F1/update-seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seats)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testReleaseSeats_Success() throws Exception {
        List<String> seats = List.of("1A");
        doNothing().when(flightService).releaseSeats("F1", seats);

        mockMvc.perform(put("/api/flight/F1/release-seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seats)))
                .andExpect(status().isOk())
                .andExpect(content().string("Seats released successfully"));
    }

    @Test
    void testReleaseSeats_Failure() throws Exception {
        List<String> seats = List.of("1A");
        doThrow(new RuntimeException("Error")).when(flightService).releaseSeats("F1", seats);

        mockMvc.perform(put("/api/flight/F1/release-seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seats)))
                .andExpect(status().isBadRequest());
    }
}