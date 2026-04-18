package com.cinx.user.model;

import com.cinx.common.model.AuditableEntity;
import com.cinx.user.consts.Gender;
import com.cinx.user.consts.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {
    private String email;
    private String name;
    private Gender gender;
    private Role role;
    private Boolean isInstructorVerified;
    private String avatarUrl;
    @Builder.Default
    @Column(columnDefinition = "boolean default false")
    private Boolean isReceivePushNotification = false;

    @Builder.Default
    private Integer xp = 0;

    private String userId;
    private String cvUrl;
}
