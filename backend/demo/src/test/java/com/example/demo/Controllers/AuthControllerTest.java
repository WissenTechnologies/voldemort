package com.example.demo.Controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import com.example.demo.Entities.OtpToken;
import com.example.demo.Entities.PasswordResetToken;
import com.example.demo.Entities.Role;
import com.example.demo.Entities.User;
import com.example.demo.Repository.AuthRepo;
import com.example.demo.Repository.OtpTokenRepository;
import com.example.demo.Repository.TokenRepo;
import com.example.demo.Services.EmailService;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.OtpResetPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.SendOtpRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthRepo userRepository;

    @MockBean
    private TokenRepo tokenRepository;

    @MockBean
    private OtpTokenRepository otpTokenRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private EmailService emailService;

    @Test
    void getAllUsers_whenUsersExist_returnsUserList() throws Exception {
        User user1 = new User("user1@test.com", "password1", "user1");
        User user2 = new User("user2@test.com", "password2", "user2");
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("user1@test.com"))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].email").value("user2@test.com"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    void getAllUsers_whenNoUsers_returnsEmptyList() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createUserByAdmin_whenValidRequest_returnsCreatedUser() throws Exception {
        User user = new User("new@test.com", "password123", "newuser");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void createUserByAdmin_whenEmailExists_returnsConflict() throws Exception {
        User user = new User("existing@test.com", "password123", "existinguser");
        User existingUser = new User("existing@test.com", "oldpass", "olduser");

        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void createUserByAdmin_whenUsernameExists_returnsConflict() throws Exception {
        User user = new User("new@test.com", "password123", "existinguser");
        User existingUser = new User("old@test.com", "oldpass", "existinguser");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void createUserByAdmin_whenMissingEmail_returnsBadRequest() throws Exception {
        User user = new User("", "password123", "newuser");

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required"));
    }

    @Test
    void createUserByAdmin_whenMissingUsername_returnsBadRequest() throws Exception {
        User user = new User("new@test.com", "password123", "");

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is required"));
    }

    @Test
    void createUserByAdmin_whenMissingPassword_returnsBadRequest() throws Exception {
        User user = new User("new@test.com", "", "newuser");

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password is required"));
    }

    @Test
    void deleteUserByAdmin_whenUserExists_returnsNoContent() throws Exception {
        User user = new User("test@test.com", "password", "testuser");
        user.setId(1L);

        when(userRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/auth/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserByAdmin_whenUserNotFound_returnsNotFound() throws Exception {
        when(userRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/auth/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void register_whenValidRequest_returnsSuccessMessage() throws Exception {
        User user = new User("new@test.com", "password123", "newuser");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to email. Please verify."));
    }

    @Test
    void register_whenEmailExists_returnsConflict() throws Exception {
        User user = new User("existing@test.com", "password123", "newuser");
        User existingUser = new User("existing@test.com", "oldpass", "olduser");

        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void register_whenAdminRole_returnsForbidden() throws Exception {
        User user = new User("admin@test.com", "password123", "admin");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin registration not allowed"));
    }

    @Test
    void login_whenValidCredentials_returnsToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");
        User user = new User("test@test.com", "encodedPassword", "testuser");
        user.setId(1L);
        user.setRole(Role.USER);
        user.setEnabled(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken("test@test.com", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(1L, "test@test.com", "USER")).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void login_whenInvalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid Email or Password"));
    }

    @Test
    void login_whenUserNotEnabled_returnsForbidden() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");
        User user = new User("test@test.com", "encodedPassword", "testuser");
        user.setId(1L);
        user.setRole(Role.USER);
        user.setEnabled(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken("test@test.com", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Please verify your email before logging in"));
    }

    @Test
    void forgotPassword_whenUserExists_returnsSuccessMessage() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");
        User user = new User("test@test.com", "password", "testuser");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset link sent to your email"));

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@test.com"), anyString());

    }

    @Test
    void forgotPassword_whenUserNotFound_returnsNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@test.com");

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void resetPassword_whenValidToken_returnsSuccessMessage() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("newPassword123");
        User user = new User("test@test.com", "oldPassword", "testuser");
        PasswordResetToken resetToken = new PasswordResetToken("valid-token", user, java.time.LocalDateTime.now().plusMinutes(30));

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));

        verify(tokenRepository).delete(resetToken);
    }

    @Test
    void resetPassword_whenExpiredToken_returnsBadRequest() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNewPassword("newPassword123");
        User user = new User("test@test.com", "oldPassword", "testuser");
        PasswordResetToken resetToken = new PasswordResetToken("expired-token", user, java.time.LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(resetToken));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token expired"));
    }

    @Test
    void sendOtp_whenUserExists_returnsSuccessMessage() throws Exception {
        SendOtpRequest request = new SendOtpRequest("test@test.com");
        User user = new User("test@test.com", "password", "testuser");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully to your email. Valid for 10 minutes."));

        verify(otpTokenRepository).deleteByUser(user);
        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(emailService).sendOtpEmail(eq("test@test.com"), anyString());
    }

    @Test
    void sendOtp_whenUserNotFound_returnsNotFound() throws Exception {
        SendOtpRequest request = new SendOtpRequest("nonexistent@test.com");

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with email: nonexistent@test.com"));
    }

    @Test
    void verifyOtp_whenValidOtp_returnsSuccessMessage() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("test@test.com", "123456");
        User user = new User("test@test.com", "password", "testuser");
        OtpToken otpToken = new OtpToken();
        otpToken.setUser(user);
        otpToken.setOtp("123456");
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findByOtpAndUserAndUsedFalse("123456", user)).thenReturn(Optional.of(otpToken));

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP verified successfully. You can now reset your password."));

        verify(otpTokenRepository).save(otpToken);
    }

    @Test
    void verifyOtp_whenInvalidOtp_returnsBadRequest() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("test@test.com", "999999");
        User user = new User("test@test.com", "password", "testuser");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findByOtpAndUserAndUsedFalse("999999", user)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid OTP"));
    }

    @Test
    void verifyEmail_whenValidOtp_returnsSuccessAndEnablesUser() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("test@test.com", "123456");
        User user = new User("test@test.com", "password", "testuser");
        user.setEnabled(false);
        OtpToken otpToken = new OtpToken();
        otpToken.setUser(user);
        otpToken.setOtp("123456");
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(otpTokenRepository.findByOtpAndUserAndUsedFalse("123456", user)).thenReturn(Optional.of(otpToken));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully"));

        verify(otpTokenRepository).save(otpToken);
        verify(userRepository).save(user);
        verify(emailService).sendWelcomeEmail("test@test.com", "testuser");
    }
}
