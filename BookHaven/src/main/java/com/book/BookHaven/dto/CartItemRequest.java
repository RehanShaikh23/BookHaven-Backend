package com.book.BookHaven.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
public class CartItemRequest {

    @NotBlank
    private String bookId;

    @Min(1)
    private Integer quantity;
}