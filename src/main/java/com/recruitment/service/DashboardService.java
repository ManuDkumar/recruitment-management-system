package com.recruitment.service;

import com.recruitment.dto.DashboardStatsResponse;
import com.recruitment.enums.ApplicationStatus;
import com.recruitment.enums.InterviewStatus;
import com.recruitment.enums.JobStatus;
import com.recruitment.enums.OfferStatus;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.repository.FeedbackRepository;
import com.recruitment.repository.InterviewRepository;
import com.recruitment.repository.JobPostingRepository;
import com.recruitment.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final FeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        Map<ApplicationStatus, Long> byStatus = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status, applicationRepository.countByStatus(status));
        }

        Double avg = feedbackRepository.averageRating();
        BigDecimal averageRating = avg == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);

        return new DashboardStatsResponse(
                companyRepository.count(),
                jobPostingRepository.count(),
                jobPostingRepository.countByStatus(JobStatus.OPEN),
                jobPostingRepository.countByStatus(JobStatus.CLOSED),
                jobPostingRepository.countByStatus(JobStatus.DRAFT),
                candidateRepository.count(),
                applicationRepository.count(),
                byStatus,
                offerRepository.countByStatus(OfferStatus.PENDING),
                offerRepository.countByStatus(OfferStatus.ACCEPTED),
                offerRepository.countByStatus(OfferStatus.DECLINED),
                averageRating,
                interviewRepository.countByStatus(InterviewStatus.SCHEDULED),
                interviewRepository.countByStatus(InterviewStatus.COMPLETED),
                interviewRepository.countByStatus(InterviewStatus.CANCELLED)
        );
    }
}
