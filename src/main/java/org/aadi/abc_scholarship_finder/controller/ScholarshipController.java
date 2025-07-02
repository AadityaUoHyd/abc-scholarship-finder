package org.aadi.abc_scholarship_finder.controller;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.aadi.abc_scholarship_finder.model.User;
import org.aadi.abc_scholarship_finder.service.GoogleAiService;
import org.aadi.abc_scholarship_finder.service.ScholarshipService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/scholarships")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;
    private final GoogleAiService googleAIService;

    @GetMapping
    public String getAllScholarships(Model model) {
        List<Scholarship> scholarships = scholarshipService.getAllActiveScholarships();
        model.addAttribute("scholarships", scholarships);
        return "scholarships";
    }

    @GetMapping("/{id}")
    public String getScholarshipDetails(@PathVariable UUID id, Model model) {
        Optional<Scholarship> scholarship = scholarshipService.getScholarshipById(id);
        if (scholarship.isPresent()) {
            model.addAttribute("scholarship", scholarship.get());
            return "scholarship-detail";
        }
        return "redirect:/scholarships"; // Or an error page
    }

    @GetMapping("/search")
    public String searchScholarships(
            @RequestParam(required = false, defaultValue = "") String country,
            @RequestParam(required = false, defaultValue = "") String field,
            @RequestParam(required = false, defaultValue = "") String educationLevel,
            @AuthenticationPrincipal User currentUser,
            Model model) {
        List<Scholarship> scholarships;
        if (country.isEmpty() && field.isEmpty() && educationLevel.isEmpty()) {
            scholarships = scholarshipService.getAllActiveScholarships();
        } else {
            scholarships = scholarshipService.findScholarships(country, field, educationLevel);
        }
        model.addAttribute("scholarships", scholarships);
        model.addAttribute("country", country);
        model.addAttribute("field", field);
        model.addAttribute("educationLevel", educationLevel);
        model.addAttribute("currentUser", currentUser);
        return "search";
    }

    @PostMapping("/search/recommend")
    public String recommendScholarships(
            @RequestParam("userBackground") String userBackground,
            @RequestParam("userGoals") String userGoals,
            @RequestParam("userInterests") String userInterests,
            RedirectAttributes redirectAttributes) {

        try {
            List<Scholarship> allScholarships = scholarshipService.getAllActiveScholarships();
            String formattedScholarships = scholarshipService.formatScholarshipsForAI(allScholarships);

            String aiRecommendations = String.valueOf(googleAIService.generateScholarshipRecommendations(
                    userBackground, userGoals, userInterests, formattedScholarships
            ));
            redirectAttributes.addFlashAttribute("aiRecommendations", aiRecommendations);
            redirectAttributes.addFlashAttribute("successMessage", "AI recommendations generated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error generating AI recommendations: " + e.getMessage());
        }
        return "redirect:/scholarships/search";
    }
}