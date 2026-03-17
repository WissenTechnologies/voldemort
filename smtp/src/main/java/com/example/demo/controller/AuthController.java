package com.example.demo.controller;

import com.example.demo.dto.EmailRequest;
import com.example.demo.dto.OtpRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetRequest;
import com.example.demo.entities.User;
import com.example.demo.services.EmailService;
import com.example.demo.services.OtpService;
import com.example.demo.services.PasswordResetService;
import com.example.demo.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    // manual constructor — replaces @RequiredArgsConstructor
    public AuthController(UserService userService, OtpService otpService,
                          EmailService emailService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User user = userService.createUser(req);
        String otp = otpService.generateAndSaveOtp(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return ResponseEntity.ok("OTP sent to " + user.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest req) {
        User user = userService.findByEmail(req.getEmail());
        if (otpService.verifyOtp(user, req.getOtp())) {
            user.setEmailVerified(true);
            userService.save(user);
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            return ResponseEntity.ok("Email verified. Welcome aboard!");
        }
        return ResponseEntity.status(400).body("Invalid or expired OTP");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequest req) {
        userService.findByEmailOptional(req.getEmail()).ifPresent(user -> {
            String link = passwordResetService.generateResetLink(user);
            emailService.sendPasswordResetEmail(user.getEmail(), link);
        });
        return ResponseEntity.ok("If that email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetRequest req) {
        if (passwordResetService.resetPassword(req.getToken(), req.getNewPassword())) {
            return ResponseEntity.ok("Password updated successfully.");
        }
        return ResponseEntity.status(400).body("Link is invalid or expired.");
    }
}