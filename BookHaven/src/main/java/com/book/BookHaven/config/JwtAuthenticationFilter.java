package com.book.BookHaven.config;

import com.book.BookHaven.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            // DEBUG: Log the request
            System.out.println("🌐 Request: " + request.getMethod() + " " + path);

            if (path.equals("/api/auth/login") || path.equals("/api/auth/register") ||
                    path.startsWith("/h2-console/") ||
                    (path.startsWith("/api/books") && "GET".equalsIgnoreCase(request.getMethod()))) {
                chain.doFilter(request, response);
                return;
            }





            String authHeader = request.getHeader("Authorization");
            System.out.println("🔍 Auth header: " + authHeader); // DEBUG

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ No valid auth header found"); // DEBUG
                chain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);
            String email = jwtService.extractUsername(jwt);
            System.out.println("👤 Email from token: " + email); // DEBUG

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("📋 User details loaded: " + userDetails.getUsername()); // DEBUG

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    Claims claims = jwtService.getClaims(jwt);
                    List<String> roles = claims.get("authorities", List.class);
                    System.out.println("🔑 Roles from token: " + roles); // DEBUG

                    List<GrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    System.out.println("✅ Granted authorities: " + authorities); // DEBUG

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("🎯 Authentication set successfully"); // DEBUG
                } else {
                    System.out.println("❌ Token validation failed"); // DEBUG
                }
            }
        } catch (Exception e) {
            System.out.println("💥 JWT Filter error: " + e.getMessage()); // DEBUG
            logger.error("JWT Filter error: ", e);
        }

        chain.doFilter(request, response);
    }
}
