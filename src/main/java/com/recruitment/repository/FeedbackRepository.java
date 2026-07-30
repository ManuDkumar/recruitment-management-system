package com.recruitment.repository;

import com.recruitment.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByInterviewId(Long interviewId);
    List<Feedback> findByUserId(Long userId);
}
