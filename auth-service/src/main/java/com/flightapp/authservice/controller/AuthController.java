package com.flightapp.authservice.controller;

import com.flightapp.authservice.payload.request.LoginRequest;
import com.flightapp.authservice.payload.request.SignupRequest;
import com.flightapp.authservice.payload.response.JwtResponse;
import com.flightapp.authservice.payload.response.MessageResponse;
import com.flightapp.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup request for username: {}", signupRequest.getUsername());
        MessageResponse response = authService.registerUser(signupRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Signin request for username: {}", loginRequest.getUsername());
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            log.info("User signin successful: {}", loginRequest.getUsername());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            log.error("Signin failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid username or password"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.debug("Health check request");
        return ResponseEntity.ok("Authentication service is running");
    }
}
