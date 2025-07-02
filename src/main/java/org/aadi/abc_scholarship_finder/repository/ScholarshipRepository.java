package org.aadi.abc_scholarship_finder.repository;

import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScholarshipRepository extends JpaRepository<Scholarship, UUID> {
    List<Scholarship> findByIsActiveTrue();

    // Custom query for filtering, allowing partial matches and case-insensitivity
    @Query("SELECT s FROM Scholarship s WHERE " +
            "(LOWER(s.country) LIKE LOWER(CONCAT('%', :country, '%')) OR :country IS NULL OR :country = '') AND " +
            "(LOWER(s.fieldOfStudy) LIKE LOWER(CONCAT('%', :field, '%')) OR :field IS NULL OR :field = '') AND " +
            "(LOWER(s.educationLevel) LIKE LOWER(CONCAT('%', :educationLevel, '%')) OR :educationLevel IS NULL OR :educationLevel = '') AND " +
            "s.isActive = TRUE")
    List<Scholarship> findByCountryContainingIgnoreCaseAndFieldOfStudyContainingIgnoreCaseAndEducationLevelContainingIgnoreCase(
            @Param("country") String country,
            @Param("field") String field,
            @Param("educationLevel") String educationLevel
    );
}