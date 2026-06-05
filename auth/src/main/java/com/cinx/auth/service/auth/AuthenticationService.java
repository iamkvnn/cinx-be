package com.cinx.auth.service.auth;

import com.cinx.auth.consts.Role;
import com.cinx.auth.consts.UserStatus;
import com.cinx.auth.dto.*;
import com.cinx.auth.dto.request.*;
import com.cinx.auth.dto.response.GoogleProfileResponse;
import com.cinx.auth.dto.response.GoogleTokenResponse;
import com.cinx.auth.dto.response.TokenResponseDto;
import com.cinx.auth.service.user.IUserService;
import com.cinx.auth.service.userProfile.IUserProfileService;
import com.cinx.common.exception.BadRequestException;
import com.cinx.auth.model.User;
import com.cinx.auth.messaging.AuthNotificationPublisher;
import com.cinx.common.exception.NotFoundException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    @Value("${jwt.access.secret}")
    private String accessKey;
    @Value("${jwt.access.expire}")
    private Long accessExpirationTime;
    @Value("${jwt.refresh.secret}")
    private String refreshKey;
    @Value("${jwt.refresh.expire}")
    private Long refreshExpirationTime;

    private final PasswordEncoder passwordEncoder;
    private final IUserService userService;
    private final AuthNotificationPublisher authNotificationPublisher;
    private final IUserProfileService userProfileService;
    private final IGoogleAuthenticationService googleAuthenticationService;

    @Override
    public void sendVerifyOtp(String email) {
        String otp = userService.generateOtp(email);
        authNotificationPublisher.publishOtpVerifyEmail(email, otp);
    }

    @Override
    public void sendForgotPasswordOtp(String email) {
        String otp = userService.generateOtp(email);
        authNotificationPublisher.publishOtpForgotPassword(email, otp);
    }

    @Override
    public TokenResponseDto authenticate(AuthRequestDto request) {
        User user = userService.findByEmail(request.email());
        if (user.getRole() != request.role()) {
            throw new BadRequestException("Invalid email or password");
        }
        if (user.getPassword() == null) {
            throw new BadRequestException("User registered with Google. Please login with Google");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        if (user.getStatus().equals(UserStatus.UNVERIFIED)) {
            throw new BadRequestException("User email is not verified");
        }
        
        userService.checkAndUnbanIfNeeded(user);
        if (user.getStatus().equals(UserStatus.BANNED)) {
            throw new BadRequestException("User account is banned");
        }
        if (user.getRole() == Role.INSTRUCTOR && !userProfileService.checkInstructorVerified(user.getId()).data()) {
            throw new BadRequestException("Instructor account is not verified by admin");
        }
        JWTPayload payload = new JWTPayload(user.getId(), user.getRole().name());
        recordLastAccess(user.getId());
        return generateTokens(payload);
    }

    @Override
    public TokenResponseDto authenticateWithGoogle(OAuthRequest request) {
        GoogleTokenResponse tokenResponse = googleAuthenticationService.exchangeCodeForToken(request);
        GoogleProfileResponse profileResponse = googleAuthenticationService.getGoogleUserProfile(tokenResponse.accessToken());
        User user = Optional.ofNullable(userService.findByGoogleProfile(profileResponse))
                .orElseGet(() -> {
                    if (request.role() == Role.ADMIN) {
                        throw new NotFoundException("Admin account not found with Google email: " + profileResponse.email());
                    }
                    return userService.createUserByGoogleProfile(profileResponse, request.role());
                });
        
        userService.checkAndUnbanIfNeeded(user);
        if (user.getStatus().equals(UserStatus.BANNED)) {
            throw new BadRequestException("User account is banned");
        }
        recordLastAccess(user.getId());
        return generateTokens(new JWTPayload(user.getId(), user.getRole().name()));
    }

    @Override
    public TokenResponseDto generateTokens(JWTPayload payload) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet accessClaimsSet = new JWTClaimsSet.Builder()
                .subject(payload.userId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(accessExpirationTime, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", payload.role())
                .build();
        Payload jwtPayload = new Payload(accessClaimsSet.toJSONObject());
        JWSObject accessJwsObject = new JWSObject(header, jwtPayload);

        JWTClaimsSet refreshClaimsSet = new JWTClaimsSet.Builder()
                .subject(payload.userId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(refreshExpirationTime, ChronoUnit.DAYS).toEpochMilli()
                ))
                .build();
        Payload refreshJwtPayload = new Payload(refreshClaimsSet.toJSONObject());
        JWSObject refreshJwsObject = new JWSObject(header, refreshJwtPayload);

        try{
            accessJwsObject.sign(new MACSigner(accessKey.getBytes()));
            String accessToken = accessJwsObject.serialize();
            refreshJwsObject.sign(new MACSigner(refreshKey.getBytes()));
            String refreshToken = refreshJwsObject.serialize();
            return new TokenResponseDto(accessToken, refreshToken);
        }
        catch (JOSEException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public TokenResponseDto refreshToken(String refreshToken) {
        try {
            JWSVerifier verifier = new MACVerifier(refreshKey.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = signedJWT.verify(verifier);
            if (!verified || expirationDate.before(new Date())) {
                throw new BadRequestException("Invalid or expired refresh token");
            }
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            User user = userService.findById(userId);
            
            userService.checkAndUnbanIfNeeded(user);
            if (user.getStatus().equals(UserStatus.BANNED)) {
                throw new BadRequestException("User account is banned");
            }
            recordLastAccess(user.getId());
            JWTPayload payload = new JWTPayload(user.getId(), user.getRole().name());
            return generateTokens(payload);
        }
        catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void recordLastAccess(String userId) {
        try {
            userProfileService.updateLastAccess(userId);
        } catch (Exception ignored) {
            // Login must not fail if the profile service is temporarily unavailable.
        }
    }
}
