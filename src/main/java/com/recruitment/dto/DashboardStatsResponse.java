package com.recruitment.dto;

import com.recruitment.enums.ApplicationStatus;
import com.recruitment.enums.InterviewStatus;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardStatsResponse(
        long totalCompanies,
        long totalJobs,
        long openJobs,
        long closedJobs,
        long draftJobs,
        long totalCandidates,
        long totalApplications,
        Map<ApplicationStatus, Long> applicationsByStatus,
        long pendingOffers,
        long acceptedOffers,
        long declinedOffers,
        BigDecimal averageRating,
        long scheduledInterviews,
        long completedInterviews,
        long cancelledInterviews
) {}
