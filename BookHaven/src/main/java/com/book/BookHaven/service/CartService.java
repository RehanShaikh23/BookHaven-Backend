package com.book.BookHaven.service;

import com.book.BookHaven.dto.CartItemRequest;
import com.book.BookHaven.dto.CartItemResponse;
import com.book.BookHaven.entity.Book;
import com.book.BookHaven.entity.Cart;
import com.book.BookHaven.entity.User;
import com.book.BookHaven.exception.ResourceNotFoundException;
import com.book.BookHaven.exception.ValidationException;
import com.book.BookHaven.repository.BookRepository;
import com.book.BookHaven.repository.CartRepository;
import com.book.BookHaven.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class CartService {

    private final GoogleBookService googleBookService;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CartService(GoogleBookService googleBookService,
                       CartRepository cartRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository) {
        this.googleBookService = googleBookService;
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "userCart", key = "#email")
    public List<CartItemResponse> getCart(@NotBlank @Email String email) {
        log.debug("Fetching cart for user: {}", email);

        User user = findUserByEmail(email);
        List<Cart> cartItems = cartRepository.findByUserIdWithBooks(user.getId());

        return cartItems.parallelStream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#email")
    public CartItemResponse addToCart(CartItemRequest request, String email) {

        String bookId = request.getBookId();
        Integer quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        log.info("Adding book {} to cart for user: {}", bookId, email);

        // Input validation
        validateBookId(bookId);

        User user = findUserByEmail(email);

        // Check if item already exists in cart
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(user.getId(), bookId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.incrementQuantity(quantity);
            Cart updated = cartRepository.save(cart);
            return CartItemResponse.fromEntity(updated);
        }


        Book book = getOrFetchBook(bookId);


        validateBookAvailability(book, quantity);

        // Create new cart item
        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(quantity)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("Successfully added book {} to cart for user: {}", bookId, email);

        return CartItemResponse.fromEntity(savedCart);
    }


    @Transactional
    @CacheEvict(value = "userCart", key = "#email")
    public CartItemResponse updateCartItem(@NotBlank String bookId,
                                           @Min(0) Integer quantity,
                                           @NotBlank @Email String email) {

        log.info("Updating cart item {} with quantity {} for user: {}", bookId, quantity, email);

        User user = findUserByEmail(email);
        Cart cart = cartRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Cart item not found for book id: %s and user: %s", bookId, email)));

        // If quantity is 0, remove the item
        if (quantity == 0) {
            cartRepository.delete(cart);
            log.info("Removed cart item {} for user: {}", bookId, email);
            return null;
        }


        validateBookAvailability(cart.getBook(), quantity);

        cart.updateQuantity(quantity);
        Cart savedCart = cartRepository.save(cart);

        return mapToCartItemResponse(savedCart);
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#email")
    public void removeFromCart(@NotBlank String bookIdStr,
                               @NotBlank @Email String email) {

        Long bookId;
        try {
            bookId = Long.parseLong(bookIdStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid book id: " + bookIdStr);
        }

        log.info("Removing book {} from cart for user: {}", bookId, email);

        User user = findUserByEmail(email);



        Cart cart = cartRepository.findByUserIdAndBookId(user.getId(), bookId.toString())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Cart item not found for book id: %s", bookId)));

        cartRepository.delete(cart);
        log.info("Successfully removed book {} from cart for user: {}", bookId, email);
    }



    @Transactional
    @CacheEvict(value = "userCart", key = "#email")
    public void clearCart(@NotBlank @Email String email) {
        log.info("Clearing cart for user: {}", email);

        User user = findUserByEmail(email);
        int deletedCount = cartRepository.deleteByUserId(user.getId());

        log.info("Cleared {} items from cart for user: {}", deletedCount, email);
    }

    @Transactional
    @CacheEvict(value = "userCart", key = "#email")
    public String checkout(@NotBlank @Email String email) {
        log.info("Processing checkout for user: {}", email);

        User user = findUserByEmail(email);
        List<Cart> cartItems = cartRepository.findByUserIdWithBooks(user.getId());

        if (cartItems.isEmpty()) {
            throw new ValidationException("Cannot checkout with empty cart");
        }

        // Validate all items are still available
        BigDecimal totalAmount = validateAndCalculateTotal(cartItems);

        // Process checkout logic here (payment, order creation, etc.)
        // This should ideally be moved to a separate OrderService

        // Clear cart after successful checkout
        cartRepository.deleteByUserId(user.getId());

        log.info("Checkout successful for user: {} with total amount: {}", email, totalAmount);
        return String.format("Checkout successful. Total amount: $%.2f", totalAmount);
    }

    @Cacheable(value = "cartCount", key = "#email")
    public int getCartItemCount(@NotBlank @Email String email) {
        User user = findUserByEmail(email);
        return cartRepository.countByUserId(user.getId());
    }

    @Cacheable(value = "cartTotal", key = "#email")
    public BigDecimal getCartTotal(@NotBlank @Email String email) {
        User user = findUserByEmail(email);
        List<Cart> cartItems = cartRepository.findByUserIdWithBooks(user.getId());

        return cartItems.stream()
                .map(cart -> cart.getBook().getPrice()
                        .multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    // Private helper methods
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User not found with email: %s", email)));
    }

    private Book getOrFetchBook(String bookId) {
        return bookRepository.findById(bookId)
                .orElseGet(() -> {
                    log.info("Book not found in database, fetching from Google Books API: {}", bookId);
                    return googleBookService.fetchBookFromGoogle(bookId);
                });
    }

    private void validateBookId(String bookId) {
        if (!StringUtils.hasText(bookId) || bookId.trim().length() < 3) {
            throw new ValidationException("Invalid book ID format");
        }
    }

    private void validateBookAvailability(Book book, Integer quantity) {
        if (book == null) {
            throw new ResourceNotFoundException("Book not found");
        }

        if (!book.isInStock()) {
            throw new ValidationException(
                    String.format("Book '%s' is currently out of stock", book.getTitle()));
        }
    }

    private BigDecimal validateAndCalculateTotal(List<Cart> cartItems) {
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cart : cartItems) {
            Book book = cart.getBook();
            validateBookAvailability(book, cart.getQuantity());

            BigDecimal itemTotal = book.getPrice()
                    .multiply(BigDecimal.valueOf(cart.getQuantity()));
            total = total.add(itemTotal);
        }

        return total;
    }


    private CartItemResponse mapToCartItemResponse(Cart cart) {
        Book book = cart.getBook();
        return new CartItemResponse(
                String.valueOf(cart.getId()),
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getImage(),
                book.getPrice(),
                cart.getQuantity()
        );
    }
}