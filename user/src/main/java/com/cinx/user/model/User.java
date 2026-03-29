package com.cinx.user.model;

import com.cinx.user.consts.Gender;
import com.cinx.user.consts.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

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
    private String name;
    private Gender gender;
    private Role role;
    private Boolean isInstructorVerified;
    private String avatarUrl;

    private String userId;
}
