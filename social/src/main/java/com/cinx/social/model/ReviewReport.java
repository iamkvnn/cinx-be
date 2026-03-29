package com.cinx.social.model;

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
public class ReviewReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String reporterId;
    private String reviewId;
    private String reason;
}
