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
        String path = request.getServletPath();
        // Aggiungi /api/auth/verify-email agli endpoint che non devono essere filtrati dal JWTAuthFilter
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/verify-email") || // <-- AGGIUNTO: Escludi l'endpoint di verifica
                path.startsWith("/oauth2/authorization") ||
                path.startsWith("/login/oauth2/code/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;

        // Se l'Authorization header non è presente, cerca il token come parametro di query
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            // Questo blocco gestisce i casi in cui il token viene passato come parametro di query,
            // come nel caso del token di verifica email.
            jwt = request.getParameter("token");
            if (jwt == null) {
                // Se non c'è Authorization header né token come parametro, è una richiesta non autenticata.
                // Permetti al filtro di procedere per far gestire l'autorizzazione a Spring Security
                // (che poi userà le regole permitAll() o authenticated()).
                filterChain.doFilter(request, response);
                return; // Importante per non processare ulteriormente la richiesta in questo filtro
            }
        }

        // Se un token (JWT o di verifica) è stato trovato, prova a processarlo
        // Solo se non c'è già un'autenticazione nel SecurityContext
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = null;
            try {
                username = jwtService.extractUsername(jwt);
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                // Cattura specificamente MalformedJwtException se il token non è un JWT valido
                // ma è un token di verifica che dovrebbe essere gestito altrove.
                // In questo caso, lasciamo che la richiesta proceda al controller.
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                // Gestisci altri errori di estrazione del username
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
                return;
            }


            if (username != null) {
                UserDetails userDetails = this.userService.loadUserByUsername(username);
                // Qui, è importante distinguere tra token di autenticazione e token di verifica.
                // Il metodo isTokenValid è per i token di autenticazione.
                // Per i token di verifica, la validazione avviene in AuthService.verifyUserEmail.
                // Se il token è un token di verifica, non dobbiamo chiamare isTokenValid qui.
                // Tuttavia, dato che shouldNotFilter ora esclude l'endpoint di verifica,
                // questa parte del codice dovrebbe essere raggiunta solo per i token di autenticazione.
                if (!jwtService.isTokenValid(jwt, userDetails)) {
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
            }
        }

        filterChain.doFilter(request, response);
    }
}
