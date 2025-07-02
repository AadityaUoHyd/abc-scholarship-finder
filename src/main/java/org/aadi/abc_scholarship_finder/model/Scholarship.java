package org.aadi.abc_scholarship_finder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scholarships")
public class Scholarship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "amount_inr", precision = 18, scale = 2)
    private BigDecimal amountInr; // Scholarship amount in Indian Rupees
    private LocalDate deadline;
    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;
    private String country;
    private String fieldOfStudy;
    private String educationLevel;
    private String provider;
    private String websiteUrl;
    private boolean isActive;
    @ManyToMany(mappedBy = "favoriteScholarships")
    private Set<User> favoritedBy = new HashSet<>();

    // Constructor for AI-parsed scholarships
    public Scholarship(String name, String provider, BigDecimal amountInr, LocalDate deadline,
                       String eligibilityCriteria, String country, String fieldOfStudy,
                       String educationLevel, String websiteUrl, boolean isActive) {
        this.name = name;
        this.provider = provider;
        this.amountInr = amountInr;
        this.deadline = deadline;
        this.eligibilityCriteria = eligibilityCriteria;
        this.country = country;
        this.fieldOfStudy = fieldOfStudy;
        this.educationLevel = educationLevel;
        this.websiteUrl = websiteUrl;
        this.isActive = isActive;
    }
}
