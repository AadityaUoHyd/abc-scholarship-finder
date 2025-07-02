package org.aadi.abc_scholarship_finder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private boolean isActive; // To enable/disable scholarships
}