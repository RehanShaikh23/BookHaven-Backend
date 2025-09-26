package com.book.BookHaven.service;

import com.book.BookHaven.entity.Book;
import com.book.BookHaven.repository.BookRepository;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class GoogleBookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes/";

    public GoogleBookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book fetchBookFromGoogle(String bookId) {
        String url = GOOGLE_BOOKS_API + bookId;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONObject json = new JSONObject(response.getBody());

            JSONObject volumeInfo = json.getJSONObject("volumeInfo");

            String title = volumeInfo.optString("title", "Unknown Title");
            String author = volumeInfo.optJSONArray("authors") != null
                    ? volumeInfo.getJSONArray("authors").optString(0, "Unknown Author")
                    : "Unknown Author";
            String genre = volumeInfo.optJSONArray("categories") != null
                    ? volumeInfo.getJSONArray("categories").optString(0, "General")
                    : "General";
            String description = volumeInfo.optString("description", "No description available");
            String thumbnail = volumeInfo.optJSONObject("imageLinks") != null
                    ? volumeInfo.getJSONObject("imageLinks").optString("thumbnail", "https://via.placeholder.com/150")
                    : "https://via.placeholder.com/150";
            double rating = volumeInfo.has("averageRating")
                    ? volumeInfo.optDouble("averageRating", 3.5)
                    : 3.5;

            // Handle publication date
            LocalDate publicationDate = parsePublicationDate(volumeInfo.optString("publishedDate", null));

            Book book = new Book();
            book.setId(bookId);
            book.setTitle(title);
            book.setAuthor(author);
            book.setGenre(genre);
            book.setDescription(description);
            book.setImage(thumbnail);
            book.setRating(rating);
            book.setPublicationDate(publicationDate);

            book.setPrice(BigDecimal.valueOf(Math.random() * 1000 + 100));
            book.setInStock(true);
            book.setAddedBy("System");

            return bookRepository.save(book);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch book from Google API: " + e.getMessage());
        }
    }

    public Book fetchAndSaveBook(String googleBookId, String addedBy) {
        String url = GOOGLE_BOOKS_API + googleBookId;
        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("volumeInfo")) {
            throw new RuntimeException("Google Books API returned no data for id: " + googleBookId);
        }

        Map volumeInfo = (Map) response.get("volumeInfo");

        String publishedDateStr = (String) volumeInfo.get("publishedDate");
        LocalDate publicationDate = parsePublicationDate(publishedDateStr);

        Book book = new Book();
        book.setId(googleBookId);
        book.setTitle((String) volumeInfo.getOrDefault("title", "Untitled"));
        book.setAuthor(((java.util.List<String>) volumeInfo.getOrDefault("authors", java.util.List.of("Unknown Author"))).get(0));
        book.setGenre(((java.util.List<String>) volumeInfo.getOrDefault("categories", java.util.List.of("General"))).get(0));
        book.setDescription((String) volumeInfo.getOrDefault("description", "No description available"));
        book.setPrice(BigDecimal.valueOf(Math.random() * 1000 + 100));
        book.setImage(((Map<String, String>) volumeInfo.getOrDefault("imageLinks", Map.of()))
                .getOrDefault("thumbnail", "https://via.placeholder.com/150"));
        book.setInStock(true);
        book.setRating(((Number) volumeInfo.getOrDefault("averageRating", 3.5)).doubleValue());
        book.setAddedBy(addedBy != null ? addedBy : "System");
        book.setPublicationDate(publicationDate);

        return bookRepository.save(book);
    }


    private LocalDate parsePublicationDate(String publishedDateStr) {
        if (publishedDateStr == null || publishedDateStr.isBlank()) {
            return LocalDate.now();
        }
        try {
            if (publishedDateStr.matches("\\d{4}")) {
                return LocalDate.of(Integer.parseInt(publishedDateStr), 1, 1);
            } else if (publishedDateStr.matches("\\d{4}-\\d{2}")) {
                YearMonth ym = YearMonth.parse(publishedDateStr, DateTimeFormatter.ofPattern("yyyy-MM"));
                return ym.atDay(1);
            } else {
                return LocalDate.parse(publishedDateStr);
            }
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
