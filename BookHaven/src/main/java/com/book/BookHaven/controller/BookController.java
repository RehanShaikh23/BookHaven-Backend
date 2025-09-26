package com.book.BookHaven.controller;

import com.book.BookHaven.dto.BookRequest;
import com.book.BookHaven.dto.BookResponse;
import com.book.BookHaven.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        try {
            return ResponseEntity.ok(bookService.getAllBooks(search, genre, sortBy, sortOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody BookRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(bookService.createBook(request, userDetails.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable String id,
                                        @RequestBody BookRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(bookService.updateBook(id, request, userDetails.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable String id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            bookService.deleteBook(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<List<BookResponse>> getFeaturedBooks() {
        return ResponseEntity.ok(bookService.getFeaturedBooks());
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<BookResponse>> getTopRatedBooks() {
        return ResponseEntity.ok(bookService.getTopRatedBooks());
    }

    @GetMapping("/related/{id}")
    public ResponseEntity<List<BookResponse>> getRelatedBooks(@PathVariable String id) {
        try {
            return ResponseEntity.ok(bookService.getRelatedBooks(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        return ResponseEntity.ok(bookService.getGenres());
    }
}