package org.aadi.abc_scholarship_finder.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        logger.debug("Processing request: {} {}", request.getMethod(), request.getServletPath());

        // Skip authentication for public endpoints
        if (shouldNotFilter(request)) {
            logger.debug("Skipping JWT filter for public endpoint: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        // 1. Try to get JWT from Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            logger.debug("Found JWT in Authorization header: {}", jwt.substring(0, Math.min(jwt.length(), 10)) + "...");
        } else {
            // 2. Try to get JWT from cookie
            if (request.getCookies() != null) {
                Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                        .filter(cookie -> "jwt".equals(cookie.getName()))
                        .findFirst();
                if (jwtCookie.isPresent()) {
                    jwt = jwtCookie.get().getValue();
                    logger.debug("Found JWT in cookie: {}", jwt.substring(0, Math.min(jwt.length(), 10)) + "...");
                } else {
                    logger.warn("No JWT cookie found for path: {}", request.getServletPath());
                }
            } else {
                logger.warn("No cookies present for path: {}", request.getServletPath());
            }
        }

        // If no JWT, proceed to next filter
        if (jwt == null) {
            logger.warn("No JWT found for path: {}, redirecting to login", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        // Extract username from JWT
        try {
            username = jwtService.extractUsername(jwt);
            logger.debug("Extracted username: {}", username);
        } catch (Exception e) {
            logger.error("JWT extraction failed for path {}: {}", request.getServletPath(), e.getMessage(), e);
            filterChain.doFilter(request, response);
            return;
        }

        // Authenticate if username is valid and no existing authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    logger.debug("JWT is valid for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.error("Invalid JWT for user: {} at path: {}", username, request.getServletPath());
                }
            } catch (Exception e) {
                logger.error("Authentication failed for user: {} at path: {}: {}", username, request.getServletPath(), e.getMessage(), e);
            }
        } else {
            logger.warn("No username extracted or authentication already exists for path: {}", request.getServletPath());
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        boolean skip = path.startsWith("/api/auth/") ||
                path.equals("/login") ||
                path.equals("/register") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/info/privacy") ||
                path.equals("/info/contact") ||
                path.equals("/info/terms") ||
                path.equals("/");
        logger.debug("shouldNotFilter for path {}: {}", path, skip);
        return skip;
    }
}
