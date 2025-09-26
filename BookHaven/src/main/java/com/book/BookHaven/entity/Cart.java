package com.book.BookHaven.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart",
        indexes = {
                @Index(name = "idx_cart_user_id", columnList = "user_id"),
                @Index(name = "idx_cart_book_id", columnList = "book_id"),
                @Index(name = "idx_cart_user_book", columnList = "user_id, book_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_book", columnNames = {"user_id", "book_id"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_user"))
    private User user;

    @NotNull(message = "Book cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_book"))
    private Book book;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking

    // Custom constructor for backward compatibility
    public Cart(User user, Book book, Integer quantity) {
        this.user = user;
        this.book = book;
        this.quantity = quantity;
    }

    // Business method to update quantity safely
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = newQuantity;
    }

    // Business method to increment quantity
    public void incrementQuantity(Integer amount) {
        if (amount == null || amount < 1) {
            throw new IllegalArgumentException("Increment amount must be positive");
        }
        this.quantity += amount;
    }
}