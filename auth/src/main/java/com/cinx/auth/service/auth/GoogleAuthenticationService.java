package com.cinx.auth.service.auth;

import com.cinx.auth.dto.request.OAuthRequest;
import com.cinx.auth.dto.response.GoogleProfileResponse;
import com.cinx.auth.dto.response.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService implements IGoogleAuthenticationService{
    private final WebClient webClient;

    @Value("${google.client-id}")
    private String clientId;
    @Value("${google.client-secret}")
    private String clientSecret;
    @Value("${google.user-info-uri}")
    private String userInfoUri;
    @Value("${google.token-uri}")
    private String tokenUri;

    @Override
    public GoogleTokenResponse exchangeCodeForToken(OAuthRequest request) {
        Map<String, String> requestBody = Map.of(
                "code", request.code(),
                "code_verifier", request.codeVerifier(),
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", request.redirectUri(),
                "grant_type", "authorization_code"
        );
        return webClient.post()
                .uri(tokenUri)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class).map(body -> new RuntimeException("Failed to exchange code for token: " + body))
                )
                .bodyToMono(GoogleTokenResponse.class)
                .block();
    }

    @Override
    public GoogleProfileResponse getGoogleUserProfile(String accessToken) {
        return webClient.get()
                .uri(userInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class).map(body -> new RuntimeException("Failed to fetch user profile: " + body))
                )
                .bodyToMono(GoogleProfileResponse.class)
                .block();
    }
}
