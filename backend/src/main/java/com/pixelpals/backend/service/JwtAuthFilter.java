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

    public JwtAuthFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        System.out.println("DEBUG JwtAuthFilter: shouldNotFilter - Request URI: " + requestURI);

        // Aggiungi /ws/info alla lista degli endpoint da escludere
        boolean shouldExclude = requestURI.startsWith("/api/auth/login") ||
                requestURI.startsWith("/api/auth/register") ||
                requestURI.startsWith("/api/auth/verify-email") ||
                requestURI.startsWith("/oauth2/authorization") ||
                requestURI.startsWith("/login/oauth2/code/") ||
                requestURI.startsWith("/ws/info"); // <-- NUOVO: Escludi l'endpoint info di SockJS

        System.out.println("DEBUG JwtAuthFilter: shouldNotFilter - Should exclude: " + shouldExclude);
        return shouldExclude;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("DEBUG JwtAuthFilter: doFilterInternal - Processing request for URI: " + request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;

        // Se l'Authorization header non è presente, cerca il token come parametro di query
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            System.out.println("DEBUG JwtAuthFilter: Token found in Authorization header.");
        } else {
            // Questo blocco gestisce i casi in cui il token viene passato come parametro di query,
            // come nel caso del token di verifica email.
            jwt = request.getParameter("token");
            if (jwt == null) {
                System.out.println("DEBUG JwtAuthFilter: No JWT found in header or query parameter. Passing through filter chain.");
                filterChain.doFilter(request, response);
                return;
            }
            System.out.println("DEBUG JwtAuthFilter: Token found in query parameter.");
        }

        // Se un token (JWT o di verifica) è stato trovato, prova a processarlo
        // Solo se non c'è già un'autenticazione nel SecurityContext
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = null;
            try {
                username = jwtService.extractUsername(jwt);
                System.out.println("DEBUG JwtAuthFilter: Extracted username: " + username);
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                // Cattura specificamente MalformedJwtException se il token non è un JWT valido
                // ma è un token di verifica che dovrebbe essere gestito altrove.
                System.out.println("DEBUG JwtAuthFilter: MalformedJwtException caught. Token is likely a verification token. Passing through filter chain.");
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                // Gestisci altri errori di estrazione del username
                System.err.println("DEBUG JwtAuthFilter: Error extracting username: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
                return;
            }


            if (username != null) {
                UserDetails userDetails = this.userService.loadUserByUsername(username);
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("DEBUG JwtAuthFilter: Token is not valid for authentication.");
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
                System.out.println("DEBUG JwtAuthFilter: Authentication set for user: " + username);
                System.out.println("DEBUG JwtAuthFilter: SecurityContextHolder authentication after setting: " + SecurityContextHolder.getContext().getAuthentication());
            }
        } else {
            System.out.println("DEBUG JwtAuthFilter: SecurityContext already has authentication. Skipping JWT processing.");
        }

        filterChain.doFilter(request, response);
    }
}
