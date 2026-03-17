package com.example.demo.Controllers;



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

import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.dto.LoginRequest;
import com.example.demo.utils.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

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
}
