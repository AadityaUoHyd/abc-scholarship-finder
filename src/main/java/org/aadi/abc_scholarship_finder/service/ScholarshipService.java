package org.aadi.abc_scholarship_finder.service;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.aadi.abc_scholarship_finder.repository.ScholarshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;

    public List<Scholarship> getAllActiveScholarships() {
        return scholarshipRepository.findByIsActiveTrue();
    }

    // This method will be used to format scholarships for AI prompt
    public String formatScholarshipsForAI(List<Scholarship> scholarships) {
        if (scholarships == null || scholarships.isEmpty()) {
            return "No scholarships found.";
        }
        return scholarships.stream()
                .map(s -> String.format(
                        "Name: %s, Description: %s, Amount: INR %.2f, Deadline: %s, Eligibility: %s, Country: %s, Field: %s, Education Level: %s, Provider: %s, Website: %s",
                        s.getName(), s.getDescription(), s.getAmountInr(), s.getDeadline(),
                        s.getEligibilityCriteria(), s.getCountry(), s.getFieldOfStudy(),
                        s.getEducationLevel(), s.getProvider(), s.getWebsiteUrl()
                ))
                .collect(Collectors.joining("\n---\n")); // Separator for clarity for AI
    }

    public List<Scholarship> findScholarships(String country, String field, String educationLevel) {
        return scholarshipRepository.findByCountryContainingIgnoreCaseAndFieldOfStudyContainingIgnoreCaseAndEducationLevelContainingIgnoreCase(country, field, educationLevel);
    }

    public Optional<Scholarship> getScholarshipById(UUID id) {
        return scholarshipRepository.findById(id);
    }
}