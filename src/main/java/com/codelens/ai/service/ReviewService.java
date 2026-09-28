package com.codelens.ai.service;

import com.codelens.ai.dto.CodeReviewRequest;
import com.codelens.ai.dto.DashboardStats;
import com.codelens.ai.dto.LlmReviewResponse;
import com.codelens.ai.entity.Review;
import com.codelens.ai.entity.ReviewImprovement;
import com.codelens.ai.entity.ReviewIssue;
import com.codelens.ai.entity.User;
import com.codelens.ai.llm.LlmReviewService;
import com.codelens.ai.repository.ReviewRepository;
import com.codelens.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LlmReviewService llmReviewService;

    @Autowired
    private CodeValidator codeValidator;

    @Transactional
    public Review processReview(CodeReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!codeValidator.isValidCode(request.getLanguage(), request.getSourceCode())) {
            throw new IllegalArgumentException("Invalid " + request.getLanguage() + " code. The submitted content does not appear to be valid source code.");
        }

        // Call Ollama LLM
        LlmReviewResponse llmResponse = llmReviewService.getReview(request.getLanguage(), request.getSourceCode());

        // Map to Entities
        Review review = new Review();
        review.setUserId(user.getId());
        review.setLanguage(request.getLanguage());
        review.setSourceCode(request.getSourceCode());
        review.setSummary(llmResponse.getSummary());

        List<ReviewIssue> issues = new ArrayList<>();
        if (llmResponse.getIssues() != null) {
            for (LlmReviewResponse.LlmIssue llmIssue : llmResponse.getIssues()) {
                ReviewIssue issue = new ReviewIssue();
                issue.setReview(review);
                issue.setCategory(llmIssue.getCategory());
                issue.setSeverity(llmIssue.getSeverity());
                issue.setLineNumber(llmIssue.getLine());
                issue.setTitle(llmIssue.getTitle());
                issue.setDescription(llmIssue.getDescription());
                issue.setExplanation(llmIssue.getExplanation());
                issue.setSuggestedFix(llmIssue.getSuggestedFix());
                issues.add(issue);
            }
        }
        review.setIssues(issues);

        if (llmResponse.getImprovedCode() != null && !llmResponse.getImprovedCode().isEmpty()) {
            ReviewImprovement improvement = new ReviewImprovement();
            improvement.setReview(review);
            improvement.setImprovedCode(llmResponse.getImprovedCode());
            improvement.setExplanation(llmResponse.getImprovementExplanation());
            review.setImprovement(improvement);
        }

        return reviewRepository.save(review);
    }

    public List<Review> getUserReviews() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public Review getReviewById(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to review");
        }
        return review;
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        reviewRepository.delete(review);
    }

    public DashboardStats getDashboardStats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Review> allReviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        long totalReviews = allReviews.size();
        long issuesFound = 0;
        long highIssues = 0;
        long criticalIssues = 0;

        for (Review review : allReviews) {
            for (ReviewIssue issue : review.getIssues()) {
                issuesFound++;
                if ("High".equalsIgnoreCase(issue.getSeverity())) {
                    highIssues++;
                } else if ("Critical".equalsIgnoreCase(issue.getSeverity())) {
                    criticalIssues++;
                }
            }
        }

        List<DashboardStats.ReviewSummaryDto> recent = allReviews.stream()
                .limit(5)
                .map(r -> DashboardStats.ReviewSummaryDto.builder()
                        .id(r.getId())
                        .language(r.getLanguage())
                        .date(r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                        .build())
                .collect(Collectors.toList());

        return DashboardStats.builder()
                .totalReviews(totalReviews)
                .issuesFound(issuesFound)
                .highIssues(highIssues)
                .criticalIssues(criticalIssues)
                .recentReviews(recent)
                .build();
    }
}
