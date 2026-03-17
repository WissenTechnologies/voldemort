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
import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.Repository.TokenRepo;
import com.example.demo.Services.EmailService;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ResetPasswordRequest;
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
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User registered but unable to send welcome email");
        }

        return ResponseEntity.ok("User Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if(authentication.isAuthenticated()){

                User user = userRepository.findByEmail(request.getEmail()).get();

                String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

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


}

