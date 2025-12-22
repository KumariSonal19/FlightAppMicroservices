package com.flightapp.authservice.controller;

import com.flightapp.authservice.model.User;
import com.flightapp.authservice.payload.request.ChangePasswordRequest;
import com.flightapp.authservice.payload.request.LoginRequest;
import com.flightapp.authservice.payload.request.SignupRequest;
import com.flightapp.authservice.payload.response.JwtResponse;
import com.flightapp.authservice.payload.response.MessageResponse;
import com.flightapp.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.flightapp.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup request for username: {}", signupRequest.getUsername());
        authService.registerUser(signupRequest);
        return ResponseEntity.ok("User registered successfully!");
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

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Incorrect old password!"));
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password updated successfully!"));
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.debug("Health check request");
        return ResponseEntity.ok("Authentication service is running");
    }
}
