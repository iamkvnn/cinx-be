package com.cinx.auth.model;

import com.cinx.auth.consts.Gender;
import com.cinx.auth.consts.Role;
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
    private String name;
    private Role role;
    private Gender gender;
    private Boolean isVerified;
    private String otp;
    private LocalDateTime otpExpireAt;
}
