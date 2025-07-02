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

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("JWT Filter - URI: " + requestURI + ", Method: " + method);
        
        // Allow login and register endpoints only
        if (requestURI.equals("/api/auth/login") || requestURI.equals("/api/auth/register")) {
            System.out.println("Allowing auth endpoint: " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // Allow GET requests to event endpoints (but not can-modify)
        if (requestURI.startsWith("/api/event/") && method.equals("GET") && !requestURI.contains("/can-modify")) {
            System.out.println("Allowing GET event endpoint: " + requestURI);
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