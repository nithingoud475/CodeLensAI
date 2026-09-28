package com.codelens.ai.repository;

import com.codelens.ai.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
}
