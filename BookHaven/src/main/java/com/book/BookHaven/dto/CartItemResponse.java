package com.book.BookHaven.dto;

import com.book.BookHaven.entity.Cart;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {

    private String id;
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private String image;
    private BigDecimal price;
    private int quantity;

    public CartItemResponse(String id, String bookId, String title, String author, String genre,
                            String image, BigDecimal price, int quantity) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.image = image;
        this.price = price;
        this.quantity = quantity;
    }

    public static CartItemResponse fromEntity(Cart cart) {
        return new CartItemResponse(
                cart.getId().toString(),
                cart.getBook().getId(),
                cart.getBook().getTitle(),
                cart.getBook().getAuthor(),
                cart.getBook().getGenre(),
                cart.getBook().getImage(),
                cart.getBook().getPrice(),
                cart.getQuantity()
        );
    }

}