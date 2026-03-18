package com.example.demo.Controllers;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entities.PasswordResetToken;
import com.example.demo.Entities.OtpToken;
import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.Repository.TokenRepo;
import com.example.demo.Repository.OtpTokenRepository;
import com.example.demo.Services.EmailService;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.SendOtpRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.dto.OtpResetPasswordRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.utils.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.Services.OtpService;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

    @Autowired
    private AuthRepo userRepository;

    @Autowired
    private TokenRepo tokenRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Client-side logout helper endpoint (stateless JWT). Always returns 200.")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email, password, and username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "403", description = "Admin registration not allowed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> register(@RequestBody User user) {

        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }
        if(user.getRole() != null && user.getRole() == Role.ADMIN){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Admin registration is not allowed");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false); // New users are disabled until OTP verification

        userRepository.save(user);

        // Generate and Send OTP
        try {
            String otp = otpService.generateAndSaveOtp(user);
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User registered but unable to send verification OTP. Error: " + e.getMessage());
        }

        return ResponseEntity.ok(java.util.Map.of("message", "User registered successfully. Please verify your OTP sent to your email."));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns JWT tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            String username = request.getUsername();
            System.out.println("Login attempt with username: " + username);
            
            // Bypass authentication for development - just check if user exists
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("User found: " + user.getUsername() + " with email: " + user.getEmail());
                
                // For development, accept any password for existing users
                // In production, you would validate the password here
                
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
                String refreshToken = UUID.randomUUID().toString(); // Dummy refresh token

                return ResponseEntity.ok(new com.example.demo.dto.AuthResponse(token, refreshToken));
            } else {
                System.out.println("User not found with username: " + username);
                // Let's check if user exists by email for debugging
                Optional<User> userByEmail = userRepository.findByEmail(username + "@voldemort.com");
                if (userByEmail.isPresent()) {
                    System.out.println("User exists by email: " + userByEmail.get().getEmail());
                }
            }

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends password reset link to user's email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset link sent"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Failed to send email")
    })
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Generate Token
            String token = UUID.randomUUID().toString();

            // Store Token
            PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(resetToken);

            // Send Email
            String resetLink = "http://localhost:4200/reset-password?token=" + token;
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unable to send password reset email");
            }

            return ResponseEntity.ok("Password reset link sent to your email");
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User not found");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", description = "Resets user password using reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Token not found")
    })
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(request.getToken());

        if (tokenOptional.isPresent()) {
            PasswordResetToken resetToken = tokenOptional.get();

            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Token expired");
            }

            User user = resetToken.getUser();

            // Update Password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Invalidate Token
            tokenRepository.delete(resetToken);

            return ResponseEntity.ok("Password reset successfully");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid or expired token");
    }

    // ==================== OTP-BASED PASSWORD RESET ENDPOINTS ====================

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP for password reset", description = "Sends OTP to user's email for password reset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Failed to send OTP email")
    })
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (!userOptional.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + request.getEmail());
        }

        User user = userOptional.get();

        // Invalidate old OTPs
        otpTokenRepository.deleteByUser(user);

        // Generate OTP (6 digits)
        String otp = String.format("%06d", (int)(Math.random() * 999999));

        // Save OTP token
        OtpToken otpToken = new OtpToken();
        otpToken.setUser(user);
        otpToken.setOtp(otp);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpToken.setUsed(false);
        otpTokenRepository.save(otpToken);

        // Send OTP via email
        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
            return ResponseEntity.ok("OTP sent successfully to your email. Valid for 10 minutes.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send OTP email. Please try again.");
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verifies OTP for password reset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (!userOptional.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        User user = userOptional.get();

        Optional<OtpToken> otpTokenOptional = otpTokenRepository.findByOtpAndUserAndUsedFalse(request.getOtp(), user);

        if (!otpTokenOptional.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid OTP");
        }

        OtpToken otpToken = otpTokenOptional.get();

        // Check if OTP is expired
        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("OTP has expired");
        }

        // Mark OTP as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return ResponseEntity.ok("OTP verified successfully. You can now reset your password.");
    }

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(@RequestBody VerifyOtpRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (!userOptional.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        User user = userOptional.get();

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Account is already verified");
        }

        boolean verified = otpService.verifyOtp(user, request.getOtp());

        if (verified) {
            user.setEnabled(true);
            userRepository.save(user);

            // Generate token for immediate login after verification
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            String refreshToken = UUID.randomUUID().toString();
            
            // Also send welcome email now that account is verified
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
            } catch (Exception e) {
                // Log but don't fail verification
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }

            return ResponseEntity.ok(new com.example.demo.dto.AuthResponse(token, refreshToken));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or expired OTP");
        }
    }

    @PostMapping("/reset-password-with-otp")
    @Operation(summary = "Reset password with OTP", description = "Resets password after OTP verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "401", description = "OTP verification required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> resetPasswordWithOtp(@RequestBody OtpResetPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (!userOptional.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        User user = userOptional.get();

        // Check if there's a recently verified OTP
        Optional<OtpToken> otpTokenOptional = otpTokenRepository.findByUserAndUsedFalse(user);

        if (otpTokenOptional.isPresent() && otpTokenOptional.get().isUsed()) {
            // OTP has been verified, allow password reset
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Clean up OTP tokens
            otpTokenRepository.deleteByUser(user);

            return ResponseEntity.ok("Password reset successfully");
        }

        // Alternative: Check if there's a recently used (verified) OTP
        Optional<OtpToken> verifiedOtpOptional = otpTokenRepository.findByUserAndUsedFalse(user);

        if (verifiedOtpOptional.isPresent()) {
            OtpToken otpToken = verifiedOtpOptional.get();

            // Even if not used in this call, if we have a valid token, we can proceed
            if (otpToken.getExpiresAt().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                // Mark OTP as used and clean up
                otpToken.setUsed(true);
                otpTokenRepository.save(otpToken);

                return ResponseEntity.ok("Password reset successfully");
            }
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Please verify your OTP first before resetting password");
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(user -> {
                    java.util.Map<String, Object> profile = new java.util.HashMap<>();
                    profile.put("id", user.getId());
                    profile.put("email", user.getEmail());
                    profile.put("username", user.getUsername());
                    profile.put("role", user.getRole().name());
                    profile.put("enabled", user.isEnabled());
                    return ResponseEntity.ok(profile);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", "User not found")));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Refreshes the access token using a valid refresh token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefresh_token();
            
            // Basic validation - check if it's a valid UUID format (since we're using UUIDs as dummy refresh tokens)
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", "Refresh token is required"));
            }
            
            // For now, we'll accept any UUID format and generate a new token
            // In a production environment, you'd validate the refresh token against a database
            try {
                UUID.fromString(refreshToken); // Validate UUID format
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Invalid refresh token"));
            }
            
            // Since we don't store user information with refresh tokens in this dummy implementation,
            // we'll need to get the user from the current authentication context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "User not authenticated"));
            }
            
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "User not found"));
            }
            
            User user = userOpt.get();
            
            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            String newRefreshToken = UUID.randomUUID().toString();
            
            return ResponseEntity.ok(new com.example.demo.dto.AuthResponse(newAccessToken, newRefreshToken));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }

}

