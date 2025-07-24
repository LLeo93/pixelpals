package com.pixelpals.backend.config;
import com.pixelpals.backend.service.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/verify-email**",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/preferredGames").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/platforms").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/skillLevels").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/availability").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/me/avatar/upload").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me/avatar/url").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users").authenticated()
                        .requestMatchers(
                                "/api/auth/me/**",
                                "/api/games",
                                "/api/online/users",
                                "/api/friends/**",
                                "/api/platforms/**",
                                "/api/friends/**"
                        ).authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/matches/find").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/matches/request-game").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/matches/{matchId}/accept").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/matches/{matchId}/decline").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/matches/pending-game-matches").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/matches/accepted-game-matches").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized: token missing or invalid\"}");
                        })
                )
                .authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
