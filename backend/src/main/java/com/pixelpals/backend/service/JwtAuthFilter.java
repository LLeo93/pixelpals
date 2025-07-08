package com.pixelpals.backend.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlacklistService blacklistService;

    public JwtAuthFilter(JwtService jwtService, UserService userService, TokenBlacklistService blacklistService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.blacklistService = blacklistService;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        System.out.println("[JWT Filter] shouldNotFilter path: " + path);
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/oauth2/authorization") ||
                path.startsWith("/login/oauth2/code/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("[JWT Filter] Richiesta intercettata per path: " + path);


        if (path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/refresh")
                || path.startsWith("/oauth2/authorization") || path.startsWith("/login/oauth2/code/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT Filter] Header Authorization mancante o malformato");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authorization header missing or invalid\"}");
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("[JWT Filter] Token estratto: " + jwt);

        // Check blacklist
        if (blacklistService.isTokenBlacklisted(jwt)) {
            System.out.println("[JWT Filter] Token nella blacklist");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token is blacklisted\"}");
            return;
        }

        String username = jwtService.extractUsername(jwt);
        System.out.println("[JWT Filter] Username estratto: " + username);

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token: no username\"}");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(username);
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("[JWT Filter] Token non valido");
                System.out.println("JWT: " + jwt);
                System.out.println("Username from JWT: " + username);
                System.out.println("Token is valid: " + jwtService.isTokenValid(jwt, userDetails));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token not valid\"}");
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("[JWT Filter] Autenticazione settata per utente: " + username);
        }

        filterChain.doFilter(request, response);
    }
}
