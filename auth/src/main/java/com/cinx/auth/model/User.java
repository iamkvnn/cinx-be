package com.cinx.auth.model;

import com.cinx.auth.consts.Role;
import com.cinx.auth.consts.UserStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String email;
    private String password;
    private Role role;
    private UserStatus status;
    private String otp;
    private LocalDateTime otpExpireAt;
}
