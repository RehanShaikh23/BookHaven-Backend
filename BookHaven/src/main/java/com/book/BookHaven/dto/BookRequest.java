package com.book.BookHaven.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotNull(message = "Publication date is required")
    private LocalDate publicationDate;

    @NotBlank(message = "Genre is required")
    private String genre;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private double price;

    private String image;

    @NotNull(message = "Stock is required")
    @Positive(message = "Stock must be positive")
    private Integer stock;
}