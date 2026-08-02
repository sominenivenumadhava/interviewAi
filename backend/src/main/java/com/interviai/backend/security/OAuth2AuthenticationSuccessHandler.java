package com.interviai.backend.security;

import com.interviai.backend.module.auth.entity.RefreshToken;
import com.interviai.backend.module.auth.service.JwtService;
import com.interviai.backend.module.auth.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Align JWT role claim with password-login (USER / ADMIN), not ROLE_USER
        String role = userPrincipal.getUser().getRole() != null
                ? userPrincipal.getUser().getRole().name()
                : "USER";

        String accessToken = jwtService.generateAccessToken(
                userPrincipal.getUser().getId(),
                userPrincipal.getUser().getEmail(),
                role
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                userPrincipal.getUser().getId(),
                request.getHeader("User-Agent"),
                request.getRemoteAddr(),
                null
        );

        // Put tokens in the URL hash fragment so they are not sent to servers via Referer/query logs
        String fragment = String.format(
                "accessToken=%s&refreshToken=%s&expiresIn=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken.getToken(), StandardCharsets.UTF_8),
                jwtService.getAccessTokenValidity()
        );

        return UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .fragment(fragment)
                .build()
                .toUriString();
    }
}
