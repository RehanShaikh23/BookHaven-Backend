package com.book.BookHaven.controller;

import com.book.BookHaven.dto.CartItemRequest;
import com.book.BookHaven.dto.CartItemResponse;
import com.book.BookHaven.exception.ResourceNotFoundException;
import com.book.BookHaven.exception.ValidationException;
import com.book.BookHaven.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<CartItemResponse> cart = cartService.getCart(userDetails.getUsername());
            return ResponseEntity.ok(cart);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartItemRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CartItemResponse response = cartService.addToCart(request, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<?> updateCartItem(@PathVariable String bookId,
                                            @RequestParam int quantity,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CartItemResponse response = cartService.updateCartItem(bookId, quantity, userDetails.getUsername());
            return response != null ? ResponseEntity.ok(response) :
                    ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> removeFromCart(@PathVariable String bookId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            cartService.removeFromCart(bookId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            cartService.clearCart(userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String response = cartService.checkout(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", response));
        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}