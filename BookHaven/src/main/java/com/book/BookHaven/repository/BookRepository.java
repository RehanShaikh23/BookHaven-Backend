package com.book.BookHaven.repository;

import com.book.BookHaven.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    List<Book> findByAddedBy(String addedBy);

    @Query("SELECT b FROM Book b WHERE " +
            "(:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.genre) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:genre IS NULL OR b.genre = :genre)")
    List<Book> findBySearchAndGenre(String search, String genre);

    List<Book> findTop3ByOrderByRatingDesc();

    List<Book> findTop3ByOrderByCreatedAtDesc();

    List<Book> findByGenreAndIdNot(String genre, String id);

    @Query("SELECT DISTINCT b.genre FROM Book b")
    List<String> findDistinctGenres();

}
