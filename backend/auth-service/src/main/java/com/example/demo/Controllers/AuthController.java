package com.example.demo.Controllers;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.example.demo.utils.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
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

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody User user) {

    if(userRepository.findByEmail(user.getEmail()).isPresent()){
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
    }
    if(userRepository.findByUsername(user.getUsername()).isPresent()){
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
    }
    if(user.getRole() != null && user.getRole() == Role.ADMIN){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin registration not allowed");
    }
    

    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setRole(Role.USER);

    // ❗ IMPORTANT
    user.setEnabled(false);

    userRepository.save(user);

    // 🔥 Send OTP instead of welcome email
    String otp = String.format("%06d", (int)(Math.random() * 999999));

    OtpToken otpToken = new OtpToken();
    otpToken.setUser(user);
    otpToken.setOtp(otp);
    otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
    otpToken.setUsed(false);

    otpTokenRepository.save(otpToken);

    emailService.sendOtpEmail(user.getEmail(), otp);

    return ResponseEntity.ok("OTP sent to email. Please verify.");
}

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    try {

        // Step 1: Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        if(authentication.isAuthenticated()){

            // Step 2: Fetch user from DB
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 🔥 Step 3: Check email verification
            if(!user.isEnabled()){
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Please verify your email before logging in");
            }

            // Step 4: Generate JWT
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );

            // Step 5: Return token
            return ResponseEntity.ok(token);
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Login Failed");

    } catch (Exception e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Email or Password");
    }
}
    @PostMapping("/forgot-password")
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

    @PostMapping("/reset-password-with-otp")
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

    @PostMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestBody VerifyOtpRequest request) {

    Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

    if (!userOptional.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    User user = userOptional.get();

    Optional<OtpToken> otpTokenOptional =
            otpTokenRepository.findByOtpAndUserAndUsedFalse(request.getOtp(), user);

    if (!otpTokenOptional.isPresent()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }

    OtpToken otpToken = otpTokenOptional.get();

    if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP expired");
    }

    otpToken.setUsed(true);
    otpTokenRepository.save(otpToken);

    // 🔥 ACTIVATE USER
    user.setEnabled(true);
    userRepository.save(user);

    // 🎉 Send welcome email
    emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

    return ResponseEntity.ok("Email verified successfully");
}


}

