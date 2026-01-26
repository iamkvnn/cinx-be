package com.cinx.auth.service.auth;

import com.cinx.auth.dto.*;
import com.cinx.auth.model.User;
import com.cinx.auth.service.mail.EmailQueueService;
import com.cinx.auth.service.user.IUserService;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.cinx.auth.utils.OtpGenerator.generateOtp;

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
    private final EmailQueueService emailQueueService;

    @Override
    public void sendOtp(String email) {
        User user = userService.findByEmail(email);
        String otp = generateOtp();
        userService.updateUser(user.getId(), User.builder().otp(otp).otpExpireAt(LocalDateTime.now().plusSeconds(90)).build());
        emailQueueService.enqueue(new EmailRequest(user.getEmail(), "Mã xác nhận OTP", "Mã OTP của bạn là: " + otp));
    }

    @Override
    public void verifyOtp(VerifyOtpDto request) {
        User user = userService.findByEmail(request.email());
        if (user.getOtpExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        if (!user.getOtp().equals(request.otp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        userService.updateUser(user.getId(), User.builder().isVerified(true).otp(null).otpExpireAt(null).build());
    }

    @Override
    public AuthResponse authenticate(AuthRequestDto request) {
        User user = userService.findByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (user.getIsVerified() == null || !user.getIsVerified()) {
            throw new IllegalArgumentException("User email is not verified");
        }
        JWTPayload payload = new JWTPayload(user.getId(), user.getRole().name());
        TokenResponseDto tokens = generateTokens(payload);
        return new AuthResponse(tokens, new UserDto(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getGender()));
    }

    @Override
    public TokenResponseDto generateTokens(JWTPayload payload) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet accessClaimsSet = new JWTClaimsSet.Builder()
                .subject(payload.userId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(accessExpirationTime, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .claim("scope", payload.role())
                .build();
        Payload jwtPayload = new Payload(accessClaimsSet.toJSONObject());
        JWSObject accessJwsObject = new JWSObject(header, jwtPayload);

        JWTClaimsSet refreshClaimsSet = new JWTClaimsSet.Builder()
                .subject(payload.userId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(refreshExpirationTime, ChronoUnit.SECONDS).toEpochMilli()
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
                throw new IllegalArgumentException("Invalid or expired refresh token");
            }
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            User user = userService.findById(userId);
            JWTPayload payload = new JWTPayload(user.getId(), user.getRole().name());
            return generateTokens(payload);
        }
        catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
