package com.recruitment.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Integer rating,
        String comments,
        LocalDateTime createdAt,
        Long interviewId,
        Long userId,
        String userName
) {}
