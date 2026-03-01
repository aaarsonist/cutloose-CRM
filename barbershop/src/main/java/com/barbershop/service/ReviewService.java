package com.barbershop.service;

import com.barbershop.model.Review;
import java.util.List;

public interface ReviewService {
    List<Review> getAllReviews();
    Review addReview(String reviewText, int rating, Long appointmentId);
    void deleteReview(Long id);
}
