package com.book.BookHaven.service;

import com.book.BookHaven.dto.BookRequest;
import com.book.BookHaven.dto.BookResponse;
import com.book.BookHaven.entity.Book;
import com.book.BookHaven.exception.ResourceNotFoundException;
import com.book.BookHaven.exception.UnauthorizedException;
import com.book.BookHaven.exception.ValidationException;
import com.book.BookHaven.repository.BookRepository;
import com.book.BookHaven.repository.UserRepository;
import com.book.BookHaven.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<BookResponse> getAllBooks(String search, String genre, String sortBy, String sortOrder) {
        List<Book> books = bookRepository.findBySearchAndGenre(search, genre);
        books.sort((a, b) -> {
            int order = "asc".equalsIgnoreCase(sortOrder) ? 1 : -1;
            switch (sortBy.toLowerCase()) {
                case "title":
                    return a.getTitle().compareToIgnoreCase(b.getTitle()) * order;
                case "author":
                    return a.getAuthor().compareToIgnoreCase(b.getAuthor()) * order;
                case "price":
                    return a.getPrice().compareTo(b.getPrice()) * order;
                case "rating":
                    return Double.compare(a.getRating(), b.getRating()) * order;
                case "publicationdate":
                    return a.getPublicationDate().compareTo(b.getPublicationDate()) * order;
                default:
                    return a.getTitle().compareToIgnoreCase(b.getTitle()) * order;
            }
        });
        return books.stream().map(this::toBookResponse).collect(Collectors.toList());
    }

    public BookResponse getBookById(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return toBookResponse(book);
    }

    public BookResponse createBook(BookRequest request, String email) {
        validateBookRequest(request);
        Book book = new Book(
                UUID.randomUUID().toString(),
                request.getTitle(),
                request.getAuthor(),
                request.getPublicationDate(),
                request.getGenre(),
                request.getDescription(),
                request.getPrice(),
                request.getImage(),
                0.0,
                request.getStock() > 0,
                email,
                LocalDateTime.now()
        );
        bookRepository.save(book);
        return toBookResponse(book);
    }

    public BookResponse updateBook(String id, BookRequest request, String email) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        if (!book.getAddedBy().equals(email) && !isAdmin(email)) {
            throw new UnauthorizedException("Not authorized to update this book");
        }
        validateBookRequest(request);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationDate(request.getPublicationDate());
        book.setGenre(request.getGenre());
        book.setDescription(request.getDescription());
        book.setPrice(BigDecimal.valueOf(request.getPrice()));
        book.setImage(request.getImage());
        book.setInStock(request.getStock() > 0);
        bookRepository.save(book);
        return toBookResponse(book);
    }

    public void deleteBook(String id, String email) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        if (!book.getAddedBy().equals(email) && !isAdmin(email)) {
            throw new UnauthorizedException("Not authorized to delete this book");
        }
        bookRepository.delete(book);
    }

    public List<BookResponse> getFeaturedBooks() {
        return bookRepository.findTop3ByOrderByCreatedAtDesc()
                .stream().map(this::toBookResponse).collect(Collectors.toList());
    }

    public List<BookResponse> getTopRatedBooks() {
        return bookRepository.findTop3ByOrderByRatingDesc()
                .stream().map(this::toBookResponse).collect(Collectors.toList());
    }

    public List<BookResponse> getRelatedBooks(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return bookRepository.findByGenreAndIdNot(book.getGenre(), id)
                .stream().limit(4).map(this::toBookResponse).collect(Collectors.toList());
    }

    public List<String> getGenres() {
        return bookRepository.findDistinctGenres();
    }

    public List<BookResponse> getMyBooks(String email) {
        return bookRepository.findByAddedBy(email)
                .stream().map(this::toBookResponse).collect(Collectors.toList());
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> "ADMIN".equals(user.getRole()))
                .orElse(false);
    }

    private void validateBookRequest(BookRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().length() < 2) {
            throw new ValidationException("Title must be at least 2 characters long");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().length() < 2) {
            throw new ValidationException("Author must be at least 2 characters long");
        }
        if (request.getPublicationDate() == null || request.getPublicationDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Publication date cannot be in the future");
        }
        if (request.getGenre() == null || request.getGenre().trim().isEmpty()) {
            throw new ValidationException("Genre is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().length() < 10 ||
                request.getDescription().length() > 1000) {
            throw new ValidationException("Description must be between 10 and 1000 characters");
        }
        if (request.getPrice() <= 0 || request.getPrice() > 999.99) {
            throw new ValidationException("Price must be between 0.01 and 999.99");
        }
        if (request.getImage() == null || !ValidationUtil.isValidUrl(request.getImage())) {
            throw new ValidationException("Invalid image URL");
        }
        if (request.getStock() < 0) {
            throw new ValidationException("Stock cannot be negative");
        }
    }

    private BookResponse toBookResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublicationDate(),
                book.getGenre(),
                book.getDescription(),
                book.getPrice(),
                book.getImage(),
                book.getRating(),
                book.isInStock(),
                book.getAddedBy(),
                book.getCreatedAt()
        );
    }
}