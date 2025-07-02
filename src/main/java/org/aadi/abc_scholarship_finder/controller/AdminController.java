package org.aadi.abc_scholarship_finder.controller;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.Scholarship;
import org.aadi.abc_scholarship_finder.model.User;
import org.aadi.abc_scholarship_finder.service.ScholarshipService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final ScholarshipService scholarshipService;

    @GetMapping("/scholarships/add")
    public String showAddScholarshipForm(Model model) {
        model.addAttribute("scholarship", new Scholarship());
        return "admin/add-scholarship";
    }

    @PostMapping("/scholarships/add")
    public String addScholarship(@ModelAttribute("scholarship") Scholarship scholarship,
                                 @AuthenticationPrincipal User currentUser,
                                 RedirectAttributes redirectAttributes) {
        if (!currentUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized access");
            return "redirect:/scholarships";
        }
        scholarship.setActive(true);
        scholarshipService.saveScholarship(scholarship);
        redirectAttributes.addFlashAttribute("successMessage", "Scholarship added successfully!");
        return "redirect:/scholarships";
    }
}