package com.book.BookHaven.repository;

import com.book.BookHaven.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {


    @Query("SELECT c FROM Cart c JOIN FETCH c.book WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Cart> findByUserIdWithBooks(@Param("userId") String userId);


    List<Cart> findByUserId(String userId);


    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.book.id = :bookId")
    Optional<Cart> findByUserIdAndBookId(@Param("userId") String userId, @Param("bookId") String bookId);


    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId")
    int countByUserId(@Param("userId") String userId);


    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    int deleteByUserId(@Param("userId") String userId);


    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.user.id = :userId AND c.book.id = :bookId")
    boolean existsByUserIdAndBookId(@Param("userId") String userId, @Param("bookId") String bookId);


    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM Cart c WHERE c.user.id = :userId")
    int getTotalQuantityByUserId(@Param("userId") String userId);


    @Query("SELECT c FROM Cart c WHERE c.updatedAt < CURRENT_TIMESTAMP - :days DAY")
    List<Cart> findStaleCartItems(@Param("days") int days);


    List<Cart> findByBookId(String bookId);


    @Modifying
    @Query("UPDATE Cart c SET c.quantity = :quantity WHERE c.user.id = :userId AND c.book.id = :bookId")
    int updateQuantity(@Param("userId") String userId, @Param("bookId") String bookId, @Param("quantity") int quantity);
}