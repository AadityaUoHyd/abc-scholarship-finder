package org.aadi.abc_scholarship_finder.controller;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.model.User;
import org.aadi.abc_scholarship_finder.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @ModelAttribute User updatedUser,
                                RedirectAttributes redirectAttributes) {
        currentUser.setFirstName(updatedUser.getFirstName());
        currentUser.setLastName(updatedUser.getLastName());
        currentUser.setEducationLevel(updatedUser.getEducationLevel());
        currentUser.setMajorField(updatedUser.getMajorField());
        currentUser.setGpa(updatedUser.getGpa());
        currentUser.setCountry(updatedUser.getCountry());
        currentUser.setInterests(updatedUser.getInterests());
        currentUser.setGoals(updatedUser.getGoals());
        userRepository.save(currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/user/profile";
    }
}