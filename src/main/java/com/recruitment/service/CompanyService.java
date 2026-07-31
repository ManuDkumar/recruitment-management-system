package com.recruitment.service;

import com.recruitment.dto.CompanyRequest;
import com.recruitment.dto.CompanyResponse;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.model.Company;
import com.recruitment.model.User;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;

    public CompanyResponse create(CompanyRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Company company = companyMapper.toEntity(request);
        company.setCreatedBy(creator);

        return companyMapper.toResponse(companyRepository.save(company));
    }

    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = getEntity(id);
        companyMapper.updateEntity(request, company);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    public CompanyResponse getById(Long id) {
        return companyMapper.toResponse(getEntity(id));
    }

    public List<CompanyResponse> getAll() {
        return companyRepository.findAll().stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    public void delete(Long id) {
        companyRepository.delete(getEntity(id));
    }

    private Company getEntity(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + id));
    }
}
