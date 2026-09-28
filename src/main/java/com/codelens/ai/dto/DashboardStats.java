package com.codelens.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardStats {
    private long totalReviews;
    private long issuesFound;
    private long highIssues;
    private long criticalIssues;
    private List<ReviewSummaryDto> recentReviews;

    @Data
    @Builder
    public static class ReviewSummaryDto {
        private Long id;
        private String language;
        private String date;
    }
}
