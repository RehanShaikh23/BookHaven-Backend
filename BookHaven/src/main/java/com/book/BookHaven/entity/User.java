package com.book.BookHaven.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements org.springframework.security.core.userdetails.UserDetails {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String role;

    public User() {}

    public User(String id, String username, String email, String password, LocalDateTime createdAt, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.role = role;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}