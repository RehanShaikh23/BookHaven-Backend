package com.book.BookHaven.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private String token;


    public UserResponse() {}


    public UserResponse(String id, String username, String email, String role, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
    }


    public UserResponse(String id, String username, String email, String role, LocalDateTime createdAt, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.token = token;
    }
}