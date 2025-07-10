package com.pixelpals.backend.service;

import com.pixelpals.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    String secretKey;

    @Value("${jwt.expiration}")
    long jwtExpiration;

    // Nuovo valore per la scadenza del token di verifica (es. 24 ore = 86400000 ms)
    @Value("${jwt.verification.expiration}")
    long verificationTokenExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        System.out.println("Username token: " + extractUsername(token));
        System.out.println("Username userDetails: " + userDetails.getUsername());
        System.out.println("Token expired? " + isTokenExpired(token));
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Modificato da private a public
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Metodo aggiunto per generare token di verifica da User ---
    public String generateVerificationToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Potresti aggiungere claims specifici per la verifica se necessario, es. "type": "verification"
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // Usiamo l'email come subject per la verifica
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + verificationTokenExpiration)) // Scadenza specifica
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // da togliere dopo i test
    public void debugToken(String token) {
        Claims claims = extractAllClaims(token);
        System.out.println("DEBUG TOKEN:");
        System.out.println("  Subject: " + claims.getSubject());
        System.out.println("  Issued At: " + claims.getIssuedAt());
        System.out.println("  Expiration: " + claims.getExpiration());
        System.out.println("  Is expired? " + isTokenExpired(token));
    }
}
