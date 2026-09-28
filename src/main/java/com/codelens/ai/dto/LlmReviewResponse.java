package com.codelens.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class LlmReviewResponse {
    private String summary;
    private List<LlmIssue> issues;
    private String improvedCode;
    private String improvementExplanation;

    @Data
    public static class LlmIssue {
        private String category;
        private String severity;
        private Integer line;
        private String title;
        private String description;
        private String explanation;
        private String suggestedFix;
    }
}
