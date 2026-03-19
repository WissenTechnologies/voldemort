package com.example.demo.Controllers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUserByAdmin(@RequestBody User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is required");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        user.setEnabled(true);

        User created = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUserByAdmin(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

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
                    user.getId(),
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
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                otpTokenRepository.deleteByUser(user);

                String otp = String.format("%06d", (int)(Math.random() * 999999));

                OtpToken otpToken = new OtpToken();
                otpToken.setUser(user);
                otpToken.setOtp(otp);
                otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
                otpToken.setUsed(false);
                otpTokenRepository.save(otpToken);

                emailService.sendPasswordResetOtpEmail(user.getEmail(), otp);
                return ResponseEntity.ok("OTP sent to your email. Please verify.");
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("forgot-password failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
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
        try {
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
            emailService.sendPasswordResetOtpEmail(user.getEmail(), otp);
            return ResponseEntity.ok("OTP sent successfully to your email. Valid for 10 minutes.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("send-otp failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
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

        // Find verified OTP (used=true) that is still valid
        Optional<OtpToken> verifiedTokenOptional = otpTokenRepository.findTopByUserAndUsedTrueOrderByExpiresAtDesc(user);

        if (verifiedTokenOptional.isPresent()) {
            OtpToken otpToken = verifiedTokenOptional.get();

            // Check if still valid (not expired)
            if (otpToken.getExpiresAt().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                // Clean up OTP tokens
                otpTokenRepository.deleteByUser(user);

                return ResponseEntity.ok("Password reset successfully");
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("OTP has expired. Please request a new OTP.");
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

