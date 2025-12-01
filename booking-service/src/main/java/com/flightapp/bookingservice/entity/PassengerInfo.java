package com.flightapp.bookingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerInfo {
    private String name;
    private String gender;
    private Integer age;
}
