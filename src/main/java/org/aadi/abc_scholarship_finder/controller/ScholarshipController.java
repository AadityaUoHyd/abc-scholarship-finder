package org.aadi.abc_scholarship_finder.controller;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.aadi.abc_scholarship_finder.model.User;
import org.aadi.abc_scholarship_finder.service.GoogleAiService;
import org.aadi.abc_scholarship_finder.service.ScholarshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final GoogleAiService googleAiService;
    private static final Logger logger = LoggerFactory.getLogger(ScholarshipController.class);

    @GetMapping
    public String getAllScholarships(@AuthenticationPrincipal User currentUser, Model model) {
        logger.info("Fetching scholarships for user: {}", currentUser != null ? currentUser.getUsername() : "Not authenticated");
        List<Scholarship> scholarships = scholarshipService.getAllActiveScholarships();
        logger.info("Retrieved {} scholarships", scholarships.size());
        model.addAttribute("scholarships", scholarships);
        model.addAttribute("currentUser", currentUser);
        return "scholarships";
    }

    @GetMapping("/{id}")
    public String getScholarshipDetails(@PathVariable UUID id, Model model) {
        Optional<Scholarship> scholarship = scholarshipService.getScholarshipById(id);
        if (scholarship.isPresent()) {
            model.addAttribute("scholarship", scholarship.get());
            return "scholarship-detail";
        }
        return "redirect:/scholarships";
    }

    @GetMapping("/search")
    public String searchScholarships(
            @RequestParam(required = false, defaultValue = "") String country,
            @RequestParam(required = false, defaultValue = "") String field,
            @RequestParam(required = false, defaultValue = "") String educationLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            Model model) {
        List<Scholarship> scholarships;
        if (country.isEmpty() && field.isEmpty() && educationLevel.isEmpty()) {
            scholarships = scholarshipService.getAllActiveScholarships();
        } else {
            scholarships = scholarshipService.findScholarships(country, field, educationLevel);
        }
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), scholarships.size());
        List<Scholarship> pagedScholarships = scholarships.subList(start, end);
        Page<Scholarship> scholarshipPage = new PageImpl<>(pagedScholarships, pageable, scholarships.size());

        model.addAttribute("scholarships", scholarshipPage);
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

            String aiRecommendations = googleAiService.generateScholarshipRecommendations(
                    userBackground, userGoals, userInterests, formattedScholarships
            ).block();
            redirectAttributes.addFlashAttribute("aiRecommendations", aiRecommendations);
            redirectAttributes.addFlashAttribute("successMessage", "AI recommendations generated!");
        } catch (Exception e) {
            logger.error("Error generating AI recommendations: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error generating AI recommendations: " + e.getMessage());
        }
        return "redirect:/scholarships/search";
    }

    @PostMapping("/search/ai")
    public String searchGlobalScholarships(
            @RequestParam("userBackground") String userBackground,
            @RequestParam("userGoals") String userGoals,
            @RequestParam("userInterests") String userInterests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to use AI search.");
            return "redirect:/login";
        }
        try {
            List<Scholarship> aiResults = (List<Scholarship>) googleAiService.searchGlobalScholarships(
                    userBackground, userGoals, userInterests
            ).block();
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), aiResults.size());
            List<Scholarship> pagedAiResults = aiResults.subList(start, end);
            Page<Scholarship> aiSearchPage = new PageImpl<>(pagedAiResults, pageable, aiResults.size());

            redirectAttributes.addFlashAttribute("aiSearchResults", aiSearchPage);
            redirectAttributes.addFlashAttribute("successMessage", "Global AI search completed!");
        } catch (Exception e) {
            logger.error("Error searching global scholarships: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error searching global scholarships: " + e.getMessage());
        }
        return "redirect:/scholarships/search?page=" + page + "&size=" + size;
    }

    @PostMapping("/{id}/favorite")
    public String addFavorite(@PathVariable UUID id, @AuthenticationPrincipal User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to favorite scholarships.");
            return "redirect:/login";
        }
        scholarshipService.addFavoriteScholarship(currentUser.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Scholarship added to favorites!");
        return "redirect:/scholarships/" + id;
    }

    @PostMapping("/{id}/unfavorite")
    public String removeFavorite(@PathVariable UUID id, @AuthenticationPrincipal User currentUser, RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to unfavorite scholarships.");
            return "redirect:/login";
        }
        scholarshipService.removeFavoriteScholarship(currentUser.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Scholarship removed from favorites!");
        return "redirect:/scholarships/" + id;
    }

    @GetMapping("/favorites")
    public String getFavoriteScholarships(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<Scholarship> favorites = scholarshipService.getFavoriteScholarships(currentUser.getId());
        model.addAttribute("favorites", favorites);
        return "favourites";
    }
}
