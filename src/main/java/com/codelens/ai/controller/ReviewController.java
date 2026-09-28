package com.codelens.ai.controller;

import com.codelens.ai.dto.CodeReviewRequest;
import com.codelens.ai.dto.DashboardStats;
import com.codelens.ai.entity.Review;
import com.codelens.ai.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<Review> submitReview(@Valid @RequestBody CodeReviewRequest request) {
        Review review = reviewService.processReview(request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviews() {
        return ResponseEntity.ok(reviewService.getUserReviews());
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<Review> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboard() {
        return ResponseEntity.ok(reviewService.getDashboardStats());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of(
                        "success", false,
                        "message", "An internal error occurred. Please try again later."
                ));
    }
}
