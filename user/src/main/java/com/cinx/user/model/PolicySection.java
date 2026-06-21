package com.cinx.user.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "policy_sections",
        indexes = @Index(name = "idx_policy_section_document", columnList = "document_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicySection extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private PolicyDocument document;

    @Column(nullable = false, length = 255)
    private String heading;

    @Column(nullable = false, length = 255)
    private String anchor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyMarkdown;

    @Column(nullable = false)
    private Integer orderIndex;
}
