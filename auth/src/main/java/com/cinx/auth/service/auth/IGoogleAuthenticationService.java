package com.cinx.auth.service.auth;

import com.cinx.auth.dto.request.OAuthRequest;
import com.cinx.auth.dto.response.GoogleProfileResponse;
import com.cinx.auth.dto.response.GoogleTokenResponse;

public interface IGoogleAuthenticationService {
        GoogleTokenResponse exchangeCodeForToken(OAuthRequest request);
        GoogleProfileResponse getGoogleUserProfile(String accessToken);
}
