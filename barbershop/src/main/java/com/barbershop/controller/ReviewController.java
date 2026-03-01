package com.barbershop.controller;

import com.barbershop.model.Review;
import com.barbershop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
    @PostMapping

    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty() ||
                review.getRating() < 1 || review.getRating() > 5 ||
                review.getAppointment() == null || review.getAppointment().getId() == null) {
            System.err.println("Validation failed: reviewText, rating, or appointment ID missing/invalid in request body.");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Review savedReview = reviewService.addReview(review.getReviewText(), review.getRating(), review.getAppointment().getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (RuntimeException e) {
            System.err.println("Error adding review: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}
