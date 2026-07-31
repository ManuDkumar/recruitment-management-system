package com.recruitment.service;

import com.recruitment.dto.CandidateRequest;
import com.recruitment.dto.CandidateResponse;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.CandidateMapper;
import com.recruitment.model.Candidate;
import com.recruitment.model.User;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CandidateMapper candidateMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public CandidateResponse getOrCreate(String email) {
        return candidateMapper.toResponse(getOrCreateEntity(email));
    }

    @Transactional
    public CandidateResponse update(String email, CandidateRequest request) {
        Candidate candidate = getOrCreateEntity(email);
        candidateMapper.updateEntity(request, candidate);
        return candidateMapper.toResponse(candidateRepository.save(candidate));
    }

    @Transactional
    public CandidateResponse uploadResume(String email, MultipartFile file) {
        validateResume(file);
        Candidate candidate = getOrCreateEntity(email);
        if (candidate.getResumePath() != null) {
            fileStorageService.delete(candidate.getResumePath());
        }
        candidate.setResumePath(fileStorageService.store(file, "candidate-" + candidate.getId()));
        return candidateMapper.toResponse(candidateRepository.save(candidate));
    }

    @Transactional(readOnly = true)
    public Candidate getEntityByEmail(String email) {
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Candidate profile not found for email: " + email));
    }

    public ResumeFile loadResume(String email) {
        Candidate candidate = getEntityByEmail(email);
        if (candidate.getResumePath() == null) {
            throw new NotFoundException("No resume uploaded for candidate: " + email);
        }
        return new ResumeFile(fileStorageService.loadAsResource(candidate.getResumePath()), candidate.getResumePath());
    }

    public record ResumeFile(Resource resource, String filename) {}

    public Candidate getOrCreateEntity(String email) {
        return candidateRepository.findByEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
            Candidate candidate = new Candidate();
            candidate.setEmail(email);
            applyName(candidate, user.getName());
            return candidateRepository.save(candidate);
        });
    }

    private void applyName(Candidate candidate, String name) {
        if (name == null || name.isBlank()) {
            candidate.setFirstName("Unknown");
            candidate.setLastName("");
            return;
        }
        String[] parts = name.trim().split("\\s+");
        candidate.setFirstName(parts[0]);
        candidate.setLastName(parts.length > 1 ? name.trim().substring(parts[0].length()).trim() : "");
    }

    private void validateResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is empty");
        }
        String filename = file.getOriginalFilename();
        String ext = filename == null || !filename.contains(".")
                ? "" : filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Only PDF, DOC, DOCX files are allowed");
        }
    }
}
