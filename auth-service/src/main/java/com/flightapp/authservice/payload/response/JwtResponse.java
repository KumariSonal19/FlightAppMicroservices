package com.flightapp.authservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String username;
    private String email;
    private List<String> roles;
    private boolean passwordExpired;
    
    public JwtResponse(String id, String username, String email, List<String> roles, String token,boolean passwordExpired) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.passwordExpired = passwordExpired;
    }
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public boolean isPasswordExpired() { return passwordExpired; }
    
    
}