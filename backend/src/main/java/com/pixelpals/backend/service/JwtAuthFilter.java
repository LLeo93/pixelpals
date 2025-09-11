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
import java.util.Arrays;
import java.util.List;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
    private static final List<String> EXCLUDE_HTTP_URL_PATTERNS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-email",
            "/oauth2/authorization",
            "/login/oauth2/code/"
    );
    public JwtAuthFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (requestURI.startsWith("/ws")) {
            return true;
        }
        return EXCLUDE_HTTP_URL_PATTERNS.stream().anyMatch(requestURI::startsWith);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            jwt = request.getParameter("token");
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String username;
            try {
                username = jwtService.extractUsername(jwt);
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
                return;
            }

            if (username != null) {
                UserDetails userDetails = this.userService.loadUserByUsername(username);
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token non valido\"}");
                    return;
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
