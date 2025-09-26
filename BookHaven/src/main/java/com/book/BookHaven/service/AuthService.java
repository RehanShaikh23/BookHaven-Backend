package com.book.BookHaven.service;

import com.book.BookHaven.dto.LoginRequest;
import com.book.BookHaven.dto.RegisterRequest;
import com.book.BookHaven.dto.UserResponse;
import com.book.BookHaven.entity.User;
import com.book.BookHaven.exception.UserAlreadyExistsException;
import com.book.BookHaven.exception.UserNotFoundException;
import com.book.BookHaven.exception.ValidationException;
import com.book.BookHaven.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (request.getUsername().matches("\\d+")) {
            throw new ValidationException("Username cannot consist only of numbers");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole("USER");

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), false);
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), token);
    }

    public User getUserDetails(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole(), request.getRememberMe());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), token);
    }

    public boolean validateToken(String token) {
        try {
            return jwtService.extractUsername(token) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken() {
        return true;
    }
}