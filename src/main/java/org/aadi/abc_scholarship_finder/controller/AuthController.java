package org.aadi.abc_scholarship_finder.controller;

import lombok.RequiredArgsConstructor;
import org.aadi.abc_scholarship_finder.dto.LoginRequest;
import org.aadi.abc_scholarship_finder.dto.RegisterRequest;
import org.aadi.abc_scholarship_finder.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/api/auth/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, RedirectAttributes redirectAttributes) {
        try {
            String token = authService.login(loginRequest);
            // In a real application, you'd send this token to the client for subsequent requests.
            // For Thymeleaf, we'll store it in a cookie or display it for demo purposes,
            // and for API calls from JS, it would be included in the Authorization header.
            redirectAttributes.addFlashAttribute("jwtToken", token);
            redirectAttributes.addFlashAttribute("successMessage", "Login successful! Welcome back.");
            return "redirect:/search"; // Redirect to a protected page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/api/auth/register")
    public String register(@ModelAttribute("registerRequest") RegisterRequest registerRequest, RedirectAttributes redirectAttributes) {
        try {
            String token = authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("jwtToken", token);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You are now logged in.");
            return "redirect:/search"; // Redirect to a protected page
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}