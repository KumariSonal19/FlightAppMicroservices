package com.flightapp.authservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private String message;
    private boolean success;
    private long timestamp;

    public MessageResponse(String message) {
        this.message = message;
        this.success = true;
        this.timestamp = System.currentTimeMillis();
    }
}
