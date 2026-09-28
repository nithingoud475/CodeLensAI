package com.codelens.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "review_issues")
@Data
public class ReviewIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Review review;

    private String category;
    private String severity;

    @Column(name = "line_number")
    private Integer lineNumber;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;
}
