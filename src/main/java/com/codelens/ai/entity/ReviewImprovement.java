package com.codelens.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "review_improvements")
@Data
public class ReviewImprovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Review review;

    @Column(name = "improved_code", columnDefinition = "TEXT")
    private String improvedCode;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}
