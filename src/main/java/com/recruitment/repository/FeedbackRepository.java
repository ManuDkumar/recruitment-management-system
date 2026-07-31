package com.recruitment.repository;

import com.recruitment.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByInterviewId(Long interviewId);
    List<Feedback> findByUserId(Long userId);

    @Query("select avg(f.rating) from Feedback f")
    Double averageRating();
}
