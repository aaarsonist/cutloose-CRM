package com.barbershop.impl;

import com.barbershop.model.Review;
import com.barbershop.model.Timetable;
import com.barbershop.repository.ReviewRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.service.ReviewService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TimetableRepository timetableRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, TimetableRepository timetableRepository) {
        this.reviewRepository = reviewRepository;
        this.timetableRepository = timetableRepository;
    }
    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    @Transactional
    public Review addReview(String reviewText, int rating, Long appointmentId) {

        Timetable appointment = timetableRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Запись расписания с ID " + appointmentId + " не найдена."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Нельзя оставить отзыв на предстоящую запись.");
        }

        if (appointment.getBookedBy() == null || !appointment.getBookedBy().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Вы можете оставить отзыв только на свои записи.");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Оценка должна быть от 1 до 5.");
        }

        Review newReview = new Review();
        newReview.setReviewText(reviewText);
        newReview.setRating(rating);
        newReview.setAppointment(appointment);

        return reviewRepository.save(newReview);
    }
    @Override
    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
