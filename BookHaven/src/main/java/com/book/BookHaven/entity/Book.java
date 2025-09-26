package com.book.BookHaven.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 500) // title can be long
    private String title;

    @Column(length = 255)
    private String author;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @Column(length = 255)
    private String genre;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // precise money type

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false)
    private boolean inStock;

    @Column(length = 255)
    private String addedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Book(String string, @NotBlank(message = "Title is required") String title, @NotBlank(message = "Author is required") String author, @NotNull(message = "Publication date is required") LocalDate publicationDate, @NotBlank(message = "Genre is required") String genre, String description, @NotNull(message = "Price is required") @Positive(message = "Price must be positive") double price, String image, double rating, boolean inStock, String email, LocalDateTime now) {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Book(String id, String title, String author, LocalDate publicationDate, String genre,
                String description, BigDecimal price, String image, double rating, boolean inStock,
                String addedBy, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publicationDate = publicationDate;
        this.genre = genre;
        this.description = description;
        this.price = price;
        this.image = image;
        this.rating = rating;
        this.inStock = inStock;
        this.addedBy = addedBy;
        this.createdAt = createdAt;
    }
}
