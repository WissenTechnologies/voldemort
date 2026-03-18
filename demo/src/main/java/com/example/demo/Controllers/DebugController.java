package com.example.demo.Controllers;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entities.OtpToken;
import com.example.demo.Entities.User;
import com.example.demo.Repository.OtpTokenRepository;
import com.example.demo.Repository.AuthRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/debug")
@Tag(name = "Debug", description = "Debug endpoints for testing")
public class DebugController {

    @Autowired
    private OtpTokenRepository otpTokenRepository;
    
    @Autowired
    private AuthRepo userRepository;

    @GetMapping("/latest-otp")
    @Operation(summary = "Get latest OTP for debugging", description = "Returns the latest OTP for a given email (for testing only)")
    public ResponseEntity<?> getLatestOtp(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found: " + email);
        }
        
        User user = userOptional.get();
        Optional<OtpToken> otpOptional = otpTokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        
        if (otpOptional.isPresent()) {
            OtpToken otp = otpOptional.get();
            return ResponseEntity.ok(String.format(
                "Latest OTP for %s: %s (Created: %s, Expires: %s, Used: %s)",
                email, 
                otp.getOtp(), 
                otp.getCreatedAt(), 
                otp.getExpiresAt(), 
                otp.isUsed()
            ));
        }
        
        return ResponseEntity.ok("No OTP found for email: " + email);
    }

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Returns all users in the system (for debugging only)")
    public ResponseEntity<?> listUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.ok("No users found in the system");
        }
        
        StringBuilder result = new StringBuilder("Users in system:\n");
        for (User user : users) {
            result.append(String.format("- Email: %s, Username: %s, Role: %s, Enabled: %s\n",
                user.getEmail(), user.getUsername(), user.getRole().name(), user.isEnabled()));
        }
        
        return ResponseEntity.ok(result.toString());
    }
}
