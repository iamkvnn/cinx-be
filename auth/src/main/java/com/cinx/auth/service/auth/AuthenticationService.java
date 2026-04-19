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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.Map;
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
import java.util.UUID;

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
    private final RabbitTemplate rabbitTemplate;
    private final IUserProfileService userProfileService;
    private final IGoogleAuthenticationService googleAuthenticationService;

    @Override
    public void sendVerifyOtp(String email) {
        String otp = userService.generateOtp(email);
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", email, "subject", "Mã xác nhận OTP", "body", "Mã OTP của bạn là: " + otp), 
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }

    @Override
    public void sendForgotPasswordOtp(String email) {
        String otp = userService.generateOtp(email);
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", email, "subject", "Yêu cầu quên mật khẩu", "body", "Mã OTP của bạn là: " + otp), 
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }

    @Override
    public void sendChangePasswordOtp(String email) {
        String otp = userService.generateOtp(email);
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", email, "subject", "Yêu cầu đổi mật khẩu", "body", "Mã OTP của bạn là: " + otp), 
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }

    @Override
    public void sendChangeEmailOtp(String email) {
        String otp = userService.generateOtp(email);
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", email, "subject", "Yêu cầu đổi email", "body", "Mã OTP của bạn là: " + otp), 
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }

    @Override
    public TokenResponseDto authenticate(AuthRequestDto request) {
        User user = userService.findByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        if (user.getStatus().equals(UserStatus.UNVERIFIED)) {
            throw new BadRequestException("User email is not verified");
        }
        if (user.getStatus().equals(UserStatus.BANNED)) {
            throw new BadRequestException("User account is banned");
        }
        if (user.getRole() == Role.INSTRUCTOR && !userProfileService.checkInstructorVerified(user.getId()).data()) {
            throw new BadRequestException("Instructor account is not verified by admin");
        }
        JWTPayload payload = new JWTPayload(user.getId(), user.getRole().name());
        return generateTokens(payload);
    }

    @Override
    public TokenResponseDto authenticateWithGoogle(OAuthRequest request) {
        GoogleTokenResponse tokenResponse = googleAuthenticationService.exchangeCodeForToken(request);
        GoogleProfileResponse profileResponse = googleAuthenticationService.getGoogleUserProfile(tokenResponse.accessToken());
        User user = userService.findOrCreateUserByGoogleProfile(profileResponse);
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
            if (user.getStatus().equals(UserStatus.BANNED)) {
                throw new BadRequestException("User account is banned");
            }
            JWTPayload payload = new JWTPayload(user.getId(), user.getRole().name());
            return generateTokens(payload);
        }
        catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
