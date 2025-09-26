package com.book.BookHaven.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookResponse {

    private String id;
    private String title;
    private String author;
    private LocalDate publicationDate;
    private String genre;
    private String description;
    private BigDecimal price;
    private String image;
    private double rating;
    private boolean inStock;
    private String addedBy;
    private LocalDateTime createdAt;

    public BookResponse(String id, String title, String author, LocalDate publicationDate, String genre, String description, BigDecimal price, String image, double rating, boolean inStock, String addedBy, LocalDateTime createdAt) {
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