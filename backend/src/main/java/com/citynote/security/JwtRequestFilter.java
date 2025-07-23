package com.citynote.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.springframework.lang.NonNull;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("JWT Filter - URI: " + requestURI + ", Method: " + method);
        
        // Allow login and register endpoints only (no JWT required)
        if (requestURI.equals("/api/auth/login") || requestURI.equals("/api/auth/register")) {
            System.out.println("Allowing auth endpoint: " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // Allow file upload endpoints (no JWT required)
        if (requestURI.equals("/api/upload/image") && method.equals("POST")) {
            System.out.println("Allowing file upload endpoint: " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // Allow GET requests to image files (no JWT required)
        if (requestURI.startsWith("/api/upload/image/") && method.equals("GET")) {
            System.out.println("Allowing image access endpoint: " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // For /api/auth/me, we need to check JWT but not block if missing
        if (requestURI.equals("/api/auth/me")) {
            System.out.println("Processing /api/auth/me endpoint");
            // Continue to JWT processing below
        }
        
        // Allow GET requests to public event endpoints (but not user-specific or can-modify)
        if (requestURI.startsWith("/api/event/") && method.equals("GET") && 
            !requestURI.contains("/can-modify") && !requestURI.contains("/user/")) {
            System.out.println("Allowing public GET event endpoint: " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // For all other endpoints, check JWT token
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + (authorizationHeader != null ? "present" : "missing"));

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtTokenUtil.getUsernameFromToken(jwt);
            System.out.println("Extracted username from token: " + username);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set for user: " + username);
                } else {
                    System.out.println("Invalid JWT token for user: " + username);
                }
            } catch (Exception e) {
                System.out.println("Error processing JWT token: " + e.getMessage());
            }
        } else if (username == null) {
            System.out.println("No JWT token found in request");
        }
        
        chain.doFilter(request, response);
    }
} 