package org.aadi.abc_scholarship_finder.service;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.aadi.abc_scholarship_finder.model.User;
import org.aadi.abc_scholarship_finder.repository.ScholarshipRepository;
import org.aadi.abc_scholarship_finder.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final UserRepository userRepository;

    public List<Scholarship> getAllActiveScholarships() {
        return scholarshipRepository.findByIsActiveTrue();
    }

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
                .collect(Collectors.joining("\n---\n"));
    }

    public List<Scholarship> findScholarships(String country, String field, String educationLevel) {
        return scholarshipRepository.findByCountryContainingIgnoreCaseAndFieldOfStudyContainingIgnoreCaseAndEducationLevelContainingIgnoreCase(country, field, educationLevel);
    }

    public Optional<Scholarship> getScholarshipById(UUID id) {
        return scholarshipRepository.findById(id);
    }

    public Scholarship saveScholarship(Scholarship scholarship) {
        return scholarshipRepository.save(scholarship);
    }

    public void addFavoriteScholarship(UUID userId, UUID scholarshipId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId).orElseThrow(() -> new RuntimeException("Scholarship not found"));
        user.addFavorite(scholarship);
        userRepository.save(user);
    }

    public void removeFavoriteScholarship(UUID userId, UUID scholarshipId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId).orElseThrow(() -> new RuntimeException("Scholarship not found"));
        user.removeFavorite(scholarship);
        userRepository.save(user);
    }

    public List<Scholarship> getFavoriteScholarships(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new ArrayList<>(user.getFavoriteScholarships());
    }
}
