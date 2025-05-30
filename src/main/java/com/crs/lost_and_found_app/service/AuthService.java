package com.crs.lost_and_found_app.service;

import com.crs.lost_and_found_app.dto.JwtAuthenticationResponse;
import com.crs.lost_and_found_app.dto.SignInRequest;
import com.crs.lost_and_found_app.dto.SignUpRequest;
import com.crs.lost_and_found_app.entity.User;
import com.crs.lost_and_found_app.enums.UserRole;
import com.crs.lost_and_found_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Attempt to register with existing username: {}", request.getUsername());
            // Consider throwing a custom exception here for better error handling
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Attempt to register with existing email: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.USER) // Default to USER if not specified
                .build();
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        String jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {}. Reason: {}", request.getUsername(), e.getMessage());
            throw new IllegalArgumentException("Invalid username or password", e);
        }

        // If authentication is successful, user is valid
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found after authentication - this should not happen"));
        logger.info("User signed in successfully: {}", user.getUsername());

        String jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
} 