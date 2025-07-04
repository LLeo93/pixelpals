package com.pixelpals.backend.oauth2;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.UserRepository;
import com.pixelpals.backend.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = ((org.springframework.security.oauth2.core.user.DefaultOAuth2User) authentication.getPrincipal()).getAttribute("email");
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String token = jwtTokenUtil.generateToken(user.get());
            response.sendRedirect("https://your-frontend-url.com/oauth2/success?token=" + token);
        } else {
            response.sendRedirect("https://your-frontend-url.com/oauth2/error");
        }
    }
}